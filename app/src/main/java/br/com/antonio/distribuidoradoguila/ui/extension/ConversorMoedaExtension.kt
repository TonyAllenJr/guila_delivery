package br.com.antonio.distribuidoradoguila.ui.extension

import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.BRASIL
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CIFRA
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PORTUGUES
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale

fun BigDecimal.moedaBrasileira() : String{
    val moedaBrasileira =
        DecimalFormat.getCurrencyInstance(Locale(PORTUGUES, BRASIL))
        return moedaBrasileira.format(this).replace(CIFRA, CIFRA )

}