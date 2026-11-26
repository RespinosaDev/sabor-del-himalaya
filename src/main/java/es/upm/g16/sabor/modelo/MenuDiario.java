package es.upm.g16.sabor.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Menú del día (US2): tres entrantes, tres principales, dos postres y una bebida
 * por un precio fijo de 15 euros. Si configurado es falso, ese día no hay menú.
 */
@Entity
public class MenuDiario {

    public static final BigDecimal PRECIO_MENU = new BigDecimal("15.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    private boolean configurado;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precio = PRECIO_MENU;

    @ManyToMany
    @JoinTable(name = "menu_entrante")
    private List<Producto> entrantes = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "menu_principal")
    private List<Producto> principales = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "menu_postre")
    private List<Producto> postres = new ArrayList<>();

    @ManyToOne
    private Producto bebida;

    protected MenuDiario() {
    }

    public MenuDiario(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public boolean isConfigurado() {
        return configurado;
    }

    public void setConfigurado(boolean configurado) {
        this.configurado = configurado;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public List<Producto> getEntrantes() {
        return entrantes;
    }

    public List<Producto> getPrincipales() {
        return principales;
    }

    public List<Producto> getPostres() {
        return postres;
    }

    public Producto getBebida() {
        return bebida;
    }

    public void setBebida(Producto bebida) {
        this.bebida = bebida;
    }

    /** Sustituye los platos del menú. */
    public void asignarPlatos(List<Producto> entrantes, List<Producto> principales, List<Producto> postres, Producto bebida) {
        this.entrantes.clear();
        this.entrantes.addAll(entrantes);
        this.principales.clear();
        this.principales.addAll(principales);
        this.postres.clear();
        this.postres.addAll(postres);
        this.bebida = bebida;
    }

    /** Deja el día sin menú */
    public void vaciar() {
        asignarPlatos(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null);
        this.configurado = false;
    }
}
