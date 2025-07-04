package com.proyecto.neotec.controllers;


import com.mysql.cj.x.protobuf.MysqlxExpr;
import com.proyecto.neotec.DAO.*;
import com.proyecto.neotec.models.*;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.VolverPantallas;
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
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Logger;
public class VerEquiposController {
    @FXML
    public TextField txtBuscardor;
    @FXML
    public ToggleButton toggleCliente;
    @FXML
    public ToggleButton toggleDispositivo;
    @FXML
    public ToggleButton toggleActivos;
    @FXML
    public DatePicker dateFechaEntrada;
    @FXML
    public ToggleButton toggleInactivos;
    @FXML
    public DatePicker dateFechaSalida;
    @FXML
    public Button btnEstados;
    @FXML
    private Button btnMod;
    @FXML
    private TableView<Equipos> tablaEquipos;
    @FXML
    private TableColumn<Equipos, Integer> columna1;
    @FXML
    private TableColumn<Equipos, String> columna2;
    @FXML
    private TableColumn<Equipos, String> columna3;
    @FXML
    private TableColumn<Equipos, String> columna4;
    @FXML
    private TableColumn<Equipos, String> columna5;
    @FXML
    private TableColumn<Equipos, String> columna6;
    @FXML
    private TableColumn<Equipos, String> columna7;
    @FXML
    private TableColumn<Equipos, String> columna8;
    @FXML
    private TableColumn<Equipos, String> columna9;
    @FXML
    private Pane workspace;
    private static final Logger logger= Logger.getLogger(VerEquiposController.class);
    private ObservableList<Equipos> equipos;

    // Constructor sin argumentos
    public VerEquiposController() {
    }

    @FXML
    public void initialize() {
        Scene scene = btnMod.getScene();
        VolverPantallas.guardarEscenaAnterior(scene);
        // Cargar datos
        cargarDatos();
        txtBuscardor.setDisable(true);
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleCliente.setToggleGroup(toggleGroup);
        toggleDispositivo.setToggleGroup(toggleGroup);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), event -> {
                    String newValue = txtBuscardor.getText().trim();
                    if (!newValue.isEmpty()) {
                        EquipoDAO equipoDAO = new EquipoDAO();
                        List<Equipos> listaequipos = new ArrayList<>();
                        if (toggleDispositivo.isSelected()){
                            listaequipos = equipoDAO.buscarDispositivo(newValue); // Obtener lista de listaequipos
                        }
                        if (toggleCliente.isSelected()){
                            ClienteDAO clienteDAO = new ClienteDAO();
                            List<Cliente> clientes = clienteDAO.buscarClientes(newValue); // Obtener lista de clientes
                            listaequipos = equipoDAO.obtenerEquiposPorClientes(clientes);
                        }
                        tablaEquipos.getItems().setAll(listaequipos);
                    } else {
                        tablaEquipos.getItems().clear();
                    }
                })
        );
        timeline.setCycleCount(1);
        // Listener para detectar cambios en la selección
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                txtBuscardor.setDisable(true);
                txtBuscardor.setText("");
                cargarDatos();
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
                cargarDatos();
            }
        });
        ToggleGroup toggleGroupEstados = new ToggleGroup();
        toggleActivos.setToggleGroup(toggleGroupEstados);
        toggleInactivos.setToggleGroup(toggleGroupEstados);

        toggleGroupEstados.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ToggleButton selectedToggle = (ToggleButton) newValue;
                if (selectedToggle.equals(toggleActivos)) {
                    BuscarActivosInnactivos(1);
                } else if (selectedToggle.equals(toggleInactivos)) {
                    BuscarActivosInnactivos(0);
                }
            }else {
                cargarDatos();
            }
        });
        //evitar ingreso manual para no tener problemas.
        dateFechaEntrada.getEditor().setDisable(true);
        dateFechaEntrada.setEditable(false);

        dateFechaSalida.getEditor().setDisable(true);
        dateFechaSalida.setEditable(false);

        dateFechaEntrada.valueProperty().addListener((observable,oldValue, newValue)->{
            buscarPorFechaEntrada(dateFechaEntrada);
        });
        dateFechaSalida.valueProperty().addListener((observable, oldvalue, newValue) ->{
            buscarPorFechasalida(dateFechaSalida);
        });
    }

    private void cargarDatos() {
        logger.debug("Intento de cargar los datos por pantalla");
        EquipoDAO equipoDAO = new EquipoDAO();
        equipos = FXCollections.observableArrayList();
        // Configurar columnas
        columna1.setCellValueFactory(new PropertyValueFactory<>("id"));
        columna2.setCellValueFactory(cellData -> {
            int idCliente = cellData.getValue().getIdcliente();
            ClienteDAO clienteDAO = new ClienteDAO();
            String nombreCompleto = clienteDAO.obtenerNombreCompletoPorId(idCliente);
            return new SimpleStringProperty(nombreCompleto);
        });
        columna3.setCellValueFactory(cellData -> {// Obtener el valor de estado del objeto `Equipos`
            int estadoInt = cellData.getValue().getEstado();
            // Convertir el valor int a una cadena de texto usando el método anterior
            String estadoString = equipoDAO.obtenerDescripcionEstadoEquipoDesdeBD(estadoInt);
            // Retornar como una `SimpleStringProperty` para que la tabla pueda manejarlo
            return new SimpleStringProperty(estadoString);
        });
        columna4.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("dispositivo"));
        columna6.setCellValueFactory(cellData -> {
            // Obtener el valor activo del objeto `Equipos`
            int activoInt = cellData.getValue().getActivo();
            // Convertir el valor int a una cadena de texto
            String activoString = obtenerActivoString(activoInt);
            // Retornar como una `SimpleStringProperty` para que la tabla lo maneje
            return new SimpleStringProperty(activoString);
        });
        columna7.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));
        columna8.setCellValueFactory(new PropertyValueFactory<>("fechaModificacion"));
        columna9.setCellValueFactory(new PropertyValueFactory<>("fechaSalida"));
        List<Equipos> listaEquipos = equipoDAO.selectAllEquipos();
        // Limpiar la lista observable y agregar los nuevos datos
        equipos.clear();
        equipos.addAll(listaEquipos);
        // Configurar el TableView con la lista observable
        tablaEquipos.setItems(equipos);
    }
    public void buscarPorFechasalida(DatePicker datePicker) {

        LocalDate fecha = datePicker.getValue();
        if (fecha != null) {
            //  mismo formato que espera la base de datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = fecha.format(formatter);
            logger.debug("Intento de filtrar equipo por fecha salida:"+fechaFormateada);
            EquipoDAO equiposDAO = new EquipoDAO();
            List<Equipos> listaEquipos = equiposDAO.buscarFechaSalida(fechaFormateada);
            tablaEquipos.getItems().setAll(listaEquipos);

        }
    }

    public void buscarPorFechaEntrada(DatePicker datePicker) {
        LocalDate fecha = datePicker.getValue();
        if (fecha != null) {
            //  mismo formato que espera la base de datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fechaFormateada = fecha.format(formatter);
            logger.debug("Intento de filtrar equipo por fecha entrada:"+fechaFormateada);
            EquipoDAO equiposDAO = new EquipoDAO();
            List<Equipos> listaEquipos = equiposDAO.buscarPorFechaIngreso(fechaFormateada);
            tablaEquipos.getItems().setAll(listaEquipos);

        }
    }
    private void BuscarActivosInnactivos(int estado) {
        logger.debug("Intento de buscar equipos por estado: "+ estado);
        EquipoDAO equipoDAO = new EquipoDAO();
        List<Equipos> listaEquipos =equipoDAO.filtrarActivoInactivo(estado);
        equipos.clear();
        equipos.addAll(listaEquipos);
        // Configurar el TableView con la lista observable
        tablaEquipos.setItems(equipos);
    }

    @FXML
    public void mostrarFormAgregarEquipos(ActionEvent actionEvent) {
        logger.debug("Intento de cargar formulario Agregar Equipos");
        try {
            // Cargar el archivo FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/agregarEquipos.fxml"));
            Parent root = loader.load();

            // Obtener el controlador
            AgregarEquipoController controller = loader.getController();

            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);

            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Agregar Equipo");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

            // Pasar el stage al controlador
            controller.setStage(stage);

            // Mostrar el pop-up
            stage.showAndWait();
            cargarDatos();
        } catch (IOException e) {
            logger.error("Error, no se pudo cargar el formulario Crear Equipos. Detalles:" + e.getMessage()+ ". "+ e);
        }
    }

    private String obtenerActivoString(int activo) {
        switch (activo) {
            case 1:
                return "Activo";
            case 0:
                return "Inactivo";
            default:
                return "Desconocido";
        }
    }


    @FXML
    public void mostrarFormModificarEquipos(ActionEvent actionEvent) {
        logger.debug("Intento de cargar formulario Modificar Equipos");
        Equipos equipoSeleccionado = tablaEquipos.getSelectionModel().getSelectedItem();
        if (equipoSeleccionado == null) {
            logger.warn("No se ha seleccionado ningún equipo");
            mostrarAlerta("Error", "No se ha seleccionado ningún equipo.", Alert.AlertType.ERROR);
            return; // Salir si no hay selección
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/modificarEquipo.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del archivo FXML
            ModificarEquipoController controller = loader.getController();
            // Pasar el usuario seleccionado al controlador
            controller.setEquipo(equipoSeleccionado);

            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);

            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Modificar Equipo");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

            // Establecer el Stage en el controlador
            controller.setStage(stage);

            // Mostrar el pop-up
            stage.showAndWait();

            // Recargar los datos en la tabla después de cerrar el pop-up
            cargarDatos();
        } catch (IOException e) {
            logger.error("Error, Ha ocurrido una excepción al cargar el formulario Modificar Equipos. Detalles:"+ e.getMessage()+". "+e);
            mostrarAlerta("Error", "Error al cargar la ventana de modificación.", Alert.AlertType.ERROR);
        }
    }


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void sacarPresupuesto(ActionEvent actionEvent) {
        logger.debug("Intento de sacar presupuesto");
        Equipos equipoSeleccionado = tablaEquipos.getSelectionModel().getSelectedItem();
        if (equipoSeleccionado == null){
            logger.warn("Se intento crear el presupuesto sin seleccionar un equipo");
            mostrarAlerta("Error","Debe seleccionar un equipo",Alert.AlertType.ERROR);
        }else {
            PresupuestoDAO pd = new PresupuestoDAO();
            // Obtener todos los presupuestos del equipo
            List<Presupuestos> listaPresupuestos = pd.obtenerPresupuestosPorIdEquipo(equipoSeleccionado.getId());

            logger.debug("Verificando si se puede crear un nuevo presupuesto para el equipo:");
            // Verificar si se puede crear un nuevo presupuesto
            if (puedeCrearNuevoPresupuesto(listaPresupuestos)) {
                //por aca
                EquipoDAO equipoDAO = new EquipoDAO();
                logger.debug("Intento de crear nuevo presupuesto para el equipo:" + equipoDAO.obtenerEquipoPorId(listaPresupuestos.get(0).getIdEquipo()).getDispositivo());

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/crearPresupuestos.fxml"));
                    Parent root = loader.load();
                    // Obtener el controlador del archivo FXML
                    CrearPresupuestoController controller = loader.getController();
                    // Pasar el usuario seleccionado al controlador
                    controller.setEquipo(equipoSeleccionado);

                    // Crear una nueva escena para el pop-up
                    Scene scene = new Scene(root);

                    // Crear un nuevo Stage (ventana) para el pop-up
                    Stage stage = new Stage();
                    stage.setTitle("Sacar Presupuesto");
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

                    // Establecer el Stage en el controlador
                    controller.setStage(stage);

                    // Mostrar el pop-up
                    stage.showAndWait();
                    cargarDatos();
                } catch (IOException e) {
                    logger.error("Error, Ha ocurrido una excepción al cargar el formulario de modificación de equipos. Detalles:"+ e.getMessage()+ ". "+ e);
                    mostrarAlerta("Error", "Error al cargar la ventana de modificación.", Alert.AlertType.ERROR);
                }
            } else {
                String nombreEquipo = equipoSeleccionado.getDispositivo();
                logger.info("No se puede crear un nuevo presupuesto para este equipo." + nombreEquipo + ". Hasta que el presupuesto actual esté cancelado o pagado");
                MostrarAlerta.mostrarAlerta("Sacar Presupuesto","No puede crear un nuevo presupuesto hasta que el actual esté cancelado o pagado", Alert.AlertType.ERROR);
            }
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

    public void DefinirEstados(ActionEvent actionEvent) {
        logger.debug("Intento de filtrar equipos por estados");
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Estado");
        dialog.setHeaderText("Por favor, elige un estado:");

        // Crear el ComboBox con opciones de estado
        ComboBox<String> comboBox = new ComboBox<>();
        List<String> estados = List.of(
                "Seleccionar todos los Equipos","Ingresado", "Revisión", "En espera de autorización",
                "Autorizado para la reparación", "Reparación no autorizada", "Reparado",
                "Entregado"
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
        EquipoDAO equipoDAO= new EquipoDAO();
        resultado.ifPresent(estadoSeleccionado -> {
            logger.debug("Estado seleccionado para filtrar:"+ estadoSeleccionado);
            if (estadoSeleccionado.equals("Seleccionar todos los Equipos")){
                List<Equipos> selectAllEquipos= equipoDAO.selectAllEquipos();
                tablaEquipos.getItems().setAll(selectAllEquipos);
            }else{
                List<Equipos> equiposFiltrados = equipoDAO.filtrarPorEstadoEquipo(equipoDAO.obtenerEstadoEquipoIdDesdeBD(estadoSeleccionado));
                tablaEquipos.getItems().setAll(equiposFiltrados);
            }
        });
    }

    public void mostrarDetalles(ActionEvent actionEvent) {
        logger.debug("Intento de mostrar detalles del equipo");
        Equipos equipoSeleccionado = tablaEquipos.getSelectionModel().getSelectedItem();
        if (equipoSeleccionado == null) {
            logger.warn("No se ha seleccionado ningún equipo para mostrar sus detalles");
            mostrarAlerta("Error", "No se ha seleccionado ningún equipo.", Alert.AlertType.ERROR);
            return;
        }
        VerImagenesController.equipo = equipoSeleccionado;
        //cambiar el contenido de la scene mostrar imagenes ""
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/verImagenes.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del archivo FXML
            VerImagenesController controller = loader.getController();
            // Crear una nueva escena para el pop-up
            Scene scene = new Scene(root);
            // Crear un nuevo Stage (ventana) para el pop-up
            Stage stage = new Stage();
            stage.setTitle("Ver Imagenes");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

            // Establecer el Stage en el controlador
            controller.setStage(stage);

            // Mostrar el pop-up
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error, Ha ocurrido una excepción al cargar la ventana Ver Detalles: "+ e.getMessage() + ". "+e);
            mostrarAlerta("Error", "Error al cargar la ventana.", Alert.AlertType.ERROR);
        }
    }
    public void entregarEquipo(ActionEvent actionEvent) {
        logger.debug("Intento de entregar equipo");
        Equipos equipoSeleccionado = tablaEquipos.getSelectionModel().getSelectedItem();
        PresupuestoDAO pd = new PresupuestoDAO();
        EquipoDAO equipoDAO = new EquipoDAO();

        if (equipoSeleccionado == null){
            logger.warn("Intento de entregar equipo sin seleccionar uno en especifico");
            mostrarAlerta("Error", "Por favor seleccione un equipo",Alert.AlertType.ERROR);
        }else if (equipoSeleccionado.getEstado() == 7){
            logger.warn("Intento de entregar un equipo que ha sido entregado anteriormente");
            mostrarAlerta("Entregar Equipo", "Este equipo ya fue entregado",Alert.AlertType.INFORMATION);
        }else if (!pd.existePresupuestoParaEquipo(equipoSeleccionado.getId())) {
            logger.warn("Intento de entregar equipo sin presupuestos asociados");
            mostrarAlerta("Entregar Equipo", "El equipo no tiene presupuestos asociados",Alert.AlertType.INFORMATION);
        }else {
            List<Presupuestos> listaPresupuestos = pd.obtenerPresupuestosPorIdEquipo(equipoSeleccionado.getId());
            // Listas para clasificar los presupuestos
            List<Integer> pendientesDePago = new ArrayList<>();
            List<Integer> porResolver = new ArrayList<>();
            List<Integer> correctos = new ArrayList<>();
            boolean puedeEntregarse = true;

            for (Presupuestos pr : listaPresupuestos) {
                int estado = pr.getEstado();
                int id = pr.getIdpresupuesto();

                switch (estado) {
                    case 2: // Aprobado
                        if (!pd.verificarEstadoPagado(id)) {
                            pendientesDePago.add(id);
                            puedeEntregarse = false;
                        } else {
                            correctos.add(id);
                        }
                        break;

                    case 4: // Pagado
                        correctos.add(id);
                        break;

                    case 3: // Cancelado
                        correctos.add(id);
                        break;

                    default: // En espera o estado no válido
                        porResolver.add(id);
                        puedeEntregarse = false;
                        break;
                }
            }

            // Lógica de entrega y alertas
            if (puedeEntregarse && !listaPresupuestos.isEmpty()) {
                // Todos los presupuestos están en estado correcto
                equipoDAO.actualizarEstadoEquipo(equipoSeleccionado.getId(), 7);
                logger.debug("El equipo "+equipoDAO.obtenerEquipoPorId(listaPresupuestos.get(0).getIdEquipo()).getDispositivo()+" ha sido entregado con exito");
                mostrarAlerta("Entregar Equipo", "El equipo ha sido entregado", Alert.AlertType.INFORMATION);
                cargarDatos();
            } else {

                logger.debug("El equipo "+equipoDAO.obtenerEquipoPorId(listaPresupuestos.get(0).getIdEquipo()).getDispositivo() +" no se puede entregar. Hay presupuestos pendientes o por resolver");
                // Hay presupuestos pendientes o por resolver
                StringBuilder mensaje = new StringBuilder();
                String titulo = "No se puede entregar el equipo";
                Alert.AlertType tipo = Alert.AlertType.WARNING;

                if (!pendientesDePago.isEmpty() && !porResolver.isEmpty()) {
                    mensaje.append("Existen presupuestos que impiden la entrega:\n\n");
                    mensaje.append("- Pendientes de pago (IDs: ").append(pendientesDePago).append(")\n");
                    mensaje.append("- Por resolver (IDs: ").append(porResolver).append(")\n\n");
                    mensaje.append("Los presupuestos pendientes deben pagarse o cancelarse.\n");
                    mensaje.append("Los presupuestos por resolver deben aprobarse o cancelarse.");
                } else if (!pendientesDePago.isEmpty()) {
                    mensaje.append("No se puede entregar el equipo:\n\n");
                    mensaje.append("Existen presupuestos aprobados pendientes de pago (N°: ").append(pendientesDePago).append(")\n\n");
                    mensaje.append("Deben pagarse o cancelarse para proceder con la entrega.");
                } else if (!porResolver.isEmpty()) {
                    mensaje.append("No se puede entregar el equipo:\n\n");
                    mensaje.append("Existen presupuestos sin estado definido (IDs: ").append(porResolver).append(")\n\n");
                    mensaje.append("Deben aprobarse o cancelarse para proceder con la entrega.");
                }
                // Mostrar alerta solo si hay problemas
                if (mensaje.length() > 0) {
                    mostrarAlerta(titulo, mensaje.toString(), tipo);
                }
            }
        }
    }
}
