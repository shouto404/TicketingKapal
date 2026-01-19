package com.example.ticketkapal

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import jxl.Workbook
import jxl.write.Label
import jxl.write.WritableWorkbook

class TicketListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketAdapter
    private val list = ArrayList<TicketModel>()
    private lateinit var dbHelper: TicketDatabaseHelper

    private lateinit var exportLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_list)

        dbHelper = TicketDatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerTicket)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TicketAdapter(list)
        recyclerView.adapter = adapter

        // ====== (kalau kamu sudah punya filter range / single, biarkan) ======
        // loadTicketsAll() atau loadTicketsRangeTanggalBuat(...)
        loadTicketsAll()

        val btnExportExcel = findViewById<Button>(R.id.btnExportExcel)

        // Launcher untuk pilih lokasi simpan file
        exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    exportCurrentListToExcel(uri)
                }
            }
        }

        btnExportExcel.setOnClickListener {
            if (list.isEmpty()) {
                Toast.makeText(this, "Data kosong, tidak bisa export.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("id", "ID")).format(Date())
            val fileName = "TicketKapal_$time.xls"

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            exportLauncher.launch(intent)
        }
    }

    // ===================== LOAD DATA (contoh all) =====================
    private fun loadTicketsAll() {
        list.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT kode_booking, no_tiket, tanggal_berlaku, tanggal_buat, nama, no_polisi, golongan, berat, harga
            FROM ticket
            ORDER BY id DESC
            """.trimIndent(), null
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
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)
                    )
                )
            } while (cursor.moveToNext())
        }
    }

    // ===================== EXPORT EXCEL =====================
    private fun exportCurrentListToExcel(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { out ->

                val workbook: WritableWorkbook = Workbook.createWorkbook(out)
                val sheet = workbook.createSheet("Daftar Tiket", 0)

                val headers = arrayOf(
                    "Kode Booking",
                    "No Tiket",
                    "Tanggal Berlaku",
                    "Tanggal Buat",
                    "Nama",
                    "No Polisi",
                    "Golongan",
                    "Berat",
                    "Harga"
                )

                // header row
                for (c in headers.indices) {
                    sheet.addCell(Label(c, 0, headers[c]))
                }

                // data rows
                for (i in list.indices) {
                    val t = list[i]
                    val r = i + 1

                    sheet.addCell(Label(0, r, t.kodeBooking))
                    sheet.addCell(Label(1, r, t.noTiket))
                    sheet.addCell(Label(2, r, t.tanggalBerlaku))
                    sheet.addCell(Label(3, r, t.tanggalBuat))
                    sheet.addCell(Label(4, r, t.nama))
                    sheet.addCell(Label(5, r, t.noPolisi))
                    sheet.addCell(Label(6, r, t.golongan))
                    sheet.addCell(Label(7, r, t.berat))
                    sheet.addCell(Label(8, r, t.harga))
                }

                workbook.write()
                workbook.close()
            }

            Toast.makeText(this, "Export Excel berhasil ✅", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export gagal: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}