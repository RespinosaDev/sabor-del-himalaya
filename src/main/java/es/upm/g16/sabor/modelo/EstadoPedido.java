package es.upm.g16.sabor.modelo;

/**
 * Estados por los que pasa un pedido (US3, US9, US10). Un pedido nace Recibido;
 * Cancelado solo lo produce el cliente dentro del plazo permitido (US11).
 */
public enum EstadoPedido {
    RECIBIDO("Recibido"),
    EN_PREPARACION("En preparación"),
    EN_REPARTO("En reparto"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
