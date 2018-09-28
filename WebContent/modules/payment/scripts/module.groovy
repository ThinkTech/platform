class Service extends ActionSupport {

    def pay(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
	      def bill = parse(request) 
	      def status = 0
	      def module = getModule(bill.service)
          if(module){
            def connection = getConnection()
            def service = getService(module)
            synchronized(this){
	            service.metaClass.getConnection = {-> connection}
			    service.metaClass.getModule = {-> module}  
			    def user = connection.firstRow("select u.*, s.name as structure from users u, structures s where u.structure_id = s.id and u.id = ?", [bill.user.id])
			    service.metaClass.getUser = {-> user}   
			    connection.executeUpdate "update bills set code = ?, status = 'finished', paidWith = ?, paidOn = NOW(), paidBy = ? where id = ?", [bill.code,bill.paidWith,user.id,bill.id]
	         	service.pay(bill)   
	         	sendMail(user.name,user.email,"Confirmation paiement "+bill.fee,parseTemplate("bill",[bill:bill,url : "https://app.thinktech.sn"]))
	         	sendSalesMail("Confirmation paiement "+bill.fee,parseTemplate("sales",[bill:bill,user:user,url : "https://thinktech-crm.herokuapp.com"]))
         	}
		    connection.close()
         	status = 1
          }
		  json([status: status])
	   }
    }
   
}