package com.proyecto.neotec.models;

import javafx.scene.control.Button;

public class Item {
    private Button actionButton;

    public Item() {
        this.actionButton = new Button("Seleccionar");
    }

    public Button getActionButton() {
        return actionButton;
    }

    public void setActionButton(Button actionButton) {
        this.actionButton = actionButton;
    }
}
