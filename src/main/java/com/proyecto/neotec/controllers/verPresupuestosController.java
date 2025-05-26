package com.proyecto.neotec.controllers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.proyecto.neotec.DAO.*;
import com.proyecto.neotec.models.*;
import com.proyecto.neotec.util.CajaEstablecida;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.SesionUsuario;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class verPresupuestosController {
    @FXML
    public ToggleButton toggleDispositivo;
    @FXML
    public TextField txtBuscardor;
    @FXML
    public ToggleButton toggleCliente;
    @FXML
    public DatePicker dateFechaCreacion;
    @FXML
    private TableView<Presupuestos> tablaPresupuestos;

    @FXML
    private TableColumn<Presupuestos, Integer> columna1;
    @FXML
    private TableColumn<Presupuestos, String> columna2;
    @FXML
    private TableColumn<Presupuestos, String> columna3;
    @FXML
    private TableColumn<Presupuestos, String> columna4;
    @FXML
    private TableColumn<Presupuestos, String> columna5;
    @FXML
    private TableColumn<Presupuestos, String> columna6;
    @FXML
    private ObservableList<Presupuestos> presupuestos;


    private PresupuestoDAO presupuestoDAO;
    @FXML
    public void initialize() {
        presupuestoDAO = new PresupuestoDAO();
        cargarDatos(presupuestoDAO.selectAllPresupuestos());

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleCliente.setToggleGroup(toggleGroup);
        toggleDispositivo.setToggleGroup(toggleGroup);
        txtBuscardor.setDisable(true);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), event -> {
                    String newValue = txtBuscardor.getText().trim();
                    if (!newValue.isEmpty()) {
                        List<Presupuestos> listaPresupuestos = new ArrayList<>();
                        if (toggleDispositivo.isSelected()) {
                            listaPresupuestos = presupuestoDAO.buscarPresupuestosPorNombreDeEquipo(newValue);
                        }
                        if (toggleCliente.isSelected()) {
                            listaPresupuestos = presupuestoDAO.buscarPresupuestosPorNombreCliente(newValue);
                        }
                        tablaPresupuestos.getItems().setAll(listaPresupuestos);
                    } else {
                        cargarDatos(presupuestoDAO.selectAllPresupuestos());
                    }
                })
        );
        timeline.setCycleCount(1);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                txtBuscardor.setDisable(true);
                txtBuscardor.setText("");
                cargarDatos(presupuestoDAO.selectAllPresupuestos());
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
                cargarDatos(presupuestoDAO.selectAllPresupuestos());
            }
        });

        dateFechaCreacion.getEditor().setDisable(true);
        dateFechaCreacion.setEditable(false);
        dateFechaCreacion.valueProperty().addListener((observable, oldvalue, newValue) -> {
            buscarPorFechaCreacion(dateFechaCreacion);
        });
    }

    private void buscarPorFechaCreacion(DatePicker dateFechaCreacion) {
        LocalDate fecha = dateFechaCreacion.getValue();
        if (fecha != null) {
            // formatear al formato que espera la base de datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = fecha.format(formatter);
            presupuestoDAO = new PresupuestoDAO();
            List<Presupuestos> listaPresupuestos = presupuestoDAO.buscarPorFechaCreacion(fechaFormateada);
            cargarDatos(listaPresupuestos);
        }
    }
    private void cargarDatos(List<Presupuestos> listaPresupuestos) {
        presupuestos = FXCollections.observableArrayList();
        columna1.setCellValueFactory(new PropertyValueFactory<>("idpresupuesto"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        columna3.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        columna4.setCellValueFactory(cellData -> {
            int estadoPresupuesto = cellData.getValue().getEstado();
            // Obtener la descripción del estado usando el DAO
            String descripcionEstado = presupuestoDAO.obtenerDescripcionEstadoDesdeBD(estadoPresupuesto);
            return new SimpleStringProperty(descripcionEstado); // Devolver la descripción del estado
        });
        columna5.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        presupuestos.clear();
        presupuestos.addAll(listaPresupuestos);
        tablaPresupuestos.setItems(presupuestos);
    }

    public void verDetalles() {
        Presupuestos prVer = tablaPresupuestos.getSelectionModel().getSelectedItem();
        if (prVer == null){
            MostrarAlerta.mostrarAlerta("Presupuestos", "Debe seleccionar un presupuesto para poder ver los detalles", Alert.AlertType.WARNING);
            return;
        }
        int idpresupuesto = prVer.getIdpresupuesto();
        try {
            File file = new File("C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + idpresupuesto + ".pdf"); // "destino" es la ruta del PDF que generaste
            System.out.println("C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + idpresupuesto + ".pdf");
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                MostrarAlerta.mostrarAlerta("Presupuestos", "El archivo que contenia los detalles del presupuesto ya no existe o su nombre fue cambiado y no se pudo encontrarlo", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crearPresupuesto() {
        Integer idequipo = obtenerNumeroDeEquipo();
        if (idequipo == null) return; // Si es null, ya se mostró una alerta y no seguimos.

        EquipoDAO ed = new EquipoDAO();
        Equipos equipoSeleccionado = ed.obtenerEquipoPorId(idequipo);

        if (equipoSeleccionado == null) {
            MostrarAlerta.mostrarAlerta("Crear Presupuesto", "No se encontró ningún equipo con el número ingresado.", Alert.AlertType.WARNING);
            return;
        }

        abrirVentanaCrearPresupuesto(equipoSeleccionado);
        tablaPresupuestos.refresh();
    }

    private Integer obtenerNumeroDeEquipo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Crear Presupuesto");
        dialog.setHeaderText("Ingrese el número de equipo:");
        dialog.setContentText("Número:");

        Optional<String> resultado = dialog.showAndWait();

        if (resultado.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(resultado.get());
        } catch (NumberFormatException e) {
            MostrarAlerta.mostrarAlerta("Crear Presupuesto", "Debe ingresar un número de equipo válido. No se permiten letras ni símbolos.", Alert.AlertType.WARNING);
            return null;
        }
    }

    private void abrirVentanaCrearPresupuesto(Equipos equipo) {
        System.out.println("ESTADO EQUIPO AL ABRIR LA VENTANA CREAR PRESUPUESTO DESDE PRESUPUESTOS: "+equipo.getEstado() );
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/crearPresupuestos.fxml"));
            Parent root = loader.load();

            CrearPresupuestoController controller = loader.getController();
            controller.setEquipo(equipo);

            Stage stage = new Stage();
            stage.setTitle("Crear Presupuesto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            controller.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            MostrarAlerta.mostrarAlerta("Error", "No se puede crear presupuestos en este momento.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void modificarPresupuesto(){
        Presupuestos pre_mod;
        pre_mod = tablaPresupuestos.getSelectionModel().getSelectedItem();
        if (pre_mod == null){
            MostrarAlerta.mostrarAlerta("Presupuestos", "Debe seleccionar un presupuesto para poder modificarlo", Alert.AlertType.WARNING);
        }else {
            EquipoDAO equipoDAO = new EquipoDAO();
            Equipos equipo = equipoDAO.obtenerEquipoPorId(equipoDAO.obtener_IDequipo_con_idpresupuesto(pre_mod.getIdpresupuesto()));
            abrirModificarPresupuesto(equipo,pre_mod);
        }
    }
    public void abrirModificarPresupuesto(Equipos equipo, Presupuestos presupuesto){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/modificarPresupuesto.fxml"));
            Parent root = loader.load();
            ModificarPresupuestoController controller = loader.getController();

            controller.setEquipo(equipo);
            controller.setPresupuesto(presupuesto);

            Stage stage = new Stage();
            stage.setTitle("Modificar Presupuesto");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            controller.setStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            MostrarAlerta.mostrarAlerta("Error", "No se puede crear presupuestos en este momento.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    public void pagarPresupuesto() {
        try {
            Presupuestos pr = tablaPresupuestos.getSelectionModel().getSelectedItem();
            if (pr == null) {
                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Debe seleccionar un presupuesto antes de continuar.", Alert.AlertType.WARNING);
                return;
            }
            PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
            // Verificar si el presupuesto ya fue pagado
            if (presupuestoDAO.verificarEstadoPagado(pr.getIdpresupuesto()))  {
                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Este presupuesto ya fue pagado anteriormente.", Alert.AlertType.WARNING);
                return;
            }
            // Realizar pago si el presupuesto está autorizado (ID: 2)
            if (presupuestoDAO.verificarEstadoPresupuesto(pr, 2)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar pago");
                alert.setHeaderText("¿El pago fue con efectivo?");
                alert.setContentText("Seleccione una opción:");
                ButtonType buttonSi = new ButtonType("Sí", ButtonBar.ButtonData.OTHER);
                ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.OTHER);
                ButtonType btnCerrar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonSi, buttonNo,btnCerrar);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == buttonSi) {
                    // Verificar que hay una caja establecida
                    Caja cajaestablecida = CajaEstablecida.getCajaSeleccionada();
                    if (cajaestablecida == null) {
                        MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Debe establecer una caja en Caja -> Establecer Caja.", Alert.AlertType.WARNING);
                    } else {
                        // Verificar apertura de caja
                        CajaDAO cajadao = new CajaDAO();
                        int idCaja = cajaestablecida.getIdcaja();
                        if (cajadao.verificarApertura(idCaja)) {
                            String respuesta = cajadao.registrarMovimientoDeCaja(
                                    idCaja,
                                    0,
                                    pr.getPrecioTotal(),
                                    SesionUsuario.getUsuarioLogueado().getIdusuarios(),
                                    "Pago presupuesto"
                            );
                            if ("Error: Ocurrió un problema al registrar el movimiento.".equals(respuesta)) {
                                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Error: Ocurrió un problema al registrar el movimiento en la caja", Alert.AlertType.WARNING);
                            } else if (cambiarEstadoEquipo(pr)){
                                presupuestoDAO.cambiarEstadoPresupuesto(pr,4);// Confirmación del pago en efectivo
                                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "El pago en efectivo se ha realizado correctamente.", Alert.AlertType.INFORMATION);
                                GenerarComprobante(pr);
                            }else if (!cambiarEstadoEquipo(pr)){
                                MostrarAlerta.mostrarAlerta(
                                        "Error",
                                        "El presupuesto utiliza productos no disponibles en stock",
                                        Alert.AlertType.INFORMATION
                                );
                            }
                            } else {
                            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "La caja establecida está cerrada", Alert.AlertType.WARNING);
                        }
                    }
                } else if (result.get() == buttonNo) {
                    // Crear el cuadro de diálogo
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Indique el tipo de transacción digital");

                    TextField textField = new TextField();
                    textField.setPromptText("Escriba aquí...");

                    // Agregar el campo de texto al contenido del diálogo
                    VBox content = new VBox(10, new Label("Observación:"), textField);
                    dialog.getDialogPane().setContent(content);

                    // Agregar botones
                    ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
                    ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                    dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, btnCancelar);

                    // Establecer el resultado del diálogo
                    dialog.setResultConverter(button -> (button == btnAceptar) ? textField.getText() : null);

                    // Mostrar el diálogo y obtener el resultado
                    Optional<String> resultado = dialog.showAndWait();

                    // Solo ejecutar el bloque si se presionó Aceptar (es decir, si hay un valor)
                    if (resultado.isPresent()) {
                        AtomicReference<String> mensaje = new AtomicReference<>(resultado.get());

                        try {
                            TransaccionesDigitalesDAO tdd = new TransaccionesDigitalesDAO();
                            tdd.registrarTransaccion(0, pr.getPrecioTotal(), 1, mensaje.get());
                            if (cambiarEstadoEquipo(pr)){
                                presupuestoDAO.cambiarEstadoPresupuesto(pr,4);
                                // Confirmación del pago en efectivo
                                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "El pago en efectivo se ha realizado correctamente.", Alert.AlertType.INFORMATION);
                                GenerarComprobante(pr);
                            }else if (!cambiarEstadoEquipo(pr)){
                                MostrarAlerta.mostrarAlerta(
                                        "Error",
                                        "El presupuesto utiliza productos no disponibles en stock",
                                        Alert.AlertType.INFORMATION
                                );
                                }
                        } catch (Exception e) {
                            e.printStackTrace();
                            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Error al registrar la transacción digital.", Alert.AlertType.ERROR);
                        }
                    }

                } else if (result.get() == btnCerrar) {
                    alert.close();
                }

            } else {
                MostrarAlerta.mostrarAlerta("Pago de presupuesto", "El presupuesto debe estar aprobado", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    public boolean cambiarEstadoEquipo(Presupuestos pr) {
        ProductosDAO productosDAO = new ProductosDAO();
        EquipoDAO equipoDAO = new EquipoDAO();

        List<Productos> productos = productosDAO.obtenerProductosPorPresupuesto(pr.getIdpresupuesto());
        boolean hayFaltante = false;

        // Verificar existencia y descontar stock
        for (Productos producto : productos) {
            int idProducto = producto.getIdProductos();
            int cantidadRequerida = producto.getCantidad();

            if (!productosDAO.productoExiste(idProducto)) {
                hayFaltante = true;
                break;
            }

            int stockDisponible = productosDAO.obtenerCantidad(idProducto);
            if (cantidadRequerida > stockDisponible) {
                hayFaltante = true;
                break;
            }
        }

        // Obtener el equipo relacionado al presupuesto
        int idEquipo = equipoDAO.obtener_IDequipo_con_idpresupuesto(pr.getIdpresupuesto());
        Equipos equipo = equipoDAO.obtenerEquipoPorId(idEquipo);

        if (hayFaltante) {
            equipoDAO.actualizarEstadoEquipo(equipo.getId(), 3); // Espera de Autorización
            List<Presupuestos> lista = presupuestoDAO.selectAllPresupuestos();
            cargarDatos(lista);
            return false;
        } else {
            for (Productos producto : productos) {
                int idProducto = producto.getIdProductos();
                int cantidadRequerida = producto.getCantidad();
                productosDAO.descontarStock(idProducto, cantidadRequerida);
            }
            return true;
        }
    }


    public void cambiarEstado(ActionEvent actionEvent) {

        Presupuestos pr = tablaPresupuestos.getSelectionModel().getSelectedItem(); // Esto deberías adaptarlo

        if (pr == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selecciona un pr primero.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        int estadoActual = pr.getEstado();

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Estado");
        dialog.setHeaderText("Por favor, elige un estado:");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().clear(); // Por si acaso
        PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
        if (estadoActual == 1) { //id:1 = "En espera"
            comboBox.getItems().addAll("Aprobado", "Cancelado");
        } else {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Cambio no permitido");
            alerta.setHeaderText(null);
            alerta.setContentText("No se puede cambiar el estado. Este presupuesto ya fue " + presupuestoDAO.obtenerDescripcionEstadoDesdeBD(estadoActual).toLowerCase() + ".");
            alerta.showAndWait();
            return; // Se sale del método para no continuar con el diálogo
        }

        if (comboBox.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Este pr ya no puede cambiar de estado.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        comboBox.setValue(comboBox.getItems().get(0));

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

            EquipoDAO equipoDAO = new EquipoDAO();
            int estado = presupuestoDAO.obtenerEstadoIntDesdeBD(estadoSeleccionado);
            int idEquipoPresupuesto = equipoDAO.obtener_IDequipo_con_idpresupuesto(pr.getIdpresupuesto());
            if (estado == 3){
                // si el pr es cancelado, cambiamos el estado del equipo a no autorizado para la reparación
                equipoDAO.actualizarEstadoEquipo(idEquipoPresupuesto,4);
            }
            if (estado==2) {
                //pr aprobado = equipo autorizado para la reparación
                equipoDAO.actualizarEstadoEquipo(idEquipoPresupuesto,5);
            }
            presupuestoDAO.cambiarEstadoPresupuesto(pr,estado);
            cargarDatos(presupuestoDAO.selectAllPresupuestos());
        });
    }

    public void QuitarFiltros(ActionEvent actionEvent) {
        presupuestoDAO= new PresupuestoDAO();
        cargarDatos(presupuestoDAO.selectAllPresupuestos());
    }
    public void GenerarComprobante(Presupuestos pr){

        PresupuestoDAO pd = new PresupuestoDAO();

        EquipoDAO equipoDAO = new EquipoDAO();
        Equipos equipo = equipoDAO.obtenerEquipoPorId(equipoDAO.obtener_IDequipo_con_idpresupuesto(pr.getIdpresupuesto()));

        LocalDateTime fechaYhora = pd.obtenerFechaHora(pr.getIdpresupuesto());

        DecimalFormat formatoPrecio = new DecimalFormat("#0.00");
        String destino = "";
        try {
            String fechaHora = fechaYhora.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fechaFormateada = fechaYhora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaFormateada = fechaYhora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            destino = "C:/COMPROBANTES_NEOTEC/Comprobante_"  + ".pdf";

            PdfWriter writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(30, 30, 30, 30);

            // Estilo similar a Mercado Pago
            Paragraph header = new Paragraph("NEOTEC REPARACIONES")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(10);
            document.add(header);

            Paragraph tipoComprobante = new Paragraph("COMPROBANTE DE SERVICIO")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(20);
            document.add(tipoComprobante);

            // Sección de detalles principales
            Paragraph detalleServicio = new Paragraph("Reparación de equipo")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginBottom(5);
            document.add(detalleServicio);

            Paragraph totalPagado = new Paragraph("Total abonado")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setMarginBottom(5);
            document.add(totalPagado);

            // Monto grande como en el ejemplo
            //TODO traer total general
            float totalGeneral = pr.getPrecioTotal();
            Paragraph monto = new Paragraph("$ " + formatoPrecio.format(totalGeneral))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold()
                    .setMarginBottom(20);
            document.add(monto);

            // Línea divisoria
            LineSeparator ls = new LineSeparator(new SolidLine());
            document.add(ls);

            // Sección de Detalles
            Paragraph detallesHeader = new Paragraph("Detalles")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(detallesHeader);

            // Tabla de detalles
            float[] columnWidths = {3, 1};
            Table detallesTable = new Table(columnWidths);
            detallesTable.setWidth(UnitValue.createPercentValue(100));

            // TODO traer Productos utilizados
            ProductosDAO productosDAO = new ProductosDAO();

            List<Productos> productosUtilizados = productosDAO.obtenerProductosPorPresupuesto(pr.getIdpresupuesto());

            Paragraph subtitulo2 = new Paragraph("Productos utilizados")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setBold();
            document.add(subtitulo2);

            // Tabla de productos
            float[] columnW = {200F, 100F, 80F, 100F};
            Table productosTable = new Table(columnW);
            productosTable.setWidth(UnitValue.createPercentValue(100));

            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Producto").setBold()));
            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Precio U").setBold()));
            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Cantidad").setBold()));
            productosTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Total Línea").setBold()));
            float totalproductos= 0;
            if (!productosUtilizados.isEmpty()) {
                for (Productos p1 : productosUtilizados) {
                    totalproductos = p1.getPrecioCosto() + totalproductos;
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
            // Tabla de productos
            Paragraph totalp = new Paragraph("Total productos utilizados: $" + totalproductos)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold();
            document.add(totalp);
            // TODO traer Mano de obra
            Paragraph totalmo = new Paragraph("Total mano de obra: $" + pr.getManoDeObra())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold();
            document.add(totalmo);
            float manoDeObra = pr.getManoDeObra();
            if (manoDeObra > 0) {
                detallesTable.addCell(new Cell().add(new Paragraph("Mano de obra")));
                detallesTable.addCell(new Cell().add(new Paragraph("$" + formatoPrecio.format(manoDeObra)))
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            //agregado
            float costosVariables = pr.getCostosVariables();
            if (costosVariables > 0) {
                detallesTable.addCell(new Cell().add(new Paragraph("Costos variables:")));
                detallesTable.addCell(new Cell().add(new Paragraph("$" + formatoPrecio.format(costosVariables)))
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(detallesTable);

            // Línea divisoria
            document.add(ls);

            // Sección Total
            Paragraph totalHeader = new Paragraph("Total")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(5);
            document.add(totalHeader);

            Paragraph totalFinal = new Paragraph("$ " + formatoPrecio.format(totalGeneral))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(20);
            document.add(totalFinal);

            // Línea divisoria
            document.add(ls);

            // Sección de información adicional
            Paragraph infoHeader = new Paragraph("Información adicional")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(10)
                    .setMarginTop(10)
                    .setMarginBottom(5);
            document.add(infoHeader);
            //TODO traer datos clientes
            //Cambiar esta linea:


            ClienteDAO cd = new ClienteDAO();
            int respuesta = equipo.getId();
            Paragraph clienteInfo = new Paragraph()
                    .add("Cliente: " + cd.obtenerNombreCompletoPorId(equipo.getIdcliente()) + "\n")
                    .add("DNI: " + cd.obtenerDniPorId(equipo.getIdcliente()) + "\n")
                    .add("Comprobante N°: " + respuesta + "\n")
                    .add("Fecha: " + fechaFormateada + "\n")
                    .add("Hora: " + horaFormateada + "\n")
                    .setFontSize(10)
                    .setMarginBottom(10);
            document.add(clienteInfo);

            // TODO traer Observaciones si las hay
            Paragraph observaciones = new Paragraph("Observaciones:\n" +pr.getObservaciones())
                    .setFontSize(10)
                    .setMarginBottom(10);
            document.add(observaciones);


            // Footer
            Paragraph footer = new Paragraph("Gracias por confiar en NEOTEC")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setItalic()
                    .setMarginTop(20);
            document.add(footer);

            document.close();

            System.out.println("✅ Comprobante generado en: " + destino);
            AbrirComprobante(destino);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void AbrirComprobante(String destino){
        // Abrir el pdf
        try {
            File file = new File(destino);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
                MostrarAlerta.mostrarAlerta("Crear Comprobante", "El comprobante ha sido creado", Alert.AlertType.INFORMATION);
            } else {
                MostrarAlerta.mostrarAlerta("Error", "Error al abrir el archivo", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void DefinirEstados(ActionEvent actionEvent) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Estado");
        dialog.setHeaderText("Por favor, elige un estado:");

        // Crear el ComboBox con opciones de estado
        ComboBox<String> comboBox = new ComboBox<>();
        List<String> estados = List.of(
                "Seleccionar todos los Presupuestos","En espera de Autorización", "Aprobado", "Cancelado",
                "Pagado"
        );

        comboBox.getItems().addAll(estados);
        comboBox.setValue(estados.get(0)); // Preseleccionar el primer estado
        // Agregar el ComboBox al contenido del diálogo
        VBox content = new VBox(10, new Label("Selecciona:"), comboBox);
        dialog.getDialogPane().setContent(content);
        // Agregar botones al diálogo
        ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, btnCancelar);
        // Configurar resultado cuando se presiona "Aceptar"
        dialog.setResultConverter(dialogButton ->
                dialogButton == btnAceptar ? comboBox.getValue() : null
        );
        // Mostrar el diálogo y obtener el resultado
        Optional<String> resultado = dialog.showAndWait();
        PresupuestoDAO presupuestosDAO = new PresupuestoDAO();
        resultado.ifPresent(estadoSeleccionado -> {
            if (estadoSeleccionado.equals("Seleccionar todos los Equipos")){
                List<Presupuestos> todos= presupuestosDAO.selectAllPresupuestos();
                tablaPresupuestos.getItems().setAll(todos);
            }else{
                List<Presupuestos> equiposFiltrados = presupuestosDAO.filtrarPorEstadoEquipo(presupuestosDAO.obtenerEstadoIntDesdeBD(estadoSeleccionado));
                tablaPresupuestos.getItems().setAll(equiposFiltrados);
            }
        });
    }

}

