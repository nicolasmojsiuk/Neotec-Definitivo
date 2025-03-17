package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Presupuestos;
import com.proyecto.neotec.models.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDAO {
    public int insertPresupuesto(int tiempoEstimado, float costoVariable, float costoManoObra, float totalGeneral, int idEquipo, float totalProductos, String obs) {
        String query = "INSERT INTO presupuestos (idEquipo, costosVariables, estado, precioTotal, diasEstimados, costoManoDeObra, totalProductos, observaciones, fechaHora) VALUES (?, ?, 2, ?, ?, ?,?,?, NOW())";
        int IDgenerado = -1; // Inicializar el ID generado

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idEquipo);
            pstmt.setFloat(2, costoVariable);
            pstmt.setFloat(3, totalGeneral);
            pstmt.setInt(4, tiempoEstimado);
            pstmt.setFloat(5,costoManoObra);
            pstmt.setFloat(6,totalProductos);
            pstmt.setString(7,obs);

            // Ejecutar la consulta de inserción
            int affectedRows = pstmt.executeUpdate();

            // Verificar si la inserción afectó filas
            if (affectedRows > 0) {
                // Obtener el ID generado
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        IDgenerado = rs.getInt(1); // Obtener el primer valor generado (idpresupuesto)
                    }
                }
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return IDgenerado; // Devolver el ID generado
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

}
