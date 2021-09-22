package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.adapter.ListaPedidosAdapter
import br.com.antonio.distribuidoradoguila.ui.viewmodel.PedidoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import kotlinx.android.synthetic.main.lista_pedidos.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListaPedidosFragment : BaseFragment() {

    private val adapter: ListaPedidosAdapter by inject()
    private val viewModel: PedidoViewModel by viewModel()
    private val usuarioViewModel: UsuarioViewModel by viewModel()
    private val controlador by lazy { findNavController() }

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
        return inflater.inflate(R.layout.lista_pedidos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usuarioViewModel.login.observe(viewLifecycleOwner, {
            it?.let { usuario ->
                if (usuario.administrador || usuario.administradorSecundario
                    || usuario.desenvolvedor) {
                    mostraBotaoTopo()
                    buscaTodos()
                } else {
                    buscaPedidos()
                }
            }
        })

        configuraLista()

        adapter.onItemClickListener = { pedido ->
            pedido.numero.let { numeroPedido ->
                pedido.id?.let { id ->
                    vaiParaDetalhesDoPedido(numeroPedido, id)
                }
            }
        }
    }

    private fun mostraBotaoTopo() {
        lista_pedidos_botao_grupo.visibility = VISIBLE
    }

    private fun vaiParaDetalhesDoPedido(numeroPedido: Long, id: String) {
        ListaPedidosFragmentDirections.acaoListaPedidosParaDetalhesPedido(numeroPedido, id)
            .let(controlador::navigate)
    }

    private fun buscaPedidos() {
        viewModel.buscaPedidosFinalizados().observe(viewLifecycleOwner, {
            it?.let { pedidos ->
                adapter.atualiza(pedidos)
            }
        })
    }

    private fun buscaTodos() {
        viewModel.buscaTodos().observe(viewLifecycleOwner, {
            it?.let { pedidos ->
                lista_pedidos_botao_grupo.addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked) {
                        when (checkedId) {
                            R.id.lista_pedidos_em_aberto -> adapter.seleciona(pedidos, false)
                            R.id.lista_pedidos_finalizado -> adapter.seleciona(pedidos, true)
                        }
                    }
                    else {
                        if (lista_pedidos_botao_grupo.checkedButtonId == View.NO_ID) {
                            adapter.removeTodos()
                        }

                    }
                }
            }
        })
    }


    private fun configuraLista() {
        lista_pedidos_recyclerview.adapter = adapter
    }


}

