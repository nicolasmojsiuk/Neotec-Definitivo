package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.CajaDAO;
import com.proyecto.neotec.models.Caja;
import com.proyecto.neotec.util.CajaEstablecida;
import com.proyecto.neotec.util.CargarPantallas;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.SesionUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public void mostrarHome() {CargarPantallas.cargar(workspace,"/vistas/PantallaHome.fxml");}
    public void mostrarVerUsuarios() {
        CargarPantallas.cargar(workspace, "/vistas/verUsuarios.fxml");
    }
    public void mostrarVerClientes() {CargarPantallas.cargar(workspace, "/vistas/verClientes.fxml");}
    public void mostrarAperturaCierreCaja() {CargarPantallas.cargar(workspace, "/vistas/verAperturaCierre.fxml");}
    public void mostrarVerProductos() {
        CargarPantallas.cargar(workspace, "/vistas/verProductos.fxml");
    }
    public void mostrarVerEquipos() {CargarPantallas.cargar(workspace,"/vistas/verEquipos.fxml");}
    public void mostrarVerPresupuestos() {CargarPantallas.cargar(workspace,"/vistas/verPresupuestos.fxml");}
    public void mostrarCajaAperturaCierre() {CargarPantallas.cargar(workspace,"/vistas/CajaAperturaCierre.fxml");}
    public void mostrarVerMovimientos() {CargarPantallas.cargar(workspace,"/vistas/verMovimientosCaja.fxml");}

    public void mostrarNuevaVenta() {CargarPantallas.cargar(workspace, "/vistas/nuevaVenta.fxml");}

    public void mostrarHistorialVentas() {CargarPantallas.cargar(workspace,"/vistas/verHistorialVentas.fxml");}

    public void establecerCaja() {
        // Crear el cuadro de diálogo
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Caja");
        dialog.setHeaderText("Por favor, elige una caja:");

        // Crear el ComboBox con opciones
        ComboBox<Integer> comboBox = new ComboBox<>();

        // Cargar las cajas disponibles
        CajaDAO cajaDAO = new CajaDAO();
        List<Integer> idcajas = cajaDAO.selectIdCajas();

        if (idcajas == null || idcajas.isEmpty()) {
            MostrarAlerta.mostrarAlerta("Caja", "No se encontraron cajas disponibles", Alert.AlertType.WARNING);
            return;
        }

        comboBox.getItems().addAll(idcajas);
        comboBox.setValue(idcajas.get(0)); // Preseleccionar la primera opción

        // Agregar el ComboBox al contenido del diálogo
        VBox content = new VBox(10, new Label("Selecciona:"), comboBox);
        dialog.getDialogPane().setContent(content);

        // Agregar botones al cuadro de diálogo
        ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, btnCancelar);

        // Configurar resultado cuando se presiona "Aceptar"
        dialog.setResultConverter(dialogButton ->
                dialogButton == btnAceptar ? comboBox.getValue() : null
        );

        // Mostrar el diálogo y obtener el resultado
        Optional<Integer> resultado = dialog.showAndWait();

        resultado.ifPresent(idCajaSeleccionada -> {
            System.out.println("Caja seleccionada: " + idCajaSeleccionada);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Seleccionaste la caja: " + idCajaSeleccionada, ButtonType.OK);
            alert.showAndWait();

            // Crear instancia de Caja y establecerla como caja de trabajo
            Caja cajaSeleccionada = new Caja(idCajaSeleccionada);
            CajaEstablecida.setCajaEstablecida(cajaSeleccionada);
        });
    }

    public void mostrarEntregarEquipos(ActionEvent actionEvent) {
    }
}
