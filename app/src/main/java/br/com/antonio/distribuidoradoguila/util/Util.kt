package br.com.antonio.distribuidoradoguila.util

import android.view.View
import androidx.fragment.app.Fragment
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import kotlinx.android.synthetic.main.item_carrinho.view.*
import java.math.BigDecimal

class Util : Fragment() {

    var quantidade: Int = 1
    var precoTotalPorItem: BigDecimal = BigDecimal.ZERO
    var numeroPedido: Long = 1


    fun configuraBotaoSubtracao(preco: BigDecimal, view: View) {
        view.item_carrinho_botao_subtracao.setOnClickListener {
            if (quantidade > 1) {
                quantidade--
            }
            preencheCampos(preco, view)
        }
    }

    fun configuraBotaoAdicao(preco: BigDecimal, estoque: Int, view: View,) {
        view.item_carrinho_botao_adicao.setOnClickListener {
            if(estoque > quantidade) {
                quantidade++
            }else if (estoque == quantidade) {
                view.snackBar("O estoque total deste produto é $estoque")
            }
            preencheCampos(preco, view)
        }
    }

    private fun preencheCampos(preco: BigDecimal, view: View) {
        precoTotalPorItem = BigDecimal(quantidade).multiply(preco)
        view.item_carrinho_preco_total.text = precoTotalPorItem.moedaBrasileira()
        view.item_carrinho_quantidade.text = quantidade.toString()
    }


}