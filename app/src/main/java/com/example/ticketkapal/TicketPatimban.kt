package com.example.ticketkapal

import android.app.DatePickerDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
class TicketPatimban : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_patimban)

        val edtTanggal = findViewById<EditText>(R.id.edtTanggal)
        val edtNama = findViewById<EditText>(R.id.edtNama)
        val edtNoPlat = findViewById<EditText>(R.id.edtNoPlat)
        val edtGolongan = findViewById<MaterialAutoCompleteTextView>(R.id.edtGolongan)
        val edtBerat = findViewById<EditText>(R.id.edtBerat)
        val edtHarga = findViewById<EditText>(R.id.edtHarga)

        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val btnLihatTicket = findViewById<Button>(R.id.btnLihatTicket)

        val golonganList = arrayOf(
            "Golongan I",
            "Golongan II A",
            "Golongan II B",
            "Golongan III",
            "Golongan IV A",
            "Golongan IV B",
            "Golongan V A",
            "Golongan V B",
            "Golongan V C",
            "Golongan VI A",
            "Golongan VI B",
            "Golongan VI C",
            "Golongan VII A",
            "Golongan VII B",
            "Golongan VII C",
            "Golongan VIII",
            "Golongan AB"
        )

        val golAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, golonganList)
        edtGolongan.setAdapter(golAdapter)

        // Date picker tanggal berlaku
        val calendar = Calendar.getInstance()
        edtTanggal.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val dd = String.format("%02d", dayOfMonth)
                    val mm = String.format("%02d", month + 1)
                    edtTanggal.setText("$dd/$mm/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dbHelper = TicketDatabaseHelperPP(this)

        btnSimpan.setOnClickListener {
            // Validasi
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
            if (edtGolongan.text.toString().isEmpty()) {
                edtGolongan.error = "Golongan tidak boleh kosong"
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

            val db = dbHelper.writableDatabase
            val kodeBooking = generateKodeBooking()

            val (noTiket, noUrut) = generateNoTiketPontianak(db)

            // tanggal buat otomatis (dd/MM/yyyy)
            val tanggalBuat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(Date())

            val sql = """
                INSERT INTO ticket_pp
                (kode_booking, no_tiket, no_urut, tanggal_buat, tanggal_berlaku, nama, no_polisi, golongan, berat, harga)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            db.execSQL(
                sql,
                arrayOf(
                    kodeBooking,
                    noTiket,
                    noUrut.toString(),
                    tanggalBuat,
                    edtTanggal.text.toString(),
                    edtNama.text.toString(),
                    edtNoPlat.text.toString(),
                    edtGolongan.text.toString(),
                    edtBerat.text.toString(),
                    edtHarga.text.toString()
                )
            )

            Toast.makeText(this, "Tiket berhasil disimpan", Toast.LENGTH_SHORT).show()
            db.close()
        }

        btnLihatTicket.setOnClickListener {
            startActivity(android.content.Intent(this, TicketListPatimbanActivity::class.java))
        }
    }

    private fun generateKodeBooking(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    private fun generateNoTiket(db: android.database.sqlite.SQLiteDatabase): String {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ticket_pp", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0) + 1
        cursor.close()
        return String.format("%011d", count)
    }

    private fun generateNoTiketPontianak(db: SQLiteDatabase): Pair<String, Int> {
        val prefix = "TKT-PTBN-PNK"
        val cal = Calendar.getInstance()
        val bulan = String.format("%02d", cal.get(Calendar.MONTH) + 1) // 01-12
        val tahun = cal.get(Calendar.YEAR).toString()

        // cari urutan terakhir untuk bulan+tahun ini
        val cursor = db.rawQuery(
            """
        SELECT MAX(no_urut) 
        FROM ticket_pp
        WHERE no_tiket LIKE ?
        """.trimIndent(),
            arrayOf("$prefix-$bulan-$tahun-%")
        )

        var last = 0
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            last = cursor.getInt(0)
        }
        cursor.close()

        val next = last + 1
        val noTiket = "$prefix-$bulan-$tahun-$next"
        return Pair(noTiket, next)
    }
}