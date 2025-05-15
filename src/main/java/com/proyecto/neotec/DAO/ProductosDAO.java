package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;

import com.proyecto.neotec.models.Categoria;
import com.proyecto.neotec.models.Producto;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductosDAO {
    public List<Producto> selectAllProductos() {
        List<Producto> listaProductos = new ArrayList<>();
        String sql = "SELECT p.idproductos, p.codigoProducto, p.marca, p.idcategoria, p.cantidad, p.precioCosto, p.precioUnitario, p.descripcion, p.nombreProducto, c.nombre FROM productos p INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto producto = new Producto();
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


    public static String agregarProducto(Producto producto) {
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

    public static String modificarProducto(Producto producto) {
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

    public static void eliminarProductoSeleccionado(Producto producto) {
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

    public Producto obtenerProductoLinea(String codigo) {
        String query = "SELECT idproductos, nombreProducto, precioUnitario FROM productos WHERE codigoProducto = ?";
        Producto producto = null;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                producto = new Producto();
                producto.setIdProductos(rs.getInt("idproductos")); // 🚨 Asegurar que el ID se está asignando
                producto.setNombreProducto(rs.getString("nombreProducto"));
                producto.setPrecioUnitario(rs.getFloat("precioUnitario"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return producto;
    }

    public List<Producto> obtenerProductosPorPresupuesto(int idPresupuesto) {
        List<Producto> productos = new ArrayList<>();
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
                Producto producto = new Producto();
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


    public List<Producto> buscarPorCodigoProducto(String texto) {
        String sql = "SELECT p.*, c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.codigoProducto LIKE ?";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + texto + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdProductos(rs.getInt("idproductos"));
                    producto.setCodigoProducto(rs.getString("codigoProducto"));
                    producto.setMarca(rs.getString("marca"));
                    producto.setCantidad(rs.getInt("cantidad"));
                    producto.setPrecioCosto(rs.getInt("precioCosto")); // usar getFloat() o getBigDecimal() si es necesario
                    producto.setPrecioUnitario(rs.getInt("precioUnitario"));
                    producto.setDescripcion(rs.getString("descripcion"));
                    producto.setNombreProducto(rs.getString("nombreProducto"));
                    producto.setCategoriaString(rs.getString("nombre")); // nombre de la categoría
                    producto.setCategoriaInt(rs.getInt("idcategoria"));
                    productos.add(producto);
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return productos;
    }


    public List<Producto> buscarPorNombreProducto(String texto) {
        String sql = "SELECT p.*, c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.nombreProducto LIKE ?";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + texto + "%"); // Búsqueda parcial
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
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
                    productos.add(producto);
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return productos;
    }

    public List<Producto> buscarPorMarcaProducto(String texto) {
        String sql = "SELECT p.*, c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.marca LIKE ?";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + texto + "%"); // Búsqueda parcial
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
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
                    productos.add(producto);
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return productos;
    }

    public List<Producto> filtrarPorCategoria(int idCategoria) {
        List<Producto> productos = new ArrayList<>();
        String query = "SELECT p.idproductos, p.codigoProducto, p.marca, p.idcategoria, p.cantidad, " +
                "p.precioCosto, p.precioUnitario, p.descripcion, p.nombreProducto, " +
                "c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.idcategoria = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProductos(rs.getInt("idproductos"));
                producto.setCodigoProducto(rs.getString("codigoProducto"));
                producto.setMarca(rs.getString("marca"));
                producto.setCategoriaInt(rs.getInt("idcategoria"));
                producto.setCantidad(rs.getInt("cantidad"));
                producto.setPrecioCosto(rs.getInt("precioCosto")); // Si es DECIMAL, usar getBigDecimal()
                producto.setPrecioUnitario(rs.getInt("precioUnitario"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setNombreProducto(rs.getString("nombreProducto"));
                producto.setCategoriaString(rs.getString("nombre")); // nombre de la categoría
                productos.add(producto);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return productos;
    }


    public List<String> obtenerCategorias(List<String> estados) {
        String query = "SELECT * FROM categoriaproductos";
        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                estados.add(rs.getString("nombre"));
            }
        }catch (SQLException e ){
            Database.handleSQLException(e);
        }
        return  estados;
    }
    public int obtenerIDcategorias(String categoria) {
        int estado= -1;
        String query = "SELECT idcategoriaProductos FROM categoriaproductos WHERE nombre = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1,categoria);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                estado= rs.getInt("idcategoriaProductos");
            }
        }catch (SQLException e ){
            Database.handleSQLException(e);
        }
        return  estado;
    }


    public List<Producto> obtenerRankingDeVentas(int limite, int periodo, int criterio, LocalDate desde1, LocalDate hasta1) {
        List<Producto> productos = new ArrayList<>();
        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();

        // Determinar el rango de fechas
        if (desde1 != null && hasta1 != null) {
            fechaInicio = desde1;
            fechaFin = hasta1;
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
                    throw new IllegalArgumentException("Período no válido.");
            }
        }

        // Definir el campo de orden según el criterio (1: por dinero, 2: por cantidad de unidades)
        String campoOrden = (criterio == 1) ? "SUM(ptv.cantidad * p.precioUnitario)" : "SUM(ptv.cantidad)";
        String aliasCampo = (criterio == 1) ? "total_dinero" : "total_unidades";

        // Consultar productos vendidos y productos utilizados en presupuestos pagados dentro del rango de fechas
        String sql = "SELECT p.idproductos, p.nombreProducto, " + campoOrden + " AS " + aliasCampo + " " +
                "FROM productos p " +
                // Relación con ventas
                "LEFT JOIN productosticketsventas ptv ON p.idproductos = ptv.idproducto " +
                "LEFT JOIN ticketsventa t ON ptv.idventa = t.idticketsventa " +
                // Relación con presupuestos pagados
                "LEFT JOIN productopresupuesto pp ON p.idproductos = pp.IDproductos " +
                "LEFT JOIN presupuestos ps ON pp.IDpresupuestos = ps.idpresupuestos " +
                "WHERE (t.fechaHora BETWEEN ? AND ? OR ps.estado = 4) " +  // Filtrar ventas y presupuestos pagados
                "GROUP BY p.idproductos " +
                "HAVING " + aliasCampo + " > 0 " +
                "ORDER BY " + aliasCampo + " DESC " +
                "LIMIT ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Establecer los parámetros de la consulta
            ps.setDate(1, java.sql.Date.valueOf(fechaInicio));
            ps.setDate(2, java.sql.Date.valueOf(fechaFin));
            ps.setInt(3, limite);

            // Ejecutar la consulta y procesar los resultados
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdproductos(rs.getInt("idproductos"));
                    producto.setNombreProducto(rs.getString("nombreProducto"));
                    producto.setVentas((float) (criterio == 1 ? rs.getDouble("total_dinero") : rs.getInt("total_unidades")));
                    productos.add(producto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productos;
    }

    public List<Categoria> obtenerRankingDeCategorias(int limite, int periodo, int criterio, LocalDate desde2, LocalDate hasta2) {
        List<Categoria> categorias = new ArrayList<>();
        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();

        if (desde2 != null && hasta2 != null) {
            fechaInicio = desde2;
            fechaFin = hasta2;
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
                    throw new IllegalArgumentException("Período no válido.");
            }
        }

        String campoOrden = (criterio == 1) ? "SUM(total_dinero)" : "SUM(total_unidades)";
        String aliasCampo = (criterio == 1) ? "total_dinero" : "total_unidades";

        String sql = "SELECT cp.idcategoriaProductos AS idcategoria, cp.nombre, " +
                "SUM(COALESCE(v.total_dinero, 0) + COALESCE(pz.total_dinero, 0)) AS total_dinero, " +
                "SUM(COALESCE(v.total_unidades, 0) + COALESCE(pz.total_unidades, 0)) AS total_unidades " +
                "FROM categoriaproductos cp " +
                "LEFT JOIN productos pr ON cp.idcategoriaProductos = pr.idcategoria " +

                // Subconsulta de ventas
                "LEFT JOIN ( " +
                "  SELECT ptv.idproducto, " +
                "         SUM(ptv.cantidad * pr.precioUnitario) AS total_dinero, " +
                "         SUM(ptv.cantidad) AS total_unidades " +
                "  FROM productosticketsventas ptv " +
                "  INNER JOIN ticketsventa tv ON ptv.idventa = tv.idticketsventa " +
                "  INNER JOIN productos pr ON ptv.idproducto = pr.idproductos " +
                "  WHERE tv.fechaHora BETWEEN ? AND ? " +
                "  GROUP BY ptv.idproducto " +
                ") v ON pr.idproductos = v.idproducto " +

                // Subconsulta de presupuestos pagados
                "LEFT JOIN ( " +
                "  SELECT pp.IDproductos AS idproducto, " +
                "         SUM(pp.cantidadUtilizada * pr.precioUnitario) AS total_dinero, " +
                "         SUM(pp.cantidadUtilizada) AS total_unidades " +
                "  FROM productopresupuesto pp " +
                "  INNER JOIN presupuestos pz ON pp.IDpresupuestos = pz.idpresupuestos " +
                "  INNER JOIN productos pr ON pp.IDproductos = pr.idproductos " +
                "  WHERE pz.estado = 4 AND pz.fechaHora BETWEEN ? AND ? " +
                "  GROUP BY pp.IDproductos " +
                ") pz ON pr.idproductos = pz.idproducto " +

                "GROUP BY cp.idcategoriaProductos " +
                "HAVING " + aliasCampo + " > 0 " +
                "ORDER BY " + aliasCampo + " DESC " +
                "LIMIT ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Establecer parámetros
            ps.setDate(1, java.sql.Date.valueOf(fechaInicio)); // para ventas
            ps.setDate(2, java.sql.Date.valueOf(fechaFin));
            ps.setDate(3, java.sql.Date.valueOf(fechaInicio)); // para presupuestos
            ps.setDate(4, java.sql.Date.valueOf(fechaFin));
            ps.setInt(5, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Categoria categoria = new Categoria();
                    categoria.setIdCategoria(rs.getInt("idcategoria"));
                    categoria.setNombreCategoria(rs.getString("nombre"));
                    categoria.setTotal((float) (criterio == 1 ? rs.getDouble("total_dinero") : rs.getInt("total_unidades")));
                    categorias.add(categoria);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categorias;
    }


}
