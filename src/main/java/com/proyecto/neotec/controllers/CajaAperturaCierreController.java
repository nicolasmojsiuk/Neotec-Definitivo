package com.proyecto.neotec.controllers;


import com.proyecto.neotec.DAO.CajaDAO;
import com.proyecto.neotec.models.Caja;
import com.proyecto.neotec.models.HistorialAperturaCierre;
import com.proyecto.neotec.util.MostrarAlerta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

public class CajaAperturaCierreController {
    @FXML
    public ComboBox cbCajas;
    @FXML
    public TextField txfCajaNum;
    @FXML
    public TextField txfFyH;
    @FXML
    public TextField txfEstadoActual;
    @FXML
    public TextField txfSaldoActual;
    @FXML
    public Button btnAbrirCerrarCaja;
    @FXML
    private TableColumn<HistorialAperturaCierre, String> columna1; // Operación
    @FXML
    private TableColumn<HistorialAperturaCierre, String> columna2; // Fecha y hora
    @FXML
    private TableColumn<HistorialAperturaCierre, String> columna3; // Saldo
    @FXML
    private TableColumn<HistorialAperturaCierre, String> columna4; // DNI Responsable
    @FXML
    private TableColumn<HistorialAperturaCierre, String> columna5; // Nombre y apellido responsable

    @FXML
    private TableView<HistorialAperturaCierre> tablaHistorial;
    @FXML
    public DatePicker dpFechaHistorial;

    @FXML
    public void initialize(){
        cargarDatos();
        columna1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOperacion()));
        columna2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaHora()));
        columna3.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSaldo()));
        columna4.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDniResponsable()));
        columna5.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreYApellidoResponsable()));
    }

    public void cargarDatos(){
        CajaDAO cajaDAO = new CajaDAO();
        List<Integer> idcajas = new ArrayList<>();
        idcajas = cajaDAO.selectIdCajas();
        if (idcajas ==null){
            MostrarAlerta.mostrarAlerta("Caja","No se encontraron cajas", Alert.AlertType.WARNING);
            return;
        }
        for (int i = 0; i < idcajas.size(); i++) {
            cbCajas.getItems().add(idcajas.get(i));
        }
    }
    public void cargarDatosCajaSeleccionada(){
        CajaDAO cajaDAO = new CajaDAO();
        Caja cajaSeleccionada = cajaDAO.selectCajaPorId((Integer) cbCajas.getSelectionModel().getSelectedItem());
        if (cajaSeleccionada==null){
            MostrarAlerta.mostrarAlerta("Caja","Error en la carga de datos", Alert.AlertType.WARNING);
            return;
        }
        cajaSeleccionada.setUltimoCambio(cajaDAO.obtenerUltimoCambio(cajaSeleccionada.getIdcaja()));
        txfCajaNum.setText(String.valueOf(cajaSeleccionada.getIdcaja()));
        txfEstadoActual.setText(String.valueOf(cajaSeleccionada.getEstado()));
        txfFyH.setText(String.valueOf(cajaSeleccionada.getUltimoCambio()));
        txfSaldoActual.setText(String.valueOf(cajaSeleccionada.getSaldoActual()));

        //si caja esta abierta boton dice "cerrar", si caja esta cerrada boton dice "abrir"
        if (cajaSeleccionada.getEstado().equals("Abierta")){
            btnAbrirCerrarCaja.setDisable(false);
            btnAbrirCerrarCaja.setText("Cerrar");
        } else if (cajaSeleccionada.getEstado().equals("Cerrada")) {
            btnAbrirCerrarCaja.setDisable(false);
            btnAbrirCerrarCaja.setText("Abrir");
        }

        //metodo que carga el historial de aperturas y cierres
        cargarHistorial(cajaSeleccionada);
    }

    private void cargarHistorial(Caja cajaSeleccionada) {
        CajaDAO cajaDAO = new CajaDAO();
        //TODO: debe agregarse el filtro de fecha
        List<HistorialAperturaCierre> datosHistorial = cajaDAO.cargarHistorialDeAperturasyCierres(cajaSeleccionada.getIdcaja());

        // Convertir los datos en ObservableList
        ObservableList<HistorialAperturaCierre> historialObservable = FXCollections.observableArrayList(datosHistorial);

        // Asignar datos a la tabla
        tablaHistorial.setItems(historialObservable);
    }

    public void abrirCerrarCaja(){
        CajaDAO cajaDAO = new CajaDAO();
        Caja cajaSeleccionada = cajaDAO.selectCajaPorId((Integer) cbCajas.getSelectionModel().getSelectedItem());
        cajaSeleccionada.setUltimoCambio(cajaDAO.obtenerUltimoCambio(cajaSeleccionada.getIdcaja()));
        if (cajaSeleccionada == null){
            MostrarAlerta.mostrarAlerta("Caja","Debe seleccionar una caja", Alert.AlertType.WARNING);
            return;
        }

        //si la caja esta cerrada se procede a abrir
        if(cajaSeleccionada.getEstado().equals("Cerrada")) {
            MostrarAlerta.mostrarAlerta("ABRIR CAJA", "Verifique el dinero en caja. SALDO ACTUAL: " + "$ " + cajaSeleccionada.getSaldoActual(), Alert.AlertType.INFORMATION);
            //Confirmacion de apertura
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("ABRIR CAJA");
            alert.setHeaderText(null);
            alert.setContentText("¿Estás seguro que desea abrir la caja?");

            // Mostrar la alerta y esperar la respuesta del usuario
            Optional<ButtonType> resultado = alert.showAndWait();

            // Comprobar si el usuario presiona "Aceptar" o "Cancelar"
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                String mensaje = cajaDAO.abrirCerrarCaja(cajaSeleccionada, 1);
                MostrarAlerta.mostrarAlerta("ABRIR CAJA", mensaje, Alert.AlertType.INFORMATION);
                cargarDatosCajaSeleccionada();
            } else {
                MostrarAlerta.mostrarAlerta("ABRIR CAJA", "Se cancelo la apertura de la caja", Alert.AlertType.INFORMATION);
            }
        } else if (cajaSeleccionada.getEstado().equals("Abierta")) {
            MostrarAlerta.mostrarAlerta("CERRAR CAJA", "Verifique el dinero en caja. SALDO ACTUAL: " + "$ " + cajaSeleccionada.getSaldoActual(), Alert.AlertType.INFORMATION);
            //Confirmacion de apertura
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("CERRAR CAJA");
            alert.setHeaderText(null);
            alert.setContentText("¿Estás seguro que desea cerrar la caja?");

            // Mostrar la alerta y esperar la respuesta del usuario
            Optional<ButtonType> resultado = alert.showAndWait();

            // Comprobar si el usuario presiona "Aceptar" o "Cancelar"
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                String mensaje = cajaDAO.abrirCerrarCaja(cajaSeleccionada, 0);
                MostrarAlerta.mostrarAlerta("CERRAR CAJA", mensaje, Alert.AlertType.INFORMATION);
                cargarDatosCajaSeleccionada();
            } else {
                MostrarAlerta.mostrarAlerta("CERRAR CAJA", "Se cancelo el cierre de la caja", Alert.AlertType.INFORMATION);
            }
        }
    }
}
