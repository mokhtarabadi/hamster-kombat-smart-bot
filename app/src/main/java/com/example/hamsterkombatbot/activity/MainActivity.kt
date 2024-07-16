package com.example.hamsterkombatbot.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.api.HamsterAPI
import com.example.hamsterkombatbot.helper.PreferencesHelper
import com.example.hamsterkombatbot.ui.theme.HamsterKombatBotTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private val telegramLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // telegram is ready to use
                Log.d(TAG, "Telegram is ready to use")

                // show toast: "Telegram is ready to use"
                Toast.makeText(this, getString(R.string.telegram_is_ready), Toast.LENGTH_SHORT)
                    .show()

                val intent = Intent(this, HamsterBotActivity::class.java)
                hamsterLauncher.launch(intent)
            }
        }

    private val hamsterLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // hamster is ready to use
                Log.d(TAG, "Hamster telegram bot is ready to use")

                // show toast: "Hamster bot is ready to use"
                Toast.makeText(
                        this,
                        getString(R.string.hamster_telegram_bot_is_ready),
                        Toast.LENGTH_SHORT
                    )
                    .show()

                val intent = result.data
                val data = intent?.getStringExtra(HamsterBotActivity.EXTRA_URL)
                Log.d("MainActivity", "onDataLoaded: $data")

                if (data == null) {
                    Toast.makeText(this, getString(R.string.failed_get_url), Toast.LENGTH_SHORT)
                        .show()
                    return@registerForActivityResult
                }

                val webViewIntent = Intent(this, WebViewActivity::class.java)
                webViewIntent.putExtra(WebViewActivity.EXTRA_URL, data)
                webviewLauncher.launch(webViewIntent)
            }
        }

    private val webviewLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                val data = intent?.getStringExtra(WebViewActivity.AUTH_PAYLOAD)
                Log.d("MainActivity", "auth payload: $data")

                // show toast: try to login to hamster please wait
                Toast.makeText(this, getString(R.string.login_to_hamster), Toast.LENGTH_SHORT)
                    .show()

                // create a non-cancelable dialog to show the progress
                val dialog = ProgressDialog(this)
                dialog.setMessage(getString(R.string.login_to_hamster))
                dialog.setCancelable(false)
                dialog.show()

                data?.let {
                    val currentAccount = PreferencesHelper.getCurrentAccount()
                    val preferencesHelper = PreferencesHelper(this, currentAccount)
                    val hamsterAPI = HamsterAPI(preferencesHelper)
                    lifecycleScope.launch {
                        if (hamsterAPI.authByTelegramWebapp(data)) {
                            onBeforeAuthenticate()
                        } else {
                            Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.failed_to_authenticate_in_hamster_api),
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                        dialog.dismiss()
                    }
                }
            } else {
                Log.d("MainActivity", "onDataLoaded: cancelled")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferencesHelper.incrementAppLaunches()

        setContent {
            HamsterKombatBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var showTermsOfService by remember { mutableStateOf(false) }

                    if (!showTermsOfService) {
                        SelectAccountScreen(modifier = Modifier) {
                            PreferencesHelper.saveCurrentAccount(it)

                            val currentAccount = it
                            val preferencesHelper = PreferencesHelper(context, currentAccount)

                            if (preferencesHelper.isTokenExists()) {
                                onBeforeAuthenticate()
                            } else {
                                showTermsOfService = true
                            }
                        }
                    } else {
                        TermsOfServiceScreen {
                            val intent = Intent(context, TelegramActivity::class.java)
                            telegramLauncher.launch(intent)
                        }
                    }
                }
            }
        }
    }

    private fun onBeforeAuthenticate() {
        val intent = Intent(this, HamsterActivity::class.java)
        hamsterLauncher.launch(intent)
        finish()
    }
}

@Composable
fun SelectAccountScreen(modifier: Modifier = Modifier, onAccountSelected: (id: Int) -> Unit) {
    var selectedNumber by remember {
        mutableIntStateOf(PreferencesHelper.getCurrentAccount())
    } // State variable to store selected number

    val numberList = (1..3).toList() // List containing numbers from 0 to 10

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.hamster_kombat_bot))
        Spacer(modifier = Modifier.height(16.dp))

        // show help for user the app can support multiple accounts up to 3
        Text(text = stringResource(R.string.this_app_can_support_up_to_3_accounts))
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.select_your_account_to_continue))
        Spacer(modifier = Modifier.height(16.dp))

        // show select box to select a number from 0 until 10
        numberList.forEach { number ->
            val isSelected = number == selectedNumber
            val backgroundColor =
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
            val textColor =
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

            Button(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = {
                    selectedNumber = number
                    onAccountSelected(number)
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = backgroundColor,
                        contentColor = textColor
                    )
            ) {
                Text(text = stringResource(R.string.account, number))
            }
        }
    }
}

@Composable
fun TermsOfServiceScreen(modifier: Modifier = Modifier, onContinue: () -> Unit) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.terms_of_service))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.terms_of_service_text))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onContinue) { Text(text = stringResource(R.string.continue_)) }
    }
}
