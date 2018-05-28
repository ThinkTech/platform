class Service extends ActionSupport {
    
	def subscribe(subscription) {
	   def ticket = new Expando()
	   ticket.with {
         subject = "configuration business email"
         service = subscription.service
         message = "<p>Configuration business email - plan "+subscription.plan+"</p>"
       }
	   def params = [ticket.subject,ticket.service,ticket.message,user.id,user.structure_id]
       connection.executeInsert 'insert into tickets(subject,service,message,user_id,structure_id) values (?, ?, ?,?,?)', params
       def bill = createBill(subscription)
       params = [bill.fee,subscription.service,bill.amount,subscription.id]
	   connection.executeInsert 'insert into bills(fee,service,amount,product_id) values (?,?,?)', params
    }
    
    def createBill(subscription){
	   def bill = new Expando()
	   bill.fee = "business email"
	   if(subscription.plan == "free") {
	      bill.amount = 20000
	   }
	   else if(subscription.plan == "standard") {
	      bill.amount = 14000
	   }else if(subscription.plan == "pro") {
	      bill.amount = 34000
	   }else if(subscription.plan == "enterprise") {
	      bill.amount = 54000
	   }
	   bill
	}
	
	def payBill(bill){
		
	}
	
}