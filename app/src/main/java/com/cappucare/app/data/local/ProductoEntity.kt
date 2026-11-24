package com.cappucare.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cappucare.app.model.CategoriaInsumo
import com.cappucare.app.model.EstadoProducto
import com.cappucare.app.model.Producto
import com.cappucare.app.model.UnidadMedida

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: String,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val unidadMedida: String,
    val cantidadDisponible: Int,
    val nivelMinimo: Int,
    val ubicacion: String,
    val estado: String
)

fun ProductoEntity.toDomain(): Producto = Producto(
    id = id,
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion,
    categoria = CategoriaInsumo.valueOf(categoria),
    unidadMedida = UnidadMedida.valueOf(unidadMedida),
    cantidadDisponible = cantidadDisponible,
    nivelMinimo = nivelMinimo,
    ubicacion = ubicacion,
    estado = EstadoProducto.valueOf(estado)
)

fun Producto.toEntity(): ProductoEntity = ProductoEntity(
    id = id,
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria.name,
    unidadMedida = unidadMedida.name,
    cantidadDisponible = cantidadDisponible,
    nivelMinimo = nivelMinimo,
    ubicacion = ubicacion,
    estado = estado.name
)