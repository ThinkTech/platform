import groovy.text.markup.MarkupTemplateEngine
import groovy.sql.Sql

class Dispatcher extends ActionSupport {

    def subscribe(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
          def status = 2
          def subscription = parse(request) 
	      def module = getModule(subscription.service)
	      if(module){
	            def count = 0
                def connection = getConnection()
			    def user = connection.firstRow("select * from users where email = ?", [subscription.email])
			    if(user) {
			        count = connection.firstRow("select count(*) as num from subscriptions where service = ? and structure_id = ?", [subscription.service,user.structure_id]).num
			        if(count) status = 0
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
			      def service = getAction(module)
			      service.metaClass.connection = connection
		          service.metaClass.module = module
		          service.metaClass.user = user   
		          def params = [subscription.service,subscription.plan,subscription.per,user.structure_id]
			      def result = connection.executeInsert 'insert into subscriptions(service,plan,per,structure_id) values (?,?,?,?)', params
		          subscription.id = result[0][0]
		          service.subscribe(subscription)
		          def mailConfig = new MailConfig(getInitParameter("smtp.email"),getInitParameter("smtp.password"),getInitParameter("smtp.host"),getInitParameter("smtp.port"))
				  def mailSender = new MailSender(mailConfig)
				  def mail = new Mail(subscription.name,subscription.email,"${subscription.name}, merci pour votre souscription au service ${subscription.service}",getSubscriptionTemplate(subscription))
				  mailSender.sendMail(mail)
			    }
			    connection.close()
	      }
	      json([status : status])
	   }
   }
   
   def createProject() {
	   response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
		   def project = parse(request) 
		   def module = getModule(project.service)
	       if(module){
	         def connection = getConnection()
		     def params = [project.subject,project.priority,project.service,project.plan,project.description,project.user,project.structure]
	         def result = connection.executeInsert 'insert into projects(subject,priority,service,plan,description,user_id,structure_id) values (?, ?, ?,?,?,?,?)', params
	         project.id = result[0][0]
	         def service = getAction(module)
	         service.metaClass.connection = connection
		     service.metaClass.module = module
		     service.createProject(project)
		     connection.close()
		     json([id: project.id])
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
		      if(subscription.plan){
		      	p("Merci pour votre souscription au plan ${subscription.plan}")
		      }
		      if(subscription.activationCode){
		      	p("Veuillez confirmer votre souscription au service pour activer votre compte.")
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
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service $subscription.service en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([subscription:subscription,url : "https://thinktech-app.herokuapp.com"])
		template.toString()
	}
   
   
   def getConnection()  {
		new Sql(dataSource)
   }
}

new Dispatcher()