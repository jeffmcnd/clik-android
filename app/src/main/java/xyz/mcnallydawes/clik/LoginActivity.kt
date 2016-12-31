package xyz.mcnallydawes.clik

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import xyz.mcnallydawes.clik.models.Gender
import xyz.mcnallydawes.clik.models.Picture
import xyz.mcnallydawes.clik.models.User
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*


class LoginActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var callbackManager: CallbackManager

    lateinit var database: FirebaseDatabase

    @BindView(R.id.facebook_sign_in_button)
    lateinit var facebookBtn: LoginButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        auth = FirebaseAuth.getInstance()
        authListener = FirebaseAuth.AuthStateListener {
            var user = it.currentUser
            if(user != null) {
                checkIfUserExists(user)
            } else {

            }
        }

        database = FirebaseDatabase.getInstance()

        callbackManager = CallbackManager.Factory.create()
        facebookBtn.setReadPermissions("email", "public_profile", "user_birthday", "user_friends", "user_work_history")

        val context = this
        facebookBtn.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                if(result != null) {
                    handleFacebookAccessToken(result.accessToken)
                } else {
                    Toast.makeText(context, "Success but result is null.", Toast.LENGTH_LONG).show();
                }
            }

            override fun onCancel() {
                Toast.makeText(context, R.string.sign_in_failure, Toast.LENGTH_LONG).show();
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(context, R.string.sign_in_failure, Toast.LENGTH_LONG).show();
            }
        })
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val context = this
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful) {
                Toast.makeText(context, R.string.sign_in_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, R.string.sign_in_failure, Toast.LENGTH_LONG).show();
            }
        }
    }

    private fun checkIfUserExists(user: FirebaseUser) {
        val userRef = database.getReference("user")
        userRef.equalTo(user.uid, "uid")
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                if(snapshot == null || snapshot.value == null) {
                    val request = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            GraphRequest.GraphJSONObjectCallback {
                                jsonObject, graphResponse ->
                                if(jsonObject != null && graphResponse.error == null) {
                                    val localUser = User()
                                    localUser.email = jsonObject.getString("email")
                                    localUser.info = ""
                                    localUser.career = ""
                                    localUser.firstName = jsonObject.getString("first_name")
                                    localUser.lastName = jsonObject.getString("last_name")

                                    val gender = jsonObject.getString("gender")
                                    if(gender == "male") {
                                        localUser.gender = Gender.MALE
                                    } else if(gender == "female") {
                                        localUser.gender = Gender.FEMALE
                                    } else {
                                        localUser.gender = Gender.OTHER
                                    }

                                    try {
                                        val birthday = jsonObject.getString("birthday")
                                        val dateFormat = SimpleDateFormat("""MM/dd/yyyy""")
                                        val parsedDate = dateFormat.parse(birthday)
                                        val calBirth = Calendar.getInstance()
                                        calBirth.time = parsedDate

                                        val now = Date()
                                        val calNow = Calendar.getInstance()
                                        calNow.time = now

                                        var diff = calNow.get(YEAR) - calBirth.get(YEAR)
                                        if (calBirth.get(MONTH) > calNow.get(MONTH) ||
                                                (calBirth.get(MONTH) == calNow.get(MONTH) && calBirth.get(DATE) > calNow.get(DATE))) {
                                            diff--;
                                        }

                                        localUser.age = diff
                                        localUser.birthday = birthday

                                    } catch (e: Exception) {

                                    }

                                    userRef.child(user.uid).setValue(localUser)

                                    goToMainActivity()
                                }
                            })
                    val parameters = Bundle()
                    parameters.putString("fields", "first_name,last_name,birthday,gender")
                    request.parameters = parameters
                    request.executeAsync()
                } else {
                    goToMainActivity()
                }
            }

            override fun onCancelled(error: DatabaseError?) {

            }
        })
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity().javaClass)
        startActivity(intent)
        finish()
    }
}

