package br.com.antonio.distribuidoradoguila.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.antonio.distribuidoradoguila.model.ItemCarrinho
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.repository.CarrinhoRepository
import br.com.antonio.distribuidoradoguila.repository.ProdutoRepository

class CarrinhoViewModel(
        private val carrinhoRepository: CarrinhoRepository,
        private val produtoRepository: ProdutoRepository
) : ViewModel() {


    fun salva(itemCarrinho: ItemCarrinho, pedido: Pedido) = carrinhoRepository.salva(itemCarrinho, pedido)

    fun buscaProdutoPorId(id : String) = produtoRepository.buscaPorId(id)

    fun buscaItens(numeroPedido: Long, pedidoId: String) : LiveData<List<ItemCarrinho>> =
        carrinhoRepository.buscaItens(numeroPedido, pedidoId)

    fun buscaItensAdmin(numeroPedido: Long, usuario: String, pedidoId: String) : LiveData<List<ItemCarrinho>> =
        carrinhoRepository.buscaItensAdmin(numeroPedido, usuario, pedidoId)

    fun remove(itemCarrinhoId: String, pedidoId: String) : LiveData<Boolean> =
        carrinhoRepository.remove(itemCarrinhoId, pedidoId)

    fun buscaPorId(id: String, pedidoId: String) = carrinhoRepository.buscaPorId(id, pedidoId)

    fun atualizaNumeroPedido(pedido: Long, pedidoId: String) =
        carrinhoRepository.atualizaNumeroPedido(pedido, pedidoId)

}