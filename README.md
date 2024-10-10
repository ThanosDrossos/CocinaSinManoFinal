
# Cocina Sin Mano

### Overview

This is a camera app that can either continuously detects hand landmarks and classifies gestures (gesture name and confidence level) from camera frames seen by your device's front camera, an image, or a video from the device's gallery using a custom **task** file.

The task file is downloaded by a Gradle script when you build and run the app. You don't need to do any additional steps to download task files into the project explicitly unless you wish to use your own custom gesture recognition task. If you do use your own task file, place it into the app's *assets* directory.

This application should be run on a physical Android device to take advantage of the camera.

![gesture recognition demo](gesturerec.gif?raw=true "Gesture Recognition Demo")
