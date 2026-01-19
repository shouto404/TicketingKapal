package com.example.ticketkapal

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TicketDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "ticket_kapal.db", null, 3) { // <-- naikkan versi

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE ticket (
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
        // Migrasi aman (tidak hapus data)
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE ticket ADD COLUMN tanggal_berlaku TEXT")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE ticket ADD COLUMN tanggal_buat TEXT")
        }
    }
}