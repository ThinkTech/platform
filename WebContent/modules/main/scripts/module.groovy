import groovy.text.markup.MarkupTemplateEngine
import groovy.sql.Sql

class ModuleAction extends ActionSupport {

    def subscribe(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
          def status = 2
          def subscription = parse(request) 
	      def module = moduleManager.getModuleByName(subscription.service)
	      if(module){
	            def count = 0
                def connection = getConnection()
			    def user = connection.firstRow("select * from users where email = ?", [subscription.email])
			    if(user) {
			        count = connection.firstRow("select count(*) as num from subscriptions where service = ? and structure_id = ?", [subscription.service,user.structure_id]).num
			        if(count){
			           status = 0
				       connection.close()
			        }
			    }else{
			        user = new Expando()
			        def params = [subscription.structure]
			        def result = connection.executeInsert 'insert into structures(name) values (?)', params
		            user.structure_id = result[0][0]
		            params = [subscription.name,subscription.email,subscription.password,"administrateur",true,user.structure_id]
		            result = connection.executeInsert 'insert into users(name,email,password,role,owner,structure_id) values (?,?,sha(?),?,?,?)', params
		            user.id = result[0][0]
		            def alphabet = (('A'..'N')+('P'..'Z')+('a'..'k')+('m'..'z')+('2'..'9')).join()  
		 	        def n = 30 
		 		    subscription.activationCode = new Random().with { (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join() }
		 		    params = [subscription.activationCode,user.id]
		            connection.executeInsert 'insert into accounts(activation_code,user_id) values (?, ?)', params
		            status = 1
			    }
			    if(!count){
				    def params = [subscription.service,user.structure_id]
			        connection.executeInsert 'insert into subscriptions(service,structure_id) values (?,?)', params
				    connection.close()
				    def service = getAction(module)
		         	subscription.user = user
		         	service.subscribe(module,subscription)
		         	def mailConfig = new MailConfig(context.getInitParameter("smtp.email"),context.getInitParameter("smtp.password"),"smtp.thinktech.sn")
				    def mailSender = new MailSender(mailConfig)
				    def mail = new Mail(subscription.name,subscription.email,"${subscription.name}, veuillez confirmer votre souscription au service ${subscription.service}",getSubscriptionTemplate(subscription))
				    mailSender.sendMail(mail)
			    }
	      }
	      json([status : status])
	   }
   }
   
   def createProject() {
	   response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
		   def project = parse(request) 
		   def module = moduleManager.getModuleByName(project.service)
	       if(module){
	         def service = getAction(module)
		     def id = service.createProject(module,project)
		     json([id: id])
		   }	   
	   }
	}
   
   def getSubscriptionTemplate(subscription) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-top:2%;height:100px;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 200%;color: #fff;margin: 3px") {
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
		      p("Merci pour votre souscription au ${subscription.plan}")
		      if(subscription.activationCode){
		      	p("Veuillez confirmer votre souscription pour activer votre compte.")
		      }
		    }
		    if(subscription.activationCode){
			    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			       a(href : "$url/users/registration/confirm?activationCode=$subscription.activationCode",style : "font-size:150%;width:180px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			         span("Confirmer")
			       }
			    }
		    }
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 11px;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu'un utilisant cet email)")
		      p("a souscrit au service $subscription.service en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([subscription:subscription,url : "http://app.thinktech.sn"])
		template.toString()
	}
   
   
   def getConnection()  {
		new Sql(dataSource)
   }
}

new ModuleAction()