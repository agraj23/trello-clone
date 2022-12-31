package com.example.trello.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.trello.R
import com.example.trello.firebase.FirestoreClass
import com.example.trello.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.jar.Attributes

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setupActionBar()

    }


    fun userRegisteredSuccess(){
        Toast.makeText(this@SignUpActivity, "You have successfully registered",
        Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    /* this function sets up the toolbar in sign up activity and also it displays the back arrow
     so that user can go back to previous activity */
    private fun setupActionBar() {
        val toolbar_sign_up_activity : Toolbar =findViewById(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24dp)
        }
        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
        val btn_sign_up : Button = findViewById(R.id.btn_sign_up)
        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    // to validate the details entered by the user
    private fun validateForm(name : String, email : String, password : String) : Boolean {
        return when {
            TextUtils.isEmpty(name) ->{
                showErrorSnackBar("Please enter name")
                false
            }
            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter email")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter password")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerUser() {
        val name : String = findViewById<EditText>(R.id.et_name).text.toString().trim{ it <= ' '}
        val email : String = findViewById<EditText>(R.id.et_email).text.toString().trim{ it <= ' '}
        val password : String = findViewById<EditText>(R.id.et_password).text.toString().trim{ it <= ' '}

        if(validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().
            createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid,name,registeredEmail)
                    FirestoreClass().registerUser(this,user)
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }

            }
        }

    }
}