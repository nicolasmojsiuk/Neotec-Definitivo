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
import org.apache.log4j.Logger;

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
    private static final Logger logger = Logger.getLogger(verPresupuestosController.class);
    private void buscarPorFechaCreacion(DatePicker dateFechaCreacion) {
        logger.info("Iniciando búsqueda de presupuestos por fecha de creación.");
        LocalDate fecha = dateFechaCreacion.getValue();
        if (fecha != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = fecha.format(formatter);
            logger.debug("Fecha seleccionada para búsqueda: "+ fechaFormateada);
            presupuestoDAO = new PresupuestoDAO();
            List<Presupuestos> listaPresupuestos = presupuestoDAO.buscarPorFechaCreacion(fechaFormateada);
            logger.info("Se encontraron " +listaPresupuestos.size()+ " presupuestos para la fecha "+ fechaFormateada);
            cargarDatos(listaPresupuestos);
        } else {
            logger.warn("No se seleccionó una fecha para la búsqueda de presupuestos.");
        }
    }
    private void cargarDatos(List<Presupuestos> listaPresupuestos) {
        logger.info("Intento de cargar datos de la tabla presupuestos");
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
        logger.info("Intento de ver detalles del presupuesto");
        Presupuestos prVer = tablaPresupuestos.getSelectionModel().getSelectedItem();
        if (prVer == null){
            logger.warn("No se ha seleccionado ningún presupuesto");
            MostrarAlerta.mostrarAlerta("Presupuestos", "Debe seleccionar un presupuesto para poder ver los detalles", Alert.AlertType.WARNING);
            return;
        }
        int idpresupuesto = prVer.getIdpresupuesto();
        logger.debug("Intento de cargar el presupuesto");
        try {
            File file = new File("C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + idpresupuesto + ".pdf"); // "destino" es la ruta del PDF que generaste
            System.out.println("C:/PRESUPUESTOS_NEOTEC/Presupuesto_" + idpresupuesto + ".pdf");
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                MostrarAlerta.mostrarAlerta("Presupuestos", "El archivo que contenia los detalles del presupuesto ya no existe o su nombre fue cambiado y no se pudo encontrarlo", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            logger.error("Ha ocurrido una excepción al tratar de abrir el presupuesto. Detalles: "+ e.getMessage() + ". " + e);
        }
    }

    public void crearPresupuesto() {
        logger.info("Intento de abrir el formulario de creación de presupuestos");
        Integer idequipo = obtenerNumeroDeEquipo();
        if (idequipo == null) return; // Si es null, ya se mostró una alerta y no seguimos.
        EquipoDAO ed = new EquipoDAO();
        Equipos equipoSeleccionado = ed.obtenerEquipoPorId(idequipo);

        if (equipoSeleccionado == null) {
            logger.warn("No se ha seleccionado ningún equipo para crear un presupuesto");
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

    public boolean puedeCrearNuevoPresupuesto(List<Presupuestos> listaPresupuestos) {
        // Si no hay presupuestos, se puede crear uno nuevo
        if (listaPresupuestos == null || listaPresupuestos.isEmpty()) {
            return true;
        }

        // Verificar el estado de todos los presupuestos
        for (Presupuestos presupuesto : listaPresupuestos) {
            int estado = presupuesto.getEstado(); // Asumiendo que hay un método getEstado()

            // Si encontramos algún presupuesto que NO esté cancelado (3) ni pagado (4)
            if (estado != 3 && estado != 4) {
                return false;
            }
        }
        // Todos los presupuestos están cancelados o pagados
        return true;
    }

    private void abrirVentanaCrearPresupuesto(Equipos equipo) {
        logger.debug("Verificando si se puede crear un nuevo presupuesto para el equipo:");
        List<Presupuestos> listaPresupuestos = presupuestoDAO.obtenerPresupuestosPorIdEquipo(equipo.getId());
        if (puedeCrearNuevoPresupuesto(listaPresupuestos)){
            logger.debug("Intento de crear nuevo presupuesto para el equipo:" + equipo.getDispositivo());
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
                logger.error("Error, Ocurrio una excepción al tratar de cargar el formulario de cración. Detalles:"+e.getMessage()+ ". "+e);
            }
        }else {
            String nombreEquipo = equipo.getDispositivo();
            logger.info("No se puede crear un nuevo presupuesto para este equipo." + nombreEquipo + ". Hasta que el presupuesto actual esté cancelado o pagado");
            MostrarAlerta.mostrarAlerta("Sacar Presupuesto","No puede crear un nuevo presupuesto hasta que el actual esté cancelado o pagado", Alert.AlertType.ERROR);
        }
    }

    public void modificarPresupuesto(){
        logger.info("Intento de modificar presupuesto");
        Presupuestos pre_mod = tablaPresupuestos.getSelectionModel().getSelectedItem();
        if (pre_mod == null){
            logger.warn("No se ha seleccionado ningún presupuesto para modificar");
            MostrarAlerta.mostrarAlerta("Presupuestos", "Debe seleccionar un presupuesto para poder modificarlo", Alert.AlertType.WARNING);
        }else {
            EquipoDAO equipoDAO = new EquipoDAO();
            Equipos equipo = equipoDAO.obtenerEquipoPorId(equipoDAO.obtener_IDequipo_con_idpresupuesto(pre_mod.getIdpresupuesto()));
            abrirModificarPresupuesto(equipo,pre_mod);
        }
    }
    public void abrirModificarPresupuesto(Equipos equipo, Presupuestos presupuesto){
        logger.info("Intento de abrir formulario de modificación");
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
            logger.error("Error, No se pudo cargar el formulario. Detalles:"+e.getMessage()+ ". "+ e);
        }
    }
    public void pagarPresupuesto() {
        logger.info("Intento de pagar presupuesto");

        try {
            Presupuestos pr = tablaPresupuestos.getSelectionModel().getSelectedItem();

            if (pr == null) {
                logger.warn("No se ha seleccionado ningún presupuesto");
                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Debe seleccionar un presupuesto antes de continuar.", Alert.AlertType.WARNING);
                return;
            }

            logger.debug("Presupuesto seleccionado: ID " + pr.getIdpresupuesto());

            PresupuestoDAO presupuestoDAO = new PresupuestoDAO();

            if (presupuestoDAO.verificarEstadoPagado(pr.getIdpresupuesto())) {
                logger.debug("El presupuesto N°" + pr.getIdpresupuesto() + " ya ha sido pagado anteriormente");
                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Este presupuesto ya fue pagado anteriormente.", Alert.AlertType.WARNING);
                return;
            }

            if (presupuestoDAO.verificarEstadoPresupuesto(pr, 2)) {
                logger.debug("El presupuesto N°" + pr.getIdpresupuesto() + " está aprobado. Mostrando confirmación de pago...");

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar pago");
                alert.setHeaderText("¿El pago fue con efectivo?");
                alert.setContentText("Seleccione una opción:");
                ButtonType buttonSi = new ButtonType("Sí", ButtonBar.ButtonData.OTHER);
                ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.OTHER);
                ButtonType btnCerrar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(buttonSi, buttonNo, btnCerrar);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == buttonSi) {
                    logger.debug("Pago seleccionado: efectivo");

                    Caja cajaestablecida = CajaEstablecida.getCajaSeleccionada();

                    if (cajaestablecida == null) {
                        logger.warn("No se puede registrar el movimiento. No se ha seleccionado una caja");
                        MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Debe establecer una caja en Caja -> Establecer Caja.", Alert.AlertType.WARNING);
                    } else {
                        CajaDAO cajadao = new CajaDAO();
                        int idCaja = cajaestablecida.getIdcaja();
                        logger.debug("Caja establecida: ID " + idCaja);

                        if (cajadao.verificarApertura(idCaja)) {
                            logger.debug("La caja está abierta. Registrando movimiento...");

                            String respuesta = cajadao.registrarMovimientoDeCaja(
                                    idCaja,
                                    0,
                                    pr.getPrecioTotal(),
                                    SesionUsuario.getUsuarioLogueado().getIdusuarios(),
                                    "Pago presupuesto"
                            );

                            if ("Error: Ocurrió un problema al registrar el movimiento.".equals(respuesta)) {
                                logger.error("Ha ocurrido un error en la base de datos al tratar de registrar el movimiento");
                                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Error: Ocurrió un problema al registrar el movimiento en la caja", Alert.AlertType.WARNING);
                            } else if (cambiarEstadoEquipo(pr)) {
                                logger.info("Movimiento registrado con éxito. Cambiando estado de presupuesto a PAGADO.");
                                presupuestoDAO.cambiarEstadoPresupuesto(pr, 4);
                                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "El pago en efectivo se ha realizado correctamente.", Alert.AlertType.INFORMATION);
                                GenerarComprobante(pr);
                            } else {
                                logger.error("No se pudo registrar el movimiento. El presupuesto utiliza productos no disponibles en stock");
                                MostrarAlerta.mostrarAlerta("Error", "El presupuesto utiliza productos no disponibles en stock", Alert.AlertType.INFORMATION);
                            }
                        } else {
                            logger.warn("No se pudo registrar el pago. La caja está cerrada");
                            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "La caja establecida está cerrada", Alert.AlertType.WARNING);
                        }
                    }

                } else if (result.get() == buttonNo) {
                    logger.debug("Pago seleccionado: transacción digital");

                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Indique el tipo de transacción digital");

                    TextField textField = new TextField();
                    textField.setPromptText("Escriba aquí...");

                    VBox content = new VBox(10, new Label("Observación:"), textField);
                    dialog.getDialogPane().setContent(content);

                    ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
                    ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                    dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, btnCancelar);

                    dialog.setResultConverter(button -> (button == btnAceptar) ? textField.getText() : null);

                    Optional<String> resultado = dialog.showAndWait();

                    if (resultado.isPresent()) {
                        String observacion = resultado.get();
                        logger.debug("Observación de transacción digital: " + observacion);

                        try {
                            TransaccionesDigitalesDAO tdd = new TransaccionesDigitalesDAO();
                            tdd.registrarTransaccion(0, pr.getPrecioTotal(), 1, observacion);
                            logger.info("Transacción digital registrada");

                            if (cambiarEstadoEquipo(pr)) {
                                presupuestoDAO.cambiarEstadoPresupuesto(pr, 4);
                                MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "El pago en efectivo se ha realizado correctamente.", Alert.AlertType.INFORMATION);
                                GenerarComprobante(pr);
                            } else {
                                logger.error("No se pudo completar el pago digital. El presupuesto usa productos no disponibles en stock");
                                MostrarAlerta.mostrarAlerta("Error", "El presupuesto utiliza productos no disponibles en stock", Alert.AlertType.INFORMATION);
                            }
                        } catch (Exception e) {
                            logger.error("Error al registrar la transacción digital", e);
                            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Error al registrar la transacción digital.", Alert.AlertType.ERROR);
                        }
                    }

                } else if (result.get() == btnCerrar) {
                    logger.debug("Pago cancelado por el usuario");
                    alert.close();
                }

            } else {
                logger.warn("El presupuesto no está en estado aprobado. No puede pagarse.");
                MostrarAlerta.mostrarAlerta("Pago de presupuesto", "El presupuesto debe estar aprobado", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            logger.error("Error inesperado durante el proceso de pago del presupuesto", e);
            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    public boolean cambiarEstadoEquipo(Presupuestos pr) {
        logger.info("Iniciando cambio de estado del equipo asociado al presupuesto ID: "+pr.getIdpresupuesto() );

        ProductosDAO productosDAO = new ProductosDAO();
        EquipoDAO equipoDAO = new EquipoDAO();

        List<Producto> productos = productosDAO.obtenerProductosPorPresupuesto(pr.getIdpresupuesto());
        logger.debug("Se obtuvieron "+productos.size()+" productos asociados al presupuesto ID: "+ pr.getIdpresupuesto());

        boolean hayFaltante = false;

        for (Producto producto : productos) {
            int idProducto = producto.getIdProductos();
            int cantidadRequerida = producto.getCantidad();

            if (!productosDAO.productoExiste(idProducto)) {
                logger.warn("El producto ID "+idProducto+" no existe en la base de datos.");
                hayFaltante = true;
                break;
            }
            int stockDisponible = productosDAO.obtenerCantidad(idProducto);
            logger.debug("Producto ID "+idProducto+": stock disponible = "+stockDisponible+", requerido = "+ cantidadRequerida);

            if (cantidadRequerida > stockDisponible) {
                logger.warn("Stock insuficiente para el producto ID "+idProducto+". Requerido:" +cantidadRequerida +" , Disponible:"+ stockDisponible);
                hayFaltante = true;
                break;
            }
        }

        int idEquipo = equipoDAO.obtener_IDequipo_con_idpresupuesto(pr.getIdpresupuesto());
        Equipos equipo = equipoDAO.obtenerEquipoPorId(idEquipo);
        logger.debug("Equipo asociado al presupuesto: ID = "+equipo.getId()+ ", Dispositivo = {}"+  equipo.getDispositivo());

        if (hayFaltante) {
            logger.info("Se detectaron faltantes. El equipo ID "+equipo.getId()+" pasará a estado 'En espera de autorización' (estado 3)" );
            equipoDAO.actualizarEstadoEquipo(equipo.getId(), 3);
            List<Presupuestos> lista = presupuestoDAO.selectAllPresupuestos();
            cargarDatos(lista);
            return false;
        } else {
            for (Producto producto : productos) {
                int idProducto = producto.getIdProductos();
                int cantidadRequerida = producto.getCantidad();
                productosDAO.descontarStock(idProducto, cantidadRequerida);
                logger.debug("Se descontaron "+cantidadRequerida+" unidades del producto ID "+ idProducto);
            }
            logger.info("Todos los productos tienen stock suficiente. Cambio de estado del equipo exitoso.");
            return true;
        }
    }

    public void cambiarEstado(ActionEvent actionEvent) {
        logger.info("Intento de cambiar el estado de un presupuesto desde la interfaz.");
        Presupuestos pr = tablaPresupuestos.getSelectionModel().getSelectedItem();
        if (pr == null) {
            logger.warn("No se ha seleccionado ningún presupuesto para modificar su estado");
            MostrarAlerta.mostrarAlerta("Error","Debe seleccionar un presupuesto para cambiar su estado", Alert.AlertType.WARNING);
            return;
        }
        int estadoActual = pr.getEstado();
        logger.debug("Presupuesto seleccionado: ID="+pr.getIdpresupuesto()+", Estado actual="+  estadoActual);
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Estado");
        dialog.setHeaderText("Por favor, elige un estado:");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().clear(); // Limpiar el comboBox por si acaso
        PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
        if (estadoActual == 1) { //id:1 = "En espera"
            comboBox.getItems().addAll("Aprobado", "Cancelado");
        } else {
            String estadoDescripcion = presupuestoDAO.obtenerDescripcionEstadoDesdeBD(estadoActual);
            logger.info("Cambio de estado no permitido. El presupuesto ya fue."+ estadoDescripcion.toLowerCase());
            MostrarAlerta.mostrarAlerta("Cambio no permitido","No se puede cambiar el estado. Este presupuesto ya fue "+presupuestoDAO.obtenerDescripcionEstadoDesdeBD(estadoActual).toLowerCase(), Alert.AlertType.INFORMATION);
            return; // Se sale del método para no continuar con el diálogo
        }
        if (comboBox.getItems().isEmpty()) {
            logger.warn("El presupuesto actual no puede cambiar su estado. Presupuesto N°"+pr.getIdpresupuesto());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Este presupuesto ya no puede cambiar de estado.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        logger.debug("Presupuesto en estado 'En espera'. Se permite cambio de estado.");
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
            logger.debug("Usuario seleccionó nuevo estado para el presupuesto: "+ estadoSeleccionado);
            EquipoDAO equipoDAO = new EquipoDAO();
            int estado = presupuestoDAO.obtenerEstadoIntDesdeBD(estadoSeleccionado);
            int idEquipoPresupuesto = equipoDAO.obtener_IDequipo_con_idpresupuesto(pr.getIdpresupuesto());
            if (estado == 3){
                logger.info("Presupuesto cancelado. Actualizando estado del equipo ID " +idEquipoPresupuesto+" a 'No autorizado para reparación' (estado 4)");
                equipoDAO.actualizarEstadoEquipo(idEquipoPresupuesto,4);
            }
            if (estado==2) {
                logger.info("Presupuesto aprobado. Actualizando estado del equipo ID "+idEquipoPresupuesto+" a 'Autorizado para reparación' (estado 5)");
                equipoDAO.actualizarEstadoEquipo(idEquipoPresupuesto,5);
            }
            presupuestoDAO.cambiarEstadoPresupuesto(pr,estado);
            logger.debug("Actualizando estado del presupuesto ID "+pr.getIdpresupuesto()+" a " + estado);
            cargarDatos(presupuestoDAO.selectAllPresupuestos());
        });
    }

    public void QuitarFiltros(ActionEvent actionEvent) {
        logger.info("Quitando filtros de busqueda");
        presupuestoDAO= new PresupuestoDAO();
        cargarDatos(presupuestoDAO.selectAllPresupuestos());
    }
    public void GenerarComprobante(Presupuestos prSeleccionado){
        logger.info("Intento de generar comprobante de pago");
        PresupuestoDAO pd = new PresupuestoDAO();
        Presupuestos pr = pd.obtenerPresupuestoPorId(prSeleccionado.getIdpresupuesto());
        EquipoDAO equipoDAO = new EquipoDAO();
        Equipos equipo = equipoDAO.obtenerEquipoPorId(equipoDAO.obtener_IDequipo_con_idpresupuesto(pr.getIdpresupuesto()));
        LocalDateTime fechaYhora = pd.obtenerFechaHora(pr.getIdpresupuesto());
        DecimalFormat formatoPrecio = new DecimalFormat("#0.00");
        String destino = "";
        // Definiendo variables antes de try para evitar inconvenientes
        String fechaHora = "";
        String fechaFormateada="";
        String horaFormateada="";
        try {
            logger.debug("Formateando fecha y hora...");
            fechaHora = fechaYhora.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fechaFormateada = fechaYhora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            horaFormateada = fechaYhora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            destino = "C:/COMPROBANTES_NEOTEC/Comprobante_" + ".pdf";
        } catch (Exception e) {
            logger.error("Error al formatear la fecha y hora", e);
            return;
        }
        // Definiendo variables antes de try para evitar inconvenientes
        PdfWriter writer = null;
        PdfDocument pdf = null;
        Document document = null;
        try {
            logger.debug("Inicializando PDF...");
            writer = new PdfWriter(destino);
            pdf = new PdfDocument(writer);
            document = new Document(pdf);
            document.setMargins(30, 30, 30, 30);
        } catch (Exception e) {
            logger.error("Error al crear PDFWriter o PdfDocument", e);
            return;
        }

        try {
            logger.debug("Agregando encabezado...");
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
        } catch (Exception e) {
            logger.error("Error al agregar encabezado al documento", e);
        }

        try {
            logger.debug("Agregando sección de servicio y total pagado...");
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

            float totalGeneral = pr.getPrecioTotal();
            Paragraph monto = new Paragraph("$ " + formatoPrecio.format(totalGeneral))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold()
                    .setMarginBottom(20);
            document.add(monto);
        } catch (Exception e) {
            logger.error("Error al agregar detalle de servicio o monto", e);
        }

        try {
            logger.debug("Agregando línea divisoria y título de detalles...");
            LineSeparator ls = new LineSeparator(new SolidLine());
            document.add(ls);

            Paragraph detallesHeader = new Paragraph("Detalles")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(detallesHeader);
        } catch (Exception e) {
            logger.error("Error al agregar línea divisoria o sección de detalles", e);
        }

        float totalproductos;
        ProductosDAO productosDAO = new ProductosDAO();
        List<Producto> productosUtilizados = null;

        try {
            logger.debug("Obteniendo productos utilizados...");
            productosUtilizados = productosDAO.obtenerProductosPorPresupuesto(pr.getIdpresupuesto());

            Paragraph subtitulo2 = new Paragraph("Productos utilizados")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setBold();
            document.add(subtitulo2);

            float[] columnW = {200F, 100F, 80F, 100F};
            Table productosTable = new Table(columnW);
            productosTable.setWidth(UnitValue.createPercentValue(100));

            productosTable.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold()));
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Precio U").setBold()));
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Total Línea").setBold()));

            if (!productosUtilizados.isEmpty()) {
                for (Producto p1 : productosUtilizados) {
                    totalproductos = p1.getPrecioUnitario() * p1.getCantidad();
                    productosTable.addCell(p1.getNombreProducto());
                    productosTable.addCell("$" + formatoPrecio.format(p1.getPrecioUnitario()));
                    productosTable.addCell(String.valueOf(p1.getCantidad()));
                    productosTable.addCell("$" + formatoPrecio.format(totalproductos));
                }
            } else {
                logger.warn("No se ha cargado la tabla de productos utilizados. La tabla esta vacía.");
                productosTable.addCell(new Cell(1, 4)
                        .add(new Paragraph("Tabla sin contenido"))
                        .setTextAlignment(TextAlignment.CENTER));
            }
            document.add(productosTable);
        } catch (Exception e) {
            logger.error("Error al obtener o mostrar productos utilizados", e);
        }

        try {
            logger.debug("Agregando mano de obra y costos variables...");
            float[] columnWidths = {3, 1};
            Table detallesTable = new Table(columnWidths);
            detallesTable.setWidth(UnitValue.createPercentValue(100));

            float manoDeObra = pr.getManoDeObra();
            if (manoDeObra > 0) {
                detallesTable.addCell(new Cell().add(new Paragraph("Mano de obra")));
                detallesTable.addCell(new Cell().add(new Paragraph("$" + formatoPrecio.format(manoDeObra)))
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            float costosVariables = pr.getCostosVariables();
            if (costosVariables > 0) {
                detallesTable.addCell(new Cell().add(new Paragraph("Costos variables:")));
                detallesTable.addCell(new Cell().add(new Paragraph("$" + formatoPrecio.format(costosVariables)))
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(detallesTable);
            document.add(new LineSeparator(new SolidLine()));
        } catch (Exception e) {
            logger.error("Error al agregar detalles financieros (mano de obra, costos)", e);
        }

        try {
            logger.debug("Agregando total final...");
            Paragraph totalHeader = new Paragraph("Total")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(5);
            document.add(totalHeader);

            float totalGeneral = pr.getPrecioTotal();
            Paragraph totalFinal = new Paragraph("$ " + formatoPrecio.format(totalGeneral))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(20);
            document.add(totalFinal);
            document.add(new LineSeparator(new SolidLine()));
        } catch (Exception e) {
            logger.error("Error al mostrar total final", e);
        }

        try {
            logger.debug("Agregando información adicional del cliente...");
            Paragraph infoHeader = new Paragraph("Información adicional")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(14)
                    .setMarginTop(14)
                    .setBold()
                    .setMarginBottom(5);
            document.add(infoHeader);

            ClienteDAO cd = new ClienteDAO();
            int respuesta = equipo.getId();
            Paragraph clienteInfo = new Paragraph()
                    .add("Cliente: " + cd.obtenerNombreCompletoPorId(equipo.getIdcliente()) + "\n")
                    .add("DNI: " + cd.obtenerDniPorId(equipo.getIdcliente()) + "\n")
                    .add("Comprobante N°: " + respuesta + "\n")
                    .add("Fecha: " + fechaFormateada + "\n")
                    .add("Hora: " + horaFormateada + "\n")
                    .setFontSize(12)
                    .setMarginBottom(12);
            document.add(clienteInfo);

            Paragraph observaciones = new Paragraph("Observaciones:\n" + pr.getObservaciones())
                    .setFontSize(12)
                    .setMarginBottom(12);
            document.add(observaciones);
        } catch (Exception e) {
            logger.error("Error al mostrar información adicional del cliente", e);
        }

        try {
            logger.debug("Agregando footer y cerrando documento...");
            Paragraph footer = new Paragraph("Gracias por confiar en NEOTEC")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setItalic()
                    .setBold()
                    .setMarginTop(20);
            document.add(footer);
            document.close();
            logger.debug("El comprobante ha sido creado correctamente");
            System.out.println("✅ Comprobante generado en: " + destino);
        } catch (Exception e) {
            logger.error("Error al finalizar y cerrar el documento PDF", e);
        }

        try {
            logger.debug("Intentando abrir el comprobante generado...");
            AbrirComprobante(destino);
        } catch (Exception e) {
            logger.error("Error al abrir el comprobante PDF generado", e);
        }

    }
    public void AbrirComprobante(String destino){
        logger.info("Intento de abrir el comprobante ubicado en la ruta: "+ destino);
        // Abrir el pdf
        try {
            File file = new File(destino);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
                MostrarAlerta.mostrarAlerta("Crear Comprobante", "El comprobante ha sido creado", Alert.AlertType.INFORMATION);
            } else {
                logger.error("Error, al abrir el archivo en la ruta indicada:" + destino);
                MostrarAlerta.mostrarAlerta("Error", "Error al abrir el archivo", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            logger.error("Ha ocurrido un error al intentar abrir el comprobante. Detalles:" + e.getMessage() + ". "+ e);
        }
    }
    public void DefinirEstados(ActionEvent actionEvent) {
        logger.info("Intento de filtrar por estados");
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
            logger.debug("Estado seleccionado para el filtrado: "+ estadoSeleccionado);
            if (estadoSeleccionado.equals("Seleccionar todos los Equipos")){
                List<Presupuestos> todos= presupuestosDAO.selectAllPresupuestos();
                logger.debug("Cargando los presupuestos");
                tablaPresupuestos.getItems().setAll(todos);
            }else{
                List<Presupuestos> equiposFiltrados = presupuestosDAO.filtrarPorEstadoPresupuesto(presupuestosDAO.obtenerEstadoIntDesdeBD(estadoSeleccionado));
                logger.debug("Cargando los presupuestos");
                tablaPresupuestos.getItems().setAll(equiposFiltrados);
            }
        });
    }
}

