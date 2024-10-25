package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.EquipoDAO;
import com.proyecto.neotec.models.Equipos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.EventListener;
import java.util.List;

public class AgregarEquipoController {
    @FXML
    private Button btnAgregarIMG;
    private String img1;
    private String img2;
    private String img3;
    private String img4;
    @FXML
    private ComboBox<String> Activo;
    @FXML
    private TextArea txtAreaObservaciones;
    @FXML
    private TextField txfDuenno;
    @FXML
    private TextField txfDispositivo;

    @FXML
    private Button btnCancelar;

    public void cancelar(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
    private Stage stage;

    // Método para recibir el Stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void agregarEquipo() {
        String duenno = txfDuenno.getText();
        String dispositivo= txfDispositivo.getText();
        int estado= 1;
        String observaciones = txtAreaObservaciones.getText();
        int activo = 1;
        if (duenno.isEmpty()||dispositivo.isEmpty()||observaciones.isEmpty()){
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
        }
        EquipoDAO equipoDAO = new EquipoDAO();
        if (equipoDAO.obtenerIDCliente(duenno) == 0){
            alertaConfirmacion();
        }else {
            Equipos equipos = new Equipos(equipoDAO.obtenerIDCliente(duenno),dispositivo,estado,observaciones,activo);
            if (!img1.isEmpty()){
                equipos.setImg1(img1);
                equipos.setImg2(img2);
                equipos.setImg3(img3);
                equipos.setImg4(img4);
            }
            if (equipoDAO.AgregarEquipo(equipos)) {
                mostrarAlerta("Equipo Agregado", "Equipo agregado exitosamente", Alert.AlertType.INFORMATION);
                // Cerrar la ventana
                stage.close();
            } else {
                mostrarAlerta("Error", "No se pudo agregar el equipo", Alert.AlertType.ERROR);
            }
        }
//        btnAgregarIMG.setOnAction(event -> e){
//            btnAgregarIMG.setVisible(false);
//        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType alerta) {
        Alert alert = new Alert(alerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    public void agregarImagenes() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imágenes");

        // Filtrar para solo mostrar archivos de imagen
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Imágenes (JPG, PNG)", "*.jpg", "*.png");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Permitir seleccionar múltiples archivos
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());

        // Verificar que el usuario haya seleccionado exactamente 4 archivos
        if (selectedFiles != null && selectedFiles.size() == 4) {
            // Inicializar contador y recorrer archivos seleccionados
            int cont = 0;
            for (File file : selectedFiles) {
                System.out.println("Imagen seleccionada: " + file.getAbsolutePath());
                switch (cont) {
                    case 0:
                        img1 = file.getAbsolutePath();
                        break;
                    case 1:
                        img2 = file.getAbsolutePath();
                        break;
                    case 2:
                        img3 = file.getAbsolutePath();
                        break;
                    case 3:
                        img4 = file.getAbsolutePath();
                        break;
                }
                cont++; // Incrementar el contador para la siguiente imagen
            }
        } else {
            mostrarAlerta("Seleccionar imágenes", "Debe seleccionar exactamente 4 imágenes", Alert.AlertType.WARNING);
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
}
