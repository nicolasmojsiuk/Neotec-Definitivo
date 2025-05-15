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



    public void cargarInformacion(){
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
                MostrarAlerta.mostrarAlerta("Ganancias", "Debe seleccionar un período válido usando el botón correspondiente", Alert.AlertType.WARNING);
                return;
            }

        } else {
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
            MostrarAlerta.mostrarAlerta("Ganancias", "Seleccione un tipo de cliente para cargar la informacion", Alert.AlertType.WARNING);
            return;
        }

        //obtener datos desde la bbdd
        GananciasDAO gd = new GananciasDAO();
        listaDatos=gd.obtenerInformacion(periodo,desde,hasta,tipoCliente);

        //cargar datos a los campos
        txfGanancia.setText(String.valueOf(listaDatos.get(0)));
        txfCostoProductos.setText(String.valueOf(listaDatos.get(1)));
        txfTotalVentas.setText(String.valueOf(listaDatos.get(2)));


    }

    public void cargarGrafico() {
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
                MostrarAlerta.mostrarAlerta("Ganancias", "Debe seleccionar un período válido usando el botón correspondiente", Alert.AlertType.WARNING);
                return;
            }

        } else {
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
            MostrarAlerta.mostrarAlerta("Ganancias", "Seleccione un tipo de cliente para cargar la informacion", Alert.AlertType.WARNING);
            return;
        }
        GananciasDAO gd = new GananciasDAO();
        List<Pair<String, Float>> datosGrafico = gd.obtenerDatosGrafico(periodo, desde, hasta, tipoCliente);


        // Crear la serie de datos
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Ganancia");

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
                    MostrarAlerta.mostrarAlerta("Período inválido", "Debe seleccionar ambas fechas.", Alert.AlertType.WARNING);
                } else if (desdeSeleccionado.isAfter(hastaSeleccionado)) {
                    MostrarAlerta.mostrarAlerta("Período inválido", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.", Alert.AlertType.WARNING);
                } else {
                    desde = desdeSeleccionado;
                    hasta = hastaSeleccionado;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    lblPeriodo.setText("Periodo Especifico Desde: " + desde.format(formatter) + " | Hasta: " + hasta.format(formatter));

                }
            }
        });
    }

}
