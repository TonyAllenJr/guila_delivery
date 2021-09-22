package br.com.antonio.distribuidoradoguila.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Login
import br.com.antonio.distribuidoradoguila.model.Usuario
import br.com.antonio.distribuidoradoguila.ui.extension.escondeTeclado
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EMAIL_OBRIGATORIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ENVIO_EMAIL_VERIFICACAO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.FALHA_CADASTRO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.OK
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SENHA_DIFERENTE
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SENHA_OBRIGATORIA
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.cadastro_login.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CadastroLoginFragment : Fragment() {

    private val controlador by lazy { findNavController() }
    private val viewModel : UsuarioViewModel by viewModel()
    private val estadoAppViewModel : EstadoAppViewModel by sharedViewModel()
    private val firebaseAuth: FirebaseAuth by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cadastro_login, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais()

        configuraBotaoCadastro()
    }

    private fun configuraBotaoCadastro() {
        cadastro_login_botao_cadastrar.setOnClickListener {

            limpaTodosCampos()

            val email = cadastro_login_email.editText?.text.toString()
            val senha = cadastro_login_senha.editText?.text.toString()
            val confirmaSenha = cadastro_login_confirma_senha.editText?.text.toString()

            val valido = validaCampos(
                email, senha, confirmaSenha)

            if (valido) {
                cadastra(Login(email, senha))
            }

            it?.escondeTeclado()

        }

    }

    private fun cadastra(login: Login) {
        viewModel.cadastraLoginESenha(login).observe(viewLifecycleOwner, {
            it?.let { recurso ->
                if (recurso.dado) {
                    firebaseAuth.currentUser?.email?.let { email ->
                        Usuario(email = email,
                            administrador = false,
                            administradorSecundario = false,
                            desenvolvedor = false)
                    }?.let { usuario -> viewModel.cadastra(usuario) }
                    AlertDialog
                        .Builder(context)
                        .setMessage(ENVIO_EMAIL_VERIFICACAO)
                        .setPositiveButton(OK, null)
                        .show()

                    CadastroLoginFragmentDirections.acaoCadastroLoginParaLogin()
                        .let(controlador::navigate)
                } else {
                    val mensagemErro = recurso.erro ?: FALHA_CADASTRO
                    view?.snackBar(mensagemErro)
                }
            }
        })
    }

    private fun validaCampos(
        email: String, senha: String,
        confirmaSenha: String
    ): Boolean {
        var valido = true

        if (email.isBlank()) {
            cadastro_login_email.error = EMAIL_OBRIGATORIO
            valido = false
        }
        if (senha.isBlank()) {
            cadastro_login_senha.error = SENHA_OBRIGATORIA
            valido = false
        }
        if (senha != confirmaSenha) {
            cadastro_login_confirma_senha.error = SENHA_DIFERENTE
            valido = false
        }

        return valido
    }

    private fun limpaTodosCampos() {
        cadastro_login_email.error = null
        cadastro_login_senha.error = null
        cadastro_login_confirma_senha.error = null
    }
}