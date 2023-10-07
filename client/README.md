# jRPC Client
## Gradle dependency
To add the project as gradle dependency, you need to reference it in your dependencies as follows:
```groovy
dependencies {
    // For native Java applications
    implementation 'com.github.SteveOberst.jRPC:client:<version>'
}
```
Current version: <>

In your repositories:
```groovy
repositories {
    maven { url('https://jitpack.io') }   
}
```

## Getting Started
To get started you should instantiate JRPCClient and call the open(); method in your startup code
```java
public class Main {
    public static void main(String[] args) {
        JRPCClient client = new JRPCClient();
        client.open();
    }
}
```
This will do two things, first it will create a configuration file in the current running directory,
if you want to manually override the data folder you can do that by passing it as parameter into the
constructor. Second it will attempt to open a connection to the server on the port specified in the config.yml.
```
JRPCClient client = new JRPCClient(String dataFolder, boolean setUncaughtExceptionHandler);
```

# Configuring
On first launch, a configuration file will be created in the data folder. 
```yaml
# Authors: SteveOberst
# GitHub: https://github.com/SteveOberst/

# The output level of the logger
log-level: ALL

# The Clients unique identifier. Requires no specific format, it should just be unique between all connected clients.
unique-id: <randomly-generated>

# This value defines the type of server in the network.
# It's used for load-balancing between clients and to group up multiple client instances
# in order to cherry-pick message targets.
type: client

# Authentication token used to authenticate with the server
authentication-token: <randomly-generated>

# IP-Address the server is running on
server-address: localhost

# Port the server is running on
server-port: 2777

# Whether the client will accept messages from a source with a different version number
allow-version-mismatch: false

# Whether to try and continue if no handshake information have been received from the server
ignore-handshake-result: false

# If set to true, the client will schedule a reconnect if the connection to the server has been lost
auto-reconnect: true

# The interval in which the client will attempt to reconnect
reconnect-interval: 30

# Whether to queue messages if the socket is currently closed
queue-messages: true

# How long messages will be queued for. Set to -1 to queue until connection has been re-established.
queued-message-timeout: 30

# The amount of time the client will wait for a response of the other side in ms.
# This is solely a default value and may be overridden within the code.
conversation-time-out: 1000
```
Most notably, you need to set the ```authentication-token``` to the one in your servers config file. On creation
of the config file, the ```unique-id``` will be set to a random UUID value, you can go ahead and change it to something
more humanly readable as it doesn't require a specific format. If you have different types of clients you should
also specify a type in order to group clients as this will be relevant for load balancing and message target
selection.

If you're done configuring the client and everything went well, you should see something like this in your console
```
[19:11:07] [Server thread/INFO] [JRPCClient] Opening socket...
[19:11:07] [Server thread/INFO] [JRPCClient] Attempting to connect to localhost:2777
[19:11:07] [Server thread/INFO] [JRPCClient] Successfully opened connection..
[19:11:07] [Server thread/INFO] [JRPCClient] Attempting to handshake server localhost:2777. [Auth Token: 3e9cdaa8-0f53-4f28******************]
[19:11:07] [Server thread/INFO] [JRPCClientChannelHandler] Awaiting handshake response...
[19:11:07] [nioEventLoopGroup-2-1/INFO] [JRPCClientHandshakeHandler] Successfully authenticated with the server.
```

You can now go ahead and start publishing messages to other clients. For more information visit the 
[Sending and Receiving data](./Docs/Sending%20and%20Receiving%20data.md) page.