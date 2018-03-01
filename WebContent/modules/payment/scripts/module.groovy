import groovy.sql.Sql

class ModuleAction extends ActionSupport {

    def payBill(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
	      def bill = parse(request) 
	      println bill
	      def modules = moduleManager.modules
	      modules.each{
	         println it.name
	      }
		  json([status: 1])
	   }
   }
   
   def getConnection()  {
		new Sql(dataSource)
   }
}

new ModuleAction()