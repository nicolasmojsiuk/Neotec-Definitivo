package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.EquipoDAO;
import com.proyecto.neotec.DAO.PresupuestoDAO;
import com.proyecto.neotec.models.Equipos;
import com.proyecto.neotec.models.Presupuestos;
import com.proyecto.neotec.models.Productos;
import com.proyecto.neotec.util.MostrarAlerta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
        columna3.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        columna4.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        List<Presupuestos> listaPresupuestos= presupuestoDAO.selectAllPresupuestos();
        presupuestos.clear();
        presupuestos.addAll(listaPresupuestos);
        tablaPresupuestos.setItems(presupuestos);
    }

    public void verDetalles() {
        Presupuestos prVer = new Presupuestos();
        prVer = tablaPresupuestos.getSelectionModel().getSelectedItem();
        if (prVer == null){
            MostrarAlerta.mostrarAlerta("Presupuestos", "Debe seleccionar un presupuesto para poder ver los detalles", Alert.AlertType.WARNING);
            return;
        }
        int idpresupuesto = prVer.getIdpresupuesto();
        try {
            File file = new File("C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + idpresupuesto + ".pdf"); // "destino" es la ruta del PDF que generaste
            System.out.println("C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + idpresupuesto + ".pdf");
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                MostrarAlerta.mostrarAlerta("Presupuestos", "El archivo que contenia los detalles del presupuesto ya no existe o su nombre fue cambiado y no se pudo encontrarlo", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crearPresupuesto(){
        int idequipo = 0;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crear Presupuesto");
        dialog.setHeaderText("Ingrese el numero de equipo:");
        dialog.setContentText("Número:");

        Optional<String> resultado = dialog.showAndWait();

        if (resultado.isPresent()) {
            try {
                idequipo = Integer.parseInt(resultado.get());
            } catch (NumberFormatException e) {
                MostrarAlerta.mostrarAlerta("Crear Presupuesto", "Debe ingresar un numero de equipo valido. El numero de equipo no contiene letras ni simbolos", Alert.AlertType.WARNING);
                return;
            }
        }

        if (idequipo == 0){
            MostrarAlerta.mostrarAlerta("Crear Presupuesto", "Debe ingresar un numero de equipo valido. El numero de equipo no contiene letras ni simbolos", Alert.AlertType.WARNING);
            return;
        }

        EquipoDAO ed = new EquipoDAO();
        Equipos equipoSeleccionado = ed.obtenerEquipoPorId(idequipo);
        if (equipoSeleccionado==null){
            MostrarAlerta.mostrarAlerta("Crear Presupuesto", "No se encontro ningun equipo correspondiente al numero ingresado", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/crearPresupuestos.fxml"));
            Parent root = loader.load();
            // Obtener el controlador del archivo FXML
            CrearPresupuestoController controller = loader.getController();
            // Pasar el usuario seleccionado al controlador
            controller.setEquipo(equipoSeleccionado);
            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);
            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Crear Presupuesto");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre
            // Establecer el Stage en el controlador
            controller.setStage(stage);
            // Mostrar el pop-up
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            MostrarAlerta.mostrarAlerta("Error", "No se puede crear presupuestos en este momento.", Alert.AlertType.ERROR);
        }
        tablaPresupuestos.refresh();
    }
    public void modificarPresupuesto(){


    }

}





