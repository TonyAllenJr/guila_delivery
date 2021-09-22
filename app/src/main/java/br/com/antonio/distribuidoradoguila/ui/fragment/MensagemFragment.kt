package br.com.antonio.distribuidoradoguila.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Mensagem
import br.com.antonio.distribuidoradoguila.ui.extension.escondeTeclado
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.MensagemViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CARACTER_MAXIMO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.MENSAGEM_ENVIADA
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.mensagem.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MensagemFragment : Fragment() {

    private val viewModel: MensagemViewModel by viewModel()
    private val usuarioViewModel: UsuarioViewModel by viewModel()
    private val controlador by lazy { findNavController() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.mensagem, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preencheMensagem(view)

    }

    private fun preencheMensagem(view: View) {
        mensagem_descricao.editText?.doAfterTextChanged {
            it?.let { caracter ->
                val caracterRestante = CARACTER_MAXIMO - caracter.length
                mensagem_caracteres.text = caracterRestante.toString()
                mensagem_botao_enviar.setOnClickListener {
                    if (caracter.isNotBlank()) {
                        usuarioViewModel.buscaPorIdAutenticado()
                            .observe(viewLifecycleOwner, { usuario ->
                                viewModel.salva(Mensagem(
                                    descricao = caracter.toString(),
                                    nome = usuario.nome,
                                    email = usuario.email,
                                    telefone = usuario.telefone,
                                )).observe(
                                    viewLifecycleOwner, {
                                        view.snackBar(MENSAGEM_ENVIADA)
                                        controlador.popBackStack()
                                        view.escondeTeclado()
                                    }
                                )
                            })

                    }
                    usuarioViewModel.buscaUsuarios.observe(viewLifecycleOwner, { usuarios ->
                        usuarios.forEach { usuario ->
                            if (usuario.administrador) {
                                FirebaseFirestore.getInstance().collection("notificationsMessage")
                                    .document(usuario.token)
                                    .set(usuario)
                            }
                        }
                    })
                }
            }
        }
    }
}