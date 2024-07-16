package com.example.hamsterkombatbot.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.ui.theme.HamsterKombatBotTheme
import com.example.hamsterkombatbot.viewmodel.TelegramViewModel

class HamsterBotActivity : ComponentActivity() {

    companion object {
        const val TAG = "HamsterBotActivity"
        const val EXTRA_URL = "url"
    }

    private val viewModel: TelegramViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HamsterKombatBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HamsterScreen(modifier = Modifier.fillMaxSize(), viewModel = viewModel) {
                        // finish activity with a result
                        Log.d(TAG, "onWebViewUrlFind: $it")

                        val intent = Intent()
                        intent.putExtra(EXTRA_URL, it)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun HamsterScreen(
    modifier: Modifier = Modifier,
    viewModel: TelegramViewModel,
    onWebViewUrlFind: (url: String) -> Unit
) {
    val authState by viewModel.state.observeAsState()
    val hamsterState by viewModel.hamsterState.observeAsState()

    var isFindingHamsterBot by remember { mutableStateOf(false) }
    val counter by remember { mutableIntStateOf(0) }

    LaunchedEffect(null) { viewModel.init() }

    if (authState is TelegramViewModel.AuthorizationState.Ready) {
        when (hamsterState) {
            is TelegramViewModel.HamsterState.ButtonUrlExtracted -> {
                SimpleStatus(modifier = modifier, text = stringResource(R.string.button_url_find))
                viewModel.tryToOpenWebView()
            }
            TelegramViewModel.HamsterState.FailedToParseButtons -> {
                SimpleStatus(
                    modifier = modifier,
                    text = stringResource(R.string.failed_to_parse_buttons)
                )
            }
            TelegramViewModel.HamsterState.Initial -> {
                SimpleStatus(modifier = modifier, text = stringResource(R.string.find_hamster_bot))
            }
            TelegramViewModel.HamsterState.Searched -> {
                SimpleStatus(
                    modifier = modifier,
                    text = stringResource(R.string.hamster_bot_searched)
                )
            }
            is TelegramViewModel.HamsterState.StartCommandSent -> {
                SimpleStatus(
                    modifier = modifier,
                    text = stringResource(R.string.hamster_bot_wait_for_response)
                )
            }
            TelegramViewModel.HamsterState.StartMessageReceived -> {
                SimpleStatus(
                    modifier = modifier,
                    text = stringResource(R.string.start_message_responded)
                )
            }
            is TelegramViewModel.HamsterState.WebViewUrl -> {
                val state = hamsterState as TelegramViewModel.HamsterState.WebViewUrl
                SimpleStatus(modifier = modifier, text = stringResource(R.string.hamster_completed))
                onWebViewUrlFind(state.url)

                if (counter.inc() == 1) {
                    viewModel.close()
                }
            }
            else -> {
                Text(text = stringResource(id = R.string.unknown_state))
            }
        }

        if (!isFindingHamsterBot) {
            viewModel.startFindingHamsterBot()
            isFindingHamsterBot = true
        }
    } else {
        SimpleStatus(modifier = modifier, text = stringResource(R.string.telegram_not_ready))
    }
}

@Composable
fun SimpleStatus(modifier: Modifier, text: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
