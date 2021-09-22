package br.com.antonio.distribuidoradoguila.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.REDEFINICAO_SENHA
import kotlinx.android.synthetic.main.redefine_senha.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RedefineSenhaFragment : Fragment() {

    private val controlador by lazy { findNavController() }
    private val viewModel: UsuarioViewModel by viewModel()
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.redefine_senha, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais()

        redefine_senha_botao.setOnClickListener {view ->
            view?.let {

                val email = redefine_senha_email.editText?.text.toString()

                viewModel.redefineSenha(email).observe(viewLifecycleOwner, {
                    it?.let {
                        RedefineSenhaFragmentDirections.acaoRedefineSenhaParaLogin()
                            .let(controlador::navigate)
                        AlertDialog
                            .Builder(context)
                            .setMessage(REDEFINICAO_SENHA)
                            .show()
                    }
                })

            }
        }
    }


}