package es.upm.g16.sabor.servicio;

import es.upm.g16.sabor.modelo.Categoria;
import es.upm.g16.sabor.modelo.Producto;

import java.math.BigDecimal;

/**
 * Datos del formulario de alta y modificación de productos (US1).
 */
public class DatosProducto {

    private String nombre;
    private String descripcion;
    private Categoria categoria;
    private BigDecimal precio;
    private boolean enCarta = true;

    public DatosProducto() {
    }

    public DatosProducto(String nombre, String descripcion, Categoria categoria, BigDecimal precio, boolean enCarta) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.enCarta = enCarta;
    }

    public static DatosProducto desde(Producto p) {
        return new DatosProducto(p.getNombre(), p.getDescripcion(), p.getCategoria(), p.getPrecio(), p.isEnCarta());
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isEnCarta() {
        return enCarta;
    }

    public void setEnCarta(boolean enCarta) {
        this.enCarta = enCarta;
    }
}
