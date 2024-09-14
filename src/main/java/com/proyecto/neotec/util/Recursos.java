package com.proyecto.neotec.util;


import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Recursos {
    // ruta del directorio de recursos desde la carpeta actual
    private final static String CARPETA_RECURSOS ="scr/main";
    public static URL url (String rutaRecursos){
        String ruta = (CARPETA_RECURSOS+"/resources/"+rutaRecursos).replace('/', File.separatorChar);
        try {
            return new File(ruta).getCanonicalFile().toURI().toURL();
        }catch (IOException e){
            System.err.println("no se puede obtener url para: "+rutaRecursos);
            return null;
        }
    }
}
