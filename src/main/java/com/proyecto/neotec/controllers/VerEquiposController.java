package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.EquipoDAO;
import com.proyecto.neotec.models.Cliente;
import com.proyecto.neotec.models.Equipos;
import com.proyecto.neotec.util.CargarPantallas;
import com.proyecto.neotec.util.VolverPantallas;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class VerEquiposController {

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

    private EquipoDAO equipoDAO;
    private ObservableList<Equipos> equipos;

    // Constructor sin argumentos
    public VerEquiposController() {
    }

    @FXML
    public void initialize() {
        Scene scene = btnMod.getScene();
        VolverPantallas.guardarEscenaAnterior(scene);
        equipoDAO = new EquipoDAO();
        // Cargar datos
        cargarDatos();
    }

    private void cargarDatos() {
        equipos = FXCollections.observableArrayList();
        // Configurar columnas
        columna1.setCellValueFactory(new PropertyValueFactory<>("id"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("idcliente"));
        columna3.setCellValueFactory(cellData -> {
            // Obtener el valor de estado del objeto `Equipos`
            int estadoInt = cellData.getValue().getEstado();
            // Convertir el valor int a una cadena de texto usando el método anterior
            String estadoString = obtenerEstadoString(estadoInt);
            // Retornar como una `SimpleStringProperty` para que la tabla pueda manejarlo
            return new SimpleStringProperty(estadoString);
        });
        columna4.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("dispositivo"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("activo"));
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

    @FXML
    public void EliminarEquipo(ActionEvent actionEvent) {
        // Verifica si hay un equipo seleccionado en la tabla
        Equipos equipo = tablaEquipos.getSelectionModel().getSelectedItem();

        if (equipo != null) {
            int idEquipo = equipo.getId(); // Obtener el ID del equipo seleccionado
            int nuevoEstado = (equipo.getActivo() == 1) ? 0 : 1; // Si está activo, lo desactiva, y viceversa

            // Llamar al método del DAO para cambiar el estado en la base de datos
            equipoDAO.cambiarEstadoActivo(idEquipo, nuevoEstado);

            // Actualizar el estado del equipo en la tabla
            cargarDatos();

            // Mostrar alerta de confirmación
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Activación / Desactivación");
            alert.setHeaderText(null);
            alert.setContentText("Se ha cambiado el estado del equipo " + equipo.getDispositivo() + " del cliente " + equipo.getIdcliente());
            alert.showAndWait();
        } else {
            // Mostrar alerta indicando que no hay un equipo seleccionado
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Activación / Desactivación");
            alert.setContentText("Debe seleccionar un equipo en la tabla");
            alert.showAndWait();
        }
    }


    @FXML
    public void mostrarFormAgregarEquipos(ActionEvent actionEvent) {
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
            e.printStackTrace();
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
        Equipos equipoSeleccionado = tablaEquipos.getSelectionModel().getSelectedItem();

        if (equipoSeleccionado == null) {
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
            stage.setTitle("Modificar Usuario");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal hasta que el pop-up se cierre

            // Establecer el Stage en el controlador
            controller.setStage(stage);

            // Mostrar el pop-up
            stage.showAndWait();

            // Recargar los datos en la tabla después de cerrar el pop-up
            cargarDatos();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void mostrarImagenes() {
        Equipos equipoSeleccionado = tablaEquipos.getSelectionModel().getSelectedItem();
        if (equipoSeleccionado == null) {
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
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar la ventana.", Alert.AlertType.ERROR);
        }
    }
    private String obtenerEstadoString(int estado) {
        switch (estado) {
            case 1:
                return "Ingresado";
            case 2:
                return "En espera de revisión";
            case 3:
                return "Revisión";
            case 4:
                return "En espera de autorización";
            case 5:
                return "Autorizado";
            case 6:
                return "Cancelado";
            case 7:
                return "Reparado";
            case 8:
                return "Pagado";
            default:
                return "Desconocido";
        }
    }

    public void sacarPresupuesto(ActionEvent actionEvent) {
        Equipos equipoSeleccionado = tablaEquipos.getSelectionModel().getSelectedItem();
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
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar la ventana de modificación.", Alert.AlertType.ERROR);
        }
    }
}

