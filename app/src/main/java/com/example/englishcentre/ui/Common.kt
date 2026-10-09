@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.englishcentre.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.englishcentre.data.CentreDao
import com.example.englishcentre.data.Course
import com.example.englishcentre.data.Prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val LocalDao = staticCompositionLocalOf<CentreDao> { error("CentreDao not provided") }
val LocalPrefs = staticCompositionLocalOf<Prefs> { error("Prefs not provided") }

/** Opens the left sidebar; provided by App(). */
val LocalDrawer = staticCompositionLocalOf<() -> Unit> { {} }

/** Observes a DAO query; the query is rebuilt only when [keys] change. */
@Composable
fun <T> watch(init: T, vararg keys: Any?, query: CentreDao.() -> Flow<T>): State<T> {
    val dao = LocalDao.current
    return remember(*keys) { dao.query() }.collectAsState(init)
}

/** Returns a launcher that runs a suspend DAO call in the screen's coroutine scope. */
@Composable
fun db(): (suspend CentreDao.() -> Unit) -> Unit {
    val dao = LocalDao.current
    val scope = rememberCoroutineScope()
    return { block -> scope.launch { dao.block() } }
}

private val ymd get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
fun money(v: Long) = String.format(Locale.US, "%,d", v).replace(',', '.') + " ₫"
fun today(): String = ymd.format(Date())
fun todayDay() = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1 = Monday ... 7 = Sunday
fun daysLeft(date: String) = ((ymd.parse(date)!!.time - ymd.parse(today())!!.time) / 86_400_000).toInt()
fun Course.clash(o: Course) = id != o.id && day == o.day && startTime < o.endTime && o.startTime < endTime

@Composable
fun slot(c: Course) = "${dayName(c.day)} · ${c.startTime}-${c.endTime} · ${t("Room %s", c.room)}"

// One color per course, used as the notebook tab on its cards.
private val tabColors = listOf(
    Color(0xFF2F5BEA), Color(0xFF2E9E6B), Color(0xFFE07A2F), Color(0xFF8E44AD), Color(0xFF1A9AA8), Color(0xFFC2185B)
)
fun tabColor(key: Long) = tabColors[Math.floorMod(key, tabColors.size.toLong()).toInt()]

val Mono = TextStyle(fontFamily = FontFamily.Monospace)

/** Ruled exercise-book paper with a red margin line: the app's signature background. */
fun Modifier.ruled(line: Color, margin: Color) = drawBehind {
    val step = 28.dp.toPx()
    var y = step
    while (y < size.height) {
        drawLine(line, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        y += step
    }
    drawLine(margin, Offset(40.dp.toPx(), 0f), Offset(40.dp.toPx(), size.height), 1.5.dp.toPx())
}

/** Highlighter-pen stroke behind the lower half of a text. */
fun Modifier.highlight(color: Color) = drawBehind {
    drawRoundRect(
        color, Offset(-4.dp.toPx(), size.height * .45f),
        Size(size.width + 8.dp.toPx(), size.height * .5f), CornerRadius(4.dp.toPx())
    )
}

/** Screen with a top bar: back arrow when [back] is set, otherwise the sidebar menu button. */
@Composable
fun Page(
    title: String,
    back: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    state: LazyListState = rememberLazyListState(),
    titleContent: @Composable () -> Unit = { Text(title, style = MaterialTheme.typography.titleLarge) },
    content: LazyListScope.() -> Unit
) = Scaffold(
    topBar = {
        TopAppBar(title = titleContent, actions = actions, navigationIcon = {
            if (back != null) IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Back")) }
            else IconButton(LocalDrawer.current) { Icon(Icons.Default.Menu, t("Menu")) }
        })
    },
    bottomBar = bottomBar
) { p ->
    LazyColumn(
        Modifier.padding(p), state, PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), content = content
    )
}

@Composable
fun Section(text: String) = Text(
    text, Modifier.padding(top = 12.dp, bottom = 2.dp),
    color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge
)

/** Small status label tinted with its color. */
@Composable
fun Pill(text: String, color: Color) = Text(
    text, Modifier.background(color.copy(alpha = .14f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp),
    color = color, style = MaterialTheme.typography.labelMedium, maxLines = 1
)

@Composable
fun Avatar(name: String, size: Dp = 40.dp) = Box(
    Modifier.size(size).background(tabColor(name.hashCode().toLong()), CircleShape), contentAlignment = Alignment.Center
) {
    val words = name.split(' ').filter { it.isNotBlank() }
    val initials = (words.firstOrNull()?.take(1) ?: "") + (if (words.size > 1) words.last().take(1) else "")
    Text(initials.uppercase(), color = Color.White, style = MaterialTheme.typography.titleSmall)
}

/** Empty state: what is missing, said plainly. */
@Composable
fun Empty(icon: ImageVector, text: String) = Column(
    Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally
) {
    Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(12.dp))
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
}

/** Number with a caption, e.g. a score or attendance rate. */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) = Card(modifier) {
    Column(Modifier.padding(14.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

/** Course card with a colored notebook tab on the left; [bottom] adds extra rows. */
@Composable
fun CourseCard(
    tab: Long, code: String, title: String, sub: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    bottom: @Composable ColumnScope.() -> Unit = {}
) = Card(Modifier.fillMaxWidth().clickable(onClick != null) { onClick?.invoke() }) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Box(Modifier.width(6.dp).fillMaxHeight().background(tabColor(tab)))
        Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(code, Modifier.weight(1f), tabColor(tab), style = MaterialTheme.typography.labelMedium.merge(Mono))
                trailing()
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (sub.isNotEmpty()) Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            bottom()
        }
    }
}

@Composable
fun Item(
    title: String, sub: String = "",
    leading: (@Composable () -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) = Card(Modifier.fillMaxWidth().clickable(onClick != null) { onClick?.invoke() }) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = sub.takeIf { it.isNotEmpty() }?.let { s -> @Composable { Text(s) } },
        leadingContent = leading,
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

/** Chat bubble: mine on the right, others on the left, with an optional time under it. */
@Composable
fun Bubble(mine: Boolean, time: String = "", content: @Composable () -> Unit) = Column(
    Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start
) {
    Surface(
        Modifier.widthIn(max = 280.dp),
        RoundedCornerShape(18.dp, 18.dp, if (mine) 4.dp else 18.dp, if (mine) 18.dp else 4.dp),
        if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
    ) { Box(Modifier.padding(14.dp, 10.dp)) { content() } }
    if (time.isNotEmpty()) Text(
        time, Modifier.padding(4.dp, 2.dp), MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall
    )
}

/** Text field with a send button, used by the chat and AI screens. */
@Composable
fun InputBar(enabled: Boolean = true, leading: @Composable () -> Unit = {}, onSend: (String) -> Unit) = Surface(
    color = MaterialTheme.colorScheme.surfaceContainer
) {
    var text by rememberSaveable { mutableStateOf("") }
    Row(Modifier.navigationBarsPadding().imePadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        leading()
        OutlinedTextField(
            text, { text = it }, Modifier.weight(1f), placeholder = { Text(t("Message")) },
            maxLines = 4, shape = RoundedCornerShape(24.dp)
        )
        FilledIconButton({ onSend(text.trim()); text = "" }, Modifier.padding(start = 6.dp), enabled = enabled && text.isNotBlank()) {
            Icon(Icons.AutoMirrored.Filled.Send, t("Send"))
        }
    }
}

/** Language (shown with its flag) and system/light/dark theme pickers, at the bottom of the sidebar. */
@Composable
fun Settings() = Column(Modifier.padding(16.dp), Arrangement.spacedBy(10.dp)) {
    val p = LocalPrefs.current
    var open by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(t("Language"), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        Box {
            OutlinedButton({ open = true }) { Text("${flags[p.lang.toInt()]}  ${languages[p.lang.toInt()]}") }
            DropdownMenu(open, { open = false }) {
                languages.forEachIndexed { i, s ->
                    DropdownMenuItem({ Text(s) }, { p.lang = i.toLong(); open = false }, leadingIcon = { Text(flags[i]) })
                }
            }
        }
    }
    Text(t("Theme"), style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Icons.Default.PhoneAndroid to "System", Icons.Default.LightMode to "Light", Icons.Default.DarkMode to "Dark")
            .forEachIndexed { i, (icon, label) ->
                FilterChip(p.theme == i.toLong(), { p.theme = i.toLong() }, { Text(t(label)) },
                    leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) })
            }
    }
}
