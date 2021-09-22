package br.com.antonio.distribuidoradoguila.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SearchView
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.NavGraphDirections
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Categoria
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.ui.adapter.ListaProdutosAdapter
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.*
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ALTERAR
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NAO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PRODUTO_REMOVIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.REMOVER
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SIM
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.lista_produtos.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListaProdutosFragment : BaseFragment() {

    private val viewModel: ProdutoViewModel by viewModel()
    private val usuarioViewModel: UsuarioViewModel by viewModel()
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()
    private val pedidoViewModel: PedidoViewModel by viewModel()
    private val adapter: ListaProdutosAdapter by inject()
    private val configuracaoViewModel: ConfiguracaoViewModel by viewModel()
    private val controlador by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buscaProdutos()
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.lista_produtos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes =
            ComponentesVisuais(appBar = true, bottomNavigation = true)


        buscaPedidoEmAberto()
        configuraNavegacaoAdministrador()
        configuraLista()
        configuraBotaoAdicionar()
        configuraAtivarApp()

    }

    private fun buscaPedidoEmAberto() {
        pedidoViewModel.buscaPedidoEmAberto().observe(viewLifecycleOwner, {
            val count = it.stream()
                .filter { pedido -> pedido.numero.toInt() == VALOR_CONSTANTE_ZERO }
                .count()
            if (count.toInt() == VALOR_CONSTANTE_ZERO) {
                pedidoViewModel.salva(Pedido())
            }
        })
    }

    private fun configuraAtivarApp() {
        configuracaoViewModel.busca().observe(viewLifecycleOwner, {
            it?.let { lista ->
                lista.forEach { item ->
                    if (item.appAtivo) {
                        lista_produtos_aberto.visibility = VISIBLE
                        lista_produtos_fechado.visibility = GONE
                    } else {
                        lista_produtos_fechado.visibility = VISIBLE
                        lista_produtos_aberto.visibility = GONE
                    }
                }
            }
        })
    }

    private fun configuraNavegacaoAdministrador() {
        usuarioViewModel.login.observe(viewLifecycleOwner, {
            it?.let { usuario ->
                if (usuario.administrador || usuario.administradorSecundario
                    || usuario.desenvolvedor
                ) {
                    estadoAppViewModel.temComponentes =
                        ComponentesVisuais(appBar = true)
                } else {
                    estadoAppViewModel.temComponentes =
                        ComponentesVisuais(appBar = true, bottomNavigation = true)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_pesquisar, menu)
        inflater.inflate(R.menu.menu_sair, menu)
        usuarioViewModel.login.observe(viewLifecycleOwner, {
            it?.let { usuario ->
                if (usuario.administrador || usuario.desenvolvedor) {
                    inflater.inflate(R.menu.menu_administrador, menu)
                } else if (usuario.administradorSecundario) {
                    inflater.inflate(R.menu.menu_administrador_secundario, menu)
                }
            }
        })

        val findItem = menu.findItem(R.id.menu_top_botao_pesquisar)
        val actionView = findItem.actionView as SearchView
        actionView.queryHint = "Digite o nome do produto"

        actionView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }

        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_botao_sair) {
            AlertDialog
                .Builder(context)
                .setTitle("Você deseja sair do aplicativo?")
                .setPositiveButton(SIM) { _, _ ->
                    usuarioViewModel.desloga()
                    vaiParaLogin()
                }
                .setNegativeButton(NAO, null)
                .show()
        }
        if (item.itemId == R.id.menu_administrador_configuracoes) {
            vaiParaConfiguracoes()
        }
        if (item.itemId == R.id.menu_administrador_pedidos) {
            vaiParaPedidos()
        }
        if (item.itemId == R.id.menu_administrador_mensagens) {
            vaiParaListaMensagens()
        }
        if (item.itemId == R.id.menu_administrador_estoqueZero) {
            viewModel.buscaEstoqueZero().observe(viewLifecycleOwner, {
                it?.let { produtos -> adapter.atualiza(produtos) }
            })
        }
        if (item.itemId == R.id.menu_administrador_relatorio_vendas) {
            ListaProdutosFragmentDirections.acaoListaProdutosParaRelatorioVendas()
                .let(controlador::navigate)
        }
        if (item.itemId == R.id.menu_administrador_relatorio_estoque) {
            ListaProdutosFragmentDirections.acaoListaProdutosParaRelatorioEstoque()
                .let(controlador::navigate)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun vaiParaListaMensagens() {
        ListaProdutosFragmentDirections.acaoListaProdutosParaListaMensagens()
            .let(controlador::navigate)
    }

    private fun vaiParaPedidos() {
        ListaProdutosFragmentDirections.acaoListaProdutosParaListaPedidos()
            .let(controlador::navigate)
    }

    private fun vaiParaConfiguracoes() {
        ListaProdutosFragmentDirections.acaoListaProdutosParaConfiguracao()
            .let(controlador::navigate)
    }


    private fun vaiParaLogin() {
        val direcao =
            NavGraphDirections.acaoGlobalLogin()
        controlador.navigate(direcao)
    }

    private fun configuraLista() {
        lista_produtos_recyclerview.adapter = adapter
        adapter.onItemClickListener = { produto ->
            produto.id?.let { id ->
                usuarioViewModel.login.observe(viewLifecycleOwner, { login ->
                    login?.let { usuario ->
                        if (usuario.administrador || usuario.desenvolvedor) {
                            AlertDialog
                                .Builder(context)
                                .setTitle("O que você deseja fazer?")
                                .setPositiveButton(ALTERAR) { _, _ ->
                                    vaiParaFormularioProduto(id)
                                }
                                .setNegativeButton(REMOVER) { _, _ ->
                                    AlertDialog
                                        .Builder(context)
                                        .setTitle("Confirma a exclusão do produto ${produto.nome}?")
                                        .setPositiveButton(SIM) { _, _ ->
                                            remove(id)
                                        }
                                        .setNegativeButton(NAO, null)
                                        .show()
                                }
                                .show()
                        } else if (usuario.administradorSecundario)
                        else {
                            vaiParaCarrinho(id)
                        }

                    }
                })
            }
        }

    }

    private fun remove(id: String) {
        viewModel.remove(id).observe(viewLifecycleOwner, {
            it?.let {
                view?.snackBar(PRODUTO_REMOVIDO)
            }
        })
    }

    private fun vaiParaCarrinho(produtoId: String) {
        val direcao = ListaProdutosFragmentDirections
            .acaoListaProdutosParaFormularioCarrinho(produtoId)
        controlador.navigate(direcao)
    }

    private fun configuraBotaoAdicionar() {
        usuarioViewModel.login.observe(viewLifecycleOwner, {
            it?.let { usuario ->
                if (usuario.administrador || usuario.desenvolvedor) {
                    botao_fab_adicionar_produto.visibility = VISIBLE
                    botao_fab_carrinho.visibility = GONE

                    botao_fab_adicionar_produto.setOnClickListener {
                        ListaProdutosFragmentDirections
                            .acaoListaProdutosParaFormularioProduto()
                            .let(controlador::navigate)
                    }

                } else if (usuario.administradorSecundario) {
                    botao_fab_carrinho.visibility = GONE
                    botao_fab_adicionar_produto.visibility = GONE
                } else {
                    botao_fab_carrinho.visibility = VISIBLE
                    botao_fab_adicionar_produto.visibility = GONE

                    botao_fab_carrinho.setOnClickListener {
                        vaiParaListaCarrinho()
                    }
                }
            }
        })
    }

    private fun vaiParaListaCarrinho() {
        pedidoViewModel.buscaPedidoEmAberto().observe(viewLifecycleOwner, {
            it.forEach { pedido ->
                if (pedido.numero.toInt() == VALOR_CONSTANTE_ZERO) {
                    pedido.id?.let { pedidoId ->
                        ListaProdutosFragmentDirections.acaoListaProdutosParaListaCarrinho(pedidoId)
                            .let(controlador::navigate)
                    }
                }

            }

        })

    }

    private fun vaiParaFormularioProduto(id: String) {
        ListaProdutosFragmentDirections
            .acaoListaProdutosParaFormularioProduto(id)
            .let(controlador::navigate)
    }

    private fun buscaProdutos() {
        viewModel.buscaTodos().observe(this, {
            it?.let { produtos ->
                lista_produtos_botao_grupo.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    if (isChecked) {
                        when (checkedId) {
                            R.id.lista_produtos_botao_cervejas -> adapter.seleciona(produtos,
                                Categoria.CERVEJA.toString)
                            R.id.lista_produtos_botao_destilado -> adapter.seleciona(produtos,
                                Categoria.DESTILADO.toString)
                            R.id.lista_produtos_botao_sem_alcool -> adapter.seleciona(produtos,
                                Categoria.SEM_ALCOOL.toString)
                            R.id.lista_produtos_botao_petiscos -> adapter.seleciona(produtos,
                                Categoria.PETISCOS.toString)
                            R.id.lista_produtos_botao_diversos -> adapter.seleciona(produtos,
                                Categoria.DIVERSOS.toString)
                        }
                    } else {
                        if (lista_produtos_botao_grupo.checkedButtonId == View.NO_ID) {
                            adapter.atualiza(produtos)
                        }

                    }

                }
                adapter.atualiza(produtos)
            }
        })
    }
}