# jRPC Remote Procedure Call Framework
jRPC is a NIO, Multithreaded RPC Framework that uses Netty for networking under the hood, natively 
supporting platforms like Bukkit.

### Overview
Experience lightning-fast request/response handling with our high-performance framework. Send up to 300,000 requests per second whilst simultaneously efficiently managing responses.

## Quick Start
In order to get started using the Framework check out the [wiki](https://github.com/SteveOberst/jRPC/wiki/). The project
is build with java 17 and will therefore require your project to be run and compiled with a java-17
compatible jre/jdk too.

## About
### Features

- **Extreme Throughput:** Achieve exceptional performance, ideal for high-demand scenarios.

- **Asynchronous Processing:** Send requests rapidly and process responses efficiently.

- **Parallel Response Handling:** Seamlessly handle responses in parallel, ensuring responsiveness.

- **Scalability:** Easily scale horizontally to meet growing demands.

### Testing Hardware

Our framework's performance has been rigorously tested on a Ryzen 9 3900X processor, ensuring it meets the highest standards of efficiency and speed.

### Use Cases

- Web Services
- Microservices
- Real-Time Analytics

... And much more!

### Skyline is the limit
In our conducted tests we were able to write 2.5 million requests with 114 bytes of data whilst simultaneously receiving responses with 144 bytes of data in 9264 milliseconds with a peak CPU usage of only 30%.

### Get Started

1. [Native Java Application](https://github.com/SteveOberst/jRPC/wiki/Client)
2. [Bukkit](https://github.com/SteveOberst/jRPC/wiki/Bukkit)
3. [How the internals work](https://github.com/SteveOberst/jRPC/wiki/Internals)

## Gradle dependency
To add this project as a dependency for your Gradle project, make sure your dependencies section of your
build.gradle looks like the following:
```groovy
dependencies {
    // For native Java applications
    implementation 'com.github.SteveOberst.jRPC:client:<version>'
    // For Bukkit
    compileOnly 'com.github.SteveOberst.jRPC:bukkit:<version>'
    // ...
}
```
In order for your build tool to resolve the dependency you will need to provide jitpack.io as repository:
```groovy
repositories {
    maven { url('https://jitpack.io') }   
}
```
In order to include the project in your own project, you will need to use the shadowJar plugin.
If you don't have it already, add the following to the top of your file:
```groovy
plugins {
    // ...
    id "com.github.johnrengelman.shadow" version "7.1.2"
}
```
# Server
The Framework relies on it's integrated server to deploy messages across the network. 
You can download it in the release section, and you can simply run it from the command line using
java -jar jrpc-server.jar. Note that it was compiled with java 17. This will create a 'config' folder
containing the servers configuration file which will look as follows:
```yaml
# Authors: SteveOberst
# GitHub: https://github.com/SteveOberst/

# The output level of the logger
log-level: INFO

# The servers unique id in the network
server-id: 2ef6c3bf-eaac-438a-bd81-2745ff396c06

# Authentication token required for clients in order to authenticate with the server
authentication-token: <randomly-generated>

# Port the server will be running on
port: 2777

# Whether the server will accept messages sent from a client with a different version number
allow-version-mismatch: false

# Whether a client can send a message to themselves
allow-self-forward: false
```
First things first, you will need the authentication token in order to authenticate with the server.
The client will create its own 'config' directory on first launch where you will need to enter the 
authentication-token that's specified in the server's config file.

## Bukkit
In order to use the bukkit version in your project you will need to have the JRPC Bukkit plugin installed.
You can get it here. Make sure to add the dependency on the bukkit module to your project as show in the 
Gradle dependency section.

```groovy
repositories {
    maven { url('https://jitpack.io') }
}

dependencies {
    compileOnly 'com.github.SteveOberst.jRPC:bukkit:<version>'
    // ...
}
```

Do not shade the dependency into your plugin and make sure to depend on it in your plugin.yml.
```yaml
depend:
  - JRPCBukkit
  #...
```

## License
```
Copyright (c) 2023 Steve Oberst

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
