package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Productos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class VerProductoController {
    @FXML
    private Pane productos;

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
    @FXML
    private TableColumn<Productos, String> columna9;


    @FXML
    private ObservableList<Productos> producto;
    private ProductosDAO productosDAO;
    private Stage stage;

    public Button btnEliminar;
    public Button btnMod;
    public Button btnCrearProducto;
    public Button btnSeleccionar;

    @FXML
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
        columna4.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("categoriaString"));
        columna7.setCellValueFactory(new PropertyValueFactory<>("precioCosto"));
        columna8.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        columna9.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        List<Productos> listaProductos = productosDAO.selectAllProductos();

        // Limpiar la lista observable y agregar los nuevos datos
        producto.clear();
        producto.addAll(listaProductos);
        // Configurar el TableView con la lista observable
        tablaProductos.setItems(producto);
    }

    public void mostrarCrearProducto(){
        try {
            // Cargar el archivo FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/agregarProducto.fxml"));
            Parent root = loader.load();
                // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);
                // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Agregar Producto");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre
                // Mostrar el pop-up
            stage.showAndWait();
            cargarDatos();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public void mostrarModificarProducto(){
        Productos productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            mostrarAlerta("Selección Incorrecta", "Por favor, selecciona un producto para modificar", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            // Cargar el archivo FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/modificarProducto.fxml"));
            Parent root = loader.load();

            // Obtener el controlador
            ModProductoController controller = loader.getController();
            // Pasar el producto seleccionado al controlador
            controller.setProducto(productoSeleccionado);

            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);

            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Modificar Producto"); // Cambié "Agregar Producto" a "Modificar Producto"
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

            // Mostrar el pop-up
            stage.showAndWait();

            // Recargar los datos después de cerrar la ventana
            cargarDatos(); // Asegúrate de que cargarDatos() esté bien implementado
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void eliminarProducto(){
        Productos productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado==null){
            mostrarAlerta("Seleccion Incorrecta","Profavor, selecciona un usuario para eliminar", Alert.AlertType.INFORMATION);
            return;
        }
        //Si seleccionaste un usario proseguimos con el dialogo e informacion
        Alert confirmacion =new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmacion de Eliminacion");
        confirmacion.setHeaderText("¿Estas segurode eliminar el usuario?");
        confirmacion.setContentText("Esta accion no se puede revertir");
        ButtonType btnConfirmacion = new ButtonType("Eliminar");//boton de cancelar y cerrar
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        //añade a la alerta los botones
        confirmacion.getButtonTypes().setAll(btnConfirmacion,btnCancelar);
        if (confirmacion.showAndWait().orElse(btnCancelar)==btnConfirmacion){
            //eliminamos el usuario
            ProductosDAO.eliminarProductoSeleccionado(productoSeleccionado);
        }
        cargarDatos();


    }
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public List<Productos> SeleccionarProductoPresupuesto() {
        Productos selectedProduct = tablaProductos.getSelectionModel().getSelectedItem();

        // Validamos si hay un producto seleccionado
        if (selectedProduct != null) {
            System.out.println("Seleccionado: " + selectedProduct.getNombreProducto());
            System.out.println("id: " + selectedProduct.getIdProductos());
            stage.close();
            return productosDAO.obtenerProductoPresupuesto(selectedProduct.getIdProductos());
        } else {
            System.out.println("No se ha seleccionado ningún producto.");
            return Collections.emptyList();
        }
    }
    public void ocultarBotones() {
        btnEliminar.setVisible(false);
        btnMod.setVisible(false);
        btnCrearProducto.setVisible(false);
        btnSeleccionar.setVisible(true);
    }
}
