package br.com.antonio.distribuidoradoguila.ui.extension

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.carregaImagem(
    endereco: String
) {
    Glide.with(this)
        .load(endereco)
        .fitCenter()
        .into(this)
}