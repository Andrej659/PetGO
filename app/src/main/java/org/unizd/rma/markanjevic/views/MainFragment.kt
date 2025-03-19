package org.unizd.rma.markanjevic.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.unizd.rma.markanjevic.R


class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val button = view.findViewById<Button>(R.id.buttonOpenSelection)
        button.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, SelectionFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
}


