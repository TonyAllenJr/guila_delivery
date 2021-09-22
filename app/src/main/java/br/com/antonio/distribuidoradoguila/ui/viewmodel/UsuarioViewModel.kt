package br.com.antonio.distribuidoradoguila.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.antonio.distribuidoradoguila.model.Login
import br.com.antonio.distribuidoradoguila.model.Usuario
import br.com.antonio.distribuidoradoguila.repository.FirebaseAuthRepository
import br.com.antonio.distribuidoradoguila.repository.Resource


class UsuarioViewModel(private val firebaseAuthRepository: FirebaseAuthRepository) : ViewModel() {

    val login : LiveData<Usuario> = firebaseAuthRepository.busca()

    val buscaUsuarios : LiveData<List<Usuario>> = firebaseAuthRepository.buscaTodos()

    fun buscaPorId(id: String): LiveData<Usuario> = firebaseAuthRepository.buscaPorId(id)

    fun buscaPorIdAutenticado(): LiveData<Usuario> = firebaseAuthRepository.buscaPorIdAutenticado()

    fun autentica(login: Login) : LiveData<Resource<Boolean>> = firebaseAuthRepository.autentica(login)

    fun cadastraLoginESenha(login: Login) : LiveData<Resource<Boolean>> = firebaseAuthRepository.cadastraLoginESenha(login)

    fun altera(senha: String) : LiveData<Resource<Boolean>> = firebaseAuthRepository.altera(senha)

    fun cadastra(usuario: Usuario): LiveData<Boolean> = firebaseAuthRepository.cadastra(usuario)

    fun estaLogado() : Boolean = firebaseAuthRepository.estaLogado()

    fun desloga() = firebaseAuthRepository.desloga()

    fun naoEstaLogado(): Boolean = !estaLogado()

    fun redefineSenha(email: String): LiveData<Boolean> = firebaseAuthRepository.redefineSenha(email)

    fun geraToken(): Boolean = firebaseAuthRepository.geraToken()

}

