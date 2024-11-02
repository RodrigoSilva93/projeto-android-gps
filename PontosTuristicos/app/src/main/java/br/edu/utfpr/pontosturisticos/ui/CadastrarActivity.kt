package br.edu.utfpr.pontosturisticos.ui

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.pontosturisticos.MainActivity
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.database.AppDatabase
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton
import java.io.IOException
import java.util.Locale

class CadastrarActivity: AppCompatActivity() {

    private lateinit var tvCadEdit: TextView

    private lateinit var btCadastrarPonto: Button
    private lateinit var btVoltar: Button

    private lateinit var textNome: EditText
    private lateinit var textDescricao: EditText
    private lateinit var textLatitude: EditText
    private lateinit var textLongitude: EditText
    private lateinit var textEndereco: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar)

        val db = DatabaseSingleton.getInstance(this).getAppDatabase()
        db.pontoTuristicoDao().getAll()

        tvCadEdit = findViewById(R.id.tvCadEdit)

        val origin = intent.getStringExtra("ORIGIN")
        if (origin.equals("main")) tvCadEdit.text = getString(R.string.cadastrar_ponto_turistico)
        else if (origin.equals("menu")) tvCadEdit.text = getString(R.string.editar_ponto_turistico)

        btCadastrarPonto = findViewById(R.id.btCadastrarPonto)
        btVoltar = findViewById(R.id.btVoltar)

        textNome = findViewById(R.id.text_nome)
        textDescricao = findViewById(R.id.text_descricao)
        textLatitude = findViewById(R.id.text_latitude)
        textLongitude = findViewById(R.id.text_longitude)
        textEndereco = findViewById(R.id.text_endereco)

        val latitude = intent.getDoubleExtra("EXTRA_LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("EXTRA_LONGITUDE", 0.0)
        textLatitude.setText(latitude.toString())
        textLongitude.setText(longitude.toString())

        getAddressFromLocation(latitude, longitude, this) { endereco ->
            if (endereco != null) textEndereco.setText(endereco)
            else textEndereco.setText("")
        }

        btVoltar.setOnClickListener { voltar() }
        btCadastrarPonto.setOnClickListener { cadastrar(db) }
    }

    override fun onStart() {
        super.onStart()
        val id = intent.getIntExtra("ID_PONTO", 0)
        if (id != 0) {
            val db = DatabaseSingleton.getInstance(this).getAppDatabase()
            val pontoTuristico = db.pontoTuristicoDao().getById(id)
            textNome.setText(pontoTuristico.nome)
            textDescricao.setText(pontoTuristico.descricao)
            textLatitude.setText(pontoTuristico.latitude)
            textLongitude.setText(pontoTuristico.longitude)
            textEndereco.setText(pontoTuristico.endereco)
        }

    }

    private fun voltar() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        finish()
    }

    private fun cadastrar(db: AppDatabase) {
        val pontoTuristico = PontoTuristico(
            nome = textNome.text.toString(),
            descricao = textDescricao.text.toString(),
            latitude = textLatitude.text.toString(),
            longitude = textLongitude.text.toString(),
            endereco = textEndereco.text.toString()
            //câmera
        )
        if (intent.hasExtra("ID_PONTO")) {
            val id = intent.getIntExtra("ID_PONTO", 0)
            pontoTuristico.uid = id
            db.pontoTuristicoDao().update(pontoTuristico)
        } else {
            db.pontoTuristicoDao().insertAll(pontoTuristico)
        }

        Toast.makeText(this, "Ponto turístico registrado.", Toast.LENGTH_SHORT).show()

        voltar()
    }

    private fun getAddressFromLocation(
        latitude: Double,
        longitude: Double,
        context: Context,
        callback: (String?) -> Unit) {

        val geocoder = Geocoder(context, Locale.getDefault())

        return try {

            if (Build.VERSION.SDK_INT >= 33) {
                val geocodeListener = GeocodeListener { enderecos ->
                    val endereco = enderecos.firstOrNull()?.getAddressLine(0)

                    callback(endereco) //return
                }
                geocoder.getFromLocation(latitude, longitude, 1, geocodeListener)
            } else {
                val enderecos = geocoder.getFromLocation(latitude, longitude, 1)

                val endereco = if (!enderecos.isNullOrEmpty()) {
                    enderecos[0].getAddressLine(0) //return
                } else null //return

                callback(endereco) //return
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Ocorreu um erro ao procurar o endereço.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            callback(null) //return
        }
    }
}