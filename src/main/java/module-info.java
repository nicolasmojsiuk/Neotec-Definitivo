module com.proyecto.neotec {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires java.sql;
    requires jbcrypt;
    requires org.apache.logging.log4j;
    requires mysql.connector.j;
    requires java.naming;
    requires javafx.web;
    requires kernel;
    requires layout;
    requires java.desktop;
    requires io;
    requires javafx.swing;


    opens com.proyecto.neotec to javafx.fxml;
    exports com.proyecto.neotec;

    opens com.proyecto.neotec.controllers to javafx.fxml;
    exports com.proyecto.neotec.controllers;

    opens com.proyecto.neotec.models to javafx.fxml;
    exports com.proyecto.neotec.models;

}