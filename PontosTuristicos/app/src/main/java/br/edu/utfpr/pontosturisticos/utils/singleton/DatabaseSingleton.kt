package br.edu.utfpr.pontosturisticos.utils.singleton

import android.content.Context
import androidx.room.Room
import br.edu.utfpr.pontosturisticos.database.AppDatabase

class DatabaseSingleton private constructor(context: Context) {
    private val appDatabase: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "ponto-turistico"
    ).fallbackToDestructiveMigration() //apenas desenvolvimento
        .allowMainThreadQueries()
        .build()

    companion object {
        @Volatile
        private var instance: DatabaseSingleton? = null

        fun getInstance(context: Context): DatabaseSingleton {
            return instance ?: synchronized(this) {
                val newInstance = DatabaseSingleton(context)
                instance = newInstance
                newInstance
            }
        }
    }

    fun getAppDatabase(): AppDatabase { return appDatabase }
}