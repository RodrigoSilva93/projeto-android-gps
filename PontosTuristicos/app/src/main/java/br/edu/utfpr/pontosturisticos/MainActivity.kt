package br.edu.utfpr.pontosturisticos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import br.edu.utfpr.pontosturisticos.database.AppDatabase
import br.edu.utfpr.pontosturisticos.ui.CadastroActivity
import br.edu.utfpr.pontosturisticos.ui.ListaActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var btCadastrar: FloatingActionButton
    private lateinit var btLista: Button //temp

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = createDatabase()

        btLista = findViewById(R.id.btLista) //temp
        btCadastrar = findViewById(R.id.btCadastrar)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0f, this)

        btCadastrar.setOnClickListener {
            addPontoTuristico()
        }

        btLista.setOnClickListener {
            listarPontosTuristicos(db)
        }
    }

    private fun createDatabase(): AppDatabase {
        return Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "ponto-turistico"
        ).allowMainThreadQueries().build()
    }

    override fun onLocationChanged(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
    }

    private fun addPontoTuristico() {
        if (currentLatitude != null && currentLongitude != null) {
            val intent = Intent(this, CadastroActivity::class.java).apply {
                putExtra("EXTRA_LATITUDE", currentLatitude)
                putExtra("EXTRA_LONGITUDE", currentLongitude)
            }
            startActivity(intent)
        } else Toast.makeText(this, "Localização ainda não disponível. Aguarde", Toast.LENGTH_SHORT)
            .show()
    }

    private fun listarPontosTuristicos(db: AppDatabase) {
        val ids = db.pontoTuristicoDao().getAllIds()

        val intent = Intent(this, ListaActivity::class.java)
        intent.putIntegerArrayListExtra("IDS_LIST", ArrayList(ids))
        startActivity(intent)
    }
}