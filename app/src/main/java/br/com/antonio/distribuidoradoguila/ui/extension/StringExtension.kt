package br.com.antonio.distribuidoradoguila.ui.extension

import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.DATA_PADRAO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.HORA_PADRAO
import java.text.SimpleDateFormat
import java.util.*

fun Date.converteParaData() : String {
    val formatoBrasileiro = SimpleDateFormat(DATA_PADRAO,
    Locale.getDefault()).format(this)
    return formatoBrasileiro

}

fun Date.converteParaHora() : String {
    val formatoBrasileiro = SimpleDateFormat(HORA_PADRAO,
        Locale.getDefault()).format(this)
    return formatoBrasileiro

}
