# Request-Response-Model
So why exactly should we respond to every request, even if we're just sending some set-and-forget
data? This is what I'll talk about in this article.

## First things first
First off, we're not exactly following the controversial Request-Response model as you might know
it from the http protocol as we're not exactly 'waiting' for the response. The framework is based around
NIO, and we do not want any waiting time on our threads. Under the hood conversations are being kept track
of by a 64-bit integer value that makes every conversation unique. When incoming data is received,
the framework checks if there is a conversation awaiting the incoming data (so, the response),
which will then lead to a callback being invoked.

## The Idea
For every data we send out, we want an approval that our data has arrived and that it has been
processed correctly. Not doing this would mean that we could never be sure that there was no error
on the other side and that the operation has succeeded. Therefor we want an approval from the other
side after every request, to verify that the task has been completed successfully.

## Example
According to this model, this would be one way to implement models for conversations in your application.
```java
public class MyConversation {
    
    @AllArgsConstructor
    public static class Request {
        public byte[] myData;
    }
    
    @AllArgsConstructor
    public static class Response {
        public Request request;
        public boolean operationSucceeded;
    }
    
    public static class Handler implements MessageHandler<Request> {
        @Override
        public void onReceive(final @NonNull MessageContext<Request> context) {
            // process data...
            context.replyDirectly(new Response(request, true));
        }

        @Override
        public boolean shouldAccept(final @NonNull Packet packet) {
            return packet instanceof Request;
        }
    }
}
```

## Do I have to stick to this convention?
The short answer is no, you don't. 

As with any other convention it is just a guideline, something you _should_ do and nothing you're forced to do.

Although the framework is designed around this model and I would highly 
suggest doing so. We always want to be aware of the state of our request and whether the data has
arrived and has been processed correctly. This way we can handle edge cases better, and it just overall
makes the application less error-prone.