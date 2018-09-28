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
		    def client = HttpClientBuilder.create().build()
			write(EntityUtils.toString(client.execute(new HttpGet(url)).getEntity()))
		}
	}

    def pay(bill){
        connection.executeUpdate "update domains set status = 'in progress' where id = ?", [bill.product_id]
        def order = connection.firstRow("select * from  domains  where id = ?", [bill.product_id])
        sendMail(user.name,user.email,"Enregistrement du domaine ${order.name} pour ${order.year} an en cours",parseTemplate("confirmation",[order:order,url : "https://app.thinktech.sn"]))
        sendSupportMail("Enregistrement du domaine ${order.name} pour ${order.year} an en cours",parseTemplate("support",[order:order,user : user,url : "https://thinktech-crm.herokuapp.com"]))
    }
	
}