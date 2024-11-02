package br.edu.utfpr.pontosturisticos.ui

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.database.AppDatabase
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton

class ListaActivity : AppCompatActivity() {
    private lateinit var lista: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        lista = findViewById(R.id.lvLista)
        val idsList = intent.getIntegerArrayListExtra("IDS_LIST")

        idsList?.let {
            val adapter = ArrayAdapter(this, R.layout.simple_list_item, idsList)
            lista.adapter = adapter
        }

        registerForContextMenu(lista)
        lista.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val id = lista.adapter.getItem(position) as Int
            editarPontoTuristico(id)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val id = lista.adapter.getItem(info.position) as Int

        return when (item.itemId) {
            R.id.menu_editar -> {
                editarPontoTuristico(id)
                true
            }
            R.id.menu_excluir -> {
                excluirPontoTuristico(id)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun editarPontoTuristico(id: Int) {
        val intent = Intent(this, CadastrarActivity::class.java).apply {
            putExtra("ID_PONTO", id)
            putExtra("ORIGIN", "menu")
        }
        startActivity(intent)
    }

    private fun excluirPontoTuristico(id: Int) {
        val db = DatabaseSingleton.getInstance(this).getAppDatabase()
        val pontoTuristico = db.pontoTuristicoDao().getById(id)
        db.pontoTuristicoDao().delete(pontoTuristico)

        Toast.makeText(this, "Ponto turístico removido.", Toast.LENGTH_SHORT).show()

        atualizarLista(db)
    }

    private fun atualizarLista(db: AppDatabase) {
        val ids = db.pontoTuristicoDao().getAllIds()
        val adapter = ArrayAdapter(this, R.layout.simple_list_item, ids)
        lista.adapter = adapter
    }
}