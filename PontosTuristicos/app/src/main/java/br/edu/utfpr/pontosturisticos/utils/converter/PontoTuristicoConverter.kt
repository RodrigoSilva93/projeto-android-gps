package br.edu.utfpr.pontosturisticos.utils.converter

import androidx.room.TypeConverter
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PontoTuristicoConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringToObject(value: String?): PontoTuristico? {
        if (value == null) return null

        val listType = object: TypeToken<PontoTuristico>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromObjectToString(pontoTuristico: PontoTuristico?): String? {
        if (pontoTuristico == null) return null

        return gson.toJson(pontoTuristico)
    }
}