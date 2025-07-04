package com.proyecto.neotec.controllers;
import com.proyecto.neotec.DAO.GananciasDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.util.MostrarAlerta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Calendar;
public class VerGanaciasController {
    @FXML
    private RadioButton rbDia;
    @FXML
    private RadioButton rbSemana;
    @FXML
    private RadioButton rbAnio;
    @FXML
    private RadioButton rbPeriodo;
    @FXML
    private RadioButton rbClientesTienda;
    @FXML
    private RadioButton rbClientesTaller;
    @FXML
    private RadioButton rbClientesA;
    @FXML
    private TextField txfTotalVentas;
    @FXML
    private TextField txfGanancia;
    @FXML
    private TextField txfCostoProductos;
    @FXML
    private Label lblPeriodo;
    private LocalDate desde;
    private LocalDate hasta;
    private int graficoCarga = 0;
    private List<Float> listaDatos = new ArrayList<>();
    private List<Pair<String, Float>> datosGrafico = new ArrayList<>();
    @FXML
    private LineChart<String, Number> graficoGanancias;
    private static final Logger logger = Logger.getLogger(VerGanaciasController.class);
    public void cargarInformacion(){
        logger.debug("Intento de cargar información por pantalla");
        int periodo = 0;
        int tipoCliente = 0;

        if (rbDia.isSelected()) {
            periodo = 1;
        } else if (rbSemana.isSelected()) {
            periodo = 7;
        } else if (rbAnio.isSelected()) {
            periodo = 365;

        } else if (rbPeriodo.isSelected()) {
            if (desde == null || hasta == null) {
                logger.warn("No se ha seleccionado un periodo de tiempo valido.");
                MostrarAlerta.mostrarAlerta("Ganancias", "Debe seleccionar un período válido usando el botón correspondiente", Alert.AlertType.WARNING);
                return;
            }

        } else {
            logger.warn("No se ha seleccionado ningún periodo de tiempo para ver las ganancias");
            MostrarAlerta.mostrarAlerta("Ganancias", "Seleccione un periodo de tiempo para cargar la informacion.", Alert.AlertType.WARNING);
            return;
        }

        if (rbClientesTaller.isSelected()) {
            tipoCliente = 2;
        } else if (rbClientesTienda.isSelected()) {
            tipoCliente = 1;
        } else if (rbClientesA.isSelected()) {
            tipoCliente=3;
        } else {
            logger.warn("Operación invalida. Intento de cargar la información sin seleccionar un tipo de cliente. ");
            MostrarAlerta.mostrarAlerta("Ganancias", "Seleccione un tipo de cliente para cargar la informacion", Alert.AlertType.WARNING);
            return;
        }
        logger.debug("Intento de obtener datos desde la base de datos");
        //obtener datos desde la bbdd
        GananciasDAO gd = new GananciasDAO();
        listaDatos=gd.obtenerInformacion(periodo,desde,hasta,tipoCliente);
        //cargar datos a los campos
        txfGanancia.setText(String.valueOf(listaDatos.get(0)));
        txfCostoProductos.setText(String.valueOf(listaDatos.get(1)));
        txfTotalVentas.setText(String.valueOf(listaDatos.get(2)));
    }

    public void cargarGrafico() {
        logger.debug("Intento de cargar grafico por pantalla");
        // Limpiar datos previos del gráfico
        graficoGanancias.getData().clear();
        int periodo = 0;
        int tipoCliente = 0;

        if (rbDia.isSelected()) {
            periodo = 1;
        } else if (rbSemana.isSelected()) {
            periodo = 7;
        } else if (rbAnio.isSelected()) {
            periodo = 365;

        } else if (rbPeriodo.isSelected()) {
            if (desde == null || hasta == null) {
                logger.warn("No se ha seleccionado un periodo de tiempo valido para ver el grafico");
                MostrarAlerta.mostrarAlerta("Ganancias", "Debe seleccionar un período válido usando el botón correspondiente", Alert.AlertType.WARNING);
                return;
            }

        } else {
            logger.error("No se ha seleccionado ningún periodo de tiempo para cargar el grafico");
            MostrarAlerta.mostrarAlerta("Ganancias", "Seleccione un periodo de tiempo para cargar la informacion.", Alert.AlertType.WARNING);
            return;
        }

        if (rbClientesTaller.isSelected()) {
            tipoCliente = 2;
        } else if (rbClientesTienda.isSelected()) {
            tipoCliente = 1;
        } else if (rbClientesA.isSelected()) {
            tipoCliente=3;
        } else {
            logger.warn("No se ha seleccionado ningún cliente para cargar la información del grafico");
            MostrarAlerta.mostrarAlerta("Ganancias", "Seleccione un tipo de cliente para cargar la informacion", Alert.AlertType.WARNING);
            return;
        }
        GananciasDAO gd = new GananciasDAO();
        List<Pair<String, Float>> datosGrafico = gd.obtenerDatosGrafico(periodo, desde, hasta, tipoCliente);
        logger.debug("Intento de crear la serie de datos del grafico ");
        // Crear la serie de datos
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Ganancia");
        logger.debug("Intento de rellenar la serie de datos");
        // Llenar la serie con los datos obtenidos
        for (Pair<String, Float> dato : datosGrafico) {
            serie.getData().add(new XYChart.Data<>(dato.getKey(), dato.getValue()));
        }
        // Limpiar datos previos del gráfico
        graficoGanancias.getData().clear();

        // Añadir la serie al gráfico
        graficoGanancias.getData().add(serie);
    }


    public void seleccionarPeriodo() {
        logger.info("Intento de seleccionar un periodo personalizado");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar período personalizado");

        // Crear DatePickers
        DatePicker dpDesde = new DatePicker();
        DatePicker dpHasta = new DatePicker();

        // Crear un GridPane para organizar los controles
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Desde:"), 0, 0);
        grid.add(dpDesde, 1, 0);
        grid.add(new Label("Hasta:"), 0, 1);
        grid.add(dpHasta, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Mostrar el diálogo y capturar el resultado
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                LocalDate desdeSeleccionado = dpDesde.getValue();
                LocalDate hastaSeleccionado = dpHasta.getValue();

                if (desdeSeleccionado == null || hastaSeleccionado == null) {
                    logger.warn("Periodo inválido, Se deben seleccionar ambas fechas");
                    MostrarAlerta.mostrarAlerta("Período inválido", "Debe seleccionar ambas fechas.", Alert.AlertType.WARNING);
                } else if (desdeSeleccionado.isAfter(hastaSeleccionado)) {
                    logger.error("Periodo invalido, Intento de seleccionar una fecha como parámetro 'Desde' posterior a la fecha 'Hasta' ");
                    MostrarAlerta.mostrarAlerta("Período inválido", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.", Alert.AlertType.WARNING);
                } else {
                    desde = desdeSeleccionado;
                    hasta = hastaSeleccionado;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    lblPeriodo.setText("Periodo Especifico Desde: " + desde.format(formatter) + " | Hasta: " + hasta.format(formatter));
                    logger.debug("Periodo Especifico seleccionado. Desde: " + desde.format(formatter) + " | Hasta: " + hasta.format(formatter));
                }
            }
        });
    }
}