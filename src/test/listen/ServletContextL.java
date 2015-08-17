package test.listen;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextL implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Listener Destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Listener initilized ");
	}

}
