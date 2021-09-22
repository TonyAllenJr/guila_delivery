package br.com.antonio.distribuidoradoguila.ui.fragment

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.model.Usuario
import br.com.antonio.distribuidoradoguila.ui.adapter.ListaCarrinhoAdapter
import br.com.antonio.distribuidoradoguila.ui.extension.converteParaData
import br.com.antonio.distribuidoradoguila.ui.extension.converteParaHora
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.CarrinhoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.PedidoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ProdutoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NAO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SIM
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.detalhes_pedido.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DetalhesPedidoFragment : Fragment() {

    private val argument by navArgs<DetalhesPedidoFragmentArgs>()
    private val numeroPedido by lazy { argument.numeroPedido }
    private val id by lazy { argument.id }
    private val carrinhoViewModel: CarrinhoViewModel by viewModel()
    private val pedidoViewModel: PedidoViewModel by viewModel()
    private val usuarioViewModel: UsuarioViewModel by viewModel()
    private val produtoViewModel: ProdutoViewModel by viewModel()
    private val adapter: ListaCarrinhoAdapter by inject()
    private val controlador by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            controlador.popBackStack(R.id.listaPedidos, true)
            controlador.navigate(R.id.listaPedidos)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.detalhes_pedido, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configuraLista()


        detalhes_pedido_numero_pedido.text = numeroPedido.toString()

        usuarioViewModel.login.observe(viewLifecycleOwner, { u ->
            u?.let { usuario ->
                if (usuario.administrador || usuario.administradorSecundario
                    || usuario.desenvolvedor
                ) {
                    mostraDadosUsuario()
                    pedidoViewModel.buscaUsuarioIdContador(numeroPedido)
                        .observe(viewLifecycleOwner, { s ->
                            s?.let { usuarioId ->
                                usuarioViewModel.buscaPorId(usuarioId)
                                    .observe(viewLifecycleOwner, { us ->
                                        us?.let { user ->
                                            preencheDadosUsuario(user)
                                        }
                                    })
                                carrinhoViewModel.buscaItensAdmin(numeroPedido, usuarioId, id)
                                    .observe(viewLifecycleOwner, { itemCarrinho ->
                                        itemCarrinho?.let {
                                            adapter.atualiza(it)
                                        }
                                    })

                                excluirPedido(usuarioId)

                                pedidoViewModel.buscaPedidoPorIdAdmin(id, usuarioId)
                                    .observe(viewLifecycleOwner, { p ->
                                        p?.let { pedido ->
                                            verificarStatusPedido(pedido)
                                            preencheDadosPedido(pedido)
                                            detalhes_pedido_cd_enviado.setOnClickListener {
                                                if (!pedido.enviado) {
                                                    dialogConfirmacaoEnvio(usuarioId)
                                                }


                                            }

                                            val calendar = Calendar.getInstance()
                                            val hora = calendar.get(Calendar.HOUR_OF_DAY)
                                            val minuto = calendar.get(Calendar.MINUTE)


                                            if (pedido.enviado && !pedido.entregue) {
                                                detalhes_pedido_cd_entrega.setOnClickListener {
                                                    dialogConfirmacaoEntrega(calendar,
                                                        usuarioId,
                                                        hora,
                                                        minuto)

                                                }
                                            }


                                        }
                                    })
                            }
                        })

                } else {

                    pedidoViewModel.buscaPedidoPorIdUsuario(id).observe(viewLifecycleOwner, { p ->
                        p?.let { pedido ->
                            verificarStatusPedido(pedido)
                            preencheDadosPedido(pedido)

                            carrinhoViewModel.buscaItens(numeroPedido, id)
                                .observe(viewLifecycleOwner, { itemCarrinho ->
                                    itemCarrinho?.let {
                                        adapter.atualiza(it)
                                    }
                                })
                        }
                    })


                }
            }
        })

    }

    private fun excluirPedido(usuarioId: String) {
        detalhes_pedido_botao_excluir.setOnClickListener {
            it?.let {
                AlertDialog
                    .Builder(context)
                    .setTitle("Você tem certeza que deseja excluir o pedido $numeroPedido?")
                    .setPositiveButton(SIM) { _, _ ->
                        pedidoViewModel.remove(usuarioId, id)
                        carrinhoViewModel.buscaItensAdmin(numeroPedido,
                            usuarioId,
                            id)
                            .observe(viewLifecycleOwner, { listaCarrinho ->
                                listaCarrinho.forEach { itemCarrinho ->
                                    produtoViewModel.adicionaQuantidadeEstoque(
                                        itemCarrinho.produtoId,
                                        itemCarrinho.quantidade
                                    )

                                }
                                DetalhesPedidoFragmentDirections.acaoDetalhePedidoParaListaPedidos()
                                    .let(controlador::navigate)
                            })
                        view?.snackBar("Pedido excluído com sucesso")
                    }
                    .setNegativeButton(NAO, null)
                    .show()
            }

        }
    }

    private fun dialogConfirmacaoEntrega(
        calendar: Calendar,
        usuarioId: String,
        hora: Int,
        minuto: Int,
    ) {
        TimePickerDialog(
            context,
            { _, hora, minuto ->

                val ano = calendar.get(Calendar.YEAR)
                val mes = calendar.get(Calendar.MONTH)
                val dia =
                    calendar.get(Calendar.DAY_OF_MONTH)

                calendar.set(ano, mes, dia, hora, minuto,
                    Calendar.SECOND)

                val horaEntrega = calendar.time

                AlertDialog
                    .Builder(context)
                    .setTitle("Registrar entrega às $hora:$minuto?")
                    .setPositiveButton(SIM) { _, _ ->
                        pedidoViewModel.atualizaEntrega(
                            usuarioId,
                            id,
                            horaEntrega
                        )
                        carrinhoViewModel.buscaItensAdmin(numeroPedido, usuarioId, id)
                            .observe(viewLifecycleOwner, { carrinhos ->
                                carrinhos.forEach { carrinho ->
                                    produtoViewModel.atualizaQuantidadeEValorVendido(carrinho.produtoId,
                                        carrinho.quantidade, carrinho.precoTotalPorItem)
                                }
                            })

                        pedidoViewModel.buscaPedidoPorIdAdmin(id, usuarioId).observe(
                            viewLifecycleOwner,
                            { pedido ->
                                pedidoViewModel.buscaTipoPagamento(pedido.tipoPagamento)
                                    .observe(viewLifecycleOwner, { valor ->
                                        pedidoViewModel.atualizaTipoPagamento(pedido.tipoPagamento,
                                            (pedido.subTotal + valor))
                                    })

                            })


                    }
                    .setNegativeButton(NAO, null)
                    .show()

            },
            hora, minuto, true
        ).show()


    }

    private fun mostraDadosUsuario() {
        detalhes_pedido_titulo_usuario.visibility = VISIBLE
        detalhes_pedido_nome_usuario.visibility = VISIBLE
        detalhes_pedido_dados_usuario.visibility = VISIBLE
    }

    private fun preencheDadosUsuario(user: Usuario) {
        detalhes_pedido_nome_usuario.text = user.nome
        detalhes_pedido_dados_usuario.text =
            getString(R.string.endereco_completo,
                user.endereco.rua,
                user.endereco.numero,
                user.endereco.bairro,
                user.endereco.complemento,
                user.endereco.referencia,
                user.telefone)
    }

    private fun dialogConfirmacaoEnvio(usuarioId: String) {
        AlertDialog
            .Builder(context)
            .setTitle("Você deseja marcar o pedido como enviado " +
                    "às ${Calendar.getInstance().time.converteParaHora()}?")
            .setPositiveButton("Sim") { _: DialogInterface, _: Int ->
                pedidoViewModel.atualizaEnvio(
                    usuarioId,
                    id,
                    Calendar.getInstance().time)

                usuarioViewModel.buscaPorId(usuarioId)
                    .observe(viewLifecycleOwner, { us ->
                        us?.let { user ->
                            FirebaseFirestore.getInstance().collection("notificationsSend")
                                .document(user.token)
                                .set(user)
                        }
                    })

            }
            .setNegativeButton(NAO, null)
            .show()
    }

    private fun verificarStatusPedido(pedido: Pedido) {
        if (pedido.enviado) {
            detalhes_pedido_enviado_hora.visibility = VISIBLE
        }
        if (pedido.entregue) {
            detalhes_pedido_entrega_hora.visibility = VISIBLE
        }
    }

    private fun preencheDadosPedido(pedido: Pedido) {
        detalhes_pedido_preco_entrega.text = pedido.taxaEntrega.moedaBrasileira()
        detalhes_pedido_preco_subTotal.text = pedido.subTotal.moedaBrasileira()
        detalhes_pedido_preco_total.text = pedido.valorTotal.moedaBrasileira()
        detalhes_pedido_tipo_pagamento.text = pedido.tipoPagamento
        detalhes_pedido_data.text = pedido.data.converteParaData()
        detalhes_pedido_recebido_hora.text = pedido.data.converteParaHora()
        detalhes_pedido_enviado_hora.text = pedido.horaEnvio.converteParaHora()
        detalhes_pedido_entrega_hora.text = pedido.horaEntrega.converteParaHora()

    }

    private fun configuraLista() {
        detalhes_pedido_recyclerview.adapter = adapter
    }
}