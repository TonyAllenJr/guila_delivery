package br.com.antonio.distribuidoradoguila.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.antonio.distribuidoradoguila.model.NumeroPedido
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_CONTADOR
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_USUARIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.DOCUMENTO_PATH_NUMERO_UM
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ENTREGUE
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ENVIADO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.HORA_ENTREGA
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.HORA_ENVIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NUMERO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.TIPO_PAGAMENTO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.USUARIO_ID
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class PedidoRepository(
    val firestore: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth,

    ) {

    fun salva(pedido: Pedido): LiveData<Resource<Boolean>> =
        MutableLiveData<Resource<Boolean>>().apply {
            val pedidoDocumento = firebaseAuth.currentUser?.uid?.let { usuarioId ->
                PedidoDocumento(
                    numero = pedido.numero,
                    tipoPagamento = pedido.tipoPagamento,
                    data = pedido.data,
                    subTotal = pedido.subTotal.toDouble(),
                    valorTotal = pedido.valorTotal.toDouble(),
                    taxaEntrega = pedido.taxaEntrega.toDouble(),
                    usuarioId = usuarioId,
                    horaEnvio = pedido.horaEnvio,
                    horaEntrega = pedido.horaEntrega,
                    enviado = pedido.enviado,
                    entregue = pedido.entregue,
                )
            }

            val collection = firebaseAuth.currentUser?.uid?.let {
                firestore.collection(COLECAO_FIRESTORE_USUARIO)
                    .document(it).collection(COLECAO_FIRESTORE_PEDIDO)
            }
            val document = pedido.id?.let { id ->
                collection?.document(id)
            } ?: collection?.document()

            value = if (pedidoDocumento != null) {
                document?.set(pedidoDocumento)
                Resource(true)
            } else {
                Resource(false)
            }


        }

    fun atualizaDados(pedido: Pedido, pedidoId: String): LiveData<Resource<Boolean>> =
        MutableLiveData<Resource<Boolean>>().apply {

            val pedidoDocumento = firebaseAuth.currentUser?.uid?.let { usuarioId ->
                PedidoDocumento(
                    numero = pedido.numero,
                    tipoPagamento = pedido.tipoPagamento,
                    data = pedido.data,
                    subTotal = pedido.subTotal.toDouble(),
                    valorTotal = pedido.valorTotal.toDouble(),
                    taxaEntrega = pedido.taxaEntrega.toDouble(),
                )
            }

            val usuario = firebaseAuth.currentUser?.uid
            if (usuario != null) {
                val document = firestore.collection(COLECAO_FIRESTORE_USUARIO)
                    .document(usuario)
                    .collection(COLECAO_FIRESTORE_PEDIDO)
                    .document(pedidoId)
                if (pedidoDocumento != null) {
                    document.update(
                        "numero", pedidoDocumento.numero,
                        "tipoPagamento", pedidoDocumento.tipoPagamento,
                        "data", pedidoDocumento.data,
                        "subTotal", pedidoDocumento.subTotal,
                        "valorTotal", pedidoDocumento.valorTotal,
                        "taxaEntrega", pedidoDocumento.taxaEntrega
                    )
                }

                value = Resource(true)

            }
        }

    fun salvaNumeroPedido(numeroPedido: NumeroPedido): LiveData<Boolean> =
        MutableLiveData<Boolean>().apply {
            val nrPedidoDocumento = firebaseAuth.currentUser?.uid.let { usuarioId ->
                NumeroPedidoDocumento(
                    pedido = numeroPedido.pedido,
                    usuarioId = usuarioId
                )
            }

            val colecao = firestore.collection(COLECAO_FIRESTORE_CONTADOR)
            val documento = numeroPedido.id?.let { id ->
                colecao.document(id)
            } ?: colecao.document()

            documento.set(nrPedidoDocumento)


            value = true

        }

    fun buscaPedidoEmAberto(): LiveData<List<Pedido>> = MutableLiveData<List<Pedido>>().apply {
        val usuario = firebaseAuth.currentUser?.uid
        if (usuario != null) {
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(usuario)
                .collection(COLECAO_FIRESTORE_PEDIDO)
                .whereEqualTo(USUARIO_ID, usuario)
                .whereEqualTo(NUMERO, VALOR_CONSTANTE_ZERO)
                .addSnapshotListener { s, _ ->
                    s?.let { snapshot ->
                        val pedidos: List<Pedido> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<PedidoDocumento>()
                                    ?.paraPedido(documento.id)

                            }
                        val selector: (Pedido) -> Long? = {
                            it.numero
                        }
                        value = pedidos.sortedByDescending(selector)

                    }

                }
        }
    }

    fun buscaPedidosFinalizados(): LiveData<List<Pedido>> = MutableLiveData<List<Pedido>>().apply {
        val usuario = firebaseAuth.currentUser?.uid
        if (usuario != null) {
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(usuario)
                .collection(COLECAO_FIRESTORE_PEDIDO)
                .whereNotEqualTo(NUMERO, VALOR_CONSTANTE_ZERO)
                .addSnapshotListener { s, _ ->
                    s?.let { snapshot ->
                        val pedidos: List<Pedido> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<PedidoDocumento>()
                                    ?.paraPedido(documento.id)

                            }
                        val selector: (Pedido) -> Long? = {
                            it.numero
                        }
                        value = pedidos.sortedByDescending(selector)

                    }

                }
        }
    }

    fun buscaTodos(): LiveData<List<Pedido>> = MutableLiveData<List<Pedido>>().apply {
        CoroutineScope(Dispatchers.IO).launch {
            firestore.collectionGroup(COLECAO_FIRESTORE_PEDIDO)
                .whereNotEqualTo(NUMERO, VALOR_CONSTANTE_ZERO)
                .addSnapshotListener { s, _ ->
                    s?.let { snapshot ->
                        val pedidos: List<Pedido> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<PedidoDocumento>()
                                    ?.paraPedido(documento.id)

                            }
                        val selector: (Pedido) -> Long? = {
                            it.numero
                        }
                        value = pedidos.sortedByDescending(selector)
                    }

                }
        }
    }

    fun buscaTodos1(): LiveData<List<Pedido>> = MutableLiveData<List<Pedido>>().apply {
        CoroutineScope(Dispatchers.IO).launch {
            firestore.collectionGroup(COLECAO_FIRESTORE_PEDIDO)
                .whereNotEqualTo(NUMERO, VALOR_CONSTANTE_ZERO)
                .get()
                .addOnSuccessListener { q ->
                    q.let { snapshot ->
                        val pedidos: List<Pedido> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<PedidoDocumento>()
                                    ?.paraPedido(documento.id)

                            }
                        val selector: (Pedido) -> Long? = {
                            it.numero
                        }
                        value = pedidos.sortedByDescending(selector)

                    }

                }
        }
    }


    fun buscaUltimoPedido(): LiveData<Long> = MutableLiveData<Long>().apply {
        firestore.collection(COLECAO_FIRESTORE_CONTADOR)
            .get()
            .addOnSuccessListener { documents ->
                value = if (documents.isEmpty) {
                    0
                } else {
                    val pedido = documents.toObjects<NumeroPedidoDocumento>().maxOf {
                        it.pedido
                    }
                    pedido
                }
            }
    }

    fun buscaPedidoPorIdUsuario(id: String): LiveData<Pedido> =
        MutableLiveData<Pedido>().apply {
            val usuario = firebaseAuth.currentUser?.uid
            if (usuario != null) {
                firestore.collection(COLECAO_FIRESTORE_USUARIO)
                    .document(usuario)
                    .collection(COLECAO_FIRESTORE_PEDIDO)
                    .document(id)
                    .addSnapshotListener { s, _ ->
                        s?.let { documento ->
                            documento.toObject<PedidoDocumento>()?.paraPedido(documento.id)
                                ?.let { pedido ->
                                    value = pedido
                                }
                        }

                    }
            }

        }

    fun buscaPedidoPorIdAdmin(id: String, usuario: String): LiveData<Pedido> =
        MutableLiveData<Pedido>().apply {
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(usuario)
                .collection(COLECAO_FIRESTORE_PEDIDO)
                .document(id)
                .addSnapshotListener { s, _ ->
                    s?.let { documento ->
                        documento.toObject<PedidoDocumento>()?.paraPedido(documento.id)
                            ?.let { pedido ->
                                value = pedido
                            }
                    }
                }
        }


    fun buscaUsuarioIdContador(numeroPedido: Long): LiveData<String> =
        MutableLiveData<String>().apply {
            firestore.collection(COLECAO_FIRESTORE_CONTADOR)
                .whereEqualTo(PEDIDO, numeroPedido)
                .get()
                .addOnSuccessListener {
                    it.forEach { documento ->
                        documento.toObject<NumeroPedidoDocumento>().paraNumeroPedido()
                            .let { usuario -> value = usuario.id }
                    }
                }

        }

    fun atualizaEnvio(usuarioId: String, pedidoId: String, hora: Date) {
        val collection = firestore.collection(COLECAO_FIRESTORE_USUARIO)
            .document(usuarioId)
            .collection(COLECAO_FIRESTORE_PEDIDO)

        collection.document(pedidoId).update(HORA_ENVIO, hora)
        collection.document(pedidoId).update(ENVIADO, true)
    }

    fun atualizaEntrega(usuarioId: String, pedidoId: String, hora: Date) {
        val collection = firestore.collection(COLECAO_FIRESTORE_USUARIO)
            .document(usuarioId)
            .collection(COLECAO_FIRESTORE_PEDIDO)

        collection.document(pedidoId).update(HORA_ENTREGA, hora)
        collection.document(pedidoId).update(ENTREGUE, true)
    }

    fun atualizaTipoPagamento(tipoPagamento: String, valor: BigDecimal) {
        firestore.collection(TIPO_PAGAMENTO)
            .document(DOCUMENTO_PATH_NUMERO_UM)
            .update(tipoPagamento, valor.toDouble())
    }

    fun buscaTipoPagamento(tipoPagamento: String): LiveData<BigDecimal> =
        MutableLiveData<BigDecimal>().apply {
            firestore.collection(TIPO_PAGAMENTO)
                .document(DOCUMENTO_PATH_NUMERO_UM)
                .get()
                .addOnSuccessListener {
                    val valor = it.data?.get(tipoPagamento).toString()
                    value = BigDecimal(valor)
                }
        }

    fun remove(usuarioId: String, pedidoId: String) {
        firestore.collection(COLECAO_FIRESTORE_USUARIO)
            .document(usuarioId)
            .collection(COLECAO_FIRESTORE_PEDIDO)
            .document(pedidoId)
            .delete()
    }

}

private class NumeroPedidoDocumento(
    val pedido: Long = 0,
    val usuarioId: String? = "",
) {
    fun paraNumeroPedido(): NumeroPedido = NumeroPedido(
        pedido = pedido,
        id = usuarioId
    )
}

private class PedidoDocumento(
    val numero: Long = 0,
    val tipoPagamento: String = "",
    val data: Date = Calendar.getInstance().time,
    val subTotal: Double = 0.0,
    val valorTotal: Double = 0.0,
    val taxaEntrega: Double = 0.0,
    val usuarioId: String = "",
    val horaEnvio: Date = Calendar.getInstance().time,
    val horaEntrega: Date = Calendar.getInstance().time,
    val enviado: Boolean = false,
    val entregue: Boolean = false,

    ) {

    fun paraPedido(id: String): Pedido = Pedido(
        id = id,
        numero = numero,
        tipoPagamento = tipoPagamento,
        data = data,
        horaEnvio = horaEnvio,
        horaEntrega = horaEntrega,
        enviado = enviado,
        entregue = entregue,
        subTotal = BigDecimal(subTotal).setScale(2, RoundingMode.HALF_EVEN),
        valorTotal = BigDecimal(valorTotal).setScale(2, RoundingMode.HALF_EVEN),
        taxaEntrega = BigDecimal(taxaEntrega).setScale(2, RoundingMode.HALF_EVEN),

        )
}

