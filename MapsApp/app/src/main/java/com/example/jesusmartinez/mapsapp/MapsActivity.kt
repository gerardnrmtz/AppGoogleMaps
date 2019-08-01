package com.example.jesusmartinez.mapsapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisocoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val CODIGO_SOLICITUD_PERMMISO = 100

    var fusedLocationClient: FusedLocationProviderClient? = null

    var locationRequest: LocationRequest? = null

    var callback: LocationCallback? = null

    private var markerGoldenState: Marker? = null
    private var markerPiramides: Marker? = null
    private var markerGisa: Marker? = null

    private var lista_marcadores: ArrayList<Marker>? = null

    private var miposicion: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (mMap != null) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    for (ubicacion in locationResult?.locations!!) {
                        Toast.makeText(
                            applicationContext, ubicacion.latitude.toString() + " - " + ubicacion.longitude.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        miposicion = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(miposicion!!).title("Im here"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(miposicion!!))
                    }
                }


            }
        }
    }

    private fun inicializarLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = 1000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        CambiarEstiloMapa()

        CrearListeners()

        MarcadoresEstaticos()

        prepararMarcadores()

        dibujarLineas()

        // Add a marker in Sydney and move the camera
    }

    private fun dibujarLineas() {

        val coordenadasLineas = PolygonOptions()
            .add(LatLng(19.434200011141158, -99.1477056965232))
            .add(LatLng(19.4410913340259, -99.14651446044444))
            .add(LatLng(19.44404092953131, -99.1405712054359))
            .add(LatLng(19.437794547975827, -99.13751095533371))
        val coordenadasPiligono = PolygonOptions()
            .add(LatLng(19.434200011141158, -99.1477056965232))
            .add(LatLng(19.4410913340259, -99.14651446044444))
            .add(LatLng(19.44404092953131, -99.1405712054359))
            .add(LatLng(19.437794547975827, -99.13751095533371))
            .strokePattern(arrayListOf<PatternItem>(Dash(10f), Gap(20f)))
            .strokeColor(Color.BLUE)
            .fillColor(Color.BLACK)

        val coordenadas = CircleOptions()
            .center(LatLng(19.434200011141158, -99.1477056965232))
            .radius(20.0)

        //mMap.addPolygon(coordenadas)
        mMap.addCircle(coordenadas)
        mMap.addPolygon((coordenadasPiligono))
    }

    private fun CrearListeners() {
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
    }

    private fun CambiarEstiloMapa() {
        val exitoCambioMapa = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.estilo_mapa))
        if (!exitoCambioMapa) {
            //mensionar que hubo un problema al cambiar el estilo del mapa
        }
        if (validarPermisosUbicacion()) {
            obtenerUbicacion()
        } else {
            pedirPermisos()
        }

    }

    private fun MarcadoresEstaticos() {
        val GOLDEN_STATE = LatLng(37.8199286, -122.4782551)
        val PIRAMIDES = LatLng(29.9772962, -122.4782551)
        val TORRE_PISA = LatLng(43.722952, 10.396597)

        markerGoldenState = mMap.addMarker(
            MarkerOptions().position(GOLDEN_STATE)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Puente de san francisco")
                .alpha(0.3f)
                .title("Golden Gate")
        )

        markerGoldenState?.tag = 0

        markerPiramides = mMap.addMarker(
            MarkerOptions().position(PIRAMIDES)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .snippet("Piramides")
                .alpha(0.6f)
                .title("Piramides")
        )
        markerPiramides?.tag = 0

        markerGisa = mMap.addMarker(
            MarkerOptions().position(TORRE_PISA)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .alpha(0.9f)
                .snippet("Torre de gisa")
                .title("Torre de gisa")
        )
        markerGisa?.tag = 0
    }


    private fun prepararMarcadores() {
        lista_marcadores = ArrayList()

        mMap.setOnMapLongClickListener {

                location: LatLng? ->
            lista_marcadores?.add(
                mMap.addMarker(
                    MarkerOptions().position(location!!)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .snippet("Marcador")
                        .alpha(0.6f)
                        .title("Marcador")
                )
            )
            lista_marcadores?.last()!!.isDraggable = true

            val coordenadas =
                LatLng(lista_marcadores?.last()!!.position.latitude, lista_marcadores?.last()!!.position.longitude)

            val origen = "origin=" + miposicion?.latitude.toString() +","+miposicion?.longitude.toString() +"&"

            val destino = "destination=" + coordenadas.latitude.toString() +","+coordenadas.longitude.toString() +"&"
            val parametros = origen + destino + "sensor=false&mode=driving"

            cargarUrl("https://maps.googleapis.com/maps/api/directions/json?" + parametros)
        }
    }

    override fun onMarkerDragEnd(marcador: Marker?) {

        Toast.makeText(this, "Termino de mover el marcador", Toast.LENGTH_SHORT).show()
    }

    override fun onMarkerDragStart(marcador: Marker?) {
        Toast.makeText(this, "Empezando a mover el marcador", Toast.LENGTH_SHORT).show()

        val index = lista_marcadores?.indexOf(marcador)


    }

    override fun onMarkerDrag(marcador: Marker?) {

        title = marcador?.position?.latitude.toString() + "-" + marcador?.position?.longitude.toString()
    }

    override fun onMarkerClick(marker: Marker?): Boolean {

        var numero_clicks = marker?.tag as? Int


        if (numero_clicks != null) {
            numero_clicks += 1

        }
        marker?.tag = numero_clicks

        Toast.makeText(this, "Numero de clicks " + numero_clicks.toString(), Toast.LENGTH_LONG).show()

        return false
    }

    private fun validarPermisosUbicacion(): Boolean {
        val hayUbicacionPremisa =
            ActivityCompat.checkSelfPermission(this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria =
            ActivityCompat.checkSelfPermission(this, permisocoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPremisa && hayUbicacionOrdinaria

    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        /*fusedLocationClient?.lastLocation?.addOnSuccessListener(this,object :OnSuccessListener<Location>{
            override fun onSuccess(location: Location?) {
                if(location!=null)
                {
                    Toast.makeText(applicationContext,location?.latitude.toString()+" - "+location?.longitude.toString(),Toast.LENGTH_LONG).show()

                }

            }

        })*/


        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private fun pedirPermisos() {

        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)
        if (deboProveerContexto) {
            //mandar mensaje
            solicitudPermiso()

        } else {
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso() {
        requestPermissions(arrayOf(permisoFineLocation, permisocoarseLocation), CODIGO_SOLICITUD_PERMMISO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CODIGO_SOLICITUD_PERMMISO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //obtener ubicacion
                    obtenerUbicacion()
                } else {
                    Toast.makeText(this, "No diste permiso para acceder ubicacion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun detenerActualizacionUbicacion() {
        fusedLocationClient?.removeLocationUpdates(callback)
    }

    private fun cargarUrl(url: String) {
        val queue = Volley.newRequestQueue(this)

        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String> {
                response ->
            Log.d("Respuesta",response)

        }, Response.ErrorListener { })
        queue.add(solicitud)
    }

    override fun onStart() {
        super.onStart()

        if (validarPermisosUbicacion()) {
            obtenerUbicacion()
        } else {
            pedirPermisos()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }
}
