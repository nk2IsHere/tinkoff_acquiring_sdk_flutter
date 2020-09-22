import 'package:json_annotation/json_annotation.dart';

part 'tinkoff_acquiring_models.g.dart';

@JsonSerializable()
class TinkoffError extends Error {
  final String message;

  TinkoffError({
    this.message
  });

  @override
  String toString() {
    return 'TinkoffError{message: $message}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
          other is TinkoffError &&
              runtimeType == other.runtimeType &&
              message == other.message;

  @override
  int get hashCode => message.hashCode;

  factory TinkoffError.fromJson(Map<String, dynamic> json) => _$TinkoffErrorFromJson(json);
  Map<String, dynamic> toJson() => _$TinkoffErrorToJson(this);
}

enum TinkoffAcquiringInitializationStatus {
  NOT_INITIALIZED,
  RESULT_OK,
  GOOGLE_PAY_NOT_AVAILABLE,
  FLUTTER_NOT_INITIALIZED,
  PLUGIN_ALREADY_INITIALIZED
}

@JsonSerializable()
class TinkoffAcquiringInitializationResponse {
  final TinkoffAcquiringInitializationStatus status;

  TinkoffAcquiringInitializationResponse({
    this.status
  });

  @override
  String toString() {
    return 'TinkoffAcquiringInitializationResponse{status: $status}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
          other is TinkoffAcquiringInitializationResponse &&
              runtimeType == other.runtimeType &&
              status == other.status;

  @override
  int get hashCode => status.hashCode;

  factory TinkoffAcquiringInitializationResponse.fromJson(Map<String, dynamic> json) => _$TinkoffAcquiringInitializationResponseFromJson(json);
  Map<String, dynamic> toJson() => _$TinkoffAcquiringInitializationResponseToJson(this);
}

enum TinkoffAcquiringCommonStatus {
  RESULT_OK,
  RESULT_CANCELLED,
  RESULT_NONE,
  RESULT_ERROR,
  ERROR_NOT_INITIALIZED,
  ERROR_NO_ACTIVITY
}

@JsonSerializable()
class TinkoffCommonResponse {
  final TinkoffAcquiringCommonStatus status;
  final String cardId;
  final int paymentId;
  final String error;

  TinkoffCommonResponse({
    this.status,
    this.cardId,
    this.paymentId,
    this.error
  });


  @override
  String toString() {
    return 'TinkoffCommonResponse{status: $status, cardId: $cardId, paymentId: $paymentId, error: $error}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is TinkoffCommonResponse &&
          runtimeType == other.runtimeType &&
          status == other.status &&
          cardId == other.cardId &&
          paymentId == other.paymentId &&
          error == other.error;

  @override
  int get hashCode =>
      status.hashCode ^ cardId.hashCode ^ paymentId.hashCode ^ error.hashCode;

  factory TinkoffCommonResponse.fromJson(Map<String, dynamic> json) => _$TinkoffCommonResponseFromJson(json);
  Map<String, dynamic> toJson() => _$TinkoffCommonResponseToJson(this);
}

enum TinkoffCheckType {
  NO,
  HOLD,
  THREE_DS,
  THREE_DS_HOLD
}

enum TinkoffDarkThemeMode {
  DISABLED,
  ENABLED,
  AUTO
}

enum TinkoffLanguage {
  RU,
  EN
}

String mapEnumToString(dynamic value) => value.toString().split('.').last;