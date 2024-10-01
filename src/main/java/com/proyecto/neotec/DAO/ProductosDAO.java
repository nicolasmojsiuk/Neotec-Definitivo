package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;

import com.proyecto.neotec.models.Productos;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductosDAO {
    public List<Productos> selectAllProductos() {
        List<Productos> Productos = new ArrayList<>();
        String sql = "SELECT idproductos,codigoProducto, marca, cantidad, precioCosto, precioUnitario, descripcion, nombreProducto FROM productos";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Productos productos = new Productos();
                productos.setIdProductos(rs.getInt("idproductos"));
                productos.setCodigoProducto(rs.getString("codigoProducto"));
                productos.setMarca(rs.getString("marca"));
                productos.setCantidad(rs.getInt("cantidad"));
                productos.setPrecioCosto(rs.getInt("precioCosto"));
                productos.setPrecioUnitario(rs.getInt("precioUnitario"));
                productos.setDescripcion(rs.getString("descripcion"));
                productos.setNombreProducto(rs.getString("nombreProducto"));

                Productos.add(productos);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return Productos;
    }
    public static String agregarProducto(Productos producto) {
        String sql = "INSERT INTO productos (codigoProducto, marca, cantidad, precioCosto, precioUnitario, descripcion,nombreProducto) VALUES (?, ?, ?, ?, ?, ?, ?)";
        if (producto.getCodigoProducto() == null || producto.getCodigoProducto().isEmpty()) {
            throw new IllegalArgumentException("El código del producto no puede ser nulo o vacío");
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) {
                return "Error: No se pudo establecer la conexión con la base de datos.";
            }

            pstmt.setString(1, producto.getCodigoProducto());
            pstmt.setString(2, producto.getMarca());
            pstmt.setInt(3, producto.getCantidad());
            pstmt.setInt(4, producto.getPrecioCosto());
            pstmt.setInt(5, producto.getPrecioUnitario());
            pstmt.setString(6, producto.getDescripcion());
            pstmt.setString(7, producto.getNombreProducto());

            pstmt.executeUpdate();
            return "Éxito";
        } catch (SQLException e) {
            Database.handleSQLException(e);
            return "Error: " + e.getMessage();
        }
    }

    public static String modificarProducto( Productos producto) {
        String mensaje = "";
        String sql = "UPDATE productos SET codigoProducto = ?, marca = ?, cantidad = ?, precioCosto = ?, precioUnitario = ?,descripcion =?,nombreProducto= ? WHERE idproductos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer los valores en la sentencia preparada
            stmt.setString(1, producto.getCodigoProducto());
            stmt.setString(2, producto.getMarca());
            stmt.setInt(3, producto.getCantidad());
            stmt.setInt(4, producto.getPrecioCosto());
            stmt.setInt(5, producto.getPrecioUnitario());
            stmt.setString(6, producto.getDescripcion());
            stmt.setString(7, producto.getNombreProducto());
            stmt.setInt(8, producto.getIdProductos());

            // Ejecutar la actualización
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                mensaje = "Producto modificado exitosamente.";
            } else {
                mensaje = "No se pudo modificar el Producto. Verifique si el producto existe.";
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return mensaje;
    }
    public static void eliminarProductoSeleccionado(Productos producto) {
        String mensaje = "";
        String sql = "DELETE FROM productos WHERE codigoProducto = ?";

        // Mostrar un diálogo de confirmación para el usuario
        int confirmacion = JOptionPane.showConfirmDialog(null,
                "¿Estás seguro de que deseas eliminar el producto con código: " + producto.getCodigoProducto() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        // Si el usuario confirma (opción "Sí")
        if (confirmacion == JOptionPane.YES_OPTION) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, producto.getCodigoProducto());
                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    mensaje = "Producto eliminado exitosamente.";
                } else {
                    mensaje = "No se encontró ningún producto con el código especificado.";
                }

            } catch (SQLException e) {
                Database.handleSQLException(e);
            }
        } else {
            mensaje = "Eliminación cancelada por el usuario.";
        }

    }

}
