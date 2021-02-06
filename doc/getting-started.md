# Tinkoff Acquiring SDK for Flutter

**ВАЖНО:**
Пока что я не поддерживаю эту библиотеку, т.к. она является (как и ASDK) большой дырой безопасности:
- ASDK не позволяет сменить сервер на кастомный (ex: прокси или свой враппер)
- terminal key и password захардкоживается в приложение без возможности работать без него
- 5 минут реверса байтода дарта, и... последствия очевидны

**РЕШЕНИЕ:**
Делать свой клиент для апи и потом свой UI на flutter.

**P.S.:**
Свою реализацию на kotlin выложу после тестирования.

Tinkoff Acquiring SDK bindings for Flutter.\
Two platforms, one package

<img src="https://raw.githubusercontent.com/nk2ishere/tinkoff_acquiring_sdk_flutter/master/doc/img/overview.png" height="500"/>

## Getting Started

Please look at these materials to get started working with this package:

- [Example of usage](https://github.com/nk2IsHere/tinkoff_acquiring_sdk_flutter/blob/master/example/lib/main.dart)
- [Tinkoff official docs](https://oplata.tinkoff.ru/develop/sdk/)
- [iOS SDK repository](https://github.com/TinkoffCreditSystems/AcquiringSdk_IOS)
- [Android SDK repository](https://github.com/TinkoffCreditSystems/AcquiringSdkAndroid)
