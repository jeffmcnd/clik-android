package xyz.mcnallydawes.clik

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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

    @BindView(R.id.login_progress)
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        auth = FirebaseAuth.getInstance()
        authListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if(user != null) {
                goToMainActivity()
            } else {
                progressBar.visibility = View.GONE
                facebookBtn.visibility = View.VISIBLE
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
                initUserInDb(it.result.user.uid)
            } else {
                Toast.makeText(context, R.string.sign_in_failure, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initUserInDb(uid: String) {
        val usersRef = database.getReference("users")
        val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), {
            jsonObject, graphResponse ->
            if (jsonObject != null && graphResponse.error == null) {
                val localUser = User()
                localUser.email = jsonObject.getString("email")
                localUser.info = ""
                localUser.career = ""
                localUser.school = ""
                localUser.firstName = jsonObject.getString("first_name")
                localUser.lastName = jsonObject.getString("last_name")
                localUser.searchName = localUser.firstName.toLowerCase() + " " + localUser.lastName.toLowerCase()
                localUser.gender = jsonObject.getString("gender")

                if(jsonObject.has("picture")) {
                    val imageUrl = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url")
                    localUser.imageUrl = imageUrl
                    localUser.smallImageUrl = imageUrl
                }
                if(localUser.gender == Constants.GENDER_MALE) localUser.lookingFor = Constants.GENDER_FEMALE
                if(localUser.gender == Constants.GENDER_FEMALE) localUser.lookingFor = Constants.GENDER_MALE

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
                        diff--
                    }

                    localUser.age = diff
                    localUser.birthday = birthday

                    if(localUser.age > 21) localUser.startAge = localUser.age - 3
                    else localUser.startAge = 18
                    if(localUser.age < 52) localUser.endAge = localUser.age + 3
                    else localUser.endAge = 55

                    usersRef.child(uid).setValue(localUser)

                    goToMainActivity()

                } catch (e: Exception) {

                }
            }
        })
        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,email,birthday,gender,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity().javaClass)
        startActivity(intent)
        finish()
    }
}

