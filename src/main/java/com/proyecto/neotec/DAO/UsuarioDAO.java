package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
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
                usuario.setIdusuarios(resultSet.getInt("idusuarios"));
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

    public static String crearUsuario(Usuario usuario) {
        String mensaje ="";
        String sql = "INSERT INTO usuarios (nombre, apellido, dni, email, contrasenna, rol, activo, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setInt(3, usuario.getDni());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getContrasenna());  // Se espera que la contraseña ya esté cifrada con jBCrypt
            stmt.setInt(6, usuario.getRol().equalsIgnoreCase("Empleado") ? 1 : 0);  // Convertimos el rol de String a int
            stmt.setInt(7, 1 );
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                mensaje = "Usuario creado exitosamente.";
            }else {
                mensaje = "No se pudo ingresar el usuario";
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return mensaje;
    }

    public static void cambiarEstadoActivo(int idUsuario, int estado) {
        String sql = "UPDATE usuarios SET activo = ? WHERE idusuarios = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estado); // El nuevo estado es un String ("Activo" o "Inactivo")
            stmt.setInt(2, idUsuario);

            stmt.executeUpdate();
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
    }


    public static void actualizarUltimoAcceso(int dni) {
        String sql = "UPDATE usuarios SET ultimo_acceso = NOW() WHERE dni = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dni);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                System.out.println("No se encontró el usuario con DNI: " + dni);
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
    }


    public static String modificarUsuarioConContrasenna(Usuario usuarioNuevo) {
        String mensaje = "";
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?, email = ?, contrasenna = ?, fecha_modificacion = NOW() WHERE idusuarios = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer los valores en la sentencia preparada
            stmt.setString(1, usuarioNuevo.getNombre());
            stmt.setString(2, usuarioNuevo.getApellido());
            stmt.setInt(3, usuarioNuevo.getDni());
            stmt.setString(4, usuarioNuevo.getEmail());
            stmt.setString(5, usuarioNuevo.getContrasenna());  // Se espera que la contraseña ya esté cifrada con jBCrypt
            stmt.setInt(6, usuarioNuevo.getRol().equalsIgnoreCase("Empleado") ? 1 : 0);  // Convertimos el rol de String a int
            stmt.setInt(7, usuarioNuevo.getIdusuarios());  // ID del usuario a actualizar

            // Ejecutar la actualización
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                mensaje = "Usuario modificado exitosamente (incluye cambio de contraseña)";
            } else {
                mensaje = "No se pudo modificar el usuario. Verifique si el usuario existe.";
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return mensaje;
    }


    public static String modificarUsuarioSinContrasenna(Usuario usuarioNuevo) {
        String mensaje = "";
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?, email = ?, rol = ?, fecha_modificacion = NOW() WHERE idusuarios = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer los valores en la sentencia preparada
            stmt.setString(1, usuarioNuevo.getNombre());
            stmt.setString(2, usuarioNuevo.getApellido());
            stmt.setInt(3, usuarioNuevo.getDni());
            stmt.setString(4, usuarioNuevo.getEmail());
            stmt.setInt(5, usuarioNuevo.getRol().equalsIgnoreCase("Empleado") ? 1 : 0);  // Convertimos el rol de String a int
            stmt.setInt(6, usuarioNuevo.getIdusuarios());  // ID del usuario a actualizar

            // Ejecutar la actualización
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
                usuario.setRol(rs.getInt("rol") == 1 ? "Empleado" : "Admin"); // Convertimos el rol a String
                usuario.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo"); // Convertimos activo a "Activo"/"Inactivo"
                // Verificación de "ultimo_acceso"
                Timestamp ultimoAccesoTimestamp = rs.getTimestamp("ultimo_acceso");
                String ultimoAcceso;
                if (ultimoAccesoTimestamp != null) {
                    ultimoAcceso = String.valueOf(ultimoAccesoTimestamp.toLocalDateTime());
                } else {
                    ultimoAcceso = "-";
                }
                usuario.setUltimoAcceso(ultimoAcceso);

                // Verificación de "fecha_creacion"
                Timestamp fechaCreacionTimestamp = rs.getTimestamp("fecha_creacion");
                String fechaCreacion;
                if (fechaCreacionTimestamp != null) {
                    fechaCreacion = String.valueOf(fechaCreacionTimestamp.toLocalDateTime());
                } else {
                    fechaCreacion = "-";  // O cualquier valor por defecto
                }
                usuario.setFechaCreacion(fechaCreacion);

                // Verificación de "fecha_modificacion"
                Timestamp fechaModificacionTimestamp = rs.getTimestamp("fecha_modificacion");
                String fechaModificacion;
                if (fechaModificacionTimestamp != null) {
                    fechaModificacion = String.valueOf(fechaModificacionTimestamp.toLocalDateTime());
                } else {
                    fechaModificacion = "-";  // O cualquier valor por defecto
                }
                usuario.setFechaModificacion(fechaModificacion);


                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return usuarios;

    }

    // Método para verificar si el usuario está activo
    public static boolean verificarActivo(int dni) {
        int activo;
        boolean activoBol = true;
        String query = "SELECT activo FROM usuarios WHERE dni = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, dni);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                activo = resultSet.getInt("activo");
                if (activo == 0){
                    activoBol = false;
                }
            }


        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return activoBol;
    }

    public static Usuario obtenerUsuarioPorId(int responsable) {
        Usuario usuario = null;
        String query = "SELECT * FROM usuarios WHERE idusuarios = ?";

        try (Connection connection = Database.getConnection(); // Obtén la conexión desde tu clase Database
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Establece el parámetro de la consulta
            statement.setInt(1, responsable);

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
}

