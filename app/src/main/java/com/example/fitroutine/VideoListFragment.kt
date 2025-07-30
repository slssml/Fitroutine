package com.example.fitroutine

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


// 유튜브 영상 리스트 보여주는 Fragment
class VideoListFragment : Fragment() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter : VideoAdapter

    companion object {
        private const val ARG_CATEGORY = "category"

        // category를 인자로 받아 VideoListFragment 인스턴스를 생성
        fun newInstance(category: String): VideoListFragment {
            val fragment = VideoListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    // XML 레이아웃과 연결
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_video_list.xml 연결
        return inflater.inflate(R.layout.fragment_video_list, container, false)
    }

    // View 생성 완료 후 RecyclerView 설정 및 DB 연결
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView_video)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val category = arguments?.getString("category") ?: return
        view.findViewById<TextView>(R.id.videoListTitle).text = category

        val repository = VideoRepository(requireContext())

        // 기본 영상 삽입(추후 삭제 예정)
        if (repository.getVideosByCategory("팔").isEmpty()) {
            repository.insert(VideoItem(title = "운동 전 최고의 스트레칭! 10분만 따라해도 운동효과 대박!", youtubeUrl = "https://youtu.be/yyjOhsNEqtE", category = "스트레칭"))
            repository.insert(VideoItem(title = "힙으뜸 기초체력 홈트 15분루틴 (ft.땀폭발 전신순환운동, 코어운동)", youtubeUrl = "https://youtu.be/rSBOuArsz1k", category = "전신"))
            repository.insert(VideoItem(title = "\uD83D\uDD25출렁이는 팔뚝살\uD83D\uDD25빨리 빼려면 1달만 이 루틴하세요. (팔뚝살빼는운동/팔뚝살 빨리 빼는법/팔뚝살 완전 제거 운동)", youtubeUrl = "https://youtu.be/T-bVqdhqW2U", category = "팔"))
            repository.insert(VideoItem(title = "생리 중 그날의 우아한 전신유산소 발레핏 / Ballet Barre Workout During Period", youtubeUrl = "https://youtu.be/9eB5QYahyTM", category = "유산소"))
            repository.insert(VideoItem(title = "♦\uFE0F허벅지 안쪽살♦\uFE0F이 쏙 빠지는 7일 홈트레이닝 루틴!", youtubeUrl = "https://youtu.be/fABFQobOIKo", category = "허벅지"))
        }

        val videoList = repository.getVideosByCategory(category)

        if (videoList.isEmpty()) {  // 카테고리 내 영상이 없는 경우
            Toast.makeText(context, "$category 영상이 없습니다.", Toast.LENGTH_SHORT).show()
        }

        adapter = VideoAdapter(videoList)
        recyclerView.adapter = adapter
    }

    fun refreshList() {
        val category = arguments?.getString("category") ?: return
        val repository = VideoRepository(requireContext())
        val newVideoList = repository.getVideosByCategory(category)

        if (::adapter.isInitialized) {
            adapter.updateData(newVideoList)
        }
    }
}