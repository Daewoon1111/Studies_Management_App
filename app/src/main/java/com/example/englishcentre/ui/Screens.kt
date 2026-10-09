package com.example.englishcentre.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.englishcentre.data.AssignmentRow
import com.example.englishcentre.data.CourseRow
import com.example.englishcentre.data.Enrollment
import com.example.englishcentre.data.NoticeRow
import com.example.englishcentre.data.Student
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    val dao = LocalDao.current
    val prefs = LocalPrefs.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var wrong by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val ready = login.isNotBlank() && password.isNotBlank() && !busy
    fun submit() {
        if (!ready) return
        busy = true
        scope.launch {
            val s = dao.login(login.trim(), password)
            if (s == null) wrong = true else prefs.user = s.id
            busy = false
        }
    }
    val errorMessage = t("Wrong student code or password.")
    val errorText: (@Composable () -> Unit)? = if (wrong) {
        { Text(errorMessage) }
    } else null
    Box(Modifier.fillMaxSize().ruled(colors.outlineVariant, colors.error.copy(alpha = .35f))) {
        Column(
            Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(24.dp),
            Arrangement.spacedBy(16.dp, Alignment.CenterVertically), Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(72.dp).background(colors.primary, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Text("EC", color = colors.onPrimary, style = MaterialTheme.typography.headlineSmall)
            }
            Text("English Centre", style = MaterialTheme.typography.displaySmall)
            Text(t("Student portal"), Modifier.highlight(colors.secondaryContainer), style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        login, { login = it; wrong = false }, Modifier.fillMaxWidth(),
                        label = { Text(t("Student code or email")) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        password, { password = it; wrong = false }, Modifier.fillMaxWidth(),
                        label = { Text(t("Password")) }, singleLine = true, isError = wrong,
                        supportingText = errorText,
                        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ show = !show }) {
                                Icon(if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility, t(if (show) "Hide password" else "Show password"))
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() })
                    )
                    Button({ submit() }, Modifier.fillMaxWidth().height(48.dp), enabled = ready) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text(t("Log in"))
                    }
                }
            }
        }
    }
}

@Composable
fun ClassesScreen(open: (Long) -> Unit) {
    val sid = LocalPrefs.current.user
    val colors = MaterialTheme.colorScheme
    val list by watch(emptyList(), sid) { courses(sid, 0) }
    val tasks by watch(emptyList(), sid) { assignments(sid) }
    val student by watch<Student?>(null, sid) { student(sid) }
    val mine = list.filter { it.registered }
    val now = todayDay()
    val day = today()
    val todayClasses = mine.filter { it.c.day == now }
    val due = tasks.count { it.submittedAt == null && it.a.due >= day }
    Page(t("My classes")) {
        item {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.primary)
                    .ruled(colors.onPrimary.copy(alpha = .10f), colors.secondaryContainer.copy(alpha = .6f))
                    .padding(start = 56.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
                Arrangement.spacedBy(6.dp)
            ) {
                Text(t("Hello, %s", student?.name?.substringAfterLast(' ') ?: ""), color = colors.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text("${dayName(now)} · $day", color = colors.onPrimary.copy(alpha = .8f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                if (todayClasses.isEmpty()) Text(t("No classes today"), color = colors.onPrimary)
                todayClasses.forEach {
                    Text(
                        "${it.c.startTime}  ${it.c.code} · ${t("Room %s", it.c.room)}", color = colors.onPrimary,
                        style = MaterialTheme.typography.bodyMedium.merge(Mono)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(t("%d courses · %d homework due", mine.size, due), color = colors.onPrimary, style = MaterialTheme.typography.labelLarge)
            }
        }
        item { Section(t("Registered courses")) }
        if (mine.isEmpty()) item { Empty(Icons.Default.School, t("You have not registered for any course.")) }
        items(mine) { r -> CourseCard(r.c.id, r.c.code, r.c.title, "${r.teacher}\n${slot(r.c)}", { open(r.c.id) }) }
    }
}

@Composable
fun ClassScreen(id: Long, back: () -> Unit) {
    val sid = LocalPrefs.current.user
    val colors = MaterialTheme.colorScheme
    val rows by watch(emptyList(), id, sid) { courses(sid, id) }
    val e by watch<Enrollment?>(null, id, sid) { enrollment(id, sid) }
    val lessons by watch(emptyList(), id, sid) { lessons(id, sid) }
    val r = rows.firstOrNull() ?: return
    val day = today()
    val done = lessons.filter { it.present != null }
    val present = done.count { it.present == true }
    Page(r.c.code, back) {
        item {
            CourseCard(
                r.c.id, r.c.code, r.c.title,
                "${t("Teacher: %s", r.teacher)}\n${slot(r.c)}\n${t("Start date: %s", r.c.startDate)} · ${money(r.c.fee)}"
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(e?.score?.toString() ?: "—", t("Score"), Modifier.weight(1f))
                StatTile(if (done.isEmpty()) "—" else "${present * 100 / done.size}%", t("Attendance"), Modifier.weight(1f))
                StatTile("${lessons.count { it.l.date < day }}/${lessons.size}", t("Lessons done"), Modifier.weight(1f))
            }
        }
        e?.note?.takeIf { it.isNotEmpty() }?.let { note ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.FormatQuote, null, tint = colors.primary)
                        Column {
                            Text(t("Teacher's note"), color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                            Text(note.pick(), fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }
        }
        item { Section(t("Lessons")) }
        items(lessons) { l ->
            val (status, color) = when (l.present) {
                true -> "Present" to colors.tertiary
                false -> "Absent" to colors.error
                null -> (if (l.l.date < day) "Not recorded" else "Upcoming") to colors.onSurfaceVariant
            }
            Item(
                t("Lesson %d: %s", l.l.no, l.l.title), l.l.date,
                leading = {
                    Box(Modifier.size(32.dp).background(color.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            when (l.present) { true -> Icons.Default.Check; false -> Icons.Default.Close; null -> Icons.Default.Schedule },
                            null, Modifier.size(18.dp), tint = color
                        )
                    }
                },
                trailing = { Pill(t(status), color) }
            )
        }
    }
}

@Composable
fun RegisterScreen(back: () -> Unit) {
    val sid = LocalPrefs.current.user
    val io = db()
    val colors = MaterialTheme.colorScheme
    val list by watch(emptyList(), sid) { courses(sid, 0) }
    val mine = list.filter { it.registered }
    val day = today()
    var confirm by remember { mutableStateOf<CourseRow?>(null) }
    Page(t("Course registration"), back) {
        item { Text(t("You can register or cancel until the first lesson."), color = colors.onSurfaceVariant) }
        items(list) { r ->
            val clash = mine.firstOrNull { it.c.clash(r.c) }
            val started = r.c.startDate <= day
            val (status, color) = when {
                r.registered && started -> t("Studying") to colors.primary
                r.registered -> t("Registered") to colors.tertiary
                started -> t("Registration closed") to colors.onSurfaceVariant
                r.students >= r.c.capacity -> t("Full") to colors.onSurfaceVariant
                clash != null -> t("Clashes with %s", clash.c.code) to colors.error
                else -> t("Open") to colors.tertiary
            }
            val canAct = !started && (r.registered || (clash == null && r.students < r.c.capacity))
            CourseCard(
                r.c.id, r.c.code, r.c.title, "${r.teacher}\n${slot(r.c)}\n${t("Start date: %s", r.c.startDate)}",
                trailing = { Pill(status, color) }
            ) {
                LinearProgressIndicator({ r.students / r.c.capacity.toFloat() }, Modifier.fillMaxWidth().padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${money(r.c.fee)} · ${t("Seats %d/%d", r.students, r.c.capacity)}", Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (canAct && r.registered) OutlinedButton({ confirm = r }) { Text(t("Cancel")) }
                    else if (canAct) Button({ confirm = r }) { Text(t("Register")) }
                }
            }
        }
    }
    confirm?.let { r ->
        AlertDialog(
            { confirm = null },
            confirmButton = {
                Button(
                    {
                        io { if (r.registered) cancel(r.c.id, sid) else register(Enrollment(r.c.id, sid)) }
                        confirm = null
                    },
                    colors = if (r.registered) ButtonDefaults.buttonColors(colors.error, colors.onError) else ButtonDefaults.buttonColors()
                ) { Text(t(if (r.registered) "Cancel" else "Register")) }
            },
            dismissButton = { TextButton({ confirm = null }) { Text(t("Close")) } },
            title = { Text(t(if (r.registered) "Cancel registration?" else "Register for this course?")) },
            text = { Text("${r.c.code} · ${r.c.title}\n${slot(r.c)}\n${money(r.c.fee)}") }
        )
    }
}

private val shortDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun TimetableScreen(open: (Long) -> Unit) {
    val sid = LocalPrefs.current.user
    val colors = MaterialTheme.colorScheme
    val list by watch(emptyList(), sid) { courses(sid, 0) }
    val mine = list.filter { it.registered }
    val now = todayDay()
    var day by rememberSaveable { mutableIntStateOf(now) }
    val classes = mine.filter { it.c.day == day }
    Page(t("Timetable")) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                (1..7).forEach { d ->
                    val selected = d == day
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(if (selected) colors.primary else colors.surfaceContainerHighest)
                            .clickable { day = d }.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            t(shortDays[d - 1]), if (d == now && !selected) Modifier.highlight(colors.secondaryContainer) else Modifier,
                            if (selected) colors.onPrimary else colors.onSurface, style = MaterialTheme.typography.labelLarge
                        )
                        Box(
                            Modifier.padding(top = 6.dp).size(6.dp).background(
                                when {
                                    mine.none { it.c.day == d } -> Color.Transparent
                                    selected -> colors.onPrimary
                                    else -> colors.primary
                                }, CircleShape
                            )
                        )
                    }
                }
            }
        }
        item {
            Text(
                dayName(day) + if (day == now) " · " + t("Today") else "",
                Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleLarge
            )
        }
        if (classes.isEmpty()) item { Empty(Icons.Default.EventAvailable, t("No classes")) }
        items(classes) { r ->
            Row(Modifier.height(IntrinsicSize.Min), Arrangement.spacedBy(10.dp)) {
                Column(Modifier.width(52.dp).padding(top = 14.dp)) {
                    Text(r.c.startTime, style = MaterialTheme.typography.titleSmall.merge(Mono))
                    Text(r.c.endTime, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall.merge(Mono))
                }
                CourseCard(r.c.id, r.c.code, r.c.title, "${t("Room %s", r.c.room)} · ${r.teacher}", { open(r.c.id) })
            }
        }
    }
}

@Composable
fun AssignmentsScreen() {
    val sid = LocalPrefs.current.user
    val colors = MaterialTheme.colorScheme
    val list by watch(emptyList(), sid) { assignments(sid) }
    var open by remember { mutableStateOf<AssignmentRow?>(null) }
    val day = today()
    val groups = listOf(
        "To do" to list.filter { it.submittedAt == null && it.a.due >= day },
        "Overdue" to list.filter { it.submittedAt == null && it.a.due < day },
        "Submitted" to list.filter { it.submittedAt != null }
    )
    Page(t("Assignments")) {
        if (list.isEmpty()) item { Empty(Icons.AutoMirrored.Filled.Assignment, t("No assignments.")) }
        groups.forEach { (title, rows) ->
            if (rows.isNotEmpty()) item { Section("${t(title)} · ${rows.size}") }
            items(rows) { r ->
                val left = daysLeft(r.a.due)
                val (status, color) = when {
                    r.score != null -> t("Score: %s", r.score.toString()) to colors.tertiary
                    r.submittedAt != null -> t("Submitted") to colors.tertiary
                    left < 0 -> t("Overdue") to colors.error
                    left == 0 -> t("Due today") to colors.error
                    else -> t("%d days left", left) to colors.primary
                }
                CourseCard(r.a.courseId, r.course, r.a.title, t("Due: %s", r.a.due), { open = r }, { Pill(status, color) })
            }
        }
    }
    open?.let { r ->
        AlertDialog(
            { open = null }, { TextButton({ open = null }) { Text(t("Close")) } },
            title = { Text(r.a.title) },
            text = {
                Text(listOfNotNull(r.course, r.a.description, t("Due: %s", r.a.due), r.submittedAt?.let { t("Submitted on %s", it) }).joinToString("\n"))
            }
        )
    }
}

@Composable
fun NoticesScreen() {
    val sid = LocalPrefs.current.user
    val io = db()
    val colors = MaterialTheme.colorScheme
    val list by watch(emptyList(), sid) { notices(sid) }
    var open by remember { mutableStateOf<NoticeRow?>(null) }
    Page(t("Notices")) {
        if (list.isEmpty()) item { Empty(Icons.Default.NotificationsNone, t("No notices.")) }
        items(list) { r ->
            Card(Modifier.fillMaxWidth().clickable { open = r; io { markSeen(r.n.id) } }) {
                Row(Modifier.padding(14.dp), Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(40.dp).background(if (r.n.seen) colors.surfaceVariant else colors.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(if (r.course == null) Icons.Default.Campaign else Icons.Default.School, null) }
                    Column(Modifier.weight(1f)) {
                        Text(
                            r.n.title.pick(), fontWeight = if (r.n.seen) FontWeight.Normal else FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(r.n.body.pick(), color = colors.onSurfaceVariant, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${r.course ?: t("Centre")} · ${r.n.date}", Modifier.padding(top = 4.dp), colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (!r.n.seen) Box(Modifier.padding(top = 6.dp).size(8.dp).background(colors.error, CircleShape))
                }
            }
        }
    }
    open?.let { r ->
        AlertDialog(
            { open = null }, { TextButton({ open = null }) { Text(t("Close")) } },
            title = { Text(r.n.title.pick()) }, text = { Text(r.n.body.pick()) }
        )
    }
}

@Composable
fun AccountScreen(back: () -> Unit) {
    val prefs = LocalPrefs.current
    val colors = MaterialTheme.colorScheme
    val s by watch<Student?>(null, prefs.user) { student(prefs.user) }
    Page(t("Account"), back) {
        item {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(s?.name ?: "", 88.dp)
                Spacer(Modifier.height(12.dp))
                Text(s?.name ?: "", style = MaterialTheme.typography.headlineSmall)
                Text(t("Student"), color = colors.onSurfaceVariant)
            }
        }
        item { Item(s?.code ?: "", t("Student code"), { Icon(Icons.Default.Badge, null) }) }
        item { Item(s?.email ?: "", t("Email"), { Icon(Icons.Default.Email, null) }) }
        item { Text(t("Your details are managed by the centre. Contact the office to change them."), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        item {
            OutlinedButton(
                { prefs.user = 0 }, Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(t("Log out"))
            }
        }
    }
}
