package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.EquipoDAO;
import com.proyecto.neotec.models.Equipos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ModificarEquipoController {
    @FXML
    private TextArea txtAreaObservaciones;
    @FXML
    private TextField txfDuenno;
    @FXML
    private TextField txfDispositivo;
    @FXML
    private ComboBox<String> tipoEstado;
    @FXML
    private Button btnCancelar;
    private Equipos equipoMod;

    EquipoDAO equipoDAO = new EquipoDAO();

    public void cancelar(ActionEvent actionEvent) {
    }
    public void setEquipo(Equipos equipo) {
        this.equipoMod = equipo;
        cargarDatosActuales();
    }

    private void cargarDatosActuales() {

        txfDuenno.setText(equipoDAO.obtenerDNI(equipoMod.getIdcliente()));
        //implementamos un metodo que determina el estado
        tipoEstado.setValue(obtenerEstadoString(equipoMod.getEstado()));
        txfDispositivo.setText(equipoMod.getDispositivo());
        txtAreaObservaciones.setText(equipoMod.getObservaciones());
        String activoString;
        switch (equipoMod.getActivo()) {
            case 1:
                activoString = "Activo";
                break;
            case 0:
                activoString = "Inactivo";
                break;
            default:
                activoString = "Desconocido"; // Por si acaso el valor es inesperado
                break;
        }

    }
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    private Stage stage; // Variable para el Stage

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void modificar(ActionEvent actionEvent) {
        String dniDuenno = (txfDuenno.getText());
        String dispositivo= txfDispositivo.getText();
        String estado= tipoEstado.getValue();
        String observaciones = txtAreaObservaciones.getText();

        int id = equipoMod.getId();
        if (dispositivo.isEmpty() || estado.isEmpty() || observaciones.isEmpty()){
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
        }
        if (equipoDAO.obtenerIDCliente(dniDuenno) == 0){
            alertaConfirmacion();
        }else {

            Equipos equipos = new Equipos(equipoDAO.obtenerIDCliente(dniDuenno),dispositivo,obtenerEstadoInt(estado),observaciones,id);
            if (equipoDAO.ModificarEquipo(equipos)){
                mostrarAlerta("Modificación de equipo", "Equipo Modificado", Alert.AlertType.INFORMATION);
                stage.close(); // Cerrar el Stage
            }
        }
    }

    private void alertaConfirmacion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("El dni ingresado no esta asociado a ningún cliente actualmente");
        alert.setContentText("¿Desea crear un nuevo cliente?:");

        // Añadir botones personalizados
        ButtonType buttonCrear = new ButtonType("Crear ");
        ButtonType buttonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonCrear, buttonCancelar);

        // Mostrar la alerta y esperar la respuesta
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonCrear) {
                mostrarFormCrearCliente();
            }
        });
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
            //mostrarAlerta("Creación de cliente ","Cliente Creado",Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String obtenerEstadoString(int estado) {
        switch (estado) {
            case 1:
                return "Ingresado";
            case 2:
                return "En espera de revisión";
            case 3:
                return "Revisión";
            case 4:
                return "En espera de autorización";
            case 5:
                return "Autorizado";
            case 6:
                return "Cancelado";
            case 7:
                return "Reparado";
            case 8:
                return "Pagado";
            default:
                return "Desconocido";
        }
    }
    public int obtenerEstadoInt(String estado) {
        switch (estado) {
            case "Ingresado":
                return 1;
            case "En espera de revisión":
                return 2;
            case "Revisión":
                return 3;
            case "En espera de autorización":
                return 4;
            case "Autorizado":
                return 5;
            case "Cancelado":
                return 6;
            case "Reparado":
                return 7;
            case "Pagado":
                return 8;
            default:
                return -1; // Valor por defecto para estados desconocidos
        }
    }

}
