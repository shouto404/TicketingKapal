package com.example.ticketkapal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.BitmapFactory
import android.graphics.RectF
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
                val pdfDocument = PdfDocument()

                // Ukuran A4 kira-kira (points). Cukup untuk text.
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)

                val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale("id", "ID"))
                val waktuCetak = dateTimeFormat.format(Date())

                drawTicket(page.canvas, t, waktuCetak)

                pdfDocument.finishPage(page)

                try {
                    FileOutputStream(destination.fileDescriptor).use { out ->
                        pdfDocument.writeTo(out)
                    }
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.message)
                } finally {
                    pdfDocument.close()
                }
            }

            private fun dpToPx(context: Context, dp: Float): Float {
                return dp * context.resources.displayMetrics.density
            }

            private fun drawTicket(canvas: Canvas, t: TicketModel, waktuCetak: String) {

                val paintTitle = Paint().apply {
                    textSize = 18f
                    isFakeBoldText = true
                    typeface = Typeface.MONOSPACE
                }
                val paint = Paint().apply {
                    textSize = 14f
                    typeface = Typeface.MONOSPACE
                }
                val paintSubTitle = Paint().apply {
                    textSize = 14f
                }

                var y = 50f
                val x = 50f
                val line = 26f


                // Paint tanggal & jam
                val paintDate = Paint().apply {
                    textSize = 12f
                }

                val dateText = "Dicetak : $waktuCetak"

                // Rata tengah
                val dateWidth = paintDate.measureText(dateText)
                val dateX = (canvas.width - dateWidth) / 2

                canvas.drawText(dateText, dateX, y, paintDate)
                y += line

                // ===== LOGO KANAN (di area header) =====
                val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_company)

                // ukuran logo (misal 40dp)
                val logoSize = dpToPx(context, 30f)

                // posisikan kanan atas dekat judul
                val logoLeft = canvas.width - logoSize - dpToPx(context, 20f)
                val logoTop = dpToPx(context, 18f)
                val logoRect = RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)

                canvas.drawBitmap(logoBitmap, null, logoRect, null)


                // ===== WATERMARK TILE (BANYAK KECIL) =====
                canvas.save()
                try {
                    // miringkan watermark (opsional)
                    canvas.rotate(-30f, canvas.width / 2f, canvas.height / 2f)

                    val wmBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.watermark_logo)
                    val wmSize = dpToPx(context, 60f)
                    val spacing = dpToPx(context, 90f)

                    val wmPaint = Paint().apply { alpha = 25 }

                    var yPos = -wmSize
                    while (yPos < canvas.height + wmSize) {
                        var xPos = -wmSize
                        while (xPos < canvas.width + wmSize) {
                            val rect = RectF(xPos, yPos, xPos + wmSize, yPos + wmSize)
                            canvas.drawBitmap(wmBitmap, null, rect, wmPaint)
                            xPos += spacing
                        }
                        yPos += spacing
                    }
                } finally {
                    canvas.restore()
                }

                val title  = "BORDING PASS"
                val title2 = "UNTUK KENDARAAN"
                val bottom = "CV. WHENDY CHALISCO LOGISTIC"
                val bottom1 = "Jl. PANGLIMA AIM KOMP. GRIYA PRATAM NO. 18"
                val bottom2 = "WA: 08115612525"
                val pageWidth = canvas.width

                paintTitle.isFakeBoldText = true
                val textWidth = paintTitle.measureText(title)
                val centerX = (pageWidth - textWidth) / 2

                canvas.drawText(title, centerX, y, paintTitle)
                y += 30f

                val textWidth2 = paintTitle.measureText(title2)
                val centerX2 = (pageWidth - textWidth2) / 2
                canvas.drawText(title2, centerX2, y, paintTitle)
                y += 40f

//                // Baris 1
//                val titleWidth1 = paintTitle.measureText(title)
//                val titleX1 = (canvas.width - titleWidth1) / 2
//                canvas.drawText(titleWidth1, titleX1, y, paintTitle)
//                y += 30f
//
//// Baris 2
//                val titleWidth2 = paintSubTitle.measureText(title2)
//                val titleX2 = (canvas.width - titleWidth2) / 2
//                canvas.drawText(titleWidth2, titleX2, y, paintSubTitle)
//                y += 40f

                canvas.drawText("--------------------------------------------------------------------------------------------------------------------------", x, y, paint); y += line
                canvas.drawText("Keberangkatan", x, y, paint); y += line
                canvas.drawText("PONTIANAK - PATIMBAN", x, y, paint); y += line
                canvas.drawText("Tanggal Berlaku : ${t.tanggalBerlaku}", x, y, paint); y += line
                canvas.drawText("Kode Booking : ${t.kodeBooking}", x, y, paint); y += line
                canvas.drawText("No Tiket     : ${t.noTiket}", x, y, paint); y += line
                canvas.drawText("Nama         : ${t.nama}", x, y, paint); y += line
                canvas.drawText("No Polisi    : ${t.noPolisi}", x, y, paint); y += line
                canvas.drawText("Golongan     : ${t.golongan}", x, y, paint); y += line
                canvas.drawText("Berat        : ${t.berat}", x, y, paint); y += line
                canvas.drawText("Harga        : ${t.harga}", x, y, paint); y += line
                canvas.drawText("--------------------------------------------------------------------------------------------------------------------------", x, y, paint); y += line

                y += line
                canvas.drawText("Keterangan:", x, y, paintTitle); y += line
                canvas.drawText("- Tunjukkan boarding pass saat naik kapal.", x, y, paint); y += line
                canvas.drawText("- Pintu kapal akan ditutup 30 menit sebelum keberangkatan.", x, y, paint); y += line
                canvas.drawText("- Harga tiket sudah termasuk asuransi.", x, y, paint); y += line
                canvas.drawText("- Tiket tidak dapat dibatalkan.", x, y, paint); y += line
                canvas.drawText("--------------------------------------------------------------------------------------------------------------------------", x, y, paint); y += line

                y += line * 3
                val textWidth3 = paintTitle.measureText(bottom)
                val centerX3 = (pageWidth - textWidth3) / 2
                canvas.drawText(bottom, centerX3, y, paintTitle); y += line
                val textWidth4 = paintTitle.measureText(bottom1)
                val centerX4 = (pageWidth - textWidth4) / 2
                canvas.drawText(bottom1, centerX4, y, paintTitle); y += line
                val textWidth5 = paintTitle.measureText(bottom2)
                val centerX5 = (pageWidth - textWidth5) / 2
                canvas.drawText(bottom2, centerX5, y, paintTitle); y += line


            }
        }

        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, adapter, attributes)
    }
}