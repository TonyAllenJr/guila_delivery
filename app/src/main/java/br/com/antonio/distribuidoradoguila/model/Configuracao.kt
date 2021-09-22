package br.com.antonio.distribuidoradoguila.model

import java.math.BigDecimal

class Configuracao(
    val id: String? = null,
    val taxaEntrega: BigDecimal = BigDecimal.ZERO,
    val pedidoMinimo: BigDecimal = BigDecimal.ZERO,
    val appAtivo: Boolean = false
)