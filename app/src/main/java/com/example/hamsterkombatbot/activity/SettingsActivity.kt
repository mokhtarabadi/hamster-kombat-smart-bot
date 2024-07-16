package com.example.hamsterkombatbot.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.helper.PreferencesHelper
import com.example.hamsterkombatbot.ui.theme.HamsterKombatBotTheme

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HamsterKombatBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentAccount = PreferencesHelper.getCurrentAccount()
    val preferencesHelper = PreferencesHelper(context, currentAccount)

    var minDelay by remember { mutableIntStateOf(preferencesHelper.getMinDelay()) }
    var maxDelay by remember { mutableIntStateOf(preferencesHelper.getMaxDelay()) }

    var autoTapMin by remember { mutableIntStateOf(preferencesHelper.getAutoTapMin()) }
    var autoTapMax by remember { mutableIntStateOf(preferencesHelper.getAutoTapMax()) }

    var maxCooldownSeconds by remember {
        mutableIntStateOf(preferencesHelper.getMaxCooldownWaitMinutes())
    }

    var maxPercentageForSmartBuy by remember {
        mutableIntStateOf(preferencesHelper.getBalancePercentagePerSmartBuy())
    }

    var maxPercentageForAutoBoost by remember {
        mutableIntStateOf(preferencesHelper.getBalancePercentagePerAutoBoost())
    }

    var roundDelayBackground by remember {
        mutableIntStateOf(PreferencesHelper.getRoundDelayBackground())
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        // min delay seconds
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.min_delay_seconds)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = minDelay.toString(),
            onValueChange = {
                preferencesHelper.setMinDelay(it.toIntOrNull() ?: 0)
                minDelay = it.toIntOrNull() ?: 0
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // max delay seconds
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.max_delay_seconds)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = maxDelay.toString(),
            onValueChange = {
                preferencesHelper.setMaxDelay(it.toIntOrNull() ?: 0)
                maxDelay = it.toIntOrNull() ?: 0
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // auto tap min
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.auto_tap_min)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = autoTapMin.toString(),
            onValueChange = {
                preferencesHelper.setAutoTapMin(it.toIntOrNull() ?: 0)
                autoTapMin = it.toIntOrNull() ?: 0
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // auto tap max
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.auto_tap_max)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = autoTapMax.toString(),
            onValueChange = {
                preferencesHelper.setAutoTapMax(it.toIntOrNull() ?: 0)
                autoTapMax = it.toIntOrNull() ?: 0
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // max percentage for smart buy
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.max_percentage_for_smart_buy)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = maxPercentageForSmartBuy.toString(),
            onValueChange = {
                preferencesHelper.setBalancePercentagePerSmartBuy(it.toIntOrNull() ?: 0)
                maxPercentageForSmartBuy = it.toIntOrNull() ?: 0
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // max percentage for auto boost
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.max_percentage_for_auto_boost)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = maxPercentageForAutoBoost.toString(),
            onValueChange = {
                preferencesHelper.setBalancePercentagePerAutoBoost(it.toIntOrNull() ?: 0)
                maxPercentageForAutoBoost = it.toIntOrNull() ?: 0
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // round delay background
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.delay_between_background_rounds_minutes))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            value = roundDelayBackground.toString(),
            onValueChange = {
                PreferencesHelper.setRoundDelayBackground(it.toIntOrNull() ?: 0)
                roundDelayBackground = it.toIntOrNull() ?: 0
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // max cooldown seconds
        TextField(
            value = maxCooldownSeconds.toString(),
            onValueChange = {
                maxCooldownSeconds = it.toIntOrNull() ?: 0
                preferencesHelper.setMaxCooldownWaitMinutes(maxCooldownSeconds)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.max_cooldown_wait_minutes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
