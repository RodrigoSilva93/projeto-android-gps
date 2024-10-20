package br.edu.utfpr.pontosturisticos.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PontoTuristico (
    @PrimaryKey val uid: Int
) {
    @ColumnInfo(name = "nome")
    var nome: String? = null

    @ColumnInfo(name = "descricao")
    var descricao: String? = null

    @ColumnInfo(name = "latitude")
    var latitude: String? = null

    @ColumnInfo(name = "longitude")
    var longitude: String? = null

    //Câmera
}