import 'dart:io';

import 'package:flutter/material.dart';

import 'package:tinkoff_acquiring_sdk/tinkoff_acquiring_sdk.dart';
import 'package:tinkoff_acquiring_sdk/tinkoff_acquiring_models.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final TinkoffAcquiringSdk tinkoffAcquiringSdk = TinkoffAcquiringSdk(
      enableDebug: true,
      terminalKey: "TestSDK",
      password: "12345678",
      publicKey: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5Yg3RyEkszggDVMDHCAG\n" +
          "zJm0mYpYT53BpasrsKdby8iaWJVACj8ueR0Wj3Tu2BY64HdIoZFvG0v7UqSFztE/\n" +
          "zUvnznbXVYguaUcnRdwao9gLUQO2I/097SHF9r++BYI0t6EtbbcWbfi755A1EWfu\n" +
          "9tdZYXTrwkqgU9ok2UIZCPZ4evVDEzDCKH6ArphVc4+iKFrzdwbFBmPmwi5Xd6CB\n" +
          "9Na2kRoPYBHePGzGgYmtKgKMNs+6rdv5v9VB3k7CS/lSIH4p74/OPRjyryo6Q7Nb\n" +
          "L+evz0+s60Qz5gbBRGfqCA57lUiB3hfXQZq5/q1YkABOHf9cR6Ov5nTRSOnjORgP\n" +
          "jwIDAQAB",
      enableGooglePay: Platform.isAndroid);

  @override
  void initState() {
    super.initState();
    tinkoffAcquiringSdk.initialize(exceptAlreadyInitialized: false);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Tinkoff example app'),
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            RaisedButton(
                onPressed: () {
                  tinkoffAcquiringSdk.openAttachCardScreen(
                      customerId: "test-id",
                      checkType: TinkoffCheckType.HOLD,
                      enableSecureKeyboard: true,
                      enableCameraCardScanner: true,
                      darkThemeMode: TinkoffDarkThemeMode.DISABLED,
                      language: TinkoffLanguage.RU);
                },
                child: Text("open attach card screen")),
            RaisedButton(
                onPressed: () {
                  tinkoffAcquiringSdk.openSavedCardsScreen(
                      customerId: "test-id",
                      checkType: TinkoffCheckType.HOLD,
                      enableSecureKeyboard: true,
                      enableCameraCardScanner: true,
                      darkThemeMode: TinkoffDarkThemeMode.ENABLED,
                      language: TinkoffLanguage.RU);
                },
                child: Text("open saved cards screen")),
            RaisedButton(
                onPressed: () {
                  tinkoffAcquiringSdk.openPaymentQrScreen(
                      enableSecureKeyboard: true,
                      enableCameraCardScanner: true,
                      darkThemeMode: TinkoffDarkThemeMode.DISABLED,
                      language: TinkoffLanguage.RU);
                },
                child: Text("open payment qr screen")),
            RaisedButton(
                onPressed: () {
                  tinkoffAcquiringSdk.openPaymentScreen(
                      orderId: "test-order-id-1",
                      title: "Test order",
                      description: "description for order",
                      money: 1000.0,
                      customerId: "test-id",
                      checkType: TinkoffCheckType.HOLD,
                      enableSecureKeyboard: true,
                      enableCameraCardScanner: true,
                      darkThemeMode: TinkoffDarkThemeMode.DISABLED,
                      language: TinkoffLanguage.RU);
                },
                child: Text("open payment screen")),
            RaisedButton(
                onPressed: () {
                  tinkoffAcquiringSdk.openGooglePay(
                      orderId: "test-order-id-1",
                      title: "Test order",
                      description: "description for order",
                      money: 1000.0,
                      customerId: "test-id",
                      checkType: TinkoffCheckType.HOLD,
                      enableSecureKeyboard: true,
                      enableCameraCardScanner: true,
                      darkThemeMode: null,
                      language: TinkoffLanguage.RU);
                },
                child: Text("open google pay")),
            RaisedButton(
                onPressed: () {
                  tinkoffAcquiringSdk.openApplePay(
                      orderId: "test-order-id-1",
                      title: "Test order",
                      description: "description for order",
                      money: 1000,
                      customerId: "test-id",
                      checkType: TinkoffCheckType.HOLD,
                      language: TinkoffLanguage.RU,
                      merchantIdentifier:
                          "merchant.tcsbank.ApplePayTestMerchantId");
                },
                child: Text("open apple pay")),
          ],
        ),
      ),
    );
  }
}
