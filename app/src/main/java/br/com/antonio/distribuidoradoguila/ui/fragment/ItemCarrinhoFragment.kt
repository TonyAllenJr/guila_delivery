package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.ItemCarrinho
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.ui.extension.carregaImagem
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.*
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PRODUTO_SEM_ESTOQUE
import br.com.antonio.distribuidoradoguila.util.Util
import kotlinx.android.synthetic.main.item_carrinho.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal

class ItemCarrinhoFragment : BaseFragment() {

    private val argument by navArgs<ItemCarrinhoFragmentArgs>()
    private val produtoId by lazy { argument.produtoId }
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()
    private val viewModel: CarrinhoViewModel by viewModel()
    private val pedidoViewModel: PedidoViewModel by viewModel()
    private val produtoViewModel: ProdutoViewModel by viewModel()
    private val controlador by lazy { findNavController() }
    private val util: Util by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.item_carrinho, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(bottomNavigation = true)

        buscaProduto()

    }

    private fun buscaProduto() {
        viewModel.buscaProdutoPorId(produtoId).observe(viewLifecycleOwner, { produto ->
            produto?.let {
                preencheCampos(produto)
                configuraBotaoAdicao(produto)
                configuraBotaoSubtracao(produto)
                configuraBotaoContinuar(produto)
            }
        })
    }

    private fun configuraBotaoSubtracao(produto: Produto?) {
        view?.let { it ->
            if (produto != null) {
                util.configuraBotaoSubtracao(produto.preco, it)
            }
        }
    }

    private fun configuraBotaoAdicao(produto: Produto?) {
        view?.let { it ->
            if (produto != null) {
                util.configuraBotaoAdicao(produto.preco, produto.estoque, it)
            }
        }
    }

    private fun preencheCampos(produto: Produto) {
        util.precoTotalPorItem = produto.preco
        item_carrinho_imagem.carregaImagem(produto.imagemUrl)
        item_carrinho_nome.text = produto.nome
        item_carrinho_quantidade.text = util.quantidade.toString()
        item_carrinho_preco.text = produto.preco.moedaBrasileira()
        item_carrinho_preco_total.text = calculaPrecoTotalPorItem(produto).moedaBrasileira()
    }

    private fun configuraBotaoContinuar(produto: Produto) {
        item_carrinho_botao_continuar.setOnClickListener {
            if (produto.estoque == 0) {
                view?.snackBar(PRODUTO_SEM_ESTOQUE)
            }else {
                pedidoViewModel.buscaPedidoEmAberto().observe(viewLifecycleOwner, {
                    it?.forEach { pedido ->
                        if (pedido.numero.toInt() == 0)
                            pedido.id?.let { pedidoId ->
                                ItemCarrinho(
                                    quantidade = util.quantidade,
                                    nome = produto.nome,
                                    imagemUrl = produto.imagemUrl,
                                    preco = produto.preco,
                                    precoTotalPorItem = util.precoTotalPorItem,
                                    produtoId = produtoId,
                                    pedidoId = pedidoId
                                )
                            }?.let { itemCarrinho ->
                                viewModel.salva(
                                    itemCarrinho, Pedido(pedido.id)
                                )
                            }

                        produtoViewModel.removeQuantidadeEstoque(produtoId, util.quantidade)

                        pedido.id?.let { it1 ->
                            ItemCarrinhoFragmentDirections.acaoItemCarrinhoParaListaCarrinho(
                                it1)
                                .let(controlador::navigate)
                        }
                    }

                })
            }



    }
}

private fun calculaPrecoTotalPorItem(produto: Produto) =
    BigDecimal(util.quantidade).multiply(produto.preco)


}