package com.tsmc.edc.utils;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.IsolatingClassLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class JarAdder {
//	public JarAddr(Vertx vertx,DeploymentOptions options){
//		
//	}

	private final Map<String, ClassLoader> classloaders = new WeakHashMap<>();
	public void addJarToClasspath(File jar ,Vertx vertx,DeploymentOptions options) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
        // Get the ClassLoader class 
		
		ContextImpl callingContext = (ContextImpl) vertx.getOrCreateContext();
	    ClassLoader cl = getClassLoader(options, callingContext);
	    
//        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Class<?> clazz = cl.getClass();
        
        // Get the protected addURL method from the parent URLClassLoader class
        Method method = clazz.getSuperclass().getDeclaredMethod("addURL", new Class[] {URL.class});
        
        // Run projected addURL method to add JAR to classpath
        method.setAccessible(true);
        method.invoke(cl, new Object[] {jar.toURI().toURL()});
    }
	

	  private ClassLoader getClassLoader(DeploymentOptions options, ContextImpl parentContext) {
	    String isolationGroup = options.getIsolationGroup();
	    ClassLoader cl;
	    if (isolationGroup == null) {
	      cl = getCurrentClassLoader();
	    } else {
	      // IMPORTANT - Isolation groups are not supported on Java 9+, because the system classloader is not an URLClassLoader
	      // anymore. Thus we can't extract the paths from the classpath and isolate the loading.
	      synchronized (this) {
	        cl = classloaders.get(isolationGroup);
	        if (cl == null) {
	          ClassLoader current = getCurrentClassLoader();
	          if (!(current instanceof URLClassLoader)) {
	            throw new IllegalStateException("Current classloader must be URLClassLoader");
	          }
	          List<URL> urls = new ArrayList<>();
	          // Add any extra URLs to the beginning of the classpath
	          List<String> extraClasspath = options.getExtraClasspath();
	          if (extraClasspath != null) {
	            for (String pathElement: extraClasspath) {
	              File file = new File(pathElement);
	              try {
	                URL url = file.toURI().toURL();
	                urls.add(url);
	              } catch (MalformedURLException e) {
	                throw new IllegalStateException(e);
	              }
	            }
	          }
	          // And add the URLs of the Vert.x classloader
	          URLClassLoader urlc = (URLClassLoader)current;
	          urls.addAll(Arrays.asList(urlc.getURLs()));

	          // Create an isolating cl with the urls
	          cl = new IsolatingClassLoader(urls.toArray(new URL[urls.size()]), getCurrentClassLoader(),
	                                        options.getIsolatedClasses());
	          classloaders.put(isolationGroup, cl);
	        }
	      }
	    }
	    return cl;
	  }

	  private ClassLoader getCurrentClassLoader() {
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    if (cl == null) {
	      cl = getClass().getClassLoader();
	    }
	    return cl;
	  }
}
