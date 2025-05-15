package com.proyecto.neotec.bbdd;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.naming.Referenceable;

public class DataSourceFactory {
    public static DataSource get(){
        Properties props = new Properties();
        FileInputStream fis = null;
        MysqlDataSource mysqldump = null;
        try{
            //1) abrir el archivo cofig.properties
            fis = new FileInputStream("config.properties");
            props.load(fis);
            //2) contruir el data source con los datos leidos
            mysqldump = new MysqlDataSource();
            mysqldump.setURL(props.getProperty("DB_URL"));
            mysqldump.setUser(props.getProperty("DB_USERNAME"));
            mysqldump.setPassword(props.getProperty("DB_PASSWORD"));
        }catch(IOException e){
            e.printStackTrace();
        }
        return mysqldump;
    }


}
