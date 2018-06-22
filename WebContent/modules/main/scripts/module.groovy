import groovy.text.markup.MarkupTemplateEngine
import groovy.sql.Sql

class Dispatcher extends ActionSupport {

    def subscribe(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
          def status = 2
          def params, result
          def subscription = parse(request) 
	      def service = getService(subscription.service)
	      if(service){
	            def count = 0
                def connection = getConnection()
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
                      structure_id = result[0][0]
                 	}	        
		            params = [user.name,user.email,user.password,"administrateur",user.telephone,true,user.structure_id]
		            result = connection.executeInsert 'insert into users(name,email,password,role,telephone,owner,structure_id) values (?,?,sha(?),?,?,?,?)', params
		            user.id = result[0][0]
		            def alphabet = (('A'..'N')+('P'..'Z')+('a'..'k')+('m'..'z')+('2'..'9')).join()  
		 	        def n = 30 
		 		    subscription.activationCode = new Random().with { (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join() }
		 		    params = [subscription.activationCode,user.id]
		            connection.executeInsert 'insert into accounts(activation_code,user_id) values (?, ?)', params
		            status = 1
			    }
			    if(!count){
			      service.metaClass.connection = connection
		          service.metaClass.user = user   
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
		          sendMail(user.name,user.email,"${user.name}, merci pour votre souscription au service ${subscription.service}",getSubscriptionTemplate(subscription))
		          sendMail("ThinkTech Sales","sales@thinktech.sn","Souscription au service ${subscription.service} reussie",getSupportTemplate(user,subscription))
			    }
			    connection.close()
	      }
	      json([status : status])
	   }
    }
	
	def order() {
	   response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
		   def order = parse(request) 
		   def service = getService(order.service)
	       if(service){
	         def connection = getConnection()
	         def user = connection.firstRow("select * from users where id = ?", [order.user_id])
		     service.metaClass.connection = connection
	         service.metaClass.user = user
		     service.order(order)
		     connection.close()
		     json([entity: order])
		   }	   
	   }
	}
   
   def getSubscriptionTemplate(subscription) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 180%;color: #fff;margin: 3px") {
		        span("Souscription reussie")
		      }
		      if(subscription.activationCode){
			      p(style : "font-size:150%;color:#fff"){
			         span("cliquer sur le bouton en bas pour confirmation")
			      }
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		      if(subscription.structure) {
		        h5(style : "font-size: 120%;color: rgb(0, 0, 0);margin-bottom: 15px") {
		         span("Structure : $subscription.structure")
		        }
		      }
		      h5(style : "font-size: 120%;color: rgb(0, 0, 0);margin-bottom: 15px") {
		         span("Service : $subscription.service")
		      }
		      if(subscription.activationCode){
		      	p("Veuillez confirmer votre souscription au service $subscription.service pour activer votre compte client.")
		      }else{
		          p("Merci pour votre souscription au service $subscription.service et au plan ${subscription.plan}.")
		      }

		    }
		    if(subscription.activationCode){
			    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			       a(href : "$url/users/registration/confirm?activationCode=$subscription.activationCode",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			         span("Confirmer")
			       }
			    }
		    }
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service $subscription.service en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([subscription:subscription,url : "https://thinktech-app.herokuapp.com"])
		template.toString()
	}
   
   
   def getSupportTemplate(user,subscription) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 180%;color: #fff;margin: 3px") {
		        span("Nouvelle Souscription reussie")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		      h5(style : "font-size: 120%;color: rgb(0, 0, 0);margin-bottom: 15px") {
		         span("Client : $user.name")
		      }
		      if(subscription.structure) {
		        h5(style : "font-size: 120%;color: rgb(0, 0, 0);margin-bottom: 15px") {
		         span("Structure : $subscription.structure")
		        }
		      }
		      h5(style : "font-size: 120%;color: rgb(0, 0, 0);margin-bottom: 15px") {
		         span("Service : $subscription.service")
		      }
		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
		       a(href : "$url/customers",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
		         span("Voir")
		       }
			}
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([user:user,subscription:subscription,url:"https://thinktech-crm.herokuapp.com"])
		template.toString()
	}
   
   def getConnection()  {
		new Sql(dataSource)
   }
}