# Atjeews

## Purpose
Android wrapper for TJWS

## Current status
The version of the Atjeews works with the latest Android SDK 32. It tested with TJWS 1.119,
after the compatibility with Java 8 restored. 

## Known problems
1. web.xml less deployment doesn't work
2. Websocket support fails with an exception: "java.lang.RuntimeException: Cannot load platform configurator" issued
   by missed directory META-INF/services in the Atjeews APK. (ApkBuilderMain bug needs to be fixed.)

Please report any other problems you encountered.
