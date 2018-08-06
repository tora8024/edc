package com.tsmc.edc.utils;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.maven.resolver.ResolutionOptions;

public class DeployUtils extends AbstractVerticle {

	static final Logger logger = LoggerFactory.getLogger(DeployUtils.class);

	public static void doDeploy(Vertx vertx, String serviceFactory,
			Future<Void> future) throws Exception {

		logger.info(String
				.format("[doDeploy]serviceFactory:%s", serviceFactory));


		Context context = vertx.getOrCreateContext();
		if (context.isEventLoopContext()) {
			logger.info("Context attached to Event Loop");
		} else if (context.isWorkerContext()) {
			logger.info("Context attached to Worker Thread");
		} else if (context.isMultiThreadedWorkerContext()) {
			logger.info("Context attached to Worker Thread - multi threaded worker");
		} else if (!Context.isOnVertxThread()) {
			logger.info("Context not attached to a thread managed by vert.x");
		}
		// The `my-verticle` is deployed using the following convention:
		// `maven:` + groupId + `:` + artifactId + `:` + version + `::` +
		// verticle name
		// deployServiceFactory like this
		// "maven:io.vertx:maven-service-factory-verticle:3.5.3::my-verticle"
		vertx.deployVerticle(
				serviceFactory,
				ar -> {
					if (ar.succeeded()) {
						logger.info(String
								.format("Deploy ServiceFactory:%s success,deploymentId:%s",
										serviceFactory, ar.result()));
						
						JsonObject newDeployedObj = new JsonObject().put(serviceFactory,ar.result());
						context.config().getJsonArray(SysConstant.DEPLOYED_VERTICLES).add(newDeployedObj);
						
						
						logger.info(String.format(
								"[doDeploy]serviceFactory:%s, deploymentId:%s",
								serviceFactory, ar.result()));
						future.complete();
					} else {
						logger.error(String.format(
								"Deploy ServiceFactory:%s fail, exception:%s",
								serviceFactory, ar.cause().getMessage()), ar
								.cause());

						future.fail(ar.cause());
					}
				});
	}

	public static void doUndeploy(Vertx vertx, String serviceFactory,
			String deploymentId, Future<Void> future) throws Exception {
		logger.info(String.format(
				"[doUndeploy]serviceFactory:%s, deploymentId:%s",
				serviceFactory, deploymentId));
		Context context = vertx.getOrCreateContext();
		if (context.isEventLoopContext()) {
			logger.info("Context attached to Event Loop");
		} else if (context.isWorkerContext()) {
			logger.info("Context attached to Worker Thread");
		} else if (context.isMultiThreadedWorkerContext()) {
			logger.info("Context attached to Worker Thread - multi threaded worker");
		} else if (!Context.isOnVertxThread()) {
			logger.info("Context not attached to a thread managed by vert.x");
		}
		// The `my-verticle` is deployed using the following convention:
		// `maven:` + groupId + `:` + artifactId + `:` + version + `::` +
		// verticle name
		// serviceFactory like this
		// "maven:io.vertx:maven-service-factory-verticle:3.5.3::my-verticle"
		
		vertx.undeploy(
				deploymentId,
				ar -> {
					if (ar.succeeded()) {
						
						//remove this serviceFactory and deploymentID
						
						JsonArray deployedVerticlesList =vertx.getOrCreateContext().config().getJsonArray(SysConstant.DEPLOYED_VERTICLES);
						deployedVerticlesList.forEach(item -> {
						    JsonObject obj = (JsonObject) item;
						    if(obj.containsKey(serviceFactory) && obj.getString(serviceFactory).equalsIgnoreCase(deploymentId)){
						    	deployedVerticlesList.remove(item);
								logger.info(String
										.format("Undeploy ServiceFactory:%s success,deploymentId:%s",
												serviceFactory, deploymentId));
						    }
						});
						future.complete();
					} else {
						logger.error(String.format(
								"Undeploy deploymentId:%s fail, exception:%s",
								deploymentId, ar.cause().getMessage()), ar
								.cause());

						future.fail(ar.cause());
					}
				});
	}
	public static void feedBack(String result, HttpServerResponse response,	String msg) {
		feedBack(200, result, response, msg);
	}
	

	private static void feedBack(int statusCode, String result, HttpServerResponse response,
			String msg) {
		JsonObject rsp = new JsonObject();
		rsp.put("result", result);
		rsp.put("message", msg);
		response.setStatusCode(statusCode).end(rsp.toBuffer());
	}
}
