import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

class Service extends ActionSupport {
    
	def subscribe(subscription) {
	    order(subscription.order)
    }
	
	def order(order){
	   def params = [order.domain,order.extension,order.price,order.year,order.action,order.eppCode,user.id,user.structure_id]
   	   def result = connection.executeInsert 'insert into domains(name,extension,price,year,action,eppCode,user_id,structure_id) values (?,?,?,?,?,?,?,?)', params
   	   order.id = result[0][0];
   	   params = ["enregistrement domaine "+order.domain,"domainhosting",order.price,result[0][0],user.structure_id]
	   result = connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
	   order.bill_id = result[0][0];
	   sendMail(user.name,user.email,"Enregistrement du domaine ${order.domain} pour ${order.year} an",getOrderTemplate(order))
	}

	
	def search(){
	    response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Cache-control", "private, max-age=7200")   
	    def domain = getParameter("domain")
	    if(domain){
		    def url = "https://api.duoservers.com/?auth_username=store203583&auth_password=apipassword&section=domains&command=check&name=${domain}&tlds[0]=com&tlds[1]=net&tlds[2]=org&tlds[3]=biz&tlds[4]=info&tlds[5]=tv&tlds[6]=press&tlds[7]=news&tlds[8]=tech&return_type=json"
		    def get = new HttpGet(url)
			def client = HttpClientBuilder.create().build()
			def response = client.execute(get)
			def body = EntityUtils.toString(response.getEntity())
			write(body)
		}
	}

    def pay(bill){
        connection.executeUpdate "update domains set status = 'in progress' where id = ?", [bill.product_id]
        def order = connection.firstRow("select * from  domains  where id = ?", [bill.product_id])
        sendMail(user.name,user.email,"Enregistrement du domaine ${order.name} pour ${order.year} an en cours",getConfirmationTemplate(order))
        sendMail("ThinkTech Support","support@thinktech.sn","Enregistrement du domaine ${order.name} pour ${order.year} an en cours",getSupportTemplate(order))
    }
    
    def getOrderTemplate(order) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Votre domaine web est en attente d\'enregistrement")
		      }
		       p(style : "font-size:100%;color:#fff"){
			        span("cliquer sur le bouton en bas pour effectuer le paiement")
			   }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.domain")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Dur&eacute;e : $order.year an")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Montant : $order.price CFA")
		     }
		     if(order.action == "transfer"){
		        h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Action : transfert")
		     	}                                
		     }
		     p("Vous devez maintenant effectuer le paiement pour l\'enregistrement de votre domaine web.")

		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/billing",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Payer")
			    }
			}
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service domainhosting en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://app.thinktech.sn"])
		template.toString()
	 }
	 
	 def getSalesTemplate(order) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Enregistrement du domaine web en attente")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.domain")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Dur&eacute;e : $order.year an")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Montant : $order.price CFA")
		     }
		     if(order.action == "transfer"){
		        h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Action : transfert")
		     	}                                
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Auteur : $user.name")
		     }
		     p("Le client doit maintenant effectuer le paiement de sa facture pour l\'enregistrement de son domaine web.")

		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/domains",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Voir")
			    }
			}
		  }
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://thinktech-crm.herokuapp.com"])
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
		        span("Enregistrement du domaine web en cours")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.name")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Dur&eacute;e : $order.year an")
		     }
		     if(order.action == "transfer"){
		        h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Action : transfert")
		     	}                                
		     }
		     p("le paiement a &eacute;t&eacute; bien effectu&eacute; et le domaine est maintenant en cours d\'enregistrement.")

		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/domains",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Voir")
			    }
			}
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service domainhosting en utilisant cette adresse")
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
		        span("Enregistrement du domaine web en cours")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.name")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Dur&eacute;e : $order.year an")
		     }
		     if(order.action == "transfer"){
		        h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Action : transfert")
		     	}                                
		     }
		     p("le paiement a &eacute;t&eacute; bien effectu&eacute; et le domaine est maintenant en cours d\'enregistrement. Cliquer sur le bouton Enregistrer en bas pour d&eacute;marrer l\'enregistrement du domaine.")

		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/domains",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Enregistrer")
			    }
			}
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://thinktech-crm.herokuapp.com"])
		template.toString()
	}
}