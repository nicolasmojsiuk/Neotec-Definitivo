package com.proyecto.neotec.controllers;

import com.proyecto.neotec.util.CargarPantallas;
import com.proyecto.neotec.util.SesionUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class IndexAController {

    @FXML
    private Label lblDniSesion;
    @FXML
    private Label lblUsuarioSesion;
    @FXML
    private Pane workspace;


    public void initialize(){
        //Carga los datos del usuario logueado en la parte superior de la ventana
        lblUsuarioSesion.setText(SesionUsuario.getUsuarioLogueado().getNombre());
        lblDniSesion.setText(String.valueOf(SesionUsuario.getUsuarioLogueado().getDni()));
        mostrarHome();
    }


    public void mostrarHome() {
        CargarPantallas.cargar(workspace,"/vistas/PantallaHome.fxml");
    }


    public void mostrarVerUsuarios() {
        CargarPantallas.cargar(workspace, "/vistas/verUsuarios.fxml");
    }
    public void mostrarVerClientes() {CargarPantallas.cargar(workspace, "/vistas/verClientes.fxml");}
    public void mostrarVerProductos(){CargarPantallas.cargar(workspace, "/vistas/verProductos.fxml");}


}
