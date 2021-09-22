package br.com.antonio.distribuidoradoguila.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.antonio.distribuidoradoguila.model.Configuracao
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_CONFIGURACOES
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.DOCUMENTO_PATH_NUMERO_UM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.math.BigDecimal
import java.math.RoundingMode

class ConfiguracaoRepository(
    val firestore: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth,

    ) {

    fun salva(configuracao: Configuracao): LiveData<Resource<Boolean>> = MutableLiveData<Resource<Boolean>>().apply {
        val configuracaoDocumento = firebaseAuth.currentUser?.uid?.let { usuarioId ->
            ConfiguracaoDocumento(
                taxaEntrega = configuracao.taxaEntrega.toDouble(),
                pedidoMinimo = configuracao.pedidoMinimo.toDouble(),
                appAtivo = configuracao.appAtivo
            )
        }

        value = if (configuracaoDocumento != null) {
            firebaseAuth.currentUser?.uid?.let {
                firestore.collection(COLECAO_FIRESTORE_CONFIGURACOES).document(
                    DOCUMENTO_PATH_NUMERO_UM)
            }?.set(configuracaoDocumento)
            Resource(true)
        }else {
            Resource(false)
        }

    }

    fun busca(): LiveData<List<Configuracao>> =
        MutableLiveData<List<Configuracao>>().apply {
            firestore.collection(COLECAO_FIRESTORE_CONFIGURACOES)
                .addSnapshotListener { s, _ ->
                    s?.let { snapshot ->
                        val valores: List<Configuracao> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<ConfiguracaoDocumento>()
                                    ?.paraConfiguracao(documento.id)
                            }
                        value = valores
                    }
                }
        }

    private class ConfiguracaoDocumento(
        val appAtivo: Boolean = false,
        val pedidoMinimo: Double = 0.0,
        val taxaEntrega: Double = 0.0


    ) {
        fun paraConfiguracao(id: String): Configuracao = Configuracao(
            id = id,
            appAtivo = appAtivo,
            pedidoMinimo = BigDecimal(pedidoMinimo).setScale(2, RoundingMode.HALF_EVEN),
            taxaEntrega = BigDecimal(taxaEntrega).setScale(2, RoundingMode.HALF_EVEN),
        )
    }

}