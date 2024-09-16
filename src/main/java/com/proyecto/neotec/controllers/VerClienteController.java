package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class VerClienteController {


    @FXML
    private TableView<Cliente> tablaClientes;
    @FXML
    private Label lblUsuarios;

    @FXML
    private TableColumn<Cliente, Integer> columna1;
    @FXML
    private TableColumn<Cliente, String> columna2;
    @FXML
    private TableColumn<Cliente, Integer> columna3;
    @FXML
    private TableColumn<Cliente, Integer> columna4;
    @FXML
    private TableColumn<Cliente, String> columna5;
    @FXML
    private TableColumn<Cliente, String> columna6;
    @FXML
    private TableColumn<Cliente, String> columna7;

    private ObservableList<Cliente> clientes;
    private ClienteDAO clienteDAO;

    @FXML
    public void initialize() {
        // Inicializar el DAO y la lista observable
        clienteDAO = new ClienteDAO();
        // Cargar datos
        cargarDatos();
    }

    private void cargarDatos() {
        clientes = FXCollections.observableArrayList();
        // Configurar columnas
        columna1.setCellValueFactory(new PropertyValueFactory<>("idclientes"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columna3.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        columna4.setCellValueFactory(new PropertyValueFactory<>("dni"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("email"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        columna7.setCellValueFactory(new PropertyValueFactory<>("activo"));

        List<Cliente> listaClientes = clienteDAO.selectAllClientes();
        // Limpiar la lista observable y agregar los nuevos datos
        clientes.clear();
        clientes.addAll(listaClientes);
        // Configurar el TableView con la lista observable
        tablaClientes.setItems(clientes);
    }

    public void mostrarFormModificarCliente() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            mostrarAlerta("Error", "No se ha seleccionado ningún cliente.", Alert.AlertType.ERROR);
            return;
        }


        try {
            // Cargar el archivo FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/modificarCliente.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del archivo FXML
            ModClienteController controller = loader.getController();
            // Pasar el usuario seleccionado al controlador
            controller.setCliente(clienteSeleccionado);

            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);
            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Crear Cliente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

            // Mostrar el pop-up
            stage.showAndWait();

            cargarDatos();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mostrarFormCrearCliente() {
        try {
            // Cargar el archivo FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/crearCliente.fxml"));
            Parent root = loader.load();
            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);
            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Crear Cliente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre
            // Mostrar el pop-up
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cambiarActivoCliente() {
        // Verifica si hay un usuario seleccionado en la tabla
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado != null) {
            int idCliente = clienteSeleccionado.getIdclientes(); // Obtener el ID del usuario seleccionado
            // Cambiar el estado actual de "Activo" a "Inactivo" y viceversa
            String estadoNuevoString = clienteSeleccionado.getActivo().equals("Activo") ? "Inactivo" : "Activo";
            int nuevoEstado;
            //pasarlo a int
            if(estadoNuevoString == "Activo"){
                nuevoEstado=1;
            }else {
                nuevoEstado=0;
            }

            // Llamar al método del DAO para cambiar el estado en la base de datos
            ClienteDAO.cambiarEstadoActivo(idCliente, nuevoEstado);
            // Actualizar el estado del usuario en la tabla
            cargarDatos();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Activacion / Desactivacion");
            alert.setHeaderText(null);
            alert.setContentText("Se ha cambiado el estado del cliente "+clienteSeleccionado.getNombre()+" "+clienteSeleccionado.getApellido());
            alert.showAndWait();
        } else {
            // Mostrar alerta o mensaje indicando que no hay un usuario seleccionado
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Activacion / Desactivacion");
            alert.setContentText("Debe seleccionar un usuario en la tabla");
            alert.showAndWait();
        }
    }


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
