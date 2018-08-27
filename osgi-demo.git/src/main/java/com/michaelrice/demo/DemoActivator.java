package com.michaelrice.demo;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import java.util.logging.Logger;

public class DemoActivator implements BundleActivator {

	  private final static Logger LOGGER = Logger.getLogger("DemoActivator");
    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("STARTING DEMO: hello, world");
        

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("STOPPING DEMO");
    }

}
