package br.com.antonio.distribuidoradoguila.ui.fragment

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Configuracao
import br.com.antonio.distribuidoradoguila.model.ItemCarrinho
import br.com.antonio.distribuidoradoguila.ui.adapter.ListaCarrinhoAdapter
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.ui.viewmodel.*
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ALTERAR
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CAMPO_VAZIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.OK
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PREENCHER_CADASTRO_FINALIZA_PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.REMOVER
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_TOTAL_NAO_ENCONTRADO
import kotlinx.android.synthetic.main.lista_carrinho.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal


class ListaCarrinhoFragment : BaseFragment() {

    val argument by navArgs<ListaCarrinhoFragmentArgs>()
    private val pedidoId by lazy { argument.pedidoId }
    private val adapter: ListaCarrinhoAdapter by inject()
    private val viewModel: CarrinhoViewModel by viewModel()
    private val usuarioViewModel: UsuarioViewModel by viewModel()
    private val controlador by lazy { findNavController() }
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()
    private val configuracaoViewModel: ConfiguracaoViewModel by viewModel()
    private val produtoViewModel: ProdutoViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            controlador.popBackStack(R.id.listaProdutos, true)
            controlador.navigate(R.id.listaProdutos)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.lista_carrinho, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(appBar = true,
        bottomNavigation = true)

        buscaTodos()
        configuraLista()
        configuraItem()
        configuraBotaoContinuarComprando()
        configuraBotaoIrParaPagamento()

    }

    private fun buscaTodos() {
        pedidoId.let {
            viewModel.buscaItens(0, it).observe(viewLifecycleOwner, { itemCarrinho ->
                itemCarrinho?.let {
                    configuracaoViewModel.busca().observe(viewLifecycleOwner, { lista ->
                        lista.forEach { valor ->
                            if (it.isEmpty()) {
                                ocultaCampos()
                                lista_carrinho_total.text = getString(R.string.carrinho_vazio)
                            }
                            mostraValores(it, valor)
                            adapter.atualiza(it)
                        }
                    })

                }
            })
        }
    }

    private fun mostraValores(
        it: List<ItemCarrinho>,
        valor: Configuracao,
    ) {
        lista_carrinho_preco_subTotal.text = calculaValorTotal(it).moedaBrasileira()
        lista_carrinho_preco_entrega.text = valor.taxaEntrega.moedaBrasileira()
        val valorTotal = calculaValorTotal(it).add(valor.taxaEntrega)
        lista_carrinho_preco_total.text = valorTotal.moedaBrasileira()
    }

    private fun ocultaCampos() {
        lista_carrinho_botao_continuar_comprando.visibility = GONE
        lista_carrinho_botao_ir_para_pagamento.visibility = GONE
        lista_carrinho_preco_total.visibility = GONE
        lista_carrinho_subTotal.visibility = GONE
        lista_carrinho_preco_subTotal.visibility = GONE
        lista_carrinho_entrega.visibility = GONE
        lista_carrinho_preco_entrega.visibility = GONE
        lista_carrinho_linha.visibility = GONE
    }

    fun calculaValorTotal(itemCarrinho: List<ItemCarrinho>): BigDecimal {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            itemCarrinho.stream()
                .map(ItemCarrinho::precoTotalPorItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        } else {
            throw IllegalArgumentException(VALOR_TOTAL_NAO_ENCONTRADO)
        }

    }

    private fun configuraBotaoIrParaPagamento() {
        lista_carrinho_botao_ir_para_pagamento.setOnClickListener { it ->
            it?.let {
                usuarioViewModel.login.observe(viewLifecycleOwner, { usuario ->
                    if ((usuario.nome != CAMPO_VAZIO &&
                            usuario.endereco.rua != CAMPO_VAZIO &&
                            usuario.endereco.bairro != CAMPO_VAZIO &&
                            usuario.telefone != CAMPO_VAZIO) &&
                            (usuario.endereco.numero != CAMPO_VAZIO ||
                                    usuario.endereco.complemento != CAMPO_VAZIO))
                            {
                        vaiParaFinalizaPedido()
                    }else {
                        AlertDialog
                            .Builder(context)
                            .setMessage(PREENCHER_CADASTRO_FINALIZA_PEDIDO)
                            .setPositiveButton(OK, null)
                            .show()
                        vaiParaCadastroUsuario()
                    }
                })
            }
        }
    }

    private fun vaiParaCadastroUsuario() {
        ListaCarrinhoFragmentDirections.acaoListaCarrinhoParaCadastroUsuario()
            .let(controlador::navigate)
    }

    private fun vaiParaFinalizaPedido() {
        pedidoId?.let {
            ListaCarrinhoFragmentDirections
                .acaoListaCarrinhoParaFinalizaPedido(it)
                .let(controlador::navigate)
        }
    }

    private fun configuraBotaoContinuarComprando() {
        lista_carrinho_botao_continuar_comprando.setOnClickListener {
            vaiParaListaProduto()
        }
    }

    private fun vaiParaListaProduto() {
        val direcao = ListaCarrinhoFragmentDirections
            .acaoListaCarrinhoParaListaProduto()
        controlador.navigate(direcao)
    }

    private fun configuraItem() {
        adapter.onItemClickListener = { itemCarrinho ->
            AlertDialog
                .Builder(context)
                .setTitle("O que você deseja fazer?")
                .setPositiveButton(ALTERAR) { _, _ ->
                    vaiParaDetalheDoCarrinho(itemCarrinho)
                }
                .setNegativeButton(REMOVER) { _, _ ->
                    produtoViewModel.adicionaQuantidadeEstoque(itemCarrinho.produtoId,
                    itemCarrinho.quantidade)
                    remove(itemCarrinho)
                }
                .show()
        }
    }

    private fun remove(itemCarrinho: ItemCarrinho) {
        itemCarrinho.id?.let {
            pedidoId.let { pedidoId ->
                viewModel.remove(it, pedidoId) }
        }
    }

    private fun vaiParaDetalheDoCarrinho(itemCarrinho: ItemCarrinho) {
        itemCarrinho.id?.let {
            pedidoId?.let { it1 ->
                ListaCarrinhoFragmentDirections
                    .acaoListaCarrinhoParaDetalheCarrinho(it, it1)
                    .let(controlador::navigate)
            }
        }
    }

    private fun configuraLista() {
        lista_carrinho_recyclerview.adapter = adapter
    }

}

