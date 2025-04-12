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
import java.util.Optional;

public class AgregarEquipoController {
    @FXML
    public Button btnCrear;
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
        String dispositivo = txfDispositivo.getText();
        int estado = 1;
        String observaciones = txtAreaObservaciones.getText();
        int activo = 1;

        if (duenno.isEmpty() || dispositivo.isEmpty() || observaciones.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        EquipoDAO equipoDAO = new EquipoDAO();
        int idCliente = equipoDAO.obtenerIDCliente(duenno);

        if (idCliente == 0) {
            alertaConfirmacion();
            return;
        }

        // Verificar si las imágenes están vacías o son null
        boolean hayImagenes = (img1 != null && !img1.isEmpty()) &&
                (img2 != null && !img2.isEmpty()) &&
                (img3 != null && !img3.isEmpty()) &&
                (img4 != null && !img4.isEmpty());

        if (!hayImagenes) {
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Imágenes no encontradas");
            alert.setHeaderText("No se encontraron todas las imágenes.");
            alert.setContentText("¿Desea continuar sin imágenes?");

            ButtonType btnConImagenes = new ButtonType("Agregar con imágenes");
            ButtonType btnSinImagenes = new ButtonType("Agregar sin imágenes");
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnConImagenes, btnSinImagenes, btnCancelar);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isEmpty() || result.get() == btnCancelar) {
                return; // Si cancela, no hace nada
            } else if (result.get() == btnSinImagenes) {
                // Agregar equipo sin imágenes
                Equipos equipoSinImagenes = new Equipos(idCliente, dispositivo, estado, observaciones, activo);
                if (equipoDAO.AgregarEquipoSinImagenes(equipoSinImagenes)) {
                    mostrarAlerta("Equipo Agregado", "Equipo agregado sin imágenes exitosamente", Alert.AlertType.INFORMATION);
                    stage.close();
                } else {
                    mostrarAlerta("Error", "No se pudo agregar el equipo", Alert.AlertType.ERROR);
                }
                return;
            } else if (result.get() == btnConImagenes) {
                agregarImagenes(); // Permitir al usuario seleccionar imágenes

                // **Recalcular si ahora hay imágenes**
                hayImagenes = (img1 != null && !img1.isEmpty()) &&
                        (img2 != null && !img2.isEmpty()) &&
                        (img3 != null && !img3.isEmpty()) &&
                        (img4 != null && !img4.isEmpty());

                if (!hayImagenes) {
                    mostrarAlerta("Error", "Debe seleccionar exactamente 4 imágenes.", Alert.AlertType.WARNING);
                    return;
                }
            }
        }

        // Si hay imágenes o el usuario eligió "Agregar con imágenes" y las cargó correctamente
        Equipos equipoConImagenes = new Equipos(idCliente, dispositivo, estado, observaciones, activo);
        equipoConImagenes.setImg1(img1);
        equipoConImagenes.setImg2(img2);
        equipoConImagenes.setImg3(img3);
        equipoConImagenes.setImg4(img4);

        if (equipoDAO.AgregarEquipoConImagenes(equipoConImagenes)) {
            mostrarAlerta("Equipo Agregado", "Equipo agregado con imágenes exitosamente", Alert.AlertType.INFORMATION);
            stage.close();
        }
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
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Imágenes (JPG, PNG, JPEG)", "*.jpg", "*.png","*.jpeg");
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
