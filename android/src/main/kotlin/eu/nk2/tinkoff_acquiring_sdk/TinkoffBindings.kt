package eu.nk2.tinkoff_acquiring_sdk

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import ru.tinkoff.acquiring.sdk.localization.Language
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.utils.Money

typealias MethodChannelFunction = suspend (call: MethodCall, result: MethodChannel.Result, delegate: TinkoffAcquiringSdkDelegate, scope: CoroutineScope) -> Unit

private const val TINKOFF_INITIALIZE_ID = "initialize"
private val tinkoffInitialize: MethodChannelFunction = { call, result, delegate, scope ->
    val methodCallResult = delegate.initialize(
        enableDebug = call.argument("enableDebug") ?: false,
        terminalKey = call.argument("terminalKey") ?: error("terminalKey is required in initialize method"),
        password = call.argument("password") ?: error("password is required in initialize method"),
        publicKey = call.argument("publicKey") ?: error("publicKey is required in initialize method"),
        enableGooglePay = call.argument("enableGooglePay") ?: false,
        requireAddress = call.argument("requireAddress") ?: false,
        requirePhone = call.argument("requirePhone") ?: false
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name
    )) }
}

private const val TINKOFF_OPEN_ATTACH_SCREEN = "openAttachCardScreen"
private val tinkoffOpenAttachCardScreen: MethodChannelFunction = { call, result, delegate, scope ->
    val methodCallResult = delegate.openAttachCardScreen(
        customerId = call.argument("customerId") ?: error("customerId is required in openAttachCardScreen method"),
        checkType = call.argument<String>("checkType")?.let { CheckType.valueOf(it) } ?: CheckType.NO,
        email = call.argument("email"),
        enableSecureKeyboard = call.argument("enableSecureKeyboard") ?: false,
        enableCameraCardScanner = call.argument("enableCameraCardScanner") ?: false,
        darkThemeMode = call.argument<String>("darkThemeMode")?.let { DarkThemeMode.valueOf(it) } ?: DarkThemeMode.AUTO,
        language = call.argument<String>("language")?.let { Language.valueOf(it) } ?: error("language is required in openAttachCardScreen method")
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name,
        "error" to methodCallResult.error?.message,
        "cardId" to methodCallResult.cardId
    )) }
}

private const val TINKOFF_OPEN_PAYMENT_SCREEN = "openPaymentScreen"
private val tinkoffOpenPaymentScreen: MethodChannelFunction = { call, result, delegate, scope ->
    val methodCallResult = delegate.openPaymentScreen(
        orderId = call.argument("orderId") ?: error("orderId is required in openPaymentScreen method"),
        title = call.argument("title") ?: error("title is required in openPaymentScreen method"),
        description = call.argument("description") ?: error("description is required in openPaymentScreen method"),
        money = Money.Companion.ofRubles(call.argument<Double>("money") ?: error("money is required in openPaymentScreen method")),
        customerId = call.argument("customerId") ?: error("customerId is required in openPaymentScreen method"),
        recurrentPayment = call.argument("recurrentPayment") ?: false,
        checkType = call.argument<String>("checkType")?.let { CheckType.valueOf(it) } ?: CheckType.NO,
        email = call.argument("email"),
        enableSecureKeyboard = call.argument("enableSecureKeyboard") ?: false,
        enableCameraCardScanner = call.argument("enableCameraCardScanner") ?: false,
        darkThemeMode = call.argument<String>("darkThemeMode")?.let { DarkThemeMode.valueOf(it) } ?: DarkThemeMode.AUTO,
        language = call.argument<String>("language")?.let { Language.valueOf(it) } ?: error("language is required in openPaymentScreen method")
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name,
        "error" to methodCallResult.error?.message,
        "cardId" to methodCallResult.cardId,
        "paymentId" to methodCallResult.paymentId
    )) }
}

private const val TINKOFF_OPEN_GOOGLE_PAY = "openGooglePay"
private val tinkoffOpenGooglePay: MethodChannelFunction = { call, result, delegate, scope ->
    val orderId: String = call.argument("orderId") ?: error("orderId is required in openPaymentScreen method")
    val title: String = call.argument("title") ?: error("title is required in openPaymentScreen method")
    val description: String = call.argument("description") ?: error("description is required in openPaymentScreen method")
    val money: Money = Money.Companion.ofRubles(call.argument<Double>("money") ?: error("money is required in openPaymentScreen method"))
    val customerId: String = call.argument("customerId") ?: error("customerId is required in openPaymentScreen method")
    val recurrentPayment: Boolean = call.argument("recurrentPayment") ?: false
    val checkType: CheckType = call.argument<String>("checkType")?.let { CheckType.valueOf(it) } ?: CheckType.NO
    val email: String? = call.argument("email")
    val enableSecureKeyboard: Boolean = call.argument("enableSecureKeyboard") ?: false
    val enableCameraCardScanner: Boolean = call.argument("enableCameraCardScanner") ?: false
    val darkThemeMode: DarkThemeMode = call.argument<String>("darkThemeMode")?.let { DarkThemeMode.valueOf(it) } ?: DarkThemeMode.AUTO
    val language: Language = call.argument<String>("language")?.let { Language.valueOf(it) } ?: error("language is required in openPaymentScreen method")

    val methodCallResult = delegate.openGooglePay(
        orderId, money, title, description, customerId, recurrentPayment,
        checkType, email, enableSecureKeyboard, enableCameraCardScanner, darkThemeMode,
        language
    )

    when(methodCallResult.status) {
        TinkoffAcquiringSdkDelegate.TinkoffAcquiringDelegateOpenGooglePayStatus.RESULT_REOPEN_UI -> {
            val methodCallResult = delegate.openPaymentScreen(
                orderId, money, title, description, customerId, recurrentPayment,
                checkType, email, enableSecureKeyboard, enableCameraCardScanner, darkThemeMode,
                methodCallResult.paymentState, language
            )

            scope.doOnMain { result.success(mapOf(
                "status" to methodCallResult.status.name,
                "error" to methodCallResult.error?.message,
                "cardId" to methodCallResult.cardId,
                "paymentId" to methodCallResult.paymentId
            )) }
        }
        else -> scope.doOnMain { result.success(mapOf(
            "status" to methodCallResult.status.name,
            "error" to methodCallResult.error?.message,
            "cardId" to methodCallResult.cardId,
            "paymentId" to methodCallResult.paymentId
        )) }
    }
}

private const val TINKOFF_OPEN_PAYMENT_QR_SCREEN = "openPaymentQrScreen"
private val tinkoffOpenPaymentQrScreen: MethodChannelFunction = { call, result, delegate, scope ->
    val methodCallResult = delegate.openPaymentQrScreen(
        enableSecureKeyboard = call.argument("enableSecureKeyboard") ?: false,
        enableCameraCardScanner = call.argument("enableCameraCardScanner") ?: false,
        darkThemeMode = call.argument<String>("darkThemeMode")?.let { DarkThemeMode.valueOf(it) } ?: DarkThemeMode.AUTO,
        language = call.argument<String>("language")?.let { Language.valueOf(it) } ?: error("language is required in openPaymentQrScreen method")
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name,
        "error" to methodCallResult.error?.message
    )) }
}

private const val TINKOFF_OPEN_SAVED_CARDS_SCREEN = "openSavedCardsScreen"
private val tinkoffOpenSavedCardsScreen: MethodChannelFunction = { call, result, delegate, scope ->
    val methodCallResult = delegate.openSavedCardsScreen(
        customerId = call.argument("customerId") ?: error("customerId is required in openPaymentScreen method"),
        checkType = call.argument<String>("checkType")?.let { CheckType.valueOf(it) } ?: CheckType.NO,
        email = call.argument("email"),
        enableSecureKeyboard = call.argument("enableSecureKeyboard") ?: false,
        enableCameraCardScanner = call.argument("enableCameraCardScanner") ?: false,
        darkThemeMode = call.argument<String>("darkThemeMode")?.let { DarkThemeMode.valueOf(it) } ?: DarkThemeMode.AUTO,
        language = call.argument<String>("language")?.let { Language.valueOf(it) } ?: error("language is required in openSavedCardsScreen method")
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name,
        "error" to methodCallResult.error?.message
    )) }
}

val tinkoffMethodBundle = mapOf(
    TINKOFF_INITIALIZE_ID to tinkoffInitialize,
    TINKOFF_OPEN_ATTACH_SCREEN to tinkoffOpenAttachCardScreen,
    TINKOFF_OPEN_PAYMENT_SCREEN to tinkoffOpenPaymentScreen,
    TINKOFF_OPEN_GOOGLE_PAY to tinkoffOpenGooglePay,
    TINKOFF_OPEN_PAYMENT_QR_SCREEN to tinkoffOpenPaymentQrScreen,
    TINKOFF_OPEN_SAVED_CARDS_SCREEN to tinkoffOpenSavedCardsScreen
)