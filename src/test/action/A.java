package test.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class A extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletContext css=getServletContext();
		css.setAttribute("user", "db1");
		css.setAttribute("pass", "system");
		css.removeAttribute("pass");
		css.setAttribute("user", "db2");
		PrintWriter out=resp.getWriter();
		out.print("<html><body>Welcome in Add Context!!</body></html>");
	}
}
