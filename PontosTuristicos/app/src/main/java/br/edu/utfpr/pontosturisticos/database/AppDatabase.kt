package br.edu.utfpr.pontosturisticos.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.edu.utfpr.pontosturisticos.dao.PontoTuristicoDAO
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.utils.converter.PontoTuristicoConverter

@Database(entities = [PontoTuristico::class], version = 2)
@TypeConverters(value = [PontoTuristicoConverter::class])
abstract class AppDatabase: RoomDatabase() {
    abstract fun pontoTuristicoDao(): PontoTuristicoDAO
}