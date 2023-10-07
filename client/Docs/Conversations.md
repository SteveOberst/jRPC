# Conversations
Conversations probably are the most important part to know about and use properly in this framework.
This article focuses on explaining its functionality and how it's supposed to be used.

## What is a Conversation?
A conversation always represents a 'conversation' between to clients in which there is a request and a response.
Conversations allow us to keep track of data being sent out and the responses they trigger. The Conversation
object defines a lot of useful methods to tell the framework how to behave when communicating with another client.

## How they work
Any write operation through the JRPCClient will return a Conversation object which contains methods that
enable us to define how we will handle the response. 
