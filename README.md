# Sabor del Himalaya

Aplicación web de pedidos de comida a domicilio para un restaurante nepalí. Proyecto del Grupo 16 de la asignatura **Ingeniería del Software II** (UPM, ETSI Informáticos, curso 2026-27), desarrollado con Scrum en dos sprints.

Los clientes se registran, consultan la carta y el menú del día, hacen pedidos y siguen su estado. Pueden modificar o cancelar un pedido durante los 10 minutos siguientes a su confirmación, siempre que el restaurante no haya empezado a prepararlo. El personal del restaurante gestiona la carta y el menú diario, actualiza el estado de los pedidos y consulta la facturación de cada día.

## Tecnologías

- Java 17 y Spring Boot 3.3.5 (Spring MVC, Spring Data JPA y Spring Mail para el correo de activación)
- Thymeleaf para las vistas, con HTML y CSS propios y sin frameworks de front-end
- H2 como base de datos: en fichero para la aplicación y en memoria para las pruebas
- JUnit 5 y Spring Boot Test
- Maven

## Funcionalidad

| Historia | Descripción | Sprint |
|---|---|---|
| US1 | Gestionar la carta: alta, modificación y retirada de productos por categorías (entrantes, principales, postres y bebidas) | 1 |
| US2 | Configurar el menú del día: 3 entrantes, 3 principales, 2 postres y 1 bebida, a precio fijo de 15 € | 1 |
| US5 | Registro de clientes | 1 |
| US6 | Activación de la cuenta mediante un enlace | 1 |
| US7 | Inicio y cierre de sesión | 1 |
| US3 | El personal actualiza el estado de los pedidos: Recibido → En preparación → En reparto → Entregado | 2 |
| US4 | Facturación diaria (los pedidos cancelados no cuentan) | 2 |
| US8 | Explorar la carta agrupada por categorías y el menú del día | 2 |
| US9 | Hacer un pedido con productos de la carta y menús del día, con un resumen antes de confirmar | 2 |
| US10 | Consultar los pedidos propios y su estado | 2 |
| US11 | Modificar o cancelar un pedido en los 10 minutos siguientes a su confirmación, mientras siga en estado Recibido | 2 |

## Puesta en marcha

Requisitos: **JDK 17** y **Maven 3.9** (o un IDE con Maven integrado, como IntelliJ IDEA o VS Code con el Extension Pack for Java).

```bash
mvn spring-boot:run
```

La aplicación queda en <http://localhost:8080>. Para usar otro puerto:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --app.url-base=http://localhost:8081"
```

- La primera vez que arranca se carga una carta de ejemplo (se desactiva con `app.datos-iniciales=false`).
- La base de datos se guarda en `data/`. Se puede consultar en <http://localhost:8080/h2-console> con la URL `jdbc:h2:file:./data/sabor`, el usuario `sa` y la contraseña vacía.
- **Correo de activación:** el enlace se escribe en la consola y, en desarrollo, también se muestra en la pantalla de registro (`app.mostrar-enlace-activacion=true`). Para enviar correos de verdad, hay que rellenar las propiedades `spring.mail.*` que están comentadas en `application.properties`.

### Recorrido rápido

1. En **Carta (personal)** y **Menú diario (personal)**, revisar la carta y configurar el menú de hoy.
2. **Registrarse**, abrir el enlace de activación y **entrar**.
3. En **Mis pedidos → Hacer un pedido**, elegir cantidades, revisar el resumen y confirmar.
4. Durante 10 minutos, y mientras siga en Recibido, el pedido se puede **modificar** o **cancelar** desde su detalle.
5. En **Pedidos (personal)**, cambiar el estado. En **Facturación (personal)**, ver el total del día.

## Pruebas

```bash
mvn test
```

Hay 42 pruebas unitarias (CPU-01 a CPU-42) en `src/test/java`, que usan una base de datos H2 en memoria:

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `CartaServiceTest` | 6 | Alta, modificación y retirada de productos (US1) |
| `MenuServiceTest` | 6 | Configuración y consulta del menú del día (US2) |
| `ClienteServiceTest` | 8 | Registro, activación e inicio de sesión (US5-US7) |
| `CifradoContrasenasTest` | 1 | Hash de contraseñas con sal |
| `PedidoServiceTest` | 17 | Pedidos, estados, plazo de 10 minutos (también con el pedido ya en preparación) y facturación (US3, US4, US9-US11) |
| `CantidadesPedidoTest` | 2 | Lectura de las cantidades del formulario de pedido |
| `CartaPorCategoriaTest` | 2 | Carta agrupada por categorías (US8) |

El plazo de 10 minutos se prueba sin esperar: `PedidoService` recibe un `java.time.Clock` inyectado (`RelojConfig`) y las pruebas usan un reloj controlable (`RelojDePrueba`) que se adelanta a voluntad (5 min, exactamente 10 min, 10 min + 1 s, 11 min).

## Estructura

```
src/main/java/es/upm/g16/sabor/
├── config/        Reloj de la aplicación y datos iniciales de la carta
├── modelo/        Entidades JPA: Producto, Categoria, MenuDiario, Cliente, Pedido, LineaPedido, EstadoPedido
├── repositorio/   Repositorios Spring Data
├── servicio/      Reglas de negocio (CartaService, MenuService, ClienteService, PedidoService...)
└── web/           Controladores MVC
src/main/resources/
├── templates/     Vistas Thymeleaf (carta, cliente, menu, pedido, personal, fragmentos comunes)
└── static/css/    Hoja de estilos
herramientas/
└── contar_loc.py  Recuento de líneas de código del producto
```

Más detalle sobre rutas, modelo y reglas de negocio en [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md).

## Recuento de LOC

```bash
python3 herramientas/contar_loc.py .
```

Cuenta las líneas físicas con código (sin líneas en blanco ni líneas que solo tienen comentarios) por tipo de fichero.
