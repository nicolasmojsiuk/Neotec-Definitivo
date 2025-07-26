package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Equipos;
import org.apache.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    private static final Logger logger = Logger.getLogger(ClienteDAO.class); // Asegúrate de poner el nombre correcto de la clase

    public List<Cliente> selectAllClientes() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT idclientes, nombre, apellido, email, telefono, dni, activo FROM clientes";

        logger.debug("Iniciando selección de todos los clientes");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdclientes(rs.getInt("idclientes"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setApellido(rs.getString("apellido"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setDni(rs.getInt("dni"));
                cliente.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

                clientes.add(cliente);
            }

            logger.info("Se recuperaron " + clientes.size() + " clientes desde la base de datos.");

        } catch (SQLException e) {
            logger.error("Error al seleccionar todos los clientes | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }
        return clientes;
    }

    public static String crearCliente(Cliente cliente) {
        String mensaje = "";
        String sql = "INSERT INTO clientes (nombre, apellido, dni, email, telefono, activo, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        logger.debug("Creando nuevo cliente - Parámetros -> Nombre: " + cliente.getNombre() +
                ", Apellido: " + cliente.getApellido() + ", DNI: " + cliente.getDni() +
                ", Email: " + cliente.getEmail() + ", Teléfono: " + cliente.getTelefono());

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setInt(3, cliente.getDni());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getTelefono());
            stmt.setInt(6, 1);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                mensaje = "Cliente creado exitosamente.";
                logger.info("Cliente creado exitosamente.");
            } else {
                mensaje = "No se pudo ingresar el cliente";
                logger.warn("No se insertó el cliente (0 filas afectadas).");
            }

        } catch (SQLException e) {
            logger.error("Error al crear cliente | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }
        return mensaje;
    }

    public static void cambiarEstadoActivo(int idCliente, int estado) {
        String sql = "UPDATE clientes SET activo = ? WHERE idclientes = ?";

        logger.debug("Cambiando estado 'activo' del cliente - ID Cliente: " + idCliente + ", Nuevo Estado: " + estado);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estado);
            stmt.setInt(2, idCliente);

            int filasActualizadas = stmt.executeUpdate();

            if (filasActualizadas > 0) {
                logger.info("Estado del cliente actualizado correctamente - ID Cliente: " + idCliente);
            } else {
                logger.warn("No se actualizó el estado del cliente (0 filas afectadas) - ID Cliente: " + idCliente);
            }

        } catch (SQLException e) {
            logger.error("Error al cambiar el estado del cliente | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }
    }

    public static String modificarCliente(Cliente cliente) {
        String mensaje = "";
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, dni = ?, email = ?, telefono = ? WHERE idclientes = ?";

        logger.debug("Modificando cliente - ID: " + cliente.getIdclientes() +
                ", Nombre: " + cliente.getNombre() +
                ", Apellido: " + cliente.getApellido() +
                ", DNI: " + cliente.getDni() +
                ", Email: " + cliente.getEmail() +
                ", Teléfono: " + cliente.getTelefono());

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setInt(3, cliente.getDni());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getTelefono());
            stmt.setInt(6, cliente.getIdclientes());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                mensaje = "Cliente modificado exitosamente.";
                logger.info("Cliente modificado exitosamente - ID: " + cliente.getIdclientes());
            } else {
                mensaje = "No se pudo modificar el Cliente. Verifique si el cliente existe.";
                logger.warn("No se modificó ningún cliente (0 filas afectadas) - ID: " + cliente.getIdclientes());
            }

        } catch (SQLException e) {
            logger.error("Error al modificar cliente | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return mensaje;
    }

    public String obtenerNombre(int id) {
        String sql = "SELECT nombre FROM clientes WHERE idclientes = ?";
        String nombre = "";
        logger.debug("Obteniendo nombre del cliente - ID: " + id);
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                    logger.info("Nombre obtenido correctamente - ID: " + id + ", Nombre: " + nombre);
                } else {
                    logger.warn("No se encontró cliente con ID: " + id);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener nombre del cliente | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return nombre;
    }
    public String obtenerNombrePorDni(int dni) {
        String sql = "SELECT nombre, apellido FROM clientes WHERE dni = ?";
        String nombre = "";
        String apellido = "";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                    apellido = rs.getString("apellido");
                } else {
                    logger.warn("No se encontró cliente con DNI: " + dni);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener nombre por DNI | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return nombre + " " + apellido;
    }

    public String obtenerNombreCompletoPorId(int id) {
        String sql = "SELECT nombre, apellido FROM clientes WHERE idclientes = ?";
        String nombre = "";
        String apellido = "";
        logger.debug("Obteniendo nombre completo del cliente - ID: " + id);
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("nombre");
                    apellido = rs.getString("apellido");
                } else {
                    logger.warn("No se encontró cliente con ID: " + id);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener nombre completo por ID | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }
        return nombre + " " + apellido;
    }

    public int obtenerIdPorDni(int dni) {
        String sql = "SELECT idclientes FROM clientes WHERE dni = ?";
        int id = 0;

        logger.debug("Obteniendo ID de cliente por DNI - DNI: " + dni);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("idclientes");
                    logger.info("ID obtenido correctamente - DNI: " + dni + ", ID Cliente: " + id);
                } else {
                    logger.warn("No se encontró cliente con DNI: " + dni);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener ID por DNI | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }
        return id;
    }

    public int obtenerDniPorId(int id) {
        String sql = "SELECT dni FROM clientes WHERE idclientes = ?";
        int dni = 0;
        logger.debug("Obteniendo DNI del cliente - ID: " + id);
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dni = rs.getInt("dni");
                    logger.info("DNI obtenido correctamente - ID: " + id + ", DNI: " + dni);
                } else {
                    logger.warn("No se encontró cliente con ID: " + id);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener DNI por ID | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }
        return dni;
    }

    public List<Cliente> buscarClientes(String text) {
        String sql = "SELECT * FROM clientes WHERE nombre LIKE ? OR apellido LIKE ?";
        List<Cliente> clientes = new ArrayList<>();
        logger.debug("Buscando clientes por texto: " + text);
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + text + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdclientes(rs.getInt("idclientes"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

                    clientes.add(cliente);
                }
                logger.info("Clientes encontrados con texto " + text + ": " + clientes.size());
            }
        } catch (SQLException e) {
            logger.error("Error al buscar clientes por texto | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public List<Cliente> filtrarActivoInnactivo(int estado) {
        List<Cliente> clientes = new ArrayList<>();
        String query = "SELECT * FROM clientes WHERE activo = ?";
        logger.debug("Filtrando clientes por estado - Estado solicitado: " + estado +
                (estado == 1 ? " (Activo)" : estado == 0 ? " (Inactivo)" : ""));

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, estado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdclientes(rs.getInt("idclientes"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setApellido(rs.getString("apellido"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setDni(rs.getInt("dni"));
                cliente.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

                clientes.add(cliente);
            }

            logger.info("Clientes encontrados con estado " + estado + ": " + clientes.size());
        } catch (SQLException e) {
            logger.error("Error al filtrar clientes por estado | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public List<Cliente> buscarPorEmail(String text) {
        String sql = "SELECT * FROM clientes WHERE email LIKE ?";
        List<Cliente> clientes = new ArrayList<>();

        logger.debug("Buscando clientes por email que contenga: \"" + text + "\"");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + text + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdclientes(rs.getInt("idclientes"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

                    clientes.add(cliente);
                }

                logger.info("Clientes encontrados con email similar a \"" + text + "\": " + clientes.size());
            }

        } catch (SQLException e) {
            logger.error("Error al buscar clientes por email | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public List<Cliente> buscarPorDNI(String text) {
        String sql = "SELECT * FROM clientes WHERE dni LIKE ?";
        List<Cliente> clientes = new ArrayList<>();

        logger.debug("Buscando clientes por DNI que contenga: \"" + text + "\"");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + text + "%"); // Búsqueda parcial

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdclientes(rs.getInt("idclientes"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

                    clientes.add(cliente);
                }

                logger.info("Clientes encontrados con DNI similar a \"" + text + "\": " + clientes.size());
            }

        } catch (SQLException e) {
            logger.error("Error al buscar clientes por DNI | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public List<Cliente> buscarPorTelefono(String text) {
        String sql = "SELECT * FROM clientes WHERE telefono LIKE ?";
        List<Cliente> clientes = new ArrayList<>();

        logger.debug("Buscando clientes por teléfono que contenga: \"" + text + "\"");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + text + "%"); // Búsqueda parcial

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdclientes(rs.getInt("idclientes"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

                    clientes.add(cliente);
                }

                logger.info("Clientes encontrados con teléfono similar a \"" + text + "\": " + clientes.size());

            }
        } catch (SQLException e) {
            logger.error("Error al buscar clientes por teléfono | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public List<Cliente> mejoresCliPorMontoVentas(int limite, int periodo, LocalDate desde, LocalDate hasta) {
        LocalDate fechaInicio = null;
        LocalDate fechaFin = LocalDate.now();

        logger.debug("Calculando mejores clientes por monto de ventas - Parámetros -> Limite: " + limite +
                ", Periodo: " + periodo + ", Desde: " + desde + ", Hasta: " + hasta);

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
        } else {
            switch (periodo) {
                case 7:
                    fechaInicio = fechaFin.minusDays(7);
                    break;
                case 30:
                    fechaInicio = fechaFin.minusDays(30);
                    break;
                case 365:
                    fechaInicio = fechaFin.minusDays(365);
                    break;
                default:
                    logger.error("Periodo no válido: " + periodo);
                    throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        String sql = "SELECT c.idclientes, c.dni, c.nombre, c.apellido, c.telefono, c.email, " +
                "SUM(tv.total) AS monto_total " +
                "FROM clientes c " +
                "INNER JOIN ticketsventa tv ON c.idclientes = tv.idCliente " +
                "WHERE tv.fechaHora BETWEEN ? AND ? " +
                "GROUP BY c.idclientes " +
                "HAVING monto_total > 0 " +
                "ORDER BY monto_total DESC " +
                "LIMIT ?";

        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
            ps.setInt(3, limite);

            logger.debug("Ejecutando query mejores clientes desde " + fechaInicio + " hasta " + fechaFin + ", límite: " + limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setCompras(rs.getFloat("monto_total"));
                    clientes.add(cliente);
                }

                logger.info("Cantidad de mejores clientes encontrados: " + clientes.size());
            }

        } catch (SQLException e) {
            logger.error("Error al obtener mejores clientes por monto de ventas | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public List<Cliente> mejoresCliPorNumVentas(int limite, int periodo, LocalDate desde, LocalDate hasta) {
        LocalDate fechaInicio = null;
        LocalDate fechaFin = LocalDate.now();

        logger.debug("Calculando mejores clientes por número de ventas - Parámetros -> Límite: " + limite +
                ", Periodo: " + periodo + ", Desde: " + desde + ", Hasta: " + hasta);

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
        } else {
            switch (periodo) {
                case 7:
                    fechaInicio = fechaFin.minusDays(7);
                    break;
                case 30:
                    fechaInicio = fechaFin.minusDays(30);
                    break;
                case 365:
                    fechaInicio = fechaFin.minusDays(365);
                    break;
                default:
                    logger.error("Periodo no válido: " + periodo);
                    throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        String sql = "SELECT c.idclientes, c.dni, c.nombre, c.apellido, c.telefono, c.email, " +
                "COUNT(tv.idticketsventa) AS cantidad_ventas " +
                "FROM clientes c " +
                "INNER JOIN ticketsventa tv ON c.idclientes = tv.idCliente " +
                "WHERE tv.fechaHora BETWEEN ? AND ? " +
                "GROUP BY c.idclientes " +
                "HAVING cantidad_ventas > 0 " +
                "ORDER BY cantidad_ventas DESC " +
                "LIMIT ?";

        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
            ps.setInt(3, limite);

            logger.debug("Ejecutando query mejores clientes por número de ventas desde " + fechaInicio + " hasta " + fechaFin + ", límite: " + limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setCompras(rs.getFloat("cantidad_ventas")); // número de tickets
                    clientes.add(cliente);
                }

                logger.info("Cantidad de mejores clientes encontrados (por número de ventas): " + clientes.size());
            }

        } catch (SQLException e) {
            logger.error("Error al obtener mejores clientes por número de ventas | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }


    public List<Cliente> mejoresCliPorNumTaller(int limite, int periodo, LocalDate desde, LocalDate hasta) {
        LocalDate fechaInicio = null;
        LocalDate fechaFin = LocalDate.now();

        logger.debug("Calculando mejores clientes por número de talleres - Parámetros -> Límite: " + limite +
                ", Periodo: " + periodo + ", Desde: " + desde + ", Hasta: " + hasta);

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
        } else {
            switch (periodo) {
                case 7:
                    fechaInicio = fechaFin.minusDays(7);
                    break;
                case 30:
                    fechaInicio = fechaFin.minusDays(30);
                    break;
                case 365:
                    fechaInicio = fechaFin.minusDays(365);
                    break;
                default:
                    logger.error("Periodo no válido: " + periodo);
                    throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        String sql = "SELECT c.idclientes, c.dni, c.nombre, c.apellido, c.telefono, c.email, " +
                "COUNT(p.idpresupuestos) AS cantidad_presupuestos " +
                "FROM clientes c " +
                "INNER JOIN equipos e ON c.idclientes = e.idclientes " +
                "INNER JOIN presupuestos p ON e.idequipos = p.idEquipo " +
                "WHERE p.fechaHora BETWEEN ? AND ? AND p.estado = 4 " +
                "GROUP BY c.idclientes " +
                "HAVING cantidad_presupuestos > 0 " +
                "ORDER BY cantidad_presupuestos DESC " +
                "LIMIT ?";

        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
            ps.setInt(3, limite);

            logger.debug("Ejecutando query mejores clientes por número de talleres desde " + fechaInicio + " hasta " + fechaFin + ", límite: " + limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setCompras(rs.getFloat("cantidad_presupuestos")); // número de presupuestos pagados
                    clientes.add(cliente);
                }

                logger.info("Cantidad de mejores clientes encontrados (por número de talleres): " + clientes.size());
            }

        } catch (SQLException e) {
            logger.error("Error al obtener mejores clientes por número de talleres | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public List<Cliente> mejoresCliPorMontoTaller(int limite, int periodo, LocalDate desde, LocalDate hasta) {
        LocalDate fechaInicio = null;
        LocalDate fechaFin = LocalDate.now();

        logger.debug("Calculando mejores clientes por monto en taller - Parámetros -> Límite: " + limite +
                ", Periodo: " + periodo + ", Desde: " + desde + ", Hasta: " + hasta);

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
        } else {
            switch (periodo) {
                case 1:
                    fechaInicio = fechaFin.minusDays(1);
                    break;
                case 7:
                    fechaInicio = fechaFin.minusDays(7);
                    break;
                case 365:
                    fechaInicio = fechaFin.minusDays(365);
                    break;
                default:
                    logger.error("Periodo no válido: " + periodo);
                    throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        String sql = "SELECT c.idclientes, c.dni, c.nombre, c.apellido, c.telefono, c.email, " +
                "SUM(p.precioTotal) AS monto_total " +
                "FROM clientes c " +
                "JOIN equipos e ON c.idclientes = e.idclientes " +
                "JOIN presupuestos p ON e.idequipos = p.idEquipo " +
                "WHERE p.estado = 4 AND p.fechaHora BETWEEN ? AND ? " +
                "GROUP BY c.idclientes " +
                "HAVING monto_total > 0 " +
                "ORDER BY monto_total DESC " +
                "LIMIT ?";

        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
            ps.setInt(3, limite);

            logger.debug("Ejecutando query mejores clientes por monto taller desde " + fechaInicio + " hasta " + fechaFin + ", límite: " + limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setDni(rs.getInt("dni"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setApellido(rs.getString("apellido"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setCompras(rs.getFloat("monto_total")); // monto total del taller
                    clientes.add(cliente);
                }

                logger.info("Cantidad de mejores clientes encontrados (por monto en taller): " + clientes.size());
            }

        } catch (SQLException e) {
            logger.error("Error al obtener mejores clientes por monto en taller | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return clientes;
    }

}

