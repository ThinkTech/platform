package app;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import groovy.sql.Sql;

@WebListener
public class RequestListener implements ServletRequestListener {
	
	@Override
    public void requestInitialized(ServletRequestEvent event)  {
       ServletRequest request = event.getServletRequest();
       ServletContext context = event.getServletContext();
       request.setAttribute("connection",new Sql((DataSource) context.getAttribute("datasource")));
    }
	
    @Override
    public void requestDestroyed(ServletRequestEvent event) {
       ServletRequest request = event.getServletRequest();
       Sql connection = (Sql) request.getAttribute("connection");
       connection.close();
    }

}
