package br.edu.utfpr.pontosturisticos.ui
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class CadastrarActivity: AppCompatActivity() {

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
    private val REQUESTCAMERAPERMISSION = 100
    private val requestImageCapture = 101
    private var imageFile: File? = null

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

        ivImagem = findViewById(R.id.ivImagem)
        btAddImagem = findViewById(R.id.btAddImagem)

        btAddImagem.setOnClickListener {
            if (checkPermissions()) {
                val photoFile = createImageFile()

                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    photoFile
                )

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
        // Verifica se imageFile não é nulo antes de continuar
        val imagemPath = imageFile?.absolutePath ?: ""

        val pontoTuristico = PontoTuristico(
            nome = textNome.text.toString(),
            descricao = textDescricao.text.toString(),
            latitude = textLatitude.text.toString(),
            longitude = textLongitude.text.toString(),
            endereco = textEndereco.text.toString(),
            //câmera
            imagem = imagemPath
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

    // Verifica as permissões
    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED
    }

    // Solicita as permissões
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTCAMERAPERMISSION)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Criar um arquivo temporário para a foto
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestImageCapture && resultCode == Activity.RESULT_OK) {
            val imageUri = Uri.parse(imagemUri.toString())
            imageFile = File(imageUri.path) // Atribui o arquivo à variável

            val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath)
            ivImagem.setImageBitmap(bitmap) // Exibe a imagem no ImageView
        }
    }

}