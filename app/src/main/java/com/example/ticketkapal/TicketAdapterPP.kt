package com.example.ticketkapal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class TicketAdapterPP(
    private val list: List<TicketModel>
) : RecyclerView.Adapter<TicketAdapterPP.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kode = view.findViewById<TextView>(R.id.txtKode)
        val tiket = view.findViewById<TextView>(R.id.txtNoTiket)

        val tanggalBuat = view.findViewById<TextView>(R.id.txtTanggalBuat)
        val tanggal = view.findViewById<TextView>(R.id.txtTanggal)
        val nama = view.findViewById<TextView>(R.id.txtNama)
        val plat = view.findViewById<TextView>(R.id.txtPlat)
        val golongan = view.findViewById<TextView>(R.id.txtGolongan)
        val berat = view.findViewById<TextView>(R.id.txtBerat)
        val harga = view.findViewById<TextView>(R.id.txtHarga)
        val btnPrint = view.findViewById<Button>(R.id.btnPrint)


        val rowRoot = view.findViewById<LinearLayout>(R.id.rowRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val t = list[position]

        holder.kode.text = t.kodeBooking
        holder.tiket.text = t.noTiket
        holder.tanggalBuat.text = t.tanggalBuat
        holder.tanggal.text = t.tanggalBerlaku
        holder.nama.text = t.nama
        holder.plat.text = t.noPolisi
        holder.golongan.text = t.golongan
        holder.berat.text = t.berat
        holder.harga.text = t.harga

        if (position % 2 == 0) {
            holder.rowRoot.setBackgroundResource(R.drawable.bg_row_even)
        } else {
            holder.rowRoot.setBackgroundResource(R.drawable.bg_row_odd)
        }

        holder.btnPrint.setOnClickListener {
            val ctx = holder.itemView.context

            val previewText = """
                Kode Booking : ${t.kodeBooking}
                No Tiket     : ${t.noTiket}
                Tanggal Berlaku  : ${t.tanggalBerlaku}
                Nama         : ${t.nama}
                No Polisi    : ${t.noPolisi}
                Golongan     : ${t.golongan}
                Berat        : ${t.berat}
                Harga        : ${t.harga}
            """.trimIndent()

            AlertDialog.Builder(ctx)
                .setTitle("Preview Ticket")
                .setMessage(previewText)
                .setNegativeButton("Tutup", null)
                .setPositiveButton("Print") { _, _ ->
                    TicketPrintHelperPP.printTicket(ctx, t)
                }
                .show()
        }
    }
}