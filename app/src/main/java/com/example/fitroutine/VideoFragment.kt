package com.example.fitroutine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class VideoFragment : Fragment() {

    // XML 레이아웃과 연결
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 운동 부위 버튼 ID 리스트
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

        // 각 버튼에 공통 클릭 이벤트 등록
        buttons.forEach { id ->
            view.findViewById<Button>(id).setOnClickListener { buttonView ->
                val category = (buttonView as Button).text.toString() // 예: "어깨", "팔" 등

                // VideoListFragment 인스턴스를 category와 함께 생성
                val fragment = VideoListFragment.newInstance(category)

                // Fragment 전환
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
