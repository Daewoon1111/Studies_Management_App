package com.example.englishcentre

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.englishcentre.data.AppDb
import com.example.englishcentre.data.Prefs
import com.example.englishcentre.data.Student
import com.example.englishcentre.data.seed
import com.example.englishcentre.ui.AccountScreen
import com.example.englishcentre.ui.AiScreen
import com.example.englishcentre.ui.AssignmentsScreen
import com.example.englishcentre.ui.Avatar
import com.example.englishcentre.ui.ChatScreen
import com.example.englishcentre.ui.ClassScreen
import com.example.englishcentre.ui.ClassesScreen
import com.example.englishcentre.ui.FloatingChat
import com.example.englishcentre.ui.LocalDao
import com.example.englishcentre.ui.LocalDrawer
import com.example.englishcentre.ui.LocalLang
import com.example.englishcentre.ui.LocalPrefs
import com.example.englishcentre.ui.LoginScreen
import com.example.englishcentre.ui.Mono
import com.example.englishcentre.ui.NoticesScreen
import com.example.englishcentre.ui.RegisterScreen
import com.example.englishcentre.ui.Settings
import com.example.englishcentre.ui.TimetableScreen
import com.example.englishcentre.ui.t
import com.example.englishcentre.ui.theme.EnglishCentreTheme
import com.example.englishcentre.ui.watch
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDb.get(this)
        val prefs = Prefs(this)
        lifecycleScope.launch { db.seed() }
        setContent {
            val dark = if (prefs.theme == 0L) isSystemInDarkTheme() else prefs.theme == 2L
            DisposableEffect(dark) { // status/navigation bar icons follow the app theme, not only the system one
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(style, style)
                onDispose {}
            }
            EnglishCentreTheme(darkTheme = dark) {
                CompositionLocalProvider(
                    LocalDao provides db.dao(), LocalPrefs provides prefs, LocalLang provides prefs.lang.toInt()
                ) {
                    Surface { if (prefs.user == 0L) LoginScreen() else App() }
                }
            }
        }
    }
}

// bottom bar, in this order
private val tabs = listOf(
    Triple("classes", "Classes", Icons.Default.School),
    Triple("timetable", "Schedule", Icons.Default.CalendarMonth),
    Triple("assignments", "Assignments", Icons.AutoMirrored.Filled.Assignment),
    Triple("notices", "Notices", Icons.Default.Notifications)
)

// left sidebar
private val menu = listOf(
    Triple("register", "Course registration", Icons.Default.AppRegistration),
    Triple("ai", "AI assistant", Icons.Default.SmartToy),
    Triple("account", "Account", Icons.Default.AccountCircle)
)

@Composable
fun App() {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val sid = LocalPrefs.current.user
    val student = watch<Student?>(null, sid) { student(sid) }.value
    val unread = watch(emptyList(), sid) { notices(sid) }.value.count { !it.n.seen }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val open: (Long) -> Unit = { nav.navigate("class/$it") }
    val back: () -> Unit = { nav.popBackStack() }
    val isTab = tabs.any { it.first == route }

    ModalNavigationDrawer({
        ModalDrawerSheet {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(student?.name ?: "", 48.dp)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(student?.name ?: "", style = MaterialTheme.typography.titleMedium)
                    Text(student?.code ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall.merge(Mono))
                }
            }
            HorizontalDivider(Modifier.padding(bottom = 8.dp))
            menu.forEach { (r, label, icon) ->
                NavigationDrawerItem(
                    { Text(t(label)) }, route == r,
                    { scope.launch { drawer.close() }; nav.navigate(r) { launchSingleTop = true } },
                    Modifier.padding(horizontal = 12.dp), { Icon(icon, null) }
                )
            }
            Spacer(Modifier.weight(1f))
            HorizontalDivider()
            Settings()
        }
    }, drawerState = drawer, gesturesEnabled = isTab || drawer.isOpen) {
        CompositionLocalProvider(LocalDrawer provides { scope.launch { drawer.open() } }) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                Scaffold(
                    contentWindowInsets = WindowInsets(0),
                    bottomBar = {
                        if (isTab) NavigationBar {
                            tabs.forEach { (r, label, icon) ->
                                NavigationBarItem(route == r, {
                                    nav.navigate(r) {
                                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }, {
                                    BadgedBox({ if (r == "notices" && unread > 0) Badge { Text("$unread") } }) { Icon(icon, t(label)) }
                                }, label = { Text(t(label)) })
                            }
                        }
                    }
                ) { pad ->
                    NavHost(nav, "classes", Modifier.padding(pad).consumeWindowInsets(pad)) {
                        composable("classes") { ClassesScreen(open) }
                        composable("timetable") { TimetableScreen(open) }
                        composable("assignments") { AssignmentsScreen() }
                        composable("notices") { NoticesScreen() }
                        composable("register") { RegisterScreen(back) }
                        composable("ai") { AiScreen(back) }
                        composable("account") { AccountScreen(back) }
                        composable("chat") { ChatScreen(back) }
                        composable("class/{id}") { e -> ClassScreen(e.arguments?.getString("id")!!.toLong(), back) }
                    }
                }
                if (route != "chat") FloatingChat((constraints.maxHeight / 2f - 300f).coerceAtLeast(0f)) { nav.navigate("chat") { launchSingleTop = true } }
            }
        }
    }
}
