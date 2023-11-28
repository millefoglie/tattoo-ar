import 'dart:async';

import 'package:tattoo_ar/abstract_augmented_image_platform.dart';
import 'package:tattoo_ar/view/augmented_image_view.dart';

/// Controller for a single ARQuidoView instance running on the host platform.
class AugmentedImageViewController {
  /// Instantiates the controller with scanner's view state that this controller
  /// instance is attached to.
  AugmentedImageViewController(this._scannerViewState) {
    AbstractAugmentedImagePlatform.instance.init();
    _connectStreams();
  }

  final AugmentedImageViewState _scannerViewState;
  final List<StreamSubscription<dynamic>> _subscriptions = [];

  /// Disposes the controller resources (event stream subscriptions).
  void dispose() {
    for (final subscription in _subscriptions) {
      subscription.cancel();
    }
  }

  /// Sets the current state of the device's flashlight.
  ///
  /// The returned [Future] completes after the switch has been triggered on the
  /// platform side.
  Future<void> toggleFlashlight({required bool shouldTurnOn}) {
    return AbstractAugmentedImagePlatform.instance.toggleFlashlight(
      shouldTurnOn: shouldTurnOn,
    );
  }

  void _connectStreams() {
    _connectLifecycleEventStreams();

    final platformInstance = AbstractAugmentedImagePlatform.instance;
    final scannerViewWidget = _scannerViewState.widget;
    if (scannerViewWidget.onError != null) {
      _subscriptions.add(
        platformInstance
            .onError()
            .listen((event) => _scannerViewState.widget.onError!(event.error)),
      );
    }
    if (scannerViewWidget.onDetectedImageTapped != null) {
      _subscriptions.add(
        platformInstance.onDetectedImageTapped().listen(
              (event) =>
                  scannerViewWidget.onDetectedImageTapped!(event.imageName),
            ),
      );
    }

    _subscriptions.add(
      platformInstance.onImageDetected().listen(
            (event) => scannerViewWidget.onImageDetected(event.imageName),
          ),
    );
  }

  void _connectLifecycleEventStreams() {
    final platformInstance = AbstractAugmentedImagePlatform.instance;
    final scannerViewWidget = _scannerViewState.widget;
    if (scannerViewWidget.onRecognitionStarted != null) {
      _subscriptions.add(
        platformInstance.onRecognitionStarted().listen(
              (event) => _scannerViewState.widget.onRecognitionStarted!(),
            ),
      );
    }
    if (scannerViewWidget.onRecognitionResumed != null) {
      _subscriptions.add(
        platformInstance.onRecognitionResumed().listen(
              (event) => _scannerViewState.widget.onRecognitionResumed!(),
            ),
      );
    }
    if (scannerViewWidget.onRecognitionPaused != null) {
      _subscriptions.add(
        platformInstance
            .onRecognitionPaused()
            .listen((event) => _scannerViewState.widget.onRecognitionPaused!()),
      );
    }
  }
}
