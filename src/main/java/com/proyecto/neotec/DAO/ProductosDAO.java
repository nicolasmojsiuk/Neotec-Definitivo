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
        List<Productos> listaProductos = new ArrayList<>();
        String sql = "SELECT p.idproductos, p.codigoProducto, p.marca, p.idcategoria, p.cantidad, p.precioCosto, p.precioUnitario, p.descripcion, p.nombreProducto, c.nombre FROM productos p INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Productos producto = new Productos();
                producto.setIdProductos(rs.getInt("idproductos"));
                producto.setCodigoProducto(rs.getString("codigoProducto"));
                producto.setMarca(rs.getString("marca"));
                producto.setCantidad(rs.getInt("cantidad"));
                producto.setPrecioCosto(rs.getInt("precioCosto"));
                producto.setPrecioUnitario(rs.getInt("precioUnitario"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setNombreProducto(rs.getString("nombreProducto"));
                producto.setCategoriaString(rs.getString("nombre"));
                producto.setCategoriaInt(rs.getInt("idcategoria"));
                listaProductos.add(producto);

            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return listaProductos;
    }


    public static String agregarProducto(Productos producto) {
        String sql = "INSERT INTO productos (codigoProducto, marca, cantidad, precioCosto, precioUnitario, descripcion,nombreProducto,idcategoria) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.setFloat(4, producto.getPrecioCosto());
            pstmt.setFloat(5, producto.getPrecioUnitario());
            pstmt.setString(6, producto.getDescripcion());
            pstmt.setString(7, producto.getNombreProducto());
            pstmt.setInt(8, producto.getCategoriaInt());
            pstmt.executeUpdate();
            return "Éxito";
        } catch (SQLException e) {
            Database.handleSQLException(e);
            return "Error: " + e.getMessage();
        }
    }

    public static String modificarProducto(Productos producto) {
        String mensaje = "";
        String sql = "UPDATE productos SET codigoProducto = ?, marca = ?, cantidad = ?, precioCosto = ?, precioUnitario = ?,descripcion =?,nombreProducto= ?,idcategoria=? WHERE idproductos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer los valores en la sentencia preparada
            stmt.setString(1, producto.getCodigoProducto());
            stmt.setString(2, producto.getMarca());
            stmt.setInt(3, producto.getCantidad());
            stmt.setFloat(4, producto.getPrecioCosto());
            stmt.setFloat(5, producto.getPrecioUnitario());
            stmt.setString(6, producto.getDescripcion());
            stmt.setString(7, producto.getNombreProducto());
            stmt.setInt(8, producto.getCategoriaInt());
            stmt.setInt(9, producto.getIdProductos());

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


    public void descontarStock(int idProductos, int cantidadUtilizada) {
        // Obtener la cantidad total del producto
        int cantidad_stock = obtenerCantidad(idProductos);
        // Verificar si la cantidad es suficiente
        if (cantidad_stock < cantidadUtilizada) {
            System.out.println("Error: no hay suficiente stock para descontar");
            return; // No continuar si no hay suficiente stock
        }
        // Restar la cantidad utilizada del stock actual
        cantidad_stock = cantidad_stock - cantidadUtilizada;
        String sql = "UPDATE productos SET cantidad = ? WHERE idproductos = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cantidad_stock);
            stmt.setInt(2, idProductos);
            stmt.executeUpdate();

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
    }

    public int obtenerCantidad(int idProductos) {
        int cantidad = 0;
        String sql = "SELECT cantidad FROM productos WHERE idproductos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProductos);
            ResultSet rs = stmt.executeQuery();

            // Si existe un resultado, asignar la cantidad
            if (rs.next()) {
                cantidad = rs.getInt("cantidad");

            } else {

            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return cantidad;
    }

    public int obtenerIDconCodigoProducto(String codigo) {
        String query = "SELECT idProductos FROM productos WHERE codigoProducto = ?";
        int idProducto = -1; // Inicializar con un valor predeterminado en caso de error

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, codigo); // Asignar el código del producto al query
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idProducto = rs.getInt("idProductos"); // Obtener el ID del producto
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return idProducto; // Retornar el ID del producto o -1 si no se encontró
    }

    public List<String> selectNombresCategorias() {
        List<String> nombresCategorias = new ArrayList<>();
        String sql = "SELECT nombre FROM categoriaproductos";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                nombresCategorias.add(rs.getString("nombre")); // Asegúrate de que 'nombre' es el nombre correcto de la columna
            }
        } catch (SQLException e) {
            Database.handleSQLException(e); // Manejo de excepción
        }

        return nombresCategorias;
    }

    public Productos obtenerProductoLinea(String codigo) {
        String query = "SELECT idproductos, nombreProducto, precioUnitario FROM productos WHERE codigoProducto = ?";
        Productos producto = null;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                producto = new Productos();
                producto.setIdProductos(rs.getInt("idproductos")); // 🚨 Asegurar que el ID se está asignando
                producto.setNombreProducto(rs.getString("nombreProducto"));
                producto.setPrecioUnitario(rs.getFloat("precioUnitario"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return producto;
    }

    public List<Productos> obtenerProductosPorPresupuesto(int idPresupuesto) {
        List<Productos> productos = new ArrayList<>();
        String query = "SELECT p.idproductos, p.nombreProducto, p.precioUnitario, pp.cantidadUtilizada " +
                "FROM productopresupuesto pp " +
                "JOIN productos p ON pp.IDproductos = p.idproductos " +
                "WHERE pp.IDpresupuestos = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, idPresupuesto);
            ResultSet rs = stmt.executeQuery();

            System.out.println("🔍 Buscando productos para el presupuesto ID: " + idPresupuesto);

            while (rs.next()) {
                Productos producto = new Productos();
                producto.setIdProductos(rs.getInt("idproductos"));
                producto.setNombreProducto(rs.getString("nombreProducto"));
                producto.setPrecioUnitario(rs.getFloat("precioUnitario"));
                producto.setCantidad(rs.getInt("cantidadUtilizada")); // Asigna la cantidad utilizada

                productos.add(producto);
                System.out.println("✅ Producto agregado: ID=" + producto.getIdProductos() + " - " + producto.getNombreProducto());
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return productos;
    }

    public boolean productoExiste(int idProducto) {
        String query = "SELECT COUNT(*) FROM productos WHERE idproductos = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, idProducto);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
