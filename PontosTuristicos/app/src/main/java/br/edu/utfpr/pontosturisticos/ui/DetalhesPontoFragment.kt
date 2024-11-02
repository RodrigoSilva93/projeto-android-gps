package br.edu.utfpr.pontosturisticos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import br.edu.utfpr.pontosturisticos.R
import br.edu.utfpr.pontosturisticos.entities.PontoTuristico
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DetalhesPontoFragment : BottomSheetDialogFragment() {
    private lateinit var tvNome: TextView
    private lateinit var tvDescricao: TextView
    private lateinit var tvEndereco: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView

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

        arguments?.let {
            tvNome.text = it.getString("NOME")
            tvDescricao.text = it.getString("DESCRICAO")
            tvEndereco.text = it.getString("ENDERECO")
            tvLatitude.text = it.getString("LATITUDE")
            tvLongitude.text = it.getString("LONGITUDE")
        }

        return view
    }

    companion object {
        fun newInstance(ponto: PontoTuristico): DetalhesPontoFragment {
            val fragment = DetalhesPontoFragment()
            val args = Bundle().apply {
                putString("NOME", ponto.nome)
                putString("DESCRICAO", ponto.descricao)
                putString("ENDERECO", ponto.endereco)
                putString("LATITUDE", ponto.latitude)
                putString("LONGITUDE", ponto.longitude)
            }
            fragment.arguments = args
            return fragment
        }
    }
}