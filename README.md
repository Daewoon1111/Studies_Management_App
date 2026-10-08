# English Centre — App sinh viên xem lớp học tiếng Anh (Android)

App Android (Kotlin + Jetpack Compose + Material 3) cho **sinh viên tự kiểm tra lớp học của mình**, dựng lại từ dữ liệu
của web bán khóa học `web_languagecentre`. Cùng bộ khung với thư mục `app` (Compose, `MaterialTheme`, `ui/theme`).
Dữ liệu lưu offline bằng Room (SQLite trên máy), không cần server.

## Giao diện

Chủ đề "vở bài tập": nền giấy kẻ dòng có lề đỏ (màn đăng nhập, thẻ chào ở trang Lớp học), mực xanh bi (màu chính),
bút dạ vàng (đánh dấu hôm nay, tab đang chọn, thông báo chưa đọc), bút đỏ của giáo viên (vắng, quá hạn, trùng lịch),
xanh lá (có mặt, đã nộp). Mỗi môn có một màu riêng làm gáy thẻ, dùng thống nhất ở mọi màn hình. Tiêu đề dùng font serif,
mã môn và giờ học dùng font monospace. Có nền tối riêng ("bàn học ban đêm"). Màu động Android 12+ được tắt để giữ bảng màu.

Sinh viên **chỉ xem**, không sửa được thông tin lớp, giáo viên, điểm, điểm danh, bài tập hay hồ sơ. Thao tác ghi duy nhất:
đăng ký / hủy đăng ký môn học, đánh dấu thông báo đã đọc và gửi tin nhắn.

## Đăng nhập thử

| Mã sinh viên | Họ tên | Mật khẩu |
|---|---|---|
| SV001 | Nguyễn Văn An | 123456 |
| SV002 | Trần Thị Bình | 123456 |
| SV003 | Lê Hoàng Cường | 123456 |
| SV004 | Phạm Ngọc Dung | 123456 |
| SV005 | Võ Minh Em | 123456 |

Đăng nhập bằng mã sinh viên hoặc email (ví dụ `an.nguyen@student.vn`). Mật khẩu mẫu lưu dạng chữ thường,
chỉ dùng để demo; khi nối server thật phải xác thực ở server.

## Chức năng

**Thanh dưới** (theo thứ tự): Lớp học · Lịch học · Bài tập · Thông báo.
**Sidebar trái** (nút ☰ trên mỗi tab): Đăng ký môn học · Trợ lý AI · Tài khoản; phía dưới là ngôn ngữ (có cờ) và nền.
**Nút tin nhắn** nổi ở cạnh phải, kéo lên/xuống được, có trên mọi màn hình trừ khung chat.

| Màn hình | Nội dung |
|---|---|
| Lớp học | Các lớp đã đăng ký → chi tiết: giáo viên, lịch, phòng, học phí, điểm, nhận xét, chuyên cần, danh sách buổi học (có mặt / vắng / sắp tới) |
| Lịch học | Thời khóa biểu theo thứ (Thứ Hai → Chủ Nhật), đánh dấu hôm nay; bấm vào để xem lớp |
| Bài tập | Bài tập của các lớp đã đăng ký: hạn nộp, trạng thái (chưa nộp / quá hạn / đã nộp / điểm); bấm để xem mô tả |
| Thông báo | Thông báo chung và của các lớp đã đăng ký; chấm ● = chưa đọc, số chưa đọc hiện trên tab |
| Đăng ký môn học | Đăng ký nếu môn chưa khai giảng, còn chỗ, không trùng lịch; hủy đăng ký nếu môn chưa khai giảng |
| Trợ lý AI | Hỏi đáp về lớp, lịch học, bài tập của chính sinh viên (OpenRouter, giống web), trả lời theo ngôn ngữ đang chọn |
| Tài khoản | Thông tin sinh viên (chỉ xem), đăng xuất |
| Tin nhắn | Đầu khung: tên người đang chat + icon mở danh sách hội thoại (giáo viên, bạn học). Ô nhập: chụp ảnh, gửi file, ghi âm, nhắn tin; bấm file để mở, bấm tin thoại để nghe |

Ngôn ngữ: 🇻🇳 Tiếng Việt / 🇬🇧 English / 🇯🇵 日本語. Nền: Hệ thống / Sáng / Tối. Hai cài đặt được lưu lại khi mở app lần sau.

Tin nhắn lưu trên máy (Room), chưa có server nên người nhận không thấy và không trả lời. Muốn nhắn thật giữa các máy
cần thêm backend (ví dụ Firebase hoặc WebSocket trên Django).

## Cấu hình AI

Thêm vào `local.properties` (file này không đưa lên git), rồi Sync Gradle:

```
OPENROUTER_API_KEY=sk-or-...
OPENROUTER_MODEL=openrouter/auto
```

Không có key thì màn hình AI báo "AI chưa được cấu hình". Lưu ý: key nằm trong APK nên ai có file APK đều lấy được;
chỉ dùng cho demo. Bản thật nên gọi AI qua backend Django (`/api/chat/` của web) để giữ key ở server.

## Dữ liệu mẫu (`data/Seed.kt`)

- 6 giáo viên: Nguyễn Thu Hà, Sarah Johnson, Trần Minh Quân, David Miller, Lê Thanh Hương, Emily Carter.
- 9 môn học: ENG101 IELTS Foundation, ENG102 IELTS Intensive 6.5+, ENG201 TOEIC 650+, ENG202 English Communication,
  ENG301 Business English, ENG302 English Grammar Essentials (đang học); ENG103 IELTS Writing Masterclass,
  ENG104 Academic Vocabulary, ENG203 Pronunciation & Speaking (đang mở đăng ký).
- 54 buổi học, 20 lượt ghi danh (giữ đúng cặp sinh viên – khóa học của `web_languagecentre/database/seed.json`),
  điểm danh các buổi đã qua, 6 thông báo (3 ngôn ngữ), 12 bài tập + bài đã nộp, tin nhắn mẫu từ giáo viên và bạn học.
- Thử trùng lịch: SV001 học ENG101 (Thứ Hai 18:00-19:30) nên không đăng ký được ENG104 (Thứ Hai 19:00-20:30).

`docs/web_seed.json` là bản gốc từ web, chỉ để tham khảo, app không đọc file này.

## Ánh xạ từ web sang app

| Web (`backend/*/models.py`) | App (`data/Db.kt`) |
|---|---|
| `User` role = teacher / student | `Teacher` / `Student` (+ mã SV, mật khẩu) |
| `Course` | `Course` (+ mã môn, thứ, giờ, phòng, sĩ số tối đa, ngày bắt đầu; học phí VNĐ) |
| `Lesson` | `Lesson` = một buổi học (+ ngày) |
| `Enrollment` | `Enrollment` (+ điểm, nhận xét) |
| — | `Attendance` (điểm danh), `Notice` (thông báo), `Assignment` + `Submission` (bài tập), `Message` (tin nhắn) |

## Cấu trúc

```
app/src/main/java/com/example/englishcentre/
  MainActivity.kt      Theme, ngôn ngữ, đăng nhập, sidebar trái, thanh dưới 4 tab, nút tin nhắn nổi, NavHost
  data/Db.kt           Entity, DAO (truy vấn chỉ đọc + đăng ký/hủy/đã đọc/gửi tin), AppDb
  data/Ai.kt           Gọi OpenRouter chat completions
  data/Seed.kt         Dữ liệu mẫu (sinh bằng script, không có logic)
  data/Prefs.kt        Lưu ngôn ngữ, nền, sinh viên đang đăng nhập
  ui/I18n.kt           Bảng chữ Việt – Anh – Nhật, t(), pick()
  ui/Common.kt         Page, Item, Bubble, InputBar, Choice, Settings (cờ + nền), watch()/db(), tiền, trùng lịch
  ui/Screens.kt        Đăng nhập, lớp của tôi, chi tiết lớp, đăng ký, thời khóa biểu, thông báo, bài tập, tài khoản
  ui/Chat.kt           Nút tin nhắn nổi, khung chat (ảnh, file, ghi âm), trợ lý AI
  ui/theme/            Theme giống thư mục app
```

## Chạy

1. Mở thư mục `app_englishcentre` bằng Android Studio, bấm **Sync Project with Gradle Files**.
2. Chọn máy ảo (ví dụ Pixel 9 Pro), bấm **Run**.

Yêu cầu: JDK 17+ (Android Studio có sẵn JBR 21), Android SDK 36. Gradle wrapper 8.14.3, AGP 8.13.0, Kotlin 2.1.21, Room 2.7.2, Material Icons Extended.
Quyền: Internet (AI), micro (ghi âm, hỏi khi bấm lần đầu). Chụp ảnh dùng app camera của máy.
Cơ sở dữ liệu lên version 3: bản cài cũ tự xóa dữ liệu cũ và nạp lại dữ liệu mẫu. Muốn nạp lại dữ liệu mẫu: xóa dữ liệu app trên máy ảo.
