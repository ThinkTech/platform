package app;

import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

@WebListener
public class AppListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		context.setAttribute("datasource", setupDataSource(context));
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		BasicDataSource ds = (BasicDataSource) context.getAttribute("datasource");
		try {
			ds.close();
		} catch (SQLException e) {
		}
	}
	
	private DataSource setupDataSource(ServletContext context) {
		  BasicDataSource ds = new BasicDataSource();
		  ds.setDriverClassName("com.mysql.jdbc.Driver");
		  ds.setUrl(System.getenv("db.url")!=null?System.getenv("db.url"):context.getInitParameter("db.url"));
		  ds.setUsername(System.getenv("db.user")!=null?System.getenv("db.user"):context.getInitParameter("db.user"));
		  ds.setPassword(System.getenv("db.password")!=null?System.getenv("db.password"):context.getInitParameter("db.password"));
		  ds.setInitialSize(3);
		  ds.setMaxTotal(10);
	      return ds;
	}
		 
}