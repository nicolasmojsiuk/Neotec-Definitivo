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
import com.proyecto.neotec.models.Presupuestos;
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

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ModificarPresupuestoController {
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

    // Declarar la variable como atributo de clase
    private ObservableList<Producto> productoPresupuestos = FXCollections.observableArrayList();

    private ObservableList<Producto> productoUtilizados;
    private Stage stage;
    private Equipos equipo;
    private Presupuestos presupuesto;

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

        productoUtilizados = FXCollections.observableArrayList();
        tablaProductos.setItems(productoUtilizados);

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

    private void cargarCampos() {
        txaObs.setText(presupuesto.getObservaciones());
        txfCostosVariables.setText(String.valueOf(presupuesto.getCostosVariables()));
        txfManoDeObra.setText(String.valueOf(presupuesto.getCostosVariables()));
        spTiempoReparacion.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, presupuesto.getDiasEstimados())
        );
        txfTotalPresupuestado.setText(String.valueOf(presupuesto.getPrecioTotal()));
        cargarProductosDesdePresupuesto(presupuesto.getIdpresupuesto());
    }
    private void cargarProductosDesdePresupuesto(int idPresupuesto) {
        ProductosDAO productosDAO = new ProductosDAO();
        productoPresupuestos.clear();
        productoPresupuestos.addAll(productosDAO.obtenerProductosPorPresupuesto(idPresupuesto));

        for (Producto p : productoPresupuestos) {
            // Calculamos el total de la línea: cantidad * precio unitario
            p.setTotalLinea(p.getCantidad() * p.getPrecioUnitario());

        }
        productoUtilizados.setAll(productoPresupuestos);
        tablaProductos.setItems(productoUtilizados);
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

        if (codigo.isEmpty()) {
            MostrarAlerta.mostrarAlerta("Creación de Presupuesto", "Ingrese el código de un producto para agregarlo al presupuesto.", Alert.AlertType.WARNING);
            return;
        }

        ProductosDAO pd = new ProductosDAO();
        Producto productoLinea = pd.obtenerProductoLinea(codigo);

        if (productoLinea == null || productoLinea.getNombreProducto() == null) {
            MostrarAlerta.mostrarAlerta("Ventas", "Producto no encontrado.", Alert.AlertType.WARNING);
            return;
        }

        // 🚨 Verificar si el ID es válido
        if (productoLinea.getIdProductos() <= 0) {
            MostrarAlerta.mostrarAlerta("Error", "El producto con código " + codigo + " tiene un ID inválido.", Alert.AlertType.ERROR);
            return;
        }

        Integer cantidad = spCantidad.getValue();
        if (cantidad == null || cantidad <= 0) {
            MostrarAlerta.mostrarAlerta("Creación de Presupuesto", "Ingrese una cantidad válida.", Alert.AlertType.WARNING);
            return;
        }

        productoLinea.setCantidad(cantidad);
        productoLinea.setTotalLinea(cantidad * productoLinea.getPrecioUnitario());

        // 🛑 Depuración: Imprimir datos antes de agregar
        System.out.println("Producto agregado:");
        System.out.println("   - ID Producto: " + productoLinea.getIdProductos());
        System.out.println("   - Nombre: " + productoLinea.getNombreProducto());
        System.out.println("   - Cantidad: " + productoLinea.getCantidad());

        productoUtilizados.add(productoLinea);
        calculatTotal();
        txfCodigo.clear();
        txfCodigo.requestFocus();
    }

    public void calculatTotal() {
        // Obtener los valores de los campos de texto y validarlos
        float manoDeObra = 0;
        String manoDeObraText = txfManoDeObra.getText();
        if (!manoDeObraText.isEmpty()) {
            manoDeObra = Float.parseFloat(manoDeObraText);
        }

        float costosVariables = 0;
        String costosVariablesText = txfCostosVariables.getText();
        if (!costosVariablesText.isEmpty()) {
            costosVariables = Float.parseFloat(costosVariablesText);
        }

        // El total de los productos debe calcularse de acuerdo con la lista de productos utilizados
        float totalProductos = 0;
        for (Producto p1 : productoUtilizados) {
            totalProductos += p1.getTotalLinea();
        }

        // Calcular el total general
        float totalGeneral = totalProductos + manoDeObra + costosVariables;

        // Actualizar la interfaz de usuario (si es necesario)
        // Por ejemplo, actualizar el campo de total
        txfTotalPresupuestado.setText(String.valueOf(totalGeneral));
    }

    public void generarPDF() {
        int respuesta = guardarPresupuesto();
        if (respuesta == -1) {
            MostrarAlerta.mostrarAlerta("Modificar Presupuesto", "Error al modificar el presupuesto, no se generará el PDF", Alert.AlertType.WARNING);
            return;
        }

        //TODO obtener fecha y hora de la BBDD para que coincida con el PDF
        PresupuestoDAO pd = new PresupuestoDAO();
        LocalDateTime fechaYhora = pd.obtenerFechaHora(respuesta);

        DecimalFormat formatoPrecio = new DecimalFormat("#0.00");
        String destino = "";
        try {
            String fechaHora = fechaYhora.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fechaHoraFormateada = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            // Usa la misma ruta y nombre del archivo para sobrescribirlo
            destino = "C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + respuesta + ".pdf";

            // El PdfWriter sobrescribirá el archivo existente
            PdfWriter writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título centrado
            Paragraph titulo = new Paragraph("PRESUPUESTO DE REPARACION")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(titulo);
            Paragraph subtitulo = new Paragraph("Los presupuestos están sujetos a cambios dependiendo el tiempo de respuesta del cliente.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11);
            document.add(subtitulo);

            // Información del ticket y cliente
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

            if (!productoUtilizados.isEmpty()) {
                for (Producto p1 : productoUtilizados) {
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
            if (!txaObs.getText().isEmpty()) {
                observaciones = txaObs.getText();
            }
            document.add(new Paragraph(observaciones));

            float costosVariables = Float.parseFloat(txfCostosVariables.getText());
            float manoDeObra = Float.parseFloat(txfManoDeObra.getText());
            float totalProductos = 0;
            for (Producto p1 : productoUtilizados){
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
            document.close();
            MostrarAlerta.mostrarAlerta("Modificar Presupuesto", "El presupuesto se ha modificado y el PDF ha sido actualizado.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            MostrarAlerta.mostrarAlerta("Error al generar el PDF", "Hubo un problema al generar el PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    public int guardarPresupuesto() {
        // Obtener id equipo y totales para actualizar el presupuesto
        int idequipo = equipo.getId();
        float costosVariables = Float.parseFloat(txfCostosVariables.getText());
        float manoDeObra = Float.parseFloat(txfManoDeObra.getText());

        // Calcular el total de productos utilizados
        float totalProductos = 0;
        for (Producto p1 : productoUtilizados) {
            totalProductos += p1.getTotalLinea();
        }

        // Calcular el total general del presupuesto
        float totalGeneral = totalProductos + manoDeObra + costosVariables;
        String observaciones = txaObs.getText();

        // Instancia de PresupuestoDAO
        PresupuestoDAO pd = new PresupuestoDAO();

        // Primero, actualizar el presupuesto
        int respuesta = pd.modificarPresupuesto(
                presupuesto.getIdpresupuesto(),
                spTiempoReparacion.getValue(),
                costosVariables,
                manoDeObra,
                totalGeneral,
                idequipo,
                totalProductos,
                observaciones
        );

        // Luego, guardar los productos utilizados en el presupuesto
        guardarProductosPresupuestos(respuesta);

        return respuesta;
    }

    private void guardarProductosPresupuestos(int idpresupuesto) {
        PresupuestoDAO pd = new PresupuestoDAO();
        ProductosDAO productosDao = new ProductosDAO();
        for (Producto p1 : productoUtilizados) {
            System.out.println("Intentando insertar producto en presupuesto:");
            System.out.println("   - ID Producto: " + p1.getIdProductos());
            System.out.println("   - Cantidad: " + p1.getCantidad());

            if (!productosDao.productoExiste(p1.getIdProductos())) {
                System.out.println("⚠ ERROR: El producto con ID " + p1.getIdProductos() + " NO EXISTE en la base de datos.");
                continue; // Evita la inserción si el producto no existe
            }
            if(pd.verificarProductoPresupuesto(idpresupuesto,p1.getIdProductos())){
                pd.insertProductoPresupuesto(idpresupuesto, p1.getIdProductos(), p1.getCantidad());
                System.out.println("✅ Producto válido, insertando en la tabla productopresupuesto...");

            }

        }
    }


    public void setPresupuesto(Presupuestos presupuesto) {
        this.presupuesto = presupuesto;
        cargarCampos();
    }
}
