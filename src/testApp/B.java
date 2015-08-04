package testApp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.GenericServlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class B extends GenericServlet{
	
	public void service(ServletRequest request,ServletResponse response) throws IOException{
		PrintWriter out=response.getWriter();
		out.print("<html><body>Finally success to get the page</body></html>");
	}
}
