import groovy.text.markup.MarkupTemplateEngine
import groovy.sql.Sql


class ModuleAction extends ActionSupport {
    
	def subscribe() {
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
          def subscription = parse(request)
	      def connection = getConnection()
	      def user = connection.firstRow("select * from users where email = ?", [subscription.email])
	      if(user) {
		    json([status : 0])
	      }else{
	        def params = [subscription.structure]
            def result = connection.executeInsert 'insert into structures(name) values (?)', params
            def structure_id = result[0][0]
            params = ["web dev",structure_id]
            connection.executeInsert 'insert into subscriptions(service,structure_id) values (?,?)', params
	        params = [subscription.name,subscription.email,subscription.password,"administrateur",true,structure_id]
            result = connection.executeInsert 'insert into users(name,email,password,role,owner,structure_id) values (?,?,sha(?),?,?,?)', params
            def user_id = result[0][0]
            def alphabet = (('A'..'N')+('P'..'Z')+('a'..'k')+('m'..'z')+('2'..'9')).join()  
 			def n = 30 
 		    subscription.activationCode = new Random().with { (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join() }
 		    params = [subscription.activationCode,user_id]
       		connection.executeInsert 'insert into accounts(activation_code,user_id) values (?, ?)', params
            def template = getSubscriptionTemplate(subscription)
            params = ["Bienvenue",template,user_id,structure_id]
       		connection.executeInsert 'insert into messages(subject,message,user_id,structure_id) values (?, ?, ?, ?)', params
	   		params = [subscription.project,subscription.project,"web dev",subscription.plan,user_id,structure_id]
       		result = connection.executeInsert 'insert into projects(subject,description,service,plan,user_id,structure_id) values (?,?,?,?,?,?)', params
       		def project_id = result[0][0]
       		def bill = createBill(subscription)
       		if(bill.amount){
		       params = [bill.fee,bill.amount,project_id]
		       connection.executeInsert 'insert into bills(fee,amount,project_id) values (?,?,?)', params
	       	   def query = 'insert into projects_tasks(task_id,info,project_id) values (?, ?, ?)'
	      	   connection.withBatch(query){ ps ->
	             10.times{
	               ps.addBatch(it+1,"aucune information",project_id)
	            } 
	           }
	          }else{
	           def query = 'insert into projects_tasks(task_id,info,project_id) values (?, ? , ?)'
	      	   connection.withBatch(query){ ps ->
	           10.times{
	              if(it!=0) ps.addBatch(it+1,"aucune information",project_id)
	           }
	          }
	        }
	        def mailConfig = new MailConfig(context.getInitParameter("smtp.email"),context.getInitParameter("smtp.password"),"smtp.thinktech.sn")
		    def mailSender = new MailSender(mailConfig)
		    def mail = new Mail(subscription.name,subscription.email,"${subscription.name}, veuillez confirmer votre souscription au ${subscription.plan}",template)
		    mailSender.sendMail(mail)
		    json([status : 1])
	      }
	     connection.close()
       }
    }
    
    def createBill(subscription){
	   def bill = new Expando()
	   bill.fee = "caution"
	   if(subscription.plan == "plan business") {
	      bill.amount = 25000 * 3
	   }else if(subscription.plan == "plan corporate") {
	      bill.amount = 20000 * 3
	   }else if(subscription.plan == "plan personal") {
	      bill.amount = 15000 * 3
	   }
	   bill
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
		      p(style : "font-size:150%;color:#fff"){
		         span("cliquer sur le bouton en bas pour confirmation")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		      if(subscription.structure) {
		        h5(style : "font-size: 120%;color: rgb(0, 0, 0);margin-bottom: 15px") {
		         span("Structure : $subscription.structure")
		        }
		      }
		      p("Merci pour votre souscription au ${subscription.plan}")
		      p("Veuillez confirmer votre souscription pour activer votre compte.")
		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
		       a(href : "$url/users/registration/confirm?activationCode=$subscription.activationCode",style : "font-size:150%;width:180px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
		         span("Confirmer")
		       }
		    }
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 11px;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu'un utilisant cet email)")
		      p("a cr&edot;&edot; un projet en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([subscription:subscription,url : "http://app.thinktech.sn"])
		template.toString()
	}

	
	def getConnection() {
		new Sql(dataSource)
	}
	
}

new ModuleAction()