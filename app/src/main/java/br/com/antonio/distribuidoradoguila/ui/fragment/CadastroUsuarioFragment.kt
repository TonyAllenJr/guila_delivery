package br.com.antonio.distribuidoradoguila.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import br.com.antonio.distribuidoradoguila.NavGraphDirections
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.model.Endereco
import br.com.antonio.distribuidoradoguila.model.Usuario
import br.com.antonio.distribuidoradoguila.ui.extension.escondeTeclado
import br.com.antonio.distribuidoradoguila.ui.extension.snackBar
import br.com.antonio.distribuidoradoguila.ui.viewmodel.ComponentesVisuais
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.BAIRRO_NAO_PREENCHIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.CADASTRO_ATUALIZADO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EDITAR_USUARIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ENDERECO_NAO_PREENCHIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NAO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NOME_NAO_PREENCHIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NUMERO_COMPLEMENTO_NAO_PREENCHIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SAIR_APLICATIVO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SEM_NUMERO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SIM
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.TELEFONE_11_DIGITOS
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.TELEFONE_11_DIGITOS_OBRIGATORIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.TELEFONE_15_DIGITOS
import br.com.antonio.distribuidoradoguila.util.formatador.FormatadorTelefone
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.cadastro_usuario.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CadastroUsuarioFragment : BaseFragment() {

    private val controlador by lazy { findNavController() }
    private val viewModel: UsuarioViewModel by viewModel()
    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()
    private val firebaseAuth: FirebaseAuth by inject()
    private val formatador: FormatadorTelefone by inject()

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
        return inflater.inflate(R.layout.cadastro_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        estadoAppViewModel.temComponentes = ComponentesVisuais(appBar = true,
            bottomNavigation = true)

        buscaUsuario()
        configuraBotaoCadastro()
        configuraTelefone()
        configurarFocusDosCampos()
        if(cadastro_usuario_nome_completo.editText?.text.toString().isNotBlank()) {
            activity?.title = EDITAR_USUARIO
        }
    }

    private fun configurarFocusDosCampos() {

        removeFocus(cadastro_usuario_nome_completo.editText, cadastro_usuario_nome_completo)
        removeFocus(cadastro_usuario_endereco.editText, cadastro_usuario_endereco)
        removeFocus(cadastro_usuario_bairro.editText, cadastro_usuario_bairro)
        removeFocus(cadastro_usuario_numero.editText, cadastro_usuario_complemento)
        removeFocus(cadastro_usuario_complemento.editText, cadastro_usuario_complemento)
    }

    private fun removeFocus(editText: EditText?, textInputLayout: TextInputLayout) {
        editText?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editText.doAfterTextChanged {
                    it?.let { caracter ->
                        if (caracter.isNotEmpty()) {
                            textInputLayout.error = null
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_sair, menu)
        inflater.inflate(R.menu.menu_alterar_senha, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_botao_sair) {
            AlertDialog
                .Builder(context)
                .setTitle(SAIR_APLICATIVO)
                .setPositiveButton(SIM) { _, _ ->
                    viewModel.desloga()
                    vaiParaLogin()
                }
                .setNegativeButton(NAO, null)
                .show()
        }
        if(item.itemId == R.id.menu_botao_alterar_senha) {
            CadastroUsuarioFragmentDirections.acaoCadastroUsuarioParaAlteraSenha()
                .let(controlador::navigate)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun vaiParaLogin() {
        val direcao =
            NavGraphDirections.acaoGlobalLogin()
        controlador.navigate(direcao)
    }

    private fun configuraTelefone() {
        cadastro_usuario_telefone.editText?.setOnFocusChangeListener { view, hasFocus ->
            view?.let {
                val telefone = cadastro_usuario_telefone.editText?.text.toString()
                val telefoneSemFormatacao = formatador.removeCaracter(telefone)
                if (hasFocus) {
                    cadastro_usuario_telefone.editText?.setText(telefoneSemFormatacao)
                    cadastro_usuario_telefone.editText?.doAfterTextChanged {
                        it?.let { fone ->
                            if (fone.length == TELEFONE_11_DIGITOS) {
                                validaTelefone()
                            }
                        }
                    }
                } else {
                    validaQuantidadeDeDigitos(telefoneSemFormatacao)
                }

            }
        }
    }

    private fun validaTelefone(): Boolean {
        val telefone = cadastro_usuario_telefone?.editText?.text.toString()
        val telefoneSemFormatacao = formatador.removeCaracter(telefone)
        if (!validaQuantidadeDeDigitos(telefoneSemFormatacao)) {
            return false
        } else {
            cadastro_usuario_telefone.error = null
        }
        val telefoneFormatado = formatador.formata(telefone)
        cadastro_usuario_telefone.editText?.setText(telefoneFormatado)
        return true
    }

    private fun validaQuantidadeDeDigitos(telefone: String): Boolean {
        if (telefone.length < TELEFONE_11_DIGITOS) {
            cadastro_usuario_telefone.error = TELEFONE_11_DIGITOS_OBRIGATORIO
            return false
        }
        return true
    }

    private fun buscaUsuario() {
        viewModel.login.observe(viewLifecycleOwner, {
            it?.let { usuario ->
                cadastro_usuario_email.text = usuario.email
                cadastro_usuario_nome_completo.editText?.setText(usuario.nome)
                cadastro_usuario_endereco.editText?.setText(usuario.endereco.rua)
                cadastro_usuario_numero.editText?.setText(usuario.endereco.numero)
                cadastro_usuario_complemento.editText?.setText(usuario.endereco.complemento)
                cadastro_usuario_referencia.editText?.setText(usuario.endereco.referencia)
                cadastro_usuario_bairro.editText?.setText(usuario.endereco.bairro)
                cadastro_usuario_telefone.editText?.setText(usuario.telefone)


            }
        })
    }

    private fun configuraBotaoCadastro() {
        cadastro_usuario_botao_cadastrar.setOnClickListener {

            limpaTodosCampos()

            val nome = cadastro_usuario_nome_completo.editText?.text.toString()
            val email = firebaseAuth.currentUser?.email
            val rua = cadastro_usuario_endereco.editText?.text.toString()
            var numero = cadastro_usuario_numero.editText?.text.toString()
            val complemento = cadastro_usuario_complemento.editText?.text.toString()
            val referencia = cadastro_usuario_referencia.editText?.text.toString()
            val bairro = cadastro_usuario_bairro.editText?.text.toString()
            val telefone = cadastro_usuario_telefone.editText?.text.toString()
            val valido = validaCampos(nome, rua, numero, bairro, telefone, complemento)


            if (valido) {

                if (email != null) {
                    if (numero.isBlank()) {
                        numero = SEM_NUMERO
                    }
                    cadastra(
                        Usuario(
                            nome, email,
                            Endereco(rua, numero,
                                complemento, referencia,
                                bairro)
                            , telefone, token = ""
                        )
                    )
                }
            }
            it.escondeTeclado()
        }
    }

    private fun cadastra(usuario: Usuario) {
        viewModel.cadastra(usuario).observe(viewLifecycleOwner, {
            it?.let {
                view?.snackBar(CADASTRO_ATUALIZADO)
                controlador.popBackStack()
            }
        })
    }

    private fun validaCampos(
        nome: String, rua: String, numero: String,
        bairro: String, telefone: String, complemento: String,
    ): Boolean {
        var valido = true

        if (nome.isBlank()) {
            cadastro_usuario_nome_completo.error = NOME_NAO_PREENCHIDO
            valido = false
        }

        if (bairro.isBlank()) {
            cadastro_usuario_bairro.error = BAIRRO_NAO_PREENCHIDO
            valido = false
        }

        if (rua.isBlank()) {
            cadastro_usuario_endereco.error = ENDERECO_NAO_PREENCHIDO
            valido = false
        }

        if (numero.isBlank() && complemento.isBlank()) {

            cadastro_usuario_complemento.error = NUMERO_COMPLEMENTO_NAO_PREENCHIDO
            valido = false
        }

        if (telefone.isBlank() || telefone.length < TELEFONE_15_DIGITOS) {
            cadastro_usuario_telefone.error = TELEFONE_11_DIGITOS_OBRIGATORIO
            valido = false
        }

        return valido
    }

    private fun limpaTodosCampos() {
        cadastro_usuario_nome_completo.error = null
        cadastro_usuario_endereco.error = null
        cadastro_usuario_numero.error = null
        cadastro_usuario_bairro.error = null
        cadastro_usuario_telefone.error = null
    }
}