package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Producto;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ModProductoController {
    private Producto productoMod;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnModificar;
    @FXML
    public ComboBox cbCategoria;
    @FXML
    private TextField txtNomProducto;
    @FXML
    private TextField txtCodigoProducto;
    @FXML
    private TextField txtMarcaProducto;
    @FXML
    private TextField txtCantidadProducto;
    @FXML
    private TextField txtPUnitarioProducto;
    @FXML
    private TextField txtPCostoProducto;
    @FXML
    private TextArea txtDescProducto;
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


    public void setProducto(Producto producto) {
        this.productoMod = producto;
        cargarDatosActuales();
    }

    private void cargarDatosActuales() {
        if (productoMod == null) {
            mostrarAlerta("Error", "No se ha seleccionado ningún usuario.", Alert.AlertType.WARNING);
            return;
        }
        txtNomProducto.setText(productoMod.getNombreProducto());
        txtCodigoProducto.setText(productoMod.getCodigoProducto());
        txtMarcaProducto.setText(productoMod.getMarca());
        txtCantidadProducto.setText(String.valueOf(productoMod.getCantidad()));
        txtPUnitarioProducto.setText(String.valueOf(productoMod.getPrecioUnitario()));
        txtPCostoProducto.setText(String.valueOf(productoMod.getPrecioCosto()));
        txtDescProducto.setText(productoMod.getDescripcion());
        cbCategoria.getSelectionModel().select(productoMod.getCategoriaInt());
    }

    @FXML
    public void guardarProducto(ActionEvent actionEvent) {
        // Obtener los datos de los campos
        String codigoP = txtCodigoProducto.getText();
        String marca = txtMarcaProducto.getText();
        String cantidad = txtCantidadProducto.getText();
        String precioC = txtPCostoProducto.getText();
        String precioU = txtPUnitarioProducto.getText();
        String desc = txtDescProducto.getText();
        String nomP = txtNomProducto.getText();
        int cat = cbCategoria.getSelectionModel().getSelectedIndex();
        if (cat<0){
            cat=0;
        }

        // Validar que todos los campos estén completos
        if (codigoP.isEmpty() || marca.isEmpty() || cantidad.isEmpty() || precioC.isEmpty() || precioU.isEmpty() || desc.isEmpty() || nomP.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.ERROR);
            return;
        }

        // Obtener el id del producto que se va a modificar
        int id = productoMod.getIdProductos();
        int cant, pU, pC;
        try {
            cant = Integer.parseInt(cantidad);
            pC = Integer.parseInt(precioC);
            pU = Integer.parseInt(precioU);

            // Crear el producto modificado
            Producto producto = new Producto(nomP, codigoP, marca, cant, pC, pU, desc);
            producto.setIdProductos(id); // Establecer el ID del producto
            producto.setCategoriaInt(cat);

            // Llamar a modificarProducto y obtener resultado
            String resultado = ProductosDAO.modificarProducto(producto);
            mostrarAlerta("Éxito", resultado, Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Por favor, ingrese valores válidos para la cantidad y precios.", Alert.AlertType.ERROR);
            return;
        }
        cargarDatosActuales();
        // Cerrar la ventana después de la modificación
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }


    @FXML
    public void cancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


}
