package xyz.mcnallydawes.clik

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import xyz.mcnallydawes.clik.models.User
import java.util.*


class MainActivity : Activity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    val TAG = "MainActivity"

    lateinit var database: FirebaseDatabase
    lateinit var currentUserRef: DatabaseReference
    lateinit var userRef: DatabaseReference

    lateinit var auth: FirebaseAuth

    lateinit var user: User
    var users = ArrayList<User>()

    lateinit var googleApiClient: GoogleApiClient
    lateinit var locationRequest: LocationRequest

    @BindView(R.id.user_iv)
    lateinit var userIv: ClikImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        database = FirebaseDatabase.getInstance()

        val context = this
        auth = FirebaseAuth.getInstance()
        currentUserRef = database.getReference("user").child(auth.currentUser!!.uid)
        currentUserRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if(snapshot != null && snapshot.value != null) {
                    user = snapshot.getValue(User().javaClass)
//                    if(user.pictures.size == 0) {
//                        val intent = Intent(context, TakePhotoActivity().javaClass)
//                        startActivity(intent)
//                    }
                }
            }

            override fun onCancelled(error: DatabaseError?) {
            }
        })

        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                googleApiClient.connect()
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
            }
        }

        TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()

        userRef = database.getReference("user")
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if(snapshot != null && snapshot.value != null) {
                    val t = object: GenericTypeIndicator<List<User>>() {}
                    for(snap in snapshot.children) {
                        users.add(snap.getValue(User().javaClass))
                    }
                    val aUser = users[0]
                    Glide.with(context).load(aUser.pictures[0].url).into(userIv)
                }
            }

            override fun onCancelled(error: DatabaseError?) {
            }
        })

    }

    override fun onConnected(bundle: Bundle?) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (e: SecurityException) {
            Toast.makeText(this, "GPS Disabled", Toast.LENGTH_LONG).show()
            googleApiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(result: ConnectionResult) {

    }

    override fun onLocationChanged(location: Location?) {
        Log.d(TAG, "onLocationChanged for LocationApi")

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        googleApiClient.disconnect()

        if(location != null) {
            currentUserRef.child("lat").setValue(location.latitude)
            currentUserRef.child("lng").setValue(location.longitude)
        }
    }
}
