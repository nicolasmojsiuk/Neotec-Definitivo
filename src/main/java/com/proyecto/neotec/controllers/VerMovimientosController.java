package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.CajaDAO;
import com.proyecto.neotec.models.MovimientosCaja;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.SesionUsuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VerMovimientosController {
    @FXML
    public ComboBox cbCajas;
    @FXML
    public Button btnLimpiarFiltros;
    @FXML
    public Button btnRegistrarEntrada;
    @FXML
    public Button btnRegistrarSalida;
    @FXML
    public ComboBox cbTipo;
    @FXML
    public DatePicker dpHasta;
    @FXML
    public DatePicker dpDesde;
    @FXML
    public ComboBox cbFiltroEntradaSalida;


    @FXML
    private TableColumn<MovimientosCaja, String> columna1;
    @FXML
    private TableColumn<MovimientosCaja, String> columna2;
    @FXML
    private TableColumn<MovimientosCaja, String> columna3;
    @FXML
    private TableColumn<MovimientosCaja, String> columna4;
    @FXML
    private TableColumn<MovimientosCaja, String> columna5;
    @FXML
    private TableColumn<MovimientosCaja, String> columna6;
    @FXML
    private TableColumn<MovimientosCaja, String> columna7;

    @FXML
    private TableView<MovimientosCaja> tablaMovimientos;

    @FXML
    public void initialize(){
        cargarDatos();
        columna1.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdcajaMovimientos())));
        columna2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEntradaSalida()));
        columna3.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoMovimiento()));
        columna4.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getMonto())));
        columna5.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaHora()));
        columna6.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getDniResponsable())));
        columna7.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNombreResponsable())));
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
        cbFiltroEntradaSalida.getItems().add("Salida");
        cbFiltroEntradaSalida.getItems().add("Entrada");
        cbTipo.getItems().addAll(cajaDAO.selectTiposDeMovimiento());
    }
    public void habilitarCarga(){
        cbTipo.setDisable(false);
        cbFiltroEntradaSalida.setDisable(false);
        dpDesde.setDisable(false);
        dpHasta.setDisable(false);
        btnLimpiarFiltros.setDisable(false);
        btnRegistrarEntrada.setDisable(false);
        btnRegistrarSalida.setDisable(false);
        cargarHistorialMovimientos();
    }

    public void cargarHistorialMovimientos() {
        Integer idcaja = cbCajas.getSelectionModel().getSelectedItem() != null
                ? (int) cbCajas.getSelectionModel().getSelectedItem()
                : null;
        Integer entradaSalida = cbFiltroEntradaSalida.getSelectionModel().getSelectedIndex() >= 0
                ? cbFiltroEntradaSalida.getSelectionModel().getSelectedIndex()
                : null;
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        Integer tipo = cbTipo.getSelectionModel().getSelectedIndex() >= 0
                ? cbTipo.getSelectionModel().getSelectedIndex() + 1
                : null;

        CajaDAO cajaDAO = new CajaDAO();
        List<MovimientosCaja> datosMovimientos = cajaDAO.cargarhistorialDeMovimientosConFiltros(idcaja, entradaSalida, desde, hasta, tipo);

        if (datosMovimientos.isEmpty()) {
            // Mostrar mensaje de que no hay datos o limpiar la tabla
            tablaMovimientos.setItems(FXCollections.observableArrayList());

            MostrarAlerta.mostrarAlerta("Movimientos de Caja", "No se encontraron movimientos para los filtros seleccionados.", Alert.AlertType.WARNING);
        } else {
            // Convertir los datos en ObservableList y asignarlos a la tabla
            ObservableList<MovimientosCaja> historialObservable = FXCollections.observableArrayList(datosMovimientos);
            tablaMovimientos.setItems(historialObservable);
        }
    }



    public void mostrarRegistrarSalida() {
        List<String> tiposMovimientos;
        CajaDAO cajaDAO = new CajaDAO();
        tiposMovimientos = cajaDAO.selectTiposDeMovimiento(0);
        mostrarDialogoDeOperacion("Registrar Salida-Gasto", tiposMovimientos, 0);
    }

    public void mostrarRegistrarEntrada() {
        List<String> tiposMovimientos;
        CajaDAO cajaDAO = new CajaDAO();
        tiposMovimientos = cajaDAO.selectTiposDeMovimiento(1);
        mostrarDialogoDeOperacion("Registrar Entrada-Pago", tiposMovimientos, 1);
    }

    public void mostrarDialogoDeOperacion(String titulo, List<String> tiposDeMovimientos, int entradaSalida) {
        // Crear el Alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText("Complete los siguientes campos:");
        alert.setContentText(null);

        // Crear el Spinner con un ValueFactory para valores decimales
        Spinner<Double> spinnerMonto = new Spinner<>();
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100000000.0, 0.0, 0.01);
        spinnerMonto.setValueFactory(valueFactory);
        spinnerMonto.setEditable(true); // Permitir entrada manual

        // Crear el ComboBox para los tipos de movimientos
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(tiposDeMovimientos);
        comboBox.setPromptText("Seleccione el tipo de movimiento");

        // Colocar los controles en un layout
        VBox layout = new VBox(10); // Espaciado de 10 píxeles
        layout.getChildren().addAll(new Label("Monto:"), spinnerMonto, new Label("Tipo de Movimiento:"), comboBox);
        layout.setPadding(new Insets(10)); // Padding interno

        // Establecer el contenido del Alert
        alert.getDialogPane().setContent(layout);

        // Mostrar el Alert y manejar la respuesta
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                float monto =-1;
                monto = Float.parseFloat(String.valueOf(spinnerMonto.getValue()));
                String opcion = comboBox.getValue();

                // Validar que se haya ingresado el monto y seleccionado una opción
                if (monto == -1 || opcion == null) {
                    mostrarError("Debe completar todos los campos.");
                } else {
                    mostrarConfirmacion(monto, opcion, titulo, entradaSalida);
                }
            }
        });
    }

    // Método para mostrar un Alert de error
    private void mostrarError(String mensaje) {
        Alert alertError = new Alert(Alert.AlertType.ERROR);
        alertError.setTitle("Error en la operación");
        alertError.setHeaderText(null);
        alertError.setContentText(mensaje);
        alertError.showAndWait();
    }

    // Método para mostrar un Alert de confirmación final
    private void mostrarConfirmacion(float monto, String opcion, String titulo, int entradaSalida) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmación");
        confirmAlert.setHeaderText("¿Está seguro de realizar esta operación?");
        confirmAlert.setContentText("Monto ingresado: " + monto + "\nTipo de movimiento: " + opcion);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String mensaje = "";
                CajaDAO cajaDAO = new CajaDAO();
                int idcaja = (int) cbCajas.getSelectionModel().getSelectedItem();

                //todavia no se verifico
                Boolean verificacionApertura = false;
                Boolean verificacionMonto = false;


                //Verificar si la caja esta abierta y si tiene monto
                verificacionApertura = cajaDAO.verificarApertura(idcaja);
                if (entradaSalida==0){
                    verificacionMonto = cajaDAO.verificarMonto(idcaja, monto);
                }

                if (entradaSalida == 1){
                    verificacionMonto=true;
                }

                if (verificacionApertura == true && verificacionMonto == true) {
                    mensaje = cajaDAO.registrarMovimientoDeCaja(idcaja, entradaSalida, monto, SesionUsuario.getUsuarioLogueado().getIdusuarios(), opcion);
                    MostrarAlerta.mostrarAlerta(titulo, mensaje, Alert.AlertType.INFORMATION);
                    cargarHistorialMovimientos();
                } else if (verificacionApertura == false) {
                    MostrarAlerta.mostrarAlerta(titulo,"La caja esta cerrada. Debe abrirla en Caja -> Apertura/cierre para operar con ella", Alert.AlertType.WARNING);
                } else if (verificacionMonto == false && verificacionApertura == true) {
                    MostrarAlerta.mostrarAlerta(titulo,"La caja no tiene el monto necesario para realizar esta operacion.", Alert.AlertType.WARNING);
                }
            } else {
                MostrarAlerta.mostrarAlerta(titulo, "Operacion cancelada", Alert.AlertType.INFORMATION);
            }
        });
    }

    public void limpiarFiltros() {
        // Limpiar ComboBox
        cbCajas.getSelectionModel().clearSelection();
        cbTipo.getSelectionModel().clearSelection();
        cbFiltroEntradaSalida.getSelectionModel().clearSelection();

        // Limpiar DatePicker
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        tablaMovimientos.getItems().clear();
    }

}
