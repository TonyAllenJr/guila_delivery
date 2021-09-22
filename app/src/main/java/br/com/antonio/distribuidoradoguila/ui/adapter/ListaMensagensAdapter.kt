package br.com.antonio.distribuidoradoguila.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Mensagem
import br.com.antonio.distribuidoradoguila.ui.extension.converteParaData
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.item_lista_mensagens.view.*

class ListaMensagensAdapter(
    private val context: Context,
    private val mensagens : MutableList<Mensagem> = mutableListOf(),
    var onItemClickListener: (mensagem: Mensagem) -> Unit = {}
) : RecyclerView.Adapter<ListaMensagensAdapter.MensagemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensagemViewHolder {
        val viewCriada = LayoutInflater.from(context)
                .inflate(R.layout.item_lista_mensagens, parent, false)
        return MensagemViewHolder(viewCriada)
    }

    override fun onBindViewHolder(holder: MensagemViewHolder, position: Int) {
        val mensagem = mensagens[position]
        holder.vincula(mensagem)
    }

    override fun getItemCount() = mensagens.size

    fun atualiza(mensagem: List<Mensagem>){
        notifyItemRangeRemoved(VALOR_CONSTANTE_ZERO, mensagens.size)
        mensagens.clear()
        mensagens.addAll(mensagem)
        notifyItemRangeInserted(VALOR_CONSTANTE_ZERO, mensagens.size)
    }

    inner class MensagemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var mensagem: Mensagem

        init {
            itemView.setOnClickListener {
                if(::mensagem.isInitialized) {
                    onItemClickListener(mensagem)
                }
            }
        }

        fun vincula(mensagem: Mensagem) {
            this.mensagem = mensagem
            itemView.item_lista_mensagens_descricao.text = mensagem.descricao
            itemView.item_lista_mensagens_nome.text = mensagem.nome
            itemView.item_lista_mensagens_email.text = mensagem.email
            itemView.item_lista_mensagens_telefone.text = mensagem.telefone
            itemView.item_lista_mensagens_data.text = mensagem.data.converteParaData()
        }
    }


}