import Flutter
import UIKit

public class SwiftTinkoffAcquiringSdkPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "tinkoff_acquiring_sdk", binaryMessenger: registrar.messenger())
    let instance = SwiftTinkoffAcquiringSdkPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
