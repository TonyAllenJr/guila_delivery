package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.FALHA_CADASTRO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SENHA_ALTERADA_SUCESSO
import kotlinx.android.synthetic.main.altera_senha.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AlteraSenhaFragment : Fragment() {

    private val controlador by lazy { findNavController() }
    private val viewModel: UsuarioViewModel by viewModel()
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.altera_senha, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais()

        altera_senha_botao.setOnClickListener {
            it?.let {

                val novaSenha = altera_senha_nova.editText?.text.toString()
                val confirmaSenha = altera_senha_confirma.editText?.text.toString()

                val validaNovaSenha = validaNovaSenha(novaSenha, confirmaSenha)

                if (validaNovaSenha) {
                    alteraSenha(novaSenha)
                }
            }
        }

    }

    private fun validaNovaSenha(novaSenha: String, confirmaSenha: String): Boolean {

        var valido = false

        if (novaSenha == confirmaSenha) {
            valido = true
        }

        return valido

    }

    private fun alteraSenha(senha: String) {
        viewModel.altera(senha).observe(
            viewLifecycleOwner, {
                it?.let { recurso ->
                    if (recurso.dado) {
                        AlteraSenhaFragmentDirections.acaoAlteraSenhaParaCadastroUsuario()
                            .let(controlador::navigate)
                        view?.snackBar(SENHA_ALTERADA_SUCESSO)
                    } else {
                        val mensagemErro = recurso.erro ?: FALHA_CADASTRO
                        view?.snackBar(mensagemErro)
                    }
                }
            })
    }


}