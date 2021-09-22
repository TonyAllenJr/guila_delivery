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
import br.com.antonio.distribuidoradoguila.ui.extension.carregaImagem
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.ui.viewmodel.CarrinhoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ProdutoViewModel
import br.com.antonio.distribuidoradoguila.util.Util
import kotlinx.android.synthetic.main.item_carrinho.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal


class DetalheCarrinhoFragment : BaseFragment() {

    private val argument by navArgs<DetalheCarrinhoFragmentArgs>()
    private val carrinhoId by lazy { argument.carrinhoId }
    private val pedidoId by lazy { argument.pedidoId }
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()
    private val viewModel: CarrinhoViewModel by viewModel()
    private val produtoViewModel: ProdutoViewModel by viewModel()
    private val util: Util by inject()
    private val controlador by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.item_carrinho, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(
            appBar = false)
        buscaItemCarrinho()
    }

    private fun buscaItemCarrinho() {
        carrinhoId.let { id ->
            viewModel.buscaPorId(id, pedidoId).observe(viewLifecycleOwner, { it ->
                it?.let { itemCarrinho ->
                    util.quantidade = itemCarrinho.quantidade
                    util.precoTotalPorItem = itemCarrinho.precoTotalPorItem

                    preencheCampos(itemCarrinho)
                    configuraBotaoSubtracao(itemCarrinho)
                    configuraBotaoAdicao(itemCarrinho)
                    configuraBotaoContinuar(itemCarrinho)
                }
            })
        }

    }

    private fun configuraBotaoContinuar(itemCarrinho: ItemCarrinho) {
        item_carrinho_botao_continuar.setOnClickListener {
            viewModel.salva(ItemCarrinho(
                id = itemCarrinho.id,
                nome = itemCarrinho.nome,
                quantidade = util.quantidade,
                imagemUrl = itemCarrinho.imagemUrl,
                preco = itemCarrinho.preco,
                precoTotalPorItem = util.precoTotalPorItem,
                usuarioId = itemCarrinho.usuarioId,
                numeroPedido = itemCarrinho.numeroPedido,
                produtoId = itemCarrinho.produtoId,
                pedidoId = itemCarrinho.pedidoId

            ), Pedido(itemCarrinho.pedidoId))
            val direcao =
                DetalheCarrinhoFragmentDirections.acaoDetalheCarrinhoParaListaCarrinho(pedidoId)
            controlador.navigate(direcao)

            atualizaEstoque(itemCarrinho)
        }
    }

    private fun atualizaEstoque(itemCarrinho: ItemCarrinho) {
        if (itemCarrinho.quantidade > util.quantidade) {
            val quantidade = itemCarrinho.quantidade - util.quantidade
            produtoViewModel.adicionaQuantidadeEstoque(itemCarrinho.produtoId, quantidade)
        }
        if (itemCarrinho.quantidade < util.quantidade) {
            val quantidade = util.quantidade - itemCarrinho.quantidade
            produtoViewModel.removeQuantidadeEstoque(itemCarrinho.produtoId, quantidade)

        }
    }

    private fun configuraBotaoAdicao(itemCarrinho: ItemCarrinho?) {
        view?.let { v ->
            if (itemCarrinho != null) {
                produtoViewModel.buscaTodos().observe(viewLifecycleOwner, {
                    it?.let { produtos ->
                        produtos.forEach { produto ->
                            if (produto.id == itemCarrinho.produtoId) {
                                util.configuraBotaoAdicao(itemCarrinho.preco, produto.estoque, v)
                            }
                        }
                    }
                })


            }
        }
    }

    private fun configuraBotaoSubtracao(itemCarrinho: ItemCarrinho?) {
        view?.let {
            if (itemCarrinho != null) {
                util.configuraBotaoSubtracao(itemCarrinho.preco, it)


            }
        }
    }

    private fun preencheCampos(itemCarrinho: ItemCarrinho) {
        util.precoTotalPorItem = itemCarrinho.preco
        item_carrinho_imagem.carregaImagem(itemCarrinho.imagemUrl)
        item_carrinho_nome.text = itemCarrinho.nome
        item_carrinho_quantidade.text = util.quantidade.toString()
        item_carrinho_preco.text = itemCarrinho.preco.moedaBrasileira()
        item_carrinho_preco_total.text = calculaPrecoTotalPorItem(itemCarrinho).moedaBrasileira()
    }

    private fun calculaPrecoTotalPorItem(itemCarrinho: ItemCarrinho) =
        BigDecimal(util.quantidade).multiply(itemCarrinho.preco)
}