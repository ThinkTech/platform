class Service extends ActionSupport {
    
	def subscribe(subscription) {
       order(subscription.order)
    }
    
    def order(order){
       def params,result,product_id
	   if(order.plan == "free"){
           if(!order.domainCreated){
               order.price = order.price/order.year         
               order.year = 1
               params = [order.domain,order.extension,order.plan,order.price,order.year,order.action,order.eppCode,user.id,user.structure_id,true,order.email]
   	           result = connection.executeInsert 'insert into domains(name,extension,plan,price,year,action,eppCode,user_id,structure_id,emailOn,email) values (?,?,?,?,?,?,?,?,?,?,?)', params
   	           product_id = result[0][0]
           }else{
               product_id = order.product_id
               connection.executeUpdate "update domains set emailOn = true, email = ?, plan = ? where id = ?", [order.email,order.plan,product_id]
           }
       }else{
         if(!order.domainCreated){
            params = [order.domain,order.extension,order.plan,order.price,order.year,order.action,order.eppCode,user.id,user.structure_id,true,order.email]
   	        result = connection.executeInsert 'insert into domains(name,extension,plan,price,year,action,eppCode,user_id,structure_id,emailOn,email) values (?,?,?,?,?,?,?,?,?,?,?)', params
   	        product_id = result[0][0]
   	        params = ["enregistrement domaine "+order.domain,"domainhosting",order.price,product_id,user.structure_id]
		    connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
		    def service = getService("domainhosting")
		    sendMail(user.name,user.email,"Enregistrement du domaine ${order.domain} pour ${order.year} an",service.getOrderTemplate(order))
         }else{
             product_id = order.product_id
             connection.executeUpdate "update domains set emailOn = true, email = ?, plan = ? where id = ?", [order.email,order.plan,product_id]
         }
       }
       def ticket = new Expando()
	   ticket.with {
         subject = "configuration email "+order.plan+" "+order.domain
         service = "mailhosting"
         message = "<p>Configuration email "+order.plan+" pour le domaine "+order.domain+"</p>"
       }
       ticket.message += "<p>Super Administrateur Email : "+order.email+"@"+order.domain+"</p>"
       params = [ticket.subject,ticket.service,ticket.message,user.id,user.structure_id,product_id,true]
       connection.executeInsert 'insert into tickets(subject,service,message,user_id,structure_id,product_id,autoClose) values (?,?,?,?,?,?,?)', params
       def bill = createBill(order)
       params = [bill.fee,"mailhosting",bill.amount,product_id,user.structure_id]
	   connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
       sendMail(user.name,user.email,"Configuration email pour le domaine ${order.domain}",parseTemplate("order",[order:order,url : "https://app.thinktech.sn"]))
	}
    
    def createBill(order){
	   def bill = new Expando(fee : "configuration email "+order.plan+" "+order.domain)
	   if(order.plan == "free") {
	      bill.amount = 20000
	   }
	   else if(order.plan == "standard") {
	      bill.amount = 14000
	   }else if(order.plan == "pro") {
	      bill.amount = 34000
	   }else if(order.plan == "enterprise") {
	      bill.amount = 54000
	   }
	   bill
	}
	
	def pay(bill){
	    connection.executeUpdate "update tickets set status = 'in progress', startedOn = Now(), progression = 10 where product_id = ? and service = 'mailhosting'", [bill.product_id]
	    def order = connection.firstRow("select * from  domains  where id = ?", [bill.product_id])
        if(order.plan=="free") connection.executeUpdate "update domains set status = if(status = 'stand by', 'in progress', status) where id = ?", [bill.product_id]
		sendMail(user.name,user.email,"Configuration email pour le domaine ${order.name} en cours",parseTemplate("configuration",[order:order,url : "https://app.thinktech.sn"]))
        sendSupportMail("Configuration email pour le domaine ${order.name} en cours",parseTemplate("support",[order:order,user : user,url : "https://thinktech-crm.herokuapp.com"]))
	}
	
}