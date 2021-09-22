package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.adapter.RelatorioVendasAdapter
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.PedidoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ProdutoViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CARTAO_DE_CREDITO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CARTAO_DE_DEBITO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.DINHEIRO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PIX
import kotlinx.android.synthetic.main.relatorio_vendas.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class RelatorioVendasFragment : BaseFragment() {

    val argument by navArgs<ListaCarrinhoFragmentArgs>()
    private val pedidoViewModel: PedidoViewModel by viewModel()
    private val produtoViewModel: ProdutoViewModel by viewModel()
    private val adapter: RelatorioVendasAdapter by inject()
    private val controlador by lazy { findNavController() }
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()

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
        return inflater.inflate(R.layout.relatorio_vendas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(appBar = false,
            bottomNavigation = false)

        buscaTodos()
        configuraLista()
    }

    private fun buscaTodos() {
        relatorio_vendas_botao_grupo.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.relatorio_vendas_botao_nome ->
                        produtoViewModel.buscaPorNome()
                            .observe(viewLifecycleOwner, { produtosPorNome ->
                                adapter.atualiza(produtosPorNome)
                            })
                    R.id.relatorio_vendas_botao_quantidade ->
                        produtoViewModel.buscaPorQuantidadeVendida()
                            .observe(viewLifecycleOwner, { produtosPorQuantidade ->
                                adapter.atualiza(produtosPorQuantidade)
                            })

                    R.id.relatorio_vendas_botao_valor ->
                        produtoViewModel.buscaPorValorVendido()
                            .observe(viewLifecycleOwner, { produtosPorValor ->
                                adapter.atualiza(produtosPorValor)
                            })

                    R.id.relatorio_vendas_botao_tipo_pagamento ->
                        pedidoViewModel.buscaTipoPagamento(DINHEIRO)
                            .observe(viewLifecycleOwner, { valorDinheiro ->
                                adapter.removeTodos()
                                visibilidadeCamposValorTipo(VISIBLE)
                                vendas_por_tipo_dinheiro.text = DINHEIRO
                                vendas_por_tipo_dinheiro_valor.text =
                                    valorDinheiro.moedaBrasileira()
                                pedidoViewModel.buscaTipoPagamento(CARTAO_DE_CREDITO)
                                    .observe(viewLifecycleOwner, { valorCartaoCredito ->
                                        vendas_por_tipo_cartao_credito.text = CARTAO_DE_CREDITO
                                        vendas_por_tipo_cartao_credito_valor.text =
                                            valorCartaoCredito.moedaBrasileira()
                                    })
                                pedidoViewModel.buscaTipoPagamento(CARTAO_DE_DEBITO)
                                    .observe(viewLifecycleOwner, { valorCartaoDebito ->
                                        vendas_por_tipo_cartao_debito.text = CARTAO_DE_DEBITO
                                        vendas_por_tipo_cartao_debito_valor.text =
                                            valorCartaoDebito.moedaBrasileira()
                                    })
                                pedidoViewModel.buscaTipoPagamento(PIX)
                                    .observe(viewLifecycleOwner, { valorPix ->
                                        vendas_por_tipo_pix.text = PIX
                                        vendas_por_tipo_pix_valor.text = valorPix.moedaBrasileira()
                                    })
                            })


                }
            } else {
                if (relatorio_vendas_botao_grupo.checkedButtonId == View.NO_ID) {
                    adapter.removeTodos()
                    visibilidadeCamposValorTipo(GONE)
                }

            }
        }

    }

    private fun visibilidadeCamposValorTipo(visibilidade: Int) {
        vendas_por_tipo_dinheiro.visibility = visibilidade
        vendas_por_tipo_dinheiro_valor.visibility = visibilidade
        vendas_por_tipo_cartao_credito.visibility = visibilidade
        vendas_por_tipo_cartao_credito_valor.visibility = visibilidade
        vendas_por_tipo_cartao_debito.visibility = visibilidade
        vendas_por_tipo_cartao_debito_valor.visibility = visibilidade
        vendas_por_tipo_pix.visibility = visibilidade
        vendas_por_tipo_pix_valor.visibility = visibilidade
    }


    private fun configuraLista() {
        relatorio_vendas_recyclerview.adapter = adapter
    }

}

