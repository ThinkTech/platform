import groovy.sql.Sql

class ModuleAction extends ActionSupport {

    def payBill(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
	      def bill = parse(request) 
	      def modules = moduleManager.modules
	      modules.each{
	         if(it.name == bill.service){
	            def reload = System.getenv("metamorphosis.reload")
		        def service = "true".equals(reload) ? moduleManager.buildAction(it,null) : moduleManager.buildAndCacheAction(it,null)
	         	service.payBill(it,bill)
	         }
	      }
		  json([status: 1])
	   }
   }
   
   def getConnection()  {
		new Sql(dataSource)
   }
}

new ModuleAction()