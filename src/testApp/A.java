package testApp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class A extends GenericServlet {

	public void service(ServletRequest request,ServletResponse response) throws ServletException,IOException{
		String s=request.getParameter("user");
		PrintWriter out=response.getWriter();
		out.print("<html><body>");
		out.print("<form action='/testApp/success'>"); 
		out.print("HIHIHI! "+s);
		out.print("<input type='submit' value='ok'>");
		out.print("</form></body></html>");
	}
}
