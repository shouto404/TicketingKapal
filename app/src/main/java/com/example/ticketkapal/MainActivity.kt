package com.example.ticketkapal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btnPontianakPatimban = findViewById<Button>(R.id.btnPontianakPatimban)
        val btnPatimbanPontianak = findViewById<Button>(R.id.btnPatimbanPontianak)


        btnPontianakPatimban.setOnClickListener {
            val intent = Intent(this, TicketPontianak::class.java)
            intent.putExtra("RUTE", "Pontianak - Patimban")
            startActivity(intent)
        }


        btnPatimbanPontianak.setOnClickListener {
            val intent = Intent(this, TicketPatimban::class.java)
            intent.putExtra("RUTE", "Patimban - Pontianak")
            startActivity(intent)
        }
    }
}