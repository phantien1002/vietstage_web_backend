const preload = (x) => null;



const INSTRUMENT_ID = "dan_tranh"
const LESSON_LIST_SCENE = "res://scenes/LessonDanTranhList.tscn"
const LessonListScript = preload("res://scripts/LessonDanTranhList.gd")

const ROADMAP = {
	1: {
		"title": "LEVEL 1: NHẬP MÔN & LÀM QUEN",
		"description": "Làm quen với đàn tranh, đọc nhạc cơ bản và luyện các ngón gảy đầu tiên."
	},
	2: {
		"title": "LEVEL 2: KỸ THUẬT DIỄN TẤU",
		"description": "Tìm hiểu về các kỹ thuật Á, nhấn, song thanh và rung dây."
	},
	7: {
		"title": "LEVEL 3: KỸ THUẬT NÂNG CAO MỞ RỘNG",
		"description": "Mở rộng khả năng diễn tấu với kỹ thuật vê và hợp âm ba âm cơ bản."
	}
}



module.exports = { preload, INSTRUMENT_ID, LESSON_LIST_SCENE, LessonListScript, ROADMAP };
