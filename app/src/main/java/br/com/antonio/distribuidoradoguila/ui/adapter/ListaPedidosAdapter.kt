package br.com.antonio.distribuidoradoguila.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.ui.extension.converteParaData
import br.com.antonio.distribuidoradoguila.ui.extension.converteParaHora
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.item_lista_pedidos.view.*

class ListaPedidosAdapter(
    private val context: Context,
    private val pedidos : MutableList<Pedido> = mutableListOf(),
    var onItemClickListener: (pedido : Pedido) -> Unit = {}
) : RecyclerView.Adapter<ListaPedidosAdapter.PedidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_lista_pedidos, parent, false)
        return PedidoViewHolder(viewCriada)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.vincula(pedido)
    }

    override fun getItemCount() = pedidos.size

    fun atualiza(pedidoEncontrado: List<Pedido>){
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, pedidos.size)
        pedidos.clear()
        pedidos.addAll(pedidoEncontrado)
        notifyItemRangeInserted(VALOR_CONSTANTE_ZERO, pedidos.size)
    }

    fun seleciona(pedidos: List<Pedido>, entregue: Boolean) {
        val filter = pedidos.filter { pedido -> pedido.entregue == entregue }
        atualiza(filter)
    }

    fun removeTodos() {
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, pedidos.size)
        pedidos.clear()

    }

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var pedido: Pedido

        init {
            itemView.setOnClickListener {
                if(::pedido.isInitialized) {
                    onItemClickListener(pedido)
                }
            }
        }

        fun vincula(pedido: Pedido) {
            this.pedido = pedido
            itemView.item_lista_pedidos_numero_pedido.text = pedido.numero.toString()
            itemView.item_lista_pedidos_data.text = pedido.data.converteParaData()
            itemView.item_lista_pedidos_hora.text = pedido.data.converteParaHora()
            itemView.item_lista_pedidos_tipo_pagamento.text = pedido.tipoPagamento
            itemView.item_lista_pedidos_valor.text = pedido.valorTotal.moedaBrasileira()
        }
    }


}