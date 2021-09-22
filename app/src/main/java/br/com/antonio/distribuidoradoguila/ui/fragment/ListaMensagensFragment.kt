package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.adapter.ListaMensagensAdapter
import br.com.antonio.distribuidoradoguila.ui.viewmodel.MensagemViewModel
import kotlinx.android.synthetic.main.lista_mensagens.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListaMensagensFragment : BaseFragment() {

    private val adapter: ListaMensagensAdapter by inject()
    private val viewModel: MensagemViewModel by viewModel()
    private val controlador by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            controlador.popBackStack(R.id.listaProdutos, true)
            controlador.navigate(R.id.listaProdutos)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.lista_mensagens, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configuraLista()

        viewModel.buscaTodas().observe(viewLifecycleOwner, {
            it?.let {mensagens ->
                adapter.atualiza(mensagens)
            }
        })

    }

    private fun configuraLista() {
        lista_mensagens_recyclerview.adapter = adapter
    }


}

