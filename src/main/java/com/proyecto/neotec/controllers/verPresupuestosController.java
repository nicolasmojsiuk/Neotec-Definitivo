package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.PresupuestoDAO;
import com.proyecto.neotec.models.Equipos;
import com.proyecto.neotec.models.Presupuestos;
import com.proyecto.neotec.models.Productos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class verPresupuestosController {
    @FXML
    private TableView<Presupuestos> tablaPresupuestos;

    @FXML
    private TableColumn<Equipos, Integer> columna1;
    @FXML
    private TableColumn<Equipos, String> columna2;
    @FXML
    private TableColumn<Equipos, String> columna3;
    @FXML
    private TableColumn<Equipos, String> columna4;
    @FXML
    private TableColumn<Equipos, String> columna5;
    @FXML
    private TableColumn<Equipos, String> columna6;
    @FXML
    private TableColumn<Equipos, String> columna7;
    @FXML
    private TableColumn<Equipos, String> columna8;
    @FXML
    private ObservableList<Presupuestos> presupuestos;
    @FXML
    private Button btnVerProductosUtilizados;
    @FXML
    private void initialize() {

        cargarDatos();
    }

    private void cargarDatos() {
        PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
        presupuestos = FXCollections.observableArrayList();
        columna1.setCellValueFactory(new PropertyValueFactory<>("idpresupuesto"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        columna3.setCellValueFactory(new PropertyValueFactory<>("costoReparacion"));
        columna4.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("manoDeObra"));
        columna7.setCellValueFactory(new PropertyValueFactory<>("diasEstimados"));
        columna8.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        List<Presupuestos> listaPresupuestos= presupuestoDAO.selectAllPresupuestos();
        presupuestos.clear();
        presupuestos.addAll(listaPresupuestos);
        tablaPresupuestos.setItems(presupuestos);
    }

    public void verProductosUtilizados(ActionEvent actionEvent) {
        Presupuestos presupuestoSeleccionado = tablaPresupuestos.getSelectionModel().getSelectedItem();
        if (presupuestoSeleccionado != null) {
            int idpresupuesto = presupuestoSeleccionado.getIdpresupuesto();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/verProductosPresupuestos.fxml"));
                Parent root = loader.load();
                // Obtener el controlador del archivo FXML
                verProductosPresupuestosController controller = loader.getController();
                // Pasar el presupuesto seleccionado al controlador
                controller.setIdpresupuesto(idpresupuesto);

                // Crear una nueva escena para el pop-up
                Scene scene = new Scene(root);

                // Crear un nuevo Stage (ventana) para el pop-up
                Stage stage = new Stage();
                stage.setTitle("Sacar Presupuesto");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre
                // Establecer el Stage en el controlador
                controller.setStage(stage);
                // Mostrar el pop-up
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error al cargar la ventana de modificación.", Alert.AlertType.ERROR);
            }
        }else {
            mostrarAlerta("Error", "No se ha seleccionado ningún equipo.", Alert.AlertType.ERROR);
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





