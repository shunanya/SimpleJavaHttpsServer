# Simple Java HTTPS server #

The current project is example which is explained how to create simple HTTPS server on pure Java platform.

## Basic Steps ##

Create Maven project and name it e.g. as "SimpleJavaHttpsServer".
_(Naturally you can use Intellij Idea or other preferable IDE)_

In project root folder create `"_ssh"` folder
and put into it the certificate __server.pfx__ that was built as described in [API_Backend project](https://github.com/shunanya/API_Backend#)


In `"./src/main/java"` folder create the folders set which is the package 
for put there Java classes (e.g. `org/example/` or something like that) 
and create there classes named `Server.java` and `ServerV.java` (the content can be copied from current project).

**Note:** don't forget to check the correspondent of `package name` to folders set where located `server.java` class.

The folder `"./src/main/resources/"` should contain files which will be requesting from server 
(in our case that are `index.html` and `iot10.jpg`)

## Checking the server workability ##

Thus, goto project root folder:

    cd <your path>/SimpleJavaHttpsServer

and start the server (for using Platform Threading Model)

    java ./src/main/java/org/example/Server.java

or start the server (for using Virtual Threading Model)

    java --enable-preview --source 19 ./src/main/java/org/example/ServerV.java

**Note:** the current project uses JDK version not less than 19.x  
 
Put in browser https://localhost:8443/index.html and you should see Test Page ![test](./Screenshot.png)

That is all.
