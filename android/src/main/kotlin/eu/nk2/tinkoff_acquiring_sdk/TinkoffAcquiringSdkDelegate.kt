package eu.nk2.tinkoff_acquiring_sdk

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.wallet.WalletConstants
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.localization.AsdkSource
import ru.tinkoff.acquiring.sdk.localization.Language
import ru.tinkoff.acquiring.sdk.localization.LocalizationSource
import ru.tinkoff.acquiring.sdk.models.AsdkState
import ru.tinkoff.acquiring.sdk.models.DarkThemeMode
import ru.tinkoff.acquiring.sdk.models.DefaultState
import ru.tinkoff.acquiring.sdk.models.GooglePayParams
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.models.options.FeaturesOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.AttachCardOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.payment.PaymentListenerAdapter
import ru.tinkoff.acquiring.sdk.payment.PaymentProcess
import ru.tinkoff.acquiring.sdk.utils.GooglePayHelper
import ru.tinkoff.acquiring.sdk.utils.Money
import ru.tinkoff.cardio.CameraCardIOScanner
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TinkoffAcquiringSdkDelegate(private val activityDelegate: ActivityDelegate) {

    private var tinkoffAcquiring: TinkoffAcquiring? = null
    private var googlePayHelper: GooglePayHelper? = null


    data class TinkoffAcquiringDelegateInitializeResponse(
        val status: TinkoffAcquiringDelegateInitializeStatus
    )
    enum class TinkoffAcquiringDelegateInitializeStatus { RESULT_OK, GOOGLE_PAY_NOT_AVAILABLE, FLUTTER_NOT_INITIALIZED, PLUGIN_ALREADY_INITIALIZED }
    suspend fun initialize(
        enableDebug: Boolean,
        terminalKey: String,
        password: String,
        publicKey: String,
        enableGooglePay: Boolean,
        requireAddress: Boolean,
        requirePhone: Boolean
    ): TinkoffAcquiringDelegateInitializeResponse {
        if(activityDelegate.activity !is FragmentActivity) error("Plugin cannot be initialized if activity you are using does not extend FlutterFragmentActivity")
        if(activityDelegate.activity == null || activityDelegate.context == null) return TinkoffAcquiringDelegateInitializeResponse(status = TinkoffAcquiringDelegateInitializeStatus.FLUTTER_NOT_INITIALIZED)
        if(tinkoffAcquiring != null || googlePayHelper != null) return TinkoffAcquiringDelegateInitializeResponse(status = TinkoffAcquiringDelegateInitializeStatus.PLUGIN_ALREADY_INITIALIZED)

        tinkoffAcquiring = TinkoffAcquiring(terminalKey, password, publicKey)
        if(enableDebug) {
            AcquiringSdk.isDeveloperMode = true
            AcquiringSdk.isDebug = true
        }

        return if(!enableGooglePay) TinkoffAcquiringDelegateInitializeResponse(status = TinkoffAcquiringDelegateInitializeStatus.RESULT_OK)
        else {
            googlePayHelper = GooglePayHelper(GooglePayParams(
                terminalKey = terminalKey,
                environment = if (enableDebug) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION,
                isAddressRequired = requireAddress,
                isPhoneRequired = requirePhone
            ))

            suspendCoroutine { sink -> googlePayHelper!!.initGooglePay(activityDelegate.context!!) { ready ->
                sink.resume(
                    if(ready) TinkoffAcquiringDelegateInitializeResponse(status = TinkoffAcquiringDelegateInitializeStatus.RESULT_OK)
                    else TinkoffAcquiringDelegateInitializeResponse(status = TinkoffAcquiringDelegateInitializeStatus.GOOGLE_PAY_NOT_AVAILABLE)
                )
            } }
        }
    }

    data class TinkoffAcquiringDelegateOpenAttachScreenResponse(
        val status: TinkoffAcquiringDelegateOpenAttachScreenStatus,
        val error: Throwable? = null,
        val cardId: String? = null
    )
    enum class TinkoffAcquiringDelegateOpenAttachScreenStatus { RESULT_OK, RESULT_CANCELLED, RESULT_NONE, RESULT_ERROR, ERROR_NOT_INITIALIZED, ERROR_NO_ACTIVITY }
    suspend fun openAttachCardScreen(
        tinkoffCustomerOptions: TinkoffCustomerOptions,
        tinkoffFeaturesOptions: TinkoffFeaturesOptions
    ): TinkoffAcquiringDelegateOpenAttachScreenResponse {
        if(tinkoffAcquiring == null) return TinkoffAcquiringDelegateOpenAttachScreenResponse(status = TinkoffAcquiringDelegateOpenAttachScreenStatus.ERROR_NOT_INITIALIZED)

        return activityDelegate.runActivityForResult(
            { activity -> tinkoffAcquiring!!.openAttachCardScreen(
                activity as FragmentActivity,
                makeTinkoffAttachCardOptions(tinkoffCustomerOptions, tinkoffFeaturesOptions),
                TINKOFF_ACQUIRING_OPEN_ATTACH_CARD_SCREEN_REQUEST
            ) },
            TINKOFF_ACQUIRING_OPEN_ATTACH_CARD_SCREEN_REQUEST,
            { resultCode, data -> when(resultCode) {
                Activity.RESULT_OK -> TinkoffAcquiringDelegateOpenAttachScreenResponse(
                    status = TinkoffAcquiringDelegateOpenAttachScreenStatus.RESULT_OK,
                    cardId = data.getStringExtra(TinkoffAcquiring.EXTRA_CARD_ID)
                )
                Activity.RESULT_CANCELED -> TinkoffAcquiringDelegateOpenAttachScreenResponse(
                    status = TinkoffAcquiringDelegateOpenAttachScreenStatus.RESULT_CANCELLED
                )
                TinkoffAcquiring.RESULT_ERROR -> TinkoffAcquiringDelegateOpenAttachScreenResponse(
                    status = TinkoffAcquiringDelegateOpenAttachScreenStatus.RESULT_ERROR,
                    error = data.getSerializableExtra(TinkoffAcquiring.EXTRA_ERROR) as Throwable
                )
                else -> TinkoffAcquiringDelegateOpenAttachScreenResponse(
                    status = TinkoffAcquiringDelegateOpenAttachScreenStatus.RESULT_NONE
                )
            } }
        ) ?: TinkoffAcquiringDelegateOpenAttachScreenResponse(status = TinkoffAcquiringDelegateOpenAttachScreenStatus.ERROR_NO_ACTIVITY)
    }

    data class TinkoffAcquiringDelegateOpenPaymentScreenResponse(
        val status: TinkoffAcquiringDelegateOpenPaymentScreenStatus,
        val error: Throwable? = null,
        val cardId: String? = null,
        val paymentId: Long? = null
    )
    enum class TinkoffAcquiringDelegateOpenPaymentScreenStatus { RESULT_OK, RESULT_CANCELLED, RESULT_NONE, RESULT_ERROR, ERROR_NOT_INITIALIZED, ERROR_NO_ACTIVITY }
    suspend fun openPaymentScreen(
        tinkoffOrderOptions: TinkoffOrderOptions,
        tinkoffCustomerOptions: TinkoffCustomerOptions,
        tinkoffFeaturesOptions: TinkoffFeaturesOptions,
        paymentState: AsdkState? = null
    ): TinkoffAcquiringDelegateOpenPaymentScreenResponse {
        if(tinkoffAcquiring == null) return TinkoffAcquiringDelegateOpenPaymentScreenResponse(status = TinkoffAcquiringDelegateOpenPaymentScreenStatus.ERROR_NOT_INITIALIZED)

        return activityDelegate.runActivityForResult(
            { activity -> tinkoffAcquiring!!.openPaymentScreen(
                activity as FragmentActivity,
                makeTinkoffPaymentOptions(tinkoffOrderOptions, tinkoffCustomerOptions, tinkoffFeaturesOptions),
                TINKOFF_ACQUIRING_OPEN_PAYMENT_SCREEN_REQUEST,
                paymentState ?: DefaultState
            ) },
            TINKOFF_ACQUIRING_OPEN_PAYMENT_SCREEN_REQUEST,
            { resultCode, data -> when(resultCode) {
                Activity.RESULT_OK -> TinkoffAcquiringDelegateOpenPaymentScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentScreenStatus.RESULT_OK,
                    cardId = data.getStringExtra(TinkoffAcquiring.EXTRA_CARD_ID),
                    paymentId = data.getLongExtra(TinkoffAcquiring.EXTRA_PAYMENT_ID, -1).let { if(it != -1L) it else null }
                )
                Activity.RESULT_CANCELED -> TinkoffAcquiringDelegateOpenPaymentScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentScreenStatus.RESULT_CANCELLED
                )
                TinkoffAcquiring.RESULT_ERROR -> TinkoffAcquiringDelegateOpenPaymentScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentScreenStatus.RESULT_ERROR,
                    error = data.getSerializableExtra(TinkoffAcquiring.EXTRA_ERROR) as Throwable
                )
                else -> TinkoffAcquiringDelegateOpenPaymentScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentScreenStatus.RESULT_NONE
                )
            } }
        ) ?: TinkoffAcquiringDelegateOpenPaymentScreenResponse(status = TinkoffAcquiringDelegateOpenPaymentScreenStatus.ERROR_NO_ACTIVITY)
    }

    data class TinkoffAcquiringDelegateOpenGooglePayResponse(
        val status: TinkoffAcquiringDelegateOpenGooglePayStatus,
        val error: Throwable? = null,
        val cardId: String? = null,
        val paymentId: Long? = null,
        val paymentState: AsdkState? = null
    )
    enum class TinkoffAcquiringDelegateOpenGooglePayStatus { RESULT_OK, RESULT_CANCELLED, RESULT_REOPEN_UI, RESULT_ERROR, ERROR_NOT_INITIALIZED, ERROR_NO_ACTIVITY }
    suspend fun openGooglePay(
        tinkoffOrderOptions: TinkoffOrderOptions,
        tinkoffCustomerOptions: TinkoffCustomerOptions,
        tinkoffFeaturesOptions: TinkoffFeaturesOptions
    ): TinkoffAcquiringDelegateOpenGooglePayResponse {
        if(tinkoffAcquiring == null || googlePayHelper == null) return TinkoffAcquiringDelegateOpenGooglePayResponse(status = TinkoffAcquiringDelegateOpenGooglePayStatus.ERROR_NOT_INITIALIZED)

        val googlePayToken = activityDelegate.runActivityForResult(
            { activity -> googlePayHelper!!.openGooglePay(activity, tinkoffOrderOptions.money, TINKOFF_ACQUIRING_OPEN_GOOGLE_PAY_REQUEST) },
            TINKOFF_ACQUIRING_OPEN_GOOGLE_PAY_REQUEST,
            { resultCode, data -> when(resultCode) {
                Activity.RESULT_OK -> GooglePayHelper.getGooglePayToken(data)
                Activity.RESULT_CANCELED -> "canceled"
                else -> null
            } }
        ) ?: return TinkoffAcquiringDelegateOpenGooglePayResponse(
            status = TinkoffAcquiringDelegateOpenGooglePayStatus.ERROR_NO_ACTIVITY
        )

        if(googlePayToken == "canceled") return TinkoffAcquiringDelegateOpenGooglePayResponse(status = TinkoffAcquiringDelegateOpenGooglePayStatus.RESULT_CANCELLED)

        return suspendCoroutine { sink ->
            tinkoffAcquiring!!.initPayment(googlePayToken,  makeTinkoffPaymentOptions(tinkoffOrderOptions, tinkoffCustomerOptions, tinkoffFeaturesOptions))
                .subscribe(object: PaymentListenerAdapter() {
                    override fun onSuccess(paymentId: Long, cardId: String?) =
                        sink.resume(TinkoffAcquiringDelegateOpenGooglePayResponse(
                            status = TinkoffAcquiringDelegateOpenGooglePayStatus.RESULT_OK,
                            paymentId = paymentId,
                            cardId = cardId
                        ))

                    override fun onError(throwable: Throwable) =
                        sink.resume(TinkoffAcquiringDelegateOpenGooglePayResponse(
                            status = TinkoffAcquiringDelegateOpenGooglePayStatus.RESULT_ERROR,
                            error = throwable
                        ))

                    override fun onUiNeeded(state: AsdkState) =
                        sink.resume(TinkoffAcquiringDelegateOpenGooglePayResponse(
                            status = TinkoffAcquiringDelegateOpenGooglePayStatus.RESULT_REOPEN_UI,
                            paymentState = state
                        ))
                })
                .start()
        }
    }

    data class TinkoffAcquiringDelegateOpenPaymentQrScreenResponse(
        val status: TinkoffAcquiringDelegateOpenPaymentQrScreenStatus,
        val error: Throwable? = null
    )
    enum class TinkoffAcquiringDelegateOpenPaymentQrScreenStatus { RESULT_OK, RESULT_CANCELLED, RESULT_ERROR, RESULT_NONE, ERROR_NOT_INITIALIZED, ERROR_NO_ACTIVITY }
    suspend fun openPaymentQrScreen(
        tinkoffFeaturesOptions: TinkoffFeaturesOptions
    ): TinkoffAcquiringDelegateOpenPaymentQrScreenResponse {
        if(tinkoffAcquiring == null) return TinkoffAcquiringDelegateOpenPaymentQrScreenResponse(status = TinkoffAcquiringDelegateOpenPaymentQrScreenStatus.ERROR_NOT_INITIALIZED)

        return activityDelegate.runActivityForResult(
            { activity -> tinkoffAcquiring!!.openStaticQrScreen(
                activity as FragmentActivity,
                tinkoffFeaturesOptions.toTinkoff(),
                TINKOFF_ACQUIRING_OPEN_PAYMENT_QR_SCREEN_REQUEST
            ) },
            TINKOFF_ACQUIRING_OPEN_PAYMENT_QR_SCREEN_REQUEST,
            { resultCode, data -> when(resultCode) {
                Activity.RESULT_OK -> TinkoffAcquiringDelegateOpenPaymentQrScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentQrScreenStatus.RESULT_OK
                )
                Activity.RESULT_CANCELED -> TinkoffAcquiringDelegateOpenPaymentQrScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentQrScreenStatus.RESULT_CANCELLED
                )
                TinkoffAcquiring.RESULT_ERROR -> TinkoffAcquiringDelegateOpenPaymentQrScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentQrScreenStatus.RESULT_ERROR,
                    error = data.getSerializableExtra(TinkoffAcquiring.EXTRA_ERROR) as Throwable
                )
                else -> TinkoffAcquiringDelegateOpenPaymentQrScreenResponse(
                    status = TinkoffAcquiringDelegateOpenPaymentQrScreenStatus.RESULT_NONE
                )
            } }
        ) ?: return TinkoffAcquiringDelegateOpenPaymentQrScreenResponse(
            status = TinkoffAcquiringDelegateOpenPaymentQrScreenStatus.ERROR_NO_ACTIVITY
        )
    }

    data class TinkoffAcquiringDelegateOpenSavedCardsScreenResponse(
        val status: TinkoffAcquiringDelegateOpenSavedCardsScreenStatus,
        val error: Throwable? = null
    )
    enum class TinkoffAcquiringDelegateOpenSavedCardsScreenStatus { RESULT_OK, RESULT_CANCELLED, RESULT_ERROR, RESULT_NONE, ERROR_NOT_INITIALIZED, ERROR_NO_ACTIVITY }
    suspend fun openSavedCardsScreen(
        tinkoffCustomerOptions: TinkoffCustomerOptions,
        tinkoffFeaturesOptions: TinkoffFeaturesOptions
    ): TinkoffAcquiringDelegateOpenSavedCardsScreenResponse {
        if(tinkoffAcquiring == null) return TinkoffAcquiringDelegateOpenSavedCardsScreenResponse(status = TinkoffAcquiringDelegateOpenSavedCardsScreenStatus.ERROR_NOT_INITIALIZED)

        return activityDelegate.runActivityForResult(
            { activity -> tinkoffAcquiring!!.openSavedCardsScreen(
                activity as FragmentActivity,
                makeTinkoffSavedCardsOptions(tinkoffCustomerOptions, tinkoffFeaturesOptions),
                TINKOFF_ACQUIRING_OPEN_SAVED_CARDS_SCREEN_REQUEST
            ) },
            TINKOFF_ACQUIRING_OPEN_SAVED_CARDS_SCREEN_REQUEST,
            { resultCode, data -> when(resultCode) {
                Activity.RESULT_OK -> TinkoffAcquiringDelegateOpenSavedCardsScreenResponse(
                    status = TinkoffAcquiringDelegateOpenSavedCardsScreenStatus.RESULT_OK
                )
                Activity.RESULT_CANCELED -> TinkoffAcquiringDelegateOpenSavedCardsScreenResponse(
                    status = TinkoffAcquiringDelegateOpenSavedCardsScreenStatus.RESULT_CANCELLED
                )
                TinkoffAcquiring.RESULT_ERROR -> TinkoffAcquiringDelegateOpenSavedCardsScreenResponse(
                    status = TinkoffAcquiringDelegateOpenSavedCardsScreenStatus.RESULT_ERROR,
                    error = data.getSerializableExtra(TinkoffAcquiring.EXTRA_ERROR) as Throwable
                )
                else -> TinkoffAcquiringDelegateOpenSavedCardsScreenResponse(
                    status = TinkoffAcquiringDelegateOpenSavedCardsScreenStatus.RESULT_NONE
                )
            } }
        ) ?: return TinkoffAcquiringDelegateOpenSavedCardsScreenResponse(
            status = TinkoffAcquiringDelegateOpenSavedCardsScreenStatus.ERROR_NO_ACTIVITY
        )
    }

    companion object {
        private const val TINKOFF_ACQUIRING_OPEN_ATTACH_CARD_SCREEN_REQUEST = 0xf0ff + 1
        private const val TINKOFF_ACQUIRING_OPEN_PAYMENT_SCREEN_REQUEST = 0xf0ff + 2
        private const val TINKOFF_ACQUIRING_OPEN_GOOGLE_PAY_REQUEST = 0xf0ff + 3
        private const val TINKOFF_ACQUIRING_OPEN_PAYMENT_QR_SCREEN_REQUEST = 0xf0ff + 4
        private const val TINKOFF_ACQUIRING_OPEN_SAVED_CARDS_SCREEN_REQUEST = 0xf0ff + 5
    }
}
