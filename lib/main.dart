import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:tattoo_ar/view/augmented_image_view.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Permission.camera.request();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String? _recognizedImage;

  void _onImageDetected(BuildContext context, String? imageName) {
    if (imageName != null && _recognizedImage != imageName) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Recognized image: $imageName'),
          duration: const Duration(milliseconds: 3000),
        ),
      );
    }

    setState(() {
      _recognizedImage = imageName;
    });
  }

  void _onDetectedImageTapped(BuildContext context, String? imageName) {
    if (imageName != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Tapped on image: $imageName'),
          duration: const Duration(milliseconds: 1500),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Tattoo AR'),
        ),
        body: Builder(
          builder: (context) {
            return Stack(
              children: [
                AugmentedImageView(
                  referenceImageNames: const ['marker'],
                  onImageDetected: (imageName) =>
                      _onImageDetected(context, imageName),
                  onDetectedImageTapped: (imageName) =>
                      _onDetectedImageTapped(context, imageName),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}
