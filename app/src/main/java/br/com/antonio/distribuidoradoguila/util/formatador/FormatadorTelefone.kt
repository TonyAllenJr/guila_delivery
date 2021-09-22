package br.com.antonio.distribuidoradoguila.util.formatador

class FormatadorTelefone {

    fun formata(telefone: String) : String {
        return telefone
            .replace(regex = Regex("([0-9]{2})([0-9]{4,5})([0-9]{4})"), replacement = "($1) $2-$3")
    }

    fun removeCaracter(telefone: String) : String {
        return telefone
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")
            .replace("-", "")
    }


}