package br.com.antonio.distribuidoradoguila.ui.extension

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import com.google.android.material.snackbar.Snackbar

fun View.snackBar(mensagem: String, duracao: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(
            this,
            mensagem,
            duracao
    ).show()
}


fun View.escondeTeclado() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, VALOR_CONSTANTE_ZERO)

}


