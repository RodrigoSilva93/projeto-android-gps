package br.edu.utfpr.pontosturisticos.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
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

    private lateinit var ivImagem: ImageView
    private lateinit var btAddImagem: Button
    private var imagemUri: Uri? = null
    private val requestCameraPermission = 100
    private val requestImageCapture = 101
    private var imageFile: File? = null

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCameraPermission) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
                Toast.makeText(this, "Permissões concedidas.", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Permissões necessárias não foram concedidas.", Toast.LENGTH_SHORT).show()
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
        ivImagem = findViewById(R.id.ivImagem)
        btAddImagem = findViewById(R.id.btAddImagem)

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
        val imagemCaminho = ponto.imagem

        if (imagemCaminho != null) {
            if (imagemCaminho.isNotEmpty()) {
                val file = File(imagemCaminho)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    println(bitmap)

                    if (bitmap != null)
                        ivImagem.setImageBitmap(bitmap)
                    else
                        Toast.makeText(this, "Erro ao carregar a imagem.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Imagem não encontrada.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Nenhuma imagem associada a este ponto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configureButtons() {
        btVoltar.setOnClickListener { finish() }
        btCadastrarPonto.setOnClickListener { savePonto() }
        btAddImagem.setOnClickListener {
            if (checkPermissions()) {
                val photoFile = createImageFile()
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    photoFile
                )
                imagemUri = photoURI
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, requestImageCapture)
                }
            } else {
                requestPermissions()
            }
        }
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
            endereco = textEndereco.text.toString(),
            imagem = imageFile?.absolutePath ?: ""
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

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), requestCameraPermission)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat.getDateTimeInstance()
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            imageFile = this
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestImageCapture && resultCode == Activity.RESULT_OK) {
            if (imagemUri != null) {
                val inputStream = contentResolver.openInputStream(imagemUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null)
                    ivImagem.setImageBitmap(bitmap)
                else
                    Toast.makeText(this, "Erro ao carregar a imagem capturada.", Toast.LENGTH_SHORT).show()
            }
            else
                Toast.makeText(this, "Erro ao capturar a imagem.", Toast.LENGTH_SHORT).show()
        }
    }
}
