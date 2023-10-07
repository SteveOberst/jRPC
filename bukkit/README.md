# JRPC-Bukkit

## Foreword
The bukkit module of this project focuses on providing a base that plugin developers can work with. For the 
most part, all the magic happens in the JRPCService. It requires the bukkit plugin
to be installed on the server and to be configured properly. You can get the plugin here.

## Gradle dependency
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

## Getting started
The service is registered in bukkit's service registry, and you can simply get an instance of it via the bukkit api.
```java
public class MyPlugin extends JavaPlugin {
    
    private JRPCService jrpcService;
    
    public final void onEnable() {
        this.jrpcService = getJRPCService();
    }
    
    public final void onDisable() {
        
    }
    
    private JRPCService getJRPCService() {
        final RegisteredServiceProvider<JRPCService> serviceProvider = Bukkit.getServicesManager().getRegistration(JRPCService.class);
        if(serviceProvider == null) {
            getLogger().severe(String.format("%s is not initialized or the plugin is not installed.", JRPCService.class));
            Bukkit.getPluginManager().disablePlugin(this);
            return null;
        }
        return serviceProvider.getProvider();
    }
}
```
Now, the service itself doesn't do much besides providing an instance to a ``DefaultMessageProcessor`` and the
``JRPCClient``. In order to register message handlers, you can access the provided instance of the 
``DefaultMessageProcessor``.
```java
public class MyPlugin extends JavaPlugin {
    private JRPCService service;
    
    private void registerHandler(final MessageHandler<?> myHandler) {
        service.getMessageProcessor().registerHandler(myHandler);
    }
}
```
## Publishing Data
Let's take a look at how to write data. In order to publish data to other clients, the ``JRPCService`` 
provides a few helper methods to publish data.

Let's take a look at an example on how to broadcast messages. First we need to create the conversation model
and the message handler:
```java
public class BroadcastConversation {
    
    @AllArgsConstructor
    public static class Request extends Packet {
        public String message;
    }
    
    @AllArgsConstructor
    public static class Response extends Packet {
        public Request request;
        public boolean success;
    }
    
    public static class BroadcastMessageHandler implements MessageHandler<Request> {
        private final JRPCService service;

        public BroadcastMessageConversationHandler(final MyPlugin plugin) {
            this.service = plugin.getService();
        }

        @Override
        public void onReceive(@NonNull MessageContext<Request> context) {
            //... process request, maybe validate data
            final Request request = context.getRequest();
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', request.message));
            context.replyDirectly(new Response(request, true));
        }

        @Override
        public boolean shouldAccept(@NonNull Packet packet) {
            return packet instanceof Request;
        }
    }
}
```
Now we could have a class that manages messages published across the network:
```java
import java.util.concurrent.CompletableFuture;

public class NetworkMessageHandler {
    
    public CompletableFuture<Integer> broadcastMessage(final String message) {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        final BroadcastMessageHandler.Request request = new BroadcastMessageHandler.Request(message);
        service.broadcast(request, BroadcastMessageConversation.Response.class)
                .onTimeout((req, responses) -> {
                    Bukkit.getLogger().info(String.format("Broadcasted message to %d instance(s)", messageContexts.size()));
                    future.complete(responses.size());
                }).waitFor(200, TimeUnit.MILLISECONDS, true).alwaysNotifyTimeout();
        
        return future;
    }
}
```
