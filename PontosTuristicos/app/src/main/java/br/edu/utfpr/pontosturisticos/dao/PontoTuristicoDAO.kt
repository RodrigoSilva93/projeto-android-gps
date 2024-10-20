package br.edu.utfpr.pontosturisticos.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico

@Dao
interface PontoTuristicoDAO {

    @Query("SELECT * from PontoTuristico")
    fun getAll(): List<PontoTuristico>

    @Query("SELECT * from PontoTuristico WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<PontoTuristico>

    @Insert
    fun insertAll(vararg pontosTuristicos: PontoTuristico)

    @Delete
    fun delete(pontoTuristico: PontoTuristico)

    @Update
    fun update(vararg pontosTuristicos: PontoTuristico)
}