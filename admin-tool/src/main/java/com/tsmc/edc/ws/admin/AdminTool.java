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

public class AdminTool extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(DeployUtils.class);
	final int SERVICE_PORT=8086;
	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {
		
		Runner.runExample(AdminTool.class);
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		
		Router router = Router.router(vertx);
	    router.route().handler(BodyHandler.create());

		router.post("/edc/adminTool/deployVerticles").handler(this::handleDeployVerticles);
		router.post("/edc/adminTool/undeployVerticles").handler(this::handleUndeployVerticles);
		router.post("/edc/adminTool/undeployVerticlesByDeploymentId").handler(this::handleUndeployVerticles);

		router.get("/edc/adminTool/getInstanceInfo").handler(this::handleGetInstanceInfo);
		

		vertx.createHttpServer().requestHandler(router::accept).listen(SERVICE_PORT);
		for(Route r:router.getRoutes()){
			if(r.getPath()==null || r.getPath().length()==0)continue;
			logger.info("Provide ws path:"+r.getPath());
		}
	}

	private void handleDeployVerticles(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		try {
			if(vertx.getOrCreateContext().config().containsKey(SysConstant.DEPLOYED_VERTICLES)==false){
				vertx.getOrCreateContext().config().put(SysConstant.DEPLOYED_VERTICLES, new JsonArray());
			}
			String serviceFactory = routingContext.getBodyAsString();
			if (serviceFactory == null || serviceFactory.length()==0) {
				throw new Exception("serviceFactory is empty");
			} 
	
			Future<Void> future = Future.future();
		
			DeployUtils.doDeploy(vertx, serviceFactory, future);
			future.setHandler(as -> {
				if(as.succeeded()){
					String msg="doDeploy success";
					logger.info(msg);	
					DeployUtils.feedBack(SysConstant.SUCCESS , response,msg);
					
				}else{
					String msg=String.format("doDeploy fail,error msg:%s",as.cause().getMessage());
					logger.error(msg);
					DeployUtils.feedBack(SysConstant.FAIL , response,msg);
				}
			});
		} catch (Exception e) {
			String msg=String.format("doDeploy fail,error msg:%s",e.getMessage());
			logger.error(msg,e);
			DeployUtils.feedBack(SysConstant.FAIL , response,msg);
		}		
	}
	
	

	private void handleUndeployVerticles(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();

		try {

			String serviceFactory = routingContext.getBodyAsString();
			if (serviceFactory == null || serviceFactory.length()==0) {
				throw new Exception("serviceFactory is empty");
			} 

			List<String> deploymentIdList=new ArrayList<String>();

			JsonArray deployedVerticlesList =vertx.getOrCreateContext().config().getJsonArray(SysConstant.DEPLOYED_VERTICLES);
			if (deployedVerticlesList == null || deployedVerticlesList.size()==0) {
				throw new Exception(String.format("Not found any verticle deployed in this vertx instance.",serviceFactory));
			} 

			for(Object obj: deployedVerticlesList){
			    if ( obj instanceof JsonObject ==false ) {
			    	continue;
			    }
			    
			    JsonObject jsonObj= (JsonObject)obj;
		    	 if(jsonObj.containsKey(serviceFactory)){
		    		 deploymentIdList.add(jsonObj.getString(serviceFactory));
			    }
			}
			
			if (deploymentIdList == null || deploymentIdList.size()==0) {
				throw new Exception(String.format("This serviceFactory:%s don't deploy in this vertx instance.",serviceFactory));
			} 
			

			
			int completeCount=0;
			List<Future<Void>> results=new ArrayList<Future<Void>>();
			
			for(String deploymentId: deploymentIdList){
				Future<Void> future = Future.future();
				DeployUtils.doUndeploy(vertx, serviceFactory,deploymentId, future);
//				future.setHandler(as -> {
//					if(as.succeeded()){
//						String msg="doUndeploy success";
//						logger.info(msg);	
////						DeployUtils.feedBack(SysConstant.SUCCESS , response,msg);
//						
//					}else{
//						String msg=String.format("doUndeploy fail,error msg:%s",as.cause().getMessage());
//						logger.error(msg);
////						DeployUtils.feedBack(SysConstant.FAIL , response,msg);
//					}
//				});
//				results.add(future);
			}
			
//			int failCount=0;
//			while(completeCount<results.size()){
//				for (Future<Void> result : results){
//		            try
//		            { 
//		            	if(result.isComplete()){
//		            		if(result.failed())failCount++;
//		            		completeCount++;
//		            	}
//		            }
//		            catch (Exception e)
//		            {
//		               e.printStackTrace();
//		            }
//				}
//			}
//			
//			if(failCount ==0){
//				String msg="doUndeploy success";
//				logger.info(msg);	
//				DeployUtils.feedBack(SysConstant.SUCCESS , response,msg);
//				
//			}else{
//				String msg=String.format("doUndeploy fail,error msg:%s","zzzzzz");
//				logger.error(msg);
//				DeployUtils.feedBack(SysConstant.FAIL , response,msg);
//			}
			
			

			DeployUtils.feedBack(SysConstant.SUCCESS , response,"good");
			
		} catch (Exception e) {
			String msg=String.format("doUndeploy fail,error msg:%s",e.getMessage());
			logger.error(msg,e);
			DeployUtils.feedBack(SysConstant.FAIL , response,msg);
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
