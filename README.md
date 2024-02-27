# Atjeews

## Purpose
Android wrapper for TJWS

## Current status
The version of the Atjeews works with the latest Android SDK 34. It tested with TJWS 1.121. 

**Note:** TJWS has to be compiled using Java 8 with [android option](https://sourceforge.net/p/tjws/git/ci/master/tree/1.x/env.xml#l50) set to *yes*. Make sure that Android SDK location is correctly defined.

The current implementation requires an Android build tool patch. The patch content is provided
in [build-tool](https://github.com/drogatkin/Atjeews/tree/master/build-tool) directory. The complete source to patch can be taken from [GitHub](https://github.com/miracle2k/android-platform_sdk).

## How to build
There are two options, either use [7Bee](https://github.com/drogatkin/7Bee) Java build tool
or [RustBee](https://gitlab.com/tools6772135/rusthub/-/tree/master)  Linux build tool. bee-build.xml or [bee.7b](https://github.com/drogatkin/Atjeews/blob/master/bee.7b) has to be edited to provide
a correct location of supplements. It is also possible to move the project under Android Studio and use Gradle. But, there
is a risk that websocket won't work correctly due a bug in Gradle.

7Bee provides also [bee-dexwar.xml](https://github.com/drogatkin/Atjeews/blob/master/bee-dexwar.xml) script for auto converting *war* files to the Dalvik format.

## Usage

When you look for a web server for mobile devices as **Atjeews**, you can ask yourself - what is a usage for them? Surprisingly, an area for such server applications is a very wide.
First, a developing web applications using web technologies is simpler than using a native applications creation, or using cross platform tools like Flutter, React Native and other.
It's also simple to share such applications for multiple devices, as other phones, tablets, or desktop computers. Directory [doc/mycontacts](https://github.com/drogatkin/Atjeews/tree/master/doc/mycontacts)
contains a simple starting application, you can develop in a matter of 5 minutes, and then deploy it and run on your phone and see the result:


![mycontacts screen shot](https://github.com/drogatkin/Atjeews/blob/master/doc/mycontacts.png?raw=true)


## Known problems
1. web.xml less deployment doesn't work

Please report any other problems you encountered.

## Google Play Store
You can still [download the server](
https://play.google.com/store/apps/details?id=rogatkin.mobile.web&hl=en) from the Google Play store. However the server requires some level
of an expertise which an ordinary user may not have. It is related to security concerns. Therefore the author
of the server decided to do not provide a frequent updates of Play store version and use the GitHub instead for distributing the server.


