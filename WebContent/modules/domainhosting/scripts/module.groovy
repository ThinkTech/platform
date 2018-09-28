import org.apache.http.HttpResponse
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
	   sendMail(user.name,user.email,"Enregistrement du domaine ${order.domain} pour ${order.year} an",parseTemplate("order",[order:order,url : "https://app.thinktech.sn"]))
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
        sendMail(user.name,user.email,"Enregistrement du domaine ${order.name} pour ${order.year} an en cours",parseTemplate("confirmation",[order:order,url : "https://app.thinktech.sn"]))
        sendSupportMail("Enregistrement du domaine ${order.name} pour ${order.year} an en cours",parseTemplate("support",[order:order,user : user,url : "https://thinktech-crm.herokuapp.com"]))
    }
    
    
	 def getSalesTemplate(order) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#3abfdd") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Enregistrement domaine web en attente")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 20px;margin-bottom:30px") {
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
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Email : $user.email")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-top:5px;margin-bottom: 0px") {
		         span("Structure : $user.structure")
		     }
		     p("le client doit maintenant effectuer le paiement de sa facture pour l\'enregistrement de son domaine web.")

		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/domains",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #3abfdd;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Voir")
			    }
			}
		  }
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,user : user,url : "https://thinktech-crm.herokuapp.com"])
		template.toString()
	 }
	
}