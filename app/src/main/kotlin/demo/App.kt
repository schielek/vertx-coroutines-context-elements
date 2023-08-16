package demo

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC


class AppVerticle : CoroutineVerticle() {
    override suspend fun start() {
        val router = Router.router(vertx).apply {
            route().handler { ctx ->
                logger.info("This handler will set some thread local value")
                MDC.put("context", ctx.request().headers().get("context"))
                logger.info("But how can it pass it along to subsequent handlers as Coroutine-ContextElement?")
                ctx.next()
            }

            get("/demo").handler { ctx ->
                launch(vertx.dispatcher()) {
                    logger.info("Same thread, MDC preserved")

                    withContext(Dispatchers.Default + MDCContext()) {
                        logger.info("Thread changed, MDC preserved, but this handler must not know that the MDC context was set by a previous handler")
                    }

                    val response: String? = withContext(Dispatchers.Default) {
                        logger.info("Thread changed, MDC lost")
                        MDC.get("context")
                    }

                    ctx
                        .end(response.toString())
                        .await()
                }
            }
        }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .await()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}

fun main(): Unit = runBlocking {
    val logger = LoggerFactory.getLogger("main")

    val vertx = Vertx.vertx()
    val client = WebClient.create(vertx)

    vertx
        .deployVerticle(AppVerticle())
        .await()

    val response = client
        .get("http://localhost/demo")
        .port(8080)
        .apply {
            headers().add("context", ">>>some information<<<")
        }
        .send()
        .await()

    logger.info(response.bodyAsString())

    vertx.close().await()
}
