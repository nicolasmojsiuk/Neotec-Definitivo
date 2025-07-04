package com.proyecto.neotec.DAO;

import com.proyecto.neotec.bbdd.Database;

import javafx.util.Pair;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;


public class GananciasDAO {
    private static final Logger logger = Logger.getLogger(GananciasDAO.class);

    public List<Float> obtenerInformacion(int periodo, LocalDate desde, LocalDate hasta, int tipoCliente) {
        logger.debug("Iniciando método obtenerInformacion - Periodo: "+periodo +", desde: "+ desde + ", hasta: " + hasta + ", tipo de cliente: " + tipoCliente);
        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
            logger.debug("Usando rango de fechas perzonalizado. Desde:"+fechaInicio + ", hasta:"+fechaFin );
        } else {
            logger.debug("Calculando rango de fechas según periodo seleccionado.");
            switch (periodo) {
                case 1:
                    fechaInicio = fechaFin.minusDays(1);
                    logger.debug("Periodo 1 día - fecha inicio: " + fechaInicio);
                    break;
                case 7:
                    fechaInicio = fechaFin.minusDays(7);
                    logger.debug("Periodo 7 días - fecha inicio: " + fechaInicio);
                    break;
                case 365:
                    fechaInicio = fechaFin.minusDays(365);
                    logger.debug("Periodo 365 días - fecha inicio: " + fechaInicio);
                    break;
                default:
                    logger.error("Periodo no válido:"+ periodo);
                    throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        float totalVentas = 0f;
        float totalCostos = 0f;

        try (Connection con = Database.getConnection()) {
            logger.debug("Conexión a la base de datos establecida");
            if (tipoCliente == 1 || tipoCliente == 3) {
                logger.debug("Procesando tickets de venta para tipoCliente: "+ tipoCliente);
                // 💵 TICKETSVENTA
                String sqlTickets = " SELECT SUM(ptv.cantidad * pr.precioUnitario) AS total_ventas, SUM(ptv.cantidad * pr.precioCosto) AS total_costos FROM ticketsventa tv JOIN productosticketsventas ptv ON tv.idticketsventa = ptv.idventa JOIN productos pr ON ptv.idproducto = pr.idproductos WHERE tv.fechaHora BETWEEN ? AND ? ";

                try (PreparedStatement ps = con.prepareStatement(sqlTickets)) {
                    ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        totalVentas += rs.getFloat("total_ventas");
                        totalCostos += rs.getFloat("total_costos");
                        logger.debug("Resultados tickets - ventas: "+totalVentas+", costos: "+ totalCostos);
                    }else {
                        logger.debug("No se encontraron resultados para tickets en el período especificado");
                    }
                }
            }

            if (tipoCliente == 2 || tipoCliente == 3) {
                logger.debug("Procesando presupuestos aprobados para tipoCliente: "+ tipoCliente);
                // 🧾 PRESUPUESTOS aprobados
                String sqlPresupuestos = "SELECT SUM(pp.cantidadUtilizada * pr.precioUnitario) AS total_ventas, SUM(pp.cantidadUtilizada * pr.precioCosto) AS total_costos FROM presupuestos p JOIN productopresupuesto pp ON p.idpresupuestos = pp.IDpresupuestos JOIN productos pr ON pp.IDproductos = pr.idproductos WHERE p.estado = 4 AND p.fechaHora BETWEEN ? AND ? ";

                try (PreparedStatement ps = con.prepareStatement(sqlPresupuestos)) {
                    logger.debug("Ejecutando consulta presupuestos con parámetros: inicio: "+ fechaInicio+ " , fin: "+  fechaFin);
                    ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        totalVentas += rs.getFloat("total_ventas");
                        totalCostos += rs.getFloat("total_costos");
                        logger.debug("Resultados presupuestos - ventas: "+totalVentas+", costos: "+ totalCostos);
                    }else {
                        logger.debug("No se encontraron resultados para presupuestos en el período especificado");
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Error de SQL al obtener información", e);
        }
        float ganancia = totalVentas - totalCostos;
        logger.debug("Cálculos finales - ganancia: "+ganancia+", totalCostos: "+totalCostos+", totalVentas: "+ totalVentas);
        return Arrays.asList(ganancia, totalCostos, totalVentas);
    }


    public List<Pair<String, Float>> obtenerDatosGrafico(int periodo, LocalDate desde, LocalDate hasta, int tipoCliente) {
        logger.debug("Iniciando método obtenerInformacion - Periodo: "+periodo +", desde: "+ desde + ", hasta: " + hasta + ", tipo de cliente: " + tipoCliente);

        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
            logger.debug("Usando rango de fechas perzonalizado. Desde:"+fechaInicio + ", hasta:"+fechaFin );

        } else {
            switch (periodo) {
                case 1: fechaInicio = fechaFin.minusDays(1);
                    logger.debug("Periodo 1 día - fecha inicio: "+ fechaInicio);
                break;
                case 7: fechaInicio = fechaFin.minusDays(7);
                    logger.debug("Periodo 7 días - fecha inicio: "+ fechaInicio);
                break;
                case 30: fechaInicio = fechaFin.minusDays(30);
                    logger.debug("Periodo 30 días - fecha inicio: "+ fechaInicio);
                break;
                case 180: fechaInicio = fechaFin.minusDays(180);
                    logger.debug("Periodo 108 días - fecha inicio: "+ fechaInicio);
                break;
                case 365: fechaInicio = fechaFin.minusDays(365);
                    logger.debug("Periodo 365 días - fecha inicio: "+ fechaInicio);
                break;

                default:
                    logger.error("Periodo no válido:"+ periodo);
                    throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        List<Pair<String, Float>> datosGrafico = new ArrayList<>();
        logger.debug("Lista datosGrafico inicializada");
        // Calcular días totales
        long totalDias = Duration.between(fechaInicio.atStartOfDay(), fechaFin.atStartOfDay()).toDays();
        logger.debug("Total de días en el rango: "+ totalDias);
        // Determinar unidad e intervalo
        Duration intervalo;
        DateTimeFormatter formato;
        if (totalDias <= 1) {
            intervalo = Duration.ofHours(1);
            formato = DateTimeFormatter.ofPattern("HH:mm");
            logger.debug("Intervalo establecido: 1 hora, formato HH:mm");
        } else if (totalDias <= 30) {
            intervalo = Duration.ofDays(1);
            formato = DateTimeFormatter.ofPattern("dd/MM");
            logger.debug("Intervalo establecido: 1 día, formato dd/MM");
        } else if (totalDias <= 180) {
            intervalo = Duration.ofDays(7);
            formato = DateTimeFormatter.ofPattern("'Sem' w");
            logger.debug("Intervalo establecido: 1 semana, formato 'Sem' w");
        } else {
            intervalo = Duration.ofDays(30);
            formato = DateTimeFormatter.ofPattern("MM/yyyy");
            logger.debug("Intervalo establecido: 1 mes, formato MM/yyyy");
        }

        LocalDateTime desdeIntervalo = fechaInicio.atStartOfDay();
        LocalDateTime hastaLimite = fechaFin.atTime(23, 59);
        logger.debug("Procesando intervalos desde "+desdeIntervalo+" hasta "+ hastaLimite);
        while (desdeIntervalo.isBefore(hastaLimite)) {
            LocalDateTime hastaIntervalo = desdeIntervalo.plusSeconds(intervalo.getSeconds()).minusSeconds(1);

            float totalVentas = 0f;
            float totalCostos = 0f;

            try (Connection con = Database.getConnection()) {
                logger.debug("Conexión a BD establecida para el intervalo actual");
                if (tipoCliente == 1 || tipoCliente == 3) {
                    logger.debug("Consultando tickets de venta para el intervalo");
                    String sql = "SELECT SUM(ptv.cantidad * pr.precioUnitario) AS total_ventas, " +
                            "SUM(ptv.cantidad * pr.precioCosto) AS total_costos " +
                            "FROM ticketsventa tv " +
                            "JOIN productosticketsventas ptv ON tv.idticketsventa = ptv.idventa " +
                            "JOIN productos pr ON ptv.idproducto = pr.idproductos " +
                            "WHERE tv.fechaHora BETWEEN ? AND ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setTimestamp(1, Timestamp.valueOf(desdeIntervalo));
                        ps.setTimestamp(2, Timestamp.valueOf(hastaIntervalo));
                        logger.debug("Ejecutando consulta tickets con parámetros: "+desdeIntervalo+" - "+ hastaIntervalo);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            totalVentas += rs.getFloat("total_ventas");
                            totalCostos += rs.getFloat("total_costos");
                            logger.debug("Resultados tickets - ventas: "+totalVentas+", costos: "+ totalCostos);
                        } else {
                            logger.debug("No se encontraron tickets en este intervalo");
                        }
                    }
                }

                if (tipoCliente == 2 || tipoCliente == 3) {
                    logger.debug("Consultando presupuestos aprobados para el intervalo");
                    String sql = "SELECT SUM(pp.cantidadUtilizada * pr.precioUnitario) AS total_ventas, " +
                            "SUM(pp.cantidadUtilizada * pr.precioCosto) AS total_costos " +
                            "FROM presupuestos p " +
                            "JOIN productopresupuesto pp ON p.idpresupuestos = pp.IDpresupuestos " +
                            "JOIN productos pr ON pp.IDproductos = pr.idproductos " +
                            "WHERE p.estado = 4 AND p.fechaHora BETWEEN ? AND ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setTimestamp(1, Timestamp.valueOf(desdeIntervalo));
                        ps.setTimestamp(2, Timestamp.valueOf(hastaIntervalo));
                        logger.debug("Ejecutando consulta presupuestos con parámetros: "+desdeIntervalo+" - "+ hastaIntervalo);

                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            totalVentas += rs.getFloat("total_ventas");
                            totalCostos += rs.getFloat("total_costos");
                            logger.debug("Resultados presupuestos - ventas: "+totalVentas+", costos: "+ totalCostos);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("Error SQL al procesar intervalo "+desdeIntervalo+" - "+ hastaIntervalo, e);

            }

            float ganancia = totalVentas - totalCostos;
            String etiqueta = formato.format(desdeIntervalo);
            datosGrafico.add(new Pair<>(etiqueta, ganancia));
            logger.debug("Añadido punto al gráfico: "+etiqueta+" - ganancia: "+ ganancia);
            desdeIntervalo = desdeIntervalo.plusSeconds(intervalo.getSeconds());
        }
        logger.debug("Proceso completado. Total de puntos en el gráfico: "+ datosGrafico.size());
        return datosGrafico;
    }
}
