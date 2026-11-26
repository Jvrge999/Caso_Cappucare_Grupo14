package com.cappucare.app.viewmodel

import com.cappucare.app.model.CategoriaInsumo
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import com.cappucare.app.model.EstadoProducto
import com.cappucare.app.model.MotivoSalida
import com.cappucare.app.model.Producto
import com.cappucare.app.model.TipoMovimiento
import com.cappucare.app.model.UnidadMedida
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CappucareViewModelTest {

    private lateinit var viewModel: CappucareViewModel

    @BeforeEach
    fun setUp() {
        viewModel = CappucareViewModel()
    }

    @Test
    fun `login falla cuando usuario o clave estan vacios o incorrectos y limpia error`() {
        viewModel.login("", "encargado123").shouldBeFalse()
        viewModel.errorLogin.value.shouldBe("Ingresa tu usuario")

        viewModel.login("encargado@cappucare.cl", "").shouldBeFalse()
        viewModel.errorLogin.value.shouldBe("Ingresa tu clave")

        viewModel.login("encargado@cappucare.cl", "claveMala").shouldBeFalse()
        viewModel.errorLogin.value.shouldBe("Usuario o clave incorrectos")

        viewModel.limpiarErrorLogin()
        viewModel.errorLogin.value.shouldBeNull()
    }

    @Test
    fun `login exitoso inicia sesion como encargado y logout limpia estado`() {
        viewModel.login("encargado@cappucare.cl", "encargado123").shouldBeTrue()
        viewModel.usuarioActivo.value.shouldNotBeNull()
        viewModel.errorLogin.value.shouldBeNull()
        viewModel.puedeGestionarCatalogo().shouldBeTrue()

        viewModel.logout()
        viewModel.usuarioActivo.value.shouldBeNull()
        viewModel.puedeGestionarCatalogo().shouldBeFalse()
    }

    @Test
    fun `usuario autorizado no puede gestionar catalogo ni registrar ingresos`() {
        viewModel.login("ventas@cappucare.cl", "ventas123").shouldBeTrue()

        viewModel.puedeGestionarCatalogo().shouldBeFalse()
        viewModel.puedeRegistrarTipoMovimiento(TipoMovimiento.SALIDA).shouldBeTrue()
        viewModel.puedeRegistrarTipoMovimiento(TipoMovimiento.INGRESO).shouldBeFalse()

        val primerProducto = viewModel.productos.value.first()
        val guardado = viewModel.guardarProducto(primerProducto.copy(nombre = "Cambio restringido"))
        guardado.shouldBeFalse()
        viewModel.mensajeRetroalimentacion.value.shouldNotBeNull()

        val ingresoRechazado = viewModel.registrarMovimiento(
            productoId = primerProducto.id,
            tipo = TipoMovimiento.INGRESO,
            cantidad = 5,
            motivo = null,
            observacion = "Intento ingreso"
        )
        ingresoRechazado.shouldBeFalse()
    }

    @Test
    fun `validarMovimiento aplica regla RN-03 y rechaza cantidades invalidas o sin motivo`() {
        val producto = viewModel.productos.value.first()
        val cantidadExcedida = (producto.cantidadDisponible + 10).toString()

        val erroresStock = viewModel.validarMovimiento(
            productoId = producto.id,
            tipo = TipoMovimiento.SALIDA,
            cantidad = cantidadExcedida,
            motivo = MotivoSalida.VENTA
        )
        erroresStock.hayErrores.shouldBeTrue()
        erroresStock.cantidad.shouldBe("Solo hay ${producto.cantidadDisponible} disponibles")

        val erroresMotivo = viewModel.validarMovimiento(
            productoId = producto.id,
            tipo = TipoMovimiento.SALIDA,
            cantidad = "1",
            motivo = null
        )
        erroresMotivo.hayErrores.shouldBeTrue()
        erroresMotivo.motivo.shouldBe("Elige el motivo de la salida")

        val erroresCantidad = viewModel.validarMovimiento(
            productoId = producto.id,
            tipo = TipoMovimiento.INGRESO,
            cantidad = "0",
            motivo = null
        )
        erroresCantidad.hayErrores.shouldBeTrue()
        erroresCantidad.cantidad.shouldBe("Ingresa una cantidad mayor que 0")
    }

    @Test
    fun `registrarMovimiento rechaza salida mayor a stock, agota en cero y reabastece con ingreso`() {
        viewModel.login("encargado@cappucare.cl", "encargado123").shouldBeTrue()

        val productoConStock = viewModel.productos.value.first { it.cantidadDisponible > 0 }

        val rechazado = viewModel.registrarMovimiento(
            productoId = productoConStock.id,
            tipo = TipoMovimiento.SALIDA,
            cantidad = productoConStock.cantidadDisponible + 1,
            motivo = MotivoSalida.VENTA,
            observacion = "Supera stock"
        )
        rechazado.shouldBeFalse()
        viewModel.productoPorId(productoConStock.id)?.cantidadDisponible.shouldBe(
            productoConStock.cantidadDisponible
        )

        val salidaValida = viewModel.registrarMovimiento(
            productoId = productoConStock.id,
            tipo = TipoMovimiento.SALIDA,
            cantidad = productoConStock.cantidadDisponible,
            motivo = MotivoSalida.VENTA,
            observacion = "Agota existencias"
        )
        salidaValida.shouldBeTrue()

        val agotado = viewModel.productoPorId(productoConStock.id)
        agotado.shouldNotBeNull()
        agotado.cantidadDisponible.shouldBe(0)
        if (productoConStock.estado == EstadoProducto.DISPONIBLE) {
            agotado.estado.shouldBe(EstadoProducto.AGOTADO)
        }

        val ingresoValido = viewModel.registrarMovimiento(
            productoId = productoConStock.id,
            tipo = TipoMovimiento.INGRESO,
            cantidad = 4,
            motivo = null,
            observacion = "Reposicion prueba"
        )
        ingresoValido.shouldBeTrue()

        val reabastecido = viewModel.productoPorId(productoConStock.id)
        reabastecido.shouldNotBeNull()
        reabastecido.cantidadDisponible.shouldBe(4)
        if (agotado.estado == EstadoProducto.AGOTADO) {
            reabastecido.estado.shouldBe(EstadoProducto.DISPONIBLE)
        }

        (viewModel.historialDe(null).size >= viewModel.historialDe(productoConStock.id).size).shouldBeTrue()
        (viewModel.historialDe(productoConStock.id).size)
    }

    @Test
    fun `guardarProducto crea, edita conservando stock, filtra y valida codigo duplicado`() {
        viewModel.login("encargado@cappucare.cl", "encargado123").shouldBeTrue()
        val base = viewModel.productos.value.first()
        val categoriaBase = base.categoria

        val erroresDuplicado = viewModel.validarProducto(
            codigo = base.codigo,
            nombre = "Insumo prueba",
            categoria = categoriaBase,
            unidadMedida = UnidadMedida.UNIDAD,
            nivelMinimo = "5",
            ubicacion = "Estante A1"
        )
        erroresDuplicado.codigo.shouldBe("Ya existe un producto con ese código")

        val nuevoProducto = Producto(
            id = "",
            codigo = "QR-TEST-999",
            nombre = "Guantes nitrilo test",
            descripcion = "Insumo de prueba para test unitario",
            categoria = categoriaBase,
            unidadMedida = UnidadMedida.CAJA,
            nivelMinimo = 10,
            ubicacion = "Bodega 1",
            cantidadDisponible = 99,
            estado = EstadoProducto.DADO_DE_BAJA
        )
        viewModel.guardarProducto(nuevoProducto).shouldBeTrue()

        val guardado = viewModel.productos.value.find { it.codigo == "QR-TEST-999" }
        guardado.shouldNotBeNull()
        guardado.cantidadDisponible.shouldBe(0)
        guardado.estado.shouldBe(EstadoProducto.DISPONIBLE)
        viewModel.buscarProductos("nitrilo", categoriaBase) shouldContain guardado

        viewModel.seleccionarProducto(guardado.id)
        viewModel.guardarProducto(guardado.copy(nombre = "Guantes nitrilo editados")).shouldBeTrue()
        viewModel.productoPorId(guardado.id)?.nombre.shouldBe("Guantes nitrilo editados")
        viewModel.productoPorId(guardado.id)?.cantidadDisponible.shouldBe(0)
    }

    @Test
    fun `productosBajoMinimo excluye dados de baja`() {
        viewModel.login("encargado@cappucare.cl", "encargado123").shouldBeTrue()
        val objetivo = viewModel.productos.value.first()

        viewModel.cambiarEstado(objetivo.id, EstadoProducto.DADO_DE_BAJA).shouldBeTrue()
        viewModel.productosBajoMinimo().map { it.id } shouldNotContain objetivo.id
    }
}