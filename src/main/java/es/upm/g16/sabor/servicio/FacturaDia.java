package es.upm.g16.sabor.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado de la consulta de facturación diaria (US4): pedidos contados y su importe total.
 */
public record FacturaDia(LocalDate fecha, int numeroPedidos, BigDecimal total) {
}
