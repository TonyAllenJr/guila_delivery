package br.com.antonio.distribuidoradoguila.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.antonio.distribuidoradoguila.repository.ProdutoRepository
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ProdutoViewModel

@Suppress("UNCHECKED_CAST")
class ListaProdutosViewModelFactory(
    private val repository: ProdutoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProdutoViewModel(repository) as T
    }

}