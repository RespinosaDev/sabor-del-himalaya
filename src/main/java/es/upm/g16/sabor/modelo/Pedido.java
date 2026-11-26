package es.upm.g16.sabor.modelo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido a domicilio de un cliente registrado (US9). Guarda la dirección y la fecha y hora
 * exactas de la confirmación, de las que depende el plazo de diez minutos de US11.
 */
@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.RECIBIDO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LineaPedido> lineas = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Cliente cliente, String direccion, LocalDateTime fechaHora) {
        this.cliente = cliente;
        this.direccion = direccion;
        this.fechaHora = fechaHora;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public List<LineaPedido> getLineas() {
        return lineas;
    }

    /** Sustituye todos los conceptos del pedido (alta y modificación). */
    public void reemplazarLineas(List<LineaPedido> nuevas) {
        lineas.clear();
        for (LineaPedido linea : nuevas) {
            linea.setPedido(this);
            lineas.add(linea);
        }
    }

    /** Suma de los importes de todos los conceptos. */
    public BigDecimal getImporteTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LineaPedido linea : lineas) {
            total = total.add(linea.getImporte());
        }
        return total;
    }
}
