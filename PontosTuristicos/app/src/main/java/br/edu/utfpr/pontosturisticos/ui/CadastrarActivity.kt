package br.edu.utfpr.pontosturisticos.ui

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton
import java.io.IOException
import java.util.Locale

class CadastrarActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_PONTO = "ID_PONTO"
        const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
    }

    private lateinit var tvCadEdit: TextView
    private lateinit var btCadastrarPonto: Button
    private lateinit var btVoltar: Button
    private lateinit var textNome: EditText
    private lateinit var textDescricao: EditText
    private lateinit var textLatitude: EditText
    private lateinit var textLongitude: EditText
    private lateinit var textEndereco: EditText

    private var pontoId: Int = 0
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar)

        initViews()
        setupMode()
        configureButtons()

        if (!isEditMode) {
            val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
            val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
            textLatitude.setText(latitude.toString())
            textLongitude.setText(longitude.toString())

            getAddressFromLocation(latitude, longitude, this) { endereco ->
                textEndereco.setText(endereco ?: "")
            }
        }
    }

    private fun initViews() {
        tvCadEdit = findViewById(R.id.tvCadEdit)
        btCadastrarPonto = findViewById(R.id.btCadastrarPonto)
        btVoltar = findViewById(R.id.btVoltar)
        textNome = findViewById(R.id.text_nome)
        textDescricao = findViewById(R.id.text_descricao)
        textLatitude = findViewById(R.id.text_latitude)
        textLongitude = findViewById(R.id.text_longitude)
        textEndereco = findViewById(R.id.text_endereco)
    }

    private fun setupMode() {
        pontoId = intent.getIntExtra(EXTRA_ID_PONTO, 0)
        isEditMode = pontoId != 0

        if (isEditMode) {
            tvCadEdit.text = getString(R.string.editar_ponto_turistico)
            carregarPonto(pontoId)
        } else {
            tvCadEdit.text = getString(R.string.cadastrar_ponto_turistico)
        }
    }

    private fun carregarPonto(id: Int) {
        val db = DatabaseSingleton.getInstance(this).getAppDatabase()
        val ponto = db.pontoTuristicoDao().getById(id)

        textNome.setText(ponto.nome)
        textDescricao.setText(ponto.descricao)
        textLatitude.setText(ponto.latitude)
        textLongitude.setText(ponto.longitude)
        textEndereco.setText(ponto.endereco)
    }

    private fun configureButtons() {
        btVoltar.setOnClickListener { finish() }
        btCadastrarPonto.setOnClickListener { savePonto() }
    }

    private fun savePonto() {
        if (textNome.text.isEmpty()) {
            Toast.makeText(this, "Preencha ao menos o nome do local.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = DatabaseSingleton.getInstance(this).getAppDatabase()
        val ponto = PontoTuristico(
            uid = if (isEditMode) pontoId else 0,
            nome = textNome.text.toString(),
            descricao = textDescricao.text.toString(),
            latitude = textLatitude.text.toString(),
            longitude = textLongitude.text.toString(),
            endereco = textEndereco.text.toString()
        )

        if (isEditMode) {
            db.pontoTuristicoDao().update(ponto)
            Toast.makeText(this, "Ponto turístico atualizado.", Toast.LENGTH_SHORT).show()
        } else {
            db.pontoTuristicoDao().insertAll(ponto)
            Toast.makeText(this, "Ponto turístico registrado.", Toast.LENGTH_SHORT).show()
        }

        setResult(RESULT_OK)
        finish()
    }

    private fun getAddressFromLocation(
        latitude: Double,
        longitude: Double,
        context: Context,
        callback: (String?) -> Unit
    ) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    val address = addresses.firstOrNull()?.getAddressLine(0)
                    callback(address)
                }
            } else {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                callback(addresses?.firstOrNull()?.getAddressLine(0))
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Erro ao buscar endereço.", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }
}
