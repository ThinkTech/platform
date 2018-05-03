import groovy.sql.Sql

class ModuleAction extends ActionSupport {

    def payBill(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
	      def bill = parse(request) 
	      def status = 0
	      def module = getModule(bill.service)
          if(module){
            def service = getAction(module)
            bill.module = module
         	service.payBill(bill)
         	status = 1
          }
		  json([status: status])
	   }
   }
   
   def getConnection()  {
		new Sql(dataSource)
   }
}

new ModuleAction()