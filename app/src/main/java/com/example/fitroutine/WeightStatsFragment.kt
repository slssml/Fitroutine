package com.example.fitroutine

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeightStatsFragment : Fragment() {

    // 라인차트 뷰 변수 선언
    private lateinit var lineChart: LineChart
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_weight_stats, container, false)
        lineChart = view.findViewById(R.id.weightLineChart)
        loadWeightData()
        return view
    }

    // Firestore에서 체중 데이터 불러오기
    private fun loadWeightData() {
        val uid = auth.currentUser?.uid ?: return
        val recordsRef = db.collection("users").document(uid).collection("dailyRecords")

        recordsRef.get().addOnSuccessListener { snapshot ->
            val weightData = mutableListOf<Pair<String, Float>>() // 날짜와 체중 저장용 리스트

            // 각 문서에서 날짜와 체중 추출
            for (doc in snapshot.documents) {
                val date = doc.id
                val weight = doc.getDouble("weight")?.toFloat()
                if (weight != null) {
                    weightData.add(Pair(date, weight))
                }
            }

            // 날짜 순으로 정렬
            val sortedData = weightData.sortedBy { it.first }
            val entries = mutableListOf<Entry>()  // 그래프에 그릴 데이터 리스트
            val colors = mutableListOf<Int>()     // 각 점 색상 리스트
            val labels = mutableListOf<String>()  // X축 라벨 리스트

            // 정렬된 데이터 반복 처리
            for (i in sortedData.indices) {
                val (date, weight) = sortedData[i]
                entries.add(Entry(i.toFloat(), weight))

                // 이전 값과 비교해 증감 계산
                val diff = if (i == 0) 0f else weight - sortedData[i - 1].second
                val diffStr = when {
                    i == 0 -> "" // 첫 데이터는 변화량 없음
                    diff > 0 -> String.format(" (+%.1fkg)", diff) // 증가
                    diff < 0 -> String.format(" (%.1fkg)", diff)  // 감소
                    else -> " (±0.0kg)" // 변화 없음
                }

                labels.add(date.substring(5) + diffStr) // 라벨에 날짜와 변화량 추가

                // 변화에 따른 점 색상 지정
                val color = when {
                    i == 0 -> ContextCompat.getColor(requireContext(), R.color.gray_light)
                    diff > 0 -> ContextCompat.getColor(requireContext(), R.color.red_accent)
                    diff < 0 -> ContextCompat.getColor(requireContext(), R.color.green_accent)
                    else -> ContextCompat.getColor(requireContext(), R.color.gray_light)
                }
                colors.add(color)
            }

            drawGraph(entries, labels, colors)
        }
    }

    // 그래프 그리기 함수
    private fun drawGraph(entries: List<Entry>, labels: List<String>, pointColors: List<Int>) {
        val dataSet = LineDataSet(entries, "체중 (kg)")
        dataSet.valueTextSize = 10f
        dataSet.circleRadius = 5f
        dataSet.setDrawValues(true)

        dataSet.color = ContextCompat.getColor(requireContext(), R.color.purple_500)
        dataSet.setCircleColors(pointColors)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        // X축 라벨 포맷터 설정 (날짜 + 변화량 표시)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in labels.indices) labels[index] else ""
            }
        }

        lineChart.axisRight.isEnabled = false  // Y축 비활성화
        lineChart.description.text = "날짜별 체중 변화" // 차트 설명
        lineChart.invalidate() // 차트 갱신
    }
}


