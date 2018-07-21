/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tsmc.edc.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.example.mqtt.util.Runner;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

import java.nio.charset.Charset;

/**
 * An example of using the MQTT client
 */
public class Subscriber extends AbstractVerticle {

  final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String MQTT_TOPIC = "my_topic";
  private static final String BROKER_HOST = "localhost";
  private static final int BROKER_PORT = 1883;

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(Subscriber.class);
  }

  @Override
  public void start() throws Exception {
    MqttClientOptions options = new MqttClientOptions().setKeepAliveTimeSeconds(2);

    MqttClient client = MqttClient.create(Vertx.vertx(), options);


    // handler will be called when we have a message in topic we subscribing for
    client.publishHandler(publish -> {
      logger.info("Just received message on [" + publish.topicName() + "] payload [" + publish.payload().toString(Charset.defaultCharset()) + "] with QoS [" + publish.qosLevel() + "]");
      
      
      vertx.eventBus().send("address.writefile", publish.payload().toString(Charset.defaultCharset()));
      
    });

    // handle response on subscribe request
    client.subscribeCompletionHandler(h -> {
      logger.info("Receive SUBACK from server with granted QoS : " + h.grantedQoSLevels());

//      // let's publish a message to the subscribed topic
//      client.publish(
//        MQTT_TOPIC,
//        Buffer.buffer(MQTT_MESSAGE),
//        MqttQoS.AT_MOST_ONCE,
//        false,
//        false,
//        s -> logger.info("Publish sent to a server"));

      // unsubscribe from receiving messages for earlier subscribed topic
//      vertx.setTimer(5000, l -> client.unsubscribe(MQTT_TOPIC));
    });

    // handle response on unsubscribe request
    client.unsubscribeCompletionHandler(h -> {
      logger.info("Receive UNSUBACK from server");
//      vertx.setTimer(5000, l ->
//        // disconnect for server
//        client.disconnect(d -> logger.info("Disconnected form server"))
//      );
    });

    // connect to a server
    client.connect(BROKER_PORT, BROKER_HOST, ch -> {
      if (ch.succeeded()) {
        logger.info("Connected to a server");
        client.subscribe(MQTT_TOPIC, 0);
      } else {
        logger.info("Failed to connect to a server");
        logger.info(ch.cause());
      }
    });
  }
}
