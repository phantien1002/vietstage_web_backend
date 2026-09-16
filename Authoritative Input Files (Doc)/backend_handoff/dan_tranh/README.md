# Database bàn giao — giáo trình Đàn tranh hiện tại

## Phạm vi và cách dùng

Đây là bản thiết kế + seed **để backend review**, không phải migration cho database đang chạy.
Không kết nối/ghi database thật; không sửa vietstage_web_backend.
SQL dùng cú pháp **PostgreSQL** trong schema riêng `dan_tranh_review`.
Nếu backend dùng DB khác, chuyển kiểu JSONB/timestamptz/ON CONFLICT tương ứng.
Không chạy mù vào production. Backend phải ghép với bảng lessons/users/exercises/progress đang có,
không tạo một hệ thống sao song song.

- `01_schema_proposal.sql`: mô hình dữ liệu tham chiếu và khóa/ràng buộc.
- `02_seed_snapshot.sql`: 26 mục bài từ source; 28 bộ hội thoại, 166 bước;
  cấu hình runtime Nhấn/Rung/Vê/Á, 17 dây và 43 hằng số nhận diện.
- `source_snapshot.json`: bản JSON đầy đủ, giữ nguyên notes, durations, fingerings,
  cues, practiceConfig và các step; dùng cho import/test đối chiếu.
- Danh mục lấy từ `LessonDanTranhList.gd:LEVELS`; cấp đang hiển thị lấy từ
  `DanTranhCourseData.gd:ROADMAP`; hội thoại và kỹ thuật lấy từ `LessonDanTranh.gd`.

**Không dùng gtDanTranh.md thay cho runtime.** Snapshot không chứa toàn bộ logic GDScript,
ngân hàng Quiz/Minigame hoặc các bước/âm thanh tạo động trong hàm. Chưa đủ để bật API thay app.

## Danh mục thật và định danh

Lộ trình đang mở có **22 mục**: Cơ bản 11, Trung cấp 7, Nâng cao 4.
Cấp nội bộ 1 → BEGINNER, 2 → INTERMEDIATE, 7 → ADVANCED.
Level 3/4/5/6 còn trong source (4 mục) được lưu riêng, không tự chuyển thành bài Nâng cao.

| Cấp | Số hiển thị | Tên bài | Mã giữ nguyên |
|---|---|---|---|
| BEGINNER | 1 | Tìm hiểu nhạc cụ Đàn tranh | `dan_tranh_level_1_bai_1_video` |
| BEGINNER | 2 | Nhịp điệu cơ bản | `dan_tranh_level_1_bai_5_practice` |
| BEGINNER | 3 | Đọc bản nhạc cơ bản | `dan_tranh_level_1_bai_4_practice` |
| BEGINNER | 4.1 | Kỹ thuật gảy ngón 2 | `dan_tranh_level_1_bai_8_practice` |
| BEGINNER | 4.2 | Kỹ thuật gảy ngón 1 | `dan_tranh_level_1_bai_7_practice` |
| BEGINNER | 4.3 | Kỹ thuật gảy ngón 3 | `dan_tranh_level_1_bai_9_practice` |
| BEGINNER | 5 | Luyện gảy các nốt cơ bản – Phần 1 | `dan_tranh_level_1_bai_2_practice` |
| BEGINNER | 6 | Luyện gảy các nốt cơ bản – Phần 2 | `dan_tranh_level_1_bai_3_practice` |
| BEGINNER | 7 | Luyện bài Lý cây đa – Nửa đoạn đầu | `dan_tranh_level_2_bai_10_practice` |
| BEGINNER | 8 | Luyện bài Lý cây đa – Nửa đoạn cuối | `dan_tranh_level_2_bai_11_practice` |
| BEGINNER | 9 | Hoàn thiện bài Lý cây đa | `dan_tranh_level_2_bai_12_practice` |
| INTERMEDIATE | 10 | Kỹ thuật Á | `dan_tranh_level_7_bai_18_practice` |
| INTERMEDIATE | 11 | Kỹ thuật nhấn | `dan_tranh_level_7_bai_19_practice` |
| INTERMEDIATE | 12 | Kỹ thuật song thanh | `dan_tranh_level_7_bai_20_practice` |
| INTERMEDIATE | 13 | Kỹ thuật rung dây | `dan_tranh_level_7_bai_21_practice` |
| INTERMEDIATE | 14 | Luyện bài Sứ thanh hoa – Nửa đoạn đầu | `dan_tranh_level_2_bai_13_practice` |
| INTERMEDIATE | 15 | Luyện bài Sứ thanh hoa – Nửa đoạn cuối | `dan_tranh_level_2_bai_14_practice` |
| INTERMEDIATE | 16 | Hoàn thiện bài Sứ thanh hoa | `dan_tranh_level_2_bai_15_practice` |
| Ngoài lộ trình | 7 | Luyện ngón tốc độ cao – Mã Vũ | `dan_tranh_level_3_bai_7_practice` |
| Ngoài lộ trình | 9 | Kỹ thuật nhấn Rung tay trái | `dan_tranh_level_4_bai_9_practice` |
| Ngoài lộ trình | 12 | Boss Stage – Thử thách sinh tồn | `dan_tranh_level_5_bai_12_practice` |
| Ngoài lộ trình | 17 | Bài 5: Chuyển hợp âm | `dan_tranh_level_6_bai_17_practice` |
| ADVANCED | 17 | Kỹ thuật Vê | `dan_tranh_level_8_bai_30_practice` |
| ADVANCED | 18 | Hợp âm ba âm cơ bản | `dan_tranh_level_8_bai_31_practice` |
| ADVANCED | 18.1 | Hợp âm Đô trưởng | `dan_tranh_level_8_bai_32_practice` |
| ADVANCED | 18.2 | Hợp âm La thứ | `dan_tranh_level_8_bai_33_practice` |

`display_number` là chuỗi (4.1/4.2/4.3, 18.1/18.2), không dùng làm PK.
`number` là số cũ; `orderIndex` là vị trí trong cấp hiện tại.
Các mã có level_7/level_8 vẫn giữ nguyên dù bài chuyển cấp.
Mục video dùng video_id làm mã chính trong snapshot; các mục còn lại dùng practice_id
hoặc mã sinh theo quy tắc app. Đây là quyết định seed cần backend duyệt trước migration.

`lesson_legacy_key` giữ practice/video/quiz riêng. Mã video/practice sinh theo quy tắc
là **ứng viên mapping**, không chứng minh có video hay một activity đang hiển thị.
Xem type/video/video_path ở source_snapshot; không tạo activity/video trống.
Không cộng gộp sao của các khóa con vào bài mới một cách tự động.

## Các bảng backend cần có hoặc mở rộng

| Bảng / thực thể | Vai trò / trường quan trọng |
|---|---|
| lessons hiện có | lessonCode bất biến, instrumentId, skillLevelId, title, orderIndex, displayNumber; thêm visibility độc lập approval |
| lesson_legacy_key | map khóa tiến độ cũ theo loại practice/video/quiz; kiểm tra trùng trước import |
| lesson_revision | phiên bản bất biến của nội dung; attempt tham chiếu đúng phiên bản đã học |
| lesson_contents hiện có / lesson_activity | từng đoạn lời cô Mai và thứ tự; activity liên kết nói, nghe mẫu, chờ nốt, thực hành, Quiz/Minigame |
| exercise_config | mode + practiceConfig versioned + passThreshold + recognition profile version |
| lesson_completion hiện có | một kết quả tích lũy cho mỗi học viên/bài; bestScore, stars, completedAt |
| completion_attempt hiện có | clientAttemptId duy nhất theo học viên, score, thời gian, lesson revision |
| star_ledger / wallet hiện có | sự kiện thưởng/chi tiêu duy nhất, giao dịch nguyên tử; ẩn bài không xóa giao dịch |
| runtime_dialogue/runtime_profile trong SQL | bảng staging để đối chiếu/import, không phải endpoint công khai |

Trong production dùng ID số và FK như backend hiện có; SQL review dùng lesson_code để tránh
bịa ID instrument/skillLevel/user. Approval seed DRAFT, is_visible=false cho tất cả;
`in_current_roadmap` chỉ mô tả app, **không đồng nghĩa admin đã duyệt trên server**.

## Các điểm phải giải quyết trước khi seed bài chạy được

1. **Nhấn:** danh mục còn chuỗi 8 nốt Mi2/Fa2/La2/Si2/Mi3/Fa3/La3/Si3.
   Runtime PRESS_EXERCISES là 6 bài, 2 lượt: Mi2→Fa2, Mi3→Fa3, Mi4→Fa4;
   La2→Si2, La3→Si3, La4→Si4. Dùng runtime để chốt, không seed danh mục cũ thành cấu hình chạy.
2. **Song thanh:** 12 cặp trong danh mục, runtime chia 6 cặp/lượt.
   Không gộp thành một hợp âm ba ngón hoặc chỉ lưu cặp đầu.
3. **Vê:** runtime gồm nhóm một dây Đô2/Sol2/Đô3 và nhóm quãng tám
   Đô2–Đô3, Sol2–Sol3, La2–La3. Cần giữ nhóm, kiểu luân phiên, thời gian/nhịp tấn công.
4. **Rung:** 7 nốt Sol2, La2, Đô3, Rê3, Mi3, Sol3, La3.
   Điều kiện nhận diện không chỉ là giữ đúng một tần số.
5. **Á:** giữ nguyên direction/stringSequence/finger nguồn; cần kiểm thử nghĩa lên/xuống
   theo cách đánh số dây app. Không tự đảo theo suy đoán âm nhạc.
6. **Hợp âm La thứ:** source có cả sự kiện Đô trưởng xen kẽ; không thay toàn bộ bằng La thứ chỉ vì tên bài.
7. **Trường độ:** giữ số thực gốc (có 0.5/1.5/3.0); backend cần chốt đơn vị beats/seconds
   theo luồng sử dụng. Không suy duration thành số giây giữ mic; không tự gán nhịp 4/4
   cho những bài không có cấu hình đó.
8. **Hội thoại:** xuất cả action/highlight/note và các thuộc tính khác. Chỉ content_text
   không diễn tả hết bước tương tác. Không seed mỗi chuỗi practice mô tả làm lời cô Mai.
9. **Ngưỡng đạt:** không tự seed 80 hay quy tắc thưởng 1/2/3 sao từ giao diện web.
   Cần thống nhất logic chấm/hoàn thành hiện hành, thông số nền tảng mic và policy backend.
10. Một số bài là lý thuyết/video, không được ép mọi bài phải có khuông hoặc chấm mic.
    Source có 28 bộ hội thoại, nhiều hơn 26 mục catalog; không xóa các bộ không match.
11. 17 dây đúng thứ tự nằm trong ALL_17_NOTES. Các âm Fa/Si dùng nguồn Mi/La khi nhấn;
    không tự tạo thêm dây. Giữ cách viết Đô/Rê ở biên dữ liệu rồi ánh xạ nhất quán với web.

## Backend cần sửa thêm so với OpenAPI đã cung cấp

- Có API ẩn/hiện riêng (visibility); không DELETE/soft-delete bài. Chỉ admin duyệt,
  quyền ẩn phải được xác định riêng. Ẩn không ảnh hưởng progress/stars/vật phẩm.
- API GET/PUT practice config và activity sequence/version; schema hiện chưa chứa
  notes, chord, fingering, technique, rounds, time signature và cấu hình nhận diện.
- API lưu nguyên tử nhiều đoạn/đổi thứ tự (hoặc transaction batch), tránh lưu một phần.
- Mapping lessonCode/legacy code ổn định và migration lịch sử có kiểm kê sao trước/sau.
- Dùng lại POST /api/users/me/lessons/{lessonId}/complete và clientAttemptId hiện có;
  không tạo endpoint thưởng song song. Practice dùng client_uuid hiện có.
- Retry cùng ID phải trả cùng kết quả, không thưởng lại. Lần cải thiện điểm chỉ cộng phần
  sao tăng thêm theo policy, không cộng lại toàn bộ; backend là nguồn số dư chính thức.
- FK RESTRICT, không cascade delete lịch sử. Ledger/attempt nên append-only; mua vật phẩm
  trừ spendableStars nguyên tử, không sửa tổng sao thành tích.
- Manifest/cache revision cho offline; không fallback qua quyền 401/403.
  Cache bài đã ẩn cần policy rõ, không hứa thu hồi tức thời khi thiết bị không có mạng.
- API master level cần đủ levelCode; LessonResponse.skillLevel hiện chỉ có id/levelName.
- Thống nhất whitelist approval status: OpenAPI chỉ mô tả string, không coi HIDDEN là approval.
- File res:// không phải URL web. Nếu phát video/audio từ server cần upload/mapping asset,
  giữ bundled fallback; việc bỏ nút upload khỏi form không có nghĩa được xóa asset server.

## Đối chiếu web

Nội dung bài: nhiều đoạn cô Mai + khuông thực hành (nốt đơn/song thanh/hợp âm tách riêng).
Nút Lưu thay đổi phải lưu toàn bộ tài liệu có phiên bản khi API hỗ trợ; hiện khuông vẫn local.
Cấu hình giáo trình: tên/cấp/thứ tự và điều kiện đánh giá; không sửa trạng thái phê duyệt.
Chỉ cho ẩn/hiện khi API có hỗ trợ; không đổi nhãn nút DELETE thành Ẩn.
Các cờ thử mở toàn bộ bài trong app không phải dữ liệu quyền/mở khóa để seed production.

## Kiểm tra trước import production

- Review SQL ở database thử, không chạy script vào schema thật.
- So sánh 22 bài thuộc lộ trình / 4 legacy, 28 bộ / 166 step và cấu hình 6 bài Nhấn.
- Kiểm tra đầy đủ file âm thanh/video tồn tại; chưa xác nhận đường dẫn tải remote.
- Đối chiếu screenshot, nghe mẫu, nhận diện và kết quả từng bài với Godot/Xogot.
- Seed lặp lại không ghi đè bài đã sửa: SQL dùng ON CONFLICT DO NOTHING.
- Không tự công bố bài; không nhập user/progress/star giả.
- Bảng revision/activity/exercise_config trong SQL để trống có chủ đích:
  backend chỉ chuyển staging sang cấu hình chạy sau khi xử lý các lệch catalog/runtime.
