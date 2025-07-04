package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Presupuestos;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class PresupuestoDAO {
    private static final Logger logger = Logger.getLogger(PresupuestoDAO.class);
    public int modificarPresupuesto(int idpresupuesto, Integer tiempoEstimado, float costosVariables, float manoDeObra, float totalGeneral, int idequipo, float totalProductos, String observaciones) {
        // Consulta de actualización
        logger.debug("Intento de ejecutar script de actualización Presupuestos");
        String queryUpdate = "UPDATE presupuestos SET idEquipo = ?, costosVariables = ?, precioTotal = ?, diasEstimados = ?, costoManoDeObra = ?, totalProductos = ?, observaciones = ?, fechaHora = NOW() WHERE idpresupuestos = ?";

        int idGenerado = -1; // Variable para guardar el id del presupuesto

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmtUpdate = conn.prepareStatement(queryUpdate)) {

            // Establecer los valores en la consulta preparada
            pstmtUpdate.setInt(1, idequipo); // Establecer idEquipo
            pstmtUpdate.setFloat(2, costosVariables); // Establecer costosVariables
            pstmtUpdate.setFloat(3, totalGeneral); // Establecer precioTotal
            pstmtUpdate.setInt(4, tiempoEstimado); // Establecer diasEstimados
            pstmtUpdate.setFloat(5, manoDeObra); // Establecer costoManoDeObra
            pstmtUpdate.setFloat(6, totalProductos); // Establecer totalProductos
            pstmtUpdate.setString(7, observaciones); // Establecer observaciones
            pstmtUpdate.setInt(8, idpresupuesto); // Establecer idpresupuesto para la actualización

            // Ejecutar la consulta de actualización
            int affectedRows = pstmtUpdate.executeUpdate();

            if (affectedRows > 0) {
                // Si se actualizó correctamente, devolver el id del presupuesto
                idGenerado = idpresupuesto;
                logger.debug("Presupuesto actualizado correctamente con ID: "+ idGenerado);
            } else {
                // Si no se afectaron filas, el presupuesto no fue encontrado
                logger.debug("El Presupuesto no fue encontrado");
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
            logger.error("Ha ocurrido una excepción al intentar actualizar los presupuestos. Detalles:"+e);
        }

        return idGenerado; // Devolver el id del presupuesto que fue actualizado (el mismo id)
    }



    public boolean existePresupuestoParaEquipo(int idEquipo) {
        String query = "SELECT COUNT(*) FROM presupuestos WHERE idEquipo = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idEquipo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Si hay al menos un presupuesto, retorna true
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return false; // No hay presupuestos para este equipo
    }

    public int insertPresupuesto(int tiempoEstimado, float costoVariable, float costoManoObra, float totalGeneral, int idEquipo, float totalProductos, String obs) {
        logger.debug("Intento de inserción de Presupuesto");
        String query = "INSERT INTO presupuestos (idEquipo, costosVariables, precioTotal, diasEstimados, costoManoDeObra, totalProductos, observaciones, estado, fechaHora) VALUES (?, ?, ?, ?, ?, ?, ?, 1, NOW())";
        int IDgenerado = -1;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            // Establecer parámetros
            pstmt.setInt(1, idEquipo);
            pstmt.setFloat(2, costoVariable);
            pstmt.setFloat(3, totalGeneral);
            pstmt.setInt(4, tiempoEstimado);
            pstmt.setFloat(5, costoManoObra);
            pstmt.setFloat(6, totalProductos);
            pstmt.setString(7, obs);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        IDgenerado = rs.getInt(1);
                        logger.debug("Presupuesto insertado correctamente con ID: " + IDgenerado);
                    }
                }
            } else {
                logger.warn("No se insertó ningún registro en la tabla presupuestos");
            }

        } catch (SQLException e) {
            logger.error("Error al insertar presupuesto", e); // loguea stacktrace
            Database.handleSQLException(e); // adicional si querés log separado por clase
        }

        // Cambio de estado del equipo
        EquipoDAO cambiarestadoEquipo = new EquipoDAO();
        logger.debug("Cambiando estado del equipo con ID: " + idEquipo + " a estado: 3 (Espera autorización)");
        cambiarestadoEquipo.actualizarEstadoEquipo(idEquipo, 3);

        return IDgenerado;
    }



    public void insertProductoPresupuesto(int idPresupuesto, int idProductos, int cantidadUtilizada) {
        logger.debug("Intento de insertar producto en presupuesto. IDpresupuesto: " + idPresupuesto + ", IDproducto: " + idProductos + ", Cantidad: " + cantidadUtilizada);
        String query = "INSERT INTO productopresupuesto (IDpresupuestos, IDproductos, cantidadUtilizada) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idPresupuesto);
            stmt.setInt(2, idProductos);
            stmt.setInt(3, cantidadUtilizada);
            stmt.executeUpdate();

            logger.debug("Producto agregado al Presupuesto correctamente");

        } catch (SQLException e) {
            logger.error("Error al insertar producto en presupuesto", e);
            Database.handleSQLException(e);
        }
    }
    public List<Presupuestos> selectAllPresupuestos() {
        logger.debug("Inicio de la consulta para obtener todos los presupuestos");
        List<Presupuestos> listaPresupuestos = new ArrayList<>();
        String sql= "SELECT * FROM presupuestos";
        try(Connection conn= Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                EquipoDAO equipoDAO = new EquipoDAO();
                ClienteDAO clienteDAO = new ClienteDAO();
                Presupuestos presupuestos = new Presupuestos();
                presupuestos.setIdpresupuesto(rs.getInt("idpresupuestos"));
                presupuestos.setEquipo(equipoDAO.obtenerPropietario(rs.getInt("idEquipo")).getDispositivo());
                presupuestos.setEstado(rs.getInt("estado"));
                presupuestos.setCostosVariables(rs.getInt("costosVariables"));
                presupuestos.setPrecioTotal(rs.getInt("precioTotal"));
                presupuestos.setManoDeObra(rs.getInt("costoManoDeObra"));
                presupuestos.setDiasEstimados(rs.getInt("diasEstimados"));
                presupuestos.setPropietario(clienteDAO.obtenerNombre(equipoDAO.obtenerPropietario(rs.getInt("idEquipo")).getIdcliente()));
                Timestamp fecha = (rs.getTimestamp("fechaHora"));
                String fechaHora;
                if (fecha != null) {
                    fechaHora = String.valueOf(fecha.toLocalDateTime());
                } else {
                    fechaHora = "-";
                }
                presupuestos.setFechaHora(fechaHora);
                presupuestos.setTotalProductos(rs.getFloat("totalProductos"));
                presupuestos.setObservaciones(rs.getString("observaciones"));
                listaPresupuestos.add(presupuestos);
                logger.debug("Presupuesto cargado con ID: " + presupuestos.getIdpresupuesto());
            }
            logger.debug("Total de presupuestos obtenidos: " + listaPresupuestos.size());

        }catch(SQLException e){
            logger.error("Error al consultar todos los presupuestos", e);
            Database.handleSQLException(e);
        }
        return listaPresupuestos;
    }

    public LocalDateTime obtenerFechaHora(int idPresupuesto) {
        String query = "SELECT fechaHora FROM presupuestos WHERE idpresupuestos = ?";
        LocalDateTime fechaHora = null;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idPresupuesto);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Timestamp timestamp = resultSet.getTimestamp("fechaHora");
                if (timestamp != null) {
                    fechaHora = timestamp.toLocalDateTime();
                }
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return fechaHora; // Retorna null si no se encuentra el presupuesto
    }
    public boolean verificarProductoPresupuesto(int idPresupuesto, int idProducto) {
        logger.debug("Verificando si el producto con ID: " + idProducto + " ya está asociado al presupuesto ID: " + idPresupuesto);
        String sql = "SELECT COUNT(*) FROM productopresupuesto WHERE IDpresupuestos = ? AND IDproductos = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idPresupuesto);
            stmt.setInt(2, idProducto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    logger.debug("El producto ya está asociado al presupuesto");
                    return true; // El producto ya existe en el presupuesto
                } else {
                    logger.debug("El producto no está asociado al presupuesto");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al verificar producto en presupuesto", e);
            Database.handleSQLException(e);
        }

        return false; // No se encontró el producto en el presupuesto
    }

    public void cambiarEstadoPresupuesto(Presupuestos presupuesto, int estadoSeleccionado) {
        logger.debug("Intentando cambiar el estado del presupuesto con ID: " + presupuesto.getIdpresupuesto() + " al estado: " + estadoSeleccionado);

        String query = "UPDATE presupuestos SET estado = ? WHERE idpresupuestos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, estadoSeleccionado);
            stmt.setInt(2, presupuesto.getIdpresupuesto());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                logger.debug("Estado del presupuesto actualizado correctamente");
            } else {
                logger.warn("No se encontró el presupuesto con ID: " + presupuesto.getIdpresupuesto());
            }

        } catch (SQLException e) {
            logger.error("Error al cambiar el estado del presupuesto", e);
            Database.handleSQLException(e);
        }
    }

    public int obtenerEstadoIntDesdeBD(String descripcion) {
        logger.debug("Buscando ID del estado con descripción: " + descripcion);

        String query = "SELECT idestadospresupuestos FROM estadospresupuestos WHERE descripcion = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, descripcion);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idEstado = rs.getInt("idestadospresupuestos");
                    logger.debug("ID del estado encontrado: " + idEstado);
                    return idEstado;
                } else {
                    logger.warn("No se encontró ningún estado con la descripción: " + descripcion);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener el ID del estado desde la base de datos", e);
            Database.handleSQLException(e);
        }

        return -1; // Estado no encontrado
    }


    public String obtenerDescripcionEstadoDesdeBD(int idEstado) {
        logger.debug("Buscando descripción del estado con ID: " + idEstado);

        String query = "SELECT descripcion FROM estadospresupuestos WHERE idestadospresupuestos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idEstado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String descripcion = rs.getString("descripcion");
                    logger.debug("Descripción del estado encontrada: " + descripcion);
                    return descripcion;
                } else {
                    logger.warn("No se encontró ninguna descripción para el estado con ID: " + idEstado);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener la descripción del estado desde la base de datos", e);
            Database.handleSQLException(e);
        }
        return "Desconocido"; // ID no válido
    }

    public boolean verificarEstadoPresupuesto(Presupuestos pr, int estadoEsperado) {
        logger.debug("Verificando si el presupuesto con ID: " + pr.getIdpresupuesto() + " tiene estado esperado: " + estadoEsperado);
        String sql = "SELECT estado FROM presupuestos WHERE idpresupuestos = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pr.getIdpresupuesto());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int estadoActual = rs.getInt("estado");
                    logger.debug("Estado actual del presupuesto: " + estadoActual);
                    return estadoActual == estadoEsperado;
                } else {
                    logger.warn("No se encontró el presupuesto con ID: " + pr.getIdpresupuesto());
                }
            }
        } catch (SQLException e) {
            logger.error("Error al verificar estado del presupuesto", e);
            Database.handleSQLException(e);
        }

        return false; // Si no se encontró el presupuesto o hubo error
    }
    public boolean verificarEstadoPagado(int idPresupuesto) {
        logger.debug("Verificando si el presupuesto con ID: " + idPresupuesto + " tiene estado 'Pagado' (estado = 4)");
        String sql = "SELECT 1 FROM presupuestos WHERE idpresupuestos = ? AND estado = 4 LIMIT 1";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPresupuesto);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                logger.debug("El presupuesto con ID: " + idPresupuesto + " tiene estado 'Pagado'");
                return true;
            } else {
                logger.debug("El presupuesto con ID: " + idPresupuesto + " no tiene estado 'Pagado'");
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error al verificar si el presupuesto está pagado", e);
            Database.handleSQLException(e);
            return false;
        }
    }

    public List<Presupuestos> buscarPorFechaCreacion(String fechaFormateada) {
        List<Presupuestos> presupuestos = new ArrayList<>();
        String query = "SELECT * FROM presupuestos WHERE fechaHora >= ? AND fechaHora < ?";
        Presupuestos presupuesto;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            logger.info("Preparando consulta de presupuestos para fecha: " + fechaFormateada);
            // Buscar presupuestos creados estableciendo un rango de fecha y hora
            stmt.setString(1, fechaFormateada + " 00:00:00");
            stmt.setString(2, fechaFormateada + " 23:59:59");
            logger.debug("Parámetros de consulta: fechaInicio=" + fechaFormateada + " 00:00:00, fechaFin=" + fechaFormateada + " 23:59:59");

            try (ResultSet rs = stmt.executeQuery()) {
                logger.info("Ejecutando consulta de presupuestos");
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    presupuesto = new Presupuestos();
                    ClienteDAO clienteDAO = new ClienteDAO();
                    EquipoDAO equipoDAO = new EquipoDAO();
                    presupuesto.setPropietario(clienteDAO.obtenerNombre(equipoDAO.obtenerPropietario(rs.getInt("idEquipo")).getIdcliente()));
                    presupuesto.setIdpresupuesto(rs.getInt("idpresupuestos"));
                    presupuesto.setIdEquipo(rs.getInt("idEquipo"));
                    presupuesto.setCostosVariables(rs.getInt("costosVariables"));
                    presupuesto.setEstado(rs.getInt("estado"));
                    presupuesto.setPrecioTotal(rs.getInt("precioTotal"));
                    presupuesto.setDiasEstimados(rs.getInt("diasEstimados"));
                    presupuesto.setManoDeObra(rs.getInt("costoManoDeObra"));
                    presupuesto.setObservaciones(rs.getString("observaciones"));
                    presupuesto.setTotalProductos(rs.getFloat("totalProductos"));
                    presupuesto.setFechaHora(rs.getString("fechaHora"));
                    presupuestos.add(presupuesto);
                    logger.debug("Presupuesto #" + contador + " cargado: ID=" + rs.getInt("idpresupuestos") +
                            ", Equipo=" + rs.getInt("idEquipo") +
                            ", Estado=" + rs.getInt("estado"));
                }
                logger.info("Total de presupuestos encontrados: " + contador);
            }
        } catch (SQLException e) {
            logger.error("Error al buscar presupuestos por fecha: " + e.getMessage());
            Database.handleSQLException(e);
        }

        return presupuestos;
    }
    public List<Presupuestos> buscarPresupuestosPorNombreDeEquipo(String nombreParcial) {
        List<Presupuestos> lista = new ArrayList<>();
        String query = "SELECT p.* FROM presupuestos p " +
                "JOIN equipos e ON p.idEquipo = e.idequipos " +
                "WHERE e.dispositivo LIKE ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            logger.info("Iniciando búsqueda de presupuestos para equipo: " + nombreParcial);
            stmt.setString(1, "%" + nombreParcial + "%");
            logger.debug("Parámetro de búsqueda SQL: %" + nombreParcial + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                logger.info("Ejecutando consulta de presupuestos por nombre de equipo");
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    Presupuestos presupuesto = new Presupuestos();

                    EquipoDAO equipoDAO = new EquipoDAO();
                    ClienteDAO clienteDAO = new ClienteDAO();

                    presupuesto.setIdpresupuesto(rs.getInt("idpresupuestos"));
                    presupuesto.setIdEquipo(rs.getInt("idEquipo"));
                    presupuesto.setEquipo(equipoDAO.obtenerEquipoPorId(presupuesto.getIdEquipo()).getDispositivo());
                    presupuesto.setCostosVariables(rs.getInt("costosVariables"));
                    presupuesto.setEstado(rs.getInt("estado"));
                    presupuesto.setPrecioTotal(rs.getInt("precioTotal"));
                    presupuesto.setDiasEstimados(rs.getInt("diasEstimados"));
                    presupuesto.setManoDeObra(rs.getInt("costoManoDeObra"));
                    presupuesto.setObservaciones(rs.getString("observaciones"));
                    presupuesto.setTotalProductos(rs.getFloat("totalProductos"));
                    presupuesto.setFechaHora(rs.getString("fechaHora"));
                    presupuesto.setPropietario(
                            clienteDAO.obtenerNombre(
                                    equipoDAO.obtenerPropietario(rs.getInt("idEquipo")).getIdcliente()
                            )
                    );
                    lista.add(presupuesto);
                    logger.debug("Presupuesto encontrado #" + contador +
                            " | ID: " + rs.getInt("idpresupuestos") +
                            " | Equipo: " + presupuesto.getEquipo() +
                            " | Estado: " + rs.getInt("estado"));
                }
                logger.info("Total de presupuestos encontrados: " + contador);
            }
        } catch (SQLException e) {
            logger.error("Error en buscarPresupuestosPorNombreDeEquipo: " + e.getMessage());
            Database.handleSQLException(e);
        }

        return lista;
    }
    public List<Presupuestos> buscarPresupuestosPorNombreCliente(String nombreParcial) {
        List<Presupuestos> lista = new ArrayList<>();
        String query = "SELECT p.*, e.dispositivo, c.nombre, c.apellido " +
                "FROM presupuestos p " +
                "JOIN equipos e ON p.idEquipo = e.idequipos " +
                "JOIN clientes c ON e.idclientes = c.idclientes " +
                "WHERE c.nombre LIKE ? OR c.apellido LIKE ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Iniciando búsqueda de presupuestos para cliente: '" + nombreParcial + "'");
            String likePattern = "%" + nombreParcial + "%";
            stmt.setString(1, likePattern);
            stmt.setString(2, likePattern);
            logger.debug("Parámetros de búsqueda SQL: [" + likePattern + ", " + likePattern + "]");

            try (ResultSet rs = stmt.executeQuery()) {
                logger.info("Ejecutando consulta de presupuestos por nombre de cliente");
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    Presupuestos presupuesto = new Presupuestos();

                    presupuesto.setIdpresupuesto(rs.getInt("idpresupuestos"));
                    presupuesto.setIdEquipo(rs.getInt("idEquipo"));
                    presupuesto.setEquipo(rs.getString("dispositivo"));
                    presupuesto.setCostosVariables(rs.getInt("costosVariables"));
                    presupuesto.setEstado(rs.getInt("estado"));
                    presupuesto.setPrecioTotal(rs.getInt("precioTotal"));
                    presupuesto.setDiasEstimados(rs.getInt("diasEstimados"));
                    presupuesto.setManoDeObra(rs.getInt("costoManoDeObra"));
                    presupuesto.setObservaciones(rs.getString("observaciones"));
                    presupuesto.setTotalProductos(rs.getFloat("totalProductos"));
                    presupuesto.setFechaHora(rs.getString("fechaHora"));

                    // Concatenamos nombre + apellido del cliente
                    String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                    presupuesto.setPropietario(nombreCompleto);

                    lista.add(presupuesto);
                    logger.debug("Presupuesto #" + contador +
                            " encontrado | ID: " + presupuesto.getIdpresupuesto() +
                            " | Cliente: " + nombreCompleto +
                            " | Equipo: " + presupuesto.getEquipo());
                }
                logger.info("Total de presupuestos encontrados: " + contador);
            }
        } catch (SQLException e) {
            logger.error("Error al buscar presupuestos por nombre de cliente: " + e.getMessage());
            Database.handleSQLException(e);
        }

        return lista;
    }
    public List<Presupuestos> obtenerPresupuestosPorIdEquipo(int idEquipo) {
        List<Presupuestos> listaPresupuestos = new ArrayList<>();
        String query = "SELECT * FROM presupuestos WHERE idEquipo = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            logger.info("Buscando presupuestos para equipo ID: " + idEquipo);
            stmt.setInt(1, idEquipo);
            logger.debug("Parámetro de consulta - idEquipo: " + idEquipo);

            ResultSet rs = stmt.executeQuery();
            logger.info("Consulta ejecutada para obtener presupuestos del equipo");
            int contador = 0;

            while (rs.next()) {
                contador++;
                Presupuestos pr = new Presupuestos();
                pr.setIdpresupuesto(rs.getInt("idpresupuestos"));
                pr.setIdEquipo(rs.getInt("idEquipo"));
                pr.setCostosVariables(rs.getInt("costosVariables"));
                pr.setEstado(rs.getInt("estado"));
                pr.setPrecioTotal(rs.getInt("precioTotal"));
                pr.setDiasEstimados(rs.getInt("diasEstimados"));
                pr.setManoDeObra(rs.getInt("costoManoDeObra"));
                pr.setObservaciones(rs.getString("observaciones"));
                pr.setTotalProductos(rs.getFloat("totalProductos"));
                pr.setFechaHora(rs.getString("fechaHora"));
                listaPresupuestos.add(pr);

                logger.debug("Presupuesto encontrado #" + contador +
                        " | ID: " + pr.getIdpresupuesto() +
                        " | Estado: " + pr.getEstado() +
                        " | Precio Total: " + pr.getPrecioTotal());
            }

            logger.info("Total de presupuestos encontrados para el equipo: " + contador);

        } catch (SQLException e) {
            logger.error("Error al obtener presupuestos por ID de equipo: " + e.getMessage());
            Database.handleSQLException(e);
        }

        return listaPresupuestos;
    }
    public Presupuestos obtenerPresupuestoPorId(int idPresupuesto) {
        String query = "SELECT * FROM presupuestos WHERE idpresupuestos = ?";
        Presupuestos pr = null;

        try (Connection con = Database.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            logger.info("Buscando presupuesto con ID: " + idPresupuesto);
            stmt.setInt(1, idPresupuesto);
            logger.debug("Parámetro de consulta - idPresupuesto: " + idPresupuesto);

            ResultSet rs = stmt.executeQuery();
            logger.info("Consulta ejecutada para obtener presupuesto por ID");

            if (rs.next()) {
                pr = new Presupuestos();
                pr.setIdpresupuesto(rs.getInt("idpresupuestos"));
                pr.setIdEquipo(rs.getInt("idEquipo"));
                pr.setCostosVariables(rs.getInt("costosVariables"));
                pr.setEstado(rs.getInt("estado"));
                pr.setPrecioTotal(rs.getInt("precioTotal"));
                pr.setDiasEstimados(rs.getInt("diasEstimados"));
                pr.setManoDeObra(rs.getInt("costoManoDeObra"));
                pr.setObservaciones(rs.getString("observaciones"));
                pr.setTotalProductos(rs.getFloat("totalProductos"));
                pr.setFechaHora(rs.getString("fechaHora"));

                logger.info("Presupuesto encontrado - ID: " + pr.getIdpresupuesto() +
                        " | Equipo ID: " + pr.getIdEquipo() +
                        " | Estado: " + pr.getEstado() +
                        " | Precio Total: $" + pr.getPrecioTotal());
            } else {
                logger.warn("No se encontró presupuesto con ID: " + idPresupuesto);
            }

        } catch (SQLException e) {
            logger.error("Error al obtener presupuesto por ID: " + e.getMessage());
            Database.handleSQLException(e);
        }

        return pr;
    }
    public List<Presupuestos> filtrarPorEstadoPresupuesto(int estado) {
        List<Presupuestos> listaPresupuestos = new ArrayList<>();
        String query = "SELECT " +
                "p.idpresupuestos, p.estado, p.costosVariables, p.precioTotal, " +
                "p.costoManoDeObra, p.diasEstimados, p.observaciones, " +
                "p.totalProductos, p.fechaHora, " +
                "e.dispositivo, c.nombre, c.apellido " +
                "FROM presupuestos p " +
                "INNER JOIN equipos e ON p.idEquipo = e.idequipos " +
                "INNER JOIN clientes c ON e.idclientes = c.idclientes " +
                "WHERE p.estado = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.info("Filtrando presupuestos por estado: " + estado);
            stmt.setInt(1, estado);

            ResultSet rs = stmt.executeQuery();
            logger.info("Ejecutando consulta de presupuestos por estado");
            int contador = 0;

            while (rs.next()) {
                contador++;
                Presupuestos presupuestos = new Presupuestos();
                presupuestos.setIdpresupuesto(rs.getInt("idpresupuestos"));
                presupuestos.setEstado(rs.getInt("estado"));
                presupuestos.setCostosVariables(rs.getInt("costosVariables"));
                presupuestos.setPrecioTotal(rs.getInt("precioTotal"));
                presupuestos.setManoDeObra(rs.getInt("costoManoDeObra"));
                presupuestos.setDiasEstimados(rs.getInt("diasEstimados"));
                presupuestos.setObservaciones(rs.getString("observaciones"));
                presupuestos.setTotalProductos(rs.getFloat("totalProductos"));

                Timestamp fecha = rs.getTimestamp("fechaHora");
                presupuestos.setFechaHora(fecha != null ? fecha.toLocalDateTime().toString() : "-");

                presupuestos.setEquipo(rs.getString("dispositivo"));
                String propietario = rs.getString("nombre") + " " + rs.getString("apellido");
                presupuestos.setPropietario(propietario);

                listaPresupuestos.add(presupuestos);

                logger.debug("Presupuesto #" + contador +
                        " | ID: " + presupuestos.getIdpresupuesto() +
                        " | Cliente: " + propietario +
                        " | Equipo: " + presupuestos.getEquipo() +
                        " | Estado: " + presupuestos.getEstado());
            }

            logger.info("Total de presupuestos encontrados con estado " + estado + ": " + contador);

        } catch (SQLException e) {
            logger.error("Error al filtrar presupuestos por estado " + estado + ": " + e.getMessage(), e);
            Database.handleSQLException(e);
        }

        return listaPresupuestos;
    }
}
