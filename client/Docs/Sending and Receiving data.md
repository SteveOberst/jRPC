# Sending and Receiving Data
You have finally configured the client and successfully opened a connection, and now you want
to know how to actually send and receive data? Then this article is for you.

## Client initialization
The framework comes with a ```DefaultMessageProcessor``` that is supposed to handle raw data and
allow for messages to be processed by handlers without having to think about (de-)serialization of the
data and error checking. You can also register your own```RawDataReceiver<T>```, however, I wouldn't
recommend on doing so unless you want to override the default processors basic logic. To register the
default message processor you can simply instantiate the class and pass it to the client:
```java
public class MyRPCHandler {
    // Keep an instance to this class as we need it later to register handlers
    // and interact with the data that is being received
    private DefaultMessageProcessor processor;
    
    // initialization...
    
    private void configure(final JRPCClient client) {
        this.processor = new DefaultMessageProcessor();
        client.registerMessageReceiver(processor);
    }
}
```
That's it. The DefaultMessageProcessor will now receive data that is being received and pass it
to it's registered handlers.

## Message Handlers
In order to actually process data, we need to register message handlers that handle incoming requests
and _respond_ accordingly. Why exactly we should respond to every request is talked about in the
[Request-Response-Model article](./Request-Response-Model.md).

First we should create a conversation class that defines our request, response and
the handler according to the Request-Response-Model.

```java
public class PingConversation {
    @AllArgsConstructor
    public static class Request extends Packet {
        public String message;
    }

    @AllArgsConstructor
    public static class Response extends Packet {
        public Request request;
        public String message;
    }
    
    public static class Handler implements MessageHandler<Request> {

        @Override
        public void onReceive(@NonNull MessageContext<Request> context) {
            context.replyDirectly(new Response(request, "Hello there!"));
        }

        @Override
        public boolean shouldAccept(@NonNull Packet packet) {
            return packet instanceof Request;
        }
    }
}
```

And then register the handler in our initialization code:
```java
public class MyRPCHandler {
    private DefaultMessageProcessor processor;
    
    public void initialize() {
        // initialize...
        registerHandlers();
    }
    
    public void registerHandlers() {
        processor.registerHandler(new PingConversation.PingConversationHandler());
    }
}
```

Now when a request from another client is being received, it should receive a response from our client 
accordingly.
```java
public class MyClass {
    public void sendData(final JRPCClient client, final Message.TargetType targetType, final String target) {
        client.write(new PingConversation.Request("Hello"), new MessageTarget(targetType, target), PingConversation.Response.class)
                .onResponse((request, context) -> {
                    System.out.printf("The client has responded with: '%s'", context.response.message);
                })
                .onTimeout((request, responses) -> {
                    System.out.printf("The request has timed out. %d Response(s) have been received. \n", responses.size());
                })
                .onExcept((throwable, errorInformationHolder) -> {
                    System.out.printf("An error occurred: %s", errorInformationHolder.getErrorDescription());
                }).overrideHandlers();
    }
}
```

If everything went well, this code should produce some output like
```
The client has responded with: 'Hello there!'
```
