package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    public List<Cliente> selectAllClientes() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT idclientes, nombre, apellido, email, telefono, dni, activo FROM clientes";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdclientes(rs.getInt("idclientes"));
                cliente.setNombre(rs.getString("nombre"));
                cliente.setApellido(rs.getString("apellido"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefono(rs.getString("telefono"));
                cliente.setDni(rs.getInt("dni"));

                // Convertir activo a "Activo" o "Inactivo"
                cliente.setActivo(rs.getInt("activo") == 1 ? "Activo" : "Inactivo");

                clientes.add(cliente);
            }
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }

        return clientes;
    }

    public static String crearCliente(Cliente cliente) {
        String mensaje ="";
        String sql = "INSERT INTO clientes (nombre, apellido, dni, email, telefono, activo, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setInt(3, cliente.getDni());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getTelefono());
            stmt.setInt(6, 1 );
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                mensaje = "Cliente creado exitosamente.";
            }else {
                mensaje = "No se pudo ingresar el cliente";
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return mensaje;
    }


    public static void cambiarEstadoActivo(int idCliente, int estado) {
        String sql = "UPDATE clientes SET activo = ? WHERE idclientes = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, estado);
            stmt.setInt(2, idCliente);

            stmt.executeUpdate();
        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
    }



    public static String modificarCliente(Cliente cliente) {
        String mensaje = "";
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, dni = ?, email = ?, telefono = ? WHERE idclientes = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer los valores en la sentencia preparada
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setInt(3, cliente.getDni());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getTelefono());
            stmt.setInt(6, cliente.getIdclientes());  // ID del cliente a actualizar

            // Ejecutar la actualización
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                mensaje = "Cliente modificado exitosamente.";
            } else {
                mensaje = "No se pudo modificar el Cliente. Verifique si el cliente existe.";
            }

        } catch (SQLException e) {
            Database.handleSQLException(e);
        }
        return mensaje;
    }


}
