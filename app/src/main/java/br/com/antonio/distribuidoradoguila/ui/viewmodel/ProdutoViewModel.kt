package br.com.antonio.distribuidoradoguila.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.repository.ProdutoRepository
import java.math.BigDecimal

class ProdutoViewModel(
        private val repository: ProdutoRepository,
        ) : ViewModel() {

    fun buscaTodos() : LiveData<List<Produto>> = repository.buscaTodos()

    fun buscaEstoqueZero() : LiveData<List<Produto>> = repository.buscaEstoqueZero()

    fun buscaPorNome() : LiveData<List<Produto>> = repository.buscaPorNome()

    fun buscaPorQuantidadeVendida() : LiveData<List<Produto>> = repository.buscaPorQuantidadeVendida()

    fun buscaPorValorVendido() : LiveData<List<Produto>> = repository.buscaPorValorVendido()

    fun buscaEstoqueProdutosPorNome() : LiveData<List<Produto>> = repository.buscaEstoqueProdutosPorNome()

    fun buscaEstoqueProdutosPorQuantidade() : LiveData<List<Produto>> = repository.buscaEstoqueProdutosPorQuantidade()

    fun atualizaQuantidadeEValorVendido(produtoId: String, quantidade: Int, valor: BigDecimal) =
        repository.atualizaQuantidadeEValorVendido(produtoId, quantidade, valor)

    fun zeraQuantidadeEValorVendido() =
        repository.zeraQuatidadeEValorVendido()

    fun remove(id: String) : LiveData<Boolean> = repository.remove(id)

    fun removeQuantidadeEstoque(id: String, quantidade: Int) = repository.removeQuantidadeEstoque(id, quantidade)

    fun adicionaQuantidadeEstoque(id: String, quantidade: Int) = repository.adicionaQuantidadeEstoque(id, quantidade)


}