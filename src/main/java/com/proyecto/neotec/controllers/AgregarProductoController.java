package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Producto;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;


import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class AgregarProductoController {
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnCrear;
    @FXML
    public TextField txtNomProducto;
    @FXML
    public TextField txtCodigoProducto;
    @FXML
    public TextField txtMarcaProducto;
    @FXML
    public ComboBox<String> cbCategoria;
    @FXML
    public TextField txtCantidadProducto;
    @FXML
    public TextField txtPUnitarioProducto;
    @FXML
    public TextField txtPCostoProducto;
    @FXML
    public TextArea txtDescProducto;
    private static final Logger logger = Logger.getLogger(AgregarProductoController.class);
    @FXML
    public void initialize() {
        restriccionesCampos();
        cargarCategorias();

    }

    private void cargarCategorias() {
        ProductosDAO productosDAO = new ProductosDAO();
        List<String> nombresCategorias = productosDAO.selectNombresCategorias();
        for (String nombre : nombresCategorias) {
            cbCategoria.getItems().add(nombre);
        }
    }


    // Método para aplicar restricciones a los campos
    private void restriccionesCampos() {
        // Permitir solo números en el campo de cantidad
        Pattern pattern = Pattern.compile("\\d*");

        // Formateador para el campo de cantidad
        TextFormatter<String> formatterCantidad = new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        txtCantidadProducto.setTextFormatter(formatterCantidad);

        // Formateadores para los campos de Precio Unitario y Precio Costo
        TextFormatter<String> formatterPUnitario = new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        txtPUnitarioProducto.setTextFormatter(formatterPUnitario);

        TextFormatter<String> formatterPCosto = new TextFormatter<>((UnaryOperator<TextFormatter.Change>) change ->
                pattern.matcher(change.getControlNewText()).matches() ? change : null);
        txtPCostoProducto.setTextFormatter(formatterPCosto);

        // Bloquear el espacio en los campos de cantidad, precio unitario y precio costo
        EventHandler<KeyEvent> spaceFilter = event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume();  // Bloquea el espacio
            }
        };

        txtCantidadProducto.addEventFilter(KeyEvent.KEY_TYPED, spaceFilter);
        txtPUnitarioProducto.addEventFilter(KeyEvent.KEY_TYPED, spaceFilter);
        txtPCostoProducto.addEventFilter(KeyEvent.KEY_TYPED, spaceFilter);
    }



    public void crearProducto(ActionEvent actionEvent) {
        String codigoP = txtCodigoProducto.getText();
        String marca = txtMarcaProducto.getText();
        String cantidad = txtCantidadProducto.getText();
        String precioC = txtPCostoProducto.getText();
        String precioU = txtPUnitarioProducto.getText();
        String desc = txtDescProducto.getText();
        String nomP = txtNomProducto.getText();
        int cat = cbCategoria.getSelectionModel().getSelectedIndex();

        if (cat < 0){
            cat=0;
        }

        if (codigoP.isEmpty() || marca.isEmpty() || cantidad.isEmpty() || precioU.isEmpty() || precioC.isEmpty()|| desc.isEmpty()|| nomP.isEmpty()) {
            logger.warn("Error de validación: campos requeridos no completados en el formulario de creación de productos");
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
            return;
        }
        //convertir en un int
        int cant;
        int pU;
        int pC;

        try {
            cant = Integer.parseInt(cantidad);
            pC = Integer.parseInt(precioC);
            pU = Integer.parseInt(precioU);
        } catch (NumberFormatException e) {
            logger.error("Error al parsear los campos numéricos: " + e.getMessage());
            mostrarAlerta("Error", "Por favor ingresá solo números en los campos de cantidad y precios.", Alert.AlertType.ERROR);
            return;
        }
        Producto productoNuevo = new Producto(nomP, codigoP, marca, cant, pC, pU, desc);
        productoNuevo.setCategoriaInt(cat);
        try {
            ProductosDAO.agregarProducto(productoNuevo);
            logger.info("Producto agregado con éxito. Código: " + productoNuevo.getCodigoProducto());
        } catch (IllegalArgumentException e) {
            logger.error("Error al agregar producto: " + e.getMessage());
        }

        Stage stage = (Stage) btnCrear.getScene().getWindow();
        stage.close();
    }

    public void cancelar(ActionEvent action) {
        logger.debug("El usuario ha cancelado la operación");
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
