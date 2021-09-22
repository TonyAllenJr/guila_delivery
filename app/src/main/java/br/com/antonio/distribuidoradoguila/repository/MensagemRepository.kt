package br.com.antonio.distribuidoradoguila.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.antonio.distribuidoradoguila.model.Mensagem
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_FALE_CONOSCO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class MensagemRepository(
    val firestore: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth,

    ) {

    fun salva(mensagem: Mensagem): LiveData<Resource<Boolean>> =
        MutableLiveData<Resource<Boolean>>().apply {
            val mensagemDocumento = firebaseAuth.currentUser?.uid?.let { usuarioId ->
                Mensagem(
                    descricao = mensagem.descricao,
                    data = Calendar.getInstance().time,
                    nome = mensagem.nome,
                    email = mensagem.email,
                    telefone = mensagem.telefone,
                    usuarioId = usuarioId
                )

            }

            val collection = firebaseAuth.currentUser?.uid?.let {
                firestore.collection(COLECAO_FIRESTORE_FALE_CONOSCO)
            }

            value = if (mensagemDocumento != null) {
                collection?.document()?.set(mensagemDocumento)
                Resource(true)
            } else {
                Resource(false)
            }

        }

    fun buscaTodas(): LiveData<List<Mensagem>> = MutableLiveData<List<Mensagem>>().apply {
        firestore.collectionGroup(COLECAO_FIRESTORE_FALE_CONOSCO)
            .orderBy(DATA, Query.Direction.DESCENDING)
            .addSnapshotListener { s, _ ->
                s?.let { snapshot ->
                    val mensagens: List<Mensagem> =
                        snapshot.documents.mapNotNull { documento ->
                            documento.toObject(Mensagem::class.java)
                        }
                    value = mensagens
                }

            }

    }

}