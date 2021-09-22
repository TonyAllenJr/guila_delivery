package br.com.antonio.distribuidoradoguila.ui.fragment

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.ItemCarrinho
import br.com.antonio.distribuidoradoguila.model.NumeroPedido
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.model.TipoPagamento
import br.com.antonio.distribuidoradoguila.ui.adapter.ListaCarrinhoAdapter
import br.com.antonio.distribuidoradoguila.ui.extension.moedaBrasileira
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.CarrinhoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ConfiguracaoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.PedidoViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CAMPO_VAZIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ERRO_PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ESCOLHA_TIPO_PAGAMENTO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.FINALIZA_PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.OK
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PROCESSANDO_PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_TOTAL_NAO_ENCONTRADO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.finaliza_pedido.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.util.*

class FinalizaPedidoFragment : BaseFragment() {

    private val adapter: ListaCarrinhoAdapter by inject()
    private val viewModel: UsuarioViewModel by viewModel()
    private val controlador by lazy { findNavController() }
    private val pedidoViewModel: PedidoViewModel by viewModel()
    private val carrinhoViewModel: CarrinhoViewModel by viewModel()
    private val configuracaoViewModel: ConfiguracaoViewModel by viewModel()
    private var tipoPagamento: String = CAMPO_VAZIO
    private var valorTotal: BigDecimal = BigDecimal.ZERO
    private var subTotal: BigDecimal = BigDecimal.ZERO
    private var incrementaUm = 1

    val argument by navArgs<FinalizaPedidoFragmentArgs>()
    private val pedidoId by lazy { argument.pedidoId }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.finaliza_pedido, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configuraLista()
        configuraBotaoEditarEndereco()
        configuraBotaoFinalizaPedido()
        editaEnderecoDeEntrega()
        buscaEnderecoDeEntrega()
        buscaTipoDePagamento()
        buscaTodos()

    }

    private fun configuraLista() {
        finaliza_pedido_recyclerview.adapter = adapter
    }

    private fun configuraBotaoFinalizaPedido() {
        finaliza_pedido_botao_finaliza_pedido.setOnClickListener { view ->
            view?.let {
                configuracaoViewModel.busca().observe(viewLifecycleOwner, {
                    it?.let { lista ->
                        lista.forEach { item ->
                            if (item.appAtivo) {
                                if (temConexao()) {
                                    if (subTotal >= item.pedidoMinimo) {
                                        if (buscaTipoDePagamento() != "") {
                                            finaliza_pedido_botao_finaliza_pedido.text = PROCESSANDO_PEDIDO
                                            salva()
                                            ocultarProduto()
                                        } else {
                                            view.snackBar(ESCOLHA_TIPO_PAGAMENTO)
                                        }
                                    } else {
                                        view.snackBar("Pedido mínimo: ${item.pedidoMinimo}")
                                    }

                                }

                            } else {
                                AlertDialog
                                    .Builder(context)
                                    .setTitle("Aplicativo fechado\n" +
                                            "Tente mais tarde")
                                    .setPositiveButton("Ok", null)
                                    .show()
                            }
                        }

                    }
                })

            }
        }
    }

    private fun ocultarProduto() {
        finaliza_pedido_linha.visibility = GONE
        finaliza_pedido_subTotal.visibility = GONE
        finaliza_pedido_preco_subTotal.visibility = GONE
        finaliza_pedido_entrega.visibility = GONE
        finaliza_pedido_preco_entrega.visibility = GONE
        finaliza_pedido_total.visibility = GONE
        finaliza_pedido_preco_total.visibility = GONE
    }

    private fun buscaTodos() {
        carrinhoViewModel.buscaItens(0, pedidoId).observe(viewLifecycleOwner, {
            it?.let {
                configuracaoViewModel.busca().observe(viewLifecycleOwner, { lista ->
                    lista.forEach { valor ->
                        subTotal = calculaValorTotal(it)
                        finaliza_pedido_preco_subTotal.text = subTotal.moedaBrasileira()
                        finaliza_pedido_preco_entrega.text = valor.taxaEntrega.moedaBrasileira()
                        valorTotal = calculaValorTotal(it).add(valor.taxaEntrega)
                        finaliza_pedido_preco_total.text = valorTotal.moedaBrasileira()
                        adapter.atualiza(it)
                    }

                })

            }

        })
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

    fun salva() {
        pedidoViewModel.buscaUltimoPedido().observe(viewLifecycleOwner, {
            it?.let { numeroPedido ->
                val numeroPedidoAtualizado = numeroPedido + incrementaUm
                pedidoViewModel.atualizaDados(
                    Pedido(
                        numero = numeroPedidoAtualizado,
                        data = Calendar.getInstance().time,
                        horaEnvio = Calendar.getInstance().time,
                        horaEntrega = Calendar.getInstance().time,
                        tipoPagamento = buscaTipoDePagamento(),
                        subTotal = subTotal,
                        valorTotal = valorTotal,
                        taxaEntrega = valorTotal - subTotal
                    ), pedidoId
                ).observe(viewLifecycleOwner, { recurso ->


                    if (recurso.dado) {
                        AlertDialog
                            .Builder(context)
                            .setTitle("Pedido efetuado com sucesso\n" +
                                    "Daqui a pouco estaremos aí :)")
                            .setPositiveButton("Ok") { _, _ ->
                                adapter.removeTodos()
                                vaiParaListaPedidos()
                            }
                            .setCancelable(false)
                            .show()


                        pedidoViewModel.salvaNumeroPedido(
                            NumeroPedido(pedido = numeroPedidoAtualizado)
                        )
                        carrinhoViewModel.atualizaNumeroPedido(numeroPedidoAtualizado, pedidoId)

                        viewModel.buscaUsuarios.observe(viewLifecycleOwner, { usuarios ->
                            usuarios.forEach { usuario ->
                                if (usuario.administrador) {
                                    FirebaseFirestore.getInstance().collection("notifications")
                                        .document(usuario.token)
                                        .set(usuario)
                                    Log.i("token", "salva: ${usuario.token}")
                                }
                                if (usuario.administradorSecundario) {
                                    FirebaseFirestore.getInstance().collection("notifications2")
                                        .document(usuario.token)
                                        .set(usuario)
                                    Log.i("token", "salva: ${usuario.token}")
                                }

                            }
                        })

                    } else {
                        finaliza_pedido_botao_finaliza_pedido.text = FINALIZA_PEDIDO
                        AlertDialog
                            .Builder(context)
                            .setTitle(ERRO_PEDIDO)
                            .setPositiveButton(OK, null)
                            .setCancelable(false)
                            .show()

                    }


                })

            }
        })


    }

    private fun vaiParaListaPedidos() {
        val direcao =
            FinalizaPedidoFragmentDirections.acaoFinalizaPedidoParaListaPedidos()
        controlador.navigate(direcao)
    }

    private fun buscaTipoDePagamento(): String {
        finaliza_pedido_tipo_pagamento.setOnCheckedChangeListener { radio, checkedId ->
            when (checkedId) {
                R.id.finaliza_pedido_tipo_pagamento_dinheiro -> {
                    tipoPagamento = TipoPagamento.DINHEIRO.toString
                }
                R.id.finaliza_pedido_tipo_pagamento_pix -> {
                    tipoPagamento = TipoPagamento.PIX.toString
                }
                R.id.finaliza_pedido_tipo_pagamento_cartao_de_debito -> {
                    tipoPagamento = TipoPagamento.CARTAO_DE_DEBITO.toString
                }
                R.id.finaliza_pedido_tipo_pagamento_cartao_de_credito -> {
                    tipoPagamento = TipoPagamento.CARTAO_DE_CREDITO.toString
                }
            }
        }
        return tipoPagamento
    }

    private fun configuraBotaoEditarEndereco() {
        finaliza_pedido_editar_endereco.setOnClickListener {
            vaiParaCadastroUsuario()
        }
    }

    private fun vaiParaCadastroUsuario() {
        FinalizaPedidoFragmentDirections.acaoFinalizaPedidoParaCadastroUsuario()
            .let(controlador::navigate)
    }

    private fun editaEnderecoDeEntrega() {
        finaliza_pedido_editar_endereco.setOnClickListener {
            it?.let {
                FinalizaPedidoFragmentDirections.acaoFinalizaPedidoParaCadastroUsuario()
                    .let(controlador::navigate)
            }
        }
    }

    private fun buscaEnderecoDeEntrega() {
        viewModel.login.observe(viewLifecycleOwner, {
            it?.let { usuario ->

                finaliza_pedido_endereco_entrega.text =
                    getString(R.string.endereco_completo,
                        usuario.endereco.rua,
                        usuario.endereco.numero,
                        usuario.endereco.bairro,
                        usuario.endereco.complemento,
                        usuario.endereco.referencia,
                        usuario.telefone)
            }
        })
    }
}
