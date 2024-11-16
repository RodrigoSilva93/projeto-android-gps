package br.edu.utfpr.pontosturisticos.ui

import PontoTuristicoAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton

class ListaActivity : AppCompatActivity() {
    private lateinit var lista: ListView

    private val editPontoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                atualizarLista()
            }
        }

    fun abrirEditarPonto(pontoId: Int) {
        val intent = Intent(this, CadastrarActivity::class.java)
        intent.putExtra("ID_PONTO", pontoId)
        editPontoLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        lista = findViewById(R.id.lvLista)
        atualizarLista()
    }

    private fun atualizarLista() {
        val pontos = DatabaseSingleton.getInstance(this).getAppDatabase().pontoTuristicoDao().getAll()
        val adapter = PontoTuristicoAdapter(this, pontos){
            atualizarLista()
        }
        lista.adapter = adapter
    }
}