package com.tsmc.edc.utils;

import java.io.File;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.DeploymentManager;
import io.vertx.core.impl.FileResolver;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.maven.resolver.ResolutionOptions;

public class DeployUtils  {

	static final Logger logger = LoggerFactory.getLogger(DeployUtils.class);
	
	public static void doDeployByFile(Vertx vertx, String serviceFactory, String fileName,
			Future<String> future) throws Exception {
//		DeploymentManager deploymentManager = new DeploymentManager((VertxInternal) vertx);
//		VertxOptions options=new VertxOptions();
//		FileResolver fileResolver = new FileResolver(vertx, options.isFileResolverCachingEnabled());
//		fileResolver.resolveFile(fileName);
		DeploymentOptions options =new DeploymentOptions();
		
		JarAdder j=new JarAdder();

		j.addJarToClasspath(new File("C:/Users/°¶³Ç/.m2/repository/io/vertx/vertx-service-proxy/3.5.3/vertx-service-proxy-3.5.3.jar"),vertx,options);
		
		j.addJarToClasspath(new File(fileName),vertx,options);
		
		vertx.setTimer(5000, handler->{
			try {
				doDeploy( vertx,  serviceFactory, future);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
	}

	public static void doDeploy(Vertx vertx, String serviceFactory,
			Future<String> future) throws Exception {

//		logger.info(String
//				.format("[doDeploy]serviceFactory:%s", serviceFactory));


//		Context context = vertx.getOrCreateContext();
//		if (context.isEventLoopContext()) {
//			logger.info("Context attached to Event Loop");
//		} else if (context.isWorkerContext()) {
//			logger.info("Context attached to Worker Thread");
//		} else if (context.isMultiThreadedWorkerContext()) {
//			logger.info("Context attached to Worker Thread - multi threaded worker");
//		} else if (!Context.isOnVertxThread()) {
//			logger.info("Context not attached to a thread managed by vert.x");
//		}
		// The `my-verticle` is deployed using the following convention:
		// `maven:` + groupId + `:` + artifactId + `:` + version + `::` +
		// verticle name
		// deployServiceFactory like this
		// "maven:io.vertx:maven-service-factory-verticle:3.5.3::my-verticle"
		DeploymentOptions options=new DeploymentOptions();
		
		vertx.deployVerticle(
				serviceFactory,options,
				ar -> {
					if (ar.succeeded()) {
						
					
						String msg =String.format(
								"[doDeploy Success]Group: %s, instance:%s,  serviceFactory:%s, deploymentId:%s",
								options.getIsolationGroup(), options.getInstances() ,serviceFactory, ar.result());
						logger.info(msg);
						future.complete(ar.result());
					} else {
						logger.error(String.format(
								"[doDeploy Fail] ServiceFactory:%s fail, exception:%s",
								serviceFactory, ar.cause().getMessage()), ar
								.cause());

						future.fail(ar.cause());
					}
				});
	}

	public static void doUndeploy(Vertx vertx, String serviceFactory,
			String deploymentId, Future<String> future) throws Exception {
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
						
//						JsonArray deployedVerticlesList =vertx.getOrCreateContext().config().getJsonArray(SysConstant.DEPLOYED_VERTICLES);
//						deployedVerticlesList.forEach(item -> {
//						    JsonObject obj = (JsonObject) item;
//						    if(obj.containsKey(serviceFactory) && obj.getString(serviceFactory).equalsIgnoreCase(deploymentId)){
//						    	deployedVerticlesList.remove(item);
//								logger.info(String
//										.format("Undeploy ServiceFactory:%s success,deploymentId:%s",
//												serviceFactory, deploymentId));
//						    }
//						});
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
