# Atjeews

## Purpose
Android wrapper for TJWS

## Current status
The version of the Atjeews works with the latest Android SDK 33. It tested with TJWS 1.119,
after the compatibility with Java 8 restored. 

The current implementation requires Android build tool patch. The patch content is provided
in "build-tool" directory. The complete source to patch can be taken from https://github.com/miracle2k/android-platform_sdk

## How to build
There are two options, either use [7Bee]https://github.com/drogatkin/7Bee Java build tool
or [RustBee](https://github.com/drogatkin/rust_utilities)  Linux build tool. bee-build.xml or bee.rb has to be edited to provide
a correct location of used components.

7Bee provides also bee-dexwar.xml script for auto converting "war" files in Android format.

## Known problems
1. web.xml less deployment doesn't work

Please report any other problems you encountered.

## Google Play Store
You can still download the server from the Google Play store. However the server requires some level
of an expertise an ordinary user may not have. It is related to security concerns. Therefore the author
of the server decided to do not provide new versions in the Play store and use the GitHub instead for distributing the server.

Thank you for understanding.

