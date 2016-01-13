## How to use in Android Studio

### Add dependencies

Add the following to your project's build.gradle file, in the buildScript dependency section so we can use android-apt.

```gradle
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
```

The app's build.gradle looks like the following.

```gradle
apply plugin: 'com.android.application'
apply plugin: 'com.neenbedankt.android-apt'

...
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    testCompile 'junit:junit:4.12'
    compile 'com.android.support:appcompat-v7:22.2.1'
    compile 'la.dahuo:command:1.0.0'
    apt 'la.dahuo:command-codegen:1.0.0'
}
```

### Define Command

```java
@CommandDef("commandName")
public class MyCommand extends Command {

    @Param
    String paramStr;

    @Param("paramIntName")
    int paramInt;

    @Override
    public void execute() {
        // do something with params
    }
}
```

### Use Command

```java
Map<String, Object> params = new HashMap<>();
params.put("paramStr", "string");
params.put("paramIntName", 1);
Command.parse("commandName", params).execute();
```

### Proguard

Add following line to your proguard config file.
```
-keep class la.dahuo.command.CommandRegisters {*;}
```

You can put your commands class in any where you like, all commands will be auto registered.