class Service extends ActionSupport {
    
	def subscribe(subscription) {
	   def ticket = new Expando()
	   ticket.service = subscription.service
	   ticket.subject = "configuration business email"
	   ticket.message = "<p>Configuration business email - plan "+subscription.plan+"</p>"
	   def user = subscription.user
	   def params = [ticket.subject,ticket.service,ticket.message,user.id,user.structure_id]
       connection.executeInsert 'insert into tickets(subject,service,message,user_id,structure_id) values (?, ?, ?,?,?)', params
    }
	
}

new Service()