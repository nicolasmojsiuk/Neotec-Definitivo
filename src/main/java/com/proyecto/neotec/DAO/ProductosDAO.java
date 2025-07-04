package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;

import com.proyecto.neotec.models.Categoria;
import com.proyecto.neotec.models.Producto;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductosDAO {
    private static final Logger logger = Logger.getLogger(ProductosDAO.class);
    public List<Producto> selectAllProductos() {
        logger.debug("Obteniendo todos los productos desde la base de datos");
        List<Producto> listaProductos = new ArrayList<>();
        String sql = "SELECT p.idproductos, p.codigoProducto, p.marca, p.idcategoria, p.cantidad, p.precioCosto, p.precioUnitario, p.descripcion, p.nombreProducto, c.nombre FROM productos p INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            logger.info("Conexión y ejecución de consulta exitosa");

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

            logger.info("Productos cargados: " + listaProductos.size());

        } catch (SQLException e) {
            logger.error("Error al obtener los productos", e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método selectAllProductos");
        return listaProductos;
    }

    public static String agregarProducto(Producto producto) {
        logger.info("Agregando productos a la base de datos..");

        String sql = "INSERT INTO productos (codigoProducto, marca, cantidad, precioCosto, precioUnitario, descripcion,nombreProducto,idcategoria) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        if (producto.getCodigoProducto() == null || producto.getCodigoProducto().isEmpty()) {
            logger.warn("Código de producto nulo o vacío");
            throw new IllegalArgumentException("El código del producto no puede ser nulo o vacío");
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                logger.error("No se pudo establecer la conexión con la base de datos.");
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

            int filasAfectadas = pstmt.executeUpdate();
            logger.info("Producto insertado correctamente. Filas afectadas: " + filasAfectadas);
            return "Éxito";

        } catch (SQLException e) {
            logger.error("Error al insertar producto", e);
            Database.handleSQLException(e);
            return "Error: " + e.getMessage();
        }
    }

    public static String modificarProducto(Producto producto) {
        logger.info("Intentando modificar los datos del producto con ID:"+ producto.getIdProductos());

        String mensaje = "";
        String sql = "UPDATE productos SET codigoProducto = ?, marca = ?, cantidad = ?, precioCosto = ?, precioUnitario = ?, descripcion = ?, nombreProducto = ?, idcategoria = ? WHERE idproductos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.debug("Preparando sentencia SQL para actualizar producto con código:" + producto.getCodigoProducto());

            stmt.setString(1, producto.getCodigoProducto());
            stmt.setString(2, producto.getMarca());
            stmt.setInt(3, producto.getCantidad());
            stmt.setFloat(4, producto.getPrecioCosto());
            stmt.setFloat(5, producto.getPrecioUnitario());
            stmt.setString(6, producto.getDescripcion());
            stmt.setString(7, producto.getNombreProducto());
            stmt.setInt(8, producto.getCategoriaInt());
            stmt.setInt(9, producto.getIdProductos());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                mensaje = "Producto modificado exitosamente.";
                logger.info("Producto actualizado correctamente. ID: " + producto.getIdProductos());
            } else {
                mensaje = "No se pudo modificar el Producto. Verifique si el producto existe.";
                logger.warn("No se actualizó ningún producto. ID posiblemente inexistente: " + producto.getIdProductos());
            }

        } catch (SQLException e) {
            logger.error("Error al modificar producto con ID: " + producto.getIdProductos(), e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método modificarProducto");
        return mensaje;
    }

    public static void eliminarProductoSeleccionado(Producto producto) {
        logger.info("Intento de eliminar un producto seleccionado con código:" +producto.getCodigoProducto());

        String mensaje = "";
        String sql = "DELETE FROM productos WHERE codigoProducto = ?";

        int confirmacion = JOptionPane.showConfirmDialog(
                null,
                "¿Estás seguro de que deseas eliminar el producto con código: " + producto.getCodigoProducto() + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            logger.info("Confirmación de eliminación aceptada para el producto con código: " + producto.getCodigoProducto());

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, producto.getCodigoProducto());
                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    mensaje = "Producto eliminado exitosamente.";
                    logger.info("Producto eliminado correctamente. Código: " + producto.getCodigoProducto());
                } else {
                    mensaje = "No se encontró ningún producto con el código especificado.";
                    logger.warn("Intento de eliminación fallido. Producto no encontrado. Código: " + producto.getCodigoProducto());
                }

            } catch (SQLException e) {
                logger.error("Error al intentar eliminar producto con código: " + producto.getCodigoProducto(), e);
                Database.handleSQLException(e);
            }

        } else {
            mensaje = "Eliminación cancelada por el usuario.";
            logger.info("Eliminación cancelada por el usuario para el producto con código: " + producto.getCodigoProducto());
        }

        logger.info("Finalizando método eliminarProductoSeleccionado");
    }

    public void descontarStock(int idProductos, int cantidadUtilizada) {
        logger.info("Intento de descontar del stock el producto con ID: " + idProductos + ", cantidad a descontar: " + cantidadUtilizada);

        int cantidad_stock = obtenerCantidad(idProductos);
        logger.debug("Stock actual para producto ID " + idProductos + ": " + cantidad_stock);

        if (cantidad_stock < cantidadUtilizada) {
            logger.warn("No hay suficiente stock para descontar. Stock disponible: " + cantidad_stock + ", solicitado: " + cantidadUtilizada);
            return;
        }

        cantidad_stock = cantidad_stock - cantidadUtilizada;

        String sql = "UPDATE productos SET cantidad = ? WHERE idproductos = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cantidad_stock);
            stmt.setInt(2, idProductos);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                logger.info("Stock actualizado correctamente. Nuevo stock: " + cantidad_stock + " para producto ID: " + idProductos);
            } else {
                logger.warn("No se actualizó ninguna fila al intentar descontar stock para producto ID: " + idProductos);
            }

        } catch (SQLException e) {
            logger.error("Error al descontar stock del producto ID: " + idProductos, e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método descontarStock para producto ID: " + idProductos);
    }

    public int obtenerCantidad(int idProductos) {
        logger.info("Iniciando método obtenerCantidad para producto ID: " + idProductos);

        int cantidad = 0;
        String sql = "SELECT cantidad FROM productos WHERE idproductos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProductos);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                cantidad = rs.getInt("cantidad");
                logger.debug("Cantidad obtenida para producto ID " + idProductos + ": " + cantidad);
            } else {
                logger.warn("No se encontró ningún producto con ID: " + idProductos);
            }

        } catch (SQLException e) {
            logger.error("Error al obtener la cantidad del producto ID: " + idProductos, e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método obtenerCantidad para producto ID: " + idProductos);
        return cantidad;
    }

    public int obtenerIDconCodigoProducto(String codigo) {
        logger.info("Iniciando método obtenerIDconCodigoProducto para código: " + codigo);

        String query = "SELECT idProductos FROM productos WHERE codigoProducto = ?";
        int idProducto = -1; // Valor por defecto si no se encuentra

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, codigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idProducto = rs.getInt("idProductos");
                    logger.debug("ID encontrado para código " + codigo + ": " + idProducto);
                } else {
                    logger.warn("No se encontró producto con código: " + codigo);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar ID del producto con código: " + codigo, e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método obtenerIDconCodigoProducto para código: " + codigo);
        return idProducto;
    }

    public List<String> selectNombresCategorias() {
        logger.info("Iniciando método selectNombresCategorias");

        List<String> nombresCategorias = new ArrayList<>();
        String sql = "SELECT nombre FROM categoriaproductos";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                nombresCategorias.add(nombre);
            }

            logger.info("Categorías obtenidas correctamente. Total: " + nombresCategorias.size());

        } catch (SQLException e) {
            logger.error("Error al obtener nombres de categorías", e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método selectNombresCategorias");
        return nombresCategorias;
    }

    public Producto obtenerProductoLinea(String codigo) {
        logger.info("Iniciando método obtenerProductoLinea para código: " + codigo);

        String query = "SELECT idproductos, nombreProducto, precioUnitario FROM productos WHERE codigoProducto = ?";
        Producto producto = null;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                producto = new Producto();
                producto.setIdProductos(rs.getInt("idproductos"));
                producto.setNombreProducto(rs.getString("nombreProducto"));
                producto.setPrecioUnitario(rs.getFloat("precioUnitario"));
                logger.debug("Producto encontrado: ID=" + producto.getIdProductos() + ", Nombre=" + producto.getNombreProducto() + ", Precio=" + producto.getPrecioUnitario());
            } else {
                logger.warn("No se encontró producto con código: " + codigo);
            }

        } catch (SQLException e) {
            logger.error("Error al obtener producto con código: " + codigo, e);
        }

        logger.info("Finalizando método obtenerProductoLinea para código: " + codigo);
        return producto;
    }


    public List<Producto> obtenerProductosPorPresupuesto(int idPresupuesto) {
        logger.info("Iniciando método obtenerProductosPorPresupuesto para presupuesto ID: " + idPresupuesto);

        List<Producto> productos = new ArrayList<>();
        String query = "SELECT p.idproductos, p.nombreProducto, p.precioUnitario, pp.cantidadUtilizada " +
                "FROM productopresupuesto pp " +
                "JOIN productos p ON pp.IDproductos = p.idproductos " +
                "WHERE pp.IDpresupuestos = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, idPresupuesto);
            ResultSet rs = stmt.executeQuery();

            logger.debug("Buscando productos para el presupuesto ID: " + idPresupuesto);

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setIdProductos(rs.getInt("idproductos"));
                producto.setNombreProducto(rs.getString("nombreProducto"));
                producto.setPrecioUnitario(rs.getFloat("precioUnitario"));
                producto.setCantidad(rs.getInt("cantidadUtilizada"));

                productos.add(producto);
                logger.debug("Producto agregado: ID=" + producto.getIdProductos() + " - " + producto.getNombreProducto());
            }

            logger.info("Productos obtenidos: " + productos.size() + " para presupuesto ID: " + idPresupuesto);

        } catch (SQLException e) {
            logger.error("Error al obtener productos del presupuesto ID: " + idPresupuesto, e);
            Database.handleSQLException(e);
        }

        logger.info("Finalizando método obtenerProductosPorPresupuesto");
        return productos;
    }

    public boolean productoExiste(int idProducto) {
        logger.debug("Verificando existencia del producto con ID: "+ idProducto);
        String query = "SELECT COUNT(*) FROM productos WHERE idproductos = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, idProducto);
            logger.debug("Parámetro establecido en la consulta - ID Producto: "+ idProducto);

            ResultSet rs = statement.executeQuery();
            logger.debug("Consulta ejecutada, procesando resultados...");

            if (rs.next()) {
                int count = rs.getInt(1);
                logger.debug("Resultado obtenido - COUNT: "+ count);
                boolean existe = count > 0;
                logger.debug("Producto "+idProducto+" existe: "+ existe);
                return existe;
            } else {
                logger.debug("No se obtuvieron resultados de la consulta");
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error al verificar existencia del producto ID: "+ idProducto, e);
            return false;
        }
    }

    public List<Producto> buscarPorCodigoProducto(String texto) {
        logger.debug("Iniciando búsqueda de productos por código: " + texto);

        String sql = "SELECT p.*, c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.codigoProducto LIKE ?";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String parametroBusqueda = "%" + texto + "%";
            stmt.setString(1, parametroBusqueda);
            logger.debug("Parámetro de búsqueda establecido: " + parametroBusqueda);

            try (ResultSet rs = stmt.executeQuery()) {
                logger.debug("Ejecutando consulta...");
                int contador = 0;

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
                    contador++;

                    logger.debug("Producto encontrado - ID: " + producto.getIdProductos() +
                            ", Código: " + producto.getCodigoProducto() +
                            ", Nombre: " + producto.getNombreProducto());
                }

                logger.debug("Búsqueda completada. Total productos encontrados: " + contador);

            } catch (SQLException e) {
                logger.error("Error al procesar resultados de la consulta: " + e.getMessage());
                Database.handleSQLException(e);
            }
        } catch (SQLException e) {
            logger.error("Error en la conexión o preparación de la consulta: " + e.getMessage());
            Database.handleSQLException(e);
        }

        logger.debug("Retornando lista con " + productos.size() + " productos");
        return productos;
    }

    public List<Producto> buscarPorNombreProducto(String texto) {
        logger.debug("Iniciando búsqueda de productos por nombre. Texto buscado: " + texto);

        String sql = "SELECT p.*, c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.nombreProducto LIKE ?";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String parametroBusqueda = "%" + texto + "%";
            stmt.setString(1, parametroBusqueda);
            logger.debug("Parámetro de búsqueda establecido: " + parametroBusqueda);

            try (ResultSet rs = stmt.executeQuery()) {
                logger.debug("Ejecutando consulta de búsqueda por nombre...");
                int contadorProductos = 0;

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
                    contadorProductos++;

                    logger.debug("Producto encontrado - ID: " + producto.getIdProductos() +
                            ", Nombre: " + producto.getNombreProducto() +
                            ", Categoría: " + producto.getCategoriaString());
                }

                logger.debug("Búsqueda finalizada. Total productos encontrados: " + contadorProductos);

            } catch (SQLException e) {
                logger.error("Error al procesar resultados de búsqueda por nombre: " + e.getMessage());
                Database.handleSQLException(e);
            }
        } catch (SQLException e) {
            logger.error("Error de conexión o preparación de consulta: " + e.getMessage());
            Database.handleSQLException(e);
        }

        logger.debug("Retornando lista con " + productos.size() + " productos encontrados");
        return productos;
    }

    public List<Producto> filtrarPorCategoria(int idCategoria) {
        logger.debug("Iniciando filtrado de productos por categoría. ID Categoría: " + idCategoria);

        List<Producto> productos = new ArrayList<>();
        String query = "SELECT p.idproductos, p.codigoProducto, p.marca, p.idcategoria, p.cantidad, " +
                "p.precioCosto, p.precioUnitario, p.descripcion, p.nombreProducto, " +
                "c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.idcategoria = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idCategoria);
            logger.debug("Parámetro establecido - ID Categoría: " + idCategoria);

            try (ResultSet rs = stmt.executeQuery()) {
                logger.debug("Ejecutando consulta de filtrado por categoría...");
                int contador = 0;

                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdProductos(rs.getInt("idproductos"));
                    producto.setCodigoProducto(rs.getString("codigoProducto"));
                    producto.setMarca(rs.getString("marca"));
                    producto.setCategoriaInt(rs.getInt("idcategoria"));
                    producto.setCantidad(rs.getInt("cantidad"));
                    producto.setPrecioCosto(rs.getInt("precioCosto"));
                    producto.setPrecioUnitario(rs.getInt("precioUnitario"));
                    producto.setDescripcion(rs.getString("descripcion"));
                    producto.setNombreProducto(rs.getString("nombreProducto"));
                    producto.setCategoriaString(rs.getString("nombre"));

                    productos.add(producto);
                    contador++;

                    logger.debug("Producto encontrado - ID: " + producto.getIdProductos() +
                            ", Nombre: " + producto.getNombreProducto() +
                            ", Categoría: " + producto.getCategoriaString());
                }

                logger.debug("Filtrado completado. Productos encontrados: " + contador);

            } catch (SQLException e) {
                logger.error("Error al procesar resultados del filtrado: " + e.getMessage());
                Database.handleSQLException(e);
            }
        } catch (SQLException e) {
            logger.error("Error en conexión o preparación de consulta: " + e.getMessage());
            Database.handleSQLException(e);
        }

        logger.debug("Retornando lista con " + productos.size() + " productos para categoría " + idCategoria);
        return productos;
    }


    public List<String> obtenerCategorias(List<String> estados) {
        logger.debug("Iniciando obtención de categorías de productos");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM categoriaproductos")) {

            logger.debug("Preparando consulta para obtener categorías");
            ResultSet rs = stmt.executeQuery();
            int contador = 0;

            while (rs.next()) {
                String nombreCategoria = rs.getString("nombre");
                estados.add(nombreCategoria);
                contador++;
                logger.debug("Categoría encontrada: " + nombreCategoria);
            }

            logger.debug("Obtención completada. Total categorías: " + contador);

        } catch (SQLException e) {
            logger.error("Error al obtener categorías: " + e.getMessage());
            Database.handleSQLException(e);
        }

        logger.debug("Retornando " + estados.size() + " categorías");
        return estados;
    }
    public int obtenerIDcategorias(String categoria) {
        logger.debug("Buscando ID para categoría: " + categoria);

        int idCategoria = -1;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT idcategoriaProductos FROM categoriaproductos WHERE nombre = ?")) {

            stmt.setString(1, categoria);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                idCategoria = rs.getInt("idcategoriaProductos");
                logger.debug("ID encontrado: " + idCategoria + " para categoría: " + categoria);
            } else {
                logger.debug("No se encontró ID para categoría: " + categoria);
            }

        } catch (SQLException e) {
            logger.error("Error al buscar ID para categoría " + categoria + ": " + e.getMessage());
            Database.handleSQLException(e);
        }

        logger.debug("Retornando ID: " + idCategoria + " para categoría: " + categoria);
        return idCategoria;
    }

    public List<Producto> obtenerRankingDeVentas(int limite, int periodo, int criterio, LocalDate desde1, LocalDate hasta1) {
        logger.debug("Iniciando obtención de ranking de ventas - Límite: " + limite + ", Periodo: " + periodo + ", Criterio: " + criterio);
        List<Producto> productos = new ArrayList<>();
        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();
        logger.debug("Fecha de referencia: " + fechaFin);
        // Determinar el rango de fechas
        if (desde1 != null && hasta1 != null) {
            fechaInicio = desde1;
            fechaFin = hasta1;
            logger.debug("Rango personalizado: Desde " + fechaInicio + " hasta " + fechaFin);
        } else {
            switch (periodo) {
                case 1:
                    fechaInicio = fechaFin.minusDays(1);
                    logger.debug("Periodo: Últimas 24 horas");
                    break;
                case 7:
                    fechaInicio = fechaFin.minusDays(7);
                    logger.debug("Periodo: Última semana");
                    break;
                case 365:
                    fechaInicio = fechaFin.minusDays(365);
                    logger.debug("Periodo: Último año");
                    break;
                default:
                    logger.error("Período no válido: " + periodo);
                    throw new IllegalArgumentException("Período no válido.");
            }
        }

        // Definir el campo de orden según el criterio (1: por dinero, 2: por cantidad de unidades)
        String campoOrden = (criterio == 1) ? "SUM(ptv.cantidad * p.precioUnitario)" : "SUM(ptv.cantidad)";
        String aliasCampo = (criterio == 1) ? "total_dinero" : "total_unidades";
        logger.debug("Criterio de ordenación: " + (criterio == 1 ? "Por monto total" : "Por cantidad de unidades"));
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
            logger.debug("Estableciendo parámetros de consulta - Fecha inicio: " + fechaInicio +
                    ", Fecha fin: " + fechaFin + ", Límite: " + limite);
            // Establecer los parámetros de la consulta
            ps.setDate(1, java.sql.Date.valueOf(fechaInicio));
            ps.setDate(2, java.sql.Date.valueOf(fechaFin));
            ps.setInt(3, limite);

            // Ejecutar la consulta y procesar los resultados
            try (ResultSet rs = ps.executeQuery()) {
                logger.debug("Ejecutando consulta de ranking...");
                int contador = 0;
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdproductos(rs.getInt("idproductos"));
                    producto.setNombreProducto(rs.getString("nombreProducto"));
                    producto.setVentas((float) (criterio == 1 ? rs.getDouble("total_dinero") : rs.getInt("total_unidades")));
                    productos.add(producto);
                    contador++;
                    logger.debug("Producto #" + contador + " - ID: " + producto.getIdproductos() +
                            ", Nombre: " + producto.getNombreProducto() +
                            ", Valor: " + producto.getVentas());
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener ranking de ventas: " + e.getMessage());
            e.printStackTrace();
        }
        logger.debug("Retornando ranking con " + productos.size() + " productos");
        return productos;
    }

    public List<Categoria> obtenerRankingDeCategorias(int limite, int periodo, int criterio, LocalDate desde2, LocalDate hasta2) {
        logger.debug("Iniciando obtención de ranking de categorías - Límite: " + limite + ", Periodo: " + periodo + ", Criterio: " + criterio);
        List<Categoria> categorias = new ArrayList<>();
        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();

        if (desde2 != null && hasta2 != null) {
            fechaInicio = desde2;
            fechaFin = hasta2;
            logger.debug("Rango personalizado: Desde " + fechaInicio + " hasta " + fechaFin);
        } else {
            switch (periodo) {
                case 1:
                    fechaInicio = fechaFin.minusDays(1);
                    logger.debug("Periodo: Últimas 24 horas");
                    break;
                case 7:
                    fechaInicio = fechaFin.minusDays(7);
                    logger.debug("Periodo: Última semana");
                    break;
                case 365:
                    fechaInicio = fechaFin.minusDays(365);
                    logger.debug("Periodo: Último año");
                    break;
                default:
                    logger.error("Período no válido: " + periodo);
                    throw new IllegalArgumentException("Período no válido.");
            }
        }

        String campoOrden = (criterio == 1) ? "SUM(total_dinero)" : "SUM(total_unidades)";
        String aliasCampo = (criterio == 1) ? "total_dinero" : "total_unidades";
        logger.debug("Criterio de ordenación: " + (criterio == 1 ? "Por monto total" : "Por cantidad de unidades"));
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
            logger.debug("Estableciendo parámetros de consulta - Fechas: " + fechaInicio + " a " + fechaFin +
                    ", Límite: " + limite);
            // Establecer parámetros
            ps.setDate(1, java.sql.Date.valueOf(fechaInicio)); // para ventas
            ps.setDate(2, java.sql.Date.valueOf(fechaFin));
            ps.setDate(3, java.sql.Date.valueOf(fechaInicio)); // para presupuestos
            ps.setDate(4, java.sql.Date.valueOf(fechaFin));
            ps.setInt(5, limite);

            try (ResultSet rs = ps.executeQuery()) {
                logger.debug("Ejecutando consulta de ranking de categorías...");
                int contador = 0;
                while (rs.next()) {
                    Categoria categoria = new Categoria();
                    categoria.setIdCategoria(rs.getInt("idcategoria"));
                    categoria.setNombreCategoria(rs.getString("nombre"));
                    categoria.setTotal((float) (criterio == 1 ? rs.getDouble("total_dinero") : rs.getInt("total_unidades")));
                    categorias.add(categoria);
                    contador++;
                    logger.debug("Categoría #" + contador + " - ID: " + categoria.getIdcategoria() +
                            ", Nombre: " + categoria.getNombreCategoria() +
                            ", Valor: " + categoria.getTotal());
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener ranking de categorías: " + e.getMessage());
            Database.handleSQLException(e);
        }
        logger.debug("Retornando ranking con " + categorias.size() + " categorías");
        return categorias;
    }

    public List<Producto> buscarPorMarcaProducto(String texto) {
        logger.debug("Iniciando búsqueda de productos por marca. Texto buscado: " + texto);

        String sql = "SELECT p.*, c.nombre FROM productos p " +
                "INNER JOIN categoriaproductos c ON p.idcategoria = c.idcategoriaProductos " +
                "WHERE p.marca LIKE ?";
        List<Producto> productos = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String parametroBusqueda = "%" + texto + "%";
            stmt.setString(1, parametroBusqueda);
            logger.debug("Parámetro de búsqueda establecido: " + parametroBusqueda);

            try (ResultSet rs = stmt.executeQuery()) {
                logger.debug("Ejecutando consulta de búsqueda por marca...");
                int contadorProductos = 0;

                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdProductos(rs.getInt("idproductos"));
                    producto.setCodigoProducto(rs.getString("codigoProducto"));
                    producto.setMarca(rs.getString("marca"));
                    producto.setCantidad(rs.getInt("cantidad"));
                    producto.setPrecioCosto(rs.getFloat("precioCosto"));
                    producto.setPrecioUnitario(rs.getFloat("precioUnitario"));
                    producto.setDescripcion(rs.getString("descripcion"));
                    producto.setNombreProducto(rs.getString("nombreProducto"));
                    producto.setCategoriaString(rs.getString("nombre")); // nombre de la categoría
                    producto.setCategoriaInt(rs.getInt("idcategoria"));

                    productos.add(producto);
                    contadorProductos++;

                    logger.debug("Producto encontrado - ID: " + producto.getIdProductos() +
                            ", Marca: " + producto.getMarca() +
                            ", Nombre: " + producto.getNombreProducto());
                }

                logger.debug("Búsqueda finalizada. Total productos encontrados: " + contadorProductos);

            } catch (SQLException e) {
                logger.error("Error al procesar resultados de búsqueda por marca: " + e.getMessage(), e);
                Database.handleSQLException(e);
            }

        } catch (SQLException e) {
            logger.error("Error de conexión o preparación de consulta para búsqueda por marca: " + e.getMessage(), e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando lista con " + productos.size() + " productos encontrados por marca");
        return productos;
    }
}
