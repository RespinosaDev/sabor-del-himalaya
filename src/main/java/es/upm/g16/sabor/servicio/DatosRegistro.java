package es.upm.g16.sabor.servicio;

/**
 * Datos del formulario de registro de cliente (US5).
 */
public class DatosRegistro {

    private String nombre;
    private String direccion;
    private String email;
    private String login;
    private String contrasena;
    private String confirmacion;

    public DatosRegistro() {
    }

    public DatosRegistro(String nombre, String direccion, String email, String login, String contrasena, String confirmacion) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.login = login;
        this.contrasena = contrasena;
        this.confirmacion = confirmacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}
