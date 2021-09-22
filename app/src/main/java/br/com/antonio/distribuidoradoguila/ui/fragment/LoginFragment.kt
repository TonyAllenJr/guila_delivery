package br.com.antonio.distribuidoradoguila.ui.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Login
import br.com.antonio.distribuidoradoguila.ui.extension.escondeTeclado
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ACESSANDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EMAIL_OBRIGATORIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ENTRAR
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SEM_CONEXAO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SENHA_OBRIGATORIA
import kotlinx.android.synthetic.main.login.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment : Fragment() {

    private val controlador by lazy { findNavController() }
    private val viewModel: UsuarioViewModel by inject()
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais()

        if (viewModel.estaLogado()) {
            vaiParaListaProdutos()
        }

        configuraBotaoLogin()
        configuraBotaoCadastro()
        configuraRedefinicaoDeSenha()
    }

    private fun configuraRedefinicaoDeSenha() {
        login_redefine_senha.setOnClickListener {
            it?.let {
                LoginFragmentDirections.acaoLoginParaRedefineSenha()
                    .let(controlador::navigate)
            }
        }
    }

    private fun configuraBotaoCadastro() {
        login_botao_cadastrar_usuario.setOnClickListener {
            val direcao = LoginFragmentDirections.acaoLoginParaCadastroLogin()
            controlador.navigate(direcao)
        }
    }

    private fun configuraBotaoLogin() {
        login_botao_logar.setOnClickListener {

            limpaCampos()

            val email = login_email.editText?.text.toString()
            val senha = login_senha.editText?.text.toString()

            if (validaCampos(email, senha)) {
                autentica(Login(email, senha))
            }

            login_botao_logar.text = ACESSANDO

            it?.escondeTeclado()
        }
    }

    private fun autentica(login: Login) {
        if (temConexao()) {
            viewModel.autentica(login)
                .observe(viewLifecycleOwner, {
                    it?.let { recurso ->
                        if (recurso.dado) {
                            viewModel.geraToken()
                            vaiParaListaProdutos()
                        } else {
                            val mensagemDeErro = recurso.erro
                            if (mensagemDeErro != null) {
                                login_botao_logar.text = ENTRAR
                                view?.snackBar(mensagemDeErro)
                            }
                        }
                    }
                })
        }else {
            view?.snackBar(SEM_CONEXAO)
        }

    }

    private fun validaCampos(email: String, senha: String): Boolean {
        var valido = true

        if (email.isBlank()) {
            login_email.error = EMAIL_OBRIGATORIO
            valido = false
        }
        if (senha.isBlank()) {
            login_senha.error = SENHA_OBRIGATORIA
            valido = false
        }
        return valido
    }

    private fun limpaCampos() {
        login_email.error = null
        login_senha.error = null
    }

    private fun vaiParaListaProdutos() {
        val direcao = LoginFragmentDirections.acaoLoginParaListaProdutos()
        controlador.navigate(direcao)
    }

    fun temConexao(): Boolean {
        val conexao: ConnectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capacidadeDeConexao = conexao.getNetworkCapabilities(conexao.activeNetwork)
        val conexaoAtiva =
            capacidadeDeConexao?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        return conexaoAtiva
    }


}