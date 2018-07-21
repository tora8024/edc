package com.tsmc.edc.eventbus.module;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.example.util.Runner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * A {@link io.vertx.core.Verticle} which implements a simple, realtime,
 * multiuser chat. Anyone can connect to the chat application on port
 * 8000 and type messages. The messages will be rebroadcast to all
 * connected users via the @{link EventBus} Websocket bridge.
 *
 * @author <a href="https://github.com/InfoSec812">Deven Phillips</a>
 */
public class WriteFile extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private String handleAddress="address.writefile";
  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(WriteFile.class);
  }

  @Override
  public void start() throws Exception {

      logger.info("[Receiver] started");
    EventBus eb = vertx.eventBus();

    // Register to listen for messages coming IN to the server
    eb.consumer(handleAddress).handler(message -> {
      // Create a timestamp string
      String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));
      // Send the message back out to all clients with the timestamp prepended.
      System.out.println("[Receiver] receive a meaasage:"+message.body().toString());
      logger.info("[Receiver] receive a meaasage:"+message.body().toString());
      
      vertx.fileSystem().writeFile(".\\"+handleAddress+"_"+timestamp+".txt", Buffer.buffer(message.body().toString()), result -> {
    	    if (result.succeeded()) {
    	        System.out.println("File written");
    	        logger.info("file written");
    	    } else {
    	        System.err.println("Oh oh ..." + result.cause());
    	    }
    	});
    });

  }
}