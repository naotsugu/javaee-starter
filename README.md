# JavaEE Starter

This is a Java EE(Jakarta EE) scaffolding project.


## Usage

```
$ ./gradlew run
```

Build is executed, the browser will be launched, and the application will start.


## Detection of source changes

If you edit a web resource, it will be automatically reflected in your application.
Web resources are located in 'web/src/main/webapp'.


If you have edited the source files, you can redeploy the application by building it in another terminal with the following command:

```
$ ./gradlew war
```

