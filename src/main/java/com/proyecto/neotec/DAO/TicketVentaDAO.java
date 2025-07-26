package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Presupuestos;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.proyecto.neotec.models.Producto;
import com.proyecto.neotec.models.TicketVenta;
import org.apache.log4j.Logger;
public class TicketVentaDAO {
    private static final Logger logger = Logger.getLogger(TicketVentaDAO.class);
    //
    public int insertTicket(int idcliente, int idusuario, float total, LocalDateTime fechayhora) {
        String query = "INSERT INTO ticketsventa (idCliente, idUsuarioVendedor, total, fechaHora) VALUES (?, ?, ?, ?)";
        int IDgenerado = -1;

        logger.debug("Preparando inserción de ticket: idCliente=" + idcliente + ", idUsuario=" + idusuario +
                ", total=" + total + ", fechaHora=" + fechayhora);

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idcliente);
            pstmt.setInt(2, idusuario);
            pstmt.setFloat(3, total);
            pstmt.setTimestamp(4, Timestamp.valueOf(fechayhora));

            logger.debug("Ejecutando inserción en la base de datos.");
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                logger.debug("Inserción exitosa. Obteniendo ID generado.");
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        IDgenerado = rs.getInt(1);
                        logger.info("Ticket insertado correctamente con ID generado: " + IDgenerado);
                    } else {
                        logger.warn("No se pudo obtener el ID generado después de la inserción.");
                    }
                }
            } else {
                logger.warn("La inserción del ticket no afectó ninguna fila.");
            }

        } catch (SQLException e) {
            logger.error("Error al insertar ticket en la base de datos.", e);
            Database.handleSQLException(e);
        }

        return IDgenerado;
    }


    public void establecerRutaTicketPDF(String rutaPDF, int numeroTicket) {
        String query = "UPDATE ticketsventa SET rutaTicket = ? WHERE idticketsventa = ?";
        int filasActualizadas = 0;

        logger.debug("Iniciando actualización de la ruta del ticket PDF. Ticket N°: " + numeroTicket + ", Ruta: " + rutaPDF);

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, rutaPDF);
            pstmt.setInt(2, numeroTicket);

            filasActualizadas = pstmt.executeUpdate();

            if (filasActualizadas > 0) {
                logger.info("Ruta del ticket PDF actualizada correctamente para el ticket N°: " + numeroTicket);
            } else {
                logger.warn("No se encontró el ticket N°: " + numeroTicket + " para actualizar la ruta del PDF.");
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar la ruta del ticket PDF para el ticket N°: " + numeroTicket);
            Database.handleSQLException(e);
        }
    }

    public boolean registrarProductosEnTransaccion(int numeroDeTicket, List<Producto> productos) {
        ProductosDAO productosDAO = new ProductosDAO();
        Connection conn = null;

        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            for (Producto producto : productos) {
                int idProducto = productosDAO.obtenerIDconCodigoProducto(producto.getCodigoProducto());
                insertProductoTicketVenta(conn, numeroDeTicket, idProducto, producto.getCantidad());
            }

            conn.commit(); // Todo bien
            logger.info("Todos los productos registrados correctamente.");
            return true;

        } catch (SQLException e) {
            logger.error("Error al registrar productos. Ejecutando rollback.", e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Rollback realizado exitosamente.");
                } catch (SQLException rollbackEx) {
                    logger.error("Error al hacer rollback.", rollbackEx);
                }
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar el estado por defecto
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error al cerrar la conexión luego del rollback.", closeEx);
                }
            }
        }
    }
    public String obtenerRutaTicket(int idticket) {
        String sql = "SELECT rutaTicket FROM ticketsventa WHERE idticketsventa = ?";
        String ruta = null;

        logger.debug("Iniciando obtención de la ruta del ticket PDF para el ticket N°: " + idticket);

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idticket);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ruta = rs.getString("rutaTicket");
                logger.info("Ruta del ticket obtenida correctamente para el ticket N°: " + idticket + ". Ruta: " + ruta);
            } else {
                logger.warn("No se encontró el ticket N°: " + idticket + " al intentar obtener la ruta del PDF.");
            }

        } catch (SQLException e) {
            logger.error("Error al obtener la ruta del ticket PDF para el ticket N°: " + idticket + ". Detalles: " + e.getMessage());
            Database.handleSQLException(e);
        }

        return ruta;
    }


    public void insertProductoTicketVenta(Connection conn, int idVenta, int idProducto, int cantidad) throws SQLException {
        String query = "INSERT INTO productosticketsventas (idventa, idproducto, cantidad) VALUES(?, ?, ?)";
        logger.info("Intentando insertar producto en ticket de venta...");
        logger.info("Parámetros recibidos - idVenta: " + idVenta +
                ", idProducto: " + idProducto +
                ", cantidad: " + cantidad);

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idVenta);
            stmt.setInt(2, idProducto);
            stmt.setInt(3, cantidad);

            int affectedRows = stmt.executeUpdate();
            logger.debug("Filas afectadas por la inserción: " + affectedRows);

            if (affectedRows > 0) {
                logger.info("Producto insertado correctamente en el ticket de venta");
            } else {
                logger.warn("La inserción no afectó ninguna fila");
            }

        } catch (SQLException e) {
            logger.error("Error al insertar producto en ticket de venta. " +
                    "idVenta: " + idVenta + ", idProducto: " + idProducto +
                    " - Error: " + e.getMessage(), e);
            throw e; // Importante: relanzar para que el que llama pueda hacer rollback
        }
    }


    public List<TicketVenta> selectAllTickets() {
        List<TicketVenta> listaTickets = new ArrayList<>();

        String query = "SELECT t.idticketsventa, t.total, t.fechaHora, t.rutaTicket, " +
                "c.idclientes AS idCliente, c.nombre AS nombreCliente, c.dni AS dniCliente, " +
                "u.idusuarios AS idUsuarioVendedor, u.nombre AS nombreVendedor, u.dni AS dniVendedor " +
                "FROM ticketsventa t " +
                "JOIN clientes c ON t.idCliente = c.idclientes " +
                "JOIN usuarios u ON t.idUsuarioVendedor = u.idusuarios";

        logger.debug("Iniciando selección de todos los tickets de venta.");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            logger.debug("Consulta ejecutada correctamente. Procesando resultados.");

            while (rs.next()) {
                TicketVenta ticket = new TicketVenta();

                ticket.setIdTicketVenta(rs.getInt("idticketsventa"));
                ticket.setTotal(rs.getFloat("total"));
                ticket.setFechayhora(rs.getTimestamp("fechaHora").toLocalDateTime());
                ticket.setRutaTicket(rs.getString("rutaTicket"));

                ticket.setIdCliente(rs.getInt("idCliente"));
                ticket.setCliente(rs.getString("nombreCliente"));
                ticket.setDniCliente(rs.getString("dniCliente"));

                ticket.setIdUsuarioVendedor(rs.getInt("idUsuarioVendedor"));
                ticket.setVendedor(rs.getString("nombreVendedor"));
                ticket.setDniVendedor(rs.getString("dniVendedor"));

                listaTickets.add(ticket);
            }

            logger.info("Se recuperaron " + listaTickets.size() + " tickets de venta.");

        } catch (SQLException e) {
            logger.error("Error al recuperar tickets de venta desde la base de datos.", e);
        }

        return listaTickets;
    }

    public List<TicketVenta> buscarTicketsPorVendedor(String vendedor, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<TicketVenta> listaTickets = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT t.idticketsventa, t.total, t.fechaHora, t.rutaTicket, " +
                        "c.idclientes AS idCliente, c.nombre AS nombreCliente, c.dni AS dniCliente, " +
                        "u.idusuarios AS idUsuarioVendedor, u.nombre AS nombreVendedor, u.dni AS dniVendedor " +
                        "FROM ticketsventa t " +
                        "JOIN clientes c ON t.idCliente = c.idclientes " +
                        "JOIN usuarios u ON t.idUsuarioVendedor = u.idusuarios " +
                        "WHERE (u.nombre LIKE ? OR u.dni LIKE ?)"
        );

        if (fechaDesde != null) {
            query.append(" AND t.fechaHora >= ? ");
        }
        if (fechaHasta != null) {
            query.append(" AND t.fechaHora <= ? ");
        }

        logger.debug("Buscando tickets por vendedor. Parámetros: vendedor='" + vendedor + "', fechaDesde=" + fechaDesde + ", fechaHasta=" + fechaHasta);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;

            stmt.setString(paramIndex++, "%" + vendedor + "%");
            stmt.setString(paramIndex++, "%" + vendedor + "%");

            if (fechaDesde != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaHasta.atTime(23, 59, 59)));
            }

            logger.debug("Ejecutando consulta preparada con filtros aplicados.");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TicketVenta ticket = new TicketVenta();

                ticket.setIdTicketVenta(rs.getInt("idticketsventa"));
                ticket.setTotal(rs.getFloat("total"));
                ticket.setFechayhora(rs.getTimestamp("fechaHora").toLocalDateTime());
                ticket.setRutaTicket(rs.getString("rutaTicket"));

                ticket.setIdCliente(rs.getInt("idCliente"));
                ticket.setCliente(rs.getString("nombreCliente"));
                ticket.setDniCliente(rs.getString("dniCliente"));

                ticket.setIdUsuarioVendedor(rs.getInt("idUsuarioVendedor"));
                ticket.setVendedor(rs.getString("nombreVendedor"));
                ticket.setDniVendedor(rs.getString("dniVendedor"));

                listaTickets.add(ticket);
            }

            logger.info("Se encontraron " + listaTickets.size() + " tickets que coinciden con los filtros.");

        } catch (SQLException e) {
            logger.error("Error al buscar tickets por vendedor.", e);
        }

        return listaTickets;
    }
    public List<TicketVenta> buscarTicketsPorCliente(String newValue, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<TicketVenta> listaTickets = new ArrayList<>();

        String query = "SELECT t.idticketsventa, t.total, t.fechaHora, t.rutaTicket, " +
                "c.idclientes AS idCliente, c.nombre AS nombreCliente, c.dni AS dniCliente, " +
                "u.idusuarios AS idUsuarioVendedor, u.nombre AS nombreVendedor, u.dni AS dniVendedor " +
                "FROM ticketsventa t " +
                "JOIN clientes c ON t.idCliente = c.idclientes " +
                "JOIN usuarios u ON t.idUsuarioVendedor = u.idusuarios " +
                "WHERE (c.nombre LIKE ? OR c.dni LIKE ?)";

        if (fechaDesde != null) {
            query += " AND t.fechaHora >= ?";
        }
        if (fechaHasta != null) {
            query += " AND t.fechaHora <= ?";
        }

        logger.debug("Buscando tickets por cliente. Parámetros: filtro='" + newValue + "', fechaDesde=" + fechaDesde + ", fechaHasta=" + fechaHasta);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;

            stmt.setString(paramIndex++, "%" + newValue + "%");
            stmt.setString(paramIndex++, "%" + newValue + "%");

            if (fechaDesde != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaHasta.atTime(23, 59, 59)));
            }

            logger.debug("Ejecutando consulta para buscar tickets por cliente...");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TicketVenta ticket = new TicketVenta();

                ticket.setIdTicketVenta(rs.getInt("idticketsventa"));
                ticket.setTotal(rs.getFloat("total"));
                ticket.setFechayhora(rs.getTimestamp("fechaHora").toLocalDateTime());
                ticket.setRutaTicket(rs.getString("rutaTicket"));

                ticket.setIdCliente(rs.getInt("idCliente"));
                ticket.setCliente(rs.getString("nombreCliente"));
                ticket.setDniCliente(rs.getString("dniCliente"));

                ticket.setIdUsuarioVendedor(rs.getInt("idUsuarioVendedor"));
                ticket.setVendedor(rs.getString("nombreVendedor"));
                ticket.setDniVendedor(rs.getString("dniVendedor"));

                listaTickets.add(ticket);
            }

            logger.info("Tickets encontrados por cliente: " + listaTickets.size());

        } catch (SQLException e) {
            logger.error("Error al buscar tickets por cliente.", e);
        }

        return listaTickets;
    }
    public List<TicketVenta> buscarTicketsPorNumero(String newValue, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<TicketVenta> listaTickets = new ArrayList<>();

        String query = "SELECT t.idticketsventa, t.total, t.fechaHora, t.rutaTicket, " +
                "c.idclientes AS idCliente, c.nombre AS nombreCliente, c.dni AS dniCliente, " +
                "u.idusuarios AS idUsuarioVendedor, u.nombre AS nombreVendedor, u.dni AS dniVendedor " +
                "FROM ticketsventa t " +
                "JOIN clientes c ON t.idCliente = c.idclientes " +
                "JOIN usuarios u ON t.idUsuarioVendedor = u.idusuarios " +
                "WHERE CAST(t.idticketsventa AS CHAR) LIKE ?";

        if (fechaDesde != null) {
            query += " AND t.fechaHora >= ?";
        }
        if (fechaHasta != null) {
            query += " AND t.fechaHora <= ?";
        }

        logger.debug("Buscando tickets por número. Filtro: '" + newValue + "', fechaDesde=" + fechaDesde + ", fechaHasta=" + fechaHasta);
        logger.debug("Query construida: " + query);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;

            stmt.setString(paramIndex++, "%" + newValue + "%");

            if (fechaDesde != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaHasta.atTime(23, 59, 59)));
            }

            logger.debug("Ejecutando consulta de tickets por número...");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TicketVenta ticket = new TicketVenta();

                ticket.setIdTicketVenta(rs.getInt("idticketsventa"));
                ticket.setTotal(rs.getFloat("total"));
                ticket.setFechayhora(rs.getTimestamp("fechaHora").toLocalDateTime());
                ticket.setRutaTicket(rs.getString("rutaTicket"));

                ticket.setIdCliente(rs.getInt("idCliente"));
                ticket.setCliente(rs.getString("nombreCliente"));
                ticket.setDniCliente(rs.getString("dniCliente"));

                ticket.setIdUsuarioVendedor(rs.getInt("idUsuarioVendedor"));
                ticket.setVendedor(rs.getString("nombreVendedor"));
                ticket.setDniVendedor(rs.getString("dniVendedor"));

                listaTickets.add(ticket);
            }

            logger.info("Cantidad de tickets encontrados por número: " + listaTickets.size());

        } catch (SQLException e) {
            logger.error("Error al buscar tickets por número de ticket.", e);
        }

        return listaTickets;
    }
    public List<TicketVenta> buscarTicketsPorFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<TicketVenta> listaTickets = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT t.idticketsventa, t.total, t.fechaHora, t.rutaTicket, " +
                        "c.idclientes AS idCliente, c.nombre AS nombreCliente, c.dni AS dniCliente, " +
                        "u.idusuarios AS idUsuarioVendedor, u.nombre AS nombreVendedor, u.dni AS dniVendedor " +
                        "FROM ticketsventa t " +
                        "JOIN clientes c ON t.idCliente = c.idclientes " +
                        "JOIN usuarios u ON t.idUsuarioVendedor = u.idusuarios "
        );

        boolean tieneWhere = false;
        if (fechaDesde != null) {
            query.append(" WHERE t.fechaHora >= ? ");
            tieneWhere = true;
        }
        if (fechaHasta != null) {
            if (tieneWhere) {
                query.append(" AND t.fechaHora <= ? ");
            } else {
                query.append(" WHERE t.fechaHora <= ? ");
                tieneWhere = true;
            }
        }

        logger.debug("Buscando tickets entre fechas. Desde: " + fechaDesde + ", Hasta: " + fechaHasta);
        logger.debug("Query generada: " + query);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (fechaDesde != null) {
                stmt.setTimestamp(paramIndex++, java.sql.Timestamp.valueOf(fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                stmt.setTimestamp(paramIndex++, java.sql.Timestamp.valueOf(fechaHasta.atTime(23, 59, 59)));
            }

            logger.debug("Ejecutando consulta para búsqueda por fechas...");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TicketVenta ticket = new TicketVenta();

                ticket.setIdTicketVenta(rs.getInt("idticketsventa"));
                ticket.setTotal(rs.getFloat("total"));
                ticket.setFechayhora(rs.getTimestamp("fechaHora").toLocalDateTime());
                ticket.setRutaTicket(rs.getString("rutaTicket"));

                ticket.setIdCliente(rs.getInt("idCliente"));
                ticket.setCliente(rs.getString("nombreCliente"));
                ticket.setDniCliente(rs.getString("dniCliente"));

                ticket.setIdUsuarioVendedor(rs.getInt("idUsuarioVendedor"));
                ticket.setVendedor(rs.getString("nombreVendedor"));
                ticket.setDniVendedor(rs.getString("dniVendedor"));

                listaTickets.add(ticket);
            }

            logger.info("Cantidad de tickets encontrados entre fechas: " + listaTickets.size());

        } catch (SQLException e) {
            logger.error("Error al buscar tickets por fechas.", e);
        }

        return listaTickets;
    }

}
