package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.*;
import com.proyecto.neotec.models.Caja;
import com.proyecto.neotec.models.Equipos;
import com.proyecto.neotec.models.Presupuestos;
import com.proyecto.neotec.models.Productos;
import com.proyecto.neotec.util.CajaEstablecida;
import com.proyecto.neotec.util.MostrarAlerta;
import com.proyecto.neotec.util.SesionUsuario;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class verPresupuestosController {
    @FXML
    private TableView<Presupuestos> tablaPresupuestos;

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
    private ObservableList<Presupuestos> presupuestos;
    @FXML
    private Button btnVerProductosUtilizados;
    @FXML
    private void initialize() {

        cargarDatos();
    }

    private void cargarDatos() {
        PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
        presupuestos = FXCollections.observableArrayList();
        columna1.setCellValueFactory(new PropertyValueFactory<>("idpresupuesto"));
        columna2.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        columna3.setCellValueFactory(new PropertyValueFactory<>("propietario"));
        columna4.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columna5.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        columna6.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        List<Presupuestos> listaPresupuestos= presupuestoDAO.selectAllPresupuestos();
        presupuestos.clear();
        presupuestos.addAll(listaPresupuestos);
        tablaPresupuestos.setItems(presupuestos);
    }

    public void verDetalles() {
        Presupuestos prVer = new Presupuestos();
        prVer = tablaPresupuestos.getSelectionModel().getSelectedItem();
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

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar pago");
            alert.setHeaderText("¿El pago fue con efectivo?");
            alert.setContentText("Seleccione una opción:");

            ButtonType buttonSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
            ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(buttonSi, buttonNo);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == buttonSi) {
                // Verificar que hay una caja establecida
                Caja cajaestablecida = CajaEstablecida.getCajaSeleccionada();
                if (cajaestablecida != null) {
                    System.out.println("ID de Caja: " + cajaestablecida.getIdcaja());
                }
                if (cajaestablecida == null) {
                    MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Debe establecer una caja en Caja -> Establecer Caja.", Alert.AlertType.WARNING);
                }else {
                    // Verificar apertura de caja

                    CajaDAO cajadao = new CajaDAO();

                        int idCaja = cajaestablecida.getIdcaja();
                        System.out.println(idCaja);
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
                            }else {
                                cambiarEstado(pr);
                            }

                            // Confirmación del pago en efectivo
                            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "El pago en efectivo se ha realizado correctamente.", Alert.AlertType.INFORMATION);
                        } else {
                            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "La caja establecida está cerrada", Alert.AlertType.WARNING);
                        }

                }
            } else if (result.isPresent() && result.get() == buttonNo) {
                // Crear el cuadro de diálogo
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Indique el tipo de transacción digital");

                // Crear el campo de texto
                TextField textField = new TextField();
                textField.setPromptText("Escriba aquí...");

                // Agregar el campo de texto al contenido del diálogo
                VBox content = new VBox(10, new Label("Observación:"), textField);
                dialog.getDialogPane().setContent(content);

                // Agregar botones
                ButtonType btnAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
                ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().addAll(btnAceptar, btnCancelar);

                // Obtener el valor ingresado
                dialog.setResultConverter(button -> (button == btnAceptar) ? textField.getText() : null);

                AtomicReference<String> mensaje = new AtomicReference<>("-");

                // Mostrar el cuadro de diálogo y procesar la respuesta
                Optional<String> resultado = dialog.showAndWait();
                resultado.ifPresent(mensaje::set);

                // Registrar la transacción con la observación ingresada
                try {
                    TransaccionesDigitalesDAO tdd = new TransaccionesDigitalesDAO();
                    tdd.registrarTransaccion(0, pr.getPrecioTotal(), 1, mensaje.get());
                    cambiarEstado(pr);
                    // Confirmación de la transacción digital
                    MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "La transacción digital se ha registrado correctamente.", Alert.AlertType.INFORMATION);

                } catch (Exception e) {
                    MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Error al registrar la transacción digital.", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            MostrarAlerta.mostrarAlerta("Pago de Presupuesto", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    public void cambiarEstado(Presupuestos pr){
        ProductosDAO productosDAO = new ProductosDAO();
        List<Productos> productos = productosDAO.obtenerProductosPorPresupuesto(pr.getIdpresupuesto());
        boolean bandera= false;
        //verificar la exitencia y descontar del stock de los productos utilizados en el presupuesto
        for (int i = 0; i < productos.size(); i++) {
            if (productosDAO.productoExiste(productos.get(i).getIdProductos())){
                if (productos.get(i).getCantidad() <= productosDAO.obtenerCantidad(productos.get(i).getIdProductos()) ){
                    productosDAO.descontarStock(productos.get(i).getIdProductos(), productos.get(i).getCantidad());
                }else {
                    bandera = true;
                }
            }
        }
        EquipoDAO equipoDAO= new EquipoDAO();
        // obtener equipo para cambiar su estado
        Equipos equipo = equipoDAO.obtenerEquipoPorId(equipoDAO.obtener_IDequipo_con_idpresupuesto(pr.getIdpresupuesto()));
        if(bandera){
            MostrarAlerta.mostrarAlerta("Error","El presupuesto utiliza productos no disponibles en stock",Alert.AlertType.INFORMATION);
            // Cambiar el estado del equipo a Espera de Autorización debido a la falta de productos en el presupueto
            equipoDAO.actualizarEstadoEquipo(equipo.getId(),3 );
        }else {
            equipoDAO.actualizarEstadoEquipo(equipo.getId(),7);
        }
    }


}

