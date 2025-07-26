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
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.proyecto.neotec.DAO.*;
import com.proyecto.neotec.bbdd.Database;
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
import org.apache.log4j.Logger;

import java.util.Objects;
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
    private static final Logger logger= Logger.getLogger(NuevaVentaController.class);
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
            logger.warn("No se ingreso el DNI del cliente ");
            MostrarAlerta.mostrarAlerta("Ventas", "Ingrese el DNI del Cliente para proseguir con la venta.", Alert.AlertType.WARNING);
            return;
        }

        ClienteDAO clientedao = new ClienteDAO();
        logger.debug("Intento de buscar cliente.");
        String nombreCompletoCliente = clientedao.obtenerNombrePorDni(Integer.parseInt(dniClienteBuscar));

        if (nombreCompletoCliente.equals(" ")) {
            logger.warn("El DNI ingresado no corresponde a ningún cliente registrado");
            MostrarAlerta.mostrarAlerta("Ventas", "El DNI ingresado no corresponde a ningún Cliente registrado.", Alert.AlertType.WARNING);
        }

        txfNombreCliente.setText(nombreCompletoCliente);
    }

    public void cargarLinea() {
        String codigo = txfCodigo.getText().trim();

        if (codigo.isEmpty()) {
            logger.warn("Intento de ingresar un código vacío");
            MostrarAlerta.mostrarAlerta("Ventas", "Ingrese un código de producto.", Alert.AlertType.WARNING);
            return;
        }

        ProductosDAO productosDAO = new ProductosDAO();
        Producto productoLinea;
        logger.debug("Intento de obtener el producto a través del código del producto");
        try {
            productoLinea = productosDAO.obtenerProductoLinea(codigo);
        } catch (Exception e) {
            logger.error("Error al intentar obtener el producto. Detalles: "+ e.getMessage());
            return;
        }

        if (productoLinea == null) {
            logger.error("Error, el producto no fue encontrado. Limpiando campos...");
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
        logger.debug("Precio unitario:"+precio+ " Cantidad seleccionada:"+ cantidad);

        float totalLinea = precio * cantidad;
        logger.debug("Total de la línea calculado: "+ totalLinea);

        txfTotalLinea.setText(String.valueOf(totalLinea));
    }

    public void agregarLinea() {
        if (txfProducto.getText().isEmpty()) {
            logger.warn("Intento de agregar producto sin haber ingresado un código.");
            MostrarAlerta.mostrarAlerta("Venta", "Ingrese un código para agregar un producto a la venta", Alert.AlertType.WARNING);
            return;
        }

        logger.debug("Agregando nueva línea de producto a la venta...");

        String precioTexto = txfPrecio.getText();

        if (precioTexto.isEmpty()) {
            logger.warn("Intento de agregar producto sin precio ingresado.");
            MostrarAlerta.mostrarAlerta("Venta", "El precio del producto no puede estar vacío", Alert.AlertType.WARNING);
            return;
        }

        float precioUnitario;

        try {
            precioUnitario = Float.parseFloat(precioTexto);
        } catch (NumberFormatException e) {
            logger.error("Formato inválido para el precio ingresado: " + precioTexto + ". Detalles:" + e.getMessage());
            MostrarAlerta.mostrarAlerta("Venta", "El precio ingresado no es válido", Alert.AlertType.ERROR);
            return;
        }

        Producto linea = new Producto();

        linea.setCodigoProducto(txfCodigo.getText());
        linea.setNombreProducto(txfProducto.getText());
        linea.setCantidad(spCantidad.getValue());
        linea.setPrecioUnitario(precioUnitario);

        float totalLinea = linea.getCantidad() * linea.getPrecioUnitario();
        linea.setTotalLinea(totalLinea);

        logger.info("Producto agregado: Código=" + linea.getCodigoProducto() +
                ", Nombre=" + linea.getNombreProducto() +
                ", Cantidad=" + linea.getCantidad() +
                ", Precio Unitario=" + linea.getPrecioUnitario() +
                ", Total Línea=" + totalLinea);

        listaProductos.add(linea);

        // Calcular Total
        calcularTotal(totalLinea);


        limpiarCamposProducto();
        txfCodigo.requestFocus();
    }

    private void calcularTotal(float totalLinea) {
        String totalStr = txfTotal.getText();
        // Validar si el campo está vacío, nulo o solo espacios
        if (totalStr == null || totalStr.trim().isEmpty()) {
            logger.warn("El campo de total estaba vacío o nulo. Se inicializa a 0.");
            totalStr = "0";
            txfTotal.setText(totalStr);
        }

        float totalActual;
        try {
            totalActual = Float.parseFloat(totalStr.trim());
        } catch (NumberFormatException e) {
            logger.error("Error al convertir el total: '" + totalStr + "'", e);
            MostrarAlerta.mostrarAlerta("Total inválido", "El total actual no es un número válido.", Alert.AlertType.WARNING);
            txfTotal.setText("0");
            return;
        }

        float nuevoTotal = totalActual + totalLinea;
        txfTotal.setText(String.valueOf(nuevoTotal));

        logger.debug("Calculando total: totalActual=" + totalActual +
                ", totalLínea=" + totalLinea +
                ", nuevoTotal=" + nuevoTotal);
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
            float total = Float.parseFloat(txfTotal.getText());
            txfTotal.setText(String.valueOf(total - productoSeleccionado.getTotalLinea()));
            logger.info("Producto eliminado: ID=" + productoSeleccionado.getIdProductos() +
                    ", Nombre=" + productoSeleccionado.getNombreProducto() +
                    ", Precio=" + productoSeleccionado.getTotalLinea()+ ", Nuevo Total= "+ txfTotal.getText()) ;
            listaProductos.remove(productoSeleccionado);
        } else {
            logger.debug("Intento de quitar un producto de la linea sin seleccionarlo");
            MostrarAlerta.mostrarAlerta("Eliminar linea", "Debe seleccionar una línea de la tabla para eliminar.", Alert.AlertType.WARNING);
        }
    }

    public void cerrarTicket() {
        if (txfDniCliente.getText().isEmpty()) {
            logger.warn("Intento de generar venta sin cliente seleccionado. Campo DNI del cliente está vacío.");
            MostrarAlerta.mostrarAlerta("Venta", "No se puede realizar una venta si no hay un cliente seleccionado", Alert.AlertType.WARNING);
            cancelarTicket();
            return;
        }

        if (listaProductos.isEmpty()) {
            logger.warn("Intento de generar venta sin productos seleccionados. La lista de productos está vacía.");
            MostrarAlerta.mostrarAlerta("Venta", "No se puede realizar una venta si no hay productos seleccionados", Alert.AlertType.WARNING);
            cancelarTicket();
            return;
        }

        logger.debug("Confirmación de método de pago. Esperando ingreso del usuario...");
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
                logger.warn("Intento de realizar una venta sin una caja establecida o con la caja cerrada.");
                MostrarAlerta.mostrarAlerta("Venta", "Para realizar una venta en efectivo debera establecer una caja en Caja->Establecer Caja. Tambien debe verificar que la caja esté 'Abierta' ", Alert.AlertType.WARNING);
                return;
            }
            logger.debug("Verificando apertura de la caja");
            //verificar apertura
            CajaDAO cajadao = new CajaDAO();
            if (cajadao.verificarApertura(cajaestablecida.getIdcaja())){
                logger.debug("Intento de registrar movimiento");
                String respuesta = cajadao.registrarMovimientoDeCaja(cajaestablecida.getIdcaja(), 1,Float.parseFloat(txfTotal.getText()),SesionUsuario.getUsuarioLogueado().getIdusuarios(),"Venta en Efectivo");
                if (respuesta == "Error: Ocurrió un problema al registrar el movimiento."){
                    //hacer un try para capturar el error
                    logger.error("Error en la base de datos, ocurrió un problema al registrar el movimiento en la caja");
                    MostrarAlerta.mostrarAlerta("Venta","Error: Ocurrió un problema al registrar el movimiento en la caja", Alert.AlertType.WARNING);
                    cancelarTicket();
                } else if (respuesta == "Registro exitoso") {
                    //linea nueva agregada
                    logger.debug("Registro de movimiento exitoso");
                    registrarVenta();
                }
            }else{
                logger.warn("La caja establecida esta cerrada");
                MostrarAlerta.mostrarAlerta("Venta", "La caja establecida esta Cerrada", Alert.AlertType.WARNING);
                cancelarTicket();
                return;
            }
        } else if (result.isPresent() && result.get() == buttonNo) {
            // Crear el cuadro de diálogo
            logger.debug("Intento de transacción virtual. Esperando ingreso del usuario..");
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
            if (resultado.isPresent()) {
                // Se apretó Aceptar
                String observacion = resultado.get();
                logger.debug("Se ha aceptado la observación: " + observacion);
                mensaje.set(observacion);
                TransaccionesDigitalesDAO tdd = new TransaccionesDigitalesDAO();
                // Registrar la transacción con la observación ingresada
                tdd.registrarTransaccion(0, Float.parseFloat(txfTotal.getText()), 1, mensaje.get());
                registrarVenta();
                logger.debug("Se ha registrado la transacción digital");

            } else {
                // Se apretó Cancelar o se cerró el diálogo
                logger.info("Se ha Cancelado la operación");
            }
        }
}
    private void registrarVenta() {
        try {
            // 1) Obtener ID del cliente
            ClienteDAO clienteDAO = new ClienteDAO();
            int idCliente = clienteDAO.obtenerIdPorDni(Integer.parseInt(txfDniCliente.getText()));

            if (idCliente == 0) {
                logger.error("Error al crear la venta, el cliente no ha sido encontrado");
                MostrarAlerta.mostrarAlerta("Venta", "Error al crear la venta. El cliente no existe.", Alert.AlertType.WARNING);
                cancelarTicket();
                return;
            }

            // 2) Obtener ID del usuario vendedor
            int idUsuarioVendedor = SesionUsuario.getUsuarioLogueado().getIdusuarios();

            // 3) Total de la venta
            float total;
            try {
                total = Float.parseFloat(txfTotal.getText());
            } catch (NumberFormatException e) {
                logger.error("Error al parsear el total: " + txfTotal.getText(), e);
                MostrarAlerta.mostrarAlerta("Venta", "Total inválido. Verifica el monto.", Alert.AlertType.WARNING);
                cancelarTicket();
                return;
            }

            // 4) Fecha y hora de la venta
            LocalDateTime fechaHora = LocalDateTime.now();

           // String ruta = "C:/TICKETS_NEOTEC/Ticket_" + fechaHora.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

            // 5) Registrar ticket en base de datos
            TicketVentaDAO ticketDAO = new TicketVentaDAO();
            int numeroDeTicket = ticketDAO.insertTicket(idCliente, idUsuarioVendedor, total, fechaHora);

            //verificar si se creo el ticket en la bbdd antes de crear el ticket en pdf
            if (numeroDeTicket == -1) {
                logger.error("Error al registrar la venta en la base de datos");
                MostrarAlerta.mostrarAlerta("Venta", "No se pudo registrar la venta. Reintente más tarde.", Alert.AlertType.WARNING);
                cancelarTicket();
                return;
            } else {
                logger.debug("Ticket registrado correctamente. Nº: " + numeroDeTicket);
            }

            // 7) Registrar productos en la base de datos
            //versión antigua sin rollback en caso de fallo
            ProductosDAO productosDAO = new ProductosDAO();
            /*for (Producto producto : listaProductos) {
                int idProducto = productosDAO.obtenerIDconCodigoProducto(producto.getCodigoProducto());
                //modificar insertProductoTicketVenta para que no tome la conexión si no funciona la nueva implementación de ese metodo
                ticketDAO.insertProductoTicketVenta(numeroDeTicket, idProducto, producto.getCantidad());
            }*/
            logger.debug("Registrando productos de la venta...");
            boolean productosRegistrados = ticketDAO.registrarProductosEnTransaccion(numeroDeTicket, listaProductos);

            if (!productosRegistrados) {
                logger.error("No se pudieron registrar los productos. Cancelando venta.");
                MostrarAlerta.mostrarAlerta("Venta", "Ocurrió un error al registrar los productos. La venta fue cancelada.", Alert.AlertType.ERROR);
                cancelarTicket();
                return;
            }

            logger.debug("Productos registrados correctamente.");

            // 8) Generar PDF del ticket
            logger.debug("Generando PDF del ticket...");
            generarPDFTicket(
                    txfNombreCliente.getText(),
                    txfDniCliente.getText(),
                    txfTotal.getText(),
                    fechaHora,
                    numeroDeTicket
            );

            cancelarTicket();

        } catch (Exception ex) {
            logger.error("Excepción inesperada al registrar la venta: " + ex.getMessage(), ex);
            MostrarAlerta.mostrarAlerta("Venta", "Se produjo un error inesperado.", Alert.AlertType.ERROR);
            cancelarTicket();
        }
    }

    public void cancelarTicket() {
        logger.debug("Se ha limpiado el formulario de venta y reseteado los campos.");
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

    public String generarPDFTicket(String cliente, String dniCliente, String totalGeneral, LocalDateTime fechayhora, int numerodeticket) {
        DecimalFormat formatoPrecio = new DecimalFormat("#0.00");
        String destino = "";
        try {
            // Asegurar que la carpeta destino exista
            File carpeta = new File("C:/TICKETS_NEOTEC/");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

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

            // Logo con validación de existencia
            //reemplazar rutaLogo con la ruta donde se encuentra el logo en el equipo
            String rutaLogo = "C:/Users/54375/Desktop/git/Neotec-Definitivo/src/main/resources/img/NeotecLogo.png";
            File logoFile = new File(rutaLogo);
            if (logoFile.exists()) {
                Image logo = new Image(ImageDataFactory.create(rutaLogo)).scaleToFit(200, 100);
                Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
                infoTable.addCell(logoCell);
            } else {
                logger.warn("Logo no encontrado en " + rutaLogo + ", se omite el logo en el ticket.");
                // Celda vacía para mantener estructura
                infoTable.addCell(new Cell().setBorder(Border.NO_BORDER));
            }

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
            logger.debug("El PDF ha sido generado en: " + destino);
            TicketVentaDAO ticketVentaDAO = new TicketVentaDAO();
            ticketVentaDAO.establecerRutaTicketPDF(destino, numerodeticket);
        } catch (Exception e) {
            logger.error("Se ha producido un error al crear el PDF. Detalles:" + e.getMessage(), e);
        }

        // Abrir el pdf
        try {
            File file = new File(destino); // "destino" es la ruta del PDF que generaste
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                logger.error("El archivo PDF no existe o no fue encontrado");
            }
        } catch (Exception e) {
            logger.error("Error, El sistema a arrojado una excepción. Detalles: " + e.getMessage());
        }
        return destino;
    }

    public void ingresoManual() {
        logger.debug("Inicio de búsqueda manual por código de producto. Esperando ingreso del usuario...");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ingrese el codigo");
        dialog.setContentText("Codigo:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(codigo -> {
            txfCodigo.setText(codigo);
            logger.debug("Código ingresado por el usuario: " + codigo );
        });
    }
}
