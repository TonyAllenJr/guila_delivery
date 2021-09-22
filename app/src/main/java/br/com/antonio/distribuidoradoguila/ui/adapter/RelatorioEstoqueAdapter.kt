package br.com.antonio.distribuidoradoguila.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.item_relatorio_estoque.view.*

class RelatorioEstoqueAdapter(
    private val context: Context,
    private val itemEstoque : MutableList<Produto> = mutableListOf(),
    var onItemClickListener: (produto: Produto) -> Unit = {}
) : RecyclerView.Adapter<RelatorioEstoqueAdapter.RelatorioEstoqueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatorioEstoqueViewHolder {
        val viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_relatorio_estoque, parent, false)
        return RelatorioEstoqueViewHolder(viewCriada)
    }

    override fun onBindViewHolder(holder: RelatorioEstoqueViewHolder, position: Int) {
        val produto = itemEstoque[position]
        holder.vincula(produto)
    }

    override fun getItemCount() = itemEstoque.size

    inner class RelatorioEstoqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var itemEstoque: Produto

        init {
            itemView.setOnClickListener {
                if (::itemEstoque.isInitialized) {
                    onItemClickListener(itemEstoque)
                }
            }
        }

        fun vincula(itemEstoque: Produto) {
            this.itemEstoque = itemEstoque
            itemView.item_relatorio_estoque_produto.text = itemEstoque.nome
            itemView.item_relatorio_estoque_quantidade.text = itemEstoque.estoque.toString()
        }
    }

    fun atualiza(item: List<Produto>){
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, itemEstoque.size)
        itemEstoque.clear()
        itemEstoque.addAll(item)
        notifyItemRangeInserted(VALOR_CONSTANTE_ZERO, itemEstoque.size)
    }

    fun removeTodos() {
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, itemEstoque.size)
        itemEstoque.clear()

    }

}