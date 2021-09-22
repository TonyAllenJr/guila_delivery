package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.ui.extension.escondeTeclado
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.FormularioProdutoViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ALTERAR_PRODUTO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PRODUTO_SALVO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PRODUTO_SALVO_FALHA
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.TODOS_CAMPOS_OBRIGATORIOS
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import kotlinx.android.synthetic.main.formulario_produto.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal

class FormularioProdutoFragment : BaseFragment() {

    private val argumentos by navArgs<FormularioProdutoFragmentArgs>()
    private val produtoId by lazy {
        argumentos.produtoId
    }

    private val viewModel : FormularioProdutoViewModel by viewModel()
    private val controlador by lazy {findNavController()}
    private val estadoAppViewModel : EstadoAppViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.formulario_produto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(appBar = true)
        tentaBuscarProduto()
        configuraBotaoSalvar()
        configuraCategoria()
    }

    private fun tentaBuscarProduto() {
        produtoId?.let { id ->
            viewModel.buscaPorId(id).observe(viewLifecycleOwner, {
                it?.let { produto ->
                    val nome = produto.nome
                    val preco = produto.preco.toString()
                    val estoque = produto.estoque.toString()
                    val categoriasRetornadas =
                        context?.resources?.getStringArray(R.array.categoria)
                    val posicaoCategoria = categoriasRetornadas?.indexOf(produto.categoria)

                    val imagemUrl = produto.imagemUrl
                    formulario_produto_imagem_url.editText?.setText(imagemUrl)
                    formulario_produto_nome.editText?.setText(nome)
                    formulario_produto_preco.editText?.setText(preco)
                    formulario_produto_estoque.editText?.setText(estoque)
                    if (posicaoCategoria != null) {
                        formulario_produto_categoria.setSelection(posicaoCategoria, true)
                    }
                    requireActivity().title = ALTERAR_PRODUTO
                }
            })
        }
    }

    private fun configuraBotaoSalvar() {
        formulario_produto_botao_salvar.setOnClickListener {
            val preco = formulario_produto_edit_text_preco.text.toString()
            val nome = formulario_produto_edit_text_nome.text.toString()
            val categoria = formulario_produto_categoria.selectedItem.toString()
            val imagemUrl = formulario_produto_edit_text_url.text.toString()
            val estoque = formulario_produto_edit_text_estoque.text.toString()

            val produto = Produto(
                id = produtoId,
                nome = nome,
                preco = stringToBigDecimal(preco),
                categoria = categoria,
                imagemUrl = imagemUrl,
                estoque = stringToInt(estoque))

            val valido = validaCampos(nome, preco, categoria, imagemUrl, estoque)

            if (valido) {
                viewModel.salva(produto).observe(viewLifecycleOwner, {it ->
                    it?.let {salvo ->
                        if(salvo) {
                            controlador.popBackStack()
                            view?.snackBar(PRODUTO_SALVO)
                            return@observe
                        }
                    }
                    view?.snackBar(PRODUTO_SALVO_FALHA)
                })
            } else {
                view?.snackBar(TODOS_CAMPOS_OBRIGATORIOS)
            }

            view?.escondeTeclado()

        }
    }

    private fun stringToBigDecimal(precoEmTexto: String) : BigDecimal {
        return try {
            BigDecimal(precoEmTexto)
        } catch (exception: NumberFormatException) {
            BigDecimal.ZERO
        }
    }

    private fun stringToInt(estoque: String) : Int {
        return try {
            estoque.toInt()
        } catch (exception: NumberFormatException) {
            VALOR_CONSTANTE_ZERO
        }
    }


    private fun configuraCategoria() {
        val adapter = ArrayAdapter.createFromResource(requireContext(),
            R.array.categoria, R.layout.support_simple_spinner_dropdown_item)
        formulario_produto_categoria.adapter = adapter

    }

    private fun validaCampos(
        nome: String, imagemUrl: String, categoria: String,
        preco: String, estoque: String
    ): Boolean {
        var valido = true

        if (nome.isBlank()) {
            valido = false
        }

        if (imagemUrl.isBlank()) {
            valido = false
        }

        if (categoria.isBlank()) {
            valido = false
        }

        if (preco.isBlank()) {
            valido = false
        }

        if (estoque.isBlank()) {
            valido = false
        }

        return valido
    }

}