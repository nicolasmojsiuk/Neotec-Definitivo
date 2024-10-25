package com.proyecto.neotec.controllers;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.proyecto.neotec.DAO.ClienteDAO;
import com.proyecto.neotec.DAO.EquipoDAO;
import com.proyecto.neotec.DAO.PresupuestoDAO;
import com.proyecto.neotec.DAO.ProductosDAO;
import com.proyecto.neotec.models.Equipos;

import com.proyecto.neotec.models.Item;
import com.proyecto.neotec.models.Productos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearPresupuestoController {
    private Map<Productos, Spinner<Integer>> spinnerMap = new HashMap<>();
    @FXML
    private TextField clienteNombre;

    @FXML
    private TextField equipoId;

    @FXML
    private TextArea equipoDescripcion;

    // nombre de la tabla
    @FXML
    private TableView<Productos> tablaProductos;
    //columnas:
    @FXML
    private TableColumn<Productos, String> columnaId; // id producto
    @FXML
    private TableColumn<Productos, String> columnaNombre; // nombre producto
    @FXML
    private TableColumn<Productos, Integer> columnaPrecioUnitario; // precio unitario
    @FXML
    private TableColumn<Productos, Integer> columnaCantidadUtilizada; // Cantidad usada
    @FXML
    private TableView<Item> tablaBotones;

    @FXML
    private TableColumn<Item,Button> columnaBotones;

    @FXML
    private TextField costoReparacion;

    @FXML
    private TextField manoDeObra;

    @FXML
    Spinner<Integer> tiempoReparacion = new Spinner<>();

    @FXML
    private TextField totalPresupuestado;
    private Equipos equipo;


    private ClienteDAO clienteDAO;

    private  ObservableList<Productos> productoPresupuesto;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1);
        tiempoReparacion.setValueFactory(valueFactory);
        clienteDAO = new ClienteDAO();

        // Agregar la primera fila con un botón
        tablaBotones.getItems().add(new Item());
        
        // Inicializamos la columnaSeleccionar con un CellFactory
        columnaBotones.setCellFactory(param -> new TableCell<>() {
            private final Button seleccionarButton = new Button("Seleccionar");

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    seleccionarButton.setOnAction(event -> {
                        try {
                            abrirModalSeleccionarProductos();


                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        agregarNuevaFila();
                    });
                    setGraphic(seleccionarButton);
                }
            }
        });
        //Spinner de la tabla productos
        columnaCantidadUtilizada.setCellFactory(param -> new TableCell<Productos, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Productos producto = getTableView().getItems().get(getIndex());

                    // Configurar el spinner para cada celda
                    SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, item != null ? item : 1);
                    spinner.setValueFactory(valueFactory);

                    // Guardar el spinner en el mapa, asociándolo al producto
                    spinnerMap.put(producto, spinner);

                    // Escuchar cambios en el valor del spinner y actualizar el modelo de productos
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        producto.setCantidad(newValue); // Actualiza la cantidad utilizada
                        calcularCostoReparacion(); // Recalcular el costo de reparación
                    });

                    setGraphic(spinner);
                }
            }
        });

        manoDeObra.textProperty().addListener((observable, oldValue, newValue) -> {
            calcularTotalPresupuestado();
        });
    }

    private void calcularCostoReparacion() {
        int total = 0;
        for (Productos producto : productoPresupuesto) {
            total += producto.getPrecioUnitario() * producto.getCantidad();
        }
        costoReparacion.setText(String.valueOf(total));
        calcularTotalPresupuestado(); // Recalcular el total presupuestado
    }

    private void calcularTotalPresupuestado() {
        try {
            int manoObra = Integer.parseInt(manoDeObra.getText());
            int costoReparacionValor = Integer.parseInt(costoReparacion.getText());
            // Calculamos el total y lo asignamos al campo totalPresupuestado
            totalPresupuestado.setText(String.valueOf(costoReparacionValor + manoObra));
        } catch (NumberFormatException e) {
            // En caso de que algún campo no contenga números válidos
            totalPresupuestado.setText("0");
        }
    }

    private void agregarNuevaFila() {
        tablaBotones.getItems().add(new Item());
    }

    private void abrirModalSeleccionarProductos() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/verProductos.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle("Tabla Productos");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        VerProductoController controller = loader.getController();
        //ocultamos los botones crear,modificar, eliminar y además habilitamos el nuevo botón seleccionar de la pantalla productos
        controller.ocultarBotones();
        controller.setStage(stage);
        stage.showAndWait();
        //cargar tabla productos al cerrar la pantalla
        cargarProductoPresupuesto(controller.SeleccionarProductoPresupuesto());
    }

    private void cargarProductoPresupuesto(List<Productos> productosSeleccionados) {
        ObservableList<Productos> productosExistentes = tablaProductos.getItems();
        if (productosExistentes == null || productosExistentes.isEmpty()) {
            productoPresupuesto = FXCollections.observableArrayList();
            tablaProductos.setItems(productoPresupuesto);
        } else {
            productoPresupuesto = productosExistentes; // Asignar la lista existente
        }
        columnaId.setCellValueFactory(new PropertyValueFactory<>("codigoProducto")); // Cambia esto si es necesario
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        columnaPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        columnaCantidadUtilizada.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        for (Productos producto : productosSeleccionados) {
            if (!productoPresupuesto.contains(producto)) { // Evitar duplicados
                productoPresupuesto.add(producto);
                System.out.println("Añadiendo producto: " + producto.getNombreProducto() + " con Código: " + producto.getCodigoProducto());
            } else {
                System.out.println("Producto ya existe en la lista: " + producto.getNombreProducto());
            }
        }
        if (!productoPresupuesto.isEmpty()) {
            int total = 0;
            for (Productos producto : productoPresupuesto) {
                total += producto.getPrecioUnitario();
                System.out.println("Precio unitario de producto: " + producto.getPrecioUnitario());
            }
            costoReparacion.setText(String.valueOf(total));
        }
    }
    private void cargarCampos() {
        //valores por defecto
        manoDeObra.setText("0");
        totalPresupuestado.setText("0");
        costoReparacion.setText("0");
        if (equipo != null) {
            equipoId.setText(String.valueOf(equipo.getDispositivo()));
            equipoDescripcion.setText(equipo.getObservaciones());
            clienteNombre.setText(String.valueOf(clienteDAO.obtenerNombre(equipo.getIdcliente())));
        } else {
            System.out.println("El equipo es nulo");
        }

    }

    private Stage stage; // Variable para el Stage

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEquipo(Equipos equipoSeleccionado) {
        this.equipo = equipoSeleccionado;
        cargarCampos();
    }
    private void realizarConsulta() {

        // Tabla Presupuesto
        int tiempoEstimado = tiempoReparacion.getValue();
        int costo = Integer.parseInt(costoReparacion.getText());
        int manoObra = Integer.parseInt(manoDeObra.getText());
        int total = Integer.parseInt(totalPresupuestado.getText());
        int idequipo = equipo.getId();

        PresupuestoDAO presupuestoDAO = new PresupuestoDAO();
        int idPresupuesto = presupuestoDAO.insertPresupuesto(tiempoEstimado, costo, manoObra, total, idequipo);
        ProductosDAO productosDAO = new ProductosDAO();
        if (idPresupuesto != -1) {
            if (!productoPresupuesto.isEmpty()) {
                for (Productos producto : productoPresupuesto) {
                    Spinner<Integer> spinner = spinnerMap.get(producto);
                    int idProducto = productosDAO.obtenerIDconCodigoProducto(producto.getCodigoProducto());
                    if (spinner != null || idProducto != -1) {
                        int cantidadUtilizada = spinner.getValue();
                        presupuestoDAO.insertProductoPresupuesto(idPresupuesto, idProducto, cantidadUtilizada); // Cambiar ID por código
                        productosDAO.descontarStock(idProducto, cantidadUtilizada); // Cambiar ID por código
                    }else {
                        System.out.println("spinner null o id -1");
                    }
                }
            }
        } else {
            System.out.println("Error al crear el presupuesto");
        }
    }



    public void imprimir(ActionEvent actionEvent) {
        //ocultamos los botones
        tablaBotones.setVisible(false);
        realizarConsulta();
        if (stage != null) {
            try {
                // Obtener la escena actual
                Scene scene = stage.getScene();
                // Crear un WritableImage del tamaño de la escena
                WritableImage image = new WritableImage((int) scene.getWidth(), (int) scene.getHeight());
                // Capturar la escena en la imagen
                scene.snapshot(image);
                // Convertir la imagen en un array de bytes (PNG)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", baos);
                baos.flush();
                byte[] imageInBytes = baos.toByteArray();
                baos.close();
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Seleccionar Carpeta");

                // Abrir el cuadro de diálogo para seleccionar una carpeta
                File selectedDirectory = directoryChooser.showDialog(stage);

                // Verificar si el usuario seleccionó una carpeta
                if (selectedDirectory != null) {
                    String carpetaSeleccionada = selectedDirectory.getAbsolutePath();
                    String pdfPath = carpetaSeleccionada +"/presupuesto.pdf";
                    PdfWriter writer = new PdfWriter(new FileOutputStream(new File(pdfPath)));
                    PdfDocument pdf = new PdfDocument(writer);
                    Document document = new Document(pdf);


                    // Convertir la imagen a un objeto de iText Image
                    ImageData imageData = ImageDataFactory.create(imageInBytes);
                    com.itextpdf.layout.element.Image pdfImage = new com.itextpdf.layout.element.Image(imageData);

                    // Escalar la imagen para que encaje en el PDF
                    pdfImage.scaleToFit(500, 800);  // Ajusta según el tamaño necesario

                    // Añadir la imagen al documento PDF
                    document.add(pdfImage);

                    // Cerrar el documento
                    document.close();
                    mostrarAlerta("Presupuesto","Presupuesto creado con exito!", Alert.AlertType.INFORMATION);
                    stage.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Stage no está inicializado.");
        }
    }


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipodealerta) {
        Alert alert = new Alert(tipodealerta);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
