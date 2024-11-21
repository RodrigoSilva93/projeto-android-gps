package br.edu.utfpr.pontosturisticos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.ui.CadastrarActivity
import br.edu.utfpr.pontosturisticos.ui.DetalhesPontoFragment
import br.edu.utfpr.pontosturisticos.ui.ListaActivity
import br.edu.utfpr.pontosturisticos.ui.SettingsActivity
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

class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private lateinit var locationManager: LocationManager
    private lateinit var btCadastrar: FloatingActionButton
    private lateinit var btLista: FloatingActionButton
    private lateinit var btConfig: FloatingActionButton
    private lateinit var svBusca: SearchView

    private lateinit var mMap: GoogleMap

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var currentLocation: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        svBusca = findViewById(R.id.svBusca)
        btLista = findViewById(R.id.btListar)
        btCadastrar = findViewById(R.id.btCadastrar)
        btConfig= findViewById(R.id.btConfig)
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

        val db = DatabaseSingleton.getInstance(this).getAppDatabase()

        btCadastrar.setOnClickListener { addPontoTuristico() }
        btLista.setOnClickListener { listarPontosTuristicos() }
        btConfig.setOnClickListener { abrirConfiguracoes() }
        svBusca.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val ponto = db.pontoTuristicoDao().getByName(it)
                    if (ponto != null) {
                        val latLng = ponto.latitude?.let { it1 -> ponto.longitude?.let { it2 -> LatLng(it1.toDouble(), it2.toDouble()) } }
                        latLng?.let { it1 -> CameraUpdateFactory.newLatLng(it1) }
                            ?.let { it2 -> mMap.animateCamera(it2) }

                        val detalhesFragment = DetalhesPontoFragment.newInstance(ponto){
                            carregarMarcadores()
                        }
                        detalhesFragment.show(supportFragmentManager, "DetalhesPontoFragment")
                    } else
                        Toast.makeText(this@MainActivity, "Ponto Turístico não encontrado.", Toast.LENGTH_SHORT).show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        carregarMarcadores()
        if (::mMap.isInitialized) {
            aplicarConfig()
        }
    }

    private fun abrirConfiguracoes() {
        val  intent = Intent( this, SettingsActivity::class.java  )
        startActivity( intent )
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

        aplicarConfig()
        carregarMarcadores()
    }

    private fun carregarMarcadores(){
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

    private fun aplicarConfig(){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val tipoMapa = sharedPreferences.getString("tipo", "Híbrido")
        val zoomPreferencia = sharedPreferences.getString("zoom", "Médio")
        mMap.mapType = when (tipoMapa) {
            "Satélite" -> GoogleMap.MAP_TYPE_SATELLITE
            "Rodoviário" -> GoogleMap.MAP_TYPE_NORMAL
            "Híbrido" -> GoogleMap.MAP_TYPE_HYBRID
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
        val zoomLevel = when (zoomPreferencia) {
            "Próximo" -> 18.0f
            "Médio" -> 12.0f
            "Distante" -> 6.0f
            else -> 12.0f
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLatitude, currentLongitude), zoomLevel))
    }
}