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



public class GananciasDAO {

    public List<Float> obtenerInformacion(int periodo, LocalDate desde, LocalDate hasta, int tipoCliente) {
        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
        } else {
            switch (periodo) {
                case 1: fechaInicio = fechaFin.minusDays(1); break;
                case 7: fechaInicio = fechaFin.minusDays(7); break;
                case 365: fechaInicio = fechaFin.minusDays(365); break;
                default: throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        float totalVentas = 0f;
        float totalCostos = 0f;

        try (Connection con = Database.getConnection()) {
            if (tipoCliente == 1 || tipoCliente == 3) {
                // 💵 TICKETSVENTA
                String sqlTickets = " SELECT SUM(ptv.cantidad * pr.precioUnitario) AS total_ventas, SUM(ptv.cantidad * pr.precioCosto) AS total_costos FROM ticketsventa tv JOIN productosticketsventas ptv ON tv.idticketsventa = ptv.idventa JOIN productos pr ON ptv.idproducto = pr.idproductos WHERE tv.fechaHora BETWEEN ? AND ? ";

                try (PreparedStatement ps = con.prepareStatement(sqlTickets)) {
                    ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        totalVentas += rs.getFloat("total_ventas");
                        totalCostos += rs.getFloat("total_costos");
                    }
                }
            }

            if (tipoCliente == 2 || tipoCliente == 3) {
                // 🧾 PRESUPUESTOS aprobados
                String sqlPresupuestos = "SELECT SUM(pp.cantidadUtilizada * pr.precioUnitario) AS total_ventas, SUM(pp.cantidadUtilizada * pr.precioCosto) AS total_costos FROM presupuestos p JOIN productopresupuesto pp ON p.idpresupuestos = pp.IDpresupuestos JOIN productos pr ON pp.IDproductos = pr.idproductos WHERE p.estado = 4 AND p.fechaHora BETWEEN ? AND ? ";

                try (PreparedStatement ps = con.prepareStatement(sqlPresupuestos)) {
                    ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        totalVentas += rs.getFloat("total_ventas");
                        totalCostos += rs.getFloat("total_costos");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        float ganancia = totalVentas - totalCostos;
        return Arrays.asList(ganancia, totalCostos, totalVentas);
    }


    public List<Pair<String, Float>> obtenerDatosGrafico(int periodo, LocalDate desde, LocalDate hasta, int tipoCliente) {
        LocalDate fechaInicio;
        LocalDate fechaFin = LocalDate.now();

        if (desde != null && hasta != null) {
            fechaInicio = desde;
            fechaFin = hasta;
        } else {
            switch (periodo) {
                case 1: fechaInicio = fechaFin.minusDays(1); break;
                case 7: fechaInicio = fechaFin.minusDays(7); break;
                case 30: fechaInicio = fechaFin.minusDays(30); break;
                case 180: fechaInicio = fechaFin.minusDays(180); break;
                case 365: fechaInicio = fechaFin.minusDays(365); break;
                default: throw new IllegalArgumentException("Periodo no válido.");
            }
        }

        List<Pair<String, Float>> datosGrafico = new ArrayList<>();

        // Calcular días totales
        long totalDias = Duration.between(fechaInicio.atStartOfDay(), fechaFin.atStartOfDay()).toDays();

        // Determinar unidad e intervalo
        Duration intervalo;
        DateTimeFormatter formato;
        if (totalDias <= 1) {
            intervalo = Duration.ofHours(1);
            formato = DateTimeFormatter.ofPattern("HH:mm");
        } else if (totalDias <= 30) {
            intervalo = Duration.ofDays(1);
            formato = DateTimeFormatter.ofPattern("dd/MM");
        } else if (totalDias <= 180) {
            intervalo = Duration.ofDays(7);
            formato = DateTimeFormatter.ofPattern("'Sem' w");
        } else {
            intervalo = Duration.ofDays(30);
            formato = DateTimeFormatter.ofPattern("MM/yyyy");
        }

        LocalDateTime desdeIntervalo = fechaInicio.atStartOfDay();
        LocalDateTime hastaLimite = fechaFin.atTime(23, 59);

        while (desdeIntervalo.isBefore(hastaLimite)) {
            LocalDateTime hastaIntervalo = desdeIntervalo.plusSeconds(intervalo.getSeconds()).minusSeconds(1);

            float totalVentas = 0f;
            float totalCostos = 0f;

            try (Connection con = Database.getConnection()) {
                if (tipoCliente == 1 || tipoCliente == 3) {
                    String sql = "SELECT SUM(ptv.cantidad * pr.precioUnitario) AS total_ventas, " +
                            "SUM(ptv.cantidad * pr.precioCosto) AS total_costos " +
                            "FROM ticketsventa tv " +
                            "JOIN productosticketsventas ptv ON tv.idticketsventa = ptv.idventa " +
                            "JOIN productos pr ON ptv.idproducto = pr.idproductos " +
                            "WHERE tv.fechaHora BETWEEN ? AND ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setTimestamp(1, Timestamp.valueOf(desdeIntervalo));
                        ps.setTimestamp(2, Timestamp.valueOf(hastaIntervalo));
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            totalVentas += rs.getFloat("total_ventas");
                            totalCostos += rs.getFloat("total_costos");
                        }
                    }
                }

                if (tipoCliente == 2 || tipoCliente == 3) {
                    String sql = "SELECT SUM(pp.cantidadUtilizada * pr.precioUnitario) AS total_ventas, " +
                            "SUM(pp.cantidadUtilizada * pr.precioCosto) AS total_costos " +
                            "FROM presupuestos p " +
                            "JOIN productopresupuesto pp ON p.idpresupuestos = pp.IDpresupuestos " +
                            "JOIN productos pr ON pp.IDproductos = pr.idproductos " +
                            "WHERE p.estado = 4 AND p.fechaHora BETWEEN ? AND ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setTimestamp(1, Timestamp.valueOf(desdeIntervalo));
                        ps.setTimestamp(2, Timestamp.valueOf(hastaIntervalo));
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            totalVentas += rs.getFloat("total_ventas");
                            totalCostos += rs.getFloat("total_costos");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            float ganancia = totalVentas - totalCostos;
            String etiqueta = formato.format(desdeIntervalo);
            datosGrafico.add(new Pair<>(etiqueta, ganancia));

            desdeIntervalo = desdeIntervalo.plusSeconds(intervalo.getSeconds());
        }

        return datosGrafico;
    }


}
