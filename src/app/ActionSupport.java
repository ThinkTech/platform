package app;

import java.util.Map;
import groovy.sql.Sql;
import groovy.text.markup.MarkupTemplateEngine;

@SuppressWarnings("serial")
public class ActionSupport extends org.metamorphosis.core.ActionSupport {
	
	public Object getConnection()  {
		 return new Sql(getDataSource());	
    }
	
	public void sendSupportMail(String object,String content){
		sendMail("ThinkTech Support","support@thinktech.sn",object,content);
	}
	
	public void sendSalesMail(String object,String content){
		sendMail("ThinkTech Sales","sales@thinktech.sn",object,content);
	}
	
	public void sendDevMail(String object,String content){
		sendMail("ThinkTech Dev","dev@thinktech.sn",object,content);
	}
	
	@SuppressWarnings("rawtypes")
	public String parseTemplate(String template, Map map) throws Exception {
		return new MarkupTemplateEngine().createTemplate(readFile("templates/"+template+".groovy")).make(map).toString();
	}
	
	@SuppressWarnings("rawtypes")
	public String parseTemplate(String service, String template, Map map) throws Exception {
		return new MarkupTemplateEngine().createTemplate(readFile(getModule(service),"templates/"+template+".groovy")).make(map).toString();
	}

	public String getAppURL() {
		return "https://app.thinktech.sn";
	}
	
	public String getCrmURL() {
		return "https://thinktech-crm.herokuapp.com";
	}
}
