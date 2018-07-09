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
   	        params = ["h&eacute;bergement domaine "+order.domain,"domainhosting",order.price,product_id,user.structure_id]
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
         message = "<p>Configuration email pour le domaine "+order.domain+" suivant le plan "+order.plan+"</p>"
       }
       ticket.message += "<p>Super Administrateur Email : "+order.email+"@"+order.domain+"</p>"
       params = [ticket.subject,ticket.service,ticket.message,user.id,user.structure_id,product_id,true]
       connection.executeInsert 'insert into tickets(subject,service,message,user_id,structure_id,product_id,autoClose) values (?,?,?,?,?,?,?)', params
       def bill = createBill(order)
       params = [bill.fee,"mailhosting",bill.amount,product_id,user.structure_id]
	   connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
       sendMail(user.name,user.email,"Configuration email pour le domaine ${order.domain}",getOrderTemplate(order))
	}
    
    def createBill(order){
	   def bill = new Expando()
	   bill.fee = "h&eacute;bergement email "+order.plan+" "+order.domain
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
		sendMail(user.name,user.email,"Configuration email pour le domaine ${order.name} en cours",getConfirmationTemplate(order))
        sendMail("ThinkTech Support","support@thinktech.sn","Configuration email pour le domaine ${order.name} en cours",getSupportTemplate(order))
	}
	
	def getOrderTemplate(order) {
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
		     p("Un ticket a &eacute;t&eacute; cr&eacute;&eacute; et vous devez maintenant effectuer le paiement pour la configuration de votre business email par notre &eacute;quipe technique.")
		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/billing",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Payer")
			    }
			}
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service mailhosting en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://app.thinktech.sn"])
		template.toString()
	}
	
	def getConfirmationTemplate(order) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Votre business email est en cours de configuration")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Plan : $order.plan")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.name")
		     }
		     p("Votre business email est en cours de configuration et le ticket est en cours de r&eacute;solution par notre &eacute;quipe technique.")
		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/support",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Voir")
			    }
			}
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service mailhosting en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://app.thinktech.sn"])
		template.toString()
	}
	
	def getSupportTemplate(order) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Business email en cours de configuration")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Plan : $order.plan")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.name")
		     }
		     p("le paiement de la facture a &eacute;t&eacute; bien effectu&eacute; et le business email est maintenant en cours de configuration. Cliquer sur le bouton Configurer en bas pour d&eacute;marrer la r&eacute;solution du ticket correspondant.")
		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/support",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Configurer")
			    }
			}
		  }
		  
		  
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://thinktech-crm.herokuapp.com"])
		template.toString()
	}
	
}