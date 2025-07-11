package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.util.MostrarAlerta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VerMejoresClientesController {
    @FXML
    private TableView <Cliente> tablaMejoresClientes;
    @FXML
    private TableColumn<Cliente, String> columnaNombre;
    @FXML
    private TableColumn<Cliente, String> columnaApellido;
    @FXML
    private TableColumn<Cliente, Integer> columnaDni;
    @FXML
    private TableColumn<Cliente, String> columnaCelular;
    @FXML
    private TableColumn<Cliente, String> columnaEmail;
    @FXML
    private TableColumn<Cliente, Float> columnaCantidad;
    @FXML
    private RadioButton rbDia;
    @FXML
    private RadioButton rbSemana;
    @FXML
    private RadioButton rbAnio;
    @FXML
    private RadioButton rbPeriodo;
    @FXML
    private RadioButton rbCompras;
    @FXML
    private RadioButton rbMontoGastado;
    @FXML
    private RadioButton rbClientesTaller;
    @FXML
    private RadioButton rbClientesTienda;
    @FXML
    private Spinner<Integer> spLimite;
    @FXML
    private Label lblPeriodo;
    private LocalDate desde;
    private LocalDate hasta;
    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private static final Logger logger = Logger.getLogger(VerMejoresClientesController.class);
    @FXML
    public void initialize(){
        logger.info("Intento de inicializar parámetros de la clase");
        // Configurar el Spinner (1 a 100, por defecto 10)
        spLimite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10));

        // Configurar columnas
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        columnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        columnaDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        columnaCelular.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("compras"));


        // Vincular la lista a la tabla
        tablaMejoresClientes.setItems(listaClientes);
    }

    public void cambioCriterio() {
        logger.info("Intento de cambiar criterios");
        if(rbCompras.isSelected()) {
            columnaCantidad.setText("N° de Compras");
        }
        if (rbMontoGastado.isSelected()){
            columnaCantidad.setText("Monto Gastado");
        }
        if(rbClientesTaller.isSelected()){
            rbCompras.setText("N° de reparaciones");
        }
        if(rbClientesTienda.isSelected()){
            rbCompras.setText("N° de compras");
        }
    }
    public void seleccionarPeriodo() {
        logger.info("Intento de seleccionar periodo personalizado");
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
                    logger.warn("Período inválido, El periodo debe seleccionarse para ambas fechas. 'Desde|Hasta'");
                    MostrarAlerta.mostrarAlerta("Período inválido", "Debe seleccionar ambas fechas.", Alert.AlertType.WARNING);
                } else if (desdeSeleccionado.isAfter(hastaSeleccionado)) {
                    logger.error("Período inválido, La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
                    MostrarAlerta.mostrarAlerta("Período inválido", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.", Alert.AlertType.WARNING);
                } else {
                    desde = desdeSeleccionado;
                    hasta = hastaSeleccionado;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    lblPeriodo.setText("Periodo Especifico: " + desde.format(formatter) + " | Hasta: " + hasta.format(formatter));
                    logger.debug("Periodo Especifico: " + desde.format(formatter) + " | Hasta: " + hasta.format(formatter));
                }
            }
        });
    }

    public void cargarTabla() {
        logger.info("Intento de cargar tabla");
        int limite = spLimite.getValue();
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
                logger.warn("Se debe seleccionar un período válido para acceder al Ranking de mejores clientes");
                MostrarAlerta.mostrarAlerta("Ranking Clientes", "Debe seleccionar un período válido usando el botón correspondiente", Alert.AlertType.WARNING);
                return;
            }

        } else {
            logger.error("No se ha seleccionado ningún periodo de tiempo para ver el Ranking de mejores clientes");
            MostrarAlerta.mostrarAlerta("Ranking Clientes", "Seleccione un periodo de tiempo para ver el ranking", Alert.AlertType.WARNING);
            return;
        }

        if (rbClientesTaller.isSelected()) {
            tipoCliente = 1;
        } else if (rbClientesTienda.isSelected()) {
            tipoCliente = 2;
        } else {
            logger.error("No se ha seleccionado ningún tipo de clientes para ver el Ranking de mejores Clientes");
            MostrarAlerta.mostrarAlerta("Ranking Clientes", "Seleccione un tipo de cliente para ver el ranking", Alert.AlertType.WARNING);
            return;
        }
        //Criterio = 1 N de compras; =2 Monto de compras
        int criterio =0;
        if (rbCompras.isSelected()){
            criterio=1;
        } else if (rbMontoGastado.isSelected()) {
            criterio=2;
        }
        if (criterio==0){
            logger.error("No se ha seleccionado ningún críterio para ver el ranking");
            MostrarAlerta.mostrarAlerta("Ranking Clientes", "Seleccione un criterio para ver el ranking", Alert.AlertType.WARNING);
            return;
        }
        // Obtener datos desde la base de datos
        ClienteDAO cd = new ClienteDAO();
        //Mejores Clientes de la tienda Ordenados por monto gastado
        if (criterio==2 && tipoCliente==2){
            logger.debug("Intento de obtener los mejores clientes de la tienda ordenados por monto gastado");
            listaClientes.setAll(cd.mejoresCliPorMontoVentas(limite, periodo, desde, hasta));
        }
        //Mejores clientes del taller por cantidad de presupuestos pagados
        if (criterio==1 && tipoCliente==1){
            logger.debug("Intento de obtener los mejores clientes del taller ordenados por cantidad de presupuestos pagados");
            listaClientes.setAll(cd.mejoresCliPorNumTaller(limite, periodo, desde, hasta));
        }
        //Mejores Clientes del taller Ordenados por monto gastado
        if (criterio==2 && tipoCliente==1){
            logger.debug("Intento de obtener los mejores clientes del taller ordenados por monto gastado");
            listaClientes.setAll(cd.mejoresCliPorMontoTaller(limite, periodo, desde, hasta));
        }
        //Mejores clientes de la tienda por cantidad de compras realizadas
        if (criterio==1 && tipoCliente==2){
            logger.debug("Intento de obtener los mejores clientes la tienda por cantidad de compras realizadas");
            listaClientes.setAll(cd.mejoresCliPorNumVentas(limite, periodo, desde, hasta));
        }
        // Si la lista está vacía, mostrar alerta
        if (listaClientes.isEmpty()) {
            logger.error("Error, La lista está vacía. No se encontraron datos para mostrar");
            MostrarAlerta.mostrarAlerta("Ranking Clientes", "No se encontraron datos para mostrar", Alert.AlertType.INFORMATION);
        }
    }
}
