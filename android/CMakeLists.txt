# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.



add_library( ffmpeg
             SHARED
             IMPORTED )

set_target_properties( # Specifies the target library.
                       ffmpeg

                       # Specifies the parameter you want to define.
                       PROPERTIES IMPORTED_LOCATION

                       # Provides the path to the library you want to import.
                       ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/libffmpeg.so )


include_directories(cpp)
include_directories(cpp/${ANDROID_ABI})

file(GLOB NATIVE_SRC
    cpp/decoder.c
    cpp/jni_decoder.c
)

add_library( # Specifies the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${NATIVE_SRC} )

target_link_libraries( native-lib ffmpeg log)