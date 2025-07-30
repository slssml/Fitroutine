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
            repository.insert(VideoItem(title = "🔥 앉아서 6분🔥 초간단 팔뚝살, 승모근, 직각 어깨 운동!", youtubeUrl = "https://youtu.be/qquOD3zklOk", category = "어깨"))
            repository.insert(VideoItem(title = "80만명이 효과를 본 승모근 없애는 운동, 일자어깨 직각어깨 만들기✨", youtubeUrl = "https://youtu.be/Ne2e0TwG5ww", category = "어깨"))
            repository.insert(VideoItem(title = "숄더프레스머신 기본 총정리편/헬스장 기구운동의 정석/제대로 알려드립니다!", youtubeUrl = "https://youtu.be/iatpz9JDb30", category = "어깨"))
            repository.insert(VideoItem(title = "어깨운동의 정석 ㅡ덤벨 프레스(김명섭과 함께 배워보는 어깨운동의 기본-덤벨프레스)", youtubeUrl = "https://youtu.be/_geC1KPo2og", category = "어깨"))
            repository.insert(VideoItem(title = "【숄더프레스】 어깨운동, 하나만 해야한다면 헬스기구 이거 쓰세요!!", youtubeUrl = "https://youtu.be/rHk4j2WRAE4", category = "어깨"))

            repository.insert(VideoItem(title = "1번만 따라해도 등 라인이 달라지는 '역대급 등살빼는운동' (급속버전🔥)", youtubeUrl = "https://youtu.be/Iuuj28MImK0", category = "등"))
            repository.insert(VideoItem(title = "등 운동 가보자고!💪", youtubeUrl = "https://youtu.be/OJevd3OyNNo", category = "등"))
            repository.insert(VideoItem(title = "당기는 원리를 모르면 등 근육을 만들 수 없습니다. (feat.랫풀다운)", youtubeUrl = "https://youtu.be/r9eZnVia2Iw", category = "등"))
            repository.insert(VideoItem(title = "데드리프트의 정석--김명섭이 제안하는 가장 효과적인 데드리프트방법!", youtubeUrl = "https://youtu.be/S8DilF8vmhE", category = "등"))
            repository.insert(VideoItem(title = "【광배】 꼭! 해야하는 ‘등근육 운동 5가지’ 이것만큼은 꼭 하세요!!", youtubeUrl = "https://youtu.be/3vKhZRSNm0Y", category = "등"))

            repository.insert(VideoItem(title = "'납작한 엉덩이?' 이 순서를 몰라서 그런거에요", youtubeUrl = "https://youtu.be/uHAroYEw2cg", category = "엉덩이"))
            repository.insert(VideoItem(title = "【엉덩이】 꼭 해야하는 엉덩이운동 5가지! 이것만큼은 꼭 하세요!!", youtubeUrl = "https://youtu.be/Bh2Ea8M58lw", category = "엉덩이"))
            repository.insert(VideoItem(title = "엉덩이 사이즈 키우는데 최고의 운동, 힙쓰러스트(대둔근+중둔근 자극법)", youtubeUrl = "https://youtu.be/jy2cdSBd7bQ", category = "엉덩이"))
            repository.insert(VideoItem(title = "한번만해도 볼륨 살아나는 둔근운동, 애플힙, 힙딥, 중둔근, 피치힙", youtubeUrl = "https://youtu.be/xua8mUT0IFQ", category = "엉덩이"))
            repository.insert(VideoItem(title = "힙업 + 탄력업 - 무릎에 무리 없이 딱 10분 힙업운동", youtubeUrl = "https://youtu.be/zyP1h5Qkndw", category = "엉덩이"))

            repository.insert(VideoItem(title = "팔뚝살 정리하고 싶은 분들! 삼두 운동 제대로 알려드릴게요", youtubeUrl = "https://youtu.be/kGOzHWVW8Zw", category = "팔"))
            repository.insert(VideoItem(title = "[오운모] 팔뚝 살 불태워버렷!!!🔥삼두 자극 200% 루틴!", youtubeUrl = "https://youtu.be/i7zX4E3SbJU", category = "팔"))
            repository.insert(VideoItem(title = "💎팔뚝살💎 7분 만에 탈탈 털어버리는 덤벨 운동!", youtubeUrl = "https://youtu.be/SmFjy_sVdjM", category = "팔"))
            repository.insert(VideoItem(title = "하루10분! 팔 근육을 키우는 덤벨운동 (이두운동) | 10 Min Biceps Workout With Dumbbells (Arm Workout)", youtubeUrl = "https://youtu.be/BfZw0qMoa1A", category = "팔"))

            repository.insert(VideoItem(title = "앞벅지 볼록, 뒷벅지 셀룰라이트, 허벅지 안쪽살 모조리 불태우고🔥 [여리탄탄 일자 허벅지] 되는 7일 루틴", youtubeUrl = "https://youtu.be/dpBYYEhdofI", category = "허벅지"))
            repository.insert(VideoItem(title = "5분! 누워서 허벅지살 돌려깎기 [하체 마라맛🔥]", youtubeUrl = "https://youtu.be/M8Rmq9_998g", category = "허벅지"))
            repository.insert(VideoItem(title = "단 3분만에 허벅지 안쪽살 정리해드립니다", youtubeUrl = "https://youtu.be/3IZOf7KVJDw", category = "허벅지"))
            repository.insert(VideoItem(title = "🦵하루 딱 5분, 이 운동을 매일 따라해 보세요! (하비탈출/5분 운동습관)ㅣ다노티비", youtubeUrl = "https://youtu.be/OKyuFF3uh5o", category = "허벅지"))
            repository.insert(VideoItem(title = "이거하면 ‘톡 튀어나온 허벅지 안쪽 맨 윗살’이 🔥무조건🔥 빠져요! (+허벅지 사이가 일자로 똑 떨어지는 7일 루틴)", youtubeUrl = "https://youtu.be/3neRUAR5r1c", category = "허벅지"))

            repository.insert(VideoItem(title = "최고의 전신 근력운동 BEST5", youtubeUrl = "https://youtu.be/Iaa8YNDRbhg", category = "전신"))
            repository.insert(VideoItem(title = "NO 층간소음 - 고민없이 하나로 끝내는 전신운동 근력 유산소 - 운동 못한 날 죄책감 씻어줄 30분 홈트", youtubeUrl = "https://youtu.be/4kZHHPH6heY", category = "전신"))
            repository.insert(VideoItem(title = "[7분] 논스탑 고강도 전신, 기적의 체지방감량 운동! 통통살 컴온!", youtubeUrl = "https://youtu.be/R6GHtRJLKQg", category = "전신"))
            repository.insert(VideoItem(title = "하루 한 번! 꼭 해야하는 10분 기본 전신근력 운동 홈트 (층간소음🙅🏻‍♀️)", youtubeUrl = "https://youtu.be/aKzE3NNFEi4", category = "전신"))

            repository.insert(VideoItem(title = "체지방 길게 태워보자 - 본운동만 40분 서서하는 유산소 운동 홈트", youtubeUrl = "https://youtu.be/sTX0C08SYBM", category = "유산소"))
            repository.insert(VideoItem(title = "🔥출렁이는 지방🔥단기간에 빼고 싶으면 이 유산소운동 1달만 하세요. (유산소 다이어트/전신 유산소 타바타/칼로리 폭발 운동)", youtubeUrl = "https://youtu.be/sucNosF93w8", category = "유산소"))
            repository.insert(VideoItem(title = "매일 아침 꼭 해야하는 12분 유산소 운동 홈트👑 (2025)", youtubeUrl = "https://youtu.be/nmlWSMNjCQ8", category = "유산소"))
            repository.insert(VideoItem(title = "전신 다이어트 최고의 운동 [칼소폭 마라맛🔥]", youtubeUrl = "https://youtu.be/F-Jd4kI6rdM", category = "유산소"))

            repository.insert(VideoItem(title = "목 결림, 어깨 뭉침을 풀어주는 스트레칭 (with 3분 마사지)", youtubeUrl = "https://youtu.be/FMOISIlhLEY", category = "스트레칭"))
            repository.insert(VideoItem(title = "[ENG] 심으뜸 매일 아침 10분 스트레칭ㅣ2023 리뉴얼", youtubeUrl = "https://youtu.be/50WCSpZtdmA", category = "스트레칭"))
            repository.insert(VideoItem(title = "운동 전 필수! 8분으로 빠르게 끝내는 초간단 전신 스트레칭ㅣ다노티비", youtubeUrl = "https://youtu.be/U6nnxml9GRs", category = "스트레칭"))
            repository.insert(VideoItem(title = "굽은어깨교정, 어깨비대칭, 오십견예방, 어깨결림, 상체 스트레칭! (일주일만 해보세요)", youtubeUrl = "https://youtu.be/kvE_1FIJusM", category = "스트레칭"))
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