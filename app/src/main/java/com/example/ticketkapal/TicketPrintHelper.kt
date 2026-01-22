package com.example.ticketkapal


import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketPrintHelper {

    fun printTicket(context: Context, t: TicketModel) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Ticket_${t.noTiket}"

        val adapter = object : PrintDocumentAdapter() {

            // ✅ Lebar thermal yang umum: 384px (58mm)
            private val pageWidthPx = 384

            // ✅ Tinggi dibuat panjang (naikkan kalau masih kepotong)
            private val pageHeightPx = 2800

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: android.os.Bundle?
            ) {
                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal,
                callback: WriteResultCallback
            ) {
                val pdf = PdfDocument()

                try {
                    val dpi = 203 // standar printer thermal
                    val widthPx = (58f / 25.4f * dpi).toInt()   // ≈ 464px
                    val heightPx = 1200
                    // Ukuran A4 kira-kira (points). Cukup untuk text.
                    val pageInfo = PdfDocument.PageInfo.Builder(widthPx, heightPx, 1).create()
                    val page = pdf.startPage(pageInfo)

                    val waktuCetak = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
                        .format(Date())

                    drawTicket(page.canvas, context, t, waktuCetak)

                    pdf.finishPage(page)

                    FileOutputStream(destination.fileDescriptor).use { out ->
                        pdf.writeTo(out)
                    }

                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                } finally {
                    pdf.close()
                }
            }

            private fun drawTicket(canvas: Canvas, context: Context, t: TicketModel, waktuCetak: String) {
                val x = 8f
                var y = 26f
                val line = 30f

                // ✅ Font dibuat BESAR untuk lebar 384px
                val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 30f
                    isFakeBoldText = true
                    typeface = Typeface.MONOSPACE
                }
                val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 24f
                    isFakeBoldText = true
                    typeface = Typeface.MONOSPACE
                }
                val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 22f
                    typeface = Typeface.MONOSPACE
                }
                val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 18f
                    typeface = Typeface.MONOSPACE
                }

                fun center(text: String, p: Paint) {
                    val cx = (canvas.width - p.measureText(text)) / 2f
                    canvas.drawText(text, cx, y, p)
                    y += line
                }

                fun hr() {
                    canvas.drawText("--------------------------------", x, y, normal)
                    y += line
                }

                // tanggal cetak
                center("Dicetak: $waktuCetak", small)
                y += 6

                // judul
                center("BOARDING PASS", title)
                center("UNTUK KENDARAAN", bold)
                hr()

                canvas.drawText("KEBERANGKATAN", x, y, bold); y += line
                canvas.drawText("PONTIANAK - PATIMBAN", x, y, bold); y += line
                hr()

                canvas.drawText("Tgl Buat   : ${t.tanggalBuat}", x, y, normal); y += line
                canvas.drawText("Tgl Berlaku: ${t.tanggalBerlaku}", x, y, normal); y += line
                canvas.drawText("Kode       : ${t.kodeBooking}", x, y, normal); y += line
                canvas.drawText("No Tiket   : ${t.noTiket}", x, y, normal); y += line
                canvas.drawText("Nama       : ${t.nama}", x, y, normal); y += line
                canvas.drawText("No Polisi  : ${t.noPolisi}", x, y, normal); y += line
                canvas.drawText("Golongan   : ${t.golongan}", x, y, normal); y += line
                canvas.drawText("Berat      : ${t.berat}", x, y, normal); y += line
                canvas.drawText("Harga      : ${t.harga}", x, y, normal); y += line

                hr()
                canvas.drawText("KETERANGAN:", x, y, bold); y += line
                canvas.drawText("- Tunjukkan boarding pass.", x, y, normal); y += line
                canvas.drawText("- Pintu ditutup 30 menit sebelum", x, y, normal); y += line
                canvas.drawText("  keberangkatan.", x, y, normal); y += line
                canvas.drawText("- Harga termasuk asuransi.", x, y, normal); y += line
                canvas.drawText("- Tiket tidak dapat dibatalkan.", x, y, normal); y += line
                hr()

                y += 10
                center("CV. WHENDY CHALISCO LOGISTIC", bold)
                center("Jl. PANGLIMA AIM KOMP. GRIYA PRATAM NO. 18", small)
                center("WA: 08115612525", bold)
            }
        }

        // ✅ Custom roll 58mm (mils) agar dialog print juga tau ini bukan A4
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, adapter, attributes)
    }
}