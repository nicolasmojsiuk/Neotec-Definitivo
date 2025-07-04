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
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

public class CajaAperturaCierreController {
    @FXML
    public ComboBox<Integer> cbCajas;
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
    private static final Logger logger = Logger.getLogger(CajaAperturaCierreController.class);
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

    public void cargarDatos() {
        CajaDAO cajaDAO = new CajaDAO();
        logger.debug("Iniciando proceso de carga de IDs de cajas desde la base de datos.");

        List<Integer> idcajas = cajaDAO.selectIdCajas();

        if (idcajas == null) {
            logger.error("No se encontraron registros de cajas en la base de datos.");
            MostrarAlerta.mostrarAlerta("Caja", "No se encontraron cajas", Alert.AlertType.WARNING);
            return;
        }

        for (Integer idcaja : idcajas) {
            cbCajas.getItems().add(idcaja);
        }

        logger.info("Carga de IDs de cajas completada. Total de cajas: " + idcajas.size());
    }

    public void cargarDatosCajaSeleccionada() {
        CajaDAO cajaDAO = new CajaDAO();
        Integer idCajaSeleccionada = (Integer) cbCajas.getSelectionModel().getSelectedItem();
        Caja cajaSeleccionada;
        if (idCajaSeleccionada == null) {
            logger.warn("No se ha seleccionado ninguna caja del ComboBox.");
            MostrarAlerta.mostrarAlerta("Selección requerida", "Por favor, seleccione una caja antes de continuar.", Alert.AlertType.WARNING);
            return;
        }else {
            logger.debug("Iniciando carga de datos para la caja seleccionada. ID: " + idCajaSeleccionada);
            cajaSeleccionada = cajaDAO.selectCajaPorId(idCajaSeleccionada);
        }

        if (cajaSeleccionada == null) {
            logger.error("No se pudo cargar la caja. ID no válido o no encontrada: " + idCajaSeleccionada);
            MostrarAlerta.mostrarAlerta("Caja", "Error en la carga de datos", Alert.AlertType.WARNING);
            return;
        }

        logger.debug("Caja encontrada. ID: " + cajaSeleccionada.getIdcaja());

        cajaSeleccionada.setUltimoCambio(cajaDAO.obtenerUltimoCambio(cajaSeleccionada.getIdcaja()));
        logger.debug("Último cambio de estado de la caja obtenido: " + cajaSeleccionada.getUltimoCambio());

        txfCajaNum.setText(String.valueOf(cajaSeleccionada.getIdcaja()));
        txfEstadoActual.setText(String.valueOf(cajaSeleccionada.getEstado()));
        txfFyH.setText(String.valueOf(cajaSeleccionada.getUltimoCambio()));
        txfSaldoActual.setText(String.valueOf(cajaSeleccionada.getSaldoActual()));

        logger.info("Datos cargados en la interfaz para la caja ID: " + cajaSeleccionada.getIdcaja());

        if (cajaSeleccionada.getEstado().equals("Abierta")) {
            btnAbrirCerrarCaja.setDisable(false);
            btnAbrirCerrarCaja.setText("Cerrar");
            logger.debug("Caja en estado 'Abierta'. Botón configurado para 'Cerrar'.");
        } else if (cajaSeleccionada.getEstado().equals("Cerrada")) {
            btnAbrirCerrarCaja.setDisable(false);
            btnAbrirCerrarCaja.setText("Abrir");
            logger.debug("Caja en estado 'Cerrada'. Botón configurado para 'Abrir'.");
        }

        cargarHistorial(cajaSeleccionada);
        logger.debug("Historial de cambios cargado para la caja ID: " + cajaSeleccionada.getIdcaja());
    }


    private void cargarHistorial(Caja cajaSeleccionada) {
        logger.debug("Cargando historial de Apertura/Cierre");
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
        if (cajaSeleccionada.getEstado().equals("Cerrada")) {
            logger.debug("Estado de la caja: Cerrada");
            logger.debug("Saldo actual de la caja: $" + cajaSeleccionada.getSaldoActual());

            MostrarAlerta.mostrarAlerta("ABRIR CAJA", "Verifique el dinero en caja. SALDO ACTUAL: " + "$ " + cajaSeleccionada.getSaldoActual(), Alert.AlertType.INFORMATION);

            // Confirmacion de apertura
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("ABRIR CAJA");
            alert.setHeaderText(null);
            alert.setContentText("¿Estás seguro que desea abrir la caja?");
            logger.debug("Mostrando confirmación para abrir la caja");

            // Mostrar la alerta y esperar la respuesta del usuario
            Optional<ButtonType> resultado = alert.showAndWait();
            logger.debug("Resultado de la confirmación: " + (resultado.isPresent() ? resultado.get() : "No presente"));

            // Comprobar si el usuario presiona "Aceptar" o "Cancelar"
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                logger.debug("El usuario confirmó la apertura de la caja");

                String mensaje = cajaDAO.abrirCerrarCaja(cajaSeleccionada, 1);
                logger.debug("Resultado del método abrirCerrarCaja (abrir): " + mensaje);

                MostrarAlerta.mostrarAlerta("ABRIR CAJA", mensaje, Alert.AlertType.INFORMATION);
                cargarDatosCajaSeleccionada();
                logger.debug("Datos de caja actualizados después de apertura");
            } else {
                logger.debug("El usuario canceló la apertura de la caja");
                MostrarAlerta.mostrarAlerta("ABRIR CAJA", "Se cancelo la apertura de la caja", Alert.AlertType.INFORMATION);
            }

        } else if (cajaSeleccionada.getEstado().equals("Abierta")) {
            logger.debug("Estado de la caja: Abierta");
            logger.debug("Saldo actual de la caja: $" + cajaSeleccionada.getSaldoActual());

            MostrarAlerta.mostrarAlerta("CERRAR CAJA", "Verifique el dinero en caja. SALDO ACTUAL: " + "$ " + cajaSeleccionada.getSaldoActual(), Alert.AlertType.INFORMATION);

            // Confirmacion de cierre
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("CERRAR CAJA");
            alert.setHeaderText(null);
            alert.setContentText("¿Estás seguro que desea cerrar la caja?");
            logger.debug("Mostrando confirmación para cerrar la caja");

            // Mostrar la alerta y esperar la respuesta del usuario
            Optional<ButtonType> resultado = alert.showAndWait();

            // Comprobar si el usuario presiona "Aceptar" o "Cancelar"
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                logger.debug("El usuario confirmó el cierre de la caja");

                String mensaje = cajaDAO.abrirCerrarCaja(cajaSeleccionada, 0);
                logger.debug("Resultado del método abrirCerrarCaja (cerrar): " + mensaje);

                MostrarAlerta.mostrarAlerta("CERRAR CAJA", mensaje, Alert.AlertType.INFORMATION);
                cargarDatosCajaSeleccionada();
                logger.debug("Datos de caja actualizados después de cierre");
            } else {
                logger.debug("El usuario canceló el cierre de la caja");
                MostrarAlerta.mostrarAlerta("CERRAR CAJA", "Se cancelo el cierre de la caja", Alert.AlertType.INFORMATION);
            }
        }

    }
}
