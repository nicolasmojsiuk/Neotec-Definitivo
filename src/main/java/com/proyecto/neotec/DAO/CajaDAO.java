package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Caja;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CajaDAO {
    public Caja selectDatosCajaActual() {
        String sql = "SELECT * FROM caja";
        Caja caja = new Caja();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String estado;

                if (rs.getInt("estado")==0){
                    estado = "Cerrada";
                }else {
                    estado = "Abierta";
                }
                caja.setEstado(estado);
                caja.setIdoperacion(rs.getInt("idoperacion"));
                caja.setFechayhora(rs.getString("fechahora"));
                UsuarioDAO usuariodao = new UsuarioDAO();
                Usuario responsable = usuariodao.obtenerUsuarioPorId(rs.getInt("responsable"));
                caja.setReponsable(responsable);
                caja.setSaldoFinal(rs.getFloat("saldoFinal"));
                caja.setSaldoInicial(rs.getFloat("saldoInicial"));
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return caja;
    }

    public void abrirCerrarCaja(Caja cajaActual, float saldo) {
        String mensaje = "";
        String sql = "INSERT INTO caja (estado, saldoInicial, saldoFinal, responsable,fechaHora) VALUES (?,?,?,?,NOW())";


        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if(cajaActual.getEstado() == "Abierta") {
                stmt.setInt(1, 0);
                stmt.setFloat(2, cajaActual.saldoInicial);
                stmt.setFloat(3, saldo);
            }
            if(cajaActual.getEstado() == "Cerrada") {
                stmt.setInt(1, 1);
                stmt.setFloat(2, saldo);
                stmt.setFloat(3, 0);
            }



            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                mensaje = "Usuario modificado exitosamente.(No se modifico la contraseña)";
            } else {
                mensaje = "No se pudo modificar el usuario. Verifique si el usuario existe.";
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return mensaje;
    }
}
