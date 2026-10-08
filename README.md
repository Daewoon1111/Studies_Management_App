# English Centre — App sinh viên xem lớp học tiếng Anh (Android)

App Android (Kotlin + Jetpack Compose + Material 3) cho **sinh viên tự kiểm tra lớp học của mình**, dựng lại từ dữ liệu
của web bán khóa học `web_languagecentre`. Cùng bộ khung với thư mục `app` (Compose, `MaterialTheme`, `ui/theme`).
Dữ liệu lưu offline bằng Room (SQLite trên máy), không cần server.

## Giao diện

Chủ đề: nền giấy kẻ dòng có lề đỏ (màn đăng nhập, thẻ chào ở trang Lớp học), mực xanh bi (màu chính),
bút dạ vàng (đánh dấu hôm nay, tab đang chọn, thông báo chưa đọc), bút đỏ của giáo viên (vắng, quá hạn, trùng lịch),
xanh lá (có mặt, đã nộp). Mỗi môn có một màu riêng làm gáy thẻ, dùng thống nhất ở mọi màn hình. Tiêu đề dùng font serif,
mã môn và giờ học dùng font monospace. Có nền tối riêng ("bàn học ban đêm").

Sinh viên **chỉ xem**, không sửa được thông tin lớp, giáo viên, điểm, điểm danh, bài tập hay hồ sơ. Thao tác ghi duy nhất:
đăng ký / hủy đăng ký môn học, đánh dấu thông báo đã đọc và gửi tin nhắn.

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
