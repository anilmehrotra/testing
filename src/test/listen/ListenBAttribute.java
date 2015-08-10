package test.listen;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;


public class ListenBAttribute implements ServletContextAttributeListener{

	@Override
	public void attributeAdded(ServletContextAttributeEvent arg0) {
		System.out.println("Context Add!!!");
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent arg0) {
		System.out.println("Context Removed!!!");
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent arg0) {
		System.out.println("Context Replaced!!!");
	}
}
