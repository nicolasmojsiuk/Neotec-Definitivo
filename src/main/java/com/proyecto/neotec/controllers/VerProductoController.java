package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Producto;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Logger;
public class VerProductoController {
    @FXML
    public Button btnCategoria;
    @FXML
    public ToggleButton toggleNombre;
    @FXML
    public ToggleButton toggleCodigo;
    @FXML
    public ToggleButton toggleMarca;
    @FXML
    public TextField txtBuscardor;
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, Integer> columna1;
    @FXML
    private TableColumn<Producto, String> columna2;
    @FXML
    private TableColumn<Producto, Integer> columna3;
    @FXML
    private TableColumn<Producto, Integer> columna4;
    @FXML
    private TableColumn<Producto, String> columna5;
    @FXML
    private TableColumn<Producto, String> columna6;
    @FXML
    private TableColumn<Producto, String> columna7;
    @FXML
    private TableColumn<Producto, String> columna8;
    @FXML
    private TableColumn<Producto, String> columna9;
    @FXML
    private ObservableList<Producto> producto;
    private ProductosDAO productosDAO;
    private Stage stage;
    private static final Logger logger = Logger.getLogger(VerProductoController.class);
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
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleCodigo.setToggleGroup(toggleGroup);
        toggleMarca.setToggleGroup(toggleGroup);
        toggleNombre.setToggleGroup(toggleGroup);
        txtBuscardor.setDisable(true);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), event -> {
                    String newValue = txtBuscardor.getText().trim();
                    if (!newValue.isEmpty()) {
                        List<Producto> listaProductos = new ArrayList<>();
                        if (toggleCodigo.isSelected()){
                            listaProductos = productosDAO.buscarPorCodigoProducto(newValue);
                        }
                        if (toggleNombre.isSelected()){
                            listaProductos = productosDAO.buscarPorNombreProducto(newValue);
                        }
                        if (toggleMarca.isSelected()) {
                            listaProductos = productosDAO.buscarPorMarcaProducto(newValue);
                        }
                        tablaProductos.getItems().setAll(listaProductos);
                    } else {
                        tablaProductos.getItems().clear();
                    }
                })
        );
        timeline.setCycleCount(1);


        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                txtBuscardor.setDisable(true);
                txtBuscardor.setText("");
                cargarDatos();
            } else {
                txtBuscardor.setDisable(false);
                if (!txtBuscardor.getText().trim().isEmpty()) {
                    timeline.playFromStart();
                }
            }
        });


        txtBuscardor.textProperty().addListener((observable, oldValue, newValue) -> {
            timeline.stop();
            if (!newValue.trim().isEmpty()) {
                timeline.playFromStart();
            } else {
                cargarDatos();
            }
        });

    }
    private void cargarDatos() {
        logger.debug("Intento de cargar datos por pantalla");
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

        List<Producto> listaProductos = productosDAO.selectAllProductos();

        // Limpiar la lista observable y agregar los nuevos datos
        producto.clear();
        producto.addAll(listaProductos);
        // Configurar el TableView con la lista observable
        tablaProductos.setItems(producto);
    }

    public void mostrarCrearProducto(){
        logger.debug("Intento de mostrar el formulario de creación de productos");
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
                logger.error("Error, No se pudo cargar el formulario. Detalles: "+ e.getMessage()+ ". "+ e);
            }
        }

    public void mostrarModificarProducto(){
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            logger.warn("No se ha seleccionado ningún producto para modificar");
            mostrarAlerta("Selección Incorrecta", "Por favor, selecciona un producto para modificar", Alert.AlertType.INFORMATION);
            return;
        }
        logger.debug("Intento de cargar el formulario de modificación de productos");
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
            logger.error("Error, no se pudo cargar el formulario de modificación. Detalles:"+e.getMessage() + ". "+ e);
        }
    }

    public void eliminarProducto(){
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado==null){
            logger.warn("No se ha seleccionado ningún producto para eliminar");
            mostrarAlerta("Selección Incorrecta","Por favor, selecciona un usuario para eliminar", Alert.AlertType.INFORMATION);
            return;
        }
        logger.debug("Intento de eliminar el producto: "+ productoSeleccionado.getNombreProducto() +" con ID:" + productoSeleccionado.getIdProductos());
        //Si seleccionaste un usario proseguimos con el dialogo e informacion
        Alert confirmacion =new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmación de Eliminación");
        confirmacion.setHeaderText("¿Estas seguro de eliminar el usuario?");
        confirmacion.setContentText("Esta acción no se puede revertir");
        ButtonType btnConfirmacion = new ButtonType("Eliminar");//boton de cancelar y cerrar
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        //añade a la alerta los botones
        confirmacion.getButtonTypes().setAll(btnConfirmacion,btnCancelar);
        if (confirmacion.showAndWait().orElse(btnCancelar)==btnConfirmacion){
            //eliminamos el producto
            ProductosDAO.eliminarProductoSeleccionado(productoSeleccionado);
            logger.info("Producto eliminado: " + productoSeleccionado.getNombreProducto() + " (ID: " + productoSeleccionado.getIdProductos() + ")");
        }else {
            logger.info("El usuario ha decidido cancelar la operación.");
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
    public void filtrarPorCategoria(ActionEvent actionEvent) {
        logger.debug("Apertura del diálogo para filtrar productos por categoría");

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Estado");
        dialog.setHeaderText("Por favor, elige un estado:");

        // Crear el ComboBox con opciones de estado
        ComboBox<String> comboBox = new ComboBox<>();

        List<String> estados = new ArrayList<>();
        estados.add("Seleccionar todos los Productos");

        comboBox.getItems().addAll(productosDAO.obtenerCategorias(estados));
        comboBox.setValue(estados.get(0)); // Preseleccionar el primer estado

        VBox content = new VBox(10, new Label("Selecciona:"), comboBox);
        dialog.getDialogPane().setContent(content);

        ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, btnCancelar);

        dialog.setResultConverter(dialogButton ->
                dialogButton == btnAceptar ? comboBox.getValue() : null
        );

        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(estadoSeleccionado -> {
            logger.debug("Categoría seleccionada: " + estadoSeleccionado);

            if (estadoSeleccionado.equals("Seleccionar todos los Productos")) {
                List<Producto> selectAllProductos = productosDAO.selectAllProductos();
                tablaProductos.getItems().setAll(selectAllProductos);
                logger.info("Se han cargado todos los productos sin aplicar filtro por categoría");
            } else {
                int idCategoria = productosDAO.obtenerIDcategorias(estadoSeleccionado);
                List<Producto> equiposFiltrados = productosDAO.filtrarPorCategoria(idCategoria);
                tablaProductos.getItems().setAll(equiposFiltrados);
                logger.info("Se han filtrado productos por categoría: " + estadoSeleccionado + " (ID: " + idCategoria + ")");
            }
        });

        if (resultado.isEmpty()) {
            logger.debug("El usuario canceló la selección de categoría");
        }
    }
}