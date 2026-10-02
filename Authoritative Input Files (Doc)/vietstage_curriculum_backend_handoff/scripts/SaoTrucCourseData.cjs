const preload = (x) => null;



const INSTRUMENT_ID = "sao_truc"
const LESSON_LIST_SCENE = "res://scenes/LessonSaoTrucList.tscn"
const LessonListScript = preload("res://scripts/LessonSaoTrucList.gd")

// Ba chặng hiển thị mới. Các giá trị là level cũ trong dữ liệu bài học; giữ nguyên
// ID bài học để tiến độ và sao đã lưu của học viên không bị mất.
const LEVEL_GROUPS = {
	1: [1, 2],
	2: [3, 4],
	3: [5, 6]
}

const LEVEL_LESSON_COUNTS = {
	1: 8,
	2: 11,
	3: 15
}

const CARD_STEP_IDS = {
	"basic": ["sao_truc_level1_1_video", "Node2", "Node3", "Node4", "Node5", "Node6", "Node7", "Node8"],
	"intermediate": ["sao_truc_level3_1", "sao_truc_level3_2", "sao_truc_level3_3", "sao_truc_level3_4", "sao_truc_level3_5", "sao_truc_level3_6", "sao_truc_level4_1", "sao_truc_level4_2", "sao_truc_level4_3", "sao_truc_level4_4", "sao_truc_level4_5"],
	"advanced": ["sao_truc_level5_1", "sao_truc_level5_2", "sao_truc_level5_3", "sao_truc_level5_4", "sao_truc_level5_5", "sao_truc_level5_6", "sao_truc_level5_7", "Node35", "Node36", "Node37", "Node38", "Node39", "Node40", "Node41", "Node42"]
}

const INTRO_LESSON_ID = "sao_truc_level1_1_video"
const INTRO_VIDEO_SEQUENCE = [
	"res://nvaore/intro1.ogv",
	"res://nvaore/intro2.ogv",
	"res://nvaore/intro3.ogv"
]

const ROADMAP = {
	"guide": "Lộ trình học tập Sáo Trúc",
	"basic_title": "LEVEL 1: NHẬP MÔN VÀ LÀM QUEN NHẠC CỤ",
	"basic_description": "Khẩu hình, hơi thở, tư thế cầm sáo và 7 nốt nền tảng.",
	"intermediate_title": "LEVEL 2: LUYỆN CÁC BÀI CƠ BẢN",
	"intermediate_description": "Ghép câu, chuyển ngón, giữ nhịp và hoàn thiện Khúc Nhạc Vui, Inh Lả Ơi.",
	"advanced_title": "LEVEL 3: LUYỆN CÁC BÀI NÂNG CAO",
	"advanced_description": "Luyện bài dài, kiểm soát hơi và biểu diễn Futari no Kimochi, Gặp Mẹ Trong Mơ."
}



module.exports = { preload, INSTRUMENT_ID, LESSON_LIST_SCENE, LessonListScript, LEVEL_GROUPS, LEVEL_LESSON_COUNTS, CARD_STEP_IDS, INTRO_LESSON_ID, INTRO_VIDEO_SEQUENCE, ROADMAP };
