package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public static boolean verificarCredenciales(int dni, String contrasenna) {
        String SQL_QUERY = "SELECT contrasenna FROM usuarios WHERE dni = ?";

        try (
                Connection connection = Database.getConnection();
                PreparedStatement pstm = connection.prepareStatement(SQL_QUERY)
        ) {
            // Establece el DNI en el PreparedStatement
            pstm.setInt(1, dni);

            // Ejecuta la consulta
            ResultSet rs = pstm.executeQuery();

            // Si el usuario existe, verifica la contraseña
            if (rs.next()) {
                String contrasennaHash = rs.getString("contrasenna");

                // Compara la contraseña proporcionada con el hash almacenado
                return BCrypt.checkpw(contrasenna, contrasennaHash);
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        // Retorna false si el usuario no existe o la contraseña no coincide
        return false;
    }

    // Método para verificar el rol de un usuario basado en el DNI
    public static int verificarRol(int dni) {
        String SQL_QUERY = "SELECT rol FROM usuarios WHERE dni = ?";
        int rol = -1; // Valor predeterminado para indicar que el DNI no se encontró

        try (Connection connection = Database.getConnection();
             PreparedStatement pstm = connection.prepareStatement(SQL_QUERY)) {

            pstm.setInt(1, dni);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                rol = rs.getInt("rol");
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return rol;
    }

    public static Usuario obtenerUsuarioPorDni(int dni) {
        Usuario usuario = null;
        String query = "SELECT * FROM usuarios WHERE dni = ?";

        try (Connection connection = Database.getConnection(); // Obtén la conexión desde tu clase Database
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Establece el parámetro de la consulta
            statement.setInt(1, dni);

            // Ejecuta la consulta
            ResultSet resultSet = statement.executeQuery();

            // Procesa el resultado
            if (resultSet.next()) {
                // Crea el objeto Usuario basado en los datos de la base de datos
                usuario = new Usuario();
                usuario.setNombre(resultSet.getString("nombre"));
                usuario.setApellido(resultSet.getString("apellido"));
                usuario.setDni(resultSet.getInt("dni"));
                usuario.setContrasenna(resultSet.getString("contrasenna"));
                usuario.setActivo(String.valueOf(resultSet.getBoolean("activo")));
                int rol= resultSet.getInt("rol");
                String rolUsuario = "";
                if (rol==1){
                    rolUsuario = "Empleado";
                }else{
                    rolUsuario = "Administrador";
                }
                usuario.setRol(rolUsuario);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Maneja las excepciones adecuadamente
        }

        return usuario;
    }

    public List<Usuario> selectAllUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT idusuarios, nombre, apellido, dni, email, rol, activo, ultimo_acceso, fecha_creacion, fecha_modificacion FROM usuarios";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdusuarios(rs.getInt("idusuarios"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellido(rs.getString("apellido"));
                usuario.setDni(rs.getInt("dni"));
                usuario.setEmail(rs.getString("email"));

                // Convertir rol y activo a String
                usuario.setRol(String.valueOf(rs.getInt("rol"))); // Convertimos el rol a String
                usuario.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo"); // Convertimos activo a "Activo"/"Inactivo"

                usuario.setUltimoAcceso(rs.getTimestamp("ultimo_acceso").toLocalDateTime());
                usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                usuario.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());

                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return usuarios;
    }
}

