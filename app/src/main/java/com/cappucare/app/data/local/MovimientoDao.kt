package com.cappucare.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC, id DESC")
    fun obtenerTodos(): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE productoId = :productoId ORDER BY fecha DESC, id DESC")
    fun obtenerPorProducto(productoId: String): Flow<List<MovimientoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(movimiento: MovimientoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(movimientos: List<MovimientoEntity>)
}