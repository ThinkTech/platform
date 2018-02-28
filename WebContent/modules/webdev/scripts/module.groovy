import groovy.text.markup.MarkupTemplateEngine
import groovy.sql.Sql


class ModuleAction extends ActionSupport {
    
	def subscribe() {
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
         def status = 2
         def subscription = parse(request)
	     def connection = getConnection()
	     def user = connection.firstRow("select * from users where email = ?", [subscription.email])
	     if(user) {
	        def count = connection.firstRow("select count(*) as num from subscriptions where service = ? and structure_id = ?", ["web dev",user.structure_id]).num
	        if(count){
	           json([status : 0])
		       connection.close()
		       return
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
	     def params = ["web dev",user.structure_id]
         connection.executeInsert 'insert into subscriptions(service,structure_id) values (?,?)', params
	     def template = getSubscriptionTemplate(subscription)
         params = ["Souscription reussie",template,user.id,user.structure_id]
         connection.executeInsert 'insert into messages(subject,message,user_id,structure_id) values (?, ?, ?, ?)', params
	   	 params = [subscription.project,subscription.project,"web dev",subscription.plan,user.id,user.structure_id]
       	 def result = connection.executeInsert 'insert into projects(subject,description,service,plan,user_id,structure_id) values (?,?,?,?,?,?)', params
       	 def project_id = result[0][0]
       	 def tasks = getTasks()
       	 def bill = createBill(subscription)
       	 if(bill.amount){
		       params = [bill.fee,bill.amount,project_id]
		       connection.executeInsert 'insert into bills(fee,amount,project_id) values (?,?,?)', params
	       	   def query = 'insert into projects_tasks(name,description,info,project_id) values (?, ?, ?, ?)'
	      	   connection.withBatch(query){ ps ->
	             tasks.each{
	               ps.addBatch(it.name,it.description,"aucune information",project_id)
	            } 
	           }
	     }else{
	           def query = 'insert into projects_tasks(name,description,info,project_id) values (?, ?, ?, ?)'
	      	   connection.withBatch(query){ ps ->
	             tasks.eachWithIndex { it, i ->
	              if(i!=0) ps.addBatch(it.name,it.description,"aucune information",project_id)
	            }
	          }
	     }
	     def mailConfig = new MailConfig(context.getInitParameter("smtp.email"),context.getInitParameter("smtp.password"),"smtp.thinktech.sn")
		 def mailSender = new MailSender(mailConfig)
		 def mail = new Mail(subscription.name,subscription.email,"${subscription.name}, veuillez confirmer votre souscription au ${subscription.plan}",template)
		 mailSender.sendMail(mail)
		 json([status : status])
	     connection.close()
       }
    }
    
    def createProject() {
	   response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
		   def project = parse(request) 
		   def connection = getConnection()
		   def params = [project.subject,project.priority,project.service,project.plan,project.description,project.user,project.structure]
	       def result = connection.executeInsert 'insert into projects(subject,priority,service,plan,description,user_id,structure_id) values (?, ?, ?,?,?,?,?)', params
	       def id = result[0][0]
	       def tasks = getTasks()
	       def bill = createBill(project)
	       if(bill.amount){
		       params = [bill.fee,bill.amount,id]
		       connection.executeInsert 'insert into bills(fee,amount,project_id) values (?,?,?)', params
	       	   def query = 'insert into projects_tasks(name,description,info,project_id) values (?, ?, ?, ?)'
	      	   connection.withBatch(query){ ps ->
	             tasks.each{
	               ps.addBatch(it.name,it.description,"aucune information",id)
	            } 
	           }
		    }else{
	           def query = 'insert into projects_tasks(name,description,info,project_id) values (?, ?, ?, ?)'
	      	   connection.withBatch(query){ ps ->
	             tasks.eachWithIndex { it, i ->
	              if(i!=0) ps.addBatch(it.name,it.description,"aucune information",id)
	            }
	          }
		   }
		   connection.close()
		   json([id: id])
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
	
	def getTasks(){
	   def tasks = []
	   def task = new Expando(name :"Contrat et Caution",description :"cette phase intiale &edot;tablit la relation l&edot;gale qui vous lie &agrave; ThinkTech")
	   tasks << task
	   task = new Expando(name :"Traitement",description : "cette phase d'approbation est celle o&ugrave; notre &edot;quipe technique prend en charge votre projet")
       tasks << task
       task = new Expando(name :"Analyse du projet",description : "cette phase est celle de l'analyse de votre projet pour une meilleure compr&edot;hension des objectifs")
	   tasks << task
	   task = new Expando(name :"D&edot;finition des fonctionnalit&edot;s",description : "cette phase est celle de la d&edot;finition des fonctionnalit&edot;s du produit")
	   tasks << task
	   task = new Expando(name :"Conception de l'interface",description : "cette phase est celle de la conception de l'interface utilisateur")
       tasks << task
       task = new Expando(name :"D&edot;veloppement des fonctionnalit&edot;s",description : "cette phase est celle du d&edot;veloppement des fonctionnalit&edot;s du produit")
       tasks << task
       task = new Expando(name :"Tests",description : "cette phase permet de tester les fonctionnalit&edot;s du produit")
       tasks << task
       task = new Expando(name :"Validation",description : "cette phase est celle de la validation des fonctionnalit&edot;s du produit")
       tasks << task
       task = new Expando(name :"Livraison du produit",description : "cette phase est celle du deploiement du produit final")
       tasks << task
       task = new Expando(name :"Formation",description : "cette phase finale est celle de la formation pour une prise en main du produit")
	   tasks << task
	   tasks
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
		         span("Service : web dev")
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
		      p("a souscrit au service web dev en utilisant cette adresse")
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