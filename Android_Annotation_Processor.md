# Android注解处理器(Annotation Processor)

在这篇文章中, 我将阐述怎么写一个Android的注解处理器(Annotation Processor)。

## 基本概念 ##
注解处理器（Annotation Processor）是javac的一个工具，它用来在编译时扫描和处理注解（Annotation）。你可以对自定义注解，并注册相应的注解处理器。到这里，我假设你已经知道什么是注解，并且知道怎么申明的一个注解。如果你不熟悉注解，你可以在这官方文档中得到更多信息。注解处理器在Java 5开始就有了，但是从Java 6（2006年12月发布）开始才有可用的API。过了一些时间，Java世界才意识到注解处理器的强大作用，所以它到最近几年才流行起来。

一个注解的注解处理器，以Java代码（或者编译过的字节码）作为输入，生成文件（通常是.java文件）作为输出。这具体的含义什么呢？你可以生成Java代码！这些生成的Java代码是在生成的.java文件中，所以你不能修改已经存在的Java类，例如向已有的类中添加方法。这些生成的Java文件，会同其他普通的手动编写的Java源代码一样被javac编译。

## 使用注解处理器 ##
下面来讲述怎么在Android Studio中使用注解处理器.
在这里就需要介绍一个gradle插件android-apt了, 
官方地址: https://bitbucket.org/hvisser/android-apt

Android Studio原本是不支持注解处理器的, 但是用这个插件后, 我们就可以使用注解处理器了, 这个插件可以自动的帮你为生成的代码创建目录, 让生成的代码编译到APK里面去, 而且它还可以让最终编译出来的APK里面不包含注解处理器本身的代码, 因为这部分代码只是编译的时候需要用来生成代码, 最终运行的时候是不需要的.

使用这个插件很简单, 首先在你项目顶层的build.gradle文件中添加依赖项, 如下:

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```

然后在app的build.gradle里面添加插件的引用以及需要依赖哪些库, 如下:

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

注意上面的`apt 'la.dahuo:command-codegen:1.0.0'`, 这里表示引用一个注解处理器的库, 这个库的代码最终不会进入编译出来的APK里面.

## 使用注解 ##
下面就来介绍怎么使用注解生成代码, 上面出现的库la.dahuo:command, 
la.dahuo:command-codegen就是我根据命令设计模式(Command Design Pattern)写的一个注解库, 它用来让我们方便的生成代码, 看下用法:

### 定义Command ###

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
上面的代码通过@CommandDef("commandName")来标注MyCommand是一个Command类, 它对应的名字为commandName. 然后通过标注@Param来定义了两个参数, @Param不填值的话表示参数名和属性名一样, 也可以通过填入一个值来指定参数名

### 使用Command ###

```java
Map<String, Object> params = new HashMap<>();
params.put("paramStr", "string");
params.put("paramIntName", 1);
Command.parse("commandName", params).execute();
```

### 分析 ###
看完上面的用法, 你可能会觉得就这么多代码就可以跑了吗? 不需要再添加什么代码了?
我的答案是: 确实可以跑, 不需要添加其他代码了.
有木有觉得用起来很方便? 我们下面来分析下这是怎么做到的.

首先标注处理器会根据@Param来获取Command的结构信息, 然后用来生成CommandFactory, 生成的代码在build/generated/source/apt下面, 生成的CommandFactory如下:

```java
public class MyCommandFactory implements CommandFactory {
  public Command newCommand(Map<String, Object> params) {
    MyCommand command = new MyCommand();
    command.paramStr = TypeConverter.toString(params.get("paramStr"));
    command.paramInt = TypeConverter.toInt(params.get("paramIntName"));
    return command;
  }
}
```
有了CommandFactory, 我们就可以通过Map<String, Object> params来构造Command了.

那Command是怎么自动注册的呢? 那我们就要看另外一个生成的类了.

```java
public class CommandRegisters {
  public static void register(Map<String, CommandFactory> map) {
    map.put("myCommand", new MyCommandFactory());
  }
}
```
它根据标注@CommandDef来获取了所有Command类的信息, 然后完成所有Command的注册.

有了上面自动生成的代码, 那么解析Command就很简单了. 代码如下:

```java
public static Command parse(String action, Map<String, Object> params) 
{
    CommandFactory factory = CommandRepository.getInstance().getFactory(action);
    if (factory != null) {
        return factory.newCommand(params);
    }
    return EmptyCommand.INSTANCE;
}
```

CommandRepository类通过反射的方式调用CommandRegisters类的register方法来完成所有Command的注册. 这是我这个注解库唯一的一处使用反射的地方, Command的创建都通过CommandFactory来完成, 性能很高, 比那些通过大量反射来实现对象映射的库性能好很多(比如GSON, 可以实现json转object), 而且也不需要依赖很多代码, 依赖一个很小的库
la.dahuo:command就够了, 才5K.

## 怎么创建注解处理器 ##
注解处理器最核心的就是要有一个Processor, 它继承自AbstractProcessor，它长成这个样子：

```java
package com.example;
 
public class MyProcessor extends AbstractProcessor {
 
    @Override
    public synchronized void init(ProcessingEnvironment env){ }
 
    @Override
    public boolean process(Set<? extends TypeElement> annoations, RoundEnvironment env) { }
 
    @Override
    public Set<String> getSupportedAnnotationTypes() { }
 
    @Override
    public SourceVersion getSupportedSourceVersion() { }
}
```

 - init(ProcessingEnvironment env)：每个注解处理器都必须有个空的构造方法。不过，有一个特殊的init方法，它会被注解处理器工具传入一个ProcessingEnvironment作为参数来调用。ProcessingEnvironment提供了一些有用的工具类，如Elements，Types和Filter。我们后面会用到它们。

 - process(Set<? extends TypeElement> annotations, RoundEnvironment env)：这个方法可以看做每个处理器的main方法。你要在这里写下你的扫描，判断和处理注解的代码，并生成java文件。通过传入的RoundEnvironment参数，你可以查询被某个特定注解注解的元素，我们稍后会看到。

 - getSupportedAnnotationTypes( )：这里你需要说明这个处理器需要针对哪些注解来注册。注意返回类型是一个字符串的Set，包含了你要用这个处理器处理的注解类型的全名。

 - getSupportedSourceVersion( )：用于指定你使用的java版本。通常你会返回SourceVersion.latestSupported( )。

接下来你需要知道的一件事就是注解处理器在它自己的JVM中运行。是的，你没看错。javac启动一个完整的Java虚拟机来给注解处理器运行。这对你意味着什么？你可以使用你在任何其他java应用中使用的东西，比如谷歌的guava！如果你愿意，你还可以使用依赖注入工具，如dagger或任何你想要使用的库。不过不要忘了，尽管这只是个小的处理器，你依然需要考虑高效的算法和设计模式，就像是你会为任何其他Java应用所做的那样。

## 注册你的处理器 ##
你也许会问自己：”我该怎样把我的处理器注册到javac？”。你需要提供一个.jar文件。就像其他的jar文件一样，你要把编译后的处理器打包到那个文件。而且你还需要把一个特别的放在META-INF/services下的叫做javax.annotation.processing.Processor的文件打包进你的.jar文件。所以你的.jar文件内容看起来会像这个样子：

```java
MyProcessor.jar
    - com
        - example
            - MyProcessor.class
    - META-INF
        - services
            - javax.annotation.processing.Processor
```

java.annotation.processing.Processor文件（打包到MyProcessor.jar中）的内容是用换行符隔开的处理器完整类名：

```java
com.example.MyProcessor
com.foo.OtherProcessor
net.blabla.SpecialProcessor
```

这样, 一个注解处理器的框架就好了, 完成代码后编译成jar文件, 然后像开始介绍的那样添加依赖就好了.

如果你想要使用command-lib库, 请在proguard配置文件里面添加
`-keep class la.dahuo.command.CommandRegisters {*;}`

上面提到的我写的Command的注解库我已经传到Github上了, 
地址: https://github.com/derron/command-lib
有兴趣的可以"查看原文"去看看.

###扫描或长按关注我们的微信技术公众号-极客联盟
![极客联盟](https://dn-cloud-disk.qbox.me/images/jklm_qr.jpg)

###“崇尚自由，推崇技术，拥抱开源” - 极客联盟：传播新技术理念，分享技术经验。 打造华中区最有影响力的技术公众号。

## 我们正在招聘APP产品经理、PHP、测试、前端、IOS、Android、Java等岗位，有意向的可以联系我们: dahuo.contact@gmail.com


