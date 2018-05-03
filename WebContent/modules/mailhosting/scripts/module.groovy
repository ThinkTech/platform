import groovy.sql.Sql

class ModuleAction extends ActionSupport {
    
	def subscribe(subscription) {
	   def connection = getConnection()
	   def ticket = new Expando()
	   ticket.subject = "configuration business email"
	   ticket.service = subscription.service
	   ticket.message = "<p>Configuration business email - plan "+subscription.plan+"</p>"
	   def user = subscription.user
	   def params = [ticket.subject,ticket.service,ticket.message,user.id,user.structure_id]
       connection.executeInsert 'insert into tickets(subject,service,message,user_id,structure_id) values (?, ?, ?,?,?)', params
	   connection.close()
    }
   
	def getConnection() {
		new Sql(dataSource)
	}
	
}

new ModuleAction()