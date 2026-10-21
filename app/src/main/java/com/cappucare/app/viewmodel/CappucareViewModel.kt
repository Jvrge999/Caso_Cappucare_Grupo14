package com.cappucare.app.viewmodel

import androidx.lifecycle.ViewModel
import com.cappucare.app.data.MockData
import com.cappucare.app.model.CategoriaInsumo
import com.cappucare.app.model.ErroresMovimiento
import com.cappucare.app.model.ErroresProducto
import com.cappucare.app.model.EstadoProducto
import com.cappucare.app.model.MotivoSalida
import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.Perfil
import com.cappucare.app.model.Producto
import com.cappucare.app.model.TipoMovimiento
import com.cappucare.app.model.UnidadMedida
import com.cappucare.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CappucareViewModel : ViewModel() {

    // =====================================================================
    // Autenticación (RF-00)
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    fun login(usuario: String, clave: String): Boolean {
        val uLimpio = usuario.trim()
        if (uLimpio.isEmpty()) {
            _errorLogin.value = "Ingresa tu usuario"
            return false
        }
        if (clave.isEmpty()) {
            _errorLogin.value = "Ingresa tu clave"
            return false
        }
        val encontrado = MockData.usuarios.find {
            it.usuario.equals(uLimpio, ignoreCase = true) && it.clave == clave
        }
        return if (encontrado != null) {
            _usuarioActivo.value = encontrado
            _errorLogin.value = null
            true
        } else {
            _errorLogin.value = "Usuario o clave incorrectos"
            false
        }
    }

    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value = MockData.usuarios.first { it.perfil == perfil }
        _errorLogin.value = null
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _productoSeleccionadoId.value = null
    }

    // =====================================================================
    // Módulo "Gestión de productos" (RF-01, RF-02, RF-03, RF-08, RN-04)
    // =====================================================================

    private val _productos = MutableStateFlow(MockData.productos)
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _productoSeleccionadoId = MutableStateFlow<String?>(null)
    val productoSeleccionadoId: StateFlow<String?> = _productoSeleccionadoId.asStateFlow()

    fun seleccionarProducto(id: String?) {
        _productoSeleccionadoId.value = id
    }

    fun productoPorId(id: String?): Producto? = _productos.value.find { it.id == id }

    fun buscarProductos(texto: String, categoria: CategoriaInsumo?): List<Producto> {
        val q = texto.trim()
        return _productos.value.filter { prod ->
            val coincideCat = categoria == null || prod.categoria == categoria
            val coincideTxt = q.isEmpty() ||
                prod.nombre.contains(q, ignoreCase = true) ||
                prod.codigo.contains(q, ignoreCase = true)
            coincideCat && coincideTxt
        }
    }

    fun validarProducto(
        codigo: String,
        nombre: String,
        categoria: CategoriaInsumo?,
        unidadMedida: UnidadMedida?,
        nivelMinimo: String,
        ubicacion: String
    ): ErroresProducto {
        val idEditando = _productoSeleccionadoId.value
        val codLimpio = codigo.trim()
        val repetido = _productos.value.any {
            it.codigo.equals(codLimpio, ignoreCase = true) && it.id != idEditando
        }
        val minInt = nivelMinimo.trim().toIntOrNull()

        return ErroresProducto(
            codigo = when {
                codLimpio.isEmpty() -> "Campo obligatorio"
                repetido -> "Ya existe un producto con ese código"
                else -> null
            },
            nombre = if (nombre.trim().isEmpty()) "Campo obligatorio" else null,
            categoria = if (categoria == null) "Elige una categoría" else null,
            unidadMedida = if (unidadMedida == null) "Elige una unidad" else null,
            nivelMinimo = if (minInt == null || minInt < 0) "Ingresa un número válido" else null,
            ubicacion = if (ubicacion.trim().isEmpty()) "Campo obligatorio" else null
        )
    }

    fun guardarProducto(producto: Producto) {
        if (producto.id.isBlank()) {
            val nuevoId = "P${_productos.value.size + 1}"
            val nuevo = producto.copy(
                id = nuevoId,
                cantidadDisponible = 0,
                estado = EstadoProducto.DISPONIBLE
            )
            _productos.value = _productos.value + nuevo
        } else {
            _productos.value = _productos.value.map { actual ->
                if (actual.id == producto.id) {
                    producto.copy(cantidadDisponible = actual.cantidadDisponible)
                } else {
                    actual
                }
            }
        }
    }

    fun cambiarEstado(productoId: String, nuevoEstado: EstadoProducto) {
        _productos.value = _productos.value.map { prod ->
            if (prod.id == productoId) prod.copy(estado = nuevoEstado) else prod
        }
    }

    // =====================================================================
    // Módulo "Movimientos de inventario" (RN-03: El stock nunca queda negativo)
    // =====================================================================

    private val _movimientos = MutableStateFlow(MockData.movimientos)
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()

    fun validarMovimiento(
        productoId: String?,
        tipo: TipoMovimiento,
        cantidad: String,
        motivo: MotivoSalida?
    ): ErroresMovimiento {
        val prod = productoPorId(productoId)
        val cantNum = cantidad.trim().toIntOrNull()

        val errorProd = if (prod == null) "Elige un producto" else null

        // RN-03: En SALIDA no se puede pedir más de la cantidadDisponible
        val errorCant = when {
            cantNum == null || cantNum <= 0 -> "Ingresa una cantidad mayor que 0"
            tipo == TipoMovimiento.SALIDA && prod != null && cantNum > prod.cantidadDisponible ->
                "Solo hay ${prod.cantidadDisponible} disponibles"
            else -> null
        }

        val errorMotivo = if (tipo == TipoMovimiento.SALIDA && motivo == null) {
            "Elige el motivo de la salida"
        } else {
            null
        }

        return ErroresMovimiento(
            producto = errorProd,
            cantidad = errorCant,
            motivo = errorMotivo
        )
    }

    // TODO(equipo): registrar movimiento y actualizar stock aplicando RN-03
    fun registrarMovimiento(
        productoId: String,
        tipo: TipoMovimiento,
        cantidad: Int,
        motivo: MotivoSalida?,
        observacion: String
    ) {
        // Pendiente para siguiente commit
    }

    // =====================================================================
    // Módulo "Consulta y reposición"
    // =====================================================================

    fun historialDe(productoId: String?): List<Movimiento> = _movimientos.value

    fun productosBajoMinimo(): List<Producto> = emptyList()
}
