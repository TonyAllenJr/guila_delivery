package br.com.antonio.distribuidoradoguila.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.antonio.distribuidoradoguila.model.ItemCarrinho
import br.com.antonio.distribuidoradoguila.model.Pedido
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_ITEM_CARRINHO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_USUARIO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NUMERO_PEDIDO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.USUARIO_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.math.BigDecimal
import java.math.RoundingMode

class CarrinhoRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) {


    fun salva(itemCarrinho: ItemCarrinho, pedido: Pedido): LiveData<Boolean> =
        MutableLiveData<Boolean>().apply {

            val itemCarrinhoDocumento = firebaseAuth.currentUser?.uid?.let {
                ItemCarrinhoDocumento(
                    quantidade = itemCarrinho.quantidade,
                    nome = itemCarrinho.nome,
                    imagemUrl = itemCarrinho.imagemUrl,
                    preco = itemCarrinho.preco.toDouble(),
                    precoTotalPorItem = itemCarrinho.precoTotalPorItem.toDouble(),
                    usuarioId = it,
                    numeroPedido = itemCarrinho.numeroPedido,
                    produtoId = itemCarrinho.produtoId,
                    pedidoId = itemCarrinho.pedidoId
                )

            }

            val collection = firebaseAuth.currentUser?.uid?.let { usuario ->
                pedido.id?.let { pedido ->
                    firestore.collection(COLECAO_FIRESTORE_USUARIO)
                        .document(usuario).collection(COLECAO_FIRESTORE_PEDIDO).document(pedido)
                        .collection(COLECAO_FIRESTORE_ITEM_CARRINHO)
                }
            }

            val document = itemCarrinho.id?.let { id ->
                collection?.document(id)
            } ?: collection?.document()

            if (itemCarrinhoDocumento != null) {
                document?.set(itemCarrinhoDocumento)
            }

            value = true


        }

    fun buscaPorId(id: String, pedidoId: String): LiveData<ItemCarrinho> =
        MutableLiveData<ItemCarrinho>().apply {
            val usuario = firebaseAuth.currentUser?.uid
            if (usuario != null) {
                firestore.collection(COLECAO_FIRESTORE_USUARIO)
                    .document(usuario)
                    .collection(COLECAO_FIRESTORE_PEDIDO)
                    .document(pedidoId)
                    .collection(COLECAO_FIRESTORE_ITEM_CARRINHO)
                    .document(id)
                    .addSnapshotListener { s, _ ->
                        s?.let { documento ->
                            documento.toObject<ItemCarrinhoDocumento>()
                                ?.paraItemCarrinho(documento.id)
                                ?.let { produto ->
                                    value = produto
                                }
                        }
                    }
            }

        }

    fun buscaItens(numeroPedido: Long, pedidoId: String): LiveData<List<ItemCarrinho>> =
        MutableLiveData<List<ItemCarrinho>>().apply {
            val usuario = firebaseAuth.currentUser?.uid
            if (usuario != null) {
                firestore.collection(COLECAO_FIRESTORE_USUARIO)
                    .document(usuario)
                    .collection(COLECAO_FIRESTORE_PEDIDO)
                    .document(pedidoId)
                    .collection(COLECAO_FIRESTORE_ITEM_CARRINHO)
                    .whereEqualTo(USUARIO_ID, usuario)
                    .whereEqualTo(NUMERO_PEDIDO, numeroPedido)
                    .addSnapshotListener { s, _ ->
                        s?.let { snapshot ->
                            val itensCarrinho: List<ItemCarrinho> =
                                snapshot.documents.mapNotNull { documento ->
                                    documento.toObject<ItemCarrinhoDocumento>()
                                        ?.paraItemCarrinho(documento.id)
                                }
                            value = itensCarrinho
                        }
                    }
            }
        }

    fun buscaItensAdmin(
        numeroPedido: Long,
        usuario: String,
        pedidoId: String,
    ): LiveData<List<ItemCarrinho>> =
        MutableLiveData<List<ItemCarrinho>>().apply {
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(usuario)
                .collection(COLECAO_FIRESTORE_PEDIDO)
                .document(pedidoId)
                .collection(COLECAO_FIRESTORE_ITEM_CARRINHO)
                .whereEqualTo(USUARIO_ID, usuario)
                .whereEqualTo(NUMERO_PEDIDO, numeroPedido)
                .addSnapshotListener { s, _ ->
                    s?.let { snapshot ->
                        val itensCarrinho: List<ItemCarrinho> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<ItemCarrinhoDocumento>()
                                    ?.paraItemCarrinho(documento.id)
                            }
                        value = itensCarrinho
                    }
                }
        }


    fun atualizaNumeroPedido(numeroPedido: Long, pedidoId: String) {
        val usuario = firebaseAuth.currentUser?.uid
        val collection = usuario?.let {
            firestore.collection(COLECAO_FIRESTORE_USUARIO)
                .document(it)
                .collection(COLECAO_FIRESTORE_PEDIDO)
                .document(pedidoId)
                .collection(COLECAO_FIRESTORE_ITEM_CARRINHO)
        }
        if (usuario != null) {
            collection?.whereEqualTo(USUARIO_ID, usuario)?.whereEqualTo(NUMERO_PEDIDO, 0)?.get()
                ?.addOnSuccessListener { snapshot ->
                    snapshot?.let {
                        val itensCarrinho: List<ItemCarrinho> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<ItemCarrinhoDocumento>()
                                    ?.paraItemCarrinho(documento.id)
                            }

                        if (snapshot.size() > 0) {
                            itensCarrinho.forEach { itemCarrinho ->
                                itemCarrinho.id?.let {
                                    collection.document(it)
                                }?.update(NUMERO_PEDIDO, numeroPedido)
                            }
                        }
                    }

                }
        }
    }

    fun remove(itemCarrinhoId: String, pedidoId: String): LiveData<Boolean> =
        MutableLiveData<Boolean>().apply {
            val usuario = firebaseAuth.currentUser?.uid
            if (usuario != null) {
                firestore.collection(COLECAO_FIRESTORE_USUARIO)
                    .document(usuario)
                    .collection(COLECAO_FIRESTORE_PEDIDO)
                    .document(pedidoId)
                    .collection(COLECAO_FIRESTORE_ITEM_CARRINHO)
                    .document(itemCarrinhoId)
                    .delete()
                value = true
            }

        }


    private class ItemCarrinhoDocumento(
        val quantidade: Int = 0,
        val nome: String = "",
        val preco: Double = 0.0,
        val imagemUrl: String = "",
        val precoTotalPorItem: Double = 0.0,
        val usuarioId: String = "",
        val numeroPedido: Long = 0,
        val produtoId: String = "",
        val pedidoId: String = "",

        ) {
        fun paraItemCarrinho(id: String): ItemCarrinho = ItemCarrinho(
            id = id,
            quantidade = quantidade,
            nome = nome,
            imagemUrl = imagemUrl,
            preco = BigDecimal(preco).setScale(2, RoundingMode.HALF_EVEN),
            precoTotalPorItem = BigDecimal(precoTotalPorItem).setScale(2,
                RoundingMode.HALF_EVEN),
            numeroPedido = numeroPedido,
            produtoId = produtoId,
            pedidoId = pedidoId
        )

    }


}