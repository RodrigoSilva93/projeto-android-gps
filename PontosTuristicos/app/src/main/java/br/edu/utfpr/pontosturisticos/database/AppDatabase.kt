package br.edu.utfpr.pontosturisticos.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.edu.utfpr.pontosturisticos.dao.PontoTuristicoDAO
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico

@Database(entities = [PontoTuristico::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun pontoTuristicoDao(): PontoTuristicoDAO
}