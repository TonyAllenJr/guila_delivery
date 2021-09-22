package br.com.antonio.distribuidoradoguila.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.antonio.distribuidoradoguila.repository.ProdutoRepository
import br.com.antonio.distribuidoradoguila.ui.viewmodel.FormularioProdutoViewModel

@Suppress("UNCHECKED_CAST")
class FormularioProdutoViewModelFactory(
        private val repository: ProdutoRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FormularioProdutoViewModel(repository) as T
    }
}