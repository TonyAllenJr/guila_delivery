package br.com.antonio.distribuidoradoguila.model

import java.math.BigDecimal
import java.util.*

data class Pedido(
    val id: String? = null,
    var numero: Long = 0,
    val tipoPagamento: String = "",
    val data : Date = Calendar.getInstance().time,
    val subTotal: BigDecimal = BigDecimal.ZERO,
    val valorTotal: BigDecimal = BigDecimal.ZERO,
    val taxaEntrega: BigDecimal = BigDecimal.ZERO,
    val usuarioId: String = "",
    val horaEnvio : Date = Calendar.getInstance().time,
    val horaEntrega: Date = Calendar.getInstance().time,
    val solicitado: Boolean = true,
    val enviado: Boolean = false,
    val entregue: Boolean = false,
    )
