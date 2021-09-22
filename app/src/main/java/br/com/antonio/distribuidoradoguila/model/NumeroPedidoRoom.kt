package br.com.antonio.distribuidoradoguila.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class NumeroPedidoRoom(
    @PrimaryKey(autoGenerate = true)
    val numeroPedido: Long = 0
) {
}