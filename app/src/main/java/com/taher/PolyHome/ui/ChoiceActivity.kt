package com.taher.PolyHome.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.taher.PolyHome.R



class ChoiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)


        val houseId = intent.getIntExtra("houseId", -1)


        val buttonGestionPrecise = findViewById<Button>(R.id.buttonGestionPrecise)
        buttonGestionPrecise.setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            intent.putExtra("houseId", houseId)
            startActivity(intent)
        }

        val buttonGestionGenerale = findViewById<Button>(R.id.buttonGestionGenerale)
        buttonGestionGenerale.setOnClickListener {
            val intent = Intent(this, OptionalCommandsActivity::class.java)
            intent.putExtra("houseId", houseId)
            startActivity(intent)
        }



    }
}
