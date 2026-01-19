package com.example.ticketkapal

data class TicketModel(
    val kodeBooking: String,
    val noTiket: String,
    val tanggalBuat: String,
    val tanggalBerlaku: String,
    val nama: String,
    val noPolisi: String,
    val golongan: String,
    val berat: String,
    val harga: String
)