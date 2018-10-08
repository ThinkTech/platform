class Dispatcher extends ActionSupport {

    def subscribe(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
          def status = 2
          def params, result
          def subscription = request.body 
	      def service = getService(subscription.service)
	      if(service){
	            def count = 0
			    def user = connection.firstRow("select * from users where email = ?", [subscription.email])
			    if(user) {
			        count = connection.firstRow("select count(*) as num from subscriptions where service = ? and structure_id = ?", [subscription.service,user.structure_id]).num
			        if(count) status = 0
			    }else{
			        user = new Expando()
			        params = [subscription.structure]
			        result = connection.executeInsert 'insert into structures(name) values (?)', params
			        user.with{
                      name = subscription.name
                      email = subscription.email
                      password = subscription.password
                      telephone = subscription.telephone
                      structure = subscription.structure
                      structure_id = result[0][0]
                 	}	        
		            params = [user.name,user.email,user.password,"administrateur","manager",user.telephone,true,user.structure_id]
		            result = connection.executeInsert 'insert into users(name,email,password,role,profession,telephone,owner,structure_id) values (?,?,sha(?),?,?,?,?,?)', params
		            user.id = result[0][0]
		            def alphabet = (('A'..'N')+('P'..'Z')+('a'..'k')+('m'..'z')+('2'..'9')).join()  
		 	        def n = 30 
		 		    subscription.activationCode = new Random().with { (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join() }
		 		    params = [subscription.activationCode,user.id]
		            connection.executeInsert 'insert into accounts(activation_code,user_id) values (?, ?)', params
		            status = 1
			    }
			    if(!count){
			      synchronized(this){
			          service.metaClass.getConnection = {-> connection}
			          service.metaClass.getUser = {-> user}   
			          params = [subscription.service,user.structure_id]
				      result = connection.executeInsert 'insert into subscriptions(service,structure_id) values (?,?)', params
			          subscription.id = result[0][0]
			          if(subscription.services){
			              subscription.services.each{
			                  params = [it,user.structure_id]
			                  connection.executeInsert 'insert into subscriptions(service,structure_id) values (?,?)', params
			              }
			          }
			          service.subscribe(subscription)	          
			          sendMail(user.name,user.email,"${user.name}, merci pour votre souscription au service ${subscription.service}",parseTemplate("subscription",[subscription:subscription,url:appURL]))
			          sendMail("ThinkTech Sales","sales@thinktech.sn","Nouvelle souscription effectu&eacute;e pour le service ${subscription.service}",service.getSalesTemplate(subscription.order))
			      }
			    }  
	      }
	      json([status:status])
	   }
    }
	
	def order() {
	   response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
		   def order = request.body 
		   def service = getService(order.service)
	       if(service){
	         def user = connection.firstRow("select u.*, s.name as structure from users u, structures s where u.structure_id = s.id and u.id = ?", [order.user_id])
	         synchronized(this){
			     service.metaClass.getConnection = {-> connection}
			     service.metaClass.getUser = {-> user}  
			     service.order(order)
			     sendSalesMail("Nouvelle vente effectu&eacute;e pour le service ${order.service}",parseTemplate(order.service,"sales",[order:order,user:user,url:crmURL]))
		     }
		     json([entity: order])
		   }	   
	   }
	}
}