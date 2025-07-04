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
import org.apache.log4j.Logger;
public class VerMovimientosController {
    @FXML
    public ComboBox<Integer> cbCajas;
    @FXML
    public Button btnLimpiarFiltros;
    @FXML
    public Button btnRegistrarEntrada;
    @FXML
    public Button btnRegistrarSalida;
    @FXML
    public ComboBox<String>cbTipo;
    @FXML
    public DatePicker dpHasta;
    @FXML
    public DatePicker dpDesde;
    @FXML
    public ComboBox<String> cbFiltroEntradaSalida;

    private static final Logger logger = Logger.getLogger(VerMovimientosController.class);
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
        logger.info("Intento de cargar datos por pantalla");
        CajaDAO cajaDAO = new CajaDAO();
        List<Integer> idcajas = new ArrayList<>();
        idcajas = cajaDAO.selectIdCajas();
        if (idcajas ==null){
            logger.error("No se han encontrado cajas registradas en la base de datos");
            MostrarAlerta.mostrarAlerta("Caja","No se encontraron cajas", Alert.AlertType.WARNING);
            return;
        }
        for (Integer idcaja : idcajas) {
            cbCajas.getItems().add(idcaja);
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
        logger.debug("Intento de cargar el historial de movimientos");

        Integer idcaja = cbCajas.getSelectionModel().getSelectedItem() != null
                ? (int) cbCajas.getSelectionModel().getSelectedItem()
                : null;
        logger.debug("Caja seleccionada: " + (idcaja != null ? idcaja : "ninguna"));

        Integer entradaSalida = cbFiltroEntradaSalida.getSelectionModel().getSelectedIndex() >= 0
                ? cbFiltroEntradaSalida.getSelectionModel().getSelectedIndex()
                : null;
        logger.debug("Filtro Entrada/Salida: " + (entradaSalida != null ? entradaSalida : "ninguno"));

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        logger.debug("Rango de fechas: desde " + (desde != null ? desde : "sin seleccionar") + " hasta " + (hasta != null ? hasta : "sin seleccionar"));

        Integer tipo = cbTipo.getSelectionModel().getSelectedIndex() >= 0
                ? cbTipo.getSelectionModel().getSelectedIndex() + 1
                : null;
        logger.debug("Tipo de movimiento seleccionado: " + (tipo != null ? tipo : "ninguno"));

        CajaDAO cajaDAO = new CajaDAO();
        logger.debug("Consultando movimientos en base de datos...");
        List<MovimientosCaja> datosMovimientos = cajaDAO.cargarhistorialDeMovimientosConFiltros(idcaja, entradaSalida, desde, hasta, tipo);

        if (datosMovimientos.isEmpty()) {
            logger.info("No se encontraron movimientos para los filtros seleccionados");
            tablaMovimientos.setItems(FXCollections.observableArrayList());
            MostrarAlerta.mostrarAlerta("Movimientos de Caja", "No se encontraron movimientos para los filtros seleccionados.", Alert.AlertType.WARNING);
        } else {
            logger.info("Se encontraron " + datosMovimientos.size() + " movimientos");
            ObservableList<MovimientosCaja> historialObservable = FXCollections.observableArrayList(datosMovimientos);
            tablaMovimientos.setItems(historialObservable);
        }
    }


    public void mostrarRegistrarSalida() {
        logger.debug("Intento de registrar movimiento de tipo Salida-Gasto");
        List<String> tiposMovimientos;
        CajaDAO cajaDAO = new CajaDAO();
        tiposMovimientos = cajaDAO.selectTiposDeMovimiento(0);
        mostrarDialogoDeOperacion("Registrar Salida-Gasto", tiposMovimientos, 0);
    }

    public void mostrarRegistrarEntrada() {
        logger.debug("Intento de registrar movimiento de tipo Entrada-Pago");
        List<String> tiposMovimientos;
        CajaDAO cajaDAO = new CajaDAO();
        tiposMovimientos = cajaDAO.selectTiposDeMovimiento(1);
        mostrarDialogoDeOperacion("Registrar Entrada-Pago", tiposMovimientos, 1);
    }
    public void mostrarDialogoDeOperacion(String titulo, List<String> tiposDeMovimientos, int entradaSalida) {
        logger.debug("Mostrando diálogo de operación con título: " + titulo + ", entrada/salida: " + entradaSalida);
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
                logger.debug("Usuario confirmó la operación");
                float monto = -1;
                try {
                    monto = Float.parseFloat(String.valueOf(spinnerMonto.getValue()));
                    logger.debug("Monto ingresado: " + monto);
                } catch (NumberFormatException e) {
                    logger.error("Error al parsear el monto ingresado: " + spinnerMonto.getValue(), e);
                    mostrarError("El monto ingresado no es válido.");
                    return;
                }

                String opcion = comboBox.getValue();
                logger.debug("Opción seleccionada: " + opcion);

                // Validar que se haya ingresado el monto y seleccionado una opción
                if (monto == -1 || opcion == null) {
                    logger.warn("Campos incompletos: monto o tipo de movimiento no especificado");
                    mostrarError("Debe completar todos los campos.");
                } else {
                    logger.info("Campos validados correctamente, mostrando confirmación");
                    mostrarConfirmacion(monto, opcion, titulo, entradaSalida);
                }
            } else {
                logger.debug("El usuario canceló la operación");
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
        logger.debug("Mostrando confirmación de operación: " + titulo + ", monto: " + monto + ", tipo: " + opcion + ", entrada/salida: " + entradaSalida);

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmación");
        confirmAlert.setHeaderText("¿Está seguro de realizar esta operación?");
        confirmAlert.setContentText("Monto ingresado: " + monto + "\nTipo de movimiento: " + opcion);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logger.debug("Usuario confirmó la operación");

                String mensaje = "";
                CajaDAO cajaDAO = new CajaDAO();
                int idcaja = (int) cbCajas.getSelectionModel().getSelectedItem();
                logger.debug("ID de caja seleccionada: " + idcaja);

                Boolean verificacionApertura = false;
                Boolean verificacionMonto = false;

                try {
                    // Verificar apertura
                    verificacionApertura = cajaDAO.verificarApertura(idcaja);
                    logger.debug("Resultado verificación de apertura: " + verificacionApertura);

                    // Verificar monto si es salida
                    if (entradaSalida == 0) {
                        verificacionMonto = cajaDAO.verificarMonto(idcaja, monto);
                        logger.debug("Resultado verificación de monto para salida: " + verificacionMonto);
                    } else if (entradaSalida == 1) {
                        verificacionMonto = true;
                        logger.debug("Operación es de entrada, verificación de monto omitida");
                    }

                    if (verificacionApertura && verificacionMonto) {
                        mensaje = cajaDAO.registrarMovimientoDeCaja(
                                idcaja,
                                entradaSalida,
                                monto,
                                SesionUsuario.getUsuarioLogueado().getIdusuarios(),
                                opcion
                        );
                        logger.info("Movimiento registrado correctamente: " + mensaje);
                        MostrarAlerta.mostrarAlerta(titulo, mensaje, Alert.AlertType.INFORMATION);
                        cargarHistorialMovimientos();
                    } else if (!verificacionApertura) {
                        logger.warn("La caja está cerrada");
                        MostrarAlerta.mostrarAlerta(titulo, "La caja está cerrada. Debe abrirla en Caja -> Apertura/cierre para operar con ella", Alert.AlertType.WARNING);
                    } else if (!verificacionMonto) {
                        logger.warn("La caja no tiene el monto suficiente para realizar la operación");
                        MostrarAlerta.mostrarAlerta(titulo, "La caja no tiene el monto necesario para realizar esta operación.", Alert.AlertType.WARNING);
                    }
                } catch (Exception e) {
                    logger.error("Error al verificar o registrar la operación en caja", e);
                    MostrarAlerta.mostrarAlerta(titulo, "Ocurrió un error al procesar la operación. Detalles: " + e.getMessage(), Alert.AlertType.ERROR);
                }

            } else {
                logger.info("El usuario canceló la operación");
                MostrarAlerta.mostrarAlerta(titulo, "Operación cancelada", Alert.AlertType.INFORMATION);
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
