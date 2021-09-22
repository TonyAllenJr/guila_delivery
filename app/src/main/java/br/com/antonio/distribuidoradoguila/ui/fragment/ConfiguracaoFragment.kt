package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Configuracao
import br.com.antonio.distribuidoradoguila.ui.extension.escondeTeclado
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ConfiguracaoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ABERTO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CONFIGURACAO_SALVA
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.FECHADO
import kotlinx.android.synthetic.main.configuracoes.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal

class ConfiguracaoFragment : Fragment() {

    private val estadoAppViewModel: EstadoAppViewModel by viewModel()
    private val controlador by lazy { findNavController() }
    private val configuracaoViewModel: ConfiguracaoViewModel by viewModel()

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
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.configuracoes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais()

        configuraAdapterSpinner()
        buscaItens()
        configuraBotaoSalvar(view)

    }

    private fun configuraBotaoSalvar(view: View) {
        configuracoes_botao_salvar.setOnClickListener {
            it?.let {
                val pedidoMinimo = configuracoes_editText_pedido_minimo.text.toString()
                val taxaEntrega = configuracoes_editText_taxa_entrega.text.toString()
                val itemSelecionado = configuracoes_ativarApp.selectedItem.toString()
                var appAtivo = false
                if (itemSelecionado == ABERTO) {
                    appAtivo = true
                }

                val configuracao = Configuracao(
                    taxaEntrega = stringToBigDecimal(taxaEntrega),
                    pedidoMinimo = stringToBigDecimal(pedidoMinimo),
                    appAtivo = appAtivo
                )

                salva(configuracao, view)

                it.escondeTeclado()
            }
        }
    }

    private fun salva(
        configuracao: Configuracao,
        view: View,
    ) {
        configuracaoViewModel.salva(configuracao).observe(viewLifecycleOwner, { recurso ->
            recurso?.let {
                if (recurso.dado) {
                    ConfiguracaoFragmentDirections.acapConfiguracoesParaListaProdutos()
                        .let(controlador::navigate)
                    view.snackBar(CONFIGURACAO_SALVA)
                }
            }
        })
    }

    private fun buscaItens() {
        configuracaoViewModel.busca().observe(viewLifecycleOwner, {
            it?.let { lista ->
                lista.forEach { item ->
                    val taxaEntrega = item.taxaEntrega.toString()
                    val pedidoMinimo = item.pedidoMinimo.toString()
                    var appAtivo = item.appAtivo.toString()
                    appAtivo = if (appAtivo == "true") {
                        ABERTO
                    } else {
                        FECHADO
                    }

                    val itensRelacionados =
                        context?.resources?.getStringArray(R.array.ativarApp)
                    val posicaoItem = itensRelacionados?.indexOf(appAtivo)


                    configuracoes_taxa_entrega.editText?.setText(taxaEntrega)
                    configuracoes_pedido_minimo.editText?.setText(pedidoMinimo)
                    if (posicaoItem != null) {
                        configuracoes_ativarApp.setSelection(posicaoItem, true)
                    }
                }

            }
        })
    }

    private fun configuraAdapterSpinner() {
        val adapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.ativarApp, R.layout.support_simple_spinner_dropdown_item)
        configuracoes_ativarApp.adapter = adapter

    }

    private fun stringToBigDecimal(precoEmTexto: String) : BigDecimal {
        return try {
            BigDecimal(precoEmTexto)
        } catch (exception: NumberFormatException) {
            BigDecimal.ZERO
        }
    }
}