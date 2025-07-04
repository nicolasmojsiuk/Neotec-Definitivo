package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TransaccionesDigitalesDAO {
    private static final Logger logger = Logger.getLogger(TransaccionesDigitalesDAO.class);
    public boolean registrarTransaccion(int entradaSalida, float monto, int tipoTransaccion, String obs) {

        String sql = "INSERT INTO transaccionesdigitales (entradaSalida, monto, fechaHora, tipoTransaccion, observacion) " +
                "VALUES (?, ?, NOW(), ?, ?)";

        logger.debug( "Registrando nueva transacción digital - Parámetros -> Entrada/Salida: " + entradaSalida +
                ", Monto: " + monto + ", TipoTransacción: " + tipoTransaccion + ", Observación: " + obs);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entradaSalida);
            stmt.setFloat(2, monto);
            stmt.setInt(3, tipoTransaccion);
            stmt.setString(4, obs);

            int filas = stmt.executeUpdate();

            if (filas > 0) {
                logger.info(  "Transacción Digital registrada exitosamente");
                return true;
            } else {
                logger.warn("La transacción no se insertó (0 filas afectadas)");
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error al registrar transacción | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug(  "StackTrace completo:", e);
            Database.handleSQLException(e);
            return false;
        }
    }
}
