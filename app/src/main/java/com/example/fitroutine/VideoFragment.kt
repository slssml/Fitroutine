package com.example.fitroutine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

// 영상 화면 프래그먼트
class VideoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 운동 부위 버튼 클릭 → VideoListFragment로 이동
        val buttons = listOf(
            R.id.ShoulderButton,
            R.id.BackButton,
            R.id.HipButton,
            R.id.ArmButton,
            R.id.ThighButton,
            R.id.Full_BodyButton,
            R.id.CardioButton,
            R.id.StretchingButton
        )

        buttons.forEach { id ->
            view.findViewById<Button>(id).setOnClickListener { buttonView ->
                val category = (buttonView as Button).text.toString()
                val fragment = VideoListFragment.newInstance(category)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // ➕ 버튼 클릭 시 영상 추가 다이얼로그
        val addButton = view.findViewById<View>(R.id.addButton)
        addButton.setOnClickListener {
            showAddVideoDialog()
        }

        val searchButton = view.findViewById<View>(R.id.serchButton)
        searchButton.setOnClickListener {
            showSearchDialog()
        }
    }

    // 영상 추가 다이얼로그
    private fun showAddVideoDialog() {
        val context = requireContext()
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("영상 추가")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val titleInput = EditText(context).apply {
            hint = "제목"
        }

        val urlInput = EditText(context).apply {
            hint = "YouTube URL"
        }

        val categorySpinner = Spinner(context)
        val categories = listOf("어깨", "등", "엉덩이", "팔", "허벅지", "전신", "유산소", "스트레칭")
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = adapter

        layout.addView(titleInput)
        layout.addView(urlInput)
        layout.addView(categorySpinner)
        builder.setView(layout)

        builder.setPositiveButton("추가") { _, _ ->
            val title = titleInput.text.toString()
            val url = urlInput.text.toString()
            val category = categorySpinner.selectedItem.toString()

            if (title.isNotBlank() && url.isNotBlank()) {
                val video = VideoItem(title = title, youtubeUrl = url, category = category)
                VideoRepository(context).insert(video)
                Toast.makeText(context, "영상이 추가되었습니다.", Toast.LENGTH_SHORT).show()

                // 현재 Fragment가 VideoListFragment라면 새로고침 시도
                val currentFragment = parentFragmentManager.findFragmentById(R.id.main_frame)
                if (currentFragment is VideoListFragment) {
                    currentFragment.refreshList()
                }

            } else {
                Toast.makeText(context, "제목과 URL을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }

    // 영상 검색 다이얼로그
    private fun showSearchDialog() {
        val context = requireContext()
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("영상 검색")

        val input = EditText(context).apply {
            hint = "제목으로 검색"
        }
        builder.setView(input)

        builder.setPositiveButton("검색") { _, _ ->
            val query = input.text.toString()
            if (query.isNotBlank()) {
                val repository = VideoRepository(context)
                val results = repository.searchVideosByTitle(query)

                // 검색 결과 프래그먼트로 전환
                val fragment = SearchResultFragment.newInstance(query, results)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }
}
