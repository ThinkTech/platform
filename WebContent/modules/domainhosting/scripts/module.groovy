import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import groovy.text.markup.MarkupTemplateEngine

class Service extends ActionSupport {
    
	def subscribe(subscription) {
	    def hosting = subscription.hosting
	    def params = [hosting.domain,hosting.extension,hosting.price,hosting.year,hosting.action,hosting.eppCode,user.structure_id]
   	    def result = connection.executeInsert 'insert into domains(name,extension,price,year,action,eppCode,structure_id) values (?,?,?,?,?,?,?)', params
   	    params = ["h&eacute;bergement domaine : "+hosting.domain,subscription.service,hosting.price,result[0][0],user.structure_id]
	    connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
	    def mailConfig = new MailConfig(getInitParameter("smtp.email"),getInitParameter("smtp.password"),getInitParameter("smtp.host"),getInitParameter("smtp.port"))
		def mailSender = new MailSender(mailConfig)
	    def mail = new Mail(subscription.name,subscription.email,"Enregistrement du domaine ${hosting.domain} pour ${hosting.year} an",getBillTemplate(subscription))
		mailSender.sendMail(mail)
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

    def payBill(bill){
        connection.executeUpdate "update domains set status = 'in progress' where id = ?", [bill.product_id]
    }
    
     def getBillTemplate(subscription) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Enregistrement du domaine web en attente")
		      }
		       p(style : "font-size:100%;color:#fff"){
			        span("cliquer sur le bouton en bas pour effectuer le paiement")
			   }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $subscription.hosting.domain")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Dur&eacute;e : $subscription.hosting.year an")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Montant : $subscription.hosting.price CFA")
		     }
		     if(subscription.hosting.action == "transfer"){
		        h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Action : transfert")
		     	}                                
		     }
		     p("Vous devez maintenant effectuer le paiement de votre facture pour l\'enregistrement de votre domaine web.")

		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/billing",style : "font-size:150%;width:180px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Payer")
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
}