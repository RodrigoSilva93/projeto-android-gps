package br.edu.utfpr.pontosturisticos.ui

import br.edu.utfpr.pontosturisticos.adapter.PontoTuristicoAdapter
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ListView
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton

class ListaActivity : AppCompatActivity() {
    private lateinit var lista: ListView
    private lateinit var svPesquisa: SearchView

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
        svPesquisa = findViewById(R.id.svPesquisa)

        svPesquisa.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchLista(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchLista(newText ?: "")
                return true
            }
        })
        atualizarLista()
    }

    private fun atualizarLista() {
        val pontos = DatabaseSingleton.getInstance(this).getAppDatabase().pontoTuristicoDao().getAll()
        val adapter = PontoTuristicoAdapter(this, pontos){
            atualizarLista()
        }
        lista.adapter = adapter
    }

    fun searchLista(name: String) {
        val pontos = DatabaseSingleton.getInstance(this).getAppDatabase().pontoTuristicoDao().getByNameLike(name)
        val adapter = PontoTuristicoAdapter(this, pontos){
            atualizarLista()
        }
        lista.adapter = adapter
    }
}