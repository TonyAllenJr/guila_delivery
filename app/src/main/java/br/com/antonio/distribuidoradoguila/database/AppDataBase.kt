package br.com.antonio.distribuidoradoguila.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.antonio.distribuidoradoguila.database.dao.NumeroPedidoDao
import br.com.antonio.distribuidoradoguila.model.NumeroPedidoRoom

@Database(entities = [NumeroPedidoRoom::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {

    abstract val numeroPedidoDao: NumeroPedidoDao

}