package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Productos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class verProductosPresupuestosController {
    @FXML
    private TableView<Productos> tablaProductos;

    @FXML
    private TableColumn<Productos, Integer> columna1;
    @FXML
    private TableColumn<Productos, String> columna2;
    @FXML
    private TableColumn<Productos, Integer> columna3;
    @FXML
    private TableColumn<Productos, Integer> columna4;
    @FXML
    private TableColumn<Productos, String> columna5;
    @FXML
    private TableColumn<Productos, String> columna6;
    @FXML
    private TableColumn<Productos, String> columna7;
    @FXML
    private TableColumn<Productos, String> columna8;
    int idpresupuesto;
    @FXML
    private ObservableList<Productos> producto;
    private ProductosDAO productosDAO;
    public void initialize() {
        // Inicializar el DAO y la lista observable
        productosDAO = new ProductosDAO();
        // Enlaza el evento al método
        // Cargar datos
        cargarDatos();
    }

    private void cargarDatos() {
        producto = FXCollections.observableArrayList();

        // Configurar columnas
        columna1.setCellValueFactory(new PropertyValueFactory<>("idProductos"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("codigoProducto"));
        columna3.setCellValueFactory(new PropertyValueFactory<>("marca"));
        columna4.setCellValueFactory(new PropertyValueFactory<>("cantidad"));  // Aquí mostrarás la cantidad utilizada
        columna5.setCellValueFactory(new PropertyValueFactory<>("precioCosto"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        columna7.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        columna8.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));

        // Obtener los productos utilizados para el presupuesto
        List<Productos> listaProductos = productosDAO.selectProductosUtilizados(idpresupuesto);

        // Limpiar la lista observable y agregar los nuevos datos
        producto.clear();
        producto.addAll(listaProductos);

        // Configurar el TableView con la lista observable
        tablaProductos.setItems(producto);
    }

    public void setIdpresupuesto(int idpresupuesto) {
        this.idpresupuesto= idpresupuesto;
        System.out.println(idpresupuesto);
        cargarDatos();
    }
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void Volver(ActionEvent actionEvent) {
        // Cerrar la ventana modal
        if (stage != null) {
            stage.close();
        }
    }

}
