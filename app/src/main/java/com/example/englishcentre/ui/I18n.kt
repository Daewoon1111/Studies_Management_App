package com.example.englishcentre.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** 0 = Tiếng Việt, 1 = English, 2 = 日本語. */
val LocalLang = staticCompositionLocalOf { 0 }

/** Translates an English UI string (the key) and fills %s/%d placeholders. */
@Composable
fun t(en: String, vararg args: Any): String {
    val lang = LocalLang.current
    val s = if (lang == 1) en else S[en]?.split('|')?.get(lang / 2) ?: en
    return s.format(*args)
}

/** Picks the current language from a database text stored as "vi|en|ja". */
@Composable
fun String.pick(): String = split('|').let { it.getOrElse(LocalLang.current) { _ -> it[0] } }

@Composable
fun dayName(day: Int) = t(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")[day - 1])

val languages = listOf("Tiếng Việt", "English", "日本語")
val flags = listOf("🇻🇳", "🇬🇧", "🇯🇵")

// English key to "Vietnamese|Japanese"
private val S = mapOf(
    "Student portal" to "Cổng thông tin sinh viên|学生ポータル",
    "Student code or email" to "Mã sinh viên hoặc email|学生番号またはメール",
    "Password" to "Mật khẩu|パスワード",
    "Wrong student code or password." to "Sai mã sinh viên hoặc mật khẩu.|学生番号またはパスワードが違います。",
    "Log in" to "Đăng nhập|ログイン",
    "Log out" to "Đăng xuất|ログアウト",
    "Language" to "Ngôn ngữ|言語",
    "Theme" to "Giao diện|テーマ",
    "System" to "Hệ thống|システム",
    "Light" to "Sáng|ライト",
    "Dark" to "Tối|ダーク",
    "Classes" to "Lớp học|クラス",
    "Register" to "Đăng ký|登録",
    "Schedule" to "Lịch học|時間割",
    "Notices" to "Thông báo|お知らせ",
    "Account" to "Tài khoản|アカウント",
    "Back" to "Quay lại|戻る",
    "My classes" to "Lớp của tôi|マイクラス",
    "You have not registered for any course." to "Bạn chưa đăng ký môn học nào.|まだ登録したコースはありません。",
    "Room %s" to "Phòng %s|教室 %s",
    "Teacher: %s" to "Giáo viên: %s|講師:%s",
    "Start date: %s" to "Ngày bắt đầu: %s|開講日:%s",
    "Seats %d/%d" to "Sĩ số %d/%d|定員 %d/%d",
    "Score: %s" to "Điểm: %s|成績:%s",
    "Lessons" to "Buổi học|授業",
    "Lesson %d: %s" to "Buổi %d: %s|第%d回:%s",
    "Present" to "Có mặt|出席",
    "Absent" to "Vắng|欠席",
    "Upcoming" to "Sắp tới|予定",
    "Not recorded" to "Chưa điểm danh|未記録",
    "Course registration" to "Đăng ký môn học|コース登録",
    "Studying" to "Đang học|受講中",
    "Registration closed" to "Hết hạn đăng ký|登録締切",
    "Full" to "Hết chỗ|満員",
    "Clashes with %s" to "Trùng lịch %s|%s と時間が重複",
    "Cancel" to "Hủy đăng ký|登録取消",
    "Cancel registration?" to "Hủy đăng ký môn này?|このコースの登録を取り消しますか?",
    "Register for this course?" to "Đăng ký môn học này?|このコースに登録しますか?",
    "Close" to "Đóng|閉じる",
    "Timetable" to "Thời khóa biểu|時間割",
    "Today" to "Hôm nay|今日",
    "No classes" to "Không có lớp|授業なし",
    "No notices." to "Chưa có thông báo.|お知らせはありません。",
    "Centre" to "Trung tâm|センター",
    "Menu" to "Menu|メニュー",
    "Assignments" to "Bài tập|課題",
    "No assignments." to "Chưa có bài tập.|課題はありません。",
    "Submitted" to "Đã nộp|提出済み",
    "Overdue" to "Quá hạn|期限切れ",
    "Due: %s" to "Hạn nộp: %s|締切:%s",
    "Submitted on %s" to "Nộp ngày %s|提出日:%s",
    "AI assistant" to "Trợ lý AI|AIアシスタント",
    "Ask about your classes, timetable or homework." to "Hỏi về lớp học, thời khóa biểu hoặc bài tập của bạn.|クラス・時間割・課題について質問してください。",
    "AI is not configured. Add OPENROUTER_API_KEY to local.properties." to "AI chưa được cấu hình. Thêm OPENROUTER_API_KEY vào local.properties.|AIが設定されていません。local.properties に OPENROUTER_API_KEY を追加してください。",
    "Messages" to "Tin nhắn|メッセージ",
    "Conversations" to "Cuộc trò chuyện|会話",
    "No messages yet" to "Chưa có tin nhắn|メッセージはまだありません",
    "Message" to "Tin nhắn|メッセージ",
    "Send" to "Gửi|送信",
    "Take photo" to "Chụp ảnh|写真を撮る",
    "Send file" to "Gửi file|ファイルを送信",
    "Record audio" to "Ghi âm|録音",
    "Stop recording" to "Dừng ghi âm|録音を停止",
    "Photo" to "Ảnh|写真",
    "File" to "File|ファイル",
    "Voice message" to "Tin nhắn thoại|ボイスメッセージ",
    "Show password" to "Hiện mật khẩu|パスワードを表示",
    "Hide password" to "Ẩn mật khẩu|パスワードを隠す",
    "Hello, %s" to "Chào %s|こんにちは、%sさん",
    "No classes today" to "Hôm nay không có lớp|今日は授業がありません",
    "%d courses · %d homework due" to "%d môn học · %d bài tập cần nộp|%d コース・提出予定の課題 %d 件",
    "Registered courses" to "Môn đã đăng ký|登録済みのコース",
    "Score" to "Điểm|成績",
    "Attendance" to "Chuyên cần|出席率",
    "Lessons done" to "Đã học|受講済み",
    "Teacher's note" to "Nhận xét của giáo viên|講師のコメント",
    "You can register or cancel until the first lesson." to "Bạn có thể đăng ký hoặc hủy trước buổi học đầu tiên.|初回授業まで登録・取消ができます。",
    "Registered" to "Đã đăng ký|登録済み",
    "Open" to "Đang mở|受付中",
    "To do" to "Cần làm|未提出",
    "Due today" to "Hạn hôm nay|今日締切",
    "%d days left" to "Còn %d ngày|残り%d日",
    "Student" to "Sinh viên|学生",
    "Student code" to "Mã sinh viên|学生番号",
    "Email" to "Email|メール",
    "Your details are managed by the centre. Contact the office to change them." to "Thông tin do trung tâm quản lý. Liên hệ văn phòng để thay đổi.|情報はセンターが管理しています。変更は事務局へお問い合わせください。",
    "Teacher" to "Giáo viên|講師",
    "Classmate" to "Bạn học|クラスメート",
    "What classes do I have today?" to "Hôm nay mình có lớp nào?|今日はどの授業がありますか?",
    "Which homework is due soon?" to "Bài tập nào sắp đến hạn?|締切が近い課題は?",
    "Which courses can I still register for?" to "Mình còn đăng ký được môn nào?|まだ登録できるコースは?",
    "Mon" to "T2|月",
    "Tue" to "T3|火",
    "Wed" to "T4|水",
    "Thu" to "T5|木",
    "Fri" to "T6|金",
    "Sat" to "T7|土",
    "Sun" to "CN|日",
    "Monday" to "Thứ Hai|月曜日",
    "Tuesday" to "Thứ Ba|火曜日",
    "Wednesday" to "Thứ Tư|水曜日",
    "Thursday" to "Thứ Năm|木曜日",
    "Friday" to "Thứ Sáu|金曜日",
    "Saturday" to "Thứ Bảy|土曜日",
    "Sunday" to "Chủ Nhật|日曜日"
)
