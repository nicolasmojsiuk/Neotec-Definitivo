package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.EquipoDAO;
import com.proyecto.neotec.models.Equipos;
import com.proyecto.neotec.util.VolverPantallas;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class VerImagenesController {
    public static Equipos equipo;

    @FXML
    private ImageView img1;
    @FXML
    private ImageView img2;
    @FXML
    private ImageView img3;
    @FXML
    private ImageView img4;

    @FXML
    public void initialize() {
        cargarImagenes();
    }

    public void cargarImagenes() {
        EquipoDAO equipodao = new EquipoDAO();
        List<String> listaImagenes = equipodao.obtenerImagenes(equipo);

        // Asegúrate de que no haya más de 4 imágenes en la lista
        int imagenIndex = 0;

        for (String rutaImagen : listaImagenes) {
            File imageFile = new File(rutaImagen);
            if (imageFile.exists()) {
                rutaImagen = "file:/" + imageFile.getAbsolutePath(); // Adaptar ruta para JavaFX
                Image image = new Image(rutaImagen);

                // Asignar la imagen a su correspondiente ImageView
                switch (imagenIndex) {
                    case 0:
                        img1.setImage(image);
                        break;
                    case 1:
                        img2.setImage(image);
                        break;
                    case 2:
                        img3.setImage(image);
                        break;
                    case 3:
                        img4.setImage(image);
                        break;
                    default:
                        break; // Si hay más de 4 imágenes, no las cargamos
                }
                imagenIndex++;
            } else {
                System.out.println("El archivo " + rutaImagen + " no existe.");
            }
        }
    }
    @FXML
    private Button btnVolver;
    public void volver() {
        stage.close();
    }
    Stage stage;
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
