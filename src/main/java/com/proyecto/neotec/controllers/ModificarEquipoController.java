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
    public ComboBox<String> Activo;
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

    public void cancelar() {
        stage.close();
    }
    public void setEquipo(Equipos equipo) {
        this.equipoMod = equipo;
        cargarDatosActuales();
    }

    private void cargarDatosActuales() {
        Equipos datosEquipo = equipoDAO.obtenerEquipoPorId(equipoMod.getId());

        txfDuenno.setText(equipoDAO.obtenerDNI(datosEquipo.getIdcliente()));
        txfDispositivo.setText(datosEquipo.getDispositivo());
        txtAreaObservaciones.setText(datosEquipo.getObservaciones());
        Activo.setValue(obtenerActivoInnactivo(datosEquipo.getActivo()));

        int estadoActual = datosEquipo.getEstado(); // Obtener el estado actual
        tipoEstado.getItems().clear(); // Limpiar ComboBox

        String estadoActualDescripcion = equipoDAO.obtenerDescripcionEstadoEquipoDesdeBD(estadoActual);

        switch (estadoActual) {
            case 1: // Ingresado
                tipoEstado.getItems().addAll("Ingresado", "Revisión");
                break;
            case 5: // Autorizado para la reparación
                tipoEstado.getItems().addAll("Autorizado para la reparación", "Reparado");
                break;
            default:
                // Estados que no pueden cambiarse manualmente, solo mostrar el actual
                tipoEstado.getItems().add(estadoActualDescripcion);
                break;
        }

        // Seleccionar el estado actual por defecto
        tipoEstado.getSelectionModel().select(estadoActualDescripcion);
        // Desactivar el ComboBox si el estado no puede cambiarse
        boolean editable = (estadoActual == 1 || estadoActual== 4|| estadoActual == 5 || estadoActual == 6);
        tipoEstado.setDisable(!editable);
    }


    private String obtenerActivoInnactivo(int activo) {
        if (activo ==0){
            return "Innactivo";
        }else if (activo ==1){
            return "Activo";
        }
        return "Estado Desconocido";
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
        String activo = Activo.getValue();

        int id = equipoMod.getId();
        if (dispositivo.isEmpty() || estado.isEmpty() || observaciones.isEmpty()){
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
        }
        if (equipoDAO.obtenerIDCliente(dniDuenno) == 0){
            alertaConfirmacion();
        }else {

            Equipos equipos = new Equipos(equipoDAO.obtenerIDCliente(dniDuenno),dispositivo,equipoDAO.obtenerEstadoEquipoIdDesdeBD(estado),observaciones,id, obtenerActivoInnactivoString(activo));

            if (equipoDAO.ModificarEquipo(equipos)){
                mostrarAlerta("Modificación de equipo", "Equipo Modificado", Alert.AlertType.INFORMATION);
                stage.close(); // Cerrar el Stage
            }
        }
    }

    private int obtenerActivoInnactivoString(String activo) {
        if (Objects.equals(activo, "Innactivo")){
            return 0;
        }else if (Objects.equals(activo, "Activo")){
            return 1;
        }
        return -1;
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



}
