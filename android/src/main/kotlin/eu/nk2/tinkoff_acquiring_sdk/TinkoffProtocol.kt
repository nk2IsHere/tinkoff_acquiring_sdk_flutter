package eu.nk2.tinkoff_acquiring_sdk

import io.flutter.plugin.common.MethodCall
import ru.tinkoff.acquiring.sdk.localization.AsdkSource
import ru.tinkoff.acquiring.sdk.localization.Language
import ru.tinkoff.acquiring.sdk.models.AsdkState
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.models.options.CustomerOptions
import ru.tinkoff.acquiring.sdk.models.options.FeaturesOptions
import ru.tinkoff.acquiring.sdk.models.options.OrderOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.AttachCardOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.utils.Money
import ru.tinkoff.cardio.CameraCardIOScanner

const val TINKOFF_COMMON_STATUS_FATAL_ERROR = "FATAL_ERROR"

data class TinkoffOrderOptions(
    val orderId: String,
    val money: Money,
    val title: String,
    val description: String,
    val recurrentPayment: Boolean
)

fun MethodCall.toTinkoffOrderOptions() = TinkoffOrderOptions(
    orderId = this.argument("orderId") ?: error("orderId is required in openPaymentScreen method"),
    title = this.argument("title") ?: error("title is required in openPaymentScreen method"),
    description = this.argument("description") ?: error("description is required in openPaymentScreen method"),
    money = Money.Companion.ofRubles(this.argument<Double>("money") ?: error("money is required in openPaymentScreen method")),
    recurrentPayment = this.argument("recurrentPayment") ?: false
)

fun TinkoffOrderOptions.toTinkoff(): OrderOptions {
    val options = OrderOptions()
    options.orderId = orderId
    options.amount = money
    options.title = title
    options.description = description
    options.recurrentPayment = recurrentPayment

    return options
}

data class TinkoffCustomerOptions(
    val customerId: String,
    val checkType: CheckType,
    val email: String?
)

fun MethodCall.toTinkoffCustomerOptions() = TinkoffCustomerOptions(
    customerId = this.argument("customerId") ?: error("customerId is required in openPaymentScreen method"),
    checkType = this.argument<String>("checkType")?.let { CheckType.valueOf(it) } ?: CheckType.NO,
    email = this.argument("email")
)

fun TinkoffCustomerOptions.toTinkoff(): CustomerOptions {
    val options = CustomerOptions()
    options.customerKey = customerId
    options.checkType = checkType.toString()
    options.email = email

    return options
}

data class TinkoffFeaturesOptions(
    val enableSecureKeyboard: Boolean,
    val enableCameraCardScanner: Boolean,
    val darkThemeMode: DarkThemeMode,
    val language: Language
)

fun MethodCall.toTinkoffFeaturesOptions() = TinkoffFeaturesOptions(
    enableSecureKeyboard = this.argument("enableSecureKeyboard") ?: false,
    enableCameraCardScanner = this.argument("enableCameraCardScanner") ?: false,
    darkThemeMode = this.argument<String>("darkThemeMode")?.let { DarkThemeMode.valueOf(it) } ?: DarkThemeMode.AUTO,
    language = this.argument<String>("language")?.let { Language.valueOf(it) } ?: error("language is required in openPaymentScreen method")
)

fun TinkoffFeaturesOptions.toTinkoff(): FeaturesOptions {
    val options = FeaturesOptions()
    options.useSecureKeyboard = enableSecureKeyboard
    if(enableCameraCardScanner) options.cameraCardScanner = CameraCardIOScanner()
    options.darkThemeMode = darkThemeMode
    options.localizationSource = AsdkSource(language)

    return options
}

fun makeTinkoffPaymentOptions(
    orderOptions: TinkoffOrderOptions,
    customerOptions: TinkoffCustomerOptions,
    featuresOptions: TinkoffFeaturesOptions,
    paymentState: AsdkState? = null
) = PaymentOptions().apply {
    order = orderOptions.toTinkoff()
    customer = customerOptions.toTinkoff()
    features = featuresOptions.toTinkoff()
    if(paymentState != null) asdkState = paymentState
}

fun makeTinkoffAttachCardOptions(
    customerOptions: TinkoffCustomerOptions,
    featuresOptions: TinkoffFeaturesOptions
) = AttachCardOptions().apply {
    customer = customerOptions.toTinkoff()
    features = featuresOptions.toTinkoff()
}

fun makeTinkoffSavedCardsOptions(
    customerOptions: TinkoffCustomerOptions,
    featuresOptions: TinkoffFeaturesOptions
) = SavedCardsOptions().apply {
    customer = customerOptions.toTinkoff()
    features = featuresOptions.toTinkoff()
}
