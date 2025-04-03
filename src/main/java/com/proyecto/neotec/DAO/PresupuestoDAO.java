package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Presupuestos;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDAO {
    public int modificarPresupuesto(int idpresupuesto, Integer tiempoEstimado, float costosVariables, float manoDeObra, float totalGeneral, int idequipo, float totalProductos, String observaciones) {
        // Consulta de actualización
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
                System.out.println("Presupuesto actualizado exitosamente.");
            } else {
                // Si no se afectaron filas, el presupuesto no fue encontrado
                System.out.println("No se encontró el presupuesto para actualizar.");
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
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
        if (existePresupuestoParaEquipo(idEquipo)) {
            System.out.println("⚠ No se puede crear el presupuesto: Ya existe uno registrado para este equipo.");
            return -1; // Indica que no se pudo insertar
        }

        String query = "INSERT INTO presupuestos (idEquipo, costosVariables, estado, precioTotal, diasEstimados, costoManoDeObra, totalProductos, observaciones, fechaHora) VALUES (?, ?, 2, ?, ?, ?, ?, ?, NOW())";
        int IDgenerado = -1;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
                    }
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return IDgenerado;
    }


    public void insertProductoPresupuesto (int idPresupuesto, int idProductos, int cantidadUtilizada) {
        String query = "INSERT INTO productopresupuesto (IDpresupuestos, IDproductos, cantidadUtilizada) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ){
            stmt.setInt(1,idPresupuesto);
            stmt.setInt(2,idProductos);
            stmt.setInt(3,cantidadUtilizada);
            stmt.executeUpdate();
        }catch (SQLException e){
            Database.handleSQLException(e);
        }
        System.out.println("llega al dao");
    }

    public List<Presupuestos> selectAllPresupuestos() {
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
            }

        }catch(SQLException e){
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
        String sql = "SELECT COUNT(*) FROM productopresupuesto WHERE IDpresupuestos = ? AND IDproductos = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idPresupuesto);
            stmt.setInt(2, idProducto);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true; // El producto ya existe en el presupuesto
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo del error
        }

        return false; // No se encontró el producto en el presupuesto
    }

}
