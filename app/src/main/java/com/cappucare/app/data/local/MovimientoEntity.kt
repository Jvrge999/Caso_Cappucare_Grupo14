package com.cappucare.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cappucare.app.model.MotivoSalida
import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.TipoMovimiento

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productoId"])]
)
data class MovimientoEntity(
    @PrimaryKey val id: String,
    val productoId: String,
    val fecha: String,
    val tipo: String,
    val cantidad: Int,
    val motivo: String?,
    val observacion: String,
    val responsable: String
)

fun MovimientoEntity.toDomain(): Movimiento = Movimiento(
    id = id,
    productoId = productoId,
    fecha = fecha,
    tipo = TipoMovimiento.valueOf(tipo),
    cantidad = cantidad,
    motivo = motivo?.let { MotivoSalida.valueOf(it) },
    observacion = observacion,
    responsable = responsable
)

fun Movimiento.toEntity(): MovimientoEntity = MovimientoEntity(
    id = id,
    productoId = productoId,
    fecha = fecha,
    tipo = tipo.name,
    cantidad = cantidad,
    motivo = motivo?.name,
    observacion = observacion,
    responsable = responsable
)