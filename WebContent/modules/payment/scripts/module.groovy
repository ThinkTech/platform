import groovy.sql.Sql

class ModuleAction extends ActionSupport {

    def payBill(){
      def bill = parse(request) 
      println bill
      def modules = moduleManager.modules
      modules.each{
         println it.name
      }
	  json([status: 1])
   }
   
   def getConnection()  {
		new Sql(dataSource)
   }
}

new ModuleAction()