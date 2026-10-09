package com.example.englishcentre.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.englishcentre.data.Ai
import com.example.englishcentre.data.Message
import com.example.englishcentre.data.Student
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Chat button that always stays on the right edge; drag it up or down. */
@Composable
fun BoxScope.FloatingChat(maxOffset: Float, onClick: () -> Unit) {
    var y by remember { mutableFloatStateOf(0f) }
    FloatingActionButton(
        onClick,
        Modifier.align(Alignment.CenterEnd).offset { IntOffset(0, y.roundToInt()) }.padding(end = 12.dp)
            .pointerInput(maxOffset) { detectVerticalDragGestures { _, d -> y = (y + d).coerceIn(-maxOffset, maxOffset) } },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { Icon(Icons.AutoMirrored.Filled.Chat, t("Messages")) }
}

private fun Context.fileName(uri: Uri) = contentResolver.query(uri, null, null, null, null)?.use {
    if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else null
} ?: "file"

@Suppress("DEPRECATION")
private fun Context.recorder(file: File) = (if (Build.VERSION.SDK_INT >= 31) MediaRecorder(this) else MediaRecorder()).apply {
    setAudioSource(MediaRecorder.AudioSource.MIC)
    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    setOutputFile(file.path)
    prepare()
    start()
}

private fun clock(time: Long) = SimpleDateFormat("dd/MM HH:mm", Locale.US).format(Date(time))
private val kinds = listOf("Message", "Photo", "File", "Voice message")

/** Conversations with teachers and classmates: text, photo, file and voice messages (stored on this device). */
@Composable
fun ChatScreen(back: () -> Unit) {
    val ctx = LocalContext.current
    val sid = LocalPrefs.current.user
    val io = db()
    val scope = rememberCoroutineScope()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val contacts by watch(emptyList(), sid) { contacts(sid) }
    var peer by rememberSaveable { mutableStateOf("") }
    val current = contacts.firstOrNull { it.peer == peer } ?: contacts.firstOrNull()
    val msgs by watch(emptyList(), sid, current?.peer) { messages(sid, current?.peer ?: "") }
    val list = rememberLazyListState()
    LaunchedEffect(msgs.size) { if (msgs.isNotEmpty()) list.scrollToItem(msgs.size - 1) }

    fun post(kind: Int, text: String, uri: String = "") {
        val c = current ?: return
        io { send(Message(owner = sid, peer = c.peer, mine = true, kind = kind, text = text, uri = uri)) }
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        bmp?.let {
            val f = File(ctx.filesDir, "photo_${System.currentTimeMillis()}.jpg")
            f.outputStream().use { o -> it.compress(Bitmap.CompressFormat.JPEG, 90, o) }
            post(1, f.name, f.path)
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { ctx.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            post(2, ctx.fileName(it), it.toString())
        }
    }
    var recording by remember { mutableStateOf<Pair<MediaRecorder, File>?>(null) }
    DisposableEffect(Unit) { onDispose { recording?.first?.release() } }
    fun record() {
        val f = File(ctx.filesDir, "voice_${System.currentTimeMillis()}.m4a")
        recording = runCatching { ctx.recorder(f) to f }.getOrNull()
    }
    val mic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) record() }
    fun toggleRecord() {
        val (r, f) = recording ?: run {
            val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (granted) record() else mic.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        val ok = runCatching { r.stop() }.isSuccess // stop() throws when the recording is too short
        r.release()
        recording = null
        if (ok) post(3, f.name, f.path)
    }

    ModalNavigationDrawer({
        ModalDrawerSheet {
            Text(t("Conversations"), Modifier.padding(20.dp, 20.dp, 20.dp, 8.dp), style = MaterialTheme.typography.titleLarge)
            LazyColumn {
                items(contacts) { c ->
                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(c.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    c.preview?.let { if (c.kind == 0) it else t(kinds[c.kind ?: 0]) } ?: t("No messages yet"),
                                    style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        selected = c.peer == current?.peer,
                        onClick = { peer = c.peer; scope.launch { drawer.close() } },
                        modifier = Modifier.padding(horizontal = 12.dp),
                        icon = { Avatar(c.name, 36.dp) }
                    )
                }
            }
        }
    }, drawerState = drawer) {
        Page(
            current?.name ?: t("Messages"), back, state = list,
            titleContent = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    current?.let { Avatar(it.name, 34.dp) }
                    Column {
                        Text(current?.name ?: t("Messages"), style = MaterialTheme.typography.titleMedium)
                        current?.let {
                            Text(
                                t(if (it.peer.startsWith("t")) "Teacher" else "Classmate"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            },
            actions = { IconButton({ scope.launch { drawer.open() } }) { Icon(Icons.Default.Forum, t("Conversations")) } },
            bottomBar = {
                InputBar(current != null, {
                    IconButton({ camera.launch(null) }) { Icon(Icons.Default.CameraAlt, t("Take photo")) }
                    IconButton({ picker.launch(arrayOf("*/*")) }) { Icon(Icons.Default.AttachFile, t("Send file")) }
                    IconButton({ toggleRecord() }) {
                        if (recording == null) Icon(Icons.Default.Mic, t("Record audio"))
                        else Icon(Icons.Default.Stop, t("Stop recording"), tint = MaterialTheme.colorScheme.error)
                    }
                }) { post(0, it) }
            }
        ) {
            if (msgs.isEmpty()) item { Empty(Icons.AutoMirrored.Filled.Chat, t("No messages yet")) }
            items(msgs) { m -> Bubble(m.mine, clock(m.time)) { MessageContent(m) } }
        }
    }
}

@Composable
private fun MessageContent(m: Message) {
    val ctx = LocalContext.current
    when (m.kind) {
        1 -> remember(m.uri) { BitmapFactory.decodeFile(m.uri)?.asImageBitmap() }?.let {
            Image(it, t("Photo"), Modifier.size(220.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        }
        2 -> Row(Modifier.clickable {
            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(m.uri)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
        }, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) { Icon(Icons.Default.AttachFile, null); Text(m.text) }
        3 -> Row(Modifier.clickable {
            runCatching { MediaPlayer().apply { setDataSource(m.uri); setOnCompletionListener { it.release() }; prepare(); start() } }
        }, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) { Icon(Icons.Default.PlayArrow, null); Text(t("Voice message")) }
        else -> Text(m.text)
    }
}

private val suggestions = listOf(
    "What classes do I have today?", "Which homework is due soon?", "Which courses can I still register for?"
)

/** Study assistant that answers from the student's own courses, timetable and homework. */
@Composable
fun AiScreen(back: () -> Unit) {
    val sid = LocalPrefs.current.user
    val scope = rememberCoroutineScope()
    val student by watch<Student?>(null, sid) { student(sid) }
    val courses by watch(emptyList(), sid) { courses(sid, 0) }
    val tasks by watch(emptyList(), sid) { assignments(sid) }
    val msgs = remember { mutableStateListOf<Pair<String, String>>() }
    var busy by remember { mutableStateOf(false) }
    val list = rememberLazyListState()
    LaunchedEffect(msgs.size, busy) { if (msgs.isNotEmpty()) list.scrollToItem(msgs.size - 1) }
    val lang = languages[LocalLang.current]
    val notConfigured = t("AI is not configured. Add OPENROUTER_API_KEY to local.properties.")
    val system = """You are the study assistant of English Centre. Answer in $lang, briefly.
        |Today: ${today()}. Student: ${student?.name} (${student?.code}).
        |Courses (registered = 1): ${courses.joinToString("; ") { "${it.c.code} ${it.c.title}, teacher ${it.teacher}, day ${it.c.day} (1 = Monday) ${it.c.startTime}-${it.c.endTime}, room ${it.c.room}, starts ${it.c.startDate}, fee ${it.c.fee} VND, seats ${it.students}/${it.c.capacity}, registered ${if (it.registered) 1 else 0}" }}
        |Homework: ${tasks.joinToString("; ") { "${it.course} ${it.a.title}, due ${it.a.due}, ${if (it.submittedAt != null) "submitted" else "not submitted"}" }}
        |Use only this data for facts about the student. You cannot change anything in the app.""".trimMargin()

    fun ask(q: String) {
        msgs += "user" to q
        if (!Ai.configured) {
            msgs += "assistant" to notConfigured
            return
        }
        busy = true
        scope.launch {
            msgs += "assistant" to runCatching { Ai.ask(listOf("system" to system) + msgs) }.getOrElse { it.message ?: "Error" }
            busy = false
        }
    }

    val asks = suggestions.map { t(it) }
    Page(t("AI assistant"), back, state = list, bottomBar = { InputBar(!busy) { ask(it) } }) {
        if (msgs.isEmpty()) item {
            Column(Modifier.padding(vertical = 24.dp), Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Text(t("Ask about your classes, timetable or homework."), style = MaterialTheme.typography.titleMedium)
                asks.forEach { q -> SuggestionChip({ ask(q) }, { Text(q) }) }
            }
        }
        items(msgs) { (role, text) -> Bubble(role == "user") { Text(text) } }
        if (busy) item { Bubble(false) { Text("…") } }
    }
}
