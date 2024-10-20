package br.edu.utfpr.pontosturisticos.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import br.edu.utfpr.pontosturisticos.MainActivity
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.database.AppDatabase
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico

class CadastroActivity: AppCompatActivity() {

    private lateinit var btCadastrarPonto: Button
    private lateinit var btVoltar: Button

    private lateinit var textNome: EditText
    private lateinit var textDescricao: EditText
    private lateinit var textLatitude: EditText
    private lateinit var textLongitude: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        val db = createDatabase()
        db.pontoTuristicoDao().getAll()

        btCadastrarPonto = findViewById(R.id.btCadastrarPonto)
        btVoltar = findViewById(R.id.btVoltar)

        textNome = findViewById(R.id.text_nome)
        textDescricao = findViewById(R.id.text_descricao)
        textLatitude = findViewById(R.id.text_latitude)
        textLongitude = findViewById(R.id.text_longitude)

        val latitude = intent.getDoubleExtra("EXTRA_LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("EXTRA_LONGITUDE", 0.0)
        textLatitude.setText(latitude.toString())
        textLongitude.setText(longitude.toString())

        btVoltar.setOnClickListener {
            voltar()
        }

        btCadastrarPonto.setOnClickListener {
            cadastrar(db, latitude, longitude)
        }
    }

    private fun createDatabase(): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "ponto-turistico"
        ).allowMainThreadQueries().build()
    }

    private fun voltar() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun cadastrar(db: AppDatabase, lat: Double, long: Double) {
        val pontoTuristico = PontoTuristico(db.pontoTuristicoDao().getAll().size + 1)
        pontoTuristico.nome = textNome.toString()
        pontoTuristico.descricao = textDescricao.toString()
        pontoTuristico.latitude = lat.toString()
        pontoTuristico.longitude = long.toString()
        //câmera

        Toast.makeText(this, "Ponto turístico registrado.", Toast.LENGTH_SHORT).show()

        voltar()
    }
}