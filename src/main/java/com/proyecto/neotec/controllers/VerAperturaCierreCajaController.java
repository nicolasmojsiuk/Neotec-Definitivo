package com.proyecto.neotec.controllers;

import com.proyecto.neotec.DAO.CajaDAO;
import com.proyecto.neotec.NeotecMultiplayerAPP;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.proyecto.neotec.models.Caja;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class VerAperturaCierreCajaController {
    @FXML
    public TextField txfEstado;
    @FXML
    public TextField txfFechayhora;
    @FXML
    public TextField txfSaldoInicialFinal;
    @FXML
    public TextField txfSaldoActual;
    @FXML
    public TextField txfResponsable;
    @FXML
    public TextField txfSaldo;
    @FXML
    public Label lblFechahoraApCi;
    @FXML
    public Label lblSaldoInicialFinal;
    @FXML
    public Label lblSaldo;
    @FXML
    public Label lblCaja;
    @FXML
    public Button btnAbrirCerrarCaja;

    public CajaDAO cajaDAO;
    public Caja cajaActual;

    @FXML
    public void initialize(){
        cargarDatosCajaActual();
    }

    private void cargarDatosCajaActual() {
        cajaActual = cajaDAO.selectDatosCajaActual();
        txfEstado.setText(cajaActual.getEstado());
        txfResponsable.setText(cajaActual.getReponsable().getNombre()+", "+cajaActual.getReponsable().getApellido());

        if (cajaActual.getEstado() == "Abierta") {
            lblFechahoraApCi.setText("Fecha y hora Apertura");
            txfFechayhora.setText(cajaActual.getFechayhoraApertura());
            lblSaldoInicialFinal.setText("Saldo Inicial");
            txfSaldoInicialFinal.setText(String.valueOf(cajaActual.getSaldoInicial()));
        }
        if (cajaActual.getEstado() == "Cerrada") {
            lblFechahoraApCi.setText("Fecha y hora Cierre");
            txfFechayhora.setText(cajaActual.getFechayhoraCierre());
            lblSaldoInicialFinal.setText("Saldo Final");
            txfSaldoInicialFinal.setText(String.valueOf(cajaActual.getSaldoFinal()));
        }
    }
    @FXML
    private void verAbrirCerrarCaja() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NeotecMultiplayerAPP.class.getResource("/vistas/abrirCerrarCaja.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Abrir/Cerrar Caja");
        stage.setScene(scene);
        Stage padre = (Stage) txfEstado.getScene().getWindow();
        stage.initOwner(padre);
        stage.initModality(Modality.APPLICATION_MODAL);

        if(cajaActual.getEstado()=="Abierta"){
            lblCaja.setText("Cerrar Caja");
            lblSaldo.setText("Saldo Final");
        }
        if(cajaActual.getEstado()=="Cerrada"){
            lblCaja.setText("Abrir Caja");
            lblSaldo.setText("Saldo inicial");
        }
        stage.show();
    }

    @FXML
    private void abrirCerrarCaja(){
        float saldoInicialFinal = Float.parseFloat(txfSaldo.getText());
        cajaDAO.abrirCerrarCaja(cajaActual,saldoInicialFinal);
    }
}
