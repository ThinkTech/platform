import groovy.sql.Sql

class ModuleAction extends ActionSupport {
    
	def subscribe(subscription) {
    }
   
	def getConnection() {
		new Sql(dataSource)
	}
	
}

new ModuleAction()