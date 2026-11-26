# Arquitectura y reglas de negocio

## Capas

La aplicación es un monolito Spring Boot con renderizado en servidor:

```
Navegador ──HTTP──▶ web/ (controladores MVC) ──▶ servicio/ (reglas de negocio) ──▶ repositorio/ (Spring Data JPA) ──▶ H2
                         │                              │
                         ▼                              └── lanza ReglaNegocioException si se incumple una regla
                    templates/ (Thymeleaf)
```

- **Controladores (`web/`)**: leen los parámetros, guardan la sesión del cliente (`HttpSession`) y eligen la vista. Si se incumple una regla de negocio, vuelven a mostrar el formulario con el mensaje de error, o redirigen con un aviso (patrón POST → redirect → GET con atributos *flash*).
- **Servicios (`servicio/`)**: contienen todas las reglas y son transaccionales. Las reglas incumplidas se señalan con `ReglaNegocioException` y su mensaje es el que ve el usuario.
- **Modelo (`modelo/`)**: entidades JPA. El esquema lo genera Hibernate (`ddl-auto=update`).
- **Reloj**: `RelojConfig` publica un `java.time.Clock` que `PedidoService` usa para la hora de los pedidos, de forma que el plazo de 10 minutos se puede probar sin esperar.

## Modelo de datos

```
Producto ──< LineaPedido >── Pedido >── Cliente
   │              │
   │              └──> MenuDiario
   └──< (platos del) MenuDiario
```

| Entidad | Datos principales |
|---|---|
| `Producto` | nombre (máx. 80), descripción (máx. 300), `Categoria`, precio, en carta, retirado |
| `Categoria` | ENTRANTE, PRINCIPAL, POSTRE, BEBIDA |
| `MenuDiario` | fecha, configurado (sí o no), 3 entrantes, 3 principales, 2 postres, 1 bebida, precio (15 €) |
| `Cliente` | nombre, dirección, correo, login, hash y sal de la contraseña, token de activación, activo |
| `Pedido` | cliente, dirección de entrega, fecha y hora de confirmación, `EstadoPedido`, líneas |
| `LineaPedido` | producto **o** menú del día, concepto, cantidad, precio unitario aplicado |
| `EstadoPedido` | RECIBIDO, EN_PREPARACION, EN_REPARTO, ENTREGADO, CANCELADO |

`LineaPedido` guarda el precio del momento del pedido: si después cambia el precio de un producto, los pedidos ya hechos no varían.

## Rutas

### Públicas

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Inicio |
| GET | `/carta` | Carta agrupada por categorías (US8) |
| GET | `/menu-del-dia?fecha=AAAA-MM-DD` | Menú de una fecha (hoy por defecto) (US2, US8) |
| GET/POST | `/registro` | Registro de cliente (US5) |
| GET | `/activar?token=…` | Activación de la cuenta (US6) |
| GET/POST | `/login` | Inicio de sesión (US7) |
| POST | `/logout` | Cierre de sesión |

### Cliente (requieren sesión; sin ella redirigen a `/login`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/cliente` | Zona del cliente |
| GET | `/cliente/pedidos` | Mis pedidos (US10) |
| GET | `/cliente/pedidos/nuevo` | Formulario de pedido (US9) |
| POST | `/cliente/pedidos/revisar` | Resumen con conceptos y total, sin guardar nada (US9) |
| POST | `/cliente/pedidos/confirmar` | Confirma el pedido en estado Recibido (US9) |
| GET | `/cliente/pedidos/{id}` | Detalle y estado (US10) |
| GET/POST | `/cliente/pedidos/{id}/modificar` | Modificar dentro del plazo (US11) |
| POST | `/cliente/pedidos/{id}/cancelar` | Cancelar dentro del plazo (US11) |

Los campos del formulario de pedido se llaman `p_<idProducto>` (cantidad de cada producto) y `menus` (número de menús del día).

### Personal del restaurante

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/personal/carta` | Lista de productos (US1) |
| GET/POST | `/personal/carta/nuevo` | Alta de producto (US1) |
| GET/POST | `/personal/carta/{id}/editar` | Modificación (US1) |
| POST | `/personal/carta/{id}/retirar` | Retirada (US1) |
| GET | `/personal/menu?fecha=…` | Configuración del menú de una fecha (US2) |
| POST | `/personal/menu/guardar` | Guardar el menú (US2) |
| POST | `/personal/menu/sin-configurar` | Marcar un día sin menú (US2) |
| POST | `/personal/menu/plato` | Añadir un plato fuera de carta al menú (US2) |
| GET | `/personal/pedidos` | Todos los pedidos y cambio de estado (US3) |
| POST | `/personal/pedidos/{id}/estado` | Cambiar el estado de un pedido (US3) |
| GET | `/personal/facturacion?fecha=…` | Facturación de un día (hoy por defecto) (US4) |

Las pantallas del personal no tienen autenticación en esta versión.

## Reglas de negocio

**Carta (US1)**
- El nombre, la categoría y el precio son obligatorios. El precio de un producto de la carta debe ser mayor que 0.
- Un producto retirado no se puede modificar ni pedir.

**Menú del día (US2)**
- Exactamente 3 entrantes, 3 principales y 2 postres, sin repetir, más 1 bebida; todos de su categoría y no retirados.
- Precio fijo de 15 €. Un día puede quedar "sin menú configurado", y entonces no se puede pedir menú.

**Clientes (US5-US7)**
- El correo debe tener un formato válido. El login tiene de 3 a 40 caracteres (letras, números, punto, guion o guion bajo) y es único. La contraseña tiene al menos 6 caracteres y debe coincidir con su confirmación.
- La contraseña se guarda como hash SHA-256 con una sal aleatoria por cliente.
- La cuenta no permite iniciar sesión hasta que se activa con el enlace (token aleatorio).

**Pedidos (US9-US11)**
- Un pedido debe tener al menos un producto o un menú. Cada cantidad está entre 1 y 20.
- Solo se pueden pedir productos que estén en la carta y menús de días configurados.
- Antes de confirmar se muestra un resumen con los conceptos y el total.
- Un cliente solo ve sus propios pedidos: el de otro cliente se trata como si no existiera.
- Un pedido se puede **modificar o cancelar hasta 10 minutos después de confirmarlo** (incluido el minuto 10) **y solo mientras siga en Recibido**: si el restaurante ya lo ha pasado a En preparación, En reparto o Entregado, se rechaza el cambio aunque no hayan pasado los 10 minutos. Al modificarlo se recalcula el importe. Un pedido cancelado ya no admite cambios.

**Estados (US3)**
- El personal puede poner los estados Recibido, En preparación, En reparto y Entregado. Cambiar un pedido no afecta a los demás.
- Solo el cliente cancela un pedido, y solo dentro del plazo. Un pedido cancelado no puede cambiar de estado.

**Facturación (US4)**
- Suma el importe de los pedidos de la fecha elegida, sin contar los cancelados, e indica cuántos pedidos se han contado.
