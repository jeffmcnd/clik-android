package xyz.mcnallydawes.clik

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    @BindView(R.id.email_et)
    lateinit var emailEt: EditText

    @BindView(R.id.password_et)
    lateinit var passwordEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        auth = FirebaseAuth.getInstance()
        authListener = FirebaseAuth.AuthStateListener {
            var user = it.currentUser
            if(user != null) {

            } else {

            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }

    @OnClick(R.id.sign_up_btn)
    fun signUp() {
        val email = emailEt.text.toString()
        val password = passwordEt.text.toString()
        val context = this
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
//                TODO: go to next activity, you're signed in!
                Toast.makeText(context, R.string.sign_up_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, R.string.sign_up_failure, Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.sign_in_btn)
    fun signIn() {
        val email = emailEt.text.toString()
        val password = passwordEt.text.toString()
        val context = this
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(it.isSuccessful) {
//                TODO: go to next activity, you're signed in!
                Toast.makeText(context, R.string.sign_in_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, R.string.sign_in_failure, Toast.LENGTH_LONG).show();
            }
        }
    }

}

