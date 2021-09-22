package br.com.antonio.distribuidoradoguila.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.antonio.distribuidoradoguila.model.Configuracao
import br.com.antonio.distribuidoradoguila.repository.ConfiguracaoRepository
import br.com.antonio.distribuidoradoguila.repository.Resource

class ConfiguracaoViewModel(
    private val repository: ConfiguracaoRepository
) : ViewModel() {

    fun busca(): LiveData<List<Configuracao>> = repository.busca()

    fun salva(configuracao: Configuracao): LiveData<Resource<Boolean>> = repository.salva(configuracao)

}