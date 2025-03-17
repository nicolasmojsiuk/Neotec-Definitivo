package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TransaccionesDigitalesDAO {
    public boolean registrarTransaccion(int entradaSalida, float monto, int tipoTransaccion, String obs){
        String sql = "INSERT INTO transaccionesdigitales (entradaSalida,monto,fechaHora,tipoTransaccion,observacion) VALUES (?, ?, NOW(), ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entradaSalida);
            stmt.setFloat(2, monto);
            stmt.setInt(3, tipoTransaccion);
            stmt.setString(4, obs);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            Database.handleSQLException(e);
            return false; // Retornar false si hubo una excepción
        }
    }
}
