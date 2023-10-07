# Response Handlers
If you do not want to use the functional style that Conversation objects provide to handle responses,
response handlers are for you.

Instead of using the functional style to handle responses
```java
public class MyClass {
    private JRPCClient client;
    
    public void foo() {
        client.write(/*...*/)
                .onResponse(/*...*/)
                .onExcept(/*...*/)
                .onTimeout(/*...*/);
    }
}
```
We can implement Conversation#ResponseHandler in order to provide the ``Conversation`` object with
a way to handle responses and get our code to be a little more organized.
```java
public class MyConversation {
    public static class Request extends Packet {
        //...   
    }

    public static class Request extends Packet {
        //...
    }
    
    public static class RequestHandler implements MessageHandler<Request> {
        //...
    }
    
    public static class RespondHandler implements Conversation.ResponseHandler<Request, Response> {
        public void onResponse(@NonNull TRequest request, @NonNull MessageContext<TResponse> messageContext) {
            // Handle responses
        }
        
        public <T extends ErrorInformationHolder> 
        void onExcept(@NonNull Throwable throwable, @NonNull T errorInformationHolder) {
            // Handle errors...
        }
        
        public void onTimeout(@NonNull TRequest request, @NonNull Set<MessageContext<TResponse>> responses) {
            // Handle Timeout...
        }
    }
}
```
You can then register the response handler in the Conversation object returned by a write operation:
```java
public class MyClass {
    private JRPCClient client;
    
    public void foo() {
        client.write(/*...*/).setResponseHandler(new MyConversation.ResponseHandler());
    }
}
```