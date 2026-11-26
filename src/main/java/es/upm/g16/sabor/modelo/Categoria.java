package es.upm.g16.sabor.modelo;

/**
 * Categorías de los productos de la carta y del menú diario.
 */
public enum Categoria {
    ENTRANTE("Entrante / ensalada"),
    PRINCIPAL("Plato principal"),
    POSTRE("Postre"),
    BEBIDA("Bebida");

    private final String etiqueta;

    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
