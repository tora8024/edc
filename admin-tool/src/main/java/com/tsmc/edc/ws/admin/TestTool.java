package com.tsmc.edc.ws.admin;

import java.util.ArrayList;
import java.util.List;

import com.tsmc.edc.utils.DeployUtils;
import com.tsmc.edc.utils.SysConstant;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.example.util.Runner;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class TestTool extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(DeployUtils.class);
	final int SERVICE_PORT=8086;
	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {
		Runner.runExample(TestTool.class);
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		
		Router router = Router.router(vertx);
	    router.route().handler(BodyHandler.create());

		router.get("/edc/test/getInstanceInfo").handler(this::handleGetInstanceInfo);
		

		vertx.createHttpServer().requestHandler(router::accept).listen(SERVICE_PORT);
		for(Route r:router.getRoutes()){
			if(r.getPath()==null || r.getPath().length()==0)continue;
			logger.info("Provide ws path:"+r.getPath());
		}
	}
	


	private void handleGetInstanceInfo(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
	
		try {

			Context context=vertx.getOrCreateContext();
			
			JsonObject instanceInfo=new JsonObject();
			instanceInfo.put("config", context.config().toString());
			instanceInfo.put("InstanceCount", vertx.getOrCreateContext().getInstanceCount());
			instanceInfo.put("InstanceDeploymentID", vertx.getOrCreateContext().deploymentID());
			
			DeployUtils.feedBack(SysConstant.SUCCESS , response, instanceInfo.toString());
			
		} catch (Exception e) {
			String msg=String.format("getVertxConfig fail,error msg:%s",e.getMessage());
			logger.error(msg,e);
			DeployUtils.feedBack(SysConstant.FAIL , response,msg);
		}		
	}

}
