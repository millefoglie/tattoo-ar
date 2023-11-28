# Tattoo AR Example

This is a PoC of an AR mobile app that lets a user try on a tattoo on his body
using a phone camera. The app is based on [ar_quido](https://github.com/miquido/AR_quido) flutter plugin
that uses [EasyAR Sense](https://www.easyar.com/view/sdk.html) for image augmentation.
That is because ARCore imposes severe restrictions on the image quality and is
not as efficient for this task.

A quick research shows that similar apps typically require drawing some mark on your
body that is detected and augmented with a tattoo image. See for example
- [AR Tattoo](https://play.google.com/store/apps/details?id=com.ar.tattoo&hl=uk&gl=US)
- [InkHunter](https://play.google.com/store/apps/details?id=tattoo.inkhunter&hl=uk&gl=US)

Thus, the present app also follows this approach. One has to apply a marker image first
to the body and then point a camera to it.

For now only a camera preview with AR is implemented. All other functionality such as
choosing images, adjusting size, position, etc. are missing. One can replace
marker and tattoo images and build the app. Also, only the Android platform
is supported.

A free vintage butterfly illustration is used a sample tattoo. 
See more images at [Vector Vectors by Vecteezy](https://www.vecteezy.com/free-vector/vector).

## Getting Started

1. Set up Flutter SDK.
2. Set up Android SDK.
3. Set up Android Studio.
4. Sign up at [EasyAR Sense](https://www.easyar.com/view/sdk.html) and create a license key. Note that this key must match
the application/package name (`com.example.tattoo_ar`). Set `EASY_AR_API_KEY` variable in `android/local.properties` to the
value of the created API key.
5. Replace asset images if desired (`assets/reference_images/marker.jpg` and `android/app/src/main/assets/tattoo_2k.jpg`).
Note that EasyAR Sense only supports `jpg` images. And tattoo images will have skin tinting and transparency effects
on white pixels.
6. Run `flutter pub get` to resolve the dependencies.
7. Set up the development mode on your device and run the app.
8. Point the camera to a marker image and see how it gets replaced with a tattoo image in the camera preview.

## Build & Run

To build the APK execute
```bash
flutter build apk
```

To run the app execute
```bash
flutter run
```

## Useful Links

- [Flutter installation instructions](https://docs.flutter.dev/get-started/install)
- [Lab: Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://docs.flutter.dev/cookbook)
- [5 Takeaways From Building an AR-based Image Recognition App with Flutter](https://medium.com/miquido/5-takeaways-from-building-an-ar-based-image-recognition-app-with-flutter-3864100a22c2)
- [ar_quido Flutter plugin](https://pub.dev/packages/augmented_reality_plugin)
- [OpenGL ES Tutorials](https://arm-software.github.io/opengl-es-sdk-for-android/tutorials.html)
