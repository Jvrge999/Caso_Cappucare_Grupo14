# 📦 Cappucare Stock — Control Inteligente de Existencias (DSY1105)

Aplicación móvil Android desarrollada con **Kotlin**, **Jetpack Compose**, **Material Design 3** y arquitectura **MVVM** para el caso de Vinculación con el Medio (VcM) **Cappucare** — Sede San Bernardo (Sección 001).

## 👥 Integrantes del Equipo (Grupo 14)
* **Jorge Aguilera**
* **Camila Henríquez**
* **Gustavo Soto**
* **Priscila Calderón**

## 🔗 Enlace al Repositorio GitHub
* **Repositorio oficial:** `https://github.com/Jvrge999/Caso_Cappucare_Grupo14.git`

---

## 🚀 Cómo ejecutar la aplicación
1. Clonar el repositorio o descomprimir el archivo `.zip` del proyecto.
2. En **Android Studio**, ir a `File > Open...` y seleccionar la carpeta `Seccion001_Cappucare` (la que contiene `settings.gradle.kts`).
3. Verificar que el **Gradle JDK** esté configurado en **JDK 17 o JDK 21** (`Settings > Build, Execution, Deployment > Build Tools > Gradle`).
4. Esperar a que finalice el **Gradle Sync** y presionar el botón **Run ('app')** sobre un emulador o dispositivo físico con **Android 7.0 (API 24)** o superior.

---

## 🔐 Usuarios de Prueba (Datos Ficticios - RNF-04)

| Perfil | Usuario (Correo) | Contraseña | Permisos principales |
| :--- | :--- | :--- | :--- |
| **Personal encargado** | `encargado@cappucare.cl` | `encargado123` | Gestión completa de catálogo, estados, ingresos, salidas y alertas. |
| **Usuario autorizado** | `ventas@cappucare.cl` | `ventas123` | Consulta de stock disponible, historial y registro de movimientos comerciales. |

---

## ✅ Requerimientos y Reglas de Negocio Implementadas
En esta entrega el equipo se enfocó en el cumplimiento íntegro de la regla de negocio asignada **RN-03 ("El stock nunca queda negativo")** y su flujo asociado:

* **RN-03 (El stock nunca queda negativo):** Validación de doble barrera en `CappucareViewModel` (`validarMovimiento()` y `registrarMovimiento()`). Impide registrar salidas por una cantidad superior al stock disponible del producto, informando en pantalla `"Solo hay N disponibles"`.
* **RN-01 y RF-07 (Stock derivado de movimientos):** Cuadratura de datos iniciales en `MockData.kt` (`M1` a `M11`) y actualización automática del stock disponible y del estado (`DISPONIBLE` / `AGOTADO`) tras cada ingreso o salida.
* **RN-02 y RF-05 (Registro de salidas y ventas):** Exigencia de motivo obligatorio en cada salida (`VENTA`, `MERMA`, `VENCIMIENTO`, `AJUSTE`, `OTRO`).
* **RN-05, RF-04 y RF-06 (Trazabilidad de ingresos y salidas):** Asignación automática de fecha/hora (`yyyy-MM-dd HH:mm`) y del nombre del usuario con sesión activa como responsable del movimiento.
* **RF-00, RF-01, RF-02, RF-03, RF-08, RF-09 y RF-10:** Autenticación de usuarios, gestión de productos con validación de código único (`RN-04`), filtros por categoría/búsqueda, historial ordenado por fecha descendente y detección de productos bajo el nivel mínimo (`RN-07`).

---

## 🏛️ 1. Contexto del Caso, Modelo de Datos y Persistencia Local (Criterio D3)

### Problemática abordada y Regla de Negocio Principal (RN-03)
El sistema resuelve el descontrol de inventario de insumos médicos mediante la implementación estricta de la **Regla de Negocio RN-03 ("El stock nunca queda negativo")**, conectada directamente con:
* **RF-05 (Registrar salida de existencias):** Toda salida exige cantidad válida y motivo obligatorio.
* **RF-07 (Actualización automática de stock):** El stock disponible nunca se edita manualmente; se calcula como `Ingresos - Salidas`.

### Modelo de Datos y Estructura para Persistencia con Room (SQLite)
* **`data/MockData.kt`:** Contiene el catálogo inicial de 8 insumos (`P1` a `P8`) y **11 movimientos cuadrados (`M1` a `M11`)** para que la suma histórica coincida exactamente con el `cantidadDisponible` inicial de cada producto.
* **`model/Producto.kt` y `model/Movimiento.kt`:** Clases de datos (`data class`) diseñadas como futuras entidades `@Entity(tableName = "productos")` y `@Entity(tableName = "movimientos")` de **Room (SQLite)**, vinculadas mediante `productoId` como clave foránea (`ForeignKey`).
* **`data/local/PersistenciaPendiente.kt`:** Define la hoja de ruta de persistencia local (`ProductoDao`, `MovimientoDao`, `AppDatabase` y `Repositorio`), donde las consultas reactivas retornan `Flow<List<Producto>>` y las escrituras se ejecutan mediante funciones `suspend`.

---

## 📱 2. Interfaz de Usuario, Navegación y Recursos Nativos (Criterios D4 y D5)

### Diseño con Jetpack Compose y Material 3
* **`navigation/AppNavigation.kt`:** Centraliza el `NavHost` y crea una única instancia compartida de `CappucareViewModel` para mantener el estado sincronizado entre todas las pantallas y limpiar la pila de navegación al cerrar sesión (`logout()`).
* **`ui/screen/RegistrarMovimientoScreen.kt`:** Pantalla principal de nuestro flujo. Observa los cambios del ViewModel en tiempo real mediante `collectAsState()` (`val productos by viewModel.productos.collectAsState()`).
* **Separación UI / Lógica:** Los `@Composable` solo dibujan el estado y muestran errores bajo cada campo (`isError = errores.cantidad != null`, `supportingText`); **ningún cálculo de stock se hace dentro de la pantalla**.

### Recurso Nativo y Controles del Dispositivo (RNF-01, RNF-02)
* **Controles de Entrada Nativos (`KeyboardOptions`):** Configuración del teclado numérico nativo del sistema (`KeyboardType.Number`) en los campos de cantidad y stock mínimo para prevenir ingreso de caracteres alfabéticos desde el sistema operativo.
* **Almacenamiento Local Offline-First:** Arquitectura preparada para persistencia local en el almacenamiento interno del dispositivo mediante SQLite/Room sin depender de conexión a internet (`RNF-01`).

---

## 🔄 3. Arquitectura MVVM y Cómo Viaja el Dato (Criterio D2)

El proyecto aplica el patrón **MVVM (Model - View - ViewModel)** garantizando que los datos fluyan de manera unidireccional:

1. **Evento en la Vista (`UI`):** En `RegistrarMovimientoScreen.kt`, el usuario selecciona un producto, ingresa la cantidad de salida y presiona el botón **"Registrar movimiento"**.
2. **Primera Barrera en ViewModel (`validarMovimiento`):** Antes de procesar, la UI invoca `viewModel.validarMovimiento(productoId, tipo, cantidad, motivo)` en `CappucareViewModel.kt`.
   * Si `tipo == TipoMovimiento.SALIDA` y `cantNum > prod.cantidadDisponible`, se activa **RN-03** devolviendo el objeto `ErroresMovimiento(cantidad = "Solo hay ${prod.cantidadDisponible} disponibles")`.
   * La interfaz recibe este error inmediatamente y bloquea el guardado mostrando el mensaje en rojo debajo del campo.
3. **Segunda Barrera y Actualización (`registrarMovimiento`):** Si la validación no arroja errores, la UI invoca `viewModel.registrarMovimiento(...)`:
   * Aplica un segundo control de seguridad (`if (tipo == TipoMovimiento.SALIDA && cantidad > prod.cantidadDisponible) return`).
   * Obtiene fecha/hora actual (`SimpleDateFormat("yyyy-MM-dd HH:mm")`) y el nombre del usuario en sesión (`_usuarioActivo.value?.nombre`).
   * Agrega el movimiento a `_movimientos` y calcula `nuevaCantidad = prod.cantidadDisponible - cantidad`.
   * Si `nuevaCantidad == 0`, cambia automáticamente el estado del producto a `EstadoProducto.AGOTADO`.
4. **Reacción Automática en Pantalla (`StateFlow`):** Al actualizar `_productos.value`, todas las pantallas suscritas con `collectAsState()` se redibujan automáticamente mostrando el nuevo stock sin recargar la app.

---

## 🧪 4. Guion Paso a Paso para la Demostración en Vivo (Criterio D1)

Seguir estos 5 pasos en orden durante la presentación para demostrar todos los casos de uso y validaciones:

### Paso 1: Inicio de Sesión (RF-00)
1. En la pantalla de Login, ingresar con las credenciales de prueba:
   * **Usuario:** `encargado@cappucare.cl` | **Clave:** `encargado123` *(o presionar "Entrar como Encargado - modo desarrollo")*.
2. Mostrar brevemente el **Panel Principal**, destacando los contadores superiores (**Total productos: 8**, **Bajo mínimo: 3**, **Agotados: 1**).

### Paso 2: Demostración de Bloqueo por Regla RN-03 (Caso de Error Principal)
1. En el Panel Principal, tocar el botón **"Registrar movimiento"**.
2. En el selector **Producto**, elegir **`PRO-002 · Mascarilla quirúrgica`** *(mostrar que el texto inferior indica: `"Disponible actualmente: 3 caja"`)*.
3. Cambiar el tipo de movimiento a **"Salida"**.
4. **Prueba de motivo vacío (RN-02):** Escribir **`2`** en Cantidad, dejar el campo **Motivo** sin seleccionar y tocar **"Registrar movimiento"** → *Mostrar que aparece en rojo `"Elige el motivo de la salida"`*.
5. **Prueba de bloqueo RN-03 (Stock insuficiente):** En Cantidad escribir **`5`** (superando las 3 disponibles), elegir motivo **"Venta"** y tocar **"Registrar movimiento"**.
   * **Resultado demostrado:** La app **bloquea el registro** y muestra en rojo bajo el campo Cantidad: **`"Solo hay 3 disponibles"`**, impidiendo que el stock quede en `-2`.

### Paso 3: Demostración de Salida Válida hasta Agotar Stock (RF-05 y RF-07)
1. En la misma pantalla con `PRO-002 · Mascarilla quirúrgica` seleccionada, cambiar la Cantidad de **`5`** a **`3`**.
2. Escribir en Observación: `"Venta mostrador turno mañana"` y presionar **"Registrar movimiento"**.
3. **Resultado demostrado:** El registro es exitoso, la app regresa al Panel Principal y el contador superior de **Agotados** sube automáticamente de `1` a **`2`**.

### Paso 4: Verificación de Cambio Automático de Estado y Reactivación
1. Entrar a **"Ver productos"** desde el Panel y buscar **`PRO-002 · Mascarilla quirúrgica`**.
2. Mostrar que ahora indica **`0 caja`** y su etiqueta cambió automáticamente de *Disponible* a **`Agotado`**.
3. Volver a **"Registrar movimiento"**, elegir `PRO-002`, dejar tipo **"Ingreso"**, poner cantidad **`10`** y registrar → Mostrar que vuelve a **`10 caja`** y recupera automáticamente el estado **`Disponible`**.

### Paso 5: Verificación de Trazabilidad en el Historial (RF-10 y RN-05)
1. Desde el Panel Principal, entrar a **"Historial de movimientos"**.
2. Mostrar que arriba del todo aparecen los movimientos recién creados (ordenados por fecha descendente), incluyendo la fecha/hora exacta, el motivo **Venta** y el responsable **`Encargada de bodega (demo)`**.

---

## 🛠️ 5. Guía Técnica de Extensibilidad y Modificaciones en Tiempo Real (Criterio D6)

Documentación de referencia rápida sobre los puntos de extensión en `CappucareViewModel.kt` y `Movimiento.kt` para ajustes de reglas de negocio en tiempo real:

### Modificación A: Personalizar el mensaje de error de RN-03 (incluir nombre y unidad de medida)
* **Archivo:** `app/src/main/java/com/cappucare/app/viewmodel/CappucareViewModel.kt`
* **Función:** `validarMovimiento(...)` (alrededor de la línea 175)
* **Implementación:** Reemplazar la condición de `RN-03` dentro de `val errorCant = when`:
  ```kotlin
  // Antes:
  tipo == TipoMovimiento.SALIDA && prod != null && cantNum > prod.cantidadDisponible ->
      "Solo hay ${prod.cantidadDisponible} disponibles"

  // Después (incluyendo nombre del producto y etiqueta de unidad):
  tipo == TipoMovimiento.SALIDA && prod != null && cantNum > prod.cantidadDisponible ->
      "Stock insuficiente de ${prod.nombre}: quedan ${prod.cantidadDisponible} (${prod.unidadMedida.etiqueta})"
  ```

### Modificación B: Agregar un tope máximo de unidades por cada salida (ej. máximo 20 unidades por operación)
* **Archivo:** `app/src/main/java/com/cappucare/app/viewmodel/CappucareViewModel.kt`
* **Función:** `validarMovimiento(...)` (dentro de `val errorCant = when`, línea 173)
* **Implementación:** Agregar una nueva rama de validación en el bloque `when`:
  ```kotlin
  val errorCant = when {
      cantNum == null || cantNum <= 0 -> "Ingresa una cantidad mayor que 0"
      tipo == TipoMovimiento.SALIDA && prod != null && cantNum > prod.cantidadDisponible ->
          "Solo hay ${prod.cantidadDisponible} disponibles"
      // Nueva regla: limitar cantidad máxima permitida en una sola salida
      tipo == TipoMovimiento.SALIDA && cantNum > 20 ->
          "No se permiten salidas mayores a 20 unidades por operación"
      else -> null
  }
  ```

### Modificación C: Extensión de motivos de salida y restricción por estado de producto

#### Caso C.1 — Agregar un nuevo motivo de salida al selector desplegable
* **Archivo:** `app/src/main/java/com/cappucare/app/model/Movimiento.kt`
* **Clase:** `enum class MotivoSalida`
* **Implementación:** Agregar el nuevo valor al enumerado (la interfaz lo carga automáticamente en el `DropdownMenu`):
  ```kotlin
  enum class MotivoSalida(val etiqueta: String) {
      VENTA("Venta"),
      MERMA("Merma / daño"),
      VENCIMIENTO("Vencimiento"),
      AJUSTE("Ajuste de inventario"),
      PRESTAMO("Préstamo interno"), // Nuevo motivo de salida
      OTRO("Otro")
  }
  ```

#### Caso C.2 — Bloquear movimientos sobre productos con estado `DADO_DE_BAJA`
* **Archivo:** `app/src/main/java/com/cappucare/app/viewmodel/CappucareViewModel.kt`
* **Función:** `validarMovimiento(...)` (variable `val errorProd`)
* **Implementación:** Reemplazar la validación simple de `errorProd` por un bloque `when`:
  ```kotlin
  val errorProd = when {
      prod == null -> "Elige un producto"
      prod.estado == EstadoProducto.DADO_DE_BAJA ->
          "Este producto está dado de baja y no admite movimientos"
      else -> null
  }
  ```
