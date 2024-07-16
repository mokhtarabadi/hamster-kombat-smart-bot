package com.example.hamsterkombatbot.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hamsterkombatbot.MainApplication
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.helper.PreferencesHelper
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.*

class TelegramViewModel(application: Application) :
    AndroidViewModel(application), Client.ResultHandler, Client.ExceptionHandler {

    companion object {
        private const val TAG = "TelegramViewModel"
    }

    private val accountID = PreferencesHelper.getCurrentAccount()

    private val _state = MutableLiveData<AuthorizationState>(AuthorizationState.Initial)
    val state: LiveData<AuthorizationState> = _state

    private val _hamsterState = MutableLiveData<HamsterState>(HamsterState.Initial)
    val hamsterState: LiveData<HamsterState> = _hamsterState

    private val _isAuthenticationSuccessful = MutableLiveData<Boolean>(false)
    val isAuthenticationSuccessful: LiveData<Boolean> = _isAuthenticationSuccessful

    private val _phoneNumber = MutableLiveData<String>("")
    val phoneNumber: LiveData<String> = _phoneNumber

    private lateinit var client: Client

    override fun onResult(p0: Object?) {
        if (p0 is UpdateAuthorizationState) {
            p0.authorizationState.let {
                when (it) {
                    is AuthorizationStateWaitTdlibParameters -> {
                        val request = SetTdlibParameters()
                        request.databaseDirectory =
                            getApplication<Application>().filesDir.absolutePath +
                                "/tdlib_${accountID}"
                        request.useMessageDatabase = true
                        request.useSecretChats = true
                        request.apiId = 94575
                        request.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"
                        request.systemLanguageCode = "en"
                        request.deviceModel = "Desktop"
                        request.applicationVersion = "1.0"
                        client.send(request, this)
                    }
                    is AuthorizationStateWaitPhoneNumber -> {
                        viewModelScope.launch {
                            _state.value = AuthorizationState.WaitForPhoneNumber
                        }
                    }
                    is AuthorizationStateWaitOtherDeviceConfirmation -> {
                        viewModelScope.launch {
                            _state.value =
                                AuthorizationState.WaitForOtherDeviceConfirmation(it.link)
                        }
                    }
                    is AuthorizationStateWaitEmailAddress -> {
                        viewModelScope.launch {
                            _state.value = AuthorizationState.WaitForEmailAddress
                        }
                    }
                    is AuthorizationStateWaitEmailCode -> {
                        viewModelScope.launch { _state.value = AuthorizationState.WaitForEmailCode }
                    }
                    is AuthorizationStateWaitCode -> {
                        viewModelScope.launch { _state.value = AuthorizationState.WaitForCode }
                    }
                    is AuthorizationStateWaitRegistration -> {
                        viewModelScope.launch {
                            _state.value = AuthorizationState.WaitForRegistration
                        }
                    }
                    is AuthorizationStateWaitPassword -> {
                        viewModelScope.launch { _state.value = AuthorizationState.WaitForPassword }
                    }
                    is AuthorizationStateReady -> {
                        viewModelScope.launch {
                            _state.value = AuthorizationState.Ready
                            _isAuthenticationSuccessful.value = true
                        }
                    }
                    is AuthorizationStateLoggingOut -> {
                        viewModelScope.launch { _state.value = AuthorizationState.LoggingOut }
                    }
                    is AuthorizationStateClosing -> {
                        viewModelScope.launch { _state.value = AuthorizationState.Closing }
                    }
                    is AuthorizationStateClosed -> {
                        viewModelScope.launch { _state.value = AuthorizationState.Closed }
                    }
                    else -> {
                        Log.d(TAG, "Unknown authorization state: $it")
                    }
                }
            }
        } else if (p0 is Error) {
            viewModelScope.launch { _state.value = AuthorizationState.Error(p0.code, p0.message) }
        } else if (p0 is UpdateNewMessage) {
            if (_hamsterState.value is HamsterState.StartCommandSent) {
                val state = _hamsterState.value as HamsterState.StartCommandSent
                val message = p0.message
                if (message.senderId is MessageSenderUser) {
                    val sender = message.senderId as MessageSenderUser
                    if (sender.userId != state.id) {
                        Log.d(TAG, "not for hamsterbot: ${message.id}")
                        return
                    }

                    viewModelScope.launch {
                        _hamsterState.value = HamsterState.StartMessageReceived
                    }

                    if (processMessage(message)) {
                        Log.d(TAG, "processMessage: ${message.id}")
                        return
                    }
                }
            }
        } else {
            // Log.d(TAG, "onResult: $p0")
        }
    }

    override fun onException(p0: Throwable?) {
        Log.d(TAG, "onException: $p0")
        viewModelScope.launch {
            _state.value =
                AuthorizationState.Error(
                    0,
                    p0?.message ?: MainApplication.appContext.getString(R.string.unknown_error)
                )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        close()
    }

    fun init() {
        client = Client.create(this, this, this)
        Log.d(TAG, "init")
        _state.value = AuthorizationState.Loading
    }

    fun close() {
        client.send(Close(), this)
        Log.d(TAG, "close")
        _state.value = AuthorizationState.Loading
    }

    fun setPhoneNumber(phoneNumber: String) {
        val request = SetAuthenticationPhoneNumber(phoneNumber, null)
        client.send(request, this)
        _state.value = AuthorizationState.Loading
        Log.d(TAG, "setPhoneNumber: $phoneNumber")
        _phoneNumber.value = phoneNumber
    }

    fun setEmail(email: String) {
        val request = SetAuthenticationEmailAddress(email)
        client.send(request, this)
        _state.value = AuthorizationState.Loading
    }

    fun setEmailCode(code: String) {
        val request = CheckAuthenticationEmailCode(EmailAddressAuthenticationCode(code))
        client.send(request, this)
        _state.value = AuthorizationState.Loading
    }

    fun setCode(code: String) {
        val request = CheckAuthenticationCode(code)
        client.send(request, this)
        _state.value = AuthorizationState.Loading
    }

    fun register(firstName: String, lastName: String) {
        val request = RegisterUser(firstName, lastName, false)
        client.send(request, this)
        _state.value = AuthorizationState.Loading
    }

    fun setPassword(password: String) {
        val request = CheckAuthenticationPassword(password)
        client.send(request, this)
        _state.value = AuthorizationState.Loading
    }

    fun changePhoneNumber() {
        _state.value = AuthorizationState.WaitForPhoneNumber
    }

    fun startFindingHamsterBot() {
        Log.d(TAG, "startFindingHamsterBot")

        val search = SearchPublicChat("hamster_kombat_bot")

        client.send(
            search,
            object : Client.ResultHandler {
                override fun onResult(p0: Object?) {
                    if (p0 is Chat) {
                        Log.d(TAG, "on search result: $p0")
                        viewModelScope.launch { _hamsterState.value = HamsterState.Searched }

                        val userId =
                            if (p0.type is ChatTypePrivate) {
                                val type = p0.type as ChatTypePrivate
                                type.userId
                            } else {
                                Log.d(TAG, "Unknown chat type: $p0")
                                null
                            }

                        p0.lastMessage?.let {
                            if (processMessage(it)) {
                                return
                            }
                        }

                        // send command /start
                        val request = SendBotStartMessage()
                        request.botUserId = userId ?: p0.id
                        request.chatId = p0.id
                        client.send(
                            request,
                            object : Client.ResultHandler {
                                override fun onResult(p0: Object?) {
                                    Log.d(TAG, "on start command result: $p0")

                                    if (p0 is Message) {
                                        val chatId = p0.chatId

                                        viewModelScope.launch {
                                            _hamsterState.value =
                                                HamsterState.StartCommandSent(userId)
                                        }

                                        CoroutineScope(Dispatchers.IO).launch {
                                            delay(TimeUnit.SECONDS.toMillis(10))
                                            viewModelScope.launch {
                                                if (
                                                    hamsterState.value
                                                        is HamsterState.StartCommandSent
                                                ) {
                                                    // need to use fallback url
                                                    val fallbackUrl =
                                                        "https://hamsterkombat.io/clicker"
                                                    _hamsterState.value =
                                                        HamsterState.ButtonUrlExtracted(
                                                            fallbackUrl,
                                                            chatId,
                                                            request.botUserId
                                                        )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    private fun processMessage(message: Message): Boolean {
        Log.d(TAG, "message: ${message.id}")

        message.replyMarkup?.let { replyMarkup ->
            if (replyMarkup is ReplyMarkupInlineKeyboard) {
                val button =
                    replyMarkup.rows
                        .flatMap { it.asIterable() }
                        .firstOrNull { it.type is InlineKeyboardButtonTypeWebApp }
                        ?.type as? InlineKeyboardButtonTypeWebApp

                if (button == null) {
                    viewModelScope.launch {
                        _hamsterState.value = HamsterState.FailedToParseButtons
                    }
                    return false
                }

                Log.d(TAG, "button url: ${button.url}")

                viewModelScope.launch {
                    _hamsterState.value =
                        HamsterState.ButtonUrlExtracted(
                            button.url,
                            message.chatId,
                            (message.senderId as MessageSenderUser).userId
                        )
                }

                return true
            } else {
                Log.d(TAG, "unhandled markup: $replyMarkup")
                viewModelScope.launch { _hamsterState.value = HamsterState.FailedToParseButtons }
                return false
            }
        }

        viewModelScope.launch { _hamsterState.value = HamsterState.FailedToParseButtons }
        return false
    }

    fun tryToOpenWebView() {
        Log.d(TAG, "try to open web view: ${_hamsterState.value}")
        if (_hamsterState.value is HamsterState.ButtonUrlExtracted) {
            val state = _hamsterState.value as HamsterState.ButtonUrlExtracted

            val request = OpenWebApp()
            request.chatId = state.chatID
            request.botUserId = state.userID
            request.url = state.url
            request.applicationName = "android"

            client.send(
                request,
                object : Client.ResultHandler {
                    override fun onResult(p0: Object?) {
                        Log.d(TAG, "on web view result: $p0")

                        if (p0 is WebAppInfo) {
                            val info = p0
                            Log.d(TAG, "web app info: $info")

                            viewModelScope.launch {
                                _hamsterState.value = HamsterState.WebViewUrl(info.url)
                            }
                        }
                    }
                }
            )
        }
    }

    sealed class HamsterState {
        data object Initial : HamsterState()

        data object Searched : HamsterState()

        data class StartCommandSent(val id: Long?) : HamsterState()

        data object StartMessageReceived : HamsterState()

        data class ButtonUrlExtracted(val url: String, val chatID: Long, val userID: Long) :
            HamsterState()

        data object FailedToParseButtons : HamsterState()

        data class WebViewUrl(val url: String) : HamsterState()
    }

    sealed class AuthorizationState {
        data object Initial : AuthorizationState()

        data object WaitForPhoneNumber : AuthorizationState()

        data class WaitForOtherDeviceConfirmation(val link: String) : AuthorizationState()

        data object WaitForEmailAddress : AuthorizationState()

        data object WaitForEmailCode : AuthorizationState()

        data object WaitForCode : AuthorizationState()

        data object WaitForRegistration : AuthorizationState()

        data object WaitForPassword : AuthorizationState()

        data object Ready : AuthorizationState()

        data object LoggingOut : AuthorizationState()

        data object Closing : AuthorizationState()

        data object Closed : AuthorizationState()

        data class Error(val code: Int, val error: String?) : AuthorizationState()

        data object Loading : AuthorizationState()
    }
}
