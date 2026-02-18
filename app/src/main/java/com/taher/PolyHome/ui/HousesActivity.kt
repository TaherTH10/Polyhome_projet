package com.taher.PolyHome.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taher.PolyHome.R
import com.taher.PolyHome.adapters.HouseAdapter
import com.taher.PolyHome.models.AddUserToHouseRequest
import com.taher.PolyHome.models.House
import com.taher.PolyHome.network.Api


class HousesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HouseAdapter
    private val api = Api()
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        token = getSharedPreferences("PolyHome", Context.MODE_PRIVATE).getString("token", null)

        // Si le token est absent, on redirige vers l'écran de connexion
        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Configuration de la liste des maisons
        recyclerView = findViewById(R.id.recyclerViewHouses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HouseAdapter(
            emptyList(),
            onHouseClick = { house ->
                Intent(this, ChoiceActivity::class.java).apply {
                    putExtra("houseId", house.houseId)
                    startActivity(this)
                }
            },
            onManageUsersClick = { house ->
                Intent(this, UsersActivity::class.java).apply {
                    putExtra("houseId", house.houseId.toString())
                    startActivity(this)
                }
            }
        )
        recyclerView.adapter = adapter

        loadHouses()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_house -> {
                showAddHouseDialog()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Charge la liste des maisons depuis l'API.
     */
    private fun loadHouses() {
        api.get<List<House>>(
            "https://polyhome.lesmoulinsdudev.com/api/houses",
            onSuccess = { code, response ->
                runOnUiThread {
                    when {
                        code == 200 && response != null -> {
                            Log.d("HousesActivity", "Maisons reçues: ${response.size}")
                            adapter.updateHouses(response)
                        }
                        code == 403 -> {
                            Toast.makeText(this, "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show()
                            clearToken()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        else -> {
                            Toast.makeText(this, "Erreur lors du chargement des maisons", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            securityToken = token
        )
    }


    private fun showAddHouseDialog() {
        val input = EditText(this).apply {
            hint = "Entrez l'ID de la maison"
        }

        AlertDialog.Builder(this)
            .setTitle("Attribuer une maison")
            .setView(input)
            .setPositiveButton("Attribuer") { dialog, _ ->
                val houseIdText = input.text.toString()
                if (houseIdText.isNotEmpty()) {
                    try {
                        val houseId = houseIdText.toInt()
                        addUserToHouse(houseId)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "L'ID de la maison doit être un nombre", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
            .show()
    }


    private fun addUserToHouse(houseId: Int) {
        val userLogin = getUserLoginFromSharedPreferences()
        if (userLogin == null) {
            Toast.makeText(this, "Information utilisateur manquante", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AddUserToHouseRequest(userLogin)
        api.post<AddUserToHouseRequest>(
            "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
            request,
            onSuccess = { code ->
                runOnUiThread {
                    when (code) {
                        200 -> {
                            Toast.makeText(this, "Accès à la maison accordé", Toast.LENGTH_SHORT).show()
                            loadHouses()
                        }
                        403 -> Toast.makeText(this, "Accès interdit. Vous n'êtes pas propriétaire.", Toast.LENGTH_SHORT).show()
                        409 -> Toast.makeText(this, "Vous avez déjà accès à cette maison", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this, "Erreur lors de l'attribution (code $code)", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            securityToken = token
        )
    }


    private fun logout() {
        clearToken()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun clearToken() {
        getSharedPreferences("PolyHome", Context.MODE_PRIVATE).edit().apply {
            remove("token")
            remove("userLogin")
            apply()
        }
    }

    private fun getUserLoginFromSharedPreferences(): String? {
        return getSharedPreferences("PolyHome", Context.MODE_PRIVATE)
            .getString("userLogin", null)
    }
}
