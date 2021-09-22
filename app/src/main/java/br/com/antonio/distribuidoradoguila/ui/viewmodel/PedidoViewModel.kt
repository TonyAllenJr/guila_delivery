package br.com.antonio.distribuidoradoguila.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.antonio.distribuidoradoguila.model.NumeroPedido
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.repository.PedidoRepository
import br.com.antonio.distribuidoradoguila.repository.Resource
import java.math.BigDecimal
import java.util.*

class PedidoViewModel(
    private val repository: PedidoRepository
) : ViewModel() {

    fun salva(pedido: Pedido): LiveData<Resource<Boolean>> = repository.salva(pedido)

    fun buscaPedidoEmAberto() : LiveData<List<Pedido>> = repository.buscaPedidoEmAberto()

    fun buscaTodos(): LiveData<List<Pedido>> = repository.buscaTodos()

    fun buscaPedidosFinalizados() : LiveData<List<Pedido>> = repository.buscaPedidosFinalizados()

    fun buscaPedidoPorIdUsuario(id: String) : LiveData<Pedido> = repository.buscaPedidoPorIdUsuario(id)

    fun buscaPedidoPorIdAdmin(id: String, usuario: String) : LiveData<Pedido> = repository.buscaPedidoPorIdAdmin(id, usuario)

    fun buscaUltimoPedido() = repository.buscaUltimoPedido()

    fun buscaUsuarioIdContador(numeroPedido: Long) : LiveData<String> = repository.buscaUsuarioIdContador(numeroPedido)

    fun salvaNumeroPedido(numeroPedido: NumeroPedido) = repository.salvaNumeroPedido(numeroPedido)

    fun atualizaEnvio(id: String, pedido: String, hora: Date) = repository.atualizaEnvio(id, pedido, hora)

    fun atualizaEntrega(id: String, pedido: String, hora: Date) = repository.atualizaEntrega(id, pedido, hora)

    fun atualizaDados(pedido: Pedido, pedidoId: String) =
        repository.atualizaDados(pedido, pedidoId)

    fun atualizaTipoPagamento(tipoPagamento: String, valor: BigDecimal) =
        repository.atualizaTipoPagamento(tipoPagamento, valor)

    fun buscaTipoPagamento(tipoPagamento: String): LiveData<BigDecimal> =
        repository.buscaTipoPagamento(tipoPagamento)

    fun remove(usuarioId: String, pedidoId: String) = repository.remove(usuarioId, pedidoId)


}