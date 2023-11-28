import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:stream_transform/stream_transform.dart';
import 'package:tattoo_ar/types/scanner_event.dart';

import 'abstract_augmented_image_platform.dart';

/// An implementation of [AbstractAugmentedImagePlatform] that uses method channels.
class AugmentedImageMethodChannelPlatform extends AbstractAugmentedImagePlatform {
  static const String _androidViewType =
      'com.example.tattoo_ar/augmented_view';
  static const String _methodChannelName =
      'com.example.tattoo_ar/augmented_method_channel';

  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel(_methodChannelName);

  final StreamController<ScannerEvent> _scannerEventStreamController =
      StreamController<ScannerEvent>.broadcast();

  @override
  void init() {
    methodChannel.setMethodCallHandler(_handleMethodCall);
  }

  @override
  void dispose() {
    _scannerEventStreamController.close();
    super.dispose();
  }

  @override
  Future<void> toggleFlashlight({required bool shouldTurnOn}) async {
    final arguments = {'shouldTurnOn': shouldTurnOn};
    await methodChannel.invokeMethod<void>(
      'scanner#toggleFlashlight',
      arguments,
    );
  }

  @override
  Stream<ImageDetectedEvent> onImageDetected() =>
      _scannerEventStreamController.stream.whereType<ImageDetectedEvent>();

  @override
  Stream<ImageTappedEvent> onDetectedImageTapped() =>
      _scannerEventStreamController.stream.whereType<ImageTappedEvent>();

  @override
  Stream<RecognitionStartedEvent> onRecognitionStarted() =>
      _scannerEventStreamController.stream.whereType<RecognitionStartedEvent>();

  @override
  Stream<RecognitionResumedEvent> onRecognitionResumed() =>
      _scannerEventStreamController.stream.whereType<RecognitionResumedEvent>();

  @override
  Stream<RecognitionPausedEvent> onRecognitionPaused() =>
      _scannerEventStreamController.stream.whereType<RecognitionPausedEvent>();

  @override
  Stream<ErrorEvent> onError() =>
      _scannerEventStreamController.stream.whereType<ErrorEvent>();

  @override
  Widget buildView(
    PlatformViewCreatedCallback onPlatformViewCreated, {
    required List<String> referenceImageNames,
  }) {
    final creationParams = _buildCreationParams(referenceImageNames);
    return switch (defaultTargetPlatform) {
      TargetPlatform.android => PlatformViewLink(
        viewType: _androidViewType,
        surfaceFactory: (context, controller) {
          return AndroidViewSurface(
            controller: controller as AndroidViewController,
            gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
            hitTestBehavior: PlatformViewHitTestBehavior.opaque,
          );
        },
        onCreatePlatformView: (params) {
          return PlatformViewsService.initSurfaceAndroidView(
            id: params.id,
            viewType: _androidViewType,
            layoutDirection: TextDirection.ltr,
            creationParams: creationParams,
            creationParamsCodec: const StandardMessageCodec(),
            onFocus: () {
              params.onFocusChanged(true);
            },
          )
            ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
            ..addOnPlatformViewCreatedListener(onPlatformViewCreated)
            ..create();
        },
      ),
      _ => throw Exception(
          '$defaultTargetPlatform is not supported',
        ),
    };
  }

  Map<String, dynamic> _buildCreationParams(List<String> referenceImageNames) =>
      <String, dynamic>{
        'referenceImageNames': referenceImageNames,
      };

  Future<void> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'scanner#start':
        _scannerEventStreamController.add(RecognitionStartedEvent());
      case 'scanner#recognitionPaused':
        _scannerEventStreamController.add(RecognitionPausedEvent());
      case 'scanner#recognitionResumed':
        _scannerEventStreamController.add(RecognitionResumedEvent());
      case 'scanner#onImageDetected':
        final arguments = (call.arguments as Map).cast<String, String?>();
        final imageName = arguments['imageName'];
        _scannerEventStreamController.add(ImageDetectedEvent(imageName));
      case 'scanner#onDetectedImageTapped':
        final arguments = (call.arguments as Map).cast<String, String?>();
        final imageName = arguments['imageName'];
        _scannerEventStreamController.add(ImageTappedEvent(imageName));
      case 'scanner#error':
        final arguments = (call.arguments as Map).cast<String, String?>();
        final error = arguments['errorCode'];
        _scannerEventStreamController.add(ErrorEvent(error!));
      default:
        throw MissingPluginException();
    }
  }
}
