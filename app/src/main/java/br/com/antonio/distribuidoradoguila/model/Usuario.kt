package br.com.antonio.distribuidoradoguila.model


data class Usuario(
    val nome: String = "",
    val email: String = "",
    val endereco: Endereco = Endereco(),
    val telefone: String = "",
    val administrador: Boolean = false,
    val administradorSecundario: Boolean = false,
    val desenvolvedor: Boolean = false,
    val token: String =  ""
) {

}