package com.example.hamsterkombatbot.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.api.LicenseAPI
import com.example.hamsterkombatbot.helper.PreferencesHelper
import com.example.hamsterkombatbot.repository.HamsterRepository
import com.example.hamsterkombatbot.ui.theme.HamsterKombatBotTheme
import com.example.hamsterkombatbot.viewmodel.HamsterViewModel
import com.example.hamsterkombatbot.viewmodel.LicenseViewModel
import com.example.hamsterkombatbot.viewmodel.ServiceViewModel

class HamsterActivity : ComponentActivity() {
    companion object {
        const val TAG = "HamsterActivity"
    }

    private val hamsterViewModel: HamsterViewModel by viewModels()
    private val licenseViewModel: LicenseViewModel by viewModels()
    private val serviceViewModel: ServiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesHelper = PreferencesHelper(this, PreferencesHelper.getCurrentAccount())

        setContent {
            HamsterKombatBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(
                        modifier = Modifier,
                        onClickExit = { finish() },
                        onClickSettings = {
                            val intent = Intent(this, SettingsActivity::class.java)
                            startActivity(intent)
                        },
                        hamsterViewModel = hamsterViewModel,
                        licenseViewModel = licenseViewModel,
                        serviceViewModel = serviceViewModel,
                        onClearClicked = {
                            preferencesHelper.clearToken()
                            finish()
                        }
                    )
                }
            }
        }

        serviceViewModel.registerReceiver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceViewModel.unregisterReceiver(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        serviceViewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

@Composable
fun Greeting(
    modifier: Modifier,
    onClickExit: () -> Unit,
    onClickSettings: () -> Unit,
    hamsterViewModel: HamsterViewModel,
    licenseViewModel: LicenseViewModel,
    serviceViewModel: ServiceViewModel,
    onClearClicked: () -> Unit,
) {
    val licenseState by licenseViewModel.licenseState.observeAsState()
    if (licenseState !is LicenseViewModel.LicenseState.Validated) {
        LicenseScreen(
            licenseViewModel = licenseViewModel,
            modifier = modifier,
            onClickExit = { onClickExit() }
        )
    } else {
        HamsterScreen(
            hamsterViewModel = hamsterViewModel,
            serviceViewModel = serviceViewModel,
            modifier = modifier,
            onClickExit = { onClickExit() },
            onClickSettings = { onClickSettings() },
            onClearClicked = { onClearClicked() }
        )
    }
}

@Composable
fun LicenseScreen(licenseViewModel: LicenseViewModel, modifier: Modifier, onClickExit: () -> Unit) {
    val licenseState by licenseViewModel.licenseState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) { licenseViewModel.init() }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (licenseState) {
            is LicenseViewModel.LicenseState.Activated -> {
                val state = licenseState as LicenseViewModel.LicenseState.Activated
                Text(text = stringResource(R.string.license_valid))
                Spacer(modifier = Modifier.height(8.dp))
                // copy token to clipboard
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.token,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(R.string.activation_token)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { licenseViewModel.validateLicense(state.token) }) {
                    Text(text = stringResource(R.string.validate))
                }
            }
            is LicenseViewModel.LicenseState.Error -> {
                val state = licenseState as LicenseViewModel.LicenseState.Error
                Text(text = stringResource(R.string.error, state.message))
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { onClickExit() }) {
                        Text(text = stringResource(R.string.exit))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(onClick = { licenseViewModel.removeToken() }) {
                        Text(text = stringResource(R.string.enter_new_license))
                    }
                }
            }
            LicenseViewModel.LicenseState.Loading -> {
                Text(text = stringResource(R.string.loading))
            }
            is LicenseViewModel.LicenseState.Validated -> {
                Text(text = stringResource(R.string.license_validation_done))
            }
            is LicenseViewModel.LicenseState.Initial -> {
                Text(text = stringResource(R.string.check_license))
            }
            LicenseViewModel.LicenseState.EnterLicense -> {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(R.string.need_license))
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        // show APP_LICENSE_URL url
                        val intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(LicenseAPI.APP_LICENSE_URL))
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = stringResource(R.string.buy_license))
                }

                Spacer(modifier = Modifier.height(10.dp))

                InputScreen(
                    modifier = modifier,
                    inputLabel = stringResource(R.string.enter_license_code),
                    buttonText = stringResource(R.string.activate),
                    inputType = KeyboardType.Text,
                    hintText = stringResource(R.string.license_description)
                ) {
                    licenseViewModel.activateLicense(it)
                }
            }
            else -> {
                Text(text = stringResource(R.string.unknown_state))
            }
        }
    }
}

@Composable
fun HamsterScreen(
    hamsterViewModel: HamsterViewModel,
    serviceViewModel: ServiceViewModel,
    modifier: Modifier,
    onClickSettings: () -> Unit,
    onClickExit: () -> Unit,
    onClearClicked: () -> Unit,
) {
    val hamsterState by hamsterViewModel.hamsterState.observeAsState()
    val hamsterEvent by hamsterViewModel.hamsterEvent.observeAsState()
    val comboState by hamsterViewModel.currentCombo.observeAsState()
    val serviceState by serviceViewModel.serviceState.observeAsState()

    val context = LocalContext.current

    LaunchedEffect(null) {
        hamsterViewModel.init()
        serviceViewModel.init()
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.welcome_description))

        Row(modifier = Modifier.wrapContentWidth()) {
            Button(
                onClick = onClickSettings,
            ) {
                Text(text = stringResource(R.string.settings))
            }
            Spacer(modifier = Modifier.size(2.dp))
            Button(onClick = onClickExit) { Text(text = stringResource(id = R.string.exit)) }
            Spacer(modifier = Modifier.size(2.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LicenseAPI.APP_UPDATE_URL))
                    context.startActivity(intent)
                }
            ) {
                Text(text = stringResource(R.string.update_app))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row {
            Text(text = stringResource(R.string.current_api_status))
            when (hamsterState) {
                is HamsterRepository.HamsterState.BoostsForBuy -> {
                    Text(text = stringResource(R.string.get_list_boost_items))
                }
                is HamsterRepository.HamsterState.Error -> {
                    val error = hamsterState as HamsterRepository.HamsterState.Error
                    Row {
                        Text(text = error.message)
                        Button(onClick = { hamsterViewModel.init() }) {
                            Text(text = stringResource(R.string.re_initial))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text(text = stringResource(R.string.hamster_many_errors))
                        Button(onClick = { onClearClicked() }) {
                            Text(text = stringResource(R.string.clear))
                        }
                    }
                }
                is HamsterRepository.HamsterState.GetConfig -> {
                    Text(text = stringResource(R.string.get_config))
                }
                is HamsterRepository.HamsterState.GetUserDetails -> {
                    Text(text = stringResource(R.string.get_user_details))
                }
                HamsterRepository.HamsterState.Initial -> {
                    Text(text = stringResource(R.string.initial))
                }
                is HamsterRepository.HamsterState.ListTasks -> {
                    Text(text = stringResource(R.string.get_list_of_tasks))
                }
                HamsterRepository.HamsterState.Loading -> {
                    Text(text = stringResource(id = R.string.loading))
                }
                is HamsterRepository.HamsterState.Sync -> {
                    Text(text = stringResource(R.string.synced))
                }
                is HamsterRepository.HamsterState.UpgradesForBuy -> {
                    Text(text = stringResource(R.string.get_upgrades_list))
                }
                is HamsterRepository.HamsterState.Ready -> {
                    Text(text = stringResource(R.string.hamster_ready))
                }
                else -> {
                    Text(text = stringResource(id = R.string.unknown_state))
                }
            }
        }

        if (hamsterState is HamsterRepository.HamsterState.Ready) {
            Spacer(modifier = Modifier.height(16.dp))
            UserDetailsScreen(modifier = Modifier, hamsterViewModel = hamsterViewModel)

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { hamsterViewModel.refresh() },
                enabled = hamsterEvent !is HamsterRepository.HamsterEvent.Wait
            ) {
                Text(text = stringResource(R.string.refresh))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text(text = stringResource(R.string.current_event))
                when (hamsterEvent) {
                    is HamsterRepository.HamsterEvent.BuyUpgrade -> {
                        Text(text = stringResource(R.string.upgrade_bought))
                    }
                    is HamsterRepository.HamsterEvent.Error -> {
                        val error = hamsterEvent as HamsterRepository.HamsterEvent.Error
                        Text(text = error.message)
                    }
                    HamsterRepository.HamsterEvent.Idle -> {
                        Text(text = stringResource(R.string.idle_state))
                    }
                    HamsterRepository.HamsterEvent.Loading -> {
                        Text(text = stringResource(id = R.string.loading))
                    }
                    is HamsterRepository.HamsterEvent.Tap -> {
                        Text(text = stringResource(R.string.tap_done))
                    }
                    is HamsterRepository.HamsterEvent.BuyBoost -> {
                        Text(text = stringResource(R.string.boost_bought))
                    }
                    is HamsterRepository.HamsterEvent.Wait -> {
                        val tapWait = hamsterEvent as HamsterRepository.HamsterEvent.Wait
                        Text(text = stringResource(R.string.waiting_for_seconds, tapWait.delay))
                    }
                    is HamsterRepository.HamsterEvent.Cooldown -> {
                        val cooldown = hamsterEvent as HamsterRepository.HamsterEvent.Cooldown
                        Text(text = stringResource(R.string.cooldown_wait, cooldown.cooldown))
                    }
                    is HamsterRepository.HamsterEvent.CooldownTooMuch -> {
                        val cooldown =
                            hamsterEvent as HamsterRepository.HamsterEvent.CooldownTooMuch
                        Text(text = stringResource(R.string.cooldown_too_much, cooldown.cooldown))
                    }
                    is HamsterRepository.HamsterEvent.BoostsEmpty -> {
                        Text(text = stringResource(R.string.no_boosts))
                    }
                    is HamsterRepository.HamsterEvent.SelectExchange -> {
                        Text(text = stringResource(R.string.select_exchange))
                    }
                    is HamsterRepository.HamsterEvent.CheckMorseCode -> {
                        Text(text = stringResource(R.string.check_morse_code))
                    }

                    // tasks empty
                    is HamsterRepository.HamsterEvent.TasksEmpty -> {
                        Text(text = stringResource(R.string.no_tasks))
                    }

                    // task done
                    is HamsterRepository.HamsterEvent.CheckTask -> {
                        Text(text = stringResource(R.string.check_task))
                    }

                    // upgrades empty
                    is HamsterRepository.HamsterEvent.UpgradesEmpty -> {
                        Text(text = stringResource(R.string.upgrades_empty))
                    }

                    // balance not enough
                    is HamsterRepository.HamsterEvent.BalanceNotEnough -> {
                        Text(text = stringResource(R.string.balance_not_enough))
                    }

                    // combo claimed
                    is HamsterRepository.HamsterEvent.ComboClaimed -> {
                        Text(text = stringResource(R.string.combo_already_claimed))
                    }

                    // combo upgrades empty
                    is HamsterRepository.HamsterEvent.ComboUpgradesEmpty -> {
                        Text(text = stringResource(R.string.combo_cards_less))
                    }

                    // daily combo claimed
                    is HamsterRepository.HamsterEvent.ClaimDailyCombo -> {
                        Text(text = stringResource(R.string.combo_claimed))
                    }
                    else -> {
                        Text(text = stringResource(id = R.string.unknown_state))
                    }
                }
            }

            // auto tap button
            Button(
                onClick = { hamsterViewModel.autoTapper() },
                enabled = hamsterEvent == HamsterRepository.HamsterEvent.Idle
            ) {
                Text(text = stringResource(R.string.auto_tap))
            }

            // auto boos button
            Button(
                onClick = { hamsterViewModel.autoBoost() },
                enabled = hamsterEvent == HamsterRepository.HamsterEvent.Idle
            ) {
                Text(text = stringResource(R.string.auto_hamster_tasks))
            }

            // smart upgrade button
            Button(
                onClick = { hamsterViewModel.smartUpgrade() },
                enabled = hamsterEvent == HamsterRepository.HamsterEvent.Idle
            ) {
                Text(text = stringResource(R.string.smart_upgrade))
            }

            // claim combo button
            Button(
                onClick = { hamsterViewModel.autoCombo() },
                enabled =
                    hamsterEvent == HamsterRepository.HamsterEvent.Idle &&
                        comboState?.upgradeIds?.size == 3
            ) {
                Text(text = stringResource(R.string.claim_combo))
            }

            // switch to background button
            when (serviceState) {
                ServiceViewModel.ServiceState.Started -> {
                    Text(text = stringResource(R.string.service_started))
                    Toast.makeText(
                            context,
                            stringResource(R.string.service_will_started),
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
                is ServiceViewModel.ServiceState.Error -> {
                    val error = serviceState as ServiceViewModel.ServiceState.Error
                    Text(text = stringResource(R.string.service_error, error.message))
                }
                ServiceViewModel.ServiceState.NotRunning -> {
                    Button(
                        onClick = {
                            serviceViewModel.checkPostNotificationPermission(
                                context as HamsterActivity
                            )
                        },
                        enabled = hamsterEvent == HamsterRepository.HamsterEvent.Idle
                    ) {
                        Text(text = stringResource(R.string.start_background_service))
                    }
                }
                ServiceViewModel.ServiceState.Running -> {
                    Button(
                        onClick = { serviceViewModel.stopService() },
                        enabled = hamsterEvent == HamsterRepository.HamsterEvent.Idle
                    ) {
                        Text(text = stringResource(R.string.stop_background_service))
                    }
                }
                else -> {
                    Text(text = stringResource(R.string.unknown_service_state))
                }
            }
        }
    }
}

@Composable
fun UserDetailsScreen(modifier: Modifier, hamsterViewModel: HamsterViewModel) {
    val user by hamsterViewModel.currentClicker.observeAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        user?.let {
            Text(text = stringResource(R.string.available_taps, it.availableTaps))
            Text(text = stringResource(R.string.balance_coins, it.balanceCoins.toInt()))
            Text(text = stringResource(R.string.earn_per_hour, it.earnPassivePerHour))
            Text(text = stringResource(R.string.earn_per_tap, it.earnPerTap))
            Text(text = stringResource(R.string.level, it.level))
            Text(text = stringResource(R.string.max_taps, it.maxTaps))
        }
    }
}
