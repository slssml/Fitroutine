package com.example.fitroutine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 루틴에 추가된 영상 목록을 보여주는 프래그먼트
class RoutineVideoListFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VideoAdapter
    private var videoList: List<VideoItem> = emptyList()

    companion object {
        private const val ARG_VIDEOS = "videos"

        fun newInstance(videos: ArrayList<VideoItem>): RoutineVideoListFragment {
            val fragment = RoutineVideoListFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList(ARG_VIDEOS, videos) // 영상 리스트를 번들에 담음
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoList = arguments?.getParcelableArrayList(ARG_VIDEOS) ?: emptyList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleTextView = view.findViewById(R.id.videoListTitle)
        titleTextView.text = "루틴 영상"
        recyclerView = view.findViewById(R.id.recyclerView_video)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = VideoAdapter(requireContext(), videoList, hideAddButton = true)
        recyclerView.adapter = adapter
    }
}

