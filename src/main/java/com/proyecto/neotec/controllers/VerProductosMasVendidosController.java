package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Producto;
import com.proyecto.neotec.models.Categoria;
import com.proyecto.neotec.util.MostrarAlerta;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.log4j.Logger;

public class VerProductosMasVendidosController {
    private static Logger logger = Logger.getLogger(VerProductosMasVendidosController.class);
    @FXML
    private TableView<Producto> tablaRanking;
    @FXML
    private TableColumn<Producto, String> columnaProducto;
    @FXML
    private TableColumn<Producto, Integer> columnaCantidad;
    @FXML
    private RadioButton rbDia;
    @FXML
    private RadioButton rbSemana;
    @FXML
    private RadioButton rbAnio;
    @FXML
    private RadioButton rbDia1;
    @FXML
    private RadioButton rbSemana1;
    @FXML
    private RadioButton rbAnio1;
    @FXML
    private RadioButton rbIngresosBrutos1;
    @FXML
    private RadioButton rbUnidades1;
    @FXML
    private RadioButton rbIngresosBrutos;
    @FXML
    private RadioButton rbUnidades;
    @FXML
    private Spinner<Integer> spLimite;
    @FXML
    private Spinner<Integer> spLimiteCategorias;
    @FXML
    private Label lblPeriodo;
    @FXML
    private Label lblPeriodo2;
    private LocalDate desde1;
    private LocalDate hasta1;
    private LocalDate desde2;
    private LocalDate hasta2;
    @FXML
    private PieChart graficoCategorias;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar el Spinner (1 a 100, por defecto 10)
        spLimite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));
        spLimiteCategorias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 10));

        // Configurar columnas
        columnaProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("ventas"));

        // Vincular la lista a la tabla
        tablaRanking.setItems(listaProductos);
    }

    public void seleccionarPeriodo1() {
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
                    desde1 = desdeSeleccionado;
                    hasta1 = hastaSeleccionado;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    lblPeriodo.setText("Mostrando Desde: " + desde1.format(formatter) + " | Hasta: " + hasta1.format(formatter));

                }
            }
        });
    }

    public void seleccionarPeriodo2() {
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
                    desde2 = desdeSeleccionado;
                    hasta2 = hastaSeleccionado;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    lblPeriodo2.setText("Mostrando Desde: " + desde2.format(formatter) + " | Hasta: " + hasta2.format(formatter));

                }
            }
        });
    }

    @FXML
    public void cargarTabla() {
        int limite = spLimite.getValue();
        int periodo = 0;

        if(desde1 != null){
            periodo = 0;
        } else if (rbDia.isSelected()) {
            periodo = 1;
        } else if (rbSemana.isSelected()) {
            periodo = 7;
        } else if (rbAnio.isSelected()) {
            periodo = 365;
        } else {
            MostrarAlerta.mostrarAlerta("Ranking Productos", "Seleccione un periodo de tiempo para ver el ranking", Alert.AlertType.WARNING);
            return;
        }

        //criterio hace referencia a si se quiere mostrar las ventas por ingresos o por cantidad de unidades
        int criterio =0;
        if (rbIngresosBrutos1.isSelected()){
            criterio=1;
        } else if (rbUnidades1.isSelected()) {
            criterio=2;
        }
        if (criterio==0){
            MostrarAlerta.mostrarAlerta("Ranking Productos", "Seleccione un criterio (unidades/ingresos) para ver el ranking", Alert.AlertType.WARNING);
            return;
        }
        // Obtener datos desde la base de datos
        ProductosDAO pd = new ProductosDAO();
        listaProductos.setAll(pd.obtenerRankingDeVentas(limite, periodo,criterio, desde1, hasta1));

        // Si la lista está vacía, mostrar alerta
        if (listaProductos.isEmpty()) {
            MostrarAlerta.mostrarAlerta("Ranking Productos", "No se encontraron productos vendidos en este período", Alert.AlertType.INFORMATION);
        }
    }

    public void cargarGrafico() {
        int limite = spLimiteCategorias.getValue();
        int periodo = 0;

        if(desde2 != null){
            periodo = 0;
        } else if (rbDia1.isSelected()) {
            periodo = 1;
        } else if (rbSemana1.isSelected()) {
            periodo = 7;
        } else if (rbAnio1.isSelected()) {
            periodo = 365;
        } else {
            MostrarAlerta.mostrarAlerta("Ranking Categorías", "Seleccione un periodo de tiempo para ver el ranking", Alert.AlertType.WARNING);
            return;
        }

        // Criterio: 1 = Cantidad de unidades, 2 = Ingresos brutos
        int criterio = 0;
        if (rbIngresosBrutos.isSelected()) {
            criterio = 1;
        } else if (rbUnidades.isSelected()) {
            criterio = 2;
        }
        if (criterio == 0) {
            MostrarAlerta.mostrarAlerta("Ranking Categorías", "Seleccione un criterio (unidades/ingresos) para ver el ranking", Alert.AlertType.WARNING);
            return;
        }

        // Obtener datos desde la base de datos
        ProductosDAO pd = new ProductosDAO();
        listaCategorias.setAll(pd.obtenerRankingDeCategorias(limite, periodo, criterio, desde2, hasta2));

        if (listaCategorias.isEmpty()) {
            MostrarAlerta.mostrarAlerta("Ranking Categorías", "No hay datos para los parámetros actuales", Alert.AlertType.WARNING);
            return;
        }

        // Limpiar gráfico antes de actualizarlo
        graficoCategorias.getData().clear();

        // Calcular el total general para obtener los porcentajes
        double totalGeneral = listaCategorias.stream()
                .mapToDouble(Categoria::getTotal)
                .sum();

        // Agregar datos con porcentaje al gráfico
        for (Categoria categoria : listaCategorias) {
            double porcentaje = (categoria.getTotal() / totalGeneral) * 100;

            PieChart.Data data = new PieChart.Data(
                    categoria.getNombreCategoria(),
                    categoria.getTotal()
            );

            //Modificar la etiqueta para que muestre el nombre y el porcentaje
            data.nameProperty().bind(
                    Bindings.concat(
                            categoria.getNombreCategoria(),
                            " (", String.format("%.1f", porcentaje), "%)"
                    )
            );

            graficoCategorias.getData().add(data);
        }
    }


    public void cambioCriterio(){
        if (rbUnidades1.isSelected()){
            columnaCantidad.setText("Cantidad");
        }
        if (rbIngresosBrutos1.isSelected()){
            columnaCantidad.setText("Monto $");
        }
        if (rbUnidades.isSelected()){

        }
        if (rbIngresosBrutos.isSelected()){

        }

    }
}
