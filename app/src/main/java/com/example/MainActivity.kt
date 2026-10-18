package com.example

import android.os.Bundle
import android.app.Activity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.data.supabase.SupabaseRoleAuthenticator
import com.example.data.supabase.SupabaseSession
import com.example.data.supabase.SupabaseSessionProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.StorePortalApp
import com.example.ui.StoreViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: StoreViewModel by viewModels()
    private val viewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SupabaseMerchantAuthGate(viewModel = viewModel) {
                    StorePortalApp(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@Composable
private fun SupabaseMerchantAuthGate(
    viewModel: StoreViewModel,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authenticator = remember(context) { SupabaseRoleAuthenticator(context, "merchant") }
    val scope = rememberCoroutineScope()
    val currentScreen by viewModel.currentScreen.collectAsState()
    var session by remember { mutableStateOf<SupabaseSession?>(null) }
    var restoring by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(false) }

    suspend fun activate(candidate: SupabaseSession): Boolean {
        SupabaseSessionProvider.session = candidate
        val result = viewModel.loginWithSupabase(candidate.userId, candidate.email.orEmpty())
        if (result.isSuccess) {
            session = candidate
            ready = true
            errorMessage = null
            return true
        }
        authenticator.signOut(candidate)
        SupabaseSessionProvider.session = null
        session = null
        ready = false
        errorMessage = result.exceptionOrNull()?.message ?: "الحساب لا يملك متجراً جاهزاً."
        return false
    }

    LaunchedEffect(authenticator) {
        val restored = authenticator.restore()
        if (restored.isSuccess) restored.getOrNull()?.let { activate(it) }
        else errorMessage = restored.exceptionOrNull()?.message
        restoring = false
    }

    LaunchedEffect(currentScreen, session) {
        if (session != null && currentScreen == StoreNavScreen.LOGIN) {
            authenticator.signOut(session)
            SupabaseSessionProvider.session = null
            session = null
            ready = false
        }
    }

    LaunchedEffect(currentScreen, session) {
        if (session != null && ready && currentScreen != StoreNavScreen.LOGIN) {
            while (true) {
                viewModel.refreshRemoteData()
                delay(15_000)
            }
        }
    }

    if (session != null && ready) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("بوابة المتجر — وصّل", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("الدخول متاح لحسابات التجار المفعّلة والمرتبطة بمتجر.")
            Spacer(Modifier.height(24.dp))
            if (restoring || loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    enabled = authenticator.isConfigured() && activity != null,
                    onClick = {
                        val hostActivity = activity ?: return@Button
                        scope.launch {
                            loading = true
                            errorMessage = null
                            val result = authenticator.signIn(hostActivity)
                            if (result.isSuccess) {
                                activate(result.getOrThrow())
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "تعذر تسجيل الدخول."
                            }
                            loading = false
                        }
                    },
                ) {
                    Text("المتابعة باستخدام Google")
                }
            }
            if (!authenticator.isConfigured()) {
                Spacer(Modifier.height(12.dp))
                Text("أكمل إعداد Supabase وGoogle OAuth في local.properties أولاً.", color = MaterialTheme.colorScheme.error)
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
