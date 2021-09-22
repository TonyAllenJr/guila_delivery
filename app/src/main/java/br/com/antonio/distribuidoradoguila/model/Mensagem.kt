package br.com.antonio.distribuidoradoguila.model

import java.util.Calendar
import java.util.Date

data class Mensagem(
    val descricao: String = "",
    val data : Date = Calendar.getInstance().time,
    val nome: String = "",
    val email: String = "",
    val telefone: String = "",
    val usuarioId: String = ""

)