package com.example.visit_jeju_app.community.model

class CommunityData {
    var docId: String? = null
    var title: String? = null
    var content: String? = null
    // 파이어베이스에 저장된 timestamp형의 데이터를 불러와서
    // activity_comm_read.xml에 최신순으로 나타나도록하는 관련코드
    // timestamp형이 아닌 string이면서 "yyyy-MM-dd HH:mm"포맷으로 파이어베이스 저장 및 조회 관련 코드
    var date: String? = null

    // 댓글 변수 지정
    var comment: String? = null

    // 카테고리를 파이어베이스에 저장하는 코드
    var category: String? = null // 추가된 라인

        // 파이어베이스에 저장된 카테고리 데이터를 디테일 뷰에 불러오는 관련 코드
        private set // 수정된 부분

    fun setCategory(category: String) {
        this.category = category
    }


}
