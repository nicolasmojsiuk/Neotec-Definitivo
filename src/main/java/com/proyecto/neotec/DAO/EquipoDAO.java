package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Equipos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipoDAO {
    public List<Equipos> selectAllEquipos() {

        List<Equipos> equipos = new ArrayList<>();
        String sql = "SELECT idequipos, idclientes, estado, observaciones,dispositivo,activo, fechaIngreso, fechaModificacion,fechaSalida FROM equipos";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Equipos equipo = new Equipos();
                equipo.setId(rs.getInt("idequipos"));
                equipo.setIdcliente(rs.getInt("idclientes"));
                equipo.setEstado(rs.getInt("estado"));
                equipo.setObservaciones(rs.getString("observaciones"));
                equipo.setDispositivo(rs.getString("dispositivo"));
                equipo.setActivo(rs.getInt("activo"));
                Timestamp fechaIngresoTimestamp = rs.getTimestamp("fechaIngreso");
                String fechaIngreso;
                if (fechaIngresoTimestamp != null) {
                    fechaIngreso = String.valueOf(fechaIngresoTimestamp.toLocalDateTime());
                } else {
                    fechaIngreso = "-";
                }

                equipo.setFechaIngreso(fechaIngreso);

                Timestamp fechaModificacionTimestamp = rs.getTimestamp("fechaModificacion");
                String fechaModificacion;
                if (fechaModificacionTimestamp != null) {
                    fechaModificacion = String.valueOf(fechaModificacionTimestamp.toLocalDateTime());
                } else {
                    fechaModificacion = "-";  // O cualquier valor por defecto
                }
                equipo.setFechaModificacion(fechaModificacion);

                Timestamp fechaSalidaTimestamp = rs.getTimestamp("fechaSalida");
                String fechaSalida;
                if (fechaSalidaTimestamp != null) {
                    fechaSalida = String.valueOf(fechaModificacionTimestamp.toLocalDateTime());
                } else {
                    fechaSalida = "-";  // O cualquier valor por defecto
                }
                equipo.setFechaSalida(fechaSalida);
                equipos.add(equipo);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return equipos;
    }

    public boolean AgregarEquipo(Equipos equipos) {
        String sql = "INSERT INTO equipos (idclientes,dispositivo,estado,observaciones,activo,img1,img2,img3,img4,fechaIngreso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, equipos.getIdcliente());
            stmt.setString(2, equipos.getDispositivo());
            stmt.setInt(3, equipos.getEstado());
            stmt.setString(4, equipos.getObservaciones());
            stmt.setInt(5, 1); // Estado activo predeterminado
            stmt.setString(6, equipos.getImg1());
            stmt.setString(7, equipos.getImg2());
            stmt.setString(8, equipos.getImg3());
            stmt.setString(9, equipos.getImg4());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            Database.handleSQLException(e);
            return false; // Retornar false si hubo una excepción
        }
    }

    public boolean ModificarEquipo(Equipos equipos){
        String sql = "UPDATE equipos SET idclientes = ?, dispositivo = ?, estado = ?, observaciones = ?, fechaModificacion = now() WHERE idequipos = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, equipos.getIdcliente());
            stmt.setString(2, equipos.getDispositivo());
            stmt.setInt(3, equipos.getEstado());
            stmt.setString(4, equipos.getObservaciones());
            stmt.setInt(5,equipos.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            Database.handleSQLException(e);
            return false;
        }
    }


        // Método para obtener las imágenes desde la base de datos y devolver una lista de rutas
    public List<String> obtenerImagenes(Equipos equipo) {
        List<String> rutasImagenes = new ArrayList<>();

        // Consulta SQL para obtener las imágenes
        String sql = "SELECT img1, img2, img3, img4 FROM equipos WHERE idequipos = ?";

        // Obtener conexión de la base de datos
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Establecer el id del equipo en la consulta
            statement.setInt(1, equipo.getId());

            // Ejecutar la consulta y obtener el resultado
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // Obtener las rutas de las imágenes de los campos de la base de datos
                String img1 = resultSet.getString("img1");
                String img2 = resultSet.getString("img2");
                String img3 = resultSet.getString("img3");
                String img4 = resultSet.getString("img4");

                // Agregar las rutas de las imágenes a la lista si no son nulas
                if (img1 != null && !img1.isEmpty()) rutasImagenes.add(img1);
                if (img2 != null && !img2.isEmpty()) rutasImagenes.add(img2);
                if (img3 != null && !img3.isEmpty()) rutasImagenes.add(img3);
                if (img4 != null && !img4.isEmpty()) rutasImagenes.add(img4);
            } else {
                System.out.println("No se encontraron imágenes para el equipo con ID: " + equipo.getId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Manejo de errores de SQL
        }

        // Devolver la lista de rutas de las imágenes
        return rutasImagenes;

    }

    public int obtenerIDCliente(String duenno) {
        int idCliente = 0;
        String query = "SELECT idclientes FROM clientes WHERE dni = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Establecer el valor del parámetro de la consulta
            stmt.setString(1, duenno);

            // Ejecutar la consulta y obtener el resultado
            ResultSet rs = stmt.executeQuery();

            // Verificar si hay resultados y obtener el valor de idclientes
            if (rs.next()) {
                idCliente = Integer.parseInt(rs.getString("idclientes"));
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        // Retornar el valor de idCliente (puede ser vacío si no hay coincidencia)
        return idCliente;
    }

    public String obtenerDNI(int idcliente) {
        String dni = "";
        String query = "SELECT dni FROM clientes WHERE idclientes = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Establecer el valor del parámetro de la consulta
            stmt.setInt(1,idcliente);

            // Ejecutar la consulta y obtener el resultado
            ResultSet rs = stmt.executeQuery();

            // Verificar si hay resultados y obtener el valor de idclientes
            if (rs.next()) {
                dni = (rs.getString("dni"));
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        // Retornar el valor de dni (puede ser vacío si no hay coincidencia)
        return dni;
    }

    public void cambiarEstadoActivo(int idEquipo, int nuevoEstado) {
        String sql = "UPDATE equipos SET activo = ? WHERE idequipos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nuevoEstado);
            stmt.setInt(2, idEquipo);

            stmt.executeUpdate();
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
    }

    public Equipos obtenerPropietario(int idEquipo) {
        Equipos equipos = new Equipos();
        String sql = "SELECT dispositivo, idclientes FROM equipos WHERE idequipos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer el parámetro idEquipo en la consulta
            stmt.setInt(1, idEquipo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    equipos.setDispositivo(rs.getString("dispositivo"));
                    equipos.setIdcliente(rs.getInt("idclientes"));
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return equipos;
    }

    public Equipos obtenerEquipoPorId(int idequipo) {
        Equipos equipo = null; // Inicializar en null para evitar devolver un objeto vacío
        String sql = "SELECT idequipos, idclientes, estado, observaciones, dispositivo, activo, fechaIngreso, fechaModificacion, fechaSalida FROM equipos WHERE idequipos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idequipo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    equipo = new Equipos(); // Instanciar solo si hay resultados
                    equipo.setId(rs.getInt("idequipos"));
                    equipo.setIdcliente(rs.getInt("idclientes"));
                    equipo.setEstado(rs.getInt("estado"));
                    equipo.setObservaciones(rs.getString("observaciones"));
                    equipo.setDispositivo(rs.getString("dispositivo"));
                    equipo.setActivo(rs.getInt("activo"));

                    equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                    equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                    equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return equipo;
    }

    // Método auxiliar para manejar fechas evitando código repetitivo
    private String obtenerFecha(ResultSet rs, String columna) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columna);
        return (timestamp != null) ? timestamp.toLocalDateTime().toString() : "-";
    }
}
