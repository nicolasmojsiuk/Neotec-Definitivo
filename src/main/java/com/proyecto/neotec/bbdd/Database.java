package com.proyecto.neotec.bbdd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public class Database {
    public static Connection getConnection() throws SQLException {
        DataSource ds = DataSourceFactory.get();
        return ds.getConnection();
    }
    public static void handleSQLException(SQLException ex){
        for (Throwable e : ex){
            if(e instanceof SQLException){
                Logger logger = LogManager.getLogger(Database.class);
                e.printStackTrace();
                logger.error("Estado SQL: "+((SQLException)e).getSQLState());
                logger.error("Codigo de error: "+((SQLException)e).getErrorCode());
                logger.error("Estado Mensaje: "+e.getMessage());
            }
        }
    }
}
