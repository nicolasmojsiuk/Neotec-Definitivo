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
import com.proyecto.neotec.DAO.EquipoDAO;
import com.proyecto.neotec.DAO.PresupuestoDAO;
import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Equipos;
import com.proyecto.neotec.models.Productos;
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
    private TableView<Productos> tablaProductos;
    @FXML
    private TableColumn<Productos, String> columnaProducto;
    @FXML
    private TableColumn<Productos, Integer> columnaPrecioUnitario;
    @FXML
    private TableColumn<Productos, Integer> columnaCantidad;
    @FXML
    private TableColumn<Productos, Integer> columnaTotalLinea;
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

    private ObservableList<Productos> productosUtilizados;
    private Stage stage;
    private Equipos equipo;



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
        
        productosUtilizados = FXCollections.observableArrayList();
        tablaProductos.setItems(productosUtilizados);
        vincularTabla(); // Configura las columnas correctamente

        // Inicializar el Spinner de cantidad con valores predeterminados
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, 1);
        spCantidad.setValueFactory(valueFactory);
        // Inicializar el Spinner de cantidad con valores predeterminados
        SpinnerValueFactory<Integer> valueFactory2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 300, 0);
        spTiempoReparacion.setValueFactory(valueFactory2);

        //Permitir solamente el ingreso de numeros a los campos de costos
        TextFieldSoloNumeros.allowOnlyNumbers(txfManoDeObra);
        TextFieldSoloNumeros.allowOnlyNumbers(txfCostosVariables);
        TextFieldSoloNumeros.allowOnlyNumbers(txfTotalPresupuestado);

        //Agregar un listener para calcular el total en todo momento
        txfManoDeObra.textProperty().addListener((observable, oldValue, newValue) -> {
            calculatTotal();
        });
        txfCostosVariables.textProperty().addListener((observable, oldValue, newValue) -> {
            calculatTotal();
        });
    }

    private void vincularTabla() {
        // Configuración de columnas
        columnaProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        columnaPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaTotalLinea.setCellValueFactory(new PropertyValueFactory<>("totalLinea"));
    }

    public void setEquipo(Equipos equipo) {
        this.equipo = equipo;
        ClienteDAO cd = new ClienteDAO();
        String nombreCliente = cd.obtenerNombre(equipo.getIdcliente());
        txfClienteNombre.setText(nombreCliente);
        txfDispositivo.setText(equipo.getDispositivo());
        txaEquipoDescripcion.setText(equipo.getObservaciones());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public void agregarLinea() {
        String codigo = txfCodigo.getText().trim();

        // Verificar si el código está vacío
        if (codigo.isEmpty()) {
            MostrarAlerta.mostrarAlerta("Creación de Presupuesto", "Ingrese el código de un producto para agregarlo al presupuesto.", Alert.AlertType.WARNING);
            return;
        }

        // Obtener el producto por código
        ProductosDAO pd = new ProductosDAO();
        Productos productoLinea = pd.obtenerProductoLinea(codigo);

        // Verificar si el producto existe
        if (productoLinea == null || productoLinea.getNombreProducto() == null) {
            MostrarAlerta.mostrarAlerta("Ventas", "Producto no encontrado.", Alert.AlertType.WARNING);
            return;
        }

        // Obtener la cantidad del Spinner
        Integer cantidad = spCantidad.getValue();
        if (cantidad == null || cantidad <= 0) {
            MostrarAlerta.mostrarAlerta("Creación de Presupuesto", "Ingrese una cantidad válida.", Alert.AlertType.WARNING);
            return;
        }

        // Calcular el total de la línea
        productoLinea.setCantidad(cantidad);
        productoLinea.setTotalLinea(cantidad * productoLinea.getPrecioUnitario());

        // Agregar el producto a la lista y actualizar la tabla
        productosUtilizados.add(productoLinea);
        calculatTotal();
        txfCodigo.clear();
        txfCodigo.requestFocus();
    }

    public void calculatTotal(){
        float costosVariables = Float.parseFloat(txfCostosVariables.getText());
        float manoDeObra = Float.parseFloat(txfManoDeObra.getText());
        float totalProductos = 0;
        for (Productos p1 : productosUtilizados){
            totalProductos = totalProductos+p1.getTotalLinea();
        }
        float totalGeneral = totalProductos+manoDeObra+costosVariables;
        txfTotalPresupuestado.setText(String.valueOf(totalGeneral));
    }

    public void generarPDF() {
        int respuesta = guardarPresupuesto();
        if (respuesta==-1){
            MostrarAlerta.mostrarAlerta("Crear Presupuesto","Error al crear el presupuesto, no se generara el PDF", Alert.AlertType.WARNING);
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
            String rutaLogo = "C:/Users/nicol/Documents/ProyectofinalNEOTEC/neotec/src/main/resources/img/NeotecLogo.png"; // Cambiar a tu ruta real
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
                for (Productos p1 : productosUtilizados) {
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
            float costosVariables = Float.parseFloat(txfCostosVariables.getText());
            float manoDeObra = Float.parseFloat(txfManoDeObra.getText());
            float totalProductos = 0;
            for (Productos p1 : productosUtilizados){
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

            System.out.println("✅ Presupuesto generado en: " + destino);

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
    public int guardarPresupuesto(){
        //obtener id equipo y totales para crear el presupuesto
        int idequipo = equipo.getId();
        float costosVariables = Float.parseFloat(txfCostosVariables.getText());
        float manoDeObra = Float.parseFloat(txfManoDeObra.getText());
        float totalProductos = 0;
        for (Productos p1 : productosUtilizados){
            totalProductos = totalProductos+p1.getTotalLinea();
        }
        float totalGeneral = totalProductos+manoDeObra+costosVariables;
        String observaciones = txaObs.getText();
        PresupuestoDAO pd =new PresupuestoDAO();
        int respuesta = pd.insertPresupuesto(spTiempoReparacion.getValue(),costosVariables,manoDeObra,totalGeneral,idequipo,totalProductos,observaciones);

        guardarProductosPresupuestos(respuesta);

        return respuesta;
    }

    private void guardarProductosPresupuestos(int idpresupuesto) {
        PresupuestoDAO pd = new PresupuestoDAO();
        for (Productos p1 : productosUtilizados){
            System.out.println("intento de insercion");
            pd.insertProductoPresupuesto(idpresupuesto,p1.getIdProductos(), p1.getCantidad());
        }
    }
}
