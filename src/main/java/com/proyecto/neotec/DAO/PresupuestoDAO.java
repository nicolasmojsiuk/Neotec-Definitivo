package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Presupuestos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresupuestoDAO {
    public int insertPresupuesto(int tiempoEstimado, int costo, int manoObra, int total, int idEquipo) {
        String query = "INSERT INTO presupuesto (idEquipo, costoReparacion, estado, precioTotal, diasEstimados, manoDeObra) VALUES (?, ?, 2, ?, ?, ?)";
        int IDgenerado = -1; // Inicializar el ID generado

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idEquipo);
            pstmt.setInt(2, costo);
            pstmt.setInt(3, total);
            pstmt.setInt(4, tiempoEstimado);
            pstmt.setInt(5,manoObra);

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


    public void insertProductoPresupuesto(int idPresupuesto, int idProductos, int cantidadUtilizada) {
        String query = "INSERT INTO producto_presupuesto (IDpresupuestos, IDproductos, cantidadUtilizada) VALUES(?, ?, ?)";
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
    }

    public List<Presupuestos> selectAllPresupuestos() {
        List<Presupuestos> listaPresupuestos = new ArrayList<>();
        String sql= "SELECT * FROM presupuesto";
        try(Connection conn= Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()){
            while (rs.next()){
                EquipoDAO equipoDAO = new EquipoDAO();
                ClienteDAO clienteDAO = new ClienteDAO();
                Presupuestos presupuestos = new Presupuestos();
                presupuestos.setIdpresupuesto(rs.getInt("idpresupuesto"));
                presupuestos.setEquipo(equipoDAO.obtenerPropietario(rs.getInt("idEquipo")).getDispositivo());
                presupuestos.setEstado(rs.getInt("estado"));
                presupuestos.setCostoReparacion(rs.getInt("costoReparacion"));
                presupuestos.setPrecioTotal(rs.getInt("precioTotal"));
                presupuestos.setManoDeObra(rs.getInt("manoDeObra"));
                presupuestos.setDiasEstimados(rs.getInt("diasEstimados"));
                presupuestos.setPropietario(clienteDAO.obtenerNombre(equipoDAO.obtenerPropietario(rs.getInt("idEquipo")).getIdcliente()));
                listaPresupuestos.add(presupuestos);
            }

        }catch(SQLException e){
            Database.handleSQLException(e);
        }
        return listaPresupuestos;
    }



}
