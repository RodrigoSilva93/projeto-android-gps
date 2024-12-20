package br.edu.utfpr.pontosturisticos.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import br.edu.utfpr.pontosturisticos.utils.singleton.DatabaseSingleton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class DetalhesPontoFragment : BottomSheetDialogFragment() {

    private lateinit var tvNome: TextView
    private lateinit var tvDescricao: TextView
    private lateinit var tvEndereco: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var ivDetailImagem: ImageView
    private lateinit var btnEditar: Button
    private lateinit var btnExcluir: Button
    private lateinit var onDataChanged: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalhes_ponto, container, false)

        tvNome = view.findViewById(R.id.tvNome)
        tvDescricao = view.findViewById(R.id.tvDescricao)
        tvEndereco = view.findViewById(R.id.tvEndereco)
        tvLatitude = view.findViewById(R.id.tvLatitude)
        tvLongitude = view.findViewById(R.id.tvLongitude)
        ivDetailImagem = view.findViewById(R.id.ivDetailImagem)
        btnEditar = view.findViewById(R.id.btnEditar)
        btnExcluir = view.findViewById(R.id.btnExcluir)

        arguments?.let {
            tvNome.text = it.getString("NOME")
            tvDescricao.text = it.getString("DESCRICAO")
            tvEndereco.text = it.getString("ENDERECO")
            tvLatitude.text = it.getString("LATITUDE")
            tvLongitude.text = it.getString("LONGITUDE")

            val imagemCaminho = it.getString("IMAGEM")
            println(imagemCaminho)

            if (imagemCaminho != null) {
                if (imagemCaminho.isNotEmpty()) {
                    val file = File(imagemCaminho)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        println(bitmap)

                        if (bitmap != null)
                            ivDetailImagem.setImageBitmap(bitmap)
                        else
                            Toast.makeText(context, "Erro ao carregar a imagem.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Imagem não encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Nenhuma imagem associada a este ponto.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnEditar.setOnClickListener {
            dismiss()
            val intent = Intent(context, CadastrarActivity::class.java)
            intent.putExtra("ID_PONTO", arguments?.getInt("ID"))
            startActivity(intent)
        }

        btnExcluir.setOnClickListener {
            val db = DatabaseSingleton.getInstance(requireContext()).getAppDatabase()
            val pontoTuristico = PontoTuristico(
                uid = arguments?.getInt("ID")!!,
                nome = tvNome.text.toString(),
                descricao = tvDescricao.text.toString(),
                endereco = tvEndereco.text.toString(),
                latitude = tvLatitude.text.toString(),
                longitude = tvLongitude.text.toString()
            )
            db.pontoTuristicoDao().delete(pontoTuristico)
            dismiss()  // Fecha o fragmento após a exclusão
            Toast.makeText(context, "Ponto turístico excluído", Toast.LENGTH_SHORT).show()
            onDataChanged()
        }

        return view
    }

    companion object {
        fun newInstance(ponto: PontoTuristico, onDataChanged: () -> Unit ): DetalhesPontoFragment {
            val fragment = DetalhesPontoFragment()
            val args = Bundle().apply {
                putInt("ID", ponto.uid)
                putString("NOME", ponto.nome)
                putString("DESCRICAO", ponto.descricao)
                putString("ENDERECO", ponto.endereco)
                putString("LATITUDE", ponto.latitude)
                putString("LONGITUDE", ponto.longitude)
                putString("IMAGEM", ponto.imagem)
            }
            fragment.arguments = args
            fragment.onDataChanged = onDataChanged
            return fragment
        }
    }
}
