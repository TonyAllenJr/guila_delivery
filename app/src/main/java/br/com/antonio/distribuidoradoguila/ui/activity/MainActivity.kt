package br.com.antonio.distribuidoradoguila.ui.activity

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.viewmodel.EstadoAppViewModel
import br.com.antonio.distribuidoradoguila.ui.viewmodel.UsuarioViewModel
import kotlinx.android.synthetic.main.main_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val controlador by lazy { findNavController(R.id.main_activity_nav_host) }
    private val viewModel: EstadoAppViewModel by viewModel()
    private val usuarioViewModel: UsuarioViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        controlador.addOnDestinationChangedListener {
                _,
                destination,
                _,
            ->

            title = destination.label

            viewModel.componentes.observe(this, {
                it?.let { temComponentes ->
                    if (temComponentes.appBar) {
                        supportActionBar?.show()
                    } else {
                        supportActionBar?.hide()
                    }

                    if (temComponentes.bottomNavigation) {
                        main_activity_bottom_navigation.visibility = VISIBLE
                    } else {
                        main_activity_bottom_navigation.visibility = GONE
                    }


                }
            })
        }
        main_activity_bottom_navigation.setupWithNavController(controlador)
    }

    override fun onResume() {
        super.onResume()
        usuarioViewModel.geraToken()
    }


}

