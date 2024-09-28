package com.example.mobilelearningapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelearningapp.MateriItemsAdapter
import com.example.mobilelearningapp.R
import com.example.mobilelearningapp.activities.MateriListActivity
import com.example.mobilelearningapp.databinding.FragmentMateriListBinding
import com.example.mobilelearningapp.models.Materi

class MateriListFragment : Fragment() {
    private var mapel: String? = null
    private var materiList: ArrayList<Materi>? = null
    private var _binding: FragmentMateriListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MateriItemsAdapter

    companion object {
        fun newInstance(mapel: String, materiList: ArrayList<Materi>): MateriListFragment {
            return MateriListFragment().apply {
                arguments = Bundle().apply {
                    putString("mapel", mapel)
                    putParcelableArrayList("materiList", materiList)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mapel = it.getString("mapel")
            materiList = it.getParcelableArrayList("materiList")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMateriListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = MateriItemsAdapter(requireContext(), materiList!!).apply {
            setOnClickListener { model ->
                (activity as? MateriListActivity)?.materiDetails(model.id)
            }
            setOnEditClickListener { model ->
                (activity as? MateriListActivity)?.updateMateri(model)
            }
            setOnDeleteClickListener { model ->
                (activity as? MateriListActivity)?.showDeleteConfirmationDialog(model)
            }
        }

        binding.rvMateriList.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            this.adapter = this@MateriListFragment.adapter
        }

        updateUI()
    }


    private fun updateUI() {
        _binding?.let { binding ->
            if (materiList!!.isEmpty()) {
                binding.rvMateriList.visibility = View.GONE
                binding.tvNoMateriAvailable.visibility = View.VISIBLE
            } else {
                binding.rvMateriList.visibility = View.VISIBLE
                binding.tvNoMateriAvailable.visibility = View.GONE
            }
        }
    }

    fun updateMateriList(newList: ArrayList<Materi>) {
        materiList!!.clear()
        materiList!!.addAll(newList)
        adapter.updateMateriList(newList)
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}