package br.com.antonio.distribuidoradoguila.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.antonio.distribuidoradoguila.model.Produto
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.COLECAO_FIRESTORE_PRODUTOS
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.ESTOQUE
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.NOME
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.QUANTIDADE_VENDIDA
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_CONSTANTE_ZERO
import br.com.antonio.distribuidoradoguila.util.Constantes.Companion.VALOR_VENDIDO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class ProdutoRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) {

    fun buscaPorId(id: String): LiveData<Produto> = MutableLiveData<Produto>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .document(id)
            .addSnapshotListener { s, _ ->
                s?.let { documento ->
                    documento.toObject<ProdutoDocumento>()?.paraProduto(documento.id)
                        ?.let { produto ->
                            value = produto
                        }
                }

            }
    }

    fun remove(produtoId: String): LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .document(produtoId)
            .delete()
        value = true
    }


    fun salva(produto: Produto): LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        val produtoDocumento = ProdutoDocumento(
            nome = produto.nome,
            preco = produto.preco.toDouble(),
            categoria = produto.categoria,
            imagemUrl = produto.imagemUrl,
            estoque = produto.estoque,
            quantidadeVendida = produto.quantidadeVendida,
            valorVendido = produto.valorVendido.toDouble()
        )

        val colecao = firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
        val documento = produto.id?.let { id ->
            colecao.document(id)
        } ?: colecao.document()

        documento.set(produtoDocumento)

        value = true
    }

    fun buscaTodos1(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .orderBy(ESTOQUE, Query.Direction.DESCENDING)
            .addSnapshotListener { s, _ ->
                devolveListaProdutos(s)
            }
    }

    fun buscaTodos(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        CoroutineScope(Dispatchers.IO).launch {
            firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
                .orderBy(ESTOQUE, Query.Direction.DESCENDING)
                .addSnapshotListener { s, _ ->
                    s?.let { snapshot ->
                        val produtos: List<Produto> = snapshot.documents.mapNotNull { documento ->
                            documento.toObject<ProdutoDocumento>()?.paraProduto(documento.id)
                        }
                        value = produtos
                    }
                }
        }


    }

    fun buscaPorNome(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .orderBy(NOME, Query.Direction.ASCENDING)
            .addSnapshotListener { s, _ ->
                devolveListaProdutos(s)
            }
    }

    fun buscaPorQuantidadeVendida(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .orderBy(QUANTIDADE_VENDIDA, Query.Direction.DESCENDING)
            .addSnapshotListener { s, _ ->
                devolveListaProdutos(s)
            }
    }

    fun buscaPorValorVendido(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .orderBy(VALOR_VENDIDO, Query.Direction.DESCENDING)
            .addSnapshotListener { s, _ ->
                devolveListaProdutos(s)
            }
    }

    fun buscaEstoqueProdutosPorNome(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .orderBy(NOME, Query.Direction.ASCENDING)
            .addSnapshotListener { s, _ ->
                devolveListaProdutos(s)
            }
    }

    fun buscaEstoqueProdutosPorQuantidade(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .orderBy(ESTOQUE, Query.Direction.DESCENDING)
            .addSnapshotListener { s, _ ->
                devolveListaProdutos(s)
            }
    }


    fun buscaEstoqueZero(): LiveData<List<Produto>> = MutableLiveData<List<Produto>>().apply {
        firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
            .whereEqualTo(ESTOQUE, VALOR_CONSTANTE_ZERO)
            .addSnapshotListener { s, _ ->
                devolveListaProdutos(s)
            }
    }

    fun removeQuantidadeEstoque(produtoId: String, quantidadeCarrinho: Int) {
        val usuario = firebaseAuth.currentUser?.uid
        val collection = firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
        if (usuario != null) {
            collection
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot?.let {
                        val produtos: List<Produto> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<ProdutoDocumento>()
                                    ?.paraProduto(documento.id)
                            }
                        produtos.forEach { produto ->
                            if (produto.id == produtoId) {
                                val estoqueAtualizado = produto.estoque - quantidadeCarrinho
                                produto.id.let {
                                    collection.document(it)
                                }.update(ESTOQUE, estoqueAtualizado)

                            }
                        }
                    }

                }
        }
    }

    fun adicionaQuantidadeEstoque(produtoId: String, quantidadeCarrinho: Int) {
        val usuario = firebaseAuth.currentUser?.uid
        val collection = firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
        if (usuario != null) {
            collection
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot?.let {
                        val produtos: List<Produto> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<ProdutoDocumento>()
                                    ?.paraProduto(documento.id)
                            }
                        produtos.forEach { produto ->
                            if (produto.id == produtoId) {
                                val estoqueAtualizado = produto.estoque + quantidadeCarrinho
                                produto.id.let {
                                    collection.document(it)
                                }.update(ESTOQUE, estoqueAtualizado)

                            }
                        }
                    }

                }
        }
    }

    fun atualizaQuantidadeEValorVendido(produtoId: String, quantidadeCarrinho: Int, valorCarrinho: BigDecimal) {
        val usuario = firebaseAuth.currentUser?.uid
        val collection = firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
        if (usuario != null) {
            collection
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot?.let {
                        val produtos: List<Produto> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<ProdutoDocumento>()
                                    ?.paraProduto(documento.id)
                            }
                        produtos.forEach { produto ->
                            if (produto.id == produtoId) {
                                val quantidadeVendidaAtualizada = produto.quantidadeVendida + quantidadeCarrinho
                                val valorVendidoAtualizado = (produto.valorVendido + valorCarrinho).toDouble()
                                produto.id.let {
                                    collection.document(it)
                                }.update(QUANTIDADE_VENDIDA, quantidadeVendidaAtualizada,
                                    VALOR_VENDIDO, valorVendidoAtualizado)

                            }
                        }
                    }

                }
        }
    }

    fun zeraQuatidadeEValorVendido() {
        val usuario = firebaseAuth.currentUser?.uid
        val collection = firestore.collection(COLECAO_FIRESTORE_PRODUTOS)
        if (usuario != null) {
            collection
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot?.let {
                        val produtos: List<Produto> =
                            snapshot.documents.mapNotNull { documento ->
                                documento.toObject<ProdutoDocumento>()
                                    ?.paraProduto(documento.id)
                            }
                        produtos.forEach { produto ->
                                produto.id.let {
                                    if (it != null) {
                                        collection.document(it)
                                            .update(
                                                QUANTIDADE_VENDIDA, 0,
                                                VALOR_VENDIDO, 0)
                                    }
                                }

                        }
                    }

                }
        }
    }

    private fun MutableLiveData<List<Produto>>.devolveListaProdutos(
        s: QuerySnapshot?,
    ) {
        s?.let { snapshot ->
            val produtos: List<Produto> = snapshot.documents.mapNotNull { documento ->
                documento.toObject<ProdutoDocumento>()?.paraProduto(documento.id)
            }
            value = produtos
        }
    }

    private class ProdutoDocumento(
        val nome: String = "",
        val preco: Double = 0.0,
        val categoria: String = "",
        val imagemUrl: String = "",
        val estoque: Int = 0,
        val quantidadeVendida: Int = 0,
        val valorVendido: Double = 0.0
    ) {
        fun paraProduto(id: String): Produto = Produto(
            id = id,
            nome = nome,
            preco = BigDecimal(preco).setScale(2, RoundingMode.HALF_EVEN),
            categoria = categoria,
            imagemUrl = imagemUrl,
            estoque = estoque,
            quantidadeVendida = quantidadeVendida,
            valorVendido = BigDecimal(valorVendido).setScale(2, RoundingMode.HALF_EVEN)

        )

    }

}

