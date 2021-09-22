package br.com.antonio.distribuidoradoguila.model

import java.math.BigDecimal

data class Produto(
    val id: String? = null,
    val nome: String = "",
    val imagemUrl: String = "",
    val categoria: String = "",
    val preco: BigDecimal = BigDecimal.ZERO,
    val estoque: Int = 0,
    val quantidadeVendida: Int = 0,
    val valorVendido: BigDecimal = BigDecimal.ZERO
)
