package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Usuario;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private static final Logger logger= Logger.getLogger(UsuarioDAO.class);
    public static boolean verificarCredenciales(int dni, String contrasenna) {
        String SQL_QUERY = "SELECT contrasenna FROM usuarios WHERE dni = ?";

        logger.info("Iniciando verificación de credenciales para DNI: " + dni);

        try (
                Connection connection = Database.getConnection();
                PreparedStatement pstm = connection.prepareStatement(SQL_QUERY)
        ) {
            pstm.setInt(1, dni);
            logger.debug("Parámetros establecidos - DNI: " + dni);

            ResultSet rs = pstm.executeQuery();
            logger.debug("Consulta ejecutada, procesando resultados...");

            if (rs.next()) {
                String contrasennaHash = rs.getString("contrasenna");
                logger.debug("Hash de contraseña recuperado de la base de datos");

                boolean credencialesCorrectas = BCrypt.checkpw(contrasenna, contrasennaHash);

                if(credencialesCorrectas) {
                    logger.info("Credenciales válidas para DNI: " + dni);
                } else {
                    logger.warn("Contraseña incorrecta para DNI: " + dni);
                }

                return credencialesCorrectas;
            } else {
                logger.warn("No se encontró usuario con DNI: " + dni);
            }

        } catch (SQLException e) {
            logger.error("Error al verificar credenciales para DNI: " + dni +
                    " - Error: " + e.getMessage(), e);
            Database.handleSQLException(e);
        }

        logger.info("Credenciales no válidas para DNI: " + dni);
        return false;
    }
    public static int verificarRol(int dni) {

        String SQL_QUERY = "SELECT rol FROM usuarios WHERE dni = ?";
        int rol = -1; // Valor predeterminado para indicar que el DNI no se encontró

        logger.info("Iniciando verificación de rol para DNI: " + dni);


        try (Connection connection = Database.getConnection();
             PreparedStatement pstm = connection.prepareStatement(SQL_QUERY)) {

            logger.debug( "Estableciendo parámetros [DNI: " + dni + "]");
            pstm.setInt(1, dni);

            logger.trace("Ejecutando consulta...");
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                rol = rs.getInt("rol");
                logger.info(" Rol " + rol + " encontrado para DNI: " + dni);

                // Log adicional para roles específicos (opcional)
                switch(rol) {
                    case 1:
                        logger.debug("Usuario con privilegios de Administrador");
                        break;
                    case 2:
                        logger.debug("Usuario con privilegios de Vendedor");
                        break;
                    // ... otros casos según necesidad
                }
            } else {
                logger.warn("No se encontró usuario con DNI: " + dni);
            }

        } catch (SQLException e) {
            logger.error( "Error al verificar rol para DNI: " + dni +
                            " | Error: " + e.getMessage() +
                            " | SQL State: " + e.getSQLState(),
                    e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando valor de rol: " + rol + " para DNI: " + dni);
        return rol;
    }

    public static Usuario obtenerUsuarioPorDni(int dni) {
        Usuario usuario = null;
        String query = "SELECT * FROM usuarios WHERE dni = ?";

        logger.info( "Intentando buscar un usuario con DNI: " + dni);
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            logger.debug( "Estableciendo parámetro DNI: " + dni);
            statement.setInt(1, dni);

            logger.trace("Ejecutando consulta...");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                logger.debug("Usuario encontrado, construyendo objeto...");

                usuario = new Usuario();
                usuario.setIdusuarios(resultSet.getInt("idusuarios"));
                usuario.setNombre(resultSet.getString("nombre"));
                usuario.setApellido(resultSet.getString("apellido"));
                usuario.setEmail(resultSet.getString("email"));
                usuario.setDni(resultSet.getInt("dni"));

                // Log seguro (no mostrar datos sensibles)
                logger.debug("Datos básicos usuario: " +
                        "Nombre=" + usuario.getNombre() + " " + usuario.getApellido() +
                        ", Email=" + usuario.getEmail());

                // Manejo seguro de contraseña (no se loguea)
                usuario.setContrasenna(resultSet.getString("contrasenna"));

                usuario.setActivo(String.valueOf(resultSet.getBoolean("activo")));

                int rol = resultSet.getInt("rol");
                String rolUsuario = (rol == 1) ? "Empleado" : "Administrador";
                usuario.setRol(rolUsuario);

                logger.info("Usuario encontrado: " + usuario.getNombre() +
                        " " + usuario.getApellido() + " | Rol: " + rolUsuario);
            } else {
                logger.warn("No se encontró usuario con DNI: " + dni);
            }

        } catch (SQLException e) {
            logger.error("Error al buscar usuario con DNI: " + dni +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( "StackTrace completo:", e);
        }

        if (usuario == null) {
            logger.debug("Retornando null (usuario no encontrado)");
        } else {
            logger.debug("Retornando objeto Usuario (DNI: " + dni + ")");
        }

        return usuario;
    }

    public static String crearUsuario(Usuario usuario) {

        String mensaje = "";
        String sql = "INSERT INTO usuarios (nombre, apellido, dni, email, contrasenna, rol, activo, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        // Log de inicio con información básica (sin datos sensibles)
        logger.info("Iniciando creación de usuario para: " +
                usuario.getNombre() + " " + usuario.getApellido());
        logger.debug("Datos usuario [DNI: " + usuario.getDni() +
                ", Rol: " + usuario.getRol() + ", Activo: 1]");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer parámetros
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setInt(3, usuario.getDni());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getContrasenna());
            stmt.setInt(6, usuario.getRol().equalsIgnoreCase("Empleado") ? 1 : 0);
            stmt.setInt(7, 1);

            logger.trace("Parámetros establecidos, ejecutando inserción...");
            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                mensaje = "Usuario creado exitosamente.";
                logger.info(mensaje + " DNI: " + usuario.getDni());
                logger.debug("Filas afectadas: " + rowsInserted);
            } else {
                mensaje = "No se pudo ingresar el usuario";
                logger.warn(mensaje + " DNI: " + usuario.getDni());
            }

        } catch (SQLException e) {
            mensaje = "Error al crear usuario";
            logger.error(mensaje + " DNI: " + usuario.getDni() +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug("Retornando mensaje: " + mensaje);
        return mensaje;
    }
    public static void cambiarEstadoActivo(int idUsuario, int estado) {
        String sql = "UPDATE usuarios SET activo = ? WHERE idusuarios = ?";
        String estadoTexto = (estado == 1) ? "Activo" : "Inactivo";

        // Log de inicio
        logger.info( "Iniciando cambio de estado. ID Usuario: " + idUsuario +
                ", Nuevo estado: " + estadoTexto);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.debug("Estableciendo parámetros - Estado: " + estado +
                    ", ID Usuario: " + idUsuario);
            stmt.setInt(1, estado);
            stmt.setInt(2, idUsuario);

            logger.trace("Ejecutando actualización...");
            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                logger.info("Estado actualizado correctamente. ID: " + idUsuario +
                        " - Nuevo estado: " + estadoTexto);
                logger.debug( "Filas afectadas: " + filasAfectadas);
            } else {
                logger.warn("No se encontró usuario con ID: " + idUsuario +
                        " o no se realizaron cambios");
            }

        } catch (SQLException e) {
            logger.error("Error al cambiar estado. ID Usuario: " + idUsuario +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug("Finalizado cambio de estado para ID: " + idUsuario);
    }


    public static void actualizarUltimoAcceso(int dni) {
        String sql = "UPDATE usuarios SET ultimo_acceso = NOW() WHERE dni = ?";

        // Log de inicio
        logger.info("Actualizando último acceso para DNI: " + dni);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.debug("Estableciendo parámetro DNI: " + dni);
            stmt.setInt(1, dni);

            logger.trace("Ejecutando actualización...");
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                logger.info("Último acceso actualizado correctamente para DNI: " + dni);
                logger.debug("Filas afectadas: " + rowsUpdated);
            } else {
                logger.warn("No se encontró usuario con DNI: " + dni);
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar último acceso. DNI: " + dni +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug("Finalizado proceso para DNI: " + dni);
    }
    public static String modificarUsuarioConContrasenna(Usuario usuarioNuevo) {
        String mensaje = "";
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?, email = ?, contrasenna = ?, rol= ?, fecha_modificacion = NOW() WHERE idusuarios = ?";

        // Log de inicio (sin datos sensibles)
        logger.info("Iniciando modificación de usuario. ID: " + usuarioNuevo.getIdusuarios());
        logger.debug("Datos a actualizar [Nombre: " + usuarioNuevo.getNombre() +
                " " + usuarioNuevo.getApellido() + ", DNI: " + usuarioNuevo.getDni() +
                ", Rol: " + usuarioNuevo.getRol() + "]");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer parámetros
            stmt.setString(1, usuarioNuevo.getNombre());
            stmt.setString(2, usuarioNuevo.getApellido());
            stmt.setInt(3, usuarioNuevo.getDni());
            stmt.setString(4, usuarioNuevo.getEmail());
            stmt.setString(5, usuarioNuevo.getContrasenna());
            stmt.setInt(6, usuarioNuevo.getRol().equalsIgnoreCase("Empleado") ? 1 : 0);
            stmt.setInt(7, usuarioNuevo.getIdusuarios());

            logger.trace("Parámetros establecidos, ejecutando actualización...");
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                mensaje = "Usuario modificado exitosamente (incluye cambio de contraseña)";
                logger.info( mensaje + " ID: " + usuarioNuevo.getIdusuarios());
                logger.debug( "Filas afectadas: " + rowsUpdated);
            } else {
                mensaje = "No se pudo modificar el usuario. Verifique si el usuario existe.";
                logger.warn(mensaje + " ID: " + usuarioNuevo.getIdusuarios());
            }

        } catch (SQLException e) {
            mensaje = "Error al modificar usuario";
            logger.error(mensaje + " ID: " + usuarioNuevo.getIdusuarios() +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug( "Resultado: " + mensaje);
        return mensaje;
    }

    public static String modificarUsuarioSinContrasenna(Usuario usuarioNuevo) {
        String mensaje = "";
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?, email = ?, rol = ?, fecha_modificacion = NOW() WHERE idusuarios = ?";

        // Log de inicio con información básica
        logger.info("Iniciando modificación de usuario (sin contraseña). ID: " + usuarioNuevo.getIdusuarios());
        logger.debug("Datos a actualizar: " +
                "Nombre=" + usuarioNuevo.getNombre() + " " + usuarioNuevo.getApellido() +
                ", DNI=" + usuarioNuevo.getDni() +
                ", Rol=" + usuarioNuevo.getRol());

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Configurar parámetros
            stmt.setString(1, usuarioNuevo.getNombre());
            stmt.setString(2, usuarioNuevo.getApellido());
            stmt.setInt(3, usuarioNuevo.getDni());
            stmt.setString(4, usuarioNuevo.getEmail());
            stmt.setInt(5, usuarioNuevo.getRol().equalsIgnoreCase("Empleado") ? 1 : 0);
            stmt.setInt(6, usuarioNuevo.getIdusuarios());

            logger.trace("Parámetros establecidos, ejecutando actualización...");
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                mensaje = "Usuario modificado exitosamente (sin cambio de contraseña)";
                logger.info( mensaje + " ID: " + usuarioNuevo.getIdusuarios());
                logger.debug("Filas afectadas: " + rowsUpdated);
            } else {
                mensaje = "No se pudo modificar el usuario. Verifique si el usuario existe.";
                logger.warn(mensaje + " ID: " + usuarioNuevo.getIdusuarios());
            }

        } catch (SQLException e) {
            mensaje = "Error al modificar usuario (sin contraseña)";
            logger.error(mensaje + " ID: " + usuarioNuevo.getIdusuarios() +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug("Resultado final: " + mensaje);
        return mensaje;
    }
    public List<Usuario> selectAllUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT idusuarios, nombre, apellido, dni, email, rol, activo, ultimo_acceso, fecha_creacion, fecha_modificacion FROM usuarios";

        // Log de inicio
        logger.info("Iniciando consulta de todos los usuarios");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            logger.trace("Ejecutando consulta...");
            int contadorUsuarios = 0;

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdusuarios(rs.getInt("idusuarios"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellido(rs.getString("apellido"));
                usuario.setDni(rs.getInt("dni"));
                usuario.setEmail(rs.getString("email"));

                // Convertir rol y activo a String
                usuario.setRol(rs.getInt("rol") == 1 ? "Empleado" : "Admin");
                usuario.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

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
                    fechaCreacion = "-";
                }
                usuario.setFechaCreacion(fechaCreacion);

                // Verificación de "fecha_modificacion"
                Timestamp fechaModificacionTimestamp = rs.getTimestamp("fecha_modificacion");
                String fechaModificacion;
                if (fechaModificacionTimestamp != null) {
                    fechaModificacion = String.valueOf(fechaModificacionTimestamp.toLocalDateTime());
                } else {
                    fechaModificacion = "-";
                }
                usuario.setFechaModificacion(fechaModificacion);

                usuarios.add(usuario);
                contadorUsuarios++;

                // Log detallado por usuario (solo en DEBUG)
                logger.debug("Usuario encontrado: ID=" + usuario.getIdusuarios() +
                        ", Nombre=" + usuario.getNombre() + " " + usuario.getApellido() +
                        ", Rol=" + usuario.getRol() + ", Estado=" + usuario.getActivo());
            }

            logger.info("Consulta completada. Total usuarios encontrados: " + contadorUsuarios);

        } catch (SQLException e) {
            logger.error( "Error al obtener lista de usuarios. Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug("StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        return usuarios;
    }

    // Método para verificar si el usuario está activo
    public static boolean verificarActivo(int dni) {
        String sql = "SELECT activo FROM usuarios WHERE dni = ?";
        boolean activoBol = true;

        // Log de inicio
        logger.info( "Verificando si el usuario está activo. DNI: " + dni);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.debug(" Estableciendo parámetro DNI: " + dni);
            stmt.setInt(1, dni);

            logger.trace( "Ejecutando consulta...");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int activo = rs.getInt("activo");
                logger.debug( " Valor obtenido de 'activo': " + activo);

                if (activo == 0) {
                    activoBol = false;
                    logger.info(" Usuario INACTIVO. DNI: " + dni);
                } else {
                    logger.info("Usuario ACTIVO. DNI: " + dni);
                }
            } else {
                logger.warn("No se encontró usuario con DNI: " + dni);
            }

        } catch (SQLException e) {
            logger.error( " Error al verificar estado activo. DNI: " + dni +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( "StackTrace completo:", e);
            Database.handleSQLException(e);
            return false;
        }

        logger.debug( "Estado retornado: " + (activoBol ? "ACTIVO" : "INACTIVO") +
                " para DNI: " + dni);
        logger.debug( "Finalizado proceso para DNI: " + dni);
        return activoBol;
    }


    public static Usuario obtenerUsuarioPorId(int responsable) {
        String query = "SELECT * FROM usuarios WHERE idusuarios = ?";
        Usuario usuario = null;

        logger.info( "Buscando usuario por ID: " + responsable);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            logger.debug("Estableciendo parámetro ID: " + responsable);
            statement.setInt(1, responsable);

            logger.trace(  "Ejecutando consulta...");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                logger.debug( "Usuario encontrado, procesando datos...");

                usuario = new Usuario();
                usuario.setNombre(resultSet.getString("nombre"));
                usuario.setApellido(resultSet.getString("apellido"));
                usuario.setDni(resultSet.getInt("dni"));
                usuario.setContrasenna(resultSet.getString("contrasenna"));
                usuario.setActivo(String.valueOf(resultSet.getBoolean("activo")));

                int rol = resultSet.getInt("rol");
                String rolUsuario = (rol == 1) ? "Empleado" : "Administrador";
                usuario.setRol(rolUsuario);

                logger.info( " Usuario procesado correctamente. ID: " + responsable);
            } else {
                logger.warn(" No se encontró usuario con ID: " + responsable);
            }

        } catch (SQLException e) {
            logger.error( "Error al obtener usuario por ID: " + responsable +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( "StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug( "Finalizado proceso para ID: " + responsable);
        return usuario;
    }
    public List<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email LIKE ?";
        List<Usuario> usuarios = new ArrayList<>();

        logger.info( "Buscando usuarios por email que contiene: " + email);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String parametro = "%" + email + "%";
            logger.debug( " Estableciendo parámetro LIKE: " + parametro);
            stmt.setString(1, parametro);

            logger.trace( "Ejecutando consulta...");
            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setIdusuarios(rs.getInt("idusuarios"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setApellido(rs.getString("apellido"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setDni(rs.getInt("dni"));
                    usuario.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");
                    usuario.setContrasenna(rs.getString("contrasenna"));
                    usuario.setFechaCreacion(rs.getString("fecha_creacion"));
                    usuario.setFechaModificacion(rs.getString("fecha_modificacion"));
                    usuario.setRol(rs.getString("rol"));
                    usuario.setUltimoAcceso(rs.getString("ultimo_acceso"));

                    usuarios.add(usuario);
                    contador++;
                }
                logger.info(" Usuarios encontrados: " + contador);
            }

        } catch (SQLException e) {
            logger.error(  "Error al buscar usuarios por email: " + email +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( "StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug( "Finalizado proceso de búsqueda para email: " + email);
        return usuarios;
    }

    public List<Usuario> buscarPorDNI(String dni) {
        String sql = "SELECT * FROM usuarios WHERE dni LIKE ?";
        List<Usuario> usuarios = new ArrayList<>();

        logger.info( "Buscando usuarios por DNI que contiene: " + dni);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String parametro = "%" + dni + "%";
            logger.debug( "Estableciendo parámetro LIKE: " + parametro);
            stmt.setString(1, parametro);

            logger.trace( "Ejecutando consulta...");
            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setIdusuarios(rs.getInt("idusuarios"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setApellido(rs.getString("apellido"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setDni(rs.getInt("dni"));
                    usuario.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");
                    usuario.setContrasenna(rs.getString("contrasenna"));
                    usuario.setFechaCreacion(rs.getString("fecha_creacion"));
                    usuario.setFechaModificacion(rs.getString("fecha_modificacion"));
                    usuario.setRol(rs.getString("rol"));
                    usuario.setUltimoAcceso(rs.getString("ultimo_acceso"));

                    usuarios.add(usuario);
                    contador++;
                }
                logger.info("Usuarios encontrados: " + contador);
            }

        } catch (SQLException e) {
            logger.error( "Error al buscar usuarios por DNI: " + dni +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( " StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug( " Finalizado proceso de búsqueda para DNI: " + dni);
        return usuarios;
    }

    public List<Usuario> buscarNombreCompleto(String nombre) {
        String sql = "SELECT * FROM usuarios WHERE nombre LIKE ? OR apellido LIKE ?";
        List<Usuario> usuarios = new ArrayList<>();

        logger.info( " Buscando usuarios por nombre o apellido que contiene: " + nombre);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + nombre + "%";
            logger.debug( "Estableciendo parámetros LIKE: " + searchPattern);
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            logger.trace( "Ejecutando consulta...");
            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setIdusuarios(rs.getInt("idusuarios"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setApellido(rs.getString("apellido"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setDni(rs.getInt("dni"));
                    usuario.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");
                    usuario.setContrasenna(rs.getString("contrasenna"));
                    usuario.setFechaCreacion(rs.getString("fecha_creacion"));
                    usuario.setFechaModificacion(rs.getString("fecha_modificacion"));
                    usuario.setRol(rs.getString("rol"));
                    usuario.setUltimoAcceso(rs.getString("ultimo_acceso"));

                    usuarios.add(usuario);
                    contador++;
                }
                logger.info( "Usuarios encontrados: " + contador);
            }

        } catch (SQLException e) {
            logger.error( "Error al buscar usuarios por nombre/apellido: " + nombre +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( "StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug(  " Finalizado proceso de búsqueda para nombre: " + nombre);
        return usuarios;
    }

    public List<Usuario> filtrarActivoInnactivo(int estado) {
        String query = "SELECT * FROM usuarios WHERE activo = ?";
        List<Usuario> usuarios = new ArrayList<>();

        logger.info( " Filtrando usuarios por estado: " + (estado == 1 ? "Activo" : "Inactivo"));

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            logger.debug( "Estableciendo parámetro 'activo': " + estado);
            stmt.setInt(1, estado);

            logger.trace( " Ejecutando consulta...");
            try (ResultSet rs = stmt.executeQuery()) {
                int contador = 0;
                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setIdusuarios(rs.getInt("idusuarios"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setApellido(rs.getString("apellido"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setDni(rs.getInt("dni"));
                    usuario.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");
                    usuario.setContrasenna(rs.getString("contrasenna"));
                    usuario.setFechaCreacion(rs.getString("fecha_creacion"));
                    usuario.setFechaModificacion(rs.getString("fecha_modificacion"));
                    usuario.setRol(rs.getString("rol"));
                    usuario.setUltimoAcceso(rs.getString("ultimo_acceso"));

                    usuarios.add(usuario);
                    contador++;
                }
                logger.info( "Usuarios encontrados: " + contador);
            }

        } catch (SQLException e) {
            logger.error( "Error al filtrar usuarios por estado: " + estado +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( "StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug( "Finalizado proceso de filtrado por estado: " + estado);
        return usuarios;
    }

    public String actualizarContrasenia(int idusuario, String contrasenia) {
        String mensaje = "";
        String sql = "UPDATE usuarios SET contrasenna = ?, fecha_modificacion = NOW() WHERE idusuarios = ?";

        logger.info( "Actualizando contraseña para ID de usuario: " + idusuario);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.debug(" Estableciendo parámetros: [contrasenia oculta], ID: " + idusuario);
            stmt.setString(1, contrasenia); // No se logea la contraseña por seguridad
            stmt.setInt(2, idusuario);

            logger.trace( " Ejecutando actualización...");
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                mensaje = "Su contraseña se actualizó exitosamente. Ya puede iniciar sesión con su nueva contraseña";
                logger.info("Contraseña actualizada correctamente para ID: " + idusuario);
                logger.debug("Filas afectadas: " + rowsUpdated);
            } else {
                mensaje = "No se pudo actualizar la contraseña. Ocurrió un error inesperado";
                logger.warn("No se encontró usuario con ID: " + idusuario);
            }

        } catch (SQLException e) {
            mensaje = "Error al actualizar la contraseña. Intente nuevamente.";
            logger.error("Error al actualizar contraseña para ID: " + idusuario +
                    " | Error: " + e.getMessage() +
                    " | SQL State: " + e.getSQLState());
            logger.debug( "StackTrace completo:", e);
            Database.handleSQLException(e);
        }

        logger.debug( "Finalizado proceso de actualización para ID: " + idusuario);
        return mensaje;
    }
}

