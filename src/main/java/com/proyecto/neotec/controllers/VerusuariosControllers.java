package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.UsuarioDAO;
import com.proyecto.neotec.models.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;


public class VerusuariosControllers {

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
    private TableColumn<Usuario, LocalDateTime> columna8;
    @FXML
    private TableColumn<Usuario, LocalDateTime> columna9;
    @FXML
    private TableColumn<Usuario, LocalDateTime> columna10;
    private ObservableList<Usuario> usuarios;
    private UsuarioDAO usuarioDAO;

    @FXML
    public void initialize() {
        // Inicializar el DAO y la lista observable
        usuarioDAO = new UsuarioDAO();
        usuarios = FXCollections.observableArrayList();
        // Configurar columnas
        columna1.setCellValueFactory(new PropertyValueFactory<>("Número"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("Nombre"));
        columna3.setCellValueFactory(new PropertyValueFactory<>("Apellido"));
        columna4.setCellValueFactory(new PropertyValueFactory<>("DNI"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("Email"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("Rol"));
        columna7.setCellValueFactory(new PropertyValueFactory<>("Activo"));
        columna8.setCellValueFactory(new PropertyValueFactory<>("Ultimo Acceso"));
        columna9.setCellValueFactory(new PropertyValueFactory<>("Fecha Creación"));
        columna10.setCellValueFactory(new PropertyValueFactory<>("Fecha Modificacion"));

        // Cargar datos
        cargarDatos();
    }

    private void cargarDatos() {
        List<Usuario> listaUsuarios = usuarioDAO.selectAllUsuarios();
        // Limpiar la lista observable y agregar los nuevos datos
        usuarios.clear();
        usuarios.addAll(listaUsuarios);
        // Configurar el TableView con la lista observable
        tablaUsuarios.setItems(usuarios);
    }

}
