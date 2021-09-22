package br.com.antonio.distribuidoradoguila.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.antonio.distribuidoradoguila.model.Endereco
import br.com.antonio.distribuidoradoguila.model.Login
import br.com.antonio.distribuidoradoguila.model.Usuario
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_USUARIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EMAIL_INVALIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EMAIL_JA_CADASTRADO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EMAIL_NAO_VERIFICADO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EMAIL_SENHA_INVALIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.EMAIL_SENHA_SEM_PREENCHIMENTO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ERRO_DESCONHECIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.SENHA_6_DIGITOS
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    fun desloga() {
        firebaseAuth.signOut()
    }

    fun cadastraLoginESenha(login: Login): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        try {
            val tarefa =
                firebaseAuth.createUserWithEmailAndPassword(login.email, login.senha)
                    .addOnCompleteListener {
                        firebaseAuth.currentUser
                            ?.sendEmailVerification()
                    }
            tarefa.addOnSuccessListener {
                liveData.value = Resource(true)
            }
            tarefa.addOnFailureListener { exception ->
                val mensagemErro: String = devolveErroDeCadastro(exception)
                liveData.value = Resource(false, mensagemErro)
            }
        } catch (e: Exception) {
            liveData.value = Resource(false, EMAIL_SENHA_INVALIDO)

        }
        return liveData
    }

    fun altera(senha: String): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        val tarefa = firebaseAuth.currentUser?.updatePassword(senha)

        tarefa?.addOnSuccessListener {
            liveData.value = Resource(true)
        }
        tarefa?.addOnFailureListener {
            val mensagemErro: String = devolveErroDeCadastro(it)
            liveData.value = Resource(false, mensagemErro)
        }

        return liveData
    }

    fun redefineSenha(email: String): LiveData<Boolean> =
        MutableLiveData<Boolean>().apply {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { tarefa ->
                    if (tarefa.isSuccessful) {
                        value = true
                    }
                }
        }

    private fun devolveErroDeCadastro(exception: java.lang.Exception): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException -> SENHA_6_DIGITOS
            is FirebaseAuthInvalidCredentialsException -> EMAIL_INVALIDO
            is FirebaseAuthUserCollisionException -> EMAIL_JA_CADASTRADO
            else -> ERRO_DESCONHECIDO
        }
    }

    fun estaLogado(): Boolean {
        val usuarioFirebase: FirebaseUser? = firebaseAuth.currentUser
        val usuarioVerificado = firebaseAuth.currentUser?.isEmailVerified
        if (usuarioFirebase != null && usuarioVerificado!!) {
            return true
        }
        return false

    }

    fun autentica(login: Login): LiveData<Resource<Boolean>> {
        val liveData = MutableLiveData<Resource<Boolean>>()
        try {
            firebaseAuth.signInWithEmailAndPassword(login.email, login.senha)
                .addOnCompleteListener { tarefa ->
                    if (tarefa.isSuccessful) {
                        if (firebaseAuth.currentUser?.isEmailVerified!!
                        ) {
                            liveData.value = Resource(true)
                        } else {
                            val mensagemDeErro = devolveErroDeAutenticacao(tarefa.exception)
                            liveData.value = Resource(false, mensagemDeErro)
                        }
                    }
                    tarefa.addOnFailureListener {
                        val mensagemDeErro = devolveErroDeAutenticacao(it)
                        liveData.value = Resource(false, mensagemDeErro)
                    }
                }
        } catch (e: IllegalArgumentException) {
            liveData.value = Resource(false, EMAIL_SENHA_SEM_PREENCHIMENTO)
        }
        return liveData
    }

    private fun devolveErroDeAutenticacao(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> EMAIL_SENHA_INVALIDO
            is FirebaseAuthInvalidCredentialsException -> EMAIL_SENHA_INVALIDO
            else -> EMAIL_NAO_VERIFICADO
        }
    }

    fun busca(): LiveData<Usuario> = MutableLiveData<Usuario>().apply {
        firebaseAuth.currentUser?.uid?.let { id ->
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(id)
                .addSnapshotListener { s, _ ->
                    s?.let { documento ->
                        documento.toObject<UsuarioDocumento>()?.toUser()
                            ?.let { usuario ->
                                value = usuario
                            }
                    }
                }
        }
    }

    fun buscaTodos(): LiveData<List<Usuario>> = MutableLiveData<List<Usuario>>().apply {
        firestore.collection(COLECAO_FIRESTORE_USUARIO)
            .addSnapshotListener { s, _ ->
                s?.let { snapshot ->
                    val usuarios: List<Usuario> = snapshot.documents.mapNotNull { documento ->
                        documento.toObject<UsuarioDocumento>()?.toUser()
                    }
                    value = usuarios
                }
            }
    }

    fun buscaPorId(id: String): LiveData<Usuario> = MutableLiveData<Usuario>().apply {
        firestore.collection(COLECAO_FIRESTORE_USUARIO)
            .document(id)
            .addSnapshotListener { s, _ ->
                s?.let { documento ->
                    documento.toObject<UsuarioDocumento>()?.toUser()
                        ?.let { usuario ->
                            value = usuario
                        }
                }
            }
    }

    fun buscaPorIdAutenticado(): LiveData<Usuario> = MutableLiveData<Usuario>().apply {
        firebaseAuth.currentUser?.uid?.let { id ->
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(id)
        }
            ?.addSnapshotListener { s, _ ->
                s?.let { documento ->
                    documento.toObject<UsuarioDocumento>()?.toUser()
                        ?.let { usuario ->
                            value = usuario
                        }
                }
            }
    }

    fun cadastra(usuario: Usuario): LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        firebaseAuth.currentUser?.uid?.let { uid ->
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(uid).set(usuario)
        }
        value = true
    }

    fun geraToken(): Boolean {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("usuarios")
                    .document(uid)
                    .update("token", token)
            }
        }
        return true
    }

    class UsuarioDocumento(
        val nome: String = "",
        val email: String = "",
        val endereco: Endereco = Endereco(),
        val telefone: String = "",
        val administrador: Boolean = false,
        val administradorSecundario: Boolean = false,
        val desenvolvedor: Boolean = false,
        val token: String = "",
    ) {
        fun toUser(): Usuario = Usuario(
            nome = nome,
            email = email,
            endereco = Endereco(endereco.rua, endereco.numero, endereco.complemento,
                endereco.referencia, endereco.bairro),
            telefone = telefone,
            administrador = administrador,
            administradorSecundario = administradorSecundario,
            desenvolvedor = desenvolvedor,
            token = token
        )
    }

}

