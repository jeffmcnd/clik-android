package xyz.mcnallydawes.clik

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
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
import xyz.mcnallydawes.clik.models.QueueUser
import xyz.mcnallydawes.clik.models.User
import java.util.*


class MainActivity : Activity(), ClikImageView.UserChoiceListener {

    val TAG = "MainActivity"

    lateinit var database: FirebaseDatabase
    lateinit var currentUserRef: DatabaseReference
    lateinit var userRef: DatabaseReference

    lateinit var auth: FirebaseAuth

    lateinit var userKey: String
    lateinit var user: User

    var users = ArrayList<QueueUser>()

    @BindView(R.id.user_iv)
    lateinit var userIv: ClikImageView
    @BindView(R.id.user_name_tv)
    lateinit var userNameTv: TextView
    @BindView(R.id.user_age_tv)
    lateinit var userAgeTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        database = FirebaseDatabase.getInstance()

        val context = this
        auth = FirebaseAuth.getInstance()
        currentUserRef = database.getReference("users").child(auth.currentUser!!.uid)

        currentUserRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot?) {
                if(snap != null && snap.value != null) {
                    userKey = snap.key
                    user = snap.getValue(User().javaClass)
                }
            }

            override fun onCancelled(error: DatabaseError?) {}
        })

        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {}
            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
        }

        TedPermission(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()

        userRef = database.getReference("queues").child(auth.currentUser!!.uid)
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot?) {
                if(snap != null && snap.value != null) {
                    for(childSnap in snap.children) {
                        users.add(childSnap.getValue(QueueUser().javaClass))
                    }
                    val aUser = users[0]
                    Glide.with(context).load(aUser.photoUrl).into(userIv)
                    userNameTv.text = aUser.name
                    userAgeTv.text = aUser.age.toString()
                }
            }

            override fun onCancelled(error: DatabaseError?) {
            }
        })

        userIv.setUserChoiceListener(this)

    }

    @OnClick(R.id.logout_btn)
    fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity().javaClass)
        startActivity(intent)
        finish()
    }

    override fun onYay() {
//        TODO: create a partial match for like user and current user
//        TODO: animate image to indicate match, remove color filter, load new user
    }

    override fun onNay() {
//        TODO: animate image to indicate match, remove color filter, load new user
    }
}
