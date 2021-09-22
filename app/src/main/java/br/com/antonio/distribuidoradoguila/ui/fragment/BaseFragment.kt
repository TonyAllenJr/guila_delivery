package br.com.antonio.distribuidoradoguila.ui.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.NavGraphDirections
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class BaseFragment : Fragment() {

    private val usuarioViewModel : UsuarioViewModel by viewModel()
    private val controlador by lazy {findNavController()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verificaSeLogado()
        temConexao()

    }

    protected fun temConexao() : Boolean {
        val conexao: ConnectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capacidadeDeConexao = conexao.getNetworkCapabilities(conexao.activeNetwork)
        val conexaoAtiva =
            capacidadeDeConexao?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        if (!conexaoAtiva) {
            NavGraphDirections.erroConexao()
                .let(controlador::navigate)
        }
        return conexaoAtiva
    }

    private fun vaiParaLogin() {
        val direcao =
                NavGraphDirections.acaoGlobalLogin()
        controlador.navigate(direcao)
    }

    private fun verificaSeLogado() {
        if (usuarioViewModel.naoEstaLogado()) {
            vaiParaLogin()
        }
    }

}