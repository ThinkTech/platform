import groovy.sql.Sql

class ModuleAction extends ActionSupport {

    def payBill(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
	      def bill = parse(request) 
	      def module = getModule(bill.service)
          if(module){
            def service = getAction(module)
         	service.payBill(module,bill)
          }
		  json([status: 1])
	   }
   }
   
   def getConnection()  {
		new Sql(dataSource)
   }
}

new ModuleAction()