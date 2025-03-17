package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Presupuestos;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketVentaDAO {
    public int insertTicket(int idcliente, int idusuario, float total, String rutaTicket, LocalDateTime fechayhora) {
        String query = "INSERT INTO ticketsventa (idCliente, idUsuarioVendedor, total, rutaTicket, fechaHora) VALUES (?, ?, ?, ?, ?)";
        int IDgenerado = -1; // Inicializar el ID generado

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idcliente);
            pstmt.setInt(2, idusuario);
            pstmt.setFloat(3, total);
            pstmt.setString(4, rutaTicket);
            pstmt.setTimestamp(5, Timestamp.valueOf(fechayhora)); // Conversión correcta

            // Ejecutar la consulta de inserción
            int affectedRows = pstmt.executeUpdate();

            // Verificar si la inserción afectó filas
            if (affectedRows > 0) {
                // Obtener el ID generado
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        IDgenerado = rs.getInt(1); // Obtener el primer valor generado (idticketventa)
                    }
                }
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return IDgenerado;
    }



    public void insertProductoTicketVenta(int idVenta, int idProducto, int cantidad) {
        String query = "INSERT INTO productosticketsventas (idventa, idproducto, cantidad) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
        ){
            stmt.setInt(1,idVenta);
            stmt.setInt(2,idProducto);
            stmt.setInt(3,cantidad);
            stmt.executeUpdate();
        }catch (SQLException e){
            Database.handleSQLException(e);
        }
    }



}
