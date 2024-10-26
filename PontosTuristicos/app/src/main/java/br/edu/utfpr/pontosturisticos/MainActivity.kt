package br.edu.utfpr.pontosturisticos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.edu.utfpr.pontosturisticos.ui.CadastroActivity
import br.edu.utfpr.pontosturisticos.ui.ListaActivity
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private lateinit var locationManager: LocationManager
    private lateinit var btCadastrar: FloatingActionButton
    private lateinit var btLista: FloatingActionButton //temp

    private lateinit var mMap: GoogleMap

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
		
        btLista = findViewById(R.id.btListar) //temp
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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btCadastrar.setOnClickListener { addPontoTuristico() }
        btLista.setOnClickListener { listarPontosTuristicos() }
    }

    override fun onLocationChanged(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude

        if (::mMap.isInitialized) {
            val localAtual = LatLng(currentLatitude, currentLongitude)
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(localAtual).title("Você está aqui"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localAtual, 10f))
        }
    }

    private fun addPontoTuristico() {
        val intent = Intent(this, CadastroActivity::class.java).apply {
            putExtra("EXTRA_LATITUDE", currentLatitude)
            putExtra("EXTRA_LONGITUDE", currentLongitude)
        }
        startActivity(intent)
    }

    private fun listarPontosTuristicos() {
        val db = DatabaseSingleton.getInstance(this).getAppDatabase()
        val ids = db.pontoTuristicoDao().getAllIds()

        val intent = Intent(this, ListaActivity::class.java)
        intent.putIntegerArrayListExtra("IDS_LIST", ArrayList(ids))
        startActivity(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val home = LatLng(currentLatitude, currentLongitude)
        mMap.addMarker(MarkerOptions().position(home).title("Marker in Home"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 10f))
    }
}