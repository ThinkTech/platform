class Service extends ActionSupport {
    
	def subscribe(subscription) {
	   def ticket = new Expando()
	   ticket.service = subscription.service
	   ticket.subject = "configuration business email"
	   ticket.message = "<p>Configuration business email - plan "+subscription.plan+"</p>"
	   def user = subscription.user
	   def params = [ticket.subject,ticket.service,ticket.message,user.id,user.structure_id]
       connection.executeInsert 'insert into tickets(subject,service,message,user_id,structure_id) values (?, ?, ?,?,?)', params
       def bill = createBill(subscription)
       if(bill.amount){
		   params = [bill.fee,bill.amount,subscription.id]
		   connection.executeInsert 'insert into bills(fee,amount,subscription_id) values (?,?,?)', params
	   }
    }
    
    def createBill(subscription){
	   def bill = new Expando()
	   bill.fee = "business email"
	   if(subscription.plan == "standard") {
	      bill.amount = 14000
	   }else if(subscription.plan == "pro") {
	      bill.amount = 34000
	   }else if(subscription.plan == "enterprise") {
	      bill.amount = 54000
	   }
	   bill
	}
	
}

new Service()