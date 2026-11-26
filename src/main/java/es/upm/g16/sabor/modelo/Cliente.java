package es.upm.g16.sabor.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/**
 * Cliente registrado (US5). La cuenta no está activa hasta que se usa el enlace
 * de activación enviado por correo (US6). La contraseña se guarda como hash con sal.
 */
@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, unique = true, length = 40)
    private String login;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 64)
    private String hashContrasena;

    @Column(nullable = false, length = 32)
    private String sal;

    private boolean activo = false;

    @Column(unique = true, length = 64)
    private String tokenActivacion;

    private LocalDateTime fechaRegistro;

    protected Cliente() {
    }

    public Cliente(String nombre, String direccion, String login, String email,
                   String hashContrasena, String sal, String tokenActivacion) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.login = login;
        this.email = email;
        this.hashContrasena = hashContrasena;
        this.sal = sal;
        this.tokenActivacion = tokenActivacion;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public String getSal() {
        return sal;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getTokenActivacion() {
        return tokenActivacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    /** Activa la cuenta y anula el enlace para que no se pueda reutilizar. */
    public void activar() {
        this.activo = true;
        this.tokenActivacion = null;
    }
}
