package com.proyecto.neotec.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class TextFieldSoloNumeros {

    public static void allowOnlyNumbers(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {  // \\d* es una expresión regular que permite solo dígitos
                    textField.setText(newValue.replaceAll("[^\\d]", ""));  // Reemplaza cualquier cosa que no sea un dígito
                }
            }
        });
    }
}

