package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import kotlinx.android.synthetic.main.pagina_erro_conexao.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ErroConexaoFragment : Fragment() {

    private val controlador by lazy { findNavController() }
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pagina_erro_conexao, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(appBar = false, bottomNavigation = false)

        pagina_erro_conexao_imagem_botao.setOnClickListener {
            controlador.popBackStack()
        }
    }
}