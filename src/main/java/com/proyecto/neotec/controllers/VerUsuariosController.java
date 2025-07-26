package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Usuario;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VerUsuariosController {
    @FXML
    public TextField txtBuscardor;
    @FXML
    public ToggleButton toggleNombreCompleto;
    @FXML
    public ToggleButton toggleActivos;
    @FXML
    public ToggleButton toggleInactivos;
    @FXML
    public ToggleButton toggleEmail;
    @FXML
    public ToggleButton toggleDNI;

    public ToggleButton ordenarASC;

    public ToggleButton ordenarDESC;
    public DatePicker dateFechaCreacion;
    public DatePicker dateFechaUltimoIngreso;
    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, Integer> columna1;
    @FXML
    private TableColumn<Usuario, String> columna2;
    @FXML
    private TableColumn<Usuario, Integer> columna3;
    @FXML
    private TableColumn<Usuario, Integer> columna4;
    @FXML
    private TableColumn<Usuario, String> columna5;
    @FXML
    private TableColumn<Usuario, String> columna6;
    @FXML
    private TableColumn<Usuario, String> columna7;
    @FXML
    private TableColumn<Usuario, String> columna8;
    @FXML
    private TableColumn<Usuario, String> columna9;
    @FXML
    private TableColumn<Usuario, String> columna10;
    private ObservableList<Usuario> usuarios;
    private UsuarioDAO usuarioDAO;
    private static final Logger logger = Logger.getLogger(VerUsuariosController.class);
    @FXML
    public void initialize() {
        // Inicializar el DAO y la lista observable
        usuarioDAO = new UsuarioDAO();
        // Cargar datos
        cargarDatos();
        //evitar ingreso manual para no tener problemas.
        dateFechaCreacion.getEditor().setDisable(true);
        dateFechaCreacion.setEditable(false);

        dateFechaUltimoIngreso.getEditor().setDisable(true);
        dateFechaUltimoIngreso.setEditable(false);
        dateFechaCreacion.valueProperty().addListener((observable,oldValue, newValue)->{
            buscarFechaCreacion(newValue);
        });
        dateFechaUltimoIngreso.valueProperty().addListener((observable, oldvalue, newValue) ->{
            buscarUltimoAcceso(newValue);
        });
        txtBuscardor.setDisable(true);
        ToggleGroup toggleFechas = new ToggleGroup();
        ordenarASC.setToggleGroup(toggleFechas);
        ordenarDESC.setToggleGroup(toggleFechas);
        toggleFechas.selectedToggleProperty().addListener((observable, oldvalue, newValue )->{
            if (newValue != null) {
                ToggleButton selectedToggle = (ToggleButton) newValue;
                if (selectedToggle.equals(ordenarASC)) {
                    onOrdenarFechaAsc();
                } else if (selectedToggle.equals(ordenarDESC)) {
                    onOrdenarFechaDesc();
                }
            }
        });
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleDNI.setToggleGroup(toggleGroup);
        toggleEmail.setToggleGroup(toggleGroup);
        toggleNombreCompleto.setToggleGroup(toggleGroup);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), event -> {
                    String newValue = txtBuscardor.getText().trim();
                    if (!newValue.isEmpty()) {
                        List<Usuario> listaUsuarios = new ArrayList<>();
                        if (toggleEmail.isSelected()) {
                            listaUsuarios = usuarioDAO.buscarPorEmail(newValue);
                        }
                        if (toggleDNI.isSelected()) {
                            listaUsuarios = usuarioDAO.buscarPorDNI(newValue);
                        } if (toggleNombreCompleto.isSelected()) {
                            listaUsuarios = usuarioDAO.buscarNombreCompleto(newValue);
                        }
                        tablaUsuarios.getItems().setAll(listaUsuarios);
                    } else {
                        tablaUsuarios.getItems().clear();
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
                    List<Usuario> listausuarios =usuarioDAO.filtrarActivoInnactivo(1);
                    tablaUsuarios.getItems().setAll(listausuarios);
                } else if (selectedToggle.equals(toggleInactivos)) {
                    List<Usuario> listausuarios =usuarioDAO.filtrarActivoInnactivo(0);
                    tablaUsuarios.getItems().setAll(listausuarios);
                }
            } else {
                cargarDatos();
            }
        });
    }

    private void buscarUltimoAcceso(LocalDate newValue) {
        logger.info("Iniciando búsqueda de usuarios por fecha de último acceso.");

        if (newValue != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = newValue.format(formatter);
            logger.debug("Fecha seleccionada para búsqueda: " + fechaFormateada);

            List<Usuario> listaUsuarios = usuarioDAO.buscarPorUltimoAcceso(fechaFormateada);

            if (listaUsuarios.isEmpty()) {
                logger.warn("No se encontraron usuarios con último acceso en la fecha: " + fechaFormateada);
                mostrarAlerta("Sin resultados", "No se encontró ningún usuario para la fecha seleccionada", Alert.AlertType.WARNING);
                cargarDatos();
            } else {
                logger.info("Se encontraron " + listaUsuarios.size() + " usuarios con último acceso en la fecha " + fechaFormateada);
                usuarios.clear();
                usuarios.addAll(listaUsuarios);
                // Configurar el TableView con la lista observable
                tablaUsuarios.setItems(usuarios);
            }
        } else {
            logger.warn("No se seleccionó una fecha para la búsqueda de usuarios.");
        }
    }


    private void buscarFechaCreacion(LocalDate newValue) {
        logger.info("Iniciando búsqueda de presupuestos por fecha de creación.");
        if (newValue != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = newValue.format(formatter);
            logger.debug("Fecha seleccionada para búsqueda: " + fechaFormateada);

            List<Usuario> listaUsuarios = usuarioDAO.buscarPorFechaCreacion(fechaFormateada);

            if (listaUsuarios.isEmpty()) {
                logger.warn("No se encontraron usuarios para la fecha: " + fechaFormateada);
                mostrarAlerta("Sin resultados", "No se encontró ningún usuario para la fecha seleccionada", Alert.AlertType.WARNING);
                cargarDatos();
            } else {
                logger.info("Se encontraron " + listaUsuarios.size() + " usuarios para la fecha " + fechaFormateada);
                usuarios.clear();
                usuarios.addAll(listaUsuarios);
                // Configurar el TableView con la lista observable
                tablaUsuarios.setItems(usuarios);
            }
        } else {
            logger.warn("No se seleccionó una fecha para la búsqueda de usuario.");
        }
    }


    private void cargarDatos() {
        logger.debug("Intento de cargar datos por pantalla");
        usuarios = FXCollections.observableArrayList();
        // Configurar columnas
        columna1.setCellValueFactory(new PropertyValueFactory<>("idusuarios"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columna3.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        columna4.setCellValueFactory(new PropertyValueFactory<>("dni"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("email"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("rol"));
        columna7.setCellValueFactory(new PropertyValueFactory<>("activo"));
        columna8.setCellValueFactory(new PropertyValueFactory<>("ultimoAcceso"));
        columna9.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        columna10.setCellValueFactory(new PropertyValueFactory<>("fechaModificacion"));
        List<Usuario> listaUsuarios = usuarioDAO.selectAllUsuarios();
        // Limpiar la lista observable y agregar los nuevos datos
        usuarios.clear();
        usuarios.addAll(listaUsuarios);
        // Configurar el TableView con la lista observable
        tablaUsuarios.setItems(usuarios);
    }

    public void mostrarFormCrearUsuario() {
        logger.info("Intento de cargar formulario de cración");
        try {
            // Cargar el archivo FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/crearUsuario.fxml"));
            Parent root = loader.load();
            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);
            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Crear Usuario");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            // Mostrar el pop-up
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            logger.error("Error, Ha ocurrido un error al cargar el fomulario. Detalles:"+e.getMessage() + ". "+e);
        }
    }

    public void cambiarActivoUsuario() {
        logger.debug("Intento de cambiar el estado activo/inactivo de un usuario");

        // Verifica si hay un usuario seleccionado en la tabla
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado != null) {
            int idUsuario = usuarioSeleccionado.getIdusuarios(); // Obtener el ID del usuario seleccionado

            // Cambiar el estado actual de "Activo" a "Inactivo" y viceversa
            String estadoNuevoString = usuarioSeleccionado.getActivo().equals("Activo") ? "Inactivo" : "Activo";
            int nuevoEstado = estadoNuevoString.equals("Activo") ? 1 : 0;

            logger.debug("Usuario seleccionado: " + usuarioSeleccionado.getNombre() + " " + usuarioSeleccionado.getApellido() +
                    " (ID: " + idUsuario + "), estado actual: " + usuarioSeleccionado.getActivo() +
                    ", nuevo estado: " + estadoNuevoString);

            // Llamar al método del DAO para cambiar el estado en la base de datos
            UsuarioDAO.cambiarEstadoActivo(idUsuario, nuevoEstado);

            logger.info("Se ha cambiado el estado del usuario (ID: " + idUsuario + ") a: " + estadoNuevoString);

            // Actualizar el estado del usuario en la tabla
            cargarDatos();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Activación / Desactivación");
            alert.setContentText("Se ha cambiado el estado del usuario " + usuarioSeleccionado.getNombre() + " " + usuarioSeleccionado.getApellido());
            alert.showAndWait();

        } else {
            logger.warn("No se ha seleccionado ningún usuario para cambiar su estado");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Activación / Desactivación");
            alert.setContentText("Debe seleccionar un usuario en la tabla");
            alert.showAndWait();
        }
    }

    public void mostrarFormModificarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            logger.warn("No se ha seleccionado ningún usuario para modificar sus datos");
            mostrarAlerta("Error", "No se ha seleccionado ningún usuario.", Alert.AlertType.ERROR);
            return;
        }
        logger.info("Intento de cargar formulario de modificación de usuarios");
        try {
            // Cargar el archivo FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/modificarUsuario.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del archivo FXML
            ModUsuarioController controller = loader.getController();
            // Pasar el usuario seleccionado al controlador
            controller.setUsuario(usuarioSeleccionado);

            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);

            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Modificar Usuario");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

            // Mostrar el pop-up
            stage.showAndWait();

            // Recargar los datos en la tabla después de cerrar el pop-up
            cargarDatos();
        } catch (IOException e) {
            logger.error("Error, No se ha podido cargar el formulario de modificación. Detalles:"+ e.getMessage() + ". "+ e);
            mostrarAlerta("Error", "Error al cargar la ventana de modificación.", Alert.AlertType.ERROR);
        }
    }
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void onOrdenarFechaAsc() {
        List<Usuario> listaUsuarios = usuarioDAO.ordenarUsuarios("ASC");
        // Limpiar la lista observable y agregar los nuevos datos
        usuarios.clear();
        usuarios.addAll(listaUsuarios);
        // Configurar el TableView con la lista observable
        tablaUsuarios.setItems(usuarios);
    }

    public void onOrdenarFechaDesc() {
        List<Usuario> listaUsuarios = usuarioDAO.ordenarUsuarios("DESC");
        // Limpiar la lista observable y agregar los nuevos datos
        usuarios.clear();
        usuarios.addAll(listaUsuarios);
        // Configurar el TableView con la lista observable
        tablaUsuarios.setItems(usuarios);
    }
}
