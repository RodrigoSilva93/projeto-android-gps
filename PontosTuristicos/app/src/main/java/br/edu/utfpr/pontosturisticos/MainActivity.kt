package br.edu.utfpr.pontosturisticos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.ui.CadastrarActivity
import br.edu.utfpr.pontosturisticos.ui.DetalhesPontoFragment
import br.edu.utfpr.pontosturisticos.ui.ListaActivity
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private lateinit var locationManager: LocationManager
    private lateinit var btCadastrar: FloatingActionButton
    private lateinit var btLista: FloatingActionButton

    private lateinit var mMap: GoogleMap

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var currentLocation: Marker? = null

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

            if (currentLocation == null) {
                currentLocation = mMap.addMarker(MarkerOptions().position(localAtual).title("Você está aqui").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localAtual, 14f))
            } else {
                currentLocation?.position = localAtual
                mMap.animateCamera(CameraUpdateFactory.newLatLng(localAtual))
            }
        }
    }

    private fun addPontoTuristico() {
        val intent = Intent(this, CadastrarActivity::class.java).apply {
            putExtra("EXTRA_LATITUDE", currentLatitude)
            putExtra("EXTRA_LONGITUDE", currentLongitude)
            putExtra("ORIGIN", "main")
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
        carregarMarcadores()
    }

    fun carregarMarcadores(){
        if (!::mMap.isInitialized) return
        mMap.clear()

        currentLocation = mMap.addMarker(MarkerOptions().position(LatLng(currentLatitude, currentLongitude)).title("Você está aqui"))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(currentLatitude, currentLongitude)))

        val mapMarkerPonto = mutableMapOf<Marker, PontoTuristico>()
        val db = DatabaseSingleton.getInstance(this).getAppDatabase()
        val marcadores = db.pontoTuristicoDao().getAll()

        for (ponto in marcadores) {
            val localizacao = LatLng(ponto.latitude!!.toDouble(), ponto.longitude!!.toDouble())
            val marcador = mMap.addMarker(MarkerOptions()
                .position(localizacao)
                .title(ponto.nome)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

            if (marcador != null) mapMarkerPonto[marcador] = ponto
        }

        //chama detalhes fragment
        mMap.setOnMarkerClickListener { marker ->
            val pontoTuristico = mapMarkerPonto[marker]

            if (pontoTuristico != null) {
                val detalhesFragment = DetalhesPontoFragment.newInstance(pontoTuristico){
                    carregarMarcadores()
                }
                detalhesFragment.show(supportFragmentManager, "DetalhesPontoFragment")
            }

            true
        }
    }

    override fun onResume() {
        super.onResume()
        carregarMarcadores()
    }
}