package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Caja;
import com.proyecto.neotec.models.HistorialAperturaCierre;
import com.proyecto.neotec.models.MovimientosCaja;
import com.proyecto.neotec.models.Usuario;
import com.proyecto.neotec.util.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CajaDAO {

    public List<Integer> selectIdCajas() {
        List<Integer> idcajas = new ArrayList<>();
        String sql = "SELECT idcaja FROM cajas";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idcaja = rs.getInt("idcaja");
                idcajas.add(idcaja);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return idcajas;
    }

    public Caja selectCajaPorId(int idcaja) {
        Caja caja = new Caja();
        String sql = "SELECT * FROM cajas WHERE idcaja = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja); // Configura el parámetro antes de ejecutar la consulta
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    caja.setIdcaja(rs.getInt("idcaja"));
                    String estado = (rs.getInt("estado") == 0) ? "Cerrada" : "Abierta";
                    caja.setEstado(estado);
                    caja.setSaldoActual(rs.getFloat("saldoActual"));
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return caja;
    }

    public String obtenerUltimoCambio(int idcaja) {
        String fechaHora = "";
        String sql = "SELECT fechaHora FROM cajaaperturacierre WHERE idcaja = ? ORDER BY fechaHora DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja); // Configura el parámetro antes de ejecutar la consulta
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { // Usa if ya que solo se espera un resultado
                    fechaHora = rs.getString("fechaHora");
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return fechaHora;
    }

    public String abrirCerrarCaja(Caja cajaSeleccionada, int apCi) {
        String mensaje = "";

        // SQL para insertar en cajaaperturacierre
        String sqlInsert = "INSERT INTO cajaaperturacierre (idcaja, aperturaCierre, fechaHora, responsable, saldo) " +
                "VALUES (?, ?, NOW(), ?, ?)";
        // SQL para actualizar el estado de la caja
        String sqlUpdate = "UPDATE cajas SET estado = ? WHERE idcaja = ?";

        try (Connection conn = Database.getConnection()) {
            // Deshabilitar auto-commit para asegurar consistencia
            conn.setAutoCommit(false);

            // Insertar en cajaaperturacierre
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setInt(1, cajaSeleccionada.getIdcaja());
                stmtInsert.setInt(2, apCi);
                System.out.println(SesionUsuario.getUsuarioLogueado().getIdusuarios());
                stmtInsert.setInt(3, SesionUsuario.getUsuarioLogueado().getIdusuarios());
                stmtInsert.setFloat(4, cajaSeleccionada.getSaldoActual());

                int rowsInserted = stmtInsert.executeUpdate();
                if (rowsInserted == 0) {
                    conn.rollback();
                    return "No se pudo realizar la operación.";
                }
            }

            // Actualizar el estado de la caja
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                stmtUpdate.setInt(1, apCi);
                stmtUpdate.setInt(2, cajaSeleccionada.getIdcaja());

                int rowsUpdated = stmtUpdate.executeUpdate();
                if (rowsUpdated == 0) {
                    conn.rollback();
                    return "No se pudo realizar la operación.";
                }
            }

            // Confirmar transacción
            conn.commit();
            mensaje = "Operación Exitosa!";
        } catch (SQLException e) {
            Database.handleSQLException(e);
            mensaje = "No se pudo realizar la operación.";
        }

        return mensaje;
    }

    public List<HistorialAperturaCierre> cargarHistorialDeAperturasyCierres(int idcaja) {
        String sql = "SELECT * FROM cajaaperturacierre WHERE idcaja = ?";
        List<HistorialAperturaCierre> datoshistorial = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja); // Configurar el parámetro antes de ejecutar la consulta
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String operacion = rs.getInt("aperturaCierre") == 0 ? "Cierre" : "Apertura";
                String fechaHora = rs.getString("fechaHora");
                String saldo = rs.getString("saldo");
                int idResponsable = rs.getInt("responsable");

                // Obtener el objeto Usuario desde su ID
                Usuario responsable = UsuarioDAO.obtenerUsuarioPorId(idResponsable);

                // Crear el objeto con la información de una fila
                HistorialAperturaCierre registro = new HistorialAperturaCierre(operacion, fechaHora, saldo, responsable);
                datoshistorial.add(registro);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return datoshistorial;
    }

    public List<MovimientosCaja> cargarhistorialDeMovimientosConFiltros(Integer idcaja, Integer entradaSalida, LocalDate desde, LocalDate hasta, Integer tipo) {
        List<MovimientosCaja> movimientos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT cm.idcajaMovimientos, cm.entradaSalida, cm.monto, cm.fechaHora, " +
                "u.idusuarios AS dniResponsable, u.nombre AS nombreResponsable, t.descripcion AS tipoMovimiento " +
                "FROM cajamovimientos cm " +
                "JOIN usuarios u ON cm.responsable = u.idusuarios " +
                "JOIN tipocajamovimiento t ON cm.tipoMovimiento = t.idtipocajamovimiento " +
                "WHERE 1=1 ");

        // Construir la consulta dinámica según los parámetros
        if (idcaja != null) {
            sql.append("AND cm.idcaja = ? ");
        }
        if (entradaSalida != null && entradaSalida >= 0) {
            sql.append("AND cm.entradaSalida = ? ");
        }
        if (desde != null) {
            sql.append("AND cm.fechaHora >= ? ");
        }
        if (hasta != null) {
            sql.append("AND cm.fechaHora <= ? ");
        }
        if (tipo != null && tipo > 0) {
            sql.append("AND cm.tipoMovimiento = ? ");
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            // Establecer parámetros dinámicos
            if (idcaja != null) {
                ps.setInt(paramIndex++, idcaja);
            }
            if (entradaSalida != null && entradaSalida >= 0) {
                ps.setInt(paramIndex++, entradaSalida);
            }
            if (desde != null) {
                ps.setString(paramIndex++, desde.toString() + " 00:00:00");
            }
            if (hasta != null) {
                ps.setString(paramIndex++, hasta.toString() + " 23:59:59");
            }
            if (tipo != null && tipo > 0) {
                ps.setInt(paramIndex++, tipo);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientosCaja movimiento = new MovimientosCaja();
                    movimiento.setIdcajaMovimientos(rs.getInt("idcajaMovimientos"));
                    movimiento.setEntradaSalida(rs.getInt("entradaSalida") == 1 ? "Entrada" : "Salida");
                    movimiento.setMonto(rs.getFloat("monto"));
                    movimiento.setFechaHora(rs.getString("fechaHora"));
                    movimiento.setDniResponsable(rs.getInt("dniResponsable"));
                    movimiento.setNombreResponsable(rs.getString("nombreResponsable"));
                    movimiento.setTipoMovimiento(rs.getString("tipoMovimiento"));

                    movimientos.add(movimiento);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return movimientos;
    }

    public List<String> selectTiposDeMovimiento(int tipo) {
        List<String> tiposMovimientos = new ArrayList<>();
        String sql = "SELECT descripcion FROM tipocajamovimiento WHERE entradaSalida = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipom = rs.getString("descripcion");
                    tiposMovimientos.add(tipom);
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return tiposMovimientos;
    }

    public List<String> selectTiposDeMovimiento() {

        List<String> tiposMovimientos = new ArrayList<>();
        String sql = "SELECT descripcion FROM tipocajamovimiento";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String tipom = rs.getString("descripcion");
                tiposMovimientos.add(tipom);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return tiposMovimientos;
    }


    public String registrarMovimientoDeCaja(int idcaja, int entradaSalida, float monto, int idusuarios, String opcion) {
        String sql = "INSERT INTO cajamovimientos (idcaja, entradaSalida, monto, fechaHora, responsable, tipoMovimiento) "
                + "VALUES (?, ?, ?, NOW(), ?, ?)";
        String mensaje = "Registro exitoso";

        // Obtener el ID del tipo de movimiento basado en la descripción (opción)
        String tipoMovimientoSql = "SELECT idtipocajamovimiento FROM tipocajamovimiento WHERE descripcion = ?";
        int tipoMovimientoId = -1;

        try (Connection conn = Database.getConnection();
             PreparedStatement tipoStmt = conn.prepareStatement(tipoMovimientoSql)) {

            // Buscar el ID del tipo de movimiento
            tipoStmt.setString(1, opcion);
            try (ResultSet rs = tipoStmt.executeQuery()) {
                if (rs.next()) {
                    tipoMovimientoId = rs.getInt("idtipocajamovimiento");
                } else {
                    return "Error: El tipo de movimiento especificado no existe.";
                }
            }

            // Insertar el movimiento en la tabla `cajamovimientos`
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idcaja);
                stmt.setInt(2, entradaSalida);
                stmt.setFloat(3, monto);
                stmt.setInt(4, idusuarios);
                stmt.setInt(5, tipoMovimientoId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    mensaje = "Error: No se pudo registrar el movimiento.";
                }
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
            mensaje = "Error: Ocurrió un problema al registrar el movimiento.";
        }

        return mensaje;
    }

    public Boolean verificarApertura(int idcaja) {
        String sql = "SELECT estado FROM cajas WHERE idcaja = ?";
        Boolean estadoAbierto = null; // Usamos null para diferenciar errores de otros valores.

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    estadoAbierto = rs.getInt("estado") == 1;
                } else {
                    estadoAbierto = false; // Caja no encontrada
                }
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
            estadoAbierto = false; // Asumimos false en caso de error
        }

        return estadoAbierto;
    }

    public Boolean verificarMonto(int idcaja, float monto) {
        String sql = "SELECT saldoActual FROM cajas WHERE idcaja = ?";
        Boolean saldoSuficiente = null; // Usamos null para diferenciar errores de otros valores.

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double saldoActual = rs.getFloat("saldoActual");
                    saldoSuficiente = saldoActual >= monto;
                } else {
                    saldoSuficiente = false; // Caja no encontrada
                }
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
            saldoSuficiente = false; // Asumimos false en caso de error
        }

        return saldoSuficiente;
    }

}


