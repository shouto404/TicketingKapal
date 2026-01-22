package com.example.ticketkapal

import android.app.DatePickerDialog
import android.database.Cursor
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class TicketListActivity : AppCompatActivity() {

    private lateinit var dbHelper: TicketDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketAdapter
    private val list = ArrayList<TicketModel>()

    private lateinit var edtDari: EditText
    private lateinit var edtSampai: EditText
    private lateinit var btnFilter: Button
    private lateinit var btnReset: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_list)

        dbHelper = TicketDatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerTicket)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TicketAdapter(list)
        recyclerView.adapter = adapter

        edtDari = findViewById(R.id.edtFilterDari)
        edtSampai = findViewById(R.id.edtFilterSampai)
        btnFilter = findViewById(R.id.btnFilterRange)
        btnReset = findViewById(R.id.btnResetRange)

        // ====== DatePicker yang "pasti jalan" (touch listener) ======
        edtDari.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showDatePicker(edtDari)
            }
            true // consume touch supaya EditText tidak fokus/keyboard
        }

        edtSampai.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                showDatePicker(edtSampai)
            }
            true
        }

        // Load awal: semua data
        loadTicketsAll()

        // Filter range berdasarkan tanggal_buat
        btnFilter.setOnClickListener {
            val dari = edtDari.text.toString().trim()
            val sampai = edtSampai.text.toString().trim()

            if (dari.isEmpty()) {
                edtDari.error = "Tanggal 'Dari' harus dipilih"
                edtDari.requestFocus()
                return@setOnClickListener
            }

            if (sampai.isEmpty()) {
                edtSampai.error = "Tanggal 'Sampai' harus dipilih"
                edtSampai.requestFocus()
                return@setOnClickListener
            }

            // validasi sederhana format dd/MM/yyyy
            if (!isValidDdMmYyyy(dari)) {
                edtDari.error = "Format harus dd/MM/yyyy"
                edtDari.requestFocus()
                return@setOnClickListener
            }
            if (!isValidDdMmYyyy(sampai)) {
                edtSampai.error = "Format harus dd/MM/yyyy"
                edtSampai.requestFocus()
                return@setOnClickListener
            }

            loadTicketsRangeTanggalBuat(dari, sampai)

            if (list.isEmpty()) {
                Toast.makeText(this, "Tidak ada tiket pada range $dari - $sampai", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset filter
        btnReset.setOnClickListener {
            edtDari.setText("")
            edtSampai.setText("")
            loadTicketsAll()
        }
    }

    private fun showDatePicker(target: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val dd = String.format("%02d", dayOfMonth)
                val mm = String.format("%02d", month + 1)
                target.setText("$dd/$mm/$year")
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadTicketsAll() {
        list.clear()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT kode_booking, no_tiket, tanggal_buat, tanggal_berlaku, nama, no_polisi, golongan, berat, harga
            FROM ticket
            ORDER BY id DESC
            """.trimIndent(),
            null
        )

        addCursorToList(cursor)
        cursor.close()
        db.close()

        adapter.notifyDataSetChanged()
    }

    /**
     * Filter RANGE berdasarkan tanggal_buat (format tersimpan dd/MM/yyyy)
     * Trick: convert dd/MM/yyyy -> yyyyMMdd saat query menggunakan substr()
     */
    private fun loadTicketsRangeTanggalBuat(dariDdMmYyyy: String, sampaiDdMmYyyy: String) {
        list.clear()
        val db = dbHelper.readableDatabase

        val dariKey = ddMMyyyyToKey(dariDdMmYyyy)     // yyyyMMdd
        val sampaiKey = ddMMyyyyToKey(sampaiDdMmYyyy) // yyyyMMdd

        val cursor: Cursor = db.rawQuery(
            """
            SELECT kode_booking, no_tiket, tanggal_buat, tanggal_berlaku, nama, no_polisi, golongan, berat, harga
            FROM ticket
            WHERE (
                substr(TRIM(tanggal_buat), 7, 4) || substr(TRIM(tanggal_buat), 4, 2) || substr(TRIM(tanggal_buat), 1, 2)
            ) BETWEEN ? AND ?
            ORDER BY id DESC
            """.trimIndent(),
            arrayOf(dariKey, sampaiKey)
        )

        addCursorToList(cursor)
        cursor.close()
        db.close()

        adapter.notifyDataSetChanged()
    }

    private fun addCursorToList(cursor: Cursor) {
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    TicketModel(
                        cursor.getString(0), // kode_booking
                        cursor.getString(1), // no_tiket
                        cursor.getString(2), // tanggal_berlaku
                        cursor.getString(3), // tanggal_buat
                        cursor.getString(4), // nama
                        cursor.getString(5), // no_polisi
                        cursor.getString(6), // golongan
                        cursor.getString(7), // berat
                        cursor.getString(8)  // harga
                    )
                )
            } while (cursor.moveToNext())
        }
    }

    private fun ddMMyyyyToKey(ddMMyyyy: String): String {
        // dd/MM/yyyy -> yyyyMMdd
        val parts = ddMMyyyy.split("/")
        val dd = parts[0]
        val mm = parts[1]
        val yyyy = parts[2]
        return yyyy + mm + dd
    }

    private fun isValidDdMmYyyy(value: String): Boolean {
        // validasi ringan: 10 char dan ada 2 slash
        if (value.length != 10) return false
        if (value[2] != '/' || value[5] != '/') return false
        val dd = value.substring(0, 2).toIntOrNull() ?: return false
        val mm = value.substring(3, 5).toIntOrNull() ?: return false
        val yyyy = value.substring(6, 10).toIntOrNull() ?: return false
        if (yyyy < 1900) return false
        if (mm !in 1..12) return false
        if (dd !in 1..31) return false
        return true
    }
}