package br.com.antonio.distribuidoradoguila.model

import java.math.BigDecimal

data class ItemCarrinho(
    val id: String? = null,
    val nome: String = "",
    val quantidade: Int = 0,
    val imagemUrl: String = "",
    val preco: BigDecimal = BigDecimal.ZERO,
    val precoTotalPorItem: BigDecimal = BigDecimal.ZERO,
    val usuarioId: String? = null,
    val numeroPedido: Long = 0,
    val produtoId: String = "",
    val pedidoId: String = ""
)

