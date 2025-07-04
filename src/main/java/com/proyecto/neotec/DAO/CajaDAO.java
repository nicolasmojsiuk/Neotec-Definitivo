package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Caja;
import com.proyecto.neotec.models.HistorialAperturaCierre;
import com.proyecto.neotec.models.MovimientosCaja;
import com.proyecto.neotec.models.Usuario;
import com.proyecto.neotec.util.SesionUsuario;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CajaDAO {
    private static final Logger logger = Logger.getLogger(CajaDAO.class);

    public List<Integer> selectIdCajas() {
        List<Integer> idcajas = new ArrayList<>();
        String sql = "SELECT idcaja FROM cajas";

        logger.info("Iniciando selección de IDs de cajas.");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idcaja = rs.getInt("idcaja");
                idcajas.add(idcaja);
                logger.debug("ID de caja encontrado y agregado: "+ idcaja);
            }
            logger.info("Selección de IDs de cajas completada. Se encontraron "+ idcajas.size()+ " IDs.");

        } catch (SQLException e) {
            logger.error("Error al seleccionar los IDs de cajas. Error: "+e.getMessage()+" - SQLState: {}"+e.getSQLState(), e);
            Database.handleSQLException(e);
        }
        return idcajas;
    }

    public Caja selectCajaPorId(int idcaja) {
        Caja caja = new Caja();
        String sql = "SELECT * FROM cajas WHERE idcaja = ?";

        logger.info("Iniciando selección de caja por ID: " + idcaja);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja);
            logger.debug("Parámetro establecido - ID de caja: " + idcaja);

            try (ResultSet rs = stmt.executeQuery()) {
                logger.debug("Consulta ejecutada para ID de caja: " + idcaja + ", procesando resultados...");
                if (rs.next()) {
                    caja.setIdcaja(rs.getInt("idcaja"));
                    String estado = (rs.getInt("estado") == 0) ? "Cerrada" : "Abierta";
                    caja.setEstado(estado);
                    caja.setSaldoActual(rs.getFloat("saldoActual"));
                    logger.info("Caja encontrada y datos mapeados para ID: " + idcaja);
                } else {
                    logger.warn("No se encontró ninguna caja con el ID: " + idcaja);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al seleccionar caja por ID: " + idcaja + " - Error: " + e.getMessage(), e);
            Database.handleSQLException(e);
        }
        return caja;
    }

    public String obtenerUltimoCambio(int idcaja) {
        String fechaHora = "";
        String sql = "SELECT fechaHora FROM cajaaperturacierre WHERE idcaja = ? ORDER BY fechaHora DESC LIMIT 1";

        logger.info("Iniciando obtención del último cambio para caja con ID: " + idcaja);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja);
            logger.debug("Parámetro establecido - ID de caja: " + idcaja);

            try (ResultSet rs = stmt.executeQuery()) {
                logger.debug("Consulta ejecutada para obtener último cambio de ID de caja: " + idcaja + ", procesando resultados...");
                if (rs.next()) {
                    fechaHora = rs.getString("fechaHora");
                    logger.info("Última fecha y hora de cambio obtenida para ID de caja " + idcaja + ": " + fechaHora);
                } else {
                    logger.warn("No se encontró ningún registro de cambio para el ID de caja: " + idcaja);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener el último cambio para ID de caja: " + idcaja + " - Error: " + e.getMessage(), e);
            Database.handleSQLException(e);
        }
        return fechaHora;
    }

    public String abrirCerrarCaja(Caja cajaSeleccionada, int apCi) {
        logger.info("Iniciando método abrirCerrarCaja para caja ID: " + cajaSeleccionada.getIdcaja() + ", operación: " + (apCi == 1 ? "Apertura" : "Cierre"));

        String mensaje = "";

        String sqlInsert = "INSERT INTO cajaaperturacierre (idcaja, aperturaCierre, fechaHora, responsable, saldo) " +
                "VALUES (?, ?, NOW(), ?, ?)";
        String sqlUpdate = "UPDATE cajas SET estado = ? WHERE idcaja = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Deshabilitar auto-commit

            // Insertar en cajaaperturacierre
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                stmtInsert.setInt(1, cajaSeleccionada.getIdcaja());
                stmtInsert.setInt(2, apCi);

                int idUsuario = SesionUsuario.getUsuarioLogueado().getIdusuarios();
                logger.debug("Responsable (usuario logueado) ID: " + idUsuario);
                stmtInsert.setInt(3, idUsuario);
                stmtInsert.setFloat(4, cajaSeleccionada.getSaldoActual());

                int rowsInserted = stmtInsert.executeUpdate();
                if (rowsInserted == 0) {
                    conn.rollback();
                    logger.warn("No se insertó registro en cajaaperturacierre. Se realizó rollback.");
                    return "No se pudo realizar la operación.";
                }
            }

            // Actualizar estado de la caja
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                stmtUpdate.setInt(1, apCi);
                stmtUpdate.setInt(2, cajaSeleccionada.getIdcaja());

                int rowsUpdated = stmtUpdate.executeUpdate();
                if (rowsUpdated == 0) {
                    conn.rollback();
                    logger.warn("No se actualizó estado de caja. Se realizó rollback.");
                    return "No se pudo realizar la operación.";
                }
            }

            conn.commit();
            mensaje = "Operación Exitosa!";
            logger.info("Caja ID " + cajaSeleccionada.getIdcaja() + " actualizada correctamente. Transacción confirmada.");

        } catch (SQLException e) {
            logger.error("Error al abrir/cerrar la caja ID: " + cajaSeleccionada.getIdcaja(), e);
            Database.handleSQLException(e);
            mensaje = "No se pudo realizar la operación.";
        }

        logger.info("Finalizando método abrirCerrarCaja para caja ID: " + cajaSeleccionada.getIdcaja());
        return mensaje;
    }


    public List<HistorialAperturaCierre> cargarHistorialDeAperturasyCierres(int idcaja) {
        logger.info("Iniciando método cargarHistorialDeAperturasyCierres para caja ID: " + idcaja);

        String sql = "SELECT * FROM cajaaperturacierre WHERE idcaja = ?";
        List<HistorialAperturaCierre> datoshistorial = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja);
            ResultSet rs = stmt.executeQuery();
            logger.debug("Consulta ejecutada para historial de caja ID: " + idcaja);

            while (rs.next()) {
                String operacion = rs.getInt("aperturaCierre") == 0 ? "Cierre" : "Apertura";
                String fechaHora = rs.getString("fechaHora");
                String saldo = rs.getString("saldo");
                int idResponsable = rs.getInt("responsable");

                Usuario responsable = UsuarioDAO.obtenerUsuarioPorId(idResponsable);

                HistorialAperturaCierre registro = new HistorialAperturaCierre(operacion, fechaHora, saldo, responsable);
                datoshistorial.add(registro);

                logger.debug("Registro agregado: [" + operacion + "] - " + fechaHora + " - Saldo: " + saldo + " - Responsable ID: " + idResponsable);
            }

            logger.info("Historial cargado. Total de registros: " + datoshistorial.size());

        } catch (SQLException e) {
            logger.error("Error al cargar historial de caja ID: " + idcaja, e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método cargarHistorialDeAperturasyCierres para caja ID: " + idcaja);
        return datoshistorial;
    }


    public List<MovimientosCaja> cargarhistorialDeMovimientosConFiltros(Integer idcaja, Integer entradaSalida, LocalDate desde, LocalDate hasta, Integer tipo) {
        logger.info("Iniciando método cargarhistorialDeMovimientosConFiltros");

        List<MovimientosCaja> movimientos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT cm.idcajaMovimientos, cm.entradaSalida, cm.monto, cm.fechaHora, " +
                "u.dni AS dniResponsable, u.nombre AS nombreResponsable, t.descripcion AS tipoMovimiento " +
                "FROM cajamovimientos cm " +
                "JOIN usuarios u ON cm.responsable = u.idusuarios " +
                "JOIN tipocajamovimiento t ON cm.tipoMovimiento = t.idtipocajamovimiento " +
                "WHERE 1=1 ");

        // Construir la consulta dinámica según los parámetros
        if (idcaja != null) {
            sql.append("AND cm.idcaja = ? ");
            logger.debug("Filtro aplicado: idcaja = " + idcaja);
        }
        if (entradaSalida != null && entradaSalida >= 0) {
            sql.append("AND cm.entradaSalida = ? ");
            logger.debug("Filtro aplicado: entradaSalida = " + entradaSalida);
        }
        if (desde != null) {
            sql.append("AND cm.fechaHora >= ? ");
            logger.debug("Filtro aplicado: desde = " + desde);
        }
        if (hasta != null) {
            sql.append("AND cm.fechaHora <= ? ");
            logger.debug("Filtro aplicado: hasta = " + hasta);
        }
        if (tipo != null && tipo > 0) {
            sql.append("AND cm.tipoMovimiento = ? ");
            logger.debug("Filtro aplicado: tipoMovimiento = " + tipo);
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

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
                    logger.debug("Movimiento agregado: ID=" + movimiento.getIdcajaMovimientos() + ", Monto=" + movimiento.getMonto());
                }

                logger.info("Total de movimientos cargados: " + movimientos.size());
            }

        } catch (SQLException e) {
            logger.error("Error al cargar historial de movimientos con filtros", e);
        }

        logger.info("Finalizando método cargarhistorialDeMovimientosConFiltros");
        return movimientos;
    }


    public List<String> selectTiposDeMovimiento(int tipo) {
        logger.info("Iniciando método selectTiposDeMovimiento para tipo: " + tipo);

        List<String> tiposMovimientos = new ArrayList<>();
        String sql = "SELECT descripcion FROM tipocajamovimiento WHERE entradaSalida = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tipo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipom = rs.getString("descripcion");
                    tiposMovimientos.add(tipom);
                    logger.debug("Tipo de movimiento agregado: " + tipom);
                }
            }

            logger.info("Tipos de movimiento obtenidos: " + tiposMovimientos.size());

        } catch (SQLException e) {
            logger.error("Error al obtener tipos de movimiento para tipo: " + tipo, e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método selectTiposDeMovimiento");
        return tiposMovimientos;
    }

    public List<String> selectTiposDeMovimiento() {
        logger.info("Iniciando método selectTiposDeMovimiento (sin filtros)");

        List<String> tiposMovimientos = new ArrayList<>();
        String sql = "SELECT descripcion FROM tipocajamovimiento";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String tipom = rs.getString("descripcion");
                tiposMovimientos.add(tipom);
                logger.debug("Tipo de movimiento encontrado: " + tipom);
            }
            logger.info("Total de tipos de movimiento obtenidos: " + tiposMovimientos.size());
        } catch (SQLException e) {
            logger.error("Error al obtener tipos de movimiento (sin filtros)", e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método selectTiposDeMovimiento (sin filtros)");
        return tiposMovimientos;
    }

    public String registrarMovimientoDeCaja(int idcaja, int entradaSalida, float monto, int idusuarios, String opcion) {
        logger.info("Iniciando método registrarMovimientoDeCaja - Caja ID: " + idcaja + ", Usuario ID: " + idusuarios + ", Movimiento: " + opcion);

        String sql = "INSERT INTO cajamovimientos (idcaja, entradaSalida, monto, fechaHora, responsable, tipoMovimiento) "
                + "VALUES (?, ?, ?, NOW(), ?, ?)";
        String mensaje = "Registro exitoso";

        String tipoMovimientoSql = "SELECT idtipocajamovimiento FROM tipocajamovimiento WHERE descripcion = ?";
        int tipoMovimientoId = -1;

        try (Connection conn = Database.getConnection();
             PreparedStatement tipoStmt = conn.prepareStatement(tipoMovimientoSql)) {

            tipoStmt.setString(1, opcion);
            try (ResultSet rs = tipoStmt.executeQuery()) {
                if (rs.next()) {
                    tipoMovimientoId = rs.getInt("idtipocajamovimiento");
                    logger.debug("ID tipoMovimiento encontrado: " + tipoMovimientoId + " para opción: " + opcion);
                } else {
                    logger.warn("Tipo de movimiento no encontrado para descripción: " + opcion);
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
                    logger.warn("No se insertó ningún movimiento en cajamovimientos.");
                    mensaje = "Error: No se pudo registrar el movimiento.";
                } else {
                    logger.info("Movimiento de caja registrado correctamente - Monto: " + monto);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al registrar movimiento de caja", e);
            Database.handleSQLException(e);
            mensaje = "Error: Ocurrió un problema al registrar el movimiento.";
        }

        logger.info("Finalizando método registrarMovimientoDeCaja - Resultado: " + mensaje);
        return mensaje;
    }

    public Boolean verificarApertura(int idcaja) {
        logger.info("Iniciando método verificarApertura para caja ID: " + idcaja);

        String sql = "SELECT estado FROM cajas WHERE idcaja = ?";
        Boolean estadoAbierto = null;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int estado = rs.getInt("estado");
                    estadoAbierto = (estado == 1);
                    logger.debug("Caja ID: " + idcaja + " - Estado obtenido: " + estado + " (Abierta: " + estadoAbierto + ")");
                } else {
                    estadoAbierto = false;
                    logger.warn("Caja ID: " + idcaja + " no encontrada en la base de datos.");
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar apertura de la caja ID: " + idcaja, e);
            Database.handleSQLException(e);
            estadoAbierto = false;
        }

        logger.info("Finalizando método verificarApertura - Resultado: " + estadoAbierto);
        return estadoAbierto;
    }

    public Boolean verificarMonto(int idcaja, float monto) {
        logger.info("Iniciando método verificarMonto para caja ID: " + idcaja + ", monto requerido: " + monto);

        String sql = "SELECT saldoActual FROM cajas WHERE idcaja = ?";
        Boolean saldoSuficiente = null; // Usamos null para diferenciar errores de otros valores.

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idcaja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double saldoActual = rs.getFloat("saldoActual");
                    saldoSuficiente = saldoActual >= monto;
                    logger.debug("Saldo actual: " + saldoActual + ", saldo suficiente: " + saldoSuficiente);
                } else {
                    saldoSuficiente = false; // Caja no encontrada
                    logger.warn("No se encontró caja con ID: " + idcaja);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar monto en caja ID: " + idcaja, e);
            Database.handleSQLException(e);
            saldoSuficiente = false; // Asumimos false en caso de error
        }

        logger.info("Finalizando método verificarMonto con resultado: " + saldoSuficiente);
        return saldoSuficiente;
    }

}


