import groovy.text.markup.MarkupTemplateEngine

class Service extends ActionSupport {
    
	def subscribe(subscription) {
	   def ticket = new Expando()
	   ticket.with {
         subject = "configuration business email"
         service = subscription.service
         message = "<p>Configuration business email - plan "+subscription.hosting.plan+"</p>"
       }
	   def params = [ticket.subject,ticket.service,ticket.message,user.id,user.structure_id]
       connection.executeInsert 'insert into tickets(subject,service,message,user_id,structure_id) values (?, ?, ?,?,?)', params
       def bill = createBill(subscription.hosting)
       params = [bill.fee,subscription.service,bill.amount,subscription.id,user.structure_id]
	   connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
	   saveDomain(subscription.hosting)
    }
    
    def saveDomain(order){
       if(order.plan == "free"){
           order.price = order.price/order.year         
           order.year = 1
           def params = [order.domain,order.extension,order.plan,order.price,order.year,order.action,order.eppCode,user.structure_id,true,order.email,"in progress"]
   	       def result = connection.executeInsert 'insert into domains(name,extension,plan,price,year,action,eppCode,structure_id,emailOn,email,status) values (?,?,?,?,?,?,?,?,?,?,?)', params   
       }else{
         def params = [order.domain,order.extension,order.plan,order.price,order.year,order.action,order.eppCode,user.structure_id,true,order.email]
   	     def result = connection.executeInsert 'insert into domains(name,extension,plan,price,year,action,eppCode,structure_id,emailOn,email) values (?,?,?,?,?,?,?,?,?,?)', params
   	     params = ["h&eacute;bergement domaine : "+order.domain,"mailhosting",order.price,result[0][0],user.structure_id]
		 connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
		 def mailConfig = new MailConfig(getInitParameter("smtp.email"),getInitParameter("smtp.password"),getInitParameter("smtp.host"),getInitParameter("smtp.port"))
		 def mailSender = new MailSender(mailConfig)
		 def mail = new Mail(user.name,user.email,"Enregistrement du domaine ${order.domain} pour ${order.year} an",getBillTemplate(order))
		 mailSender.sendMail(mail)
       }
	}
    
    def createBill(hosting){
	   def bill = new Expando()
	   bill.fee = "h&eacute;bergement email"
	   if(hosting.plan == "free") {
	      bill.amount = 20000
	   }
	   else if(hosting.plan == "standard") {
	      bill.amount = 14000
	   }else if(hosting.plan == "pro") {
	      bill.amount = 34000
	   }else if(hosting.plan == "enterprise") {
	      bill.amount = 54000
	   }
	   bill
	}
	
	def payBill(bill){
		
	}
	
	def getBillTemplate(order) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Votre business email est en attente de configuration")
		      }
		       p(style : "font-size:100%;color:#fff"){
			        span("cliquer sur le bouton en bas pour effectuer le paiement")
			   }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Plan : $order.plan")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.domain")
		     }
		     p("Vous devez maintenant effectuer le paiement pour la configuration de votre business email.")
		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/billing",style : "font-size:150%;width:180px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Payer")
			    }
			}
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service emailhosting en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://thinktech-app.herokuapp.com"])
		template.toString()
	}
}