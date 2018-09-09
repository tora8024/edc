package com.tsmc.edc.ws.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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

public class AdminTool2 extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(AdminTool2.class);
	static List<String> deploymentList=new ArrayList<String>();

	int i=0;

	int timer=5000;
	// Convenience method so you can run it in your IDE
	public static void main(String[] args) {

		Runner.runExample(AdminTool2.class);
		
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		
		
		
		
			List<String> list=new ArrayList<String>();
			list.add("service:service-ProcessorService");
			
			list.add("maven:io.vertx:service-consumer:3.5.3::service-ConsumerVerticle");


			Future<Void> f1 = Future.future();
			
			String fileName ="D:\\Workspace\\Vertx\\vertx-examples-master\\service-proxy-examples\\service-provider\\target\\service-provider-3.5.3.jar";
			
			
			deployVerticlesByFile(list.get(i++),fileName,f1);
			f1.setHandler(h->{
				
//				undeployVerticles("",deploymentList.get(deploymentList.size()-1), Future.future());
						
				Future<Void> f2 = Future.future();
				deployVerticles(list.get(i++),f2);
				
//				f2.setHandler(h2->{
//					Future<Void> f3 = Future.future();
//					deployVerticles(list.get(i++),f3);
					
//					f3.setHandler(h3->{
//						Future<Void> f4 = Future.future();
//						deployVerticles(list.get(i++),f4);
//						
//						f4.setHandler(h4->{
//							Future<Void> f5 = Future.future();
//							deployVerticles(list.get(i++),f5);
//							
//							f5.setHandler(h5->{
//								deploymentList.stream().forEach(str->{
//									System.out.println(str);
//								});
//							});
//						});
//						
//						
//					} );  
//				} );  
			} );  
		
		
	}
	private void deployVerticlesByFile(String serviceFactory, String fileName ,Future<Void> done) {
		try{
			if(StringUtils.isEmpty(serviceFactory))return;
			Future<String> future = Future.future();
			
			DeployUtils.doDeployByFile(vertx, serviceFactory, fileName, future); 
			future.setHandler(as -> {
				if(as.succeeded()){
					deploymentList.add(as.result());
					done.complete();
				}else{
					logger.info(as.result());	
					done.fail(as.result());
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	private void deployVerticles(String serviceFactory, Future<Void> done) {
		try{
			if(StringUtils.isEmpty(serviceFactory))return;
			Future<String> future = Future.future();
			
			DeployUtils.doDeploy(vertx, serviceFactory, future);
			future.setHandler(as -> {
				if(as.succeeded()){
					deploymentList.add(as.result());
					done.complete();
				}else{
					logger.info(as.result());	
					done.fail(as.result());
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void undeployVerticles(String serviceFactory,
			String deploymentId, Future<Void> done) {
		try{
//			if(StringUtils.isEmpty(serviceFactory))return;
			Future<String> future = Future.future();
			
			DeployUtils.doUndeploy(vertx, serviceFactory,
					deploymentId, future);
			future.setHandler(as -> {
				if(as.succeeded()){
//					deploymentList.add(as.result());
					done.complete();
				}else{
					logger.info(as.result());	
					done.fail(as.result());
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
