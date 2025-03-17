package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Productos;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.ArrayList;
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
    public ComboBox cbCategoria;
    @FXML
    public TextField txtCantidadProducto;
    @FXML
    public TextField txtPUnitarioProducto;
    @FXML
    public TextField txtPCostoProducto;
    @FXML
    public TextArea txtDescProducto;
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
        System.out.println("El método crearProducto ha sido llamado");
        String codigoP = txtCodigoProducto.getText();
        String marca = txtMarcaProducto.getText();
        String cantidad = txtCantidadProducto.getText();
        String precioC = txtPCostoProducto.getText();
        String precioU = txtPUnitarioProducto.getText();
        String desc = txtDescProducto.getText();
        String nomP = txtNomProducto.getText();
        int cat = cbCategoria.getSelectionModel().getSelectedIndex();

        System.out.println("indice categoia:"+cat);

        if (cat < 0){
            cat=0;
        }

        System.out.println("Código del producto: '" + codigoP + "'");
        System.out.println("Marca: '" + marca + "'");
        System.out.println("Cantidad: '" + cantidad + "'");
        System.out.println("Precio Costo: '" + precioC + "'");
        System.out.println("Precio Unitario: '" + precioU + "'");
        System.out.println("Descripción: '" + desc + "'");
        System.out.println("Nombre del Producto: '" + nomP + "'");


        if (codigoP.isEmpty() || marca.isEmpty() || cantidad.isEmpty() || precioU.isEmpty() || precioC.isEmpty()|| desc.isEmpty()|| nomP.isEmpty()) {
            mostrarAlerta("Error", "Por favor, complete todos los campos.", Alert.AlertType.WARNING);
            return;
        }
        //convertir en un int
        int cant;
        int pU;
        int pC;
        cant = Integer.parseInt(cantidad);
        pC = Integer.parseInt(precioC);
        pU = Integer.parseInt(precioU);

        Productos productoNuevo = new Productos(nomP,codigoP,marca,cant,pC,pU,desc);
        productoNuevo.setCategoriaInt(cat);
        try {
            ProductosDAO.agregarProducto(productoNuevo);
        } catch (IllegalArgumentException e) {
            // Manejo del error, tal vez mostrando un mensaje al usuario
            System.out.println("Error al agregar producto: " + e.getMessage());
        }
        Stage stage = (Stage) btnCrear.getScene().getWindow();
        stage.close();
    }

    public void cancelar(ActionEvent action) {
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
