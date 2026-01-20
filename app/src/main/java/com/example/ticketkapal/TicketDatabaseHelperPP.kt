package com.example.ticketkapal

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TicketDatabaseHelperPP(context: Context) :
    SQLiteOpenHelper(context, "ticket_kapal_pp.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE ticket_pp (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kode_booking TEXT,
                no_tiket TEXT,
                tanggal_buat TEXT,
                tanggal_berlaku TEXT,
                nama TEXT,
                no_polisi TEXT,
                golongan TEXT,
                berat TEXT,
                harga TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ticket_pp")
        onCreate(db)
    }
}
