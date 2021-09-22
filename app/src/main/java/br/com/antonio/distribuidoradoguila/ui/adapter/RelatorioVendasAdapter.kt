package br.com.antonio.distribuidoradoguila.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.item_relatorio_vendas.view.*

class RelatorioVendasAdapter(
    private val context: Context,
    private val itemVendas : MutableList<Produto> = mutableListOf(),
) : RecyclerView.Adapter<RelatorioVendasAdapter.RelatorioVendasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatorioVendasViewHolder {
        val viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_relatorio_vendas, parent, false)
        return RelatorioVendasViewHolder(viewCriada)
    }

    override fun onBindViewHolder(holder: RelatorioVendasViewHolder, position: Int) {
        val carrinho = itemVendas[position]
        holder.vincula(carrinho)
    }

    override fun getItemCount() = itemVendas.size

    inner class RelatorioVendasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var itemVendas: Produto

        fun vincula(itemVendas: Produto) {
            this.itemVendas = itemVendas
            itemView.item_relatorio_vendas_produto.text = itemVendas.nome
            itemView.item_relatorio_vendas_quantidade.text = itemVendas.quantidadeVendida.toString()
            itemView.item_relario_vendas_valor.text = itemVendas.valorVendido.moedaBrasileira()
        }
    }

    fun atualiza(item: List<Produto>){
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, itemVendas.size)
        itemVendas.clear()
        itemVendas.addAll(item)
        notifyItemRangeInserted(VALOR_CONSTANTE_ZERO, itemVendas.size)
    }

    fun removeTodos() {
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, itemVendas.size)
        itemVendas.clear()

    }

}