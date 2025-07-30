package com.example.fitroutine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchResultFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VideoAdapter
    private lateinit var query: String
    private lateinit var results: List<VideoItem>

    companion object {
        fun newInstance(query: String, results: List<VideoItem>): SearchResultFragment {
            val fragment = SearchResultFragment()
            fragment.query = query
            fragment.results = results
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_list, container, false)
    }

    // 영상 탭 검색 결과 출력
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.videoListTitle).text = "\"$query\" 검색 결과"

        recyclerView = view.findViewById(R.id.recyclerView_video)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (results.isEmpty()) {
            Toast.makeText(requireContext(), "\"$query\"에 대한 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        adapter = VideoAdapter(results)
        recyclerView.adapter = adapter
    }
}
