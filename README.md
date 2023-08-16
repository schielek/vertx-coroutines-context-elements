# vertx-coroutines-context-elements
Demonstrate the problem with vert.x's coroutine bridge when it comes to context elements

## Goal
The goal is to install a handler for every route that sets some thread-local value from the requests header.
Thread-local values must passed into a coroutine `ContextElement` in order to preserve them when a thread changes.
Since vert.x's handlers break coroutine boundaries, I cannot imagine a way to set the coroutine context for subsequent handlers.
