package br.com.antonio.distribuidoradoguila.model

enum class TipoPagamento(val toString: String) {
    DINHEIRO("Dinheiro"), PIX("Pix"),
    CARTAO_DE_DEBITO("Cartão de Débito"), CARTAO_DE_CREDITO("Cartão de Crédito")


}
