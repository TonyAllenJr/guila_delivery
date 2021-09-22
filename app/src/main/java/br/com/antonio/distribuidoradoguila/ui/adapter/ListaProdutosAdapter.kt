package br.com.antonio.distribuidoradoguila.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.ui.extension.carregaImagem
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SEM_ESTOQUE
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.item_lista_produtos.view.*
import java.util.*
import kotlin.collections.ArrayList

class ListaProdutosAdapter(
    private val context: Context,
    private val produtos: MutableList<Produto> = mutableListOf(),
    var onItemClickListener: (produto: Produto) -> Unit = {},
) : RecyclerView.Adapter<ListaProdutosAdapter.ProdutoViewHolder>(), Filterable {

    var produtosFilter: ArrayList<Produto>

    init {
        produtosFilter = produtos as ArrayList<Produto>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {

        val viewCriada =
            LayoutInflater.from(context).inflate(R.layout.item_lista_produtos, parent, false)
        return ProdutoViewHolder(viewCriada)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, posicao: Int) {
        val produto = produtosFilter[posicao]
        holder.vincula(produto)

    }

    override fun getItemCount() = produtosFilter.size

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val filteredList = ArrayList<Produto>()

                if (constraint.toString().isEmpty()) {
                    produtosFilter = produtos as ArrayList<Produto>
                } else {
                    for (produto in produtos) {
                        if (produto.nome.toLowerCase(Locale.ROOT)
                                .contains(constraint.toString()
                                    .toLowerCase(Locale.ROOT))
                        ) {
                            filteredList.add(produto)
                        }
                    }
                    produtosFilter = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = produtosFilter
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                produtosFilter = results?.values as ArrayList<Produto>
                notifyDataSetChanged()
            }
        }
    }

    fun atualiza(produtosNovos: List<Produto>) {
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, produtos.size)
        produtos.clear()
        produtos.addAll(produtosNovos)
        notifyItemRangeInserted(VALOR_CONSTANTE_ZERO, produtos.size)
    }

    fun seleciona(produtosNovos: List<Produto>, categoria: String) {
        val filter = produtosNovos.filter { produto -> produto.categoria == categoria }
        atualiza(filter)
    }

    inner class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var produto: Produto

        init {
            itemView.setOnClickListener {
                if (::produto.isInitialized) {
                    onItemClickListener(produto)
                }
            }
        }

        fun vincula(produto: Produto) {
            this.produto = produto
            itemView.item_lista_produtos_imagem.carregaImagem(produto.imagemUrl)
            if (produto.estoque == VALOR_CONSTANTE_ZERO) {
                itemView.item_produto_nome.text = SEM_ESTOQUE
                itemView.item_produto_preco.text = ""
            }else {
                itemView.item_produto_nome.text = produto.nome
                itemView.item_produto_preco.text = produto.preco.moedaBrasileira()
            }


        }
    }
}

