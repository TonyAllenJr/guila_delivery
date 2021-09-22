package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.adapter.RelatorioEstoqueAdapter
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ProdutoViewModel
import kotlinx.android.synthetic.main.relatorio_estoque.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class RelatorioEstoqueFragment : BaseFragment() {

    private val produtoViewModel: ProdutoViewModel by viewModel()
    private val adapter: RelatorioEstoqueAdapter by inject()
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
        return inflater.inflate(R.layout.relatorio_estoque, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(appBar = false,
            bottomNavigation = false)

        buscaTodos()
        configuraLista()
    }

    private fun buscaTodos() {
        relatorio_estoque_botao_grupo.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.relatorio_estoque_botao_nome ->
                        produtoViewModel.buscaEstoqueProdutosPorNome()
                            .observe(viewLifecycleOwner, { produtosPorNome ->
                                adapter.atualiza(produtosPorNome)
                                vaiParaFormularioProduto()
                            })
                    R.id.relatorio_estoque_botao_quantidade ->
                        produtoViewModel.buscaEstoqueProdutosPorQuantidade()
                            .observe(viewLifecycleOwner, { produtosPorQuantidade ->
                                adapter.atualiza(produtosPorQuantidade)
                                vaiParaFormularioProduto()
                            })

                }
            } else {
                if (relatorio_estoque_botao_grupo.checkedButtonId == View.NO_ID) {
                    adapter.removeTodos()
                }

            }
        }

    }

    private fun vaiParaFormularioProduto() {
        adapter.onItemClickListener = { produto ->
            produto.id?.let { id ->
                RelatorioEstoqueFragmentDirections
                    .acaoRelatorioEstoqueParaFormularioProduto(id)
                    .let(controlador::navigate)
            }
        }
    }

    private fun configuraLista() {
        relatorio_estoque_recyclerview.adapter = adapter
    }

}

