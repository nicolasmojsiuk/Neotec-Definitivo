package com.proyecto.neotec.util;

import com.proyecto.neotec.bbdd.Database;

import java.sql.*;
import java.time.LocalDateTime;

public class BloquearLogin {
    public static void bloquearLogin(int minutosBloqueo) {
        String SQL_UPDATE = "UPDATE bloquear SET bloqueado = 1, fyhdesbloqueo = ?;";
        try (
                Connection connection = Database.getConnection();
                PreparedStatement pstm = connection.prepareStatement(SQL_UPDATE);
        ) {
            LocalDateTime fechaDesbloqueo = LocalDateTime.now().plusMinutes(minutosBloqueo);
            pstm.setTimestamp(1, Timestamp.valueOf(fechaDesbloqueo));
            pstm.executeUpdate();
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
    }

    public static void desbloquearLogin() {
        String SQL_UPDATE = "UPDATE bloquear SET bloqueado = 0, fyhdesbloqueo = NULL;";
        try (
                Connection connection = Database.getConnection();
                PreparedStatement pstm = connection.prepareStatement(SQL_UPDATE);
        ) {
            pstm.executeUpdate();
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
    }


    public static boolean estaBloqueado() {
        String SQL_QUERY = "SELECT bloqueado, fyhdesbloqueo FROM bloquear;";
        try (
                Connection connection = Database.getConnection();
                PreparedStatement pstm = connection.prepareStatement(SQL_QUERY);
                ResultSet rs = pstm.executeQuery();
        ) {
            if (rs.next()) {
                int bloqueado = rs.getInt("bloqueado");
                Timestamp fyhDesbloqueo = rs.getTimestamp("fyhdesbloqueo");

                if (bloqueado == 1) {
                    if (fyhDesbloqueo != null && LocalDateTime.now().isAfter(fyhDesbloqueo.toLocalDateTime())) {
                        // Si la fecha y hora actual ya ha pasado la fecha de desbloqueo, desbloquear automáticamente
                        desbloquearLogin();  // Método que desbloquea el sistema
                        return false;  // Ya no está bloqueado
                    }
                    return true;  // Está bloqueado y aún no ha pasado el tiempo de desbloqueo
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return false;  // No está bloqueado si no hay resultados o si hay un error
    }
    public static boolean puedeDesbloquearse() {
        String SQL_QUERY = "SELECT fyhdesbloqueo FROM bloquear;";
        try (
                Connection connection = Database.getConnection();
                PreparedStatement pstm = connection.prepareStatement(SQL_QUERY);
                ResultSet rs = pstm.executeQuery();
        ) {
            if (rs.next()) {
                Timestamp fyhDesbloqueo = rs.getTimestamp("fyhdesbloqueo");
                if (fyhDesbloqueo != null) {
                    LocalDateTime fechaDesbloqueo = fyhDesbloqueo.toLocalDateTime();
                    return LocalDateTime.now().isAfter(fechaDesbloqueo);
                }
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return false;
    }







}
