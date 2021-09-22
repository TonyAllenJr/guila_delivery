package br.com.antonio.distribuidoradoguila.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.ItemCarrinho
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.item_lista_carrinho.view.*

class ListaCarrinhoAdapter(
    private val context: Context,
    private val itemCarrinhos : MutableList<ItemCarrinho> = mutableListOf(),
    var onItemClickListener: (itemCarrinho: ItemCarrinho) -> Unit = {}
) : RecyclerView.Adapter<ListaCarrinhoAdapter.CarrinhoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_lista_carrinho, parent, false)
        return CarrinhoViewHolder(viewCriada)
    }

    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        val carrinho = itemCarrinhos[position]
        holder.vincula(carrinho)
    }

    override fun getItemCount() = itemCarrinhos.size

    inner class CarrinhoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var itemCarrinho : ItemCarrinho

        init {
            itemView.setOnClickListener {
                if(::itemCarrinho.isInitialized) {
                    onItemClickListener(itemCarrinho)
                }
            }
        }

        fun vincula(itemCarrinho: ItemCarrinho) {
            this.itemCarrinho = itemCarrinho
            itemView.item_lista_carrinho_nome.text = itemCarrinho.nome
            itemView.item_lista_carrinho_quantidade.text = itemCarrinho.quantidade.toString()
            itemView.item_lista_carrinho_preco.text = itemCarrinho.precoTotalPorItem.moedaBrasileira()
        }
    }

    fun atualiza(compraEncontrada: List<ItemCarrinho>){
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, itemCarrinhos.size)
        itemCarrinhos.clear()
        itemCarrinhos.addAll(compraEncontrada)
        notifyItemRangeInserted(VALOR_CONSTANTE_ZERO, itemCarrinhos.size)
    }

    fun removeTodos() {
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, itemCarrinhos.size)
        itemCarrinhos.clear()

    }

}