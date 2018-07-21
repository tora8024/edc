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

package com.tsmc.edc;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.example.mqtt.util.Runner;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;

import java.util.ArrayList;
import java.util.List;

/**
 * An example of using the MQTT server
 */
public class DeployTool extends AbstractVerticle {
   final Logger logger = LoggerFactory.getLogger(this.getClass());

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(DeployTool.class);
  }

  @Override
  public void start() throws Exception {
	// Deploy a verticle
	  vertx.deployVerticle("com.tsmc.edc.mqtt.Subscriber", new Handler<AsyncResult<String>>() {
	    public void handle(AsyncResult<String> result) {
	      // If the verticle was successfully deployed
	      if (result.succeeded()) {

		        // Get the deployment ID
		        String deploymentID = result.result();
		        logger.info(String.format("deploymentID:%s seccessed deploy.", deploymentID));
	    	  vertx.deployVerticle("com.tsmc.edc.eventbus.module.WriteFile", new Handler<AsyncResult<String>>() {
	  		    public void handle(AsyncResult<String> result) {
	  		      // If the verticle was successfully deployed
	  		      if (result.succeeded()) {
	  		        // Get the deployment ID
	  		        String deploymentID = result.result();
			        logger.info(String.format("deploymentID:%s seccessed deploy.", deploymentID));

	  		        // Undeploy the verticle using the deployment ID
	  		        vertx.undeploy(deploymentID);
	  		      }
	  		    }
	  		  });

	        // Undeploy the verticle using the deployment ID
	        vertx.undeploy(deploymentID);
	      }
	    }
	  });
	  
	 
  }
}
