import 'package:flutter/material.dart';
import 'package:tattoo_ar/abstract_augmented_image_platform.dart';
import 'package:tattoo_ar/view/augmented_image_view_controller.dart';

/// A widget which displays a camera stream with image recognition features enabled.
class AugmentedImageView extends StatefulWidget {
  /// Creates a widget displaying a camera stream and handling the image
  /// recognition.
  const AugmentedImageView({
    required this.referenceImageNames,
    required this.onImageDetected,
    this.onDetectedImageTapped,
    this.onViewCreated,
    this.onRecognitionStarted,
    this.onRecognitionPaused,
    this.onRecognitionResumed,
    this.onError,
    super.key,
  });

  /// A list of names of files, that contain reference images. The names should
  /// be provided without their path and extension.
  ///
  /// All image files should be placed in `assets/reference_images` directory
  /// and should be listed in the `assets` section of `pubspec.yaml`.
  ///
  /// Only `.jpg` image files are supported at this time.
  final List<String> referenceImageNames;

  /// Callback method for when the view is ready to be used.
  ///
  /// Used to receive a [AugmentedImageViewController] for this [AugmentedImageView].
  final void Function(AugmentedImageViewController controller)? onViewCreated;

  /// Called when one of the reference images is detected in the current camera
  /// stream.
  final void Function(String? imageName) onImageDetected;

  /// Called when a marker for detected reference image is tapped.
  final void Function(String? imageName)? onDetectedImageTapped;

  /// Called when all reference images are loaded to the recognition module and
  /// the view starts the detection.
  final VoidCallback? onRecognitionStarted;

  /// iOS only; called when the view becomes invisible in the current context
  /// (e.g. after navigating to another [Route]).
  final VoidCallback? onRecognitionPaused;

  /// iOS only; called when the view is restored after being paused (e.g. after
  /// navigating to the [Route] with the view).
  final VoidCallback? onRecognitionResumed;

  /// Called when the view encounters an error.
  final void Function(String error)? onError;

  @override
  State<AugmentedImageView> createState() => AugmentedImageViewState();
}

/// Scanner's view state that's used by the [AugmentedImageViewController] instance
/// to to connect widget's arguments with the platform interface.
///
/// It should be used only inside [AugmentedImageViewController] instance.
class AugmentedImageViewState extends State<AugmentedImageView> {
  AugmentedImageViewController? _controller;

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  void _onPlatformViewCreated(int id) {
    final controller = AugmentedImageViewController(this);
    widget.onViewCreated?.call(controller);
  }

  @override
  Widget build(BuildContext context) {
    return AbstractAugmentedImagePlatform.instance.buildView(
      _onPlatformViewCreated,
      referenceImageNames: widget.referenceImageNames,
    );
  }
}
