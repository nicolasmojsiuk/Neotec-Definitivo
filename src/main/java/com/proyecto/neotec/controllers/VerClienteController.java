package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.models.Cliente;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VerClienteController {
    @FXML
    public ToggleButton toggleNombreCliente;
    @FXML
    public TextField txtBuscardor;
    @FXML
    public ToggleButton toggleActivos;
    @FXML
    public ToggleButton toggleInactivos;
    @FXML
    public ToggleButton toggleEmail;
    @FXML
    public ToggleButton toggleTelefono;
    @FXML
    public ToggleButton toggleDNI;
    @FXML
    private TableView<Cliente> tablaClientes;
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
    private static final Logger logger = Logger.getLogger(VerClienteController.class);

    private ObservableList<Cliente> clientes;
    private ClienteDAO clienteDAO;

    @FXML
    public void initialize() {
        clienteDAO = new ClienteDAO();
        cargarDatos();

        txtBuscardor.setDisable(true);

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleDNI.setToggleGroup(toggleGroup);
        toggleEmail.setToggleGroup(toggleGroup);
        toggleNombreCliente.setToggleGroup(toggleGroup);
        toggleTelefono.setToggleGroup(toggleGroup);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), event -> {
                    String newValue = txtBuscardor.getText().trim();
                    if (!newValue.isEmpty()) {
                        ClienteDAO DAO = new ClienteDAO();
                        List<Cliente> listaclientes = new ArrayList<>();
                        if (toggleEmail.isSelected()) {
                            listaclientes = DAO.buscarPorEmail(newValue);
                        }
                        if (toggleDNI.isSelected()) {
                            listaclientes = DAO.buscarPorDNI(newValue);
                        }
                        if (toggleTelefono.isSelected()) {
                            listaclientes = DAO.buscarPorTelefono(newValue);
                        }
                        if (toggleNombreCliente.isSelected()) {
                            listaclientes = DAO.buscarClientes(newValue);
                        }
                        tablaClientes.getItems().setAll(listaclientes);
                    } else {
                        tablaClientes.getItems().clear();
                    }
                })
        );
        timeline.setCycleCount(1);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                txtBuscardor.setDisable(true);
                txtBuscardor.setText("");
                cargarDatos();
            } else {
                txtBuscardor.setDisable(false);
                if (!txtBuscardor.getText().trim().isEmpty()) {
                    timeline.playFromStart();
                }
            }
        });

        txtBuscardor.textProperty().addListener((observable, oldValue, newValue) -> {
            timeline.stop();
            if (!newValue.trim().isEmpty()) {
                timeline.playFromStart();
            } else {
                cargarDatos();
            }
        });

        ToggleGroup toggleGroupEstados = new ToggleGroup();
        toggleActivos.setToggleGroup(toggleGroupEstados);
        toggleInactivos.setToggleGroup(toggleGroupEstados);

        toggleGroupEstados.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ToggleButton selectedToggle = (ToggleButton) newValue;
                if (selectedToggle.equals(toggleActivos)) {
                    BuscarActivosInnactivos(1);
                } else if (selectedToggle.equals(toggleInactivos)) {
                    BuscarActivosInnactivos(0);
                }
            } else {
                cargarDatos();
            }
        });
    }
    private void BuscarActivosInnactivos(int estado) {
        logger.debug("Buscando clientes Activos/Innativos. Parametro de busqueda:"+ estado);
        List<Cliente> listaclientes =clienteDAO.filtrarActivoInnactivo(estado);
        clientes.clear();
        clientes.addAll(listaclientes);
        // Configurar el TableView con la lista observable
        tablaClientes.setItems(clientes);
    }
    private void cargarDatos() {
        logger.debug("Intento de cargar datos por pantalla");
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
            logger.warn("No se ha seleccionado ningún cliente para modificar");
            mostrarAlerta("Error", "No se ha seleccionado ningún cliente.", Alert.AlertType.ERROR);
            return;
        }
        logger.debug("Intento de abrir formulario modificar cliente..");
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
            logger.error("Error, ha ocurrido una excepción al intentar abrir el fomulario. Detalles:"+ e.getMessage() + ". " +e);
        }
    }

    public void mostrarFormCrearCliente() {
        logger.debug("Intento de cargar el formulario crear clientes");
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
            logger.error("Error, Ha ocurrido una excepción al cargar el formulario crear clientes. Detalles: "+ e.getMessage()+ ". " + e);
        }
    }

    public void cambiarActivoCliente() {
        logger.debug("Intento de cambiar el estado del cliente a Activo/Innactivo");
        // Verifica si hay un usuario seleccionado en la tabla
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado != null) {
            int idCliente = clienteSeleccionado.getIdclientes(); // Obtener el ID del usuario seleccionado
            // Cambiar el estado actual de "Activo" a "Inactivo" y viceversa
            String estadoNuevoString = clienteSeleccionado.getActivo().equals("Activo") ? "Inactivo" : "Activo";
            logger.debug("Nuevo estado para el cliente " + clienteSeleccionado.getNombre()+" "+clienteSeleccionado.getApellido()+ ": "+estadoNuevoString);
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
            logger.warn("No se ha seleccionado ningún cliente de la tabla");
            // Mostrar alerta o mensaje indicando que no hay un usuario seleccionado
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Activacion / Desactivacion");
            alert.setContentText("Debe seleccionar un cliente en la tabla");
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
