package com.example.ticketkapal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class TicketPontianak : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_pontianak)

        // ===== AMBIL VIEW =====
        val edtTanggal = findViewById<EditText>(R.id.edtTanggal)
        val edtNama = findViewById<EditText>(R.id.edtNama)
        val edtNoPlat = findViewById<EditText>(R.id.edtNoPlat)
        val edtGolongan = findViewById<MaterialAutoCompleteTextView>(R.id.edtGolongan)
        val edtBerat = findViewById<EditText>(R.id.edtBerat)
        val edtHarga = findViewById<EditText>(R.id.edtHarga)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val tilGolongan = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilGolongan)

        val golonganList = arrayOf(
            "Golongan I",
            "Golongan II",
            "Golongan III",
            "Golongan IV",
            "Golongan V",
            "Golongan VI",
            "Golongan VII",
            "Golongan VIII"
        )

        val golAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, golonganList)
        edtGolongan.setAdapter(golAdapter)

        // ===== DATE PICKER =====
        val calendar = Calendar.getInstance()
        edtTanggal.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    edtTanggal.setText("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // ===== DATABASE =====
        val dbHelper = TicketDatabaseHelper(this)

        // ===== BUTTON SIMPAN =====
        btnSimpan.setOnClickListener {

            // ===== VALIDASI INPUT =====
            if (edtTanggal.text.toString().isEmpty()) {
                edtTanggal.error = "Tanggal berlaku harus diisi terlebih dahulu"
                edtTanggal.requestFocus()
                return@setOnClickListener
            }

            if (edtNama.text.toString().isEmpty()) {
                edtNama.error = "Nama harus diisi terlebih dahulu"
                edtNama.requestFocus()
                return@setOnClickListener
            }

            if (edtNoPlat.text.toString().isEmpty()) {
                edtNoPlat.error = "Mobil harus diisi terlebih dahulu"
                edtNoPlat.requestFocus()
                return@setOnClickListener
            }

            tilGolongan.error = null
            if (edtGolongan.text.toString().isEmpty()) {
                tilGolongan.error = "Golongan tidak boleh kosong"
                edtGolongan.requestFocus()
                return@setOnClickListener
            }

            if (edtBerat.text.toString().isEmpty()) {
                edtBerat.error = "Berat kendaraan harus diisi"
                edtBerat.requestFocus()
                return@setOnClickListener
            }

            if (edtHarga.text.toString().isEmpty()) {
                edtHarga.error = "Harga harus diisi"
                edtHarga.requestFocus()
                return@setOnClickListener
            }

            // ===== GENERATE KODE =====
            val db = dbHelper.writableDatabase
            val kodeBooking = generateKodeBooking()
            val noTiket = generateNoTiket(db)

            // ===== SIMPAN KE DATABASE =====
            val sql = """
                INSERT INTO ticket 
                (kode_booking, no_tiket, tanggal_berlaku, nama, no_polisi, golongan, berat, harga)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            db.execSQL(
                sql,
                arrayOf(
                    kodeBooking,
                    noTiket,
                    edtTanggal.text.toString(),
                    edtNama.text.toString(),
                    edtNoPlat.text.toString(),
                    edtGolongan.text.toString(),
                    edtBerat.text.toString(),
                    edtHarga.text.toString()
                )
            )

            Toast.makeText(
                this,
                "Ticket berhasil disimpan\nKode Booking: $kodeBooking\nNo Tiket: $noTiket",
                Toast.LENGTH_LONG
            ).show()

            db.close()
        }

        val btnLihatTicket = findViewById<Button>(R.id.btnLihatTicket)

        btnLihatTicket.setOnClickListener {
            val intent = Intent(this, TicketListActivity::class.java)
            startActivity(intent)
        }
    }

    // ===== GENERATE KODE BOOKING (RANDOM HURUF + ANGKA) =====
    private fun generateKodeBooking(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }

    // ===== GENERATE NO TIKET (AUTO INCREMENT) =====
    private fun generateNoTiket(db: android.database.sqlite.SQLiteDatabase): String {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ticket", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0) + 1
        cursor.close()
        return String.format("%011d", count)
    }
}
