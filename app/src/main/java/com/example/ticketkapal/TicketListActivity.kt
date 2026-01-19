package com.example.ticketkapal

import android.database.Cursor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.HorizontalScrollView

class TicketListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_list)

        val headerScroll = findViewById<HorizontalScrollView>(R.id.headerScroll)
        val bodyScroll = findViewById<HorizontalScrollView>(R.id.bodyScroll)

        bodyScroll.viewTreeObserver.addOnScrollChangedListener {
            headerScroll.scrollTo(bodyScroll.scrollX, 0)
        }


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTicket)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dbHelper = TicketDatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val list = ArrayList<TicketModel>()

        val cursor: Cursor = db.rawQuery(
            "SELECT kode_booking, no_tiket, tanggal_berlaku, nama, no_polisi, golongan, berat, harga FROM ticket",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    TicketModel(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        recyclerView.adapter = TicketAdapter(list)
    }
}