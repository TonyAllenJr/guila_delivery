package br.com.antonio.distribuidoradoguila.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.repository.ProdutoRepository

class FormularioProdutoViewModel(private val repository: ProdutoRepository,
) : ViewModel() {

    fun salva(produto: Produto) : LiveData<Boolean> = repository.salva(produto)

    fun buscaPorId(id: String): LiveData<Produto> = repository.buscaPorId(id)


}