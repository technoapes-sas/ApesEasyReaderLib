-keep class com.apes.capuchin.capuchinrfidlib.** { *; }
-keep class com.apes.capuchin.rfidcorelib.** { *; }
-keep interface com.apes.capuchin.capuchinrfidlib.** { *; }
-keep interface com.apes.capuchin.rfidcorelib.** { *; }
-keepclassmembers class com.apes.capuchin.capuchinrfidlib.** { *; }
-keepclassmembers class com.apes.capuchin.rfidcorelib.** { *; }

-keep class java.lang.** { *; }
-keep interface java.lang.** { *; }
-keepclassmembers class java.lang.** { *; }

-dontwarn java.lang.invoke.StringConcatFactory

-keep class com.rscja.deviceapi.** { *; }
-keep interface com.rscja.deviceapi.** { *; }
-keepclassmembers class com.rscja.deviceapi.** { *; }

-keep class com.zebra.rfid.api3.** { *; }
-keep class com.zebra.scannercontrol.** { *; }
-keep interface com.zebra.rfid.api3.** { *; }
-keep interface com.zebra.scannercontrol.** { *; }
-keepclassmembers class com.zebra.rfid.api3.** { *; }
-keepclassmembers class com.zebra.scannercontrol.** { *; }