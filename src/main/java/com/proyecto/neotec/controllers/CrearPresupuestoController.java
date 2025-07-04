package com.proyecto.neotec.controllers;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.PresupuestoDAO;
import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Equipos;
import com.proyecto.neotec.models.Producto;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.TextFieldSoloNumeros;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CrearPresupuestoController {

    @FXML
    private Spinner<Integer> spCantidad;
    @FXML
    private TextField txfClienteNombre;
    @FXML
    private TextField txfDispositivo;
    @FXML
    private TextField txfCodigo;
    @FXML
    private TextArea txaEquipoDescripcion;
    @FXML
    private TextArea txaObs;
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, String> columnaProducto;
    @FXML
    private TableColumn<Producto, Integer> columnaPrecioUnitario;
    @FXML
    private TableColumn<Producto, Integer> columnaCantidad;
    @FXML
    private TableColumn<Producto, Integer> columnaTotalLinea;
    @FXML
    private TextField txfCostosVariables;
    @FXML
    private TextField txfManoDeObra;
    @FXML
    private Spinner<Integer> spTiempoReparacion;
    @FXML
    private TextField txfTotalPresupuestado;
    @FXML
    private Button btnAgregarLinea;

    private ObservableList<Producto> productosUtilizados;
    private Stage stage;
    private Equipos equipo;
    private static Logger logger = Logger.getLogger(CrearClienteController.class);
    @FXML
    public void initialize() {
        logger.debug("Inicializando controlador...");

        Platform.runLater(() -> {
            Scene scene = btnAgregarLinea.getScene();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    logger.debug("ENTER presionado - Activando btnAgregarLinea");
                    btnAgregarLinea.fire();
                    event.consume();
                }
            });
        });

        productosUtilizados = FXCollections.observableArrayList();
        tablaProductos.setItems(productosUtilizados);
        logger.debug("Tabla de productos vinculada con lista observable");

        vincularTabla(); // Configura las columnas correctamente

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, 1);
        spCantidad.setValueFactory(valueFactory);
        logger.debug("Spinner de cantidad inicializado");

        SpinnerValueFactory<Integer> valueFactory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 300, 0);
        spTiempoReparacion.setValueFactory(valueFactory2);
        logger.debug("Spinner de tiempo de reparación inicializado");

        TextFieldSoloNumeros.allowOnlyNumbers(txfManoDeObra);
        TextFieldSoloNumeros.allowOnlyNumbers(txfCostosVariables);
        TextFieldSoloNumeros.allowOnlyNumbers(txfTotalPresupuestado);
        logger.debug("Restricciones de solo números aplicadas a campos de texto");

        txfManoDeObra.textProperty().addListener((observable, oldValue, newValue) -> {
            calculatTotal();
        });
        txfCostosVariables.textProperty().addListener((observable, oldValue, newValue) -> {
            calculatTotal();
        });

        logger.info("Inicialización completa");
    }

    private void vincularTabla() {
        logger.debug("Configurando columnas de la tabla de productos");
        columnaProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        columnaPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaTotalLinea.setCellValueFactory(new PropertyValueFactory<>("totalLinea"));
        logger.info("Columnas de la tabla configuradas");
    }

    public void setEquipo(Equipos equipo) {
        logger.debug("Asignando equipo con ID cliente: "+equipo.getIdcliente());
        this.equipo = equipo;

        ClienteDAO cd = new ClienteDAO();
        String nombreCliente = cd.obtenerNombre(equipo.getIdcliente());
        logger.debug("Nombre del cliente obtenido: "+ nombreCliente);

        txfClienteNombre.setText(nombreCliente);
        txfDispositivo.setText(equipo.getDispositivo());
        txaEquipoDescripcion.setText(equipo.getObservaciones());

        logger.info("Datos del equipo cargados en los campos");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void agregarLinea() {
        String codigo = txfCodigo.getText().trim();
        logger.debug("Intentando agregar un producto a la línea con código: "+codigo);

        if (codigo.isEmpty()) {
            logger.warn("Código de producto vacío al intentar agregar línea");
            MostrarAlerta.mostrarAlerta("Creación de Presupuesto", "Ingrese el código de un producto para agregarlo al presupuesto.", Alert.AlertType.WARNING);
            return;
        }

        ProductosDAO pd = new ProductosDAO();
        Producto productoLinea = pd.obtenerProductoLinea(codigo);

        if (productoLinea == null || productoLinea.getNombreProducto() == null) {
            logger.error("Producto no encontrado para el código: "+ codigo);
            MostrarAlerta.mostrarAlerta("Productos", "Producto no encontrado.", Alert.AlertType.WARNING);
            return;
        }

        Integer cantidad = spCantidad.getValue();
        logger.debug("Cantidad seleccionada: "+cantidad);

        if (cantidad == null || cantidad <= 0) {
            logger.warn("Cantidad inválida: "+ cantidad);
            MostrarAlerta.mostrarAlerta("Creación de Presupuesto", "Ingrese una cantidad válida.", Alert.AlertType.WARNING);
            return;
        }

        productoLinea.setCantidad(cantidad);
        productoLinea.setTotalLinea(cantidad * productoLinea.getPrecioUnitario());
        productosUtilizados.add(productoLinea);

        logger.info("Producto agregado: " + productoLinea.getNombreProducto() + " x " + cantidad + " = " + productoLinea.getTotalLinea());
        calculatTotal();
        txfCodigo.clear();
        txfCodigo.requestFocus();
    }

    public void calculatTotal() {
        logger.debug("Calculando total del presupuesto");

        float costosVariables = txfCostosVariables.getText().isEmpty() ? 0 : Float.parseFloat(txfCostosVariables.getText());
        float manoDeObra = txfManoDeObra.getText().isEmpty() ? 0 : Float.parseFloat(txfManoDeObra.getText());
        float totalProductos = 0;

        for (Producto p1 : productosUtilizados) {
            totalProductos += p1.getTotalLinea();
        }

        float totalGeneral = totalProductos + manoDeObra + costosVariables;
        txfTotalPresupuestado.setText(String.valueOf(((int) totalGeneral)));
        logger.info("Total actualizado: Productos = " + totalProductos + ", Mano de Obra = " + manoDeObra + ", Costos Variables = " + costosVariables + ", Total = " + totalGeneral);
    }

    public void generarPDF() {
        int respuesta = guardarPresupuesto();
        if (respuesta==-1){
            logger.error("Error al crear el presupuesto, PDF no generado ");
            MostrarAlerta.mostrarAlerta("Crear Presupuesto","Error al crear el presupuesto, PDF no generado", Alert.AlertType.WARNING);
            return;
        }

        //TODO obtener fecha y hora de la bbdd para que coincida con el pdf
        PresupuestoDAO pd = new PresupuestoDAO();
        LocalDateTime fechaYhora = pd.obtenerFechaHora(respuesta);

        DecimalFormat formatoPrecio = new DecimalFormat("#0.00");
        String destino = "";
        try {
            String fechaHora = fechaYhora.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fechaHoraFormateada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            destino = "C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + respuesta + ".pdf";

            PdfWriter writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título centrado
            Paragraph titulo = new Paragraph("PRESUPUESTO DE REPARACION")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(titulo);
            Paragraph subtitulo = new Paragraph("Los presupuestos estan sujetos a cambios dependiendo el tiempo de respuesta del cliente.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11);
            document.add(subtitulo);

            // Info del ticket y cliente
            Table infoTable = new Table(new float[]{2, 3});
            infoTable.setWidth(UnitValue.createPercentValue(100));

            // Logo
            String rutaLogo = "C:/Users/54375/Documents/Analista_de_sistemas/neotec-master/src/main/resources/img/NeotecLogo.png"; // Cambiar a tu ruta real
            Image logo = new Image(ImageDataFactory.create(rutaLogo)).scaleToFit(200, 100);
            com.itextpdf.layout.element.Cell logoCell = new com.itextpdf.layout.element.Cell().add(logo)
                    .setBorder(Border.NO_BORDER);
            infoTable.addCell(logoCell);

            ClienteDAO cd = new ClienteDAO();
            // Datos del cliente
            com.itextpdf.layout.element.Cell datosCliente = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("Fecha y hora: " + fechaHoraFormateada))
                    .add(new Paragraph("Presupuesto N°: " + respuesta))
                    .add(new Paragraph("Cliente: " + cd.obtenerNombreCompletoPorId(equipo.getIdcliente())))
                    .add(new Paragraph("DNI: " + cd.obtenerDniPorId(equipo.getIdcliente())))
                    .setBorder(Border.NO_BORDER);
            infoTable.addCell(datosCliente);

            document.add(infoTable);

            document.add(new Paragraph("\n"));

            Paragraph subtitulo2 = new Paragraph("Productos utilizados")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setBold();
            document.add(subtitulo2);

            // Tabla de productos
            float[] columnWidths = {200F, 100F, 80F, 100F};
            Table productosTable = new Table(columnWidths);
            productosTable.setWidth(UnitValue.createPercentValue(100));

            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Producto").setBold()));
            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Precio U").setBold()));
            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Cantidad").setBold()));
            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Total Línea").setBold()));

            if (!productosUtilizados.isEmpty()) {
                for (Producto p1 : productosUtilizados) {
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

            Paragraph subtitulo3 = new Paragraph("Detalles y observaciones")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setBold();
            document.add(subtitulo3);
            String observaciones = "-";
            if (! txaObs.getText().isEmpty()){
                observaciones = txaObs.getText();
            }
            Paragraph subtitulo4 = new Paragraph(observaciones)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11);
            document.add(subtitulo4);


            //calcular totales
            // verifica si el TextField está vacío y asigna un valor por defecto
            float costosVariables = txfCostosVariables.getText().isEmpty() ? 0 : Float.parseFloat(txfCostosVariables.getText());
            float manoDeObra = txfManoDeObra.getText().isEmpty() ? 0 : Float.parseFloat(txfManoDeObra.getText());
            float totalProductos = 0;
            for (Producto p1 : productosUtilizados){
                totalProductos = totalProductos+p1.getTotalLinea();
            }
            float totalGeneral = totalProductos+manoDeObra+costosVariables;
            txfTotalPresupuestado.setText(String.valueOf(totalGeneral));

            // Totales
            Paragraph totalp = new Paragraph("Total productos: $" + totalProductos)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold();
            document.add(totalp);

            // Total general
            Paragraph totalmo = new Paragraph("Total mano de obra: $" + manoDeObra)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold();
            document.add(totalmo);

            // Total general
            Paragraph totalv = new Paragraph("Total costos variables: $" + costosVariables)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold();
            document.add(totalv);

            // Total general
            Paragraph total = new Paragraph("Total: $" + totalGeneral)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(20);
            document.add(total);

            document.add(new Paragraph("\n"));

            // Mensaje final
            Paragraph despedida = new Paragraph("Gracias por elegirnos...")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic();
            document.add(despedida);

            document.close();

            logger.debug("Presupuesto Creado exitosamente. El PDF se ha generado con éxito");

        } catch (Exception e) {
            logger.error("Error al crear el PDF. Detalles:" + e.getMessage());
        }

        //Abrir el pdf
        try {
            File file = new File(destino); // "destino" es la ruta del PDF que generaste
            if (file.exists()) {
                Desktop.getDesktop().open(file);
                MostrarAlerta.mostrarAlerta("Crear Presupuesto", "El presupuesto ha sido creado", Alert.AlertType.INFORMATION);
                stage.close();
            } else {
                MostrarAlerta.mostrarAlerta("Error", "Error al abrir el archivo", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            logger.error("Error, no se ha podido abrir el PDF. Detalles:" + e.getMessage());
        }
    }
    public int guardarPresupuesto() {
        int idequipo = equipo.getId();
        float costosVariables = txfCostosVariables.getText().isEmpty() ? 0 : Float.parseFloat(txfCostosVariables.getText());
        float manoDeObra = txfManoDeObra.getText().isEmpty() ? 0 : Float.parseFloat(txfManoDeObra.getText());
        float totalProductos = 0;
        for (Producto p1 : productosUtilizados) {
            totalProductos += p1.getTotalLinea();
        }
        float totalGeneral = totalProductos + manoDeObra + costosVariables;
        String observaciones = txaObs.getText();
        PresupuestoDAO pd = new PresupuestoDAO();
        int respuesta = pd.insertPresupuesto(spTiempoReparacion.getValue(), costosVariables, manoDeObra, totalGeneral, idequipo, totalProductos, observaciones);
        logger.debug("El presupuesto con ID: "+ respuesta + " ha sido creado");
        if (respuesta != -1){
            guardarProductosPresupuestos(respuesta);
        }
        return respuesta;
    }

    private void guardarProductosPresupuestos(int idpresupuesto) {
        PresupuestoDAO pd = new PresupuestoDAO();
        logger.info("Iniciando registro de productos en presupuesto. ID Presupuesto: " + idpresupuesto);
        int insertados = 0;
        for (Producto p1 : productosUtilizados){
            int idProducto = p1.getIdProductos();
            logger.info("Producto insertado en presupuesto. ID Presupuesto: " + idpresupuesto +
                    ", ID Producto: " + idProducto + ", Cantidad: " + p1.getCantidad());
            insertados++;
            pd.insertProductoPresupuesto(idpresupuesto,p1.getIdProductos(), p1.getCantidad());
        }
        logger.debug("Registro de productos en el presupuesto: "+ idpresupuesto+ ". Finalizado");
    }
}