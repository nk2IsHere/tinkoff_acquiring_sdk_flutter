package eu.nk2.tinkoff_acquiring_sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** TinkoffAcquiringSdkPlugin */
class TinkoffAcquiringSdkPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, ActivityDelegate {

  private lateinit var channel : MethodChannel

  private val methodCallScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private lateinit var delegate: TinkoffAcquiringSdkDelegate

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "eu.nk2/tinkoff_acquiring_sdk")
    channel.setMethodCallHandler(this)

    delegate = TinkoffAcquiringSdkDelegate(this)
  }

  //
  // Method-related stuff
  //

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    methodCallScope.launch {
      tinkoffMethodBundle[call.method]?.invoke(call, result, delegate, methodCallScope) ?: result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    methodCallScope.coroutineContext.cancelChildren()
  }

  //
  // Activity-related stuff
  //

  private var binding: ActivityPluginBinding? = null

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    this.binding = binding
  }

  override fun onDetachedFromActivityForConfigChanges() {
    binding = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    this.binding = binding
  }

  override fun onDetachedFromActivity() {
    binding = null
  }

  override val activity: Activity? get() = binding?.activity
  override val context: Context? get() = binding?.activity

  override suspend fun <T> runActivityForResult(
      initializeActivity: (Activity) -> Unit,
      requestCode: Int,
      activityResultMapper: (resultCode: Int, data: Intent) -> T?
  ): T? {
    if(binding == null) return null

    methodCallScope.doOnMain { initializeActivity(binding!!.activity) }
    return suspendCoroutine { sink ->
      binding?.addActivityResultListener(object: ActivityResultListener {
        override fun onActivityResult(_requestCode: Int, resultCode: Int, data: Intent?): Boolean {
          binding?.removeActivityResultListener(this)
          return if (requestCode != _requestCode || data == null) false
          else {
            methodCallScope.doOnMain { sink.resume(activityResultMapper(resultCode, data)) }
            true
          }
        }
      })
    }
  }
}
