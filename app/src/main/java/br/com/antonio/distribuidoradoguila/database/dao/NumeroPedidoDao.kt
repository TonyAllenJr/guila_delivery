package br.com.antonio.distribuidoradoguila.database.dao

import androidx.room.Dao
import androidx.room.Insert
import br.com.antonio.distribuidoradoguila.model.NumeroPedidoRoom

@Dao
interface NumeroPedidoDao {

    @Insert
    fun inclui(numeroPedidoRoom: NumeroPedidoRoom): Long

}