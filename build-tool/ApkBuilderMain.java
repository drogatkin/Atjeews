/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.build;


import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Properties;
import java.util.HashSet;

/**
 * Command line APK builder with signing support.
 */
public final class ApkBuilderMain {

    private final static Pattern PATTERN_JAR_EXT = Pattern.compile("^.+\\.jar$",
            Pattern.CASE_INSENSITIVE);
            
    static HashSet<String> exclusions;

    /**
     * Main method. This is meant to be called from the command line through an exec.
     * <p/>WARNING: this will call {@link System#exit(int)} if anything goes wrong.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsageAndQuit();
        }

        System.err.println("\nTHIS TOOL IS DEPRECATED. See --help for more information.\n");

        try {
            File outApk = new File(args[0]);

            File dexFile = null;
            ArrayList<File> zipArchives = new ArrayList<File>();
            ArrayList<File> sourceFolders = new ArrayList<File>();
            ArrayList<File> jarFiles = new ArrayList<File>();
            ArrayList<File> nativeFolders = new ArrayList<File>();

            boolean verbose = false;
            boolean signed = true;
            boolean debug = false;

            int index = 1;
            do {
                String argument = args[index++];

                if ("-v".equals(argument)) {
                    verbose = true;

                } else if ("-d".equals(argument)) {
                    debug = true;

                } else if ("-u".equals(argument)) {
                    signed = false;

                } else if ("-z".equals(argument)) {
                    // quick check on the next argument.
                    if (index == args.length)  {
                        printAndExit("Missing value for -z");
                    }

                    zipArchives.add(new File(args[index++]));
                } else if ("-f". equals(argument)) {
                    if (dexFile != null) {
                        // can't have more than one dex file.
                        printAndExit("Can't have more than one dex file (-f)");
                    }
                    // quick check on the next argument.
                    if (index == args.length) {
                        printAndExit("Missing value for -f");
                    }

                    dexFile = new File(args[index++]);
                } else if ("-rf". equals(argument)) {
                    // quick check on the next argument.
                    if (index == args.length) {
                        printAndExit("Missing value for -rf");
                    }

                    sourceFolders.add(new File(args[index++]));
                } else if ("-rj". equals(argument)) {
                    // quick check on the next argument.
                    if (index == args.length) {
                        printAndExit("Missing value for -rj");
                    }

                    jarFiles.add(new File(args[index++]));
                } else if ("-nf".equals(argument)) {
                    // quick check on the next argument.
                    if (index == args.length) {
                        printAndExit("Missing value for -nf");
                    }

                    nativeFolders.add(new File(args[index++]));
                } else if ("-storetype".equals(argument)) {
                    // quick check on the next argument.
                    if (index == args.length) {
                        printAndExit("Missing value for -storetype");
                    }

                    // FIXME
                } else {
                    printAndExit("Unknown argument: " + argument);
                }
            } while (index < args.length);

            if (zipArchives.size() == 0) {
                printAndExit("No zip archive, there must be one for the resources");
            }

            readSettings();
            // create the builder with the basic files.
            ApkBuilder builder = new ApkBuilder(outApk, zipArchives.get(0), dexFile,
                    signed ? ApkBuilder.getDebugKeystore() : null,
                    verbose ? System.out : null);
            builder.setDebugMode(debug);

            // add the rest of the files.
            // first zip Archive was used in the constructor.
            for (int i = 1 ; i < zipArchives.size() ; i++) {
                builder.addZipFile(zipArchives.get(i));
            }

            for (File sourceFolder : sourceFolders) {
                builder.addSourceFolder(sourceFolder);
            }

            for (File jarFile : jarFiles) {
                if (jarFile.isDirectory()) {
                    String[] filenames = jarFile.list(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return PATTERN_JAR_EXT.matcher(name).matches();
                        }
                    });

                    for (String filename : filenames) {
                        builder.addResourcesFromJar(new File(jarFile, filename));
                    }
                } else {
                    builder.addResourcesFromJar(jarFile);
                }
            }

            for (File nativeFolder : nativeFolders) {
                builder.addNativeLibraries(nativeFolder, null /*abiFilter*/);
            }

            // seal the apk
            builder.sealApk();


        } catch (ApkCreationException e) {
            printAndExit(e.getMessage());
        } catch (DuplicateFileException e) {
            printAndExit(String.format(
                    "Found duplicate file for APK: %1$s\nOrigin 1: %2$s\nOrigin 2: %3$s",
                    e.getArchivePath(), e.getFile1(), e.getFile2()));
        } catch (SealedApkException e) {
            printAndExit(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void readSettings() {
        Properties settings = new Properties();
        exclusions = new HashSet<>();
        try {
          settings.load(new InputStreamReader(new FileInputStream("builder-settings.properties"), "UTF-8"));
          String exclusionsProp = settings.getProperty("excludedDir");
          if (exclusions != null) {
              for (String exclusion:exclusionsProp.split(",")) {
                  exclusions.add(exclusion. toUpperCase());
              }
          }
        } catch(Exception e) {
           System.err.println("Settings not read, because: " +e);
        }
        
    }

    private static void printUsageAndQuit() {
        // 80 cols marker:  01234567890123456789012345678901234567890123456789012345678901234567890123456789
        System.err.println("\n\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.err.println("THIS TOOL IS DEPRECATED and may stop working at any time!\n");
        System.err.println("If you wish to use apkbuilder for a custom build system, please look at the");
        System.err.println("com.android.sdklib.build.ApkBuilder which provides support for");
        System.err.println("recent build improvements including library projects.");
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
        System.err.println("A command line tool to package an Android application from various sources.");
        System.err.println("Usage: apkbuilder <out archive> [-v][-u][-storetype STORE_TYPE] [-z inputzip]");
        System.err.println("            [-f inputfile] [-rf input-folder] [-rj -input-path]");
        System.err.println("");
        System.err.println("    -v      Verbose.");
        System.err.println("    -d      Debug Mode: Includes debug files in the APK file.");
        System.err.println("    -u      Creates an unsigned package.");
        System.err.println("    -storetype Forces the KeyStore type. If ommited the default is used.");
        System.err.println("");
        System.err.println("    -z      Followed by the path to a zip archive.");
        System.err.println("            Adds the content of the application package.");
        System.err.println("");
        System.err.println("    -f      Followed by the path to a file.");
        System.err.println("            Adds the file to the application package.");
        System.err.println("");
        System.err.println("    -rf     Followed by the path to a source folder.");
        System.err.println("            Adds the java resources found in that folder to the application");
        System.err.println("            package, while keeping their path relative to the source folder.");
        System.err.println("");
        System.err.println("    -rj     Followed by the path to a jar file or a folder containing");
        System.err.println("            jar files.");
        System.err.println("            Adds the java resources found in the jar file(s) to the application");
        System.err.println("            package.");
        System.err.println("");
        System.err.println("    -nf     Followed by the root folder containing native libraries to");
        System.err.println("            include in the application package.");

        System.exit(1);
    }

    private static void printAndExit(String... messages) {
        for (String message : messages) {
            System.err.println(message);
        }
        System.exit(1);
    }

    private ApkBuilderMain() {
    }
}
