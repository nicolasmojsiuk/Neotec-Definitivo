package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Presupuestos;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
public class TicketVentaDAO {
    private static final Logger logger = Logger.getLogger(TicketVentaDAO.class);
    public int insertTicket(int idcliente, int idusuario, float total, String rutaTicket, LocalDateTime fechayhora) {
        String query = "INSERT INTO ticketsventa (idCliente, idUsuarioVendedor, total, rutaTicket, fechaHora) VALUES (?, ?, ?, ?, ?)";
        int IDgenerado = -1; // Inicializar el ID generado

        logger.info("Intentando insertar nuevo ticket de venta...");
        logger.info("Parámetros recibidos - idCliente: " + idcliente +
                ", idUsuarioVendedor: " + idusuario +
                ", total: " + total +
                ", rutaTicket: " + rutaTicket +
                ", fechaHora: " + fechayhora);

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idcliente);
            pstmt.setInt(2, idusuario);
            pstmt.setFloat(3, total);
            pstmt.setString(4, rutaTicket);
            pstmt.setTimestamp(5, Timestamp.valueOf(fechayhora)); // Conversión correcta

            // Ejecutar la consulta de inserción
            int affectedRows = pstmt.executeUpdate();
            logger.debug("Filas afectadas por la inserción: " + affectedRows);

            // Verificar si la inserción afectó filas
            if (affectedRows > 0) {
                // Obtener el ID generado
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        IDgenerado = rs.getInt(1); // Obtener el primer valor generado (idticketventa)
                        logger.info("Ticket insertado correctamente con ID: " + IDgenerado);
                    }
                }
            } else {
                logger.warn("La inserción no afectó ninguna fila");
            }

        } catch (SQLException e) {
            logger.error("Error al insertar el ticket de venta: " + e.getMessage(), e);
            Database.handleSQLException(e);
        }

        return IDgenerado;
    }


    public void insertProductoTicketVenta(int idVenta, int idProducto, int cantidad) {
        String query = "INSERT INTO productosticketsventas (idventa, idproducto, cantidad) VALUES(?, ?, ?)";

        logger.info("Intentando insertar producto en ticket de venta...");
        logger.info("Parámetros recibidos - idVenta: " + idVenta +
                ", idProducto: " + idProducto +
                ", cantidad: " + cantidad);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

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
            Database.handleSQLException(e);
        }
    }



}
