package test.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class A extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		ServletContext css=getServletContext();
		css.setAttribute("user", "db1");
		css.setAttribute("pass", "system");
		css.removeAttribute("pass");
		css.setAttribute("user", "db2");
		
		HttpSession session=req.getSession();
		session.setAttribute("A","AB");
		session.setAttribute("B","AB");
		session.invalidate();// it will call sessionDestroyed And then attributeRemoved from listener
		HttpSession session1=req.getSession();
		session1.setAttribute("C", "CAA");
		session1.setAttribute("B", "BAA");
		session1.setAttribute("B", "DDD");
		
		PrintWriter out=resp.getWriter();
		out.print("<html><body>Welcome in Add Context!!</body></html>");
	}
}
