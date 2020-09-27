
import 'dart:async';
import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:tinkoff_acquiring_sdk/tinkoff_acquiring_models.dart';

enum TinkoffAcquiringSdkStatus {
  NOT_INITIALIZED,
  INITIALIZATION_ERROR,
  INITIALIZED
}

class TinkoffAcquiringSdk {
  static const MethodChannel _channel = const MethodChannel('eu.nk2/tinkoff_acquiring_sdk');

  final bool enableDebug;
  final String terminalKey;
  final String password;
  final String publicKey;
  final bool enableGooglePay;
  final bool requireAddress;
  final bool requirePhone;

  TinkoffAcquiringSdkStatus _status = TinkoffAcquiringSdkStatus.NOT_INITIALIZED;

  TinkoffAcquiringSdk({
    this.enableDebug = false,
    @required this.terminalKey,
    @required this.password,
    @required this.publicKey,
    this.enableGooglePay = false,
    this.requireAddress = false,
    this.requirePhone = false,
  }): assert(terminalKey != null),
    assert(password != null),
    assert(publicKey != null),
    assert(!enableGooglePay || (enableGooglePay && Platform.isAndroid));

  Future<TinkoffAcquiringInitializationResponse> initialize({
    bool exceptAlreadyInitialized = true
  }) async {
    if(_status != TinkoffAcquiringSdkStatus.INITIALIZED) {
      if(exceptAlreadyInitialized) throw TinkoffError(message: 'Plugin was already initialized when the initialize() was called.');
    }

    final Map<dynamic, dynamic> response = await _channel.invokeMethod('initialize', {
      'enableDebug': this.enableDebug,
      'terminalKey': this.terminalKey,
      'password': this.password,
      'publicKey': this.publicKey.replaceAll('\n', ''), //iOS shits about it
      'enableGooglePay': this.enableGooglePay,
      'requireAddress': this.requireAddress,
      'requirePhone': this.requirePhone,
    });

    final TinkoffAcquiringInitializationResponse status = TinkoffAcquiringInitializationResponse.fromJson(response.cast<String, dynamic>());

    if(status.status == TinkoffAcquiringInitializationStatus.RESULT_ERROR) {
      _status = TinkoffAcquiringSdkStatus.INITIALIZATION_ERROR;
      throw TinkoffError(message: status.error);
    }

    if(status.status == TinkoffAcquiringInitializationStatus.FLUTTER_NOT_INITIALIZED) {
      _status = TinkoffAcquiringSdkStatus.INITIALIZATION_ERROR;
      throw TinkoffError(message: 'Flutter was not initialized when the initialize() was called.');
    }

    _status = TinkoffAcquiringSdkStatus.INITIALIZED;
    if(status.status == TinkoffAcquiringInitializationStatus.PLUGIN_ALREADY_INITIALIZED) {
      if(exceptAlreadyInitialized) throw TinkoffError(message: 'Plugin was already initialized when the initialize() was called.');
    }

    return status;
  }

  Future<TinkoffCommonResponse> openAttachCardScreen({
    String customerId,
    TinkoffCheckType checkType,
    String email,
    bool enableSecureKeyboard,
    bool enableCameraCardScanner,
    TinkoffDarkThemeMode darkThemeMode,
    TinkoffLanguage language
  }) async {
    assert(_status == TinkoffAcquiringSdkStatus.INITIALIZED);
    assert(customerId != null);
    assert(language != null);

    final Map<dynamic, dynamic> response = await _channel.invokeMethod('openAttachCardScreen', {
      'customerId': customerId,
      'checkType': checkType != null? mapEnumToString(checkType) : null,
      'email': email,
      'enableSecureKeyboard': enableSecureKeyboard,
      'enableCameraCardScanner': enableCameraCardScanner,
      'darkThemeMode': mapEnumToString(darkThemeMode),
      'language': mapLanguageToPlatform(language)
    });

    final TinkoffCommonResponse status = TinkoffCommonResponse.fromJson(response.cast<String, dynamic>());

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NO_ACTIVITY)
      throw TinkoffError(message: 'Plugin is running without activity.');

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NOT_INITIALIZED)
      throw TinkoffError(message: 'Plugin is not initialized.');

    if(status.error != null)
      throw TinkoffError(message: status.error);

    return status;
  }

  Future<TinkoffCommonResponse> openPaymentScreen({
    String orderId,
    String title,
    String description,
    double money,
    bool recurrentPayment,
    String customerId,
    TinkoffCheckType checkType,
    String email,
    bool enableSecureKeyboard,
    bool enableCameraCardScanner,
    TinkoffDarkThemeMode darkThemeMode,
    TinkoffLanguage language
  }) async {
    assert(_status == TinkoffAcquiringSdkStatus.INITIALIZED);
    assert(orderId != null);
    assert(title != null);
    assert(description != null);
    assert(money != null);
    assert(customerId != null);
    assert(language != null);

    final Map<dynamic, dynamic> response = await _channel.invokeMethod('openPaymentScreen', {
      'orderId': orderId,
      'title': title,
      'description': description,
      'money': money,
      'recurrentPayment': recurrentPayment,
      'customerId': customerId,
      'checkType': checkType != null? mapEnumToString(checkType) : null,
      'email': email,
      'enableSecureKeyboard': enableSecureKeyboard,
      'enableCameraCardScanner': enableCameraCardScanner,
      'darkThemeMode': mapEnumToString(darkThemeMode),
      'language': mapLanguageToPlatform(language)
    });

    final TinkoffCommonResponse status = TinkoffCommonResponse.fromJson(response.cast<String, dynamic>());

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NO_ACTIVITY)
      throw TinkoffError(message: 'Plugin is running without activity.');

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NOT_INITIALIZED)
      throw TinkoffError(message: 'Plugin is not initialized.');

    if(status.error != null)
      throw TinkoffError(message: status.error);

    return status;
  }

  Future<TinkoffCommonResponse> openGooglePay({
    String orderId,
    String title,
    String description,
    double money,
    bool recurrentPayment,
    String customerId,
    TinkoffCheckType checkType,
    String email,
    bool enableSecureKeyboard,
    bool enableCameraCardScanner,
    TinkoffDarkThemeMode darkThemeMode,
    TinkoffLanguage language
  }) async {
    assert(_status == TinkoffAcquiringSdkStatus.INITIALIZED);
    assert(Platform.isAndroid);
    assert(orderId != null);
    assert(title != null);
    assert(description != null);
    assert(money != null);
    assert(customerId != null);
    assert(language != null);

    final Map<dynamic, dynamic> response = await _channel.invokeMethod('openGooglePay', {
      'orderId': orderId,
      'title': title,
      'description': description,
      'money': money,
      'recurrentPayment': recurrentPayment,
      'customerId': customerId,
      'checkType': checkType != null? mapEnumToString(checkType) : null,
      'email': email,
      'enableSecureKeyboard': enableSecureKeyboard,
      'enableCameraCardScanner': enableCameraCardScanner,
      'darkThemeMode': mapEnumToString(darkThemeMode),
      'language': mapLanguageToPlatform(language)
    });

    final TinkoffCommonResponse status = TinkoffCommonResponse.fromJson(response.cast<String, dynamic>());

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NO_ACTIVITY)
      throw TinkoffError(message: 'Plugin is running without activity.');

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NOT_INITIALIZED)
      throw TinkoffError(message: 'Plugin is not initialized.');

    if(status.error != null)
      throw TinkoffError(message: status.error);

    return status;
  }

  Future<TinkoffCommonResponse> openApplePay({
    String orderId,
    String title,
    String description,
    double money,
    bool recurrentPayment,
    String customerId,
    TinkoffCheckType checkType,
    String email,
    TinkoffLanguage language,
    String merchantIdentifier
  }) async {
    assert(_status == TinkoffAcquiringSdkStatus.INITIALIZED);
    assert(Platform.isIOS);
    assert(orderId != null);
    assert(title != null);
    assert(description != null);
    assert(money != null);
    assert(customerId != null);
    assert(language != null);
    assert(merchantIdentifier != null);

    final Map<dynamic, dynamic> response = await _channel.invokeMethod('openApplePay', {
      'orderId': orderId,
      'title': title,
      'description': description,
      'money': money,
      'recurrentPayment': recurrentPayment,
      'customerId': customerId,
      'checkType': checkType != null? mapEnumToString(checkType) : null,
      'email': email,
      'language': mapLanguageToPlatform(language),
      'merchantIdentifier': merchantIdentifier
    });

    final TinkoffCommonResponse status = TinkoffCommonResponse.fromJson(response.cast<String, dynamic>());

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NO_ACTIVITY)
      throw TinkoffError(message: 'Plugin is running without activity.');

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NOT_INITIALIZED)
      throw TinkoffError(message: 'Plugin is not initialized.');

    if(status.error != null)
      throw TinkoffError(message: status.error);

    return status;
  }

  Future<TinkoffCommonResponse> openPaymentQrScreen({
    bool enableSecureKeyboard,
    bool enableCameraCardScanner,
    TinkoffDarkThemeMode darkThemeMode,
    TinkoffLanguage language
  }) async {
    assert(_status == TinkoffAcquiringSdkStatus.INITIALIZED);
    assert(language != null);

    final Map<dynamic, dynamic> response = await _channel.invokeMethod('openPaymentQrScreen', {
      'enableSecureKeyboard': enableSecureKeyboard,
      'enableCameraCardScanner': enableCameraCardScanner,
      'darkThemeMode': mapEnumToString(darkThemeMode),
      'language': mapLanguageToPlatform(language)
    });

    final TinkoffCommonResponse status = TinkoffCommonResponse.fromJson(response.cast<String, dynamic>());

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NO_ACTIVITY)
      throw TinkoffError(message: 'Plugin is running without activity.');

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NOT_INITIALIZED)
      throw TinkoffError(message: 'Plugin is not initialized.');

    if(status.error != null)
      throw TinkoffError(message: status.error);

    return status;
  }

  Future<TinkoffCommonResponse> openSavedCardsScreen({
    String customerId,
    TinkoffCheckType checkType,
    String email,
    bool enableSecureKeyboard,
    bool enableCameraCardScanner,
    TinkoffDarkThemeMode darkThemeMode,
    TinkoffLanguage language
  }) async {
    assert(_status == TinkoffAcquiringSdkStatus.INITIALIZED);
    assert(customerId != null);
    assert(language != null);

    final Map<dynamic, dynamic> response = await _channel.invokeMethod('openSavedCardsScreen', {
      'customerId': customerId,
      'checkType': checkType != null? mapEnumToString(checkType) : null,
      'email': email,
      'enableSecureKeyboard': enableSecureKeyboard,
      'enableCameraCardScanner': enableCameraCardScanner,
      'darkThemeMode': mapEnumToString(darkThemeMode),
      'language': mapLanguageToPlatform(language)
    });

    final TinkoffCommonResponse status = TinkoffCommonResponse.fromJson(response.cast<String, dynamic>());

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NO_ACTIVITY)
      throw TinkoffError(message: 'Plugin is running without activity.');

    if(status.status == TinkoffAcquiringCommonStatus.ERROR_NOT_INITIALIZED)
      throw TinkoffError(message: 'Plugin is not initialized.');

    if(status.error != null)
      throw TinkoffError(message: status.error);

    return status;
  }
}
