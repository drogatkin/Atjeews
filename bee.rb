# the build script is for building Atjeews

android_sdk ="/home/dmitriy/Android/Sdk"
jdk_root =""
tjws_src ="/home/dmitriy/projects/TJWS2/1.x"
jasper ="/home/dmitriy/projects/jasper-8.5.78"
websoketx ="false"
fast_scanner ="/home/dmitriy/projects/fast-classpath-scanner/lib"
keystore ="/home/dmitriy/Documents/android-publish.keystore"
sdklib_custom ="/home/dmitriy/projects/sdklib/build"
target ="34"
min_sdk= "6"
version_name ="1.49"
 version_code ="25"
build_tool ="30.0.3"
 build_directory= "build"
apk file=atjeews
playstore list=false

aidl=${android_sdk}/build-tools/${build_tool}/aidl
aapt2=${android_sdk}/build-tools/${build_tool}/aapt2
d8=${android_sdk}/build-tools/${build_tool}/d8
tmpdir=/tmp
apk tmp dir=${tmpdir}/apk_temp

target remove_old {
     dependency {true}
    {
        display(Cleaning the working folder)
        exec rm (-r,${apk tmp dir}/)
        if {
         neq(${~~}, 0)
         then {
            display("Can not clean the working directory, please do it manually and restart the script")
         }
       }
    }
}
apk gen dir=${tmpdir}/apk_temp/gen
target "create dirs" {
        dependency {target(remove_old)}
       {
           exec mkdir(-p, ${apk gen dir})
       }
}

target "idl gen" {
   dependency {target(create dirs)}
   {
      exec aidl(
            -I,
            src,
            -o,
             ${apk gen dir},
            src/rogatkin/mobile/web/RCServ.aidl)
   }
}

target "pack_res"{
         dependency {target(idl gen)}
         {
            exec "aapt2"(
                compile,
                -o,
               ${apk gen dir},
             
               --dir,
               res)
        }
}

target "link_res"{
       dependency {target("pack_res")}
      {
         newerthan(${apk gen dir}/.flat)
         assign(apk gen file,~~)
         assign(manifest file,AndroidManifest-playstore.xml)  # -playstore
        if {
          eq(playstore list, false)
          then {
             assign(manifest file,AndroidManifest.xml)
          }
       }
       display (Manifest ${manifest file})
         exec "aapt2"(
             link,
             apk gen file,
             --java,
             ${apk gen dir},
             -I, 
             ${android_sdk}/platforms/android-${target}/android.jar,
             --min-sdk-version,
             ${min_sdk},
             --target-sdk-version,
             ${target},
             -o,
             Atjeews.apk.link,
             --manifest, 
             manifest file,
             --version-code,
             ${version_code},
             --version-name,
             ${version_name},
             -v)
      }
}

CUSTOM CP=[${tjws_src}/lib/webserver.jar,${tjws_src}/lib/war.jar,${tjws_src}/lib/wskt.jar,${jasper}/build/jasper.jar,${fast_scanner}/class-scanner.jar]

comp target=1.8

target compile{
     dependency{ target(check build)}
     dependency{ target(link_res)}
   {
    
    newerthan(src/rogatkin/mobile/web/.java)
    assign(java sources,~~)
    scalar(CUSTOM CP,${~path_separator~})
    assign(class path,${~~})
    display(Sources: ${java sources} and cp ${class path})
    exec "javac"(
       -Xbootclasspath:${android_sdk}/platforms/android-${target}/android.jar,
       -classpath,
       ${class path},
       -source,
       ${comp target},
       -target,
       ${comp target},
       -d,
       ${build_directory},
      java sources,
      ${apk gen dir}/rogatkin/mobile/web/RCServ.java,
      ${apk gen dir}/rogatkin/mobile/web/R.java)
      if {
         neq(${~~}, 0)
         then {
            panic("compilation error(s)")
         }
     }
  }
}

target "jar"{
    dependency{ target(compile)}

    {
       display(Jarring ${apk file}...)
       exec jar (
         -cf,
       ${build_directory}/${apk file}.jar,
       -C,
       ${build_directory},
       rogatkin)
      if {
         neq(${~~}, 0)
         then {
            panic("jarring error(s)")
         }
     }
   }
}

target d8{
  
     dependency{ target(jar)}

    {
      exec d8 (
            --lib,
            ${android_sdk}/platforms/android-${target}/android.jar,
            --release,
            --output,
            ${apk tmp dir},
            ${build_directory}/${apk file}.jar,
            CUSTOM CP)
      if {
         neq(${~~}, 0)
         then {
            panic("dex8ing error(s)")
         }
     }
   }
}

unpacked apk=${apk tmp dir}/${apk file}.apk

target complete{
      dependency{ target(d8)}
      {
         exec mkdir(unpacked apk)
     
     for zip file:CUSTOM CP {
        exec unzip(
            zip file,
            *.properties,
            *.dtd,
            *.xsd,
            META-INF/services/javax.websocket.server.ServerEndpointConfig$Configurator,
            *.ServletContainerInitializer,
            -d,
            unpacked apk)
     }
     # Note: the builder is patched to populate META-INF/... correctly 
     # The custom build mimics sdklib-26.0.0-dev.jar from Androllid SDK tools 
     exec java(
           -cp,
           ${sdklib_custom}/android-build.jar,
          com.android.sdklib.build.ApkBuilderMain,
         Atjeews.apk.unaligned,
         -u,
         -v,
         -z,
         Atjeews.apk.link,
         -f,
         ${apk tmp dir}/classes.dex,
         -rf,
         unpacked apk)
         if {
            neq(${~~}, 0)
            then {
              panic("apk building error(s)")
           }
        }

        assign(apk dir,${~cwd~})
        assign(tool dir,${android_sdk}/build-tools/${build_tool})

        exec ./zipalign:tool dir(
         -v,
         -f,
          4,
          ${apk dir}/Atjeews.apk.unaligned,
          ${apk dir}/${apk file}.align) 
         if {
            neq(${~~}, 0)
            then {
              panic("zip aligning error(s)")
           }
        }

      exec ./apksigner:tool dir(
          sign,
          -ks,
          ${keystore},
          ${apk dir}/${apk file}.align)
         if {
            neq(${~~}, 0)
            then {
              panic("apk signing error(s)")
           }
        }

      exec rm(
         Atjeews.apk.unaligned,
         Atjeews.apk.link)
     exec mv(
         ${apk file}.align,
         ${apk file}.apk)
     
     display(Done.)
   }
}
