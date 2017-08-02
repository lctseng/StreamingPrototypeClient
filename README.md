# Overview
This is the client side application for [StreamingPrototype](https://github.com/KaoCC/StreamingPrototype). It's an Android application built on LibGDX, with some third party libraries like Google VRToolkit and FFMpeg. It can display the light field images that streamed from the server. 

# Demo Video
[Demo Video on Youtube](https://youtu.be/lMdvIzpLWpQ)

# Environment
- Android Studio 2.3 or higher
- Windows 10 and Mac OSX 10.11 are tested and they can build this application successfully

# SDK dependency
- Note: these dependency can be installed inside Android Studio
- Android Studio SDK (Version 22, 25, install them if Gradle build is failed)
- NDK (for building JNI)
- CMake (for building JNI)

# Build
- Embedded shared objects (.so) are built for armeabi-v7a only
  - libffmpeg
  - libvrtoolkit
- Configuring signatory may be needed for some libraries

