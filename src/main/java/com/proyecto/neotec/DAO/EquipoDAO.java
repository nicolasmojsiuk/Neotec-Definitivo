package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Equipos;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.neotec.util.FechaFormatear;
import org.apache.log4j.Logger;
public class EquipoDAO {
    private static final Logger logger = Logger.getLogger(EquipoDAO.class);
    public List<Equipos> ordenarEquipos(String orden) {
        if (!orden.equalsIgnoreCase("ASC") && !orden.equalsIgnoreCase("DESC")) {
            orden = "DESC"; // valor por defecto
        }
        List<Equipos> equipos = new ArrayList<>();
        String sql = "SELECT e.idequipos, e.idclientes, c.nombre, c.apellido, e.estado, e.observaciones, e.dispositivo, e.activo, " +
                "e.fechaIngreso, e.fechaModificacion, e.fechaSalida " +
                "FROM equipos e " +
                "INNER JOIN clientes c ON e.idclientes = c.idclientes " +
                "ORDER BY e.fechaIngreso " + orden;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            logger.info("Iniciando consulta para obtener todos los equipos");
            int contador = 0;

            while (rs.next()) {
                contador++;
                Equipos equipo = new Equipos();
                equipo.setId(rs.getInt("idequipos"));
                equipo.setIdcliente(rs.getInt("idclientes"));
                equipo.setEstado(rs.getInt("estado"));
                equipo.setObservaciones(rs.getString("observaciones"));
                equipo.setDispositivo(rs.getString("dispositivo"));
                equipo.setActivo(rs.getInt("activo"));

                // Procesamiento de fechas
                Timestamp fechaIngresoTimestamp = rs.getTimestamp("fechaIngreso");
                String fechaIngreso = (fechaIngresoTimestamp != null) ?
                        FechaFormatear.formatear(fechaIngresoTimestamp.toLocalDateTime()) : "-";
                equipo.setFechaIngreso(fechaIngreso);

                Timestamp fechaModificacionTimestamp = rs.getTimestamp("fechaModificacion");
                String fechaModificacion = (fechaModificacionTimestamp != null) ?
                        FechaFormatear.formatear(fechaModificacionTimestamp.toLocalDateTime()) : "-";
                equipo.setFechaModificacion(fechaModificacion);

                Timestamp fechaSalidaTimestamp = rs.getTimestamp("fechaSalida");
                String fechaSalida = (fechaSalidaTimestamp != null) ?
                        FechaFormatear.formatear(fechaSalidaTimestamp.toLocalDateTime()) : "-";
                equipo.setFechaSalida(fechaSalida);
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                equipo.setNombreCompletoCliente(nombreCompleto);

                equipos.add(equipo);
            }

            logger.info("Total de equipos encontrados: " + contador);

        } catch (SQLException e) {
            logger.error("Error al obtener todos los equipos: " + e.getMessage());
            Database.handleSQLException(e);
        }

        logger.info("Retornando lista de equipos. Cantidad: " + equipos.size());
        return equipos;
    }

    public List<Equipos> selectAllEquipos() {

        List<Equipos> equipos = new ArrayList<>();
        String sql = "SELECT e.idequipos, e.idclientes, c.nombre, c.apellido, e.estado, e.observaciones, e.dispositivo, e.activo, " +
                "e.fechaIngreso, e.fechaModificacion, e.fechaSalida " +
                "FROM equipos e " +
                "INNER JOIN clientes c ON e.idclientes = c.idclientes " +
                "ORDER BY e.fechaIngreso DESC" ;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            logger.info("Iniciando consulta para obtener todos los equipos");
            int contador = 0;

            while (rs.next()) {
                contador++;
                Equipos equipo = new Equipos();
                equipo.setId(rs.getInt("idequipos"));
                equipo.setIdcliente(rs.getInt("idclientes"));
                equipo.setEstado(rs.getInt("estado"));
                equipo.setObservaciones(rs.getString("observaciones"));
                equipo.setDispositivo(rs.getString("dispositivo"));
                equipo.setActivo(rs.getInt("activo"));

                // Procesamiento de fechas
                Timestamp fechaIngresoTimestamp = rs.getTimestamp("fechaIngreso");
                String fechaIngreso = (fechaIngresoTimestamp != null) ?
                        FechaFormatear.formatear(fechaIngresoTimestamp.toLocalDateTime()) : "-";
                equipo.setFechaIngreso(fechaIngreso);

                Timestamp fechaModificacionTimestamp = rs.getTimestamp("fechaModificacion");
                String fechaModificacion = (fechaModificacionTimestamp != null) ?
                        FechaFormatear.formatear(fechaModificacionTimestamp.toLocalDateTime()) : "-";
                equipo.setFechaModificacion(fechaModificacion);

                Timestamp fechaSalidaTimestamp = rs.getTimestamp("fechaSalida");
                String fechaSalida = (fechaSalidaTimestamp != null) ?
                        FechaFormatear.formatear(fechaSalidaTimestamp.toLocalDateTime()) : "-";
                equipo.setFechaSalida(fechaSalida);
                String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                equipo.setNombreCompletoCliente(nombreCompleto);

                equipos.add(equipo);
            }

            logger.info("Total de equipos encontrados: " + contador);

        } catch (SQLException e) {
            logger.error("Error al obtener todos los equipos: " + e.getMessage());
            Database.handleSQLException(e);
        }

        logger.info("Retornando lista de equipos. Cantidad: " + equipos.size());
        return equipos;
    }
    public void asignarFechaSalida(int idEquipo) {
        String sql = "UPDATE equipos SET fechaSalida = NOW() WHERE idequipos = ?";

        logger.debug("Iniciando asignación de fecha de salida para el equipo N°: " + idEquipo);

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEquipo);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                logger.info("Fecha de salida asignada correctamente para el equipo N°: " + idEquipo);
            } else {
                logger.warn("No se encontró el equipo N°: " + idEquipo + " para asignar la fecha de salida.");
            }

        } catch (SQLException e) {
            logger.error("Error al asignar la fecha de salida para el equipo N°: " + idEquipo + ". Detalles: " + e.getMessage());
            Database.handleSQLException(e);
        }
    }

    public boolean AgregarEquipoConImagenes(Equipos equipos) {
        String sql = "INSERT INTO equipos (idclientes,dispositivo,estado,observaciones,activo,img1,img2,img3,img4,fechaIngreso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("Iniciando inserción de nuevo equipo con imágenes");
            logger.debug("Datos del equipo: " +
                    "ClienteID=" + equipos.getIdcliente() +
                    ", Dispositivo=" + equipos.getDispositivo() +
                    ", Estado=" + equipos.getEstado());

            stmt.setInt(1, equipos.getIdcliente());
            stmt.setString(2, equipos.getDispositivo());
            stmt.setInt(3, equipos.getEstado());
            stmt.setString(4, equipos.getObservaciones());
            stmt.setInt(5, 1); // Estado activo predeterminado
            stmt.setString(6, equipos.getImg1());
            stmt.setString(7, equipos.getImg2());
            stmt.setString(8, equipos.getImg3());
            stmt.setString(9, equipos.getImg4());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                logger.info("Equipo insertado correctamente. Filas afectadas: " + filasAfectadas);
                logger.debug("Imágenes asociadas: " +
                        (equipos.getImg1() != null ? "[Img1]" : "") +
                        (equipos.getImg2() != null ? "[Img2]" : "") +
                        (equipos.getImg3() != null ? "[Img3]" : "") +
                        (equipos.getImg4() != null ? "[Img4]" : ""));
                return true;
            } else {
                logger.error("No se ha insertado ningún equipo");
            }

        } catch (SQLException e) {
            logger.error("Error al insertar equipo con imágenes: " + e.getMessage());
            logger.debug("Detalle del error: ", e);
            Database.handleSQLException(e);
        }

        return false;
    }


    public boolean ModificarEquipo(Equipos equipos) {
        String sql = "UPDATE equipos SET idclientes = ?, dispositivo = ?, estado = ?, observaciones = ?, activo = ?, fechaModificacion = now() WHERE idequipos = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("Iniciando modificación del equipo ID: " + equipos.getId());
            logger.debug("Datos a actualizar: " +
                    "ClienteID=" + equipos.getIdcliente() +
                    ", Dispositivo='" + equipos.getDispositivo() + "'" +
                    ", Estado=" + equipos.getEstado() +
                    ", Activo=" + equipos.getActivo());

            stmt.setInt(1, equipos.getIdcliente());
            stmt.setString(2, equipos.getDispositivo());
            stmt.setInt(3, equipos.getEstado());
            stmt.setString(4, equipos.getObservaciones());
            stmt.setInt(5, equipos.getActivo());
            stmt.setInt(6, equipos.getId());

            int filasActualizadas = stmt.executeUpdate();

            if (filasActualizadas > 0) {
                logger.info("Equipo ID: " + equipos.getId() + " actualizado correctamente. Filas afectadas: " + filasActualizadas);
                return true;
            } else {
                logger.warn("No se encontró el equipo ID: " + equipos.getId() + " para actualizar");
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error al modificar equipo ID: " + equipos.getId() + " - " + e.getMessage());
            logger.debug("Detalle del error:", e);
            Database.handleSQLException(e);
            return false;
        }
    }

    // Método para obtener las imágenes desde la base de datos y devolver una lista de rutas
    public List<String> obtenerImagenes(Equipos equipo) {
        List<String> rutasImagenes = new ArrayList<>();
        String sql = "SELECT img1, img2, img3, img4 FROM equipos WHERE idequipos = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            logger.info("Obteniendo imágenes para equipo ID: " + equipo.getId());
            statement.setInt(1, equipo.getId());

            ResultSet resultSet = statement.executeQuery();
            logger.debug("Consulta ejecutada para obtener imágenes del equipo");

            if (resultSet.next()) {
                String img1 = resultSet.getString("img1");
                String img2 = resultSet.getString("img2");
                String img3 = resultSet.getString("img3");
                String img4 = resultSet.getString("img4");

                if (img1 != null && !img1.isEmpty()) {
                    rutasImagenes.add(img1);
                    logger.debug("Imagen 1 encontrada");
                }
                if (img2 != null && !img2.isEmpty()) {
                    rutasImagenes.add(img2);
                    logger.debug("Imagen 2 encontrada");
                }
                if (img3 != null && !img3.isEmpty()) {
                    rutasImagenes.add(img3);
                    logger.debug("Imagen 3 encontrada");
                }
                if (img4 != null && !img4.isEmpty()) {
                    rutasImagenes.add(img4);
                    logger.debug("Imagen 4 encontrada");
                }

                logger.info("Total de imágenes encontradas: " + rutasImagenes.size());
            } else {
                logger.warn("No se encontró el equipo con ID: " + equipo.getId());
            }

        } catch (SQLException e) {
            logger.error("Error al obtener imágenes para equipo ID: " + equipo.getId() + " - " + e.getMessage() +"Detalle del error:"+ e);
        }

        logger.debug("Retornando lista con " + rutasImagenes.size() + " imágenes");
        return rutasImagenes;
    }
    public void actualizarEstadoEquipo(int idEquipo, int nuevoEstado) {
        String sql = "UPDATE equipos SET estado = ?, fechaModificacion = CURRENT_TIMESTAMP WHERE idequipos = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            logger.info("Actualizando estado del equipo ID: " + idEquipo +
                    " | Nuevo estado: " + nuevoEstado);
            stmt.setInt(1, nuevoEstado);
            stmt.setInt(2, idEquipo);

            int filasActualizadas = stmt.executeUpdate();

            if (filasActualizadas > 0) {
                logger.info("Estado actualizado correctamente. Filas afectadas: " + filasActualizadas);
            } else {
                logger.warn("No se encontró el equipo ID: " + idEquipo + " para actualizar");
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar estado del equipo ID: " + idEquipo +
                    " | Error: " + e.getMessage()+"Detalle del error:"+ e);
        }
    }

    public int obtenerIDCliente(String dni) {
        int idCliente = 0;
        String query = "SELECT idclientes FROM clientes WHERE dni = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Buscando ID de cliente con DNI: " + dni);
            stmt.setString(1, dni);

            ResultSet rs = stmt.executeQuery();
            logger.debug("Consulta ejecutada para obtener ID de cliente");

            if (rs.next()) {
                idCliente = Integer.parseInt(rs.getString("idclientes"));
                logger.info("ID encontrado: " + idCliente + " para DNI: " + dni);
            } else {
                logger.warn("No se encontró cliente con DNI: " + dni);
            }

        } catch (SQLException e) {
            logger.error("Error al buscar ID de cliente con DNI: " + dni + " - " + e.getMessage());
            logger.debug("Detalle del error:", e);
            Database.handleSQLException(e);
        } catch (NumberFormatException e) {
            logger.error("Error al convertir ID de cliente para DNI: " + dni + " - " + e.getMessage() +". Detalle del error:"+ e);
            logger.debug("Detalle del error:"+ e);
        }

        logger.debug("Retornando ID: " + idCliente);
        return idCliente;
    }

    public String obtenerDNI(int idcliente) {
        String dni = "";
        String query = "SELECT dni FROM clientes WHERE idclientes = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Buscando DNI para cliente ID: " + idcliente);
            stmt.setInt(1, idcliente);

            ResultSet rs = stmt.executeQuery();
            logger.debug("Consulta ejecutada para obtener DNI");

            if (rs.next()) {
                dni = rs.getString("dni");
                logger.info("DNI encontrado: " + dni + " para cliente ID: " + idcliente);
            } else {
                logger.warn("No se encontró DNI para cliente ID: " + idcliente);
            }
        } catch (SQLException e) {
            logger.error("Error al buscar DNI para cliente ID: " + idcliente + " - " + e.getMessage()+ "Detalle del error:"+ e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando DNI: " + (dni.isEmpty() ? "[no encontrado]" : dni));
        return dni;
    }
    public Equipos obtenerPropietario(int idEquipo) {
        Equipos equipos = new Equipos();
        String sql = "SELECT dispositivo, idclientes FROM equipos WHERE idequipos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEquipo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    equipos.setDispositivo(rs.getString("dispositivo"));
                    equipos.setIdcliente(rs.getInt("idclientes"));
                } else {
                    logger.warn("No se encontró el equipo con ID: " + idEquipo);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar propietario del equipo ID: " + idEquipo +
                    " - " + e.getMessage() +"Detalle del error:"+ e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando datos del equipo/propietario");
        return equipos;
    }
    public Equipos obtenerEquipoPorId(int idequipo) {
        String sql = "SELECT idequipos, idclientes, estado, observaciones, dispositivo, activo, fechaIngreso, fechaModificacion, fechaSalida FROM equipos WHERE idequipos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("Buscando equipo con ID: " + idequipo);
            stmt.setInt(1, idequipo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Equipos equipo = new Equipos();
                    equipo.setId(rs.getInt("idequipos"));
                    equipo.setIdcliente(rs.getInt("idclientes"));
                    equipo.setEstado(rs.getInt("estado"));
                    equipo.setObservaciones(rs.getString("observaciones"));
                    equipo.setDispositivo(rs.getString("dispositivo"));
                    equipo.setActivo(rs.getInt("activo"));
                    equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                    equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                    equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));

                    logger.info("Equipo encontrado - ID: " + equipo.getId() +
                            " | Dispositivo: " + equipo.getDispositivo() +
                            " | Cliente ID: " + equipo.getIdcliente());
                    logger.debug("Detalles completos: " + equipo.toString());

                    return equipo;
                } else {
                    logger.warn("No se encontró equipo con ID: " + idequipo);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener equipo con ID: " + idequipo + " - " + e.getMessage() + "Detalle del error:"+e);
            Database.handleSQLException(e);
        }
        logger.debug("Retornando null (equipo no encontrado)");
        return null;
    }
    public int obtener_IDequipo_con_idpresupuesto(int idpresupuesto) {
        String query = "SELECT idEquipo FROM presupuestos WHERE idpresupuestos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Buscando ID de equipo para presupuesto ID: " + idpresupuesto);
            stmt.setInt(1, idpresupuesto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id_equipo = rs.getInt("idEquipo");
                    logger.info("ID de equipo encontrado: " + id_equipo + " para presupuesto ID: " + idpresupuesto);
                    return id_equipo;
                } else {
                    logger.warn("No se encontró equipo asociado al presupuesto ID: " + idpresupuesto);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar ID de equipo para presupuesto ID: " + idpresupuesto + " - " + e.getMessage() +". Detalle del error:"+ e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando 0 (no se encontró equipo asociado)");
        return 0;
    }
    // Método auxiliar para manejar fechas evitando código repetitivo
    private String obtenerFecha(ResultSet rs, String columna) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columna);

        if (timestamp != null) {
            LocalDateTime fecha = timestamp.toLocalDateTime();
            String formateada = FechaFormatear.formatear(fecha);
            logger.debug("Fecha obtenida y formateada para columna '" + columna + "': " + formateada);
            return formateada;
        } else {
            logger.debug("Columna '" + columna + "' es null, retornando '-'");
            return "-";
        }
    }

    public List<Equipos> obtenerEquiposPorClientes(List<Cliente> clientes) {
        List<Equipos> equipos = new ArrayList<>();

        if (clientes.isEmpty()) {
            logger.info("Lista de clientes vacía - retornando lista de equipos vacía");
            return equipos;
        }

        StringBuilder query = new StringBuilder("SELECT * FROM equipos WHERE idclientes IN (");
        for (int i = 0; i < clientes.size(); i++) {
            query.append("?");
            if (i < clientes.size() - 1) {
                query.append(",");
            }
        }
        query.append(")");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            logger.info("Buscando equipos para " + clientes.size() + " cliente(s)");

            for (int i = 0; i < clientes.size(); i++) {
                stmt.setInt(i + 1, clientes.get(i).getIdclientes());
                logger.debug("Asignado ID cliente " + (i+1) + ": " + clientes.get(i).getIdclientes());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                int contadorEquipos = 0;
                while (rs.next()) {
                    contadorEquipos++;
                    Equipos equipo = new Equipos();
                    equipo.setId(rs.getInt("idequipos"));
                    equipo.setIdcliente(rs.getInt("idclientes"));
                    equipo.setDispositivo(rs.getString("dispositivo"));
                    equipo.setEstado(rs.getInt("estado"));
                    equipo.setObservaciones(rs.getString("observaciones"));
                    equipo.setActivo(rs.getInt("activo"));

                    equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                    equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                    equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));

                    equipos.add(equipo);
                    logger.debug("Equipo #" + contadorEquipos + " encontrado - ID: " + equipo.getId() +
                            " | Dispositivo: " + equipo.getDispositivo());
                }
                logger.info("Total de equipos encontrados: " + contadorEquipos);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener equipos por clientes: " + e.getMessage() +"Detalle del error:"+ e);
            Database.handleSQLException(e);
        }
        logger.debug("Retornando lista con " + equipos.size() + " equipos");
        return equipos;
    }
    public List<Equipos> filtrarActivoInactivo(int estado) {
        List<Equipos> equipos = new ArrayList<>();
        String query = "SELECT * FROM equipos WHERE activo = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            logger.info("Filtrando equipos por estado activo: " + estado);
            stmt.setInt(1, estado);
            ResultSet rs = stmt.executeQuery();
            EquipoDAO equipoDAO = new EquipoDAO();
            int contador = 0;

            while (rs.next()) {
                contador++;
                Equipos equipo = new Equipos();

                equipo.setId(rs.getInt("idequipos"));
                equipo.setDispositivo(rs.getString("dispositivo"));
                equipo.setActivo(rs.getInt("activo"));
                equipo.setEstado(rs.getInt("estado"));
                equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));
                equipo.setObservaciones(rs.getString("observaciones"));
                // Obtener el propietario
                Equipos propietario = equipoDAO.obtenerPropietario(equipo.getId());
                equipo.setIdcliente(propietario.getIdcliente());
                logger.debug("Equipo #" + contador +
                        " | ID: " + equipo.getId() +
                        " | Dispositivo: " + equipo.getDispositivo() +
                        " | ID Cliente: " + propietario.getIdcliente());

                equipos.add(equipo);
            }

            logger.info("Total de equipos encontrados: " + contador);

        } catch (SQLException e) {
            logger.error("Error al filtrar equipos por estado activo=" + estado + ": " + e.getMessage() +"Detalle del error:"+ e);
        }

        logger.debug("Retornando " + equipos.size() + " equipos");
        return equipos;
    }

    public List<Equipos> filtrarPorEstadoEquipo(int estado) {
        List<Equipos> equipos = new ArrayList<>();
        String query = "SELECT * FROM equipos WHERE estado = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Filtrando equipos por estado: " + estado);
            stmt.setInt(1, estado);

            ResultSet rs = stmt.executeQuery();
            EquipoDAO equipoDAO = new EquipoDAO();
            int contador = 0;

            while (rs.next()) {
                contador++;
                Equipos equipo = new Equipos();

                equipo.setId(rs.getInt("idequipos"));
                equipo.setDispositivo(rs.getString("dispositivo"));
                equipo.setActivo(rs.getInt("activo"));
                equipo.setEstado(rs.getInt("estado"));
                equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));

                equipo.setObservaciones(rs.getString("observaciones"));

                Equipos propietario = equipoDAO.obtenerPropietario(equipo.getId());
                equipo.setIdcliente(propietario.getIdcliente());
                equipos.add(equipo);
            }

            logger.info("Total de equipos encontrados con estado " + estado + ": " + contador);

        } catch (SQLException e) {
            logger.error("Error al filtrar equipos por estado " + estado + ": " + e.getMessage());
            logger.debug("Stack trace completo:", e);
        }

        logger.debug("Retornando lista con " + equipos.size() + " equipos");
        return equipos;
    }
    public boolean AgregarEquipoSinImagenes(Equipos equipoSinImagenes) {
        String sql = "INSERT INTO equipos (idclientes, dispositivo, estado, observaciones, activo, fechaIngreso) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("Iniciando inserción de equipo sin imágenes");
            logger.debug("Datos del equipo: " +
                    "ClienteID=" + equipoSinImagenes.getIdcliente() +
                    ", Dispositivo='" + equipoSinImagenes.getDispositivo() + "'" +
                    ", Estado=" + equipoSinImagenes.getEstado() +
                    ", Observaciones='" + equipoSinImagenes.getObservaciones() + "'");

            stmt.setInt(1, equipoSinImagenes.getIdcliente());
            stmt.setString(2, equipoSinImagenes.getDispositivo());

            stmt.setInt(3, equipoSinImagenes.getEstado());
            stmt.setString(4, equipoSinImagenes.getObservaciones());
            stmt.setInt(5, 1); // Estado activo predeterminado

            int filasAfectadas = stmt.executeUpdate();
            logger.info("Equipo insertado correctamente. Filas afectadas: " + filasAfectadas);
            return true;

        } catch (SQLException e) {
            logger.error("Error al insertar equipo sin imágenes: " + e.getMessage()+ "Detalle del error:"+ e);
            Database.handleSQLException(e);
            return false;
        }
    }
    public boolean equipoTieneImagenes(int idEquipo) {
        String query = "SELECT img1, img2, img3, img4 FROM equipos WHERE idequipos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idEquipo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("img1") != null || rs.getString("img2") != null ||
                        rs.getString("img3") != null || rs.getString("img4") != null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Equipos> buscarDispositivo(String text) {
        String sql = "SELECT * FROM equipos WHERE dispositivo LIKE ?";
        List<Equipos> equipos = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("Buscando dispositivos con texto: '" + text + "'");
            String parametroBusqueda = "%" + text + "%";
            stmt.setString(1, parametroBusqueda);
            logger.debug("Parámetro de búsqueda SQL: '" + parametroBusqueda + "'");

            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    Equipos equipo = new Equipos();
                    equipo.setId(rs.getInt("idequipos"));
                    equipo.setDispositivo(rs.getString("dispositivo"));
                    equipo.setActivo(rs.getInt("activo"));
                    equipo.setEstado(rs.getInt("estado"));
                    equipo.setIdcliente(rs.getInt("idclientes"));
                    equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                    equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                    equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));
                    equipo.setObservaciones(rs.getString("observaciones"));
                    equipos.add(equipo);
                }
                logger.info("Total de equipos encontrados: " + contador);
            }
        } catch (SQLException e) {
            logger.error("Error al buscar dispositivos con texto '" + text + "': " + e.getMessage()+ "Detalle del error:" + e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando " + equipos.size() + " equipos");
        return equipos;
    }
    public List<Equipos> buscarPorFechaIngreso(String texto) {
        List<Equipos> equipos = new ArrayList<>();
        String query = "SELECT * FROM equipos WHERE DATE(fechaIngreso) = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Buscando equipos por fecha de ingreso: " + texto);
            stmt.setString(1, texto); // formato "yyyy-MM-dd"

            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    Equipos equipo = new Equipos();
                    equipo.setId(rs.getInt("idequipos"));
                    equipo.setDispositivo(rs.getString("dispositivo"));
                    equipo.setActivo(rs.getInt("activo"));
                    equipo.setEstado(rs.getInt("estado"));
                    equipo.setIdcliente(rs.getInt("idclientes"));
                    equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                    equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                    equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));

                    equipo.setObservaciones(rs.getString("observaciones"));

                    equipos.add(equipo);
                }
                logger.info("Total de equipos encontrados: " + contador);
            }
        } catch (SQLException e) {
            logger.error("Error al buscar equipos por fecha '" + texto + "': " + e.getMessage() + "Detalle del error:"+ e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando " + equipos.size() + " equipos");
        return equipos;
    }

    public List<Equipos> buscarFechaSalida(String texto) {
        List<Equipos> equipos = new ArrayList<>();
        String query = "SELECT * FROM equipos WHERE DATE(fechaSalida) = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Buscando equipos por fecha de salida: " + texto);
            stmt.setString(1, texto); // Formato "yyyy-MM-dd"

            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    Equipos equipo = new Equipos();
                    equipo.setId(rs.getInt("idequipos"));
                    equipo.setDispositivo(rs.getString("dispositivo"));
                    equipo.setActivo(rs.getInt("activo"));
                    equipo.setEstado(rs.getInt("estado"));
                    equipo.setIdcliente(rs.getInt("idclientes"));
                    equipo.setFechaIngreso(obtenerFecha(rs, "fechaIngreso"));
                    equipo.setFechaModificacion(obtenerFecha(rs, "fechaModificacion"));
                    equipo.setFechaSalida(obtenerFecha(rs, "fechaSalida"));

                    equipo.setObservaciones(rs.getString("observaciones"));

                    equipos.add(equipo);
                }
                logger.info("Total de equipos encontrados con fecha de salida " + texto + ": " + contador);
            }
        } catch (SQLException e) {
            logger.error("Error al buscar equipos por fecha de salida '" + texto + "': " + e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando lista con " + equipos.size() + " equipos");
        return equipos;
    }
    public int obtenerEstadoEquipoIdDesdeBD(String descripcion) {
        logger.info("Inicio de obtenerEstadoEquipoIdDesdeBD con descripción: " + descripcion);

        String query = "SELECT idestadosequipos FROM estadosequipos WHERE descripcion = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.debug("Conexión a base de datos establecida correctamente.");
            stmt.setString(1, descripcion);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idEstado = rs.getInt("idestadosequipos");
                    logger.info("ID de estado obtenido: " + idEstado);
                    return idEstado;
                } else {
                    logger.warn("No se encontró ningún estado con la descripción: " + descripcion);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener ID de estado del equipo desde la BD", e);
            Database.handleSQLException(e);
        }
        logger.info("Retornando -1: estado no encontrado");
        return -1;
    }

    public String obtenerDescripcionEstadoEquipoDesdeBD(int idEstado) {

        String query = "SELECT descripcion FROM estadosequipos WHERE idestadosequipos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idEstado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String descripcion = rs.getString("descripcion");
                    return descripcion;
                } else {
                    logger.warn("No se encontró ninguna descripción desde la base de datos para el ID: " + idEstado);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener la descripción del estado del equipo desde la BD", e);
            Database.handleSQLException(e);
        }

        logger.info("Retornando 'Desconocido': ID no válido o no encontrado");
        return "Desconocido";
    }


}
