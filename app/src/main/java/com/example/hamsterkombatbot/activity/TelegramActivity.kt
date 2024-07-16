package com.example.hamsterkombatbot.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.helper.PreferencesHelper
import com.example.hamsterkombatbot.ui.theme.HamsterKombatBotTheme
import com.example.hamsterkombatbot.viewmodel.TelegramViewModel

class TelegramActivity : ComponentActivity() {
    private val telegramViewModel: TelegramViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HamsterKombatBotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TelegramAuthScreen(telegramViewModel, Modifier) {
                        // finish activity with a result
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun TelegramAuthScreen(viewModel: TelegramViewModel, modifier: Modifier, onAuthDone: () -> Unit) {
    val authenticationState by viewModel.state.observeAsState()
    val counter by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    val currentAccount = PreferencesHelper.getCurrentAccount()
    val preferencesHelper = PreferencesHelper(context, currentAccount)

    LaunchedEffect(null) { viewModel.init() }

    when (authenticationState) {
        TelegramViewModel.AuthorizationState.Initial -> {
            InitializeTelegramScreen(modifier = modifier)
        }
        TelegramViewModel.AuthorizationState.WaitForCode -> {
            Column {
                // resend button
                Button(
                    modifier =
                        modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp).fillMaxWidth(),
                    onClick = { viewModel.changePhoneNumber() }
                ) {
                    Text(text = stringResource(R.string.change_phone_number))
                }
                Spacer(modifier = modifier.height(10.dp))
                InputScreen(
                    modifier = modifier,
                    inputLabel = stringResource(R.string.enter_code),
                    buttonText = stringResource(R.string.login),
                    inputType = KeyboardType.Number,
                    hintText =
                        stringResource(
                            R.string.a_code_has_been_sent_to,
                            viewModel.phoneNumber.value
                                ?: stringResource(R.string.failed_to_get_phone_number)
                        ),
                ) {
                    viewModel.setCode(it)
                }
            }
        }
        TelegramViewModel.AuthorizationState.WaitForEmailAddress -> {
            InputScreen(
                modifier = modifier,
                inputLabel = stringResource(R.string.enter_the_email_address),
                buttonText = stringResource(id = R.string.continue_),
                inputType = KeyboardType.Email
            ) {
                viewModel.setEmail(it)
            }
        }
        TelegramViewModel.AuthorizationState.WaitForEmailCode -> {
            InputScreen(
                modifier = modifier,
                inputLabel = stringResource(R.string.enter_email_code),
                buttonText = stringResource(R.string.submit),
                inputType = KeyboardType.Number,
                hintText = stringResource(R.string.email_code_description)
            ) {
                viewModel.setEmailCode(it)
            }
        }
        is TelegramViewModel.AuthorizationState.WaitForOtherDeviceConfirmation -> {
            val state =
                authenticationState
                    as TelegramViewModel.AuthorizationState.WaitForOtherDeviceConfirmation
            VerifyOtherDeviceScreen(modifier = modifier, link = state.link)
        }
        TelegramViewModel.AuthorizationState.WaitForPassword -> {
            InputScreen(
                modifier = modifier,
                inputLabel = stringResource(R.string.enter_password),
                buttonText = stringResource(id = R.string.submit),
                inputType = KeyboardType.Password,
                hintText = stringResource(R.string.password_description)
            ) {
                viewModel.setPassword(it)
            }
        }
        TelegramViewModel.AuthorizationState.WaitForPhoneNumber -> {
            Column {
                Text(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    text = stringResource(R.string.enter_phone_description)
                )
                Spacer(modifier = modifier.height(10.dp))
                InputScreen(
                    modifier = modifier,
                    inputLabel = stringResource(R.string.enter_phone_number),
                    buttonText = stringResource(id = R.string.continue_),
                    inputType = KeyboardType.Phone,
                    hintText = stringResource(R.string.phone_number_description)
                ) {
                    viewModel.setPhoneNumber(it)
                }
            }
        }
        TelegramViewModel.AuthorizationState.WaitForRegistration -> {
            RegisterScreen(
                modifier = modifier,
                onButtonClick = { firstName, lastName -> viewModel.register(firstName, lastName) }
            )
        }
        is TelegramViewModel.AuthorizationState.Error -> {
            val state = authenticationState as TelegramViewModel.AuthorizationState.Error
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.telegram_error,
                            state.code,
                            state.error ?: stringResource(R.string.unknown)
                        )
                )
                Spacer(modifier = Modifier.height(10.dp))
                // retry button
                Button(onClick = { viewModel.close() }) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
        TelegramViewModel.AuthorizationState.Loading -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 5.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(id = R.string.loading))
            }
        }
        TelegramViewModel.AuthorizationState.Ready -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = stringResource(id = R.string.telegram_is_ready))

                if (counter.inc() == 1) {
                    viewModel.close()
                }
            }
        }
        TelegramViewModel.AuthorizationState.Closing -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 5.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = stringResource(R.string.closing))
            }
        }
        TelegramViewModel.AuthorizationState.Closed -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = stringResource(R.string.closed))

                if (viewModel.isAuthenticationSuccessful.value == true) {
                    onAuthDone()
                } else {
                    viewModel.init()
                }
            }
        }
        else -> {
            Text(stringResource(id = R.string.unknown_state))
        }
    }
}

@Composable
fun InitializeTelegramScreen(
    modifier: Modifier,
    circularProgressIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = circularProgressIndicatorColor,
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.initializing_telegram),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

@Composable
fun InputScreen(
    modifier: Modifier,
    inputType: KeyboardType = KeyboardType.Text,
    inputLabel: String,
    buttonText: String,
    hintText: String? = null,
    onButtonClick: (value: String) -> Unit,
) {
    var inputText by remember { mutableStateOf("") }
    val isInputEmpty = inputText.isBlank()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (hintText != null) {
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = inputLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = inputType),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (!isInputEmpty) {
                    onButtonClick(inputText)
                    inputText = ""
                }
            },
            enabled = !isInputEmpty,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = buttonText)
        }
    }
}

@Composable
fun RegisterScreen(
    modifier: Modifier,
    onButtonClick: (fistName: String, lastName: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val isFirstNameEmpty = firstName.isBlank()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.first_name)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.last_name)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (!isFirstNameEmpty) {
                    onButtonClick(firstName, lastName)
                    firstName = ""
                    lastName = ""
                }
            },
            enabled = !isFirstNameEmpty,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.register))
        }
    }
}

@Composable
fun VerifyOtherDeviceScreen(
    modifier: Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    link: String,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.verify_telegram_link),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(intent)
            }
        ) {
            Text(text = stringResource(R.string.verify))
        }
    }
}
