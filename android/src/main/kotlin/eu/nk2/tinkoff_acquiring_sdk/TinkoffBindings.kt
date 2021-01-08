package eu.nk2.tinkoff_acquiring_sdk

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import ru.tinkoff.acquiring.sdk.localization.Language
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.utils.Money
import java.lang.Exception

typealias MethodChannelFunction = suspend (call: MethodCall, result: MethodChannel.Result, delegate: TinkoffAcquiringSdkDelegate, scope: CoroutineScope) -> Unit

fun safe(function: MethodChannelFunction): MethodChannelFunction = { call, result, delegate, scope ->
    try {
        function(call, result, delegate, scope)
    } catch (e: Exception) {
        result.error(TINKOFF_COMMON_STATUS_FATAL_ERROR, e.message, null)
    }
}

private const val TINKOFF_INITIALIZE_ID = "initialize"
private val tinkoffInitialize: MethodChannelFunction = safe { call, result, delegate, scope ->
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
private val tinkoffOpenAttachCardScreen: MethodChannelFunction = safe { call, result, delegate, scope ->
    val methodCallResult = delegate.openAttachCardScreen(
        tinkoffCustomerOptions = call.toTinkoffCustomerOptions(),
        tinkoffFeaturesOptions = call.toTinkoffFeaturesOptions()
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name,
        "error" to methodCallResult.error?.message,
        "cardId" to methodCallResult.cardId
    )) }
}

private const val TINKOFF_OPEN_PAYMENT_SCREEN = "openPaymentScreen"
private val tinkoffOpenPaymentScreen: MethodChannelFunction = safe { call, result, delegate, scope ->
    val methodCallResult = delegate.openPaymentScreen(
        tinkoffOrderOptions = call.toTinkoffOrderOptions(),
        tinkoffCustomerOptions = call.toTinkoffCustomerOptions(),
        tinkoffFeaturesOptions = call.toTinkoffFeaturesOptions()
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name,
        "error" to methodCallResult.error?.message,
        "cardId" to methodCallResult.cardId,
        "paymentId" to methodCallResult.paymentId
    )) }
}

private const val TINKOFF_OPEN_GOOGLE_PAY = "openGooglePay"
private val tinkoffOpenGooglePay: MethodChannelFunction = safe { call, result, delegate, scope ->
    val tinkoffOrderOptions = call.toTinkoffOrderOptions()
    val tinkoffCustomerOptions = call.toTinkoffCustomerOptions()
    val tinkoffFeaturesOptions = call.toTinkoffFeaturesOptions()

    val methodCallResult = delegate.openGooglePay(
        tinkoffOrderOptions, tinkoffCustomerOptions, tinkoffFeaturesOptions
    )

    when(methodCallResult.status) {
        TinkoffAcquiringSdkDelegate.TinkoffAcquiringDelegateOpenGooglePayStatus.RESULT_REOPEN_UI -> {
            val methodCallResult = delegate.openPaymentScreen(
                tinkoffOrderOptions, tinkoffCustomerOptions, tinkoffFeaturesOptions, methodCallResult.paymentState
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
            "paymentId" to methodCallResult.paymentId,
            "rebillId" to methodCallResult.rebillId
        )) }
    }
}

private const val TINKOFF_OPEN_PAYMENT_QR_SCREEN = "openPaymentQrScreen"
private val tinkoffOpenPaymentQrScreen: MethodChannelFunction = safe { call, result, delegate, scope ->
    val methodCallResult = delegate.openPaymentQrScreen(
        tinkoffFeaturesOptions = call.toTinkoffFeaturesOptions()
    )

    scope.doOnMain { result.success(mapOf(
        "status" to methodCallResult.status.name,
        "error" to methodCallResult.error?.message
    )) }
}

private const val TINKOFF_OPEN_SAVED_CARDS_SCREEN = "openSavedCardsScreen"
private val tinkoffOpenSavedCardsScreen: MethodChannelFunction = safe { call, result, delegate, scope ->
    val methodCallResult = delegate.openSavedCardsScreen(
        tinkoffCustomerOptions = call.toTinkoffCustomerOptions(),
        tinkoffFeaturesOptions = call.toTinkoffFeaturesOptions()
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