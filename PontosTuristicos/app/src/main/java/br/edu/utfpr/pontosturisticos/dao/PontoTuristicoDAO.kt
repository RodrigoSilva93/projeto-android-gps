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

    @Query("SELECT uid FROM PontoTuristico")
    fun getAllIds(): List<Int>

    @Query("SELECT * FROM PontoTuristico WHERE uid IN (:uid)")
    fun getById(uid: Int): PontoTuristico

    @Query("SELECT * FROM PontoTuristico WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<PontoTuristico>

    @Query("SELECT * FROM PontoTuristico WHERE nome LIKE :name || '%'")
    fun getByName(name: String): PontoTuristico?

    @Insert
    fun insertAll(vararg pontosTuristicos: PontoTuristico)

    @Delete
    fun delete(pontoTuristico: PontoTuristico)

    @Update
    fun update(vararg pontosTuristicos: PontoTuristico)

    @Query("SELECT * FROM PontoTuristico WHERE nome = :name LIMIT 1")
    fun getByExactlyName(name: String): PontoTuristico?

    @Query("SELECT * FROM PontoTuristico WHERE nome LIKE '%' || :name || '%'")
    fun getByNameLike(name: String): List<PontoTuristico>
}