package com.example.englishcentre.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// Mapped from web_languagecentre: User(role=teacher|student) -> Teacher/Student, Course -> Course (+ timetable slot),
// Lesson -> Lesson (one class session), Enrollment -> Enrollment (+ score, teacher note); new: Attendance, Notice.
// new: Assignment + Submission (homework, read-only), Message (local chat history).
// Students can only read data; the only writes are course registration/cancellation, marking notices as read
// and sending chat messages.

@Entity
data class Teacher(@PrimaryKey val id: Long, val name: String, val email: String)

@Entity(indices = [Index("code", unique = true)])
data class Student(@PrimaryKey val id: Long, val code: String, val name: String, val email: String, val password: String)

@Entity(foreignKeys = [ForeignKey(Teacher::class, ["id"], ["teacherId"])], indices = [Index("teacherId")])
data class Course(
    @PrimaryKey val id: Long, val code: String, val title: String, val teacherId: Long,
    val day: Int, val startTime: String, val endTime: String, val room: String, // day: 1 = Monday ... 7 = Sunday
    val fee: Long, val capacity: Int, val startDate: String
)

@Entity(foreignKeys = [ForeignKey(Course::class, ["id"], ["courseId"], onDelete = CASCADE)], indices = [Index("courseId")])
data class Lesson(@PrimaryKey val id: Long, val courseId: Long, val no: Int, val title: String, val date: String)

@Entity(
    primaryKeys = ["courseId", "studentId"],
    foreignKeys = [
        ForeignKey(Course::class, ["id"], ["courseId"], onDelete = CASCADE),
        ForeignKey(Student::class, ["id"], ["studentId"], onDelete = CASCADE)],
    indices = [Index("studentId")]
)
data class Enrollment(val courseId: Long, val studentId: Long, val score: Double? = null, val note: String = "")

@Entity(
    primaryKeys = ["lessonId", "studentId"],
    foreignKeys = [
        ForeignKey(Lesson::class, ["id"], ["lessonId"], onDelete = CASCADE),
        ForeignKey(Student::class, ["id"], ["studentId"], onDelete = CASCADE)],
    indices = [Index("studentId")]
)
data class Attendance(val lessonId: Long, val studentId: Long, val present: Boolean)

/** courseId = null: notice for everyone. Texts hold "vi|en|ja". */
@Entity(foreignKeys = [ForeignKey(Course::class, ["id"], ["courseId"], onDelete = CASCADE)], indices = [Index("courseId")])
data class Notice(
    @PrimaryKey val id: Long, val courseId: Long?, val date: String,
    val title: String, val body: String, val seen: Boolean = false
)

@Entity(foreignKeys = [ForeignKey(Course::class, ["id"], ["courseId"], onDelete = CASCADE)], indices = [Index("courseId")])
data class Assignment(@PrimaryKey val id: Long, val courseId: Long, val title: String, val description: String, val due: String)

@Entity(
    primaryKeys = ["assignmentId", "studentId"],
    foreignKeys = [
        ForeignKey(Assignment::class, ["id"], ["assignmentId"], onDelete = CASCADE),
        ForeignKey(Student::class, ["id"], ["studentId"], onDelete = CASCADE)],
    indices = [Index("studentId")]
)
data class Submission(val assignmentId: Long, val studentId: Long, val submittedAt: String, val score: Double?)

/** Chat message stored on this device. peer = "t<teacherId>" or "s<studentId>"; kind: 0 text, 1 photo, 2 file, 3 voice. */
@Entity(indices = [Index("owner", "peer")])
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, val owner: Long, val peer: String, val mine: Boolean,
    val kind: Int = 0, val text: String = "", val uri: String = "", val time: Long = System.currentTimeMillis()
)

data class CourseRow(@Embedded val c: Course, val teacher: String, val students: Int, val registered: Boolean)
data class LessonRow(@Embedded val l: Lesson, val present: Boolean?)
data class NoticeRow(@Embedded val n: Notice, val course: String?)
data class AssignmentRow(@Embedded val a: Assignment, val course: String, val submittedAt: String?, val score: Double?)
data class Contact(val peer: String, val name: String, val preview: String?, val kind: Int?, val time: Long?)

@Dao
interface CentreDao {
    @Insert suspend fun seed(
        t: List<Teacher>, s: List<Student>, c: List<Course>, l: List<Lesson>,
        e: List<Enrollment>, a: List<Attendance>, n: List<Notice>,
        h: List<Assignment>, u: List<Submission>, m: List<Message>
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun register(e: Enrollment)
    @Query("DELETE FROM Enrollment WHERE courseId = :courseId AND studentId = :studentId")
    suspend fun cancel(courseId: Long, studentId: Long)
    @Query("UPDATE Notice SET seen = 1 WHERE id = :id") suspend fun markSeen(id: Long)
    @Insert suspend fun send(m: Message)

    @Query("SELECT COUNT(*) FROM Teacher") suspend fun count(): Int
    @Query("SELECT * FROM Student WHERE (code = :login OR email = :login) AND password = :password")
    suspend fun login(login: String, password: String): Student?
    @Query("SELECT * FROM Student WHERE id = :id") fun student(id: Long): Flow<Student?>

    /** courseId = 0: every course. */
    @Query(
        """SELECT c.*, t.name AS teacher, (SELECT COUNT(*) FROM Enrollment e WHERE e.courseId = c.id) AS students,
        EXISTS(SELECT 1 FROM Enrollment e WHERE e.courseId = c.id AND e.studentId = :studentId) AS registered
        FROM Course c JOIN Teacher t ON t.id = c.teacherId WHERE :courseId = 0 OR c.id = :courseId
        ORDER BY c.day, c.startTime"""
    )
    fun courses(studentId: Long, courseId: Long): Flow<List<CourseRow>>

    @Query("SELECT * FROM Enrollment WHERE courseId = :courseId AND studentId = :studentId")
    fun enrollment(courseId: Long, studentId: Long): Flow<Enrollment?>

    @Query(
        """SELECT l.*, a.present FROM Lesson l
        LEFT JOIN Attendance a ON a.lessonId = l.id AND a.studentId = :studentId
        WHERE l.courseId = :courseId ORDER BY l.no"""
    )
    fun lessons(courseId: Long, studentId: Long): Flow<List<LessonRow>>

    @Query(
        """SELECT n.*, c.code AS course FROM Notice n LEFT JOIN Course c ON c.id = n.courseId
        WHERE n.courseId IS NULL OR n.courseId IN (SELECT courseId FROM Enrollment WHERE studentId = :studentId)
        ORDER BY n.date DESC"""
    )
    fun notices(studentId: Long): Flow<List<NoticeRow>>

    /** Homework of the registered courses: not submitted first, then by due date. */
    @Query(
        """SELECT a.*, c.code AS course, s.submittedAt, s.score FROM Assignment a
        JOIN Course c ON c.id = a.courseId
        JOIN Enrollment e ON e.courseId = a.courseId AND e.studentId = :studentId
        LEFT JOIN Submission s ON s.assignmentId = a.id AND s.studentId = :studentId
        ORDER BY s.submittedAt IS NOT NULL, a.due"""
    )
    fun assignments(studentId: Long): Flow<List<AssignmentRow>>

    /** Every teacher and other student with the latest message; recent conversations first. */
    @Query(
        """SELECT p.peer, p.name, m.text AS preview, m.kind, m.time FROM
        (SELECT 't' || id AS peer, name FROM Teacher UNION ALL SELECT 's' || id, name FROM Student WHERE id != :owner) p
        LEFT JOIN Message m ON m.id = (SELECT id FROM Message WHERE owner = :owner AND peer = p.peer ORDER BY time DESC, id DESC LIMIT 1)
        ORDER BY m.time IS NULL, m.time DESC, p.name"""
    )
    fun contacts(owner: Long): Flow<List<Contact>>

    @Query("SELECT * FROM Message WHERE owner = :owner AND peer = :peer ORDER BY time, id")
    fun messages(owner: Long, peer: String): Flow<List<Message>>
}

@Database(
    entities = [Teacher::class, Student::class, Course::class, Lesson::class, Enrollment::class, Attendance::class, Notice::class,
        Assignment::class, Submission::class, Message::class],
    version = 3, exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): CentreDao

    companion object {
        @Volatile private var db: AppDb? = null
        fun get(ctx: Context) = db ?: synchronized(this) {
            db ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "englishcentre.db")
                .fallbackToDestructiveMigration(true).build().also { db = it }
        }
    }
}
