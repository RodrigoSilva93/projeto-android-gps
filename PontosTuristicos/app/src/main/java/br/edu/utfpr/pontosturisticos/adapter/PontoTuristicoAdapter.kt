import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.ui.CadastrarActivity
import br.edu.utfpr.pontosturisticos.ui.ListaActivity
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton

class PontoTuristicoAdapter(context: Context,
                            pontos: List<PontoTuristico>,
                            private val onDataChanged: () -> Unit ) :


ArrayAdapter<PontoTuristico>(context, 0, pontos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val ponto = getItem(position)

        val tvNome = view.findViewById<TextView>(R.id.tvNome)
        val tvID = view.findViewById<TextView>(R.id.tvID)
        val btnEditar = view.findViewById<Button>(R.id.btnEditar)
        val btnExcluir = view.findViewById<Button>(R.id.btnExcluir)

        tvNome.text = ponto?.nome
        tvID.text = ponto?.uid.toString()

        btnEditar.setOnClickListener {
            if (context is ListaActivity) {
                (context as ListaActivity).abrirEditarPonto(ponto?.uid ?: 0)
            }
        }

        btnExcluir.setOnClickListener {
            val db = DatabaseSingleton.getInstance(context).getAppDatabase()
            db.pontoTuristicoDao().delete(ponto!!)
            onDataChanged()
            Toast.makeText(context, "Ponto turístico excluído", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}