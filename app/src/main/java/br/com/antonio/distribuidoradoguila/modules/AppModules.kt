package br.com.antonio.distribuidoradoguila.modules

import androidx.room.Room
import br.com.antonio.distribuidoradoguila.database.AppDataBase
import br.com.antonio.distribuidoradoguila.database.dao.NumeroPedidoDao
import br.com.antonio.distribuidoradoguila.model.Usuario
import br.com.antonio.distribuidoradoguila.repository.*
import br.com.antonio.distribuidoradoguila.ui.adapter.*
import br.com.antonio.distribuidoradoguila.ui.viewmodel.*
import br.com.antonio.distribuidoradoguila.util.Util
import br.com.antonio.distribuidoradoguila.util.formatador.FormatadorTelefone
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataBaseModule = module {
    single<AppDataBase> {
        Room
            .databaseBuilder(
                get(),
                AppDataBase::class.java,
                "produto.db")
            .build()
    }
}

val viewModelModule = module {
    viewModel<ProdutoViewModel> {ProdutoViewModel(get())}
    viewModel<FormularioProdutoViewModel> { FormularioProdutoViewModel (get()) }
    viewModel<UsuarioViewModel> {UsuarioViewModel(get())}
    viewModel<EstadoAppViewModel> {EstadoAppViewModel()}
    viewModel<CarrinhoViewModel> {CarrinhoViewModel(get(), get())}
    viewModel<PedidoViewModel> { PedidoViewModel(get()) }
    viewModel<ConfiguracaoViewModel> { ConfiguracaoViewModel(get()) }
    viewModel<MensagemViewModel> { MensagemViewModel(get())  }
}

val repositoryModule = module {
    single<NumeroPedidoDao> {get<AppDataBase>().numeroPedidoDao}
    single<ProdutoRepository> {ProdutoRepository(get(), get())}
    single<FirebaseAuthRepository> { FirebaseAuthRepository(get(), get()) }
    single<CarrinhoRepository> { CarrinhoRepository(get(), get()) }
    single<PedidoRepository> {PedidoRepository(get(), get())}
    single<ConfiguracaoRepository> { ConfiguracaoRepository(get(), get()) }
    single<MensagemRepository> {MensagemRepository(get(), get())}
}

val uiModule = module {
    factory<ListaProdutosAdapter> {ListaProdutosAdapter(get())}
    factory<ListaCarrinhoAdapter> { ListaCarrinhoAdapter(get()) }
    factory<ListaPedidosAdapter> {ListaPedidosAdapter(get())}
    factory<ListaMensagensAdapter> { ListaMensagensAdapter(get()) }
    factory<RelatorioVendasAdapter> {RelatorioVendasAdapter(get())}
    factory<RelatorioEstoqueAdapter> {RelatorioEstoqueAdapter(get())}
    factory<Util>{ Util() }
    factory<Usuario> {Usuario()}
    factory<FormatadorTelefone> {FormatadorTelefone()}
}

val firebaseModule = module {
    single<FirebaseAuth> { Firebase.auth}
    single<FirebaseFirestore> {Firebase.firestore}
}
