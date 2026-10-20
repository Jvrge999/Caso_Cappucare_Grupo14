# Cappucare Stock — Control Inteligente de Existencias (DSY1105)

Aplicación móvil Android desarrollada con **Kotlin**, **Jetpack Compose**, **Material Design 3** y arquitectura **MVVM** para el caso de Vinculación con el Medio (VcM) **Cappucare** — Sede San Bernardo (Sección 001).

## 👥 Integrantes del Equipo
* **Jorge Aguilera**
* **Camila Henriquez**
* **Gustavo Soto**
* **Priscila Calderon**

## 🔗 Enlace al Repositorio GitHub
* **Repositorio oficial:** `https://github.com/Jvrge999/Caso_Cappucare_Grupo14.git`

## 🚀 Cómo ejecutar la aplicación
1. Clonar el repositorio o descomprimir el archivo `.zip` del proyecto.
2. En **Android Studio**, ir a `File > Open...` y seleccionar la carpeta `Seccion001_Cappucare` (la que contiene `settings.gradle.kts`).
3. Verificar que el **Gradle JDK** esté configurado en **JDK 17 o JDK 21** (`Settings > Build, Execution, Deployment > Build Tools > Gradle`).
4. Esperar a que finalice el **Gradle Sync** y presionar el botón **Run ('app')** sobre un emulador o dispositivo físico con **Android 7.0 (API 24)** o superior.

## 🔐 Usuarios de Prueba (Datos Ficticios - RNF-04)
| Perfil | Usuario (Correo) | Contraseña | Permisos principales |
| :--- | :--- | :--- | :--- |
| **Personal encargado** | `encargado@cappucare.cl` | `encargado123` | Gestión completa de catálogo, estados, ingresos, salidas y alertas. |
| **Usuario autorizado** | `ventas@cappucare.cl` | `ventas123` | Consulta de stock disponible, historial y registro de movimientos comerciales. |

## ✅ Requerimientos y Reglas de Negocio Implementadas
En esta entrega el equipo se enfocó en el cumplimiento íntegro de la regla de negocio asignada **RN-03 ("El stock nunca queda negativo")** y su flujo asociado:

* **RN-03 (El stock nunca queda negativo):** Validación de doble barrera en `CappucareViewModel` (`validarMovimiento()` y `registrarMovimiento()`). Impide registrar salidas por una cantidad superior al stock disponible del producto, informando en pantalla `"Solo hay N disponibles"`.
* **RN-01 y RF-07 (Stock derivado de movimientos):** Cuadratura de datos iniciales en `MockData.kt` (`M1` a `M11`) y actualización automática del stock disponible y del estado (`DISPONIBLE` / `AGOTADO`) tras cada ingreso o salida.
* **RN-02 y RF-05 (Registro de salidas y ventas):** Exigencia de motivo obligatorio en cada salida (`VENTA`, `MERMA`, `VENCIMIENTO`, `AJUSTE`, `OTRO`).
* **RN-05, RF-04 y RF-06 (Trazabilidad de ingresos y salidas):** Asignación automática de fecha/hora (`yyyy-MM-dd HH:mm`) y del nombre del usuario con sesión activa como responsable del movimiento.
* **RF-00, RF-01, RF-02, RF-03, RF-08, RF-09 y RF-10:** Autenticación de usuarios, gestión de productos con validación de código único (`RN-04`), filtros por categoría/búsqueda, historial ordenado por fecha descendente y detección de productos bajo el nivel mínimo (`RN-07`).

### 🧪 Caso de Prueba Principal (RN-03)
1. Iniciar sesión con cualquiera de los usuarios de prueba.
2. Ir a **Registrar movimiento** y seleccionar el producto `PRO-002 · Mascarilla quirúrgica` (Stock disponible actual: **3 cajas**).
3. Seleccionar tipo **Salida**, ingresar cantidad **5** y motivo **Venta**.
4. Al presionar **Registrar movimiento**, el sistema bloquea la operación y muestra bajo el campo cantidad: `"Solo hay 3 disponibles"`, garantizando que el stock jamás quede en negativo.

## 📱 Arquitectura y Recursos del Dispositivo
* **Arquitectura MVVM (`RNF-10`):** Separación estricta de responsabilidades entre interfaz declarativa (`ui/screen/`), estado reactivo con `StateFlow` (`viewmodel/CappucareViewModel.kt`) y capa de datos ficticios (`data/MockData.kt`).
* **Recurso nativo / Almacenamiento y Controles Nativos:** Uso de almacenamiento en memoria/local preparado para persistencia con `Room` (SQLite) para operación sin conexión (`RNF-01`) y controles de teclado numérico nativo y diálogos de validación en tiempo real (`RNF-02`).
