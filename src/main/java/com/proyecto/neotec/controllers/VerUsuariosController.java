package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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


public class VerUsuariosController {

    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private Label lblUsuarios;

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

    @FXML
    public void initialize() {
        // Inicializar el DAO y la lista observable
        usuarioDAO = new UsuarioDAO();
        // Cargar datos
        cargarDatos();
    }

    private void cargarDatos() {
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
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre
            // Mostrar el pop-up
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cambiarActivoUsuario() {
        // Verifica si hay un usuario seleccionado en la tabla
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado != null) {
            int idUsuario = usuarioSeleccionado.getIdusuarios(); // Obtener el ID del usuario seleccionado
            // Cambiar el estado actual de "Activo" a "Inactivo" y viceversa
            String estadoNuevoString = usuarioSeleccionado.getActivo().equals("Activo") ? "Inactivo" : "Activo";
            int nuevoEstado;
            //pasarlo a int
            if(estadoNuevoString == "Activo"){
                nuevoEstado=1;
            }else {
                nuevoEstado=0;
            }

            // Llamar al método del DAO para cambiar el estado en la base de datos
            UsuarioDAO.cambiarEstadoActivo(idUsuario, nuevoEstado);
            // Actualizar el estado del usuario en la tabla
            cargarDatos();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Activacion / Desactivacion");
            alert.setContentText("Se ha cambiado el estado del usuario "+usuarioSeleccionado.getNombre()+" "+usuarioSeleccionado.getApellido());
            alert.showAndWait();
        } else {
            // Mostrar alerta o mensaje indicando que no hay un usuario seleccionado
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Activacion / Desactivacion");
            alert.setContentText("Debe seleccionar un usuario en la tabla");
            alert.showAndWait();
        }
    }

    public void mostrarFormModificarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            mostrarAlerta("Error", "No se ha seleccionado ningún usuario.", Alert.AlertType.ERROR);
            return;
        }

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
            e.printStackTrace();
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





}
