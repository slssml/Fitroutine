package com.example.fitroutine

import android.app.AlertDialog
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

    private lateinit var titleTextView: TextView     // 화면 상단 텍스트
    private lateinit var recyclerView: RecyclerView  // 영상 리사이클뷰
    private lateinit var adapter: VideoAdapter       // 어댑터
    private var videoList: List<VideoItem> = emptyList()  // 현재 루틴에 포함된 영상 목록

    companion object {
        private const val ARG_VIDEOS = "videos"  // 번들 키

        // 외부에서 이 프래그먼트 생성할 때 영상 목록 함께 전달
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

        // 어댑터 초기화 (영상 추가 버튼 숨김, 삭제 기능)
        adapter = VideoAdapter(requireContext(), videoList, hideAddButton = true) { videoToDelete ->
            showDeleteConfirmDialog(videoToDelete)
        }
        recyclerView.adapter = adapter
    }

    // 영상 삭제 확인 다이얼로그
    private fun showDeleteConfirmDialog(video: VideoItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("영상 삭제")
            .setMessage("이 영상을 루틴에서 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                videoList = videoList.filter { it != video }
                adapter.updateData(videoList)
            }
            .setNegativeButton("취소", null)
            .show()
    }

}

