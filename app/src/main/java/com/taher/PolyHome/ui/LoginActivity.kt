package com.taher.PolyHome.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.taher.PolyHome.R
import com.taher.PolyHome.models.UserLoginRequest
import com.taher.PolyHome.models.UserLoginResponse
import com.taher.PolyHome.network.Api


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.txtMail)
        val passwordInput = findViewById<EditText>(R.id.txtPassword)
        val loginButton = findViewById<Button>(R.id.btnConect)
        val registerButton = findViewById<Button>(R.id.btnGoToRegister)

        // Bouton "Connexion"
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Veuillez entrer vos identifiants", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }


    private fun loginUser(email: String, password: String) {
        val api = Api()
        val loginRequest = UserLoginRequest(login = email, password = password)

        Log.d("LoginActivity", "Tentative de connexion avec $email")
        Log.d("LoginActivity", "Payload JSON: ${api.toJSON(loginRequest)}")

        api.post<UserLoginRequest, UserLoginResponse>(
            "https://polyhome.lesmoulinsdudev.com/api/users/auth",
            loginRequest,
            onSuccess = { code, response ->
                Log.d("LoginActivity", "Code de réponse: $code")

                if (code == 200 && response != null) {
                    val token = response.token
                    Log.d("LoginActivity", "Token reçu: ${token.take(10)}...")

                    // Sauvegarde du token et login utilisateur
                    val sharedPref = getSharedPreferences("PolyHome", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("token", token)
                        putString("userLogin", email)
                        apply()
                    }

                    runOnUiThread {
                        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HousesActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            securityToken = null
        )
    }
}
