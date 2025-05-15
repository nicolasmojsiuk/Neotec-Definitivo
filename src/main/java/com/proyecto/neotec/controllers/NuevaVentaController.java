package com.proyecto.neotec.controllers;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.proyecto.neotec.DAO.*;
import com.proyecto.neotec.models.Caja;
import com.proyecto.neotec.models.Producto;
import com.proyecto.neotec.util.CajaEstablecida;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.SesionUsuario;
import com.proyecto.neotec.util.TextFieldSoloNumeros;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NuevaVentaController {
    @FXML
    private TextField txfDniCliente;
    @FXML
    private TextField txfNombreCliente;
    @FXML
    private TextField txfCodigo;
    @FXML
    private TextField txfProducto;
    @FXML
    private TextField txfPrecio;
    @FXML
    private TextField txfTotal;
    @FXML
    private Spinner<Integer> spCantidad;
    @FXML
    private TextField txfTotalLinea;
    @FXML
    private TableView<Producto> tablaVenta;
    @FXML
    private TableColumn<Producto, String> columna1; //codigo
    @FXML
    private TableColumn<Producto, String> columna2; //producto-nombre
    @FXML
    private TableColumn<Producto, Float> columna3; //precio unitario
    @FXML
    private TableColumn<Producto, Integer> columna4; //cantidad
    @FXML
    private TableColumn<Producto, Float> columna5; //total linea
    @FXML
    private Button btnAgregarLinea;
    @FXML
    private Button btnIngresoManual;
    @FXML
    private Button btnCerrarTicket;
    @FXML
    private Button btnEliminarLinea;
    @FXML
    private Button btnCancelar;



    // Lista observable para cargar las líneas de venta
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //TODO: Atajo de teclas
        Platform.runLater(() -> {
            Scene scene = btnAgregarLinea.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    btnAgregarLinea.fire();
                    event.consume(); // evita que el Enter haga otra acción como saltar de campo
                }
            });
        });
        Platform.runLater(() -> {
            Scene scene = btnCerrarTicket.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.F10) {
                    btnCerrarTicket.fire();
                    event.consume(); // evita que el Enter haga otra acción como saltar de campo
                }
            });
        });

        Platform.runLater(() -> {
            Scene scene = btnIngresoManual.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.F11) {
                    btnIngresoManual.fire();
                    event.consume(); // evita que el Enter haga otra acción como saltar de campo
                }
            });
        });

        Platform.runLater(() -> {
            Scene scene = btnEliminarLinea.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.DELETE) {
                    btnEliminarLinea.fire();
                    event.consume(); // evita que el Enter haga otra acción como saltar de campo
                }
            });
        });

        Platform.runLater(() -> {
            Scene scene = btnCancelar.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    btnCancelar.fire();
                    event.consume(); // evita que el Enter haga otra acción como saltar de campo
                }
            });
        });

        TextFieldSoloNumeros.allowOnlyNumbers(txfDniCliente);
        spCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));

        // Configurar columnas de la tabla
        columna1.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("codigoProducto"));
        columna2.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombreProducto"));
        columna3.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("precioUnitario"));
        columna4.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cantidad"));
        columna5.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalLinea"));

        // Asociar la lista observable con la tabla
        tablaVenta.setItems(listaProductos);

        // Listeners
        txfCodigo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                cargarLinea();
            }
        });

        spCantidad.valueProperty().addListener((observable, oldValue, newValue) -> {
            calcularTotalLinea();
        });
    }

    public void buscarCliente() {
        String dniClienteBuscar = txfDniCliente.getText();
        if (dniClienteBuscar.isEmpty()) {
            MostrarAlerta.mostrarAlerta("Ventas", "Ingrese el DNI del Cliente para proseguir con la venta.", Alert.AlertType.WARNING);
            return;
        }

        ClienteDAO clientedao = new ClienteDAO();
        String nombreCompletoCliente = clientedao.obtenerNombrePorDni(Integer.parseInt(dniClienteBuscar));

        if (nombreCompletoCliente.equals(" ")) {
            MostrarAlerta.mostrarAlerta("Ventas", "El DNI ingresado no corresponde a ningún Cliente registrado.", Alert.AlertType.WARNING);
        }

        txfNombreCliente.setText(nombreCompletoCliente);
    }

    public void cargarLinea() {
        String codigo = txfCodigo.getText().trim();

        if (codigo.isEmpty()) {
            MostrarAlerta.mostrarAlerta("Ventas", "Ingrese un código de producto.", Alert.AlertType.WARNING);
            return;
        }

        ProductosDAO productosDAO = new ProductosDAO();
        Producto productoLinea;

        try {
            productoLinea = productosDAO.obtenerProductoLinea(codigo);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (productoLinea == null) {
            MostrarAlerta.mostrarAlerta("Ventas", "Producto no encontrado.", Alert.AlertType.WARNING);
            limpiarCamposProducto();
            return;
        }

        txfProducto.setText(productoLinea.getNombreProducto());
        txfPrecio.setText(String.valueOf(productoLinea.getPrecioUnitario()));

        if (spCantidad.getValueFactory() != null) {
            spCantidad.getValueFactory().setValue(1);
        }

        calcularTotalLinea();
    }

    public void calcularTotalLinea() {
        if (txfPrecio.getText().isEmpty()) {
            txfTotalLinea.clear();
            return;
        }
        float precio = Float.parseFloat(txfPrecio.getText());
        int cantidad = spCantidad.getValue();
        float totalLinea = precio * cantidad;
        txfTotalLinea.setText(String.valueOf(totalLinea));
    }

    public void agregarLinea() {
        if (txfProducto.getText().isEmpty()) {
            MostrarAlerta.mostrarAlerta("Venta", "Ingrese un código para agregar un producto a la venta", Alert.AlertType.WARNING);
            return;
        }

        Producto linea = new Producto();
        linea.setCodigoProducto(txfCodigo.getText());
        linea.setNombreProducto(txfProducto.getText());
        linea.setCantidad(spCantidad.getValue());
        linea.setPrecioUnitario(Float.parseFloat(txfPrecio.getText()));

        float totalLinea = linea.getCantidad() * linea.getPrecioUnitario();
        linea.setTotalLinea(totalLinea);

        listaProductos.add(linea);

        //calcular Total
        calcularTotal(totalLinea);

        limpiarCamposProducto();
        txfCodigo.requestFocus();
    }

    private void calcularTotal(float totalLinea) {
        float totalActual = Float.parseFloat(txfTotal.getText());
        txfTotal.setText(String.valueOf(totalActual+totalLinea));
    }


    private void limpiarCamposProducto() {
        txfCodigo.clear();
        txfProducto.clear();
        txfPrecio.clear();
        spCantidad.getValueFactory().setValue(1);
        txfTotalLinea.clear();
    }


    public void eliminarLinea() {
        Producto productoSeleccionado = tablaVenta.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            listaProductos.remove(productoSeleccionado);
        } else {
            MostrarAlerta.mostrarAlerta("Eliminar linea", "Debe seleccionar una línea de la tabla para eliminar.", Alert.AlertType.WARNING);
        }
    }

    public void cerrarTicket() {

        if (txfDniCliente.getText().isEmpty()){
            MostrarAlerta.mostrarAlerta("Venta","No se puede realizar una venta si no hay un cliente seleccionado", Alert.AlertType.WARNING);
            cancelarTicket();
            return;
        }

        if (listaProductos.isEmpty()){
            MostrarAlerta.mostrarAlerta("Venta","No se puede realizar una venta si no hay productos seleccionados", Alert.AlertType.WARNING);
            cancelarTicket();
            return;
        }


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar pago");
        alert.setHeaderText("¿El pago fue con efectivo?");
        alert.setContentText("Seleccione una opción:");

        ButtonType buttonSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(buttonSi, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == buttonSi) {
            //afectar a la caja

            //verificar que hay una caja establecida
            Caja cajaestablecida = CajaEstablecida.getCajaSeleccionada();
            if (cajaestablecida == null){
                MostrarAlerta.mostrarAlerta("Venta", "Para realizar una venta en efectivo debera establecer una caja en Caja->Establecer Caja. Tambien debe verificar que la caja esté 'Abierta' ", Alert.AlertType.WARNING);
                return;
            }

            //verificar apertura
            CajaDAO cajadao = new CajaDAO();
            if (cajadao.verificarApertura(cajaestablecida.getIdcaja())){
                String respuesta = cajadao.registrarMovimientoDeCaja(cajaestablecida.getIdcaja(), 1,Float.parseFloat(txfTotal.getText()),SesionUsuario.getUsuarioLogueado().getIdusuarios(),"Venta en Efectivo");
                if (respuesta == "Error: Ocurrió un problema al registrar el movimiento."){
                    MostrarAlerta.mostrarAlerta("Venta","Error: Ocurrió un problema al registrar el movimiento en la caja", Alert.AlertType.WARNING);
                    cancelarTicket();
                    return;
                }
            }else{
                MostrarAlerta.mostrarAlerta("Venta", "La caja establecida esta Cerrada", Alert.AlertType.WARNING);
                cancelarTicket();
                return;
            }
        } else if (result.isPresent() && result.get() == buttonNo) {
            // Crear el cuadro de diálogo
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Observación de la transacción");

            // Crear el campo de texto
            TextField textField = new TextField();
            textField.setPromptText("Escribe aquí...");

            // Agregar el campo de texto al contenido del diálogo
            VBox content = new VBox(10, new Label("Observación:"), textField);
            dialog.getDialogPane().setContent(content);

            // Agregar botones
            ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, btnCancelar);

            // Obtener el valor ingresado
            dialog.setResultConverter(button -> (button == btnAceptar) ? textField.getText() : null);

            // Variable para almacenar la observación
            AtomicReference<String> mensaje = new AtomicReference<>("-");

            // Mostrar el cuadro de diálogo y procesar la respuesta
            Optional<String> resultado = dialog.showAndWait();
            resultado.ifPresent(mensaje::set);

            // Registrar la transacción con la observación ingresada
            TransaccionesDigitalesDAO tdd = new TransaccionesDigitalesDAO();
            tdd.registrarTransaccion(0, Float.parseFloat(txfTotal.getText()), 1, mensaje.get());
        }

        //TODO: registrar la venta en la bbdd
        //obtener los datos necesarios
        //1) idcliente
        int idcliente=0;
        ClienteDAO cd = new ClienteDAO();
        idcliente = cd.obtenerIdPorDni(Integer.parseInt(txfDniCliente.getText()));
        if (idcliente==0){
            MostrarAlerta.mostrarAlerta("Venta", "Error al crear la venta. Error en la base de datos", Alert.AlertType.WARNING);
            cancelarTicket();
            return;
        }

        //2) idusuario vendedor
        int idusuarioVendedor = SesionUsuario.getUsuarioLogueado().getIdusuarios();
        //3)total
        float total = Float.parseFloat(txfTotal.getText());
        //4) fechayhora
        LocalDateTime fechayhora = LocalDateTime.now();
        //5)ruta Ticket
        String ruta = "C:/TICKETS_NEOTEC/Ticket_" + fechayhora + ".pdf";



        TicketVentaDAO tvd = new TicketVentaDAO();
        int numerodeticket = tvd.insertTicket(idcliente, idusuarioVendedor,total,ruta,fechayhora);

        //verificar si se creo el ticket en la bbdd antes de crear el ticket en pdf
        if (numerodeticket == -1){
            MostrarAlerta.mostrarAlerta("Venta", "Error al registrar la venta en la base de datos", Alert.AlertType.WARNING);
            cancelarTicket();
            return;
        }

        //Todo: registrar los productos involucrados
        for (Producto p1 : listaProductos) {
            ProductosDAO pd = new ProductosDAO();
            int idproducto = pd.obtenerIDconCodigoProducto(p1.getCodigoProducto());
            tvd.insertProductoTicketVenta(numerodeticket,idproducto,p1.getCantidad());
        }

        //TODO: generar un pdf con los datos de la venta
        generarPDFTicket(txfNombreCliente.getText(), txfDniCliente.getText(), txfTotal.getText(),fechayhora, numerodeticket);
        cancelarTicket();
    }


    public void cancelarTicket() {
        listaProductos.clear();
        tablaVenta.refresh();
        txfNombreCliente.clear();
        txfTotal.clear();
        txfDniCliente.clear();
        txfProducto.clear();
        txfPrecio.clear();
        txfTotalLinea.clear();
        txfCodigo.clear();
    }

    public void generarPDFTicket(String cliente, String dniCliente, String totalGeneral, LocalDateTime fechayhora, int numerodeticket) {
        DecimalFormat formatoPrecio = new DecimalFormat("#0.00");
        String destino = "";
        try {
            String fechaHora = fechayhora.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fechaHoraFormateada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            destino = "C:/TICKETS_NEOTEC/Ticket_" + numerodeticket + ".pdf";

            PdfWriter writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título centrado
            Paragraph titulo = new Paragraph("TICKET DE VENTA")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(titulo);
            Paragraph subtitulo = new Paragraph("No válido como factura")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11);
            document.add(subtitulo);

            // Info del ticket y cliente
            Table infoTable = new Table(new float[]{2, 3});
            infoTable.setWidth(UnitValue.createPercentValue(100));

            // Logo
            String rutaLogo = "C:/Users/nicol/Documents/ProyectofinalNEOTEC/neotec/src/main/resources/img/NeotecLogo.png"; // Cambiar a tu ruta real
            Image logo = new Image(ImageDataFactory.create(rutaLogo)).scaleToFit(200, 100);
            Cell logoCell = new Cell().add(logo)
                    .setBorder(Border.NO_BORDER);
            infoTable.addCell(logoCell);

            // Datos del cliente
            Cell datosCliente = new Cell()
                    .add(new Paragraph("Fecha y hora: " + fechaHoraFormateada))
                    .add(new Paragraph("Número de ticket: " + numerodeticket))
                    .add(new Paragraph("Cliente: " + cliente))
                    .add(new Paragraph("DNI: " + dniCliente))
                    .setBorder(Border.NO_BORDER);
            infoTable.addCell(datosCliente);

            document.add(infoTable);

            document.add(new Paragraph("\n"));

            // Tabla de productos
            float[] columnWidths = {200F, 100F, 80F, 100F};
            Table productosTable = new Table(columnWidths);
            productosTable.setWidth(UnitValue.createPercentValue(100));

            productosTable.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold()));
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Precio U").setBold()));
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Total Línea").setBold()));

            if (!listaProductos.isEmpty()) {
                for (Producto p1 : listaProductos) {
                    productosTable.addCell(p1.getNombreProducto());
                    productosTable.addCell("$" + formatoPrecio.format(p1.getPrecioUnitario()));
                    productosTable.addCell(String.valueOf(p1.getCantidad()));
                    productosTable.addCell("$" + formatoPrecio.format(p1.getTotalLinea()));
                }
            } else {
                productosTable.addCell(new Cell(1, 4)
                        .add(new Paragraph("Tabla sin contenido"))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            document.add(productosTable);

            document.add(new Paragraph("\n"));

            // Total general
            Paragraph total = new Paragraph("Total: $" + totalGeneral)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold();
            document.add(total);

            document.add(new Paragraph("\n"));

            // Mensaje final
            Paragraph despedida = new Paragraph("Gracias por su compra! Vuelva pronto...")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic();
            document.add(despedida);

            document.close();

            System.out.println("✅ Ticket generado: " + destino);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Abrir el pdf
        try {
            File file = new File(destino); // "destino" es la ruta del PDF que generaste
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("Error al abrir el archivo");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ingresoManual() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ingrese el codigo");
        dialog.setContentText("Codigo:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(codigo -> {
            txfCodigo.setText(codigo);
        });
    }
}
