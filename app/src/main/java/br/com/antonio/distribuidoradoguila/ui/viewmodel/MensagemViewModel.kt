package br.com.antonio.distribuidoradoguila.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import br.com.antonio.distribuidoradoguila.model.Mensagem
import br.com.antonio.distribuidoradoguila.repository.MensagemRepository
import br.com.antonio.distribuidoradoguila.repository.Resource

class MensagemViewModel(private val repository: MensagemRepository,
) : ViewModel() {

    fun salva(mensagem: Mensagem): LiveData<Resource<Boolean>> = repository.salva(mensagem)

    fun buscaTodas(): LiveData<List<Mensagem>> = repository.buscaTodas()
}