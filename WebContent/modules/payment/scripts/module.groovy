class Service extends ActionSupport {

    def pay(){
       response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
	      def bill = parse(request) 
	      def status = 0
	      def module = getModule(bill.service)
          if(module){
            def connection = getConnection()
            def service = getService(module)
            service.metaClass.connection = connection
		    service.metaClass.module = module
		    def user = connection.firstRow("select * from users where id = ?", [bill.user.id])
		    service.metaClass.user = user 
		    connection.executeUpdate "update bills set code = ?, status = 'finished', paidWith = ?, paidOn = NOW(), paidBy = ? where id = ?", [bill.code,bill.paidWith,user.id,bill.id]
         	service.pay(bill)   
         	sendMail(user.name,user.email,"Confirmation paiement "+bill.fee,getBillTemplate(bill))
		    connection.close()
         	status = 1
          }
		  json([status: status])
	   }
   }
   
   def getConnection()  {
		new Sql(dataSource)
   }
   
   def getBillTemplate(bill) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-top:2%;padding-bottom:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 200%;color: #fff;margin: 3px") {
		        span("Paiement reussie")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		    p("Le paiement de votre facture a &eacute;t&eacute; effectu&eacute; avec succ&eacute;s. ")  
		    h5(style : "font-size: 120%;color: rgb(0, 0, 0);margin-bottom: 15px") {
		         span("Service : $bill.service")
		    }
		    table {
		        tr{
		           td(style:"text-align:right;vertical-align:top;width : 100px;white-space : nowrap;padding-right : 2px"){
		               span("Montant :")
		           }
                   td{
                       span("$bill.amount CFA")
                   }
		        }
		        tr{
		           td(style:"text-align:right;vertical-align:top;width : 100px;white-space : nowrap;padding-right : 2px"){
		               span("Pay&eacute; par :")
		           }
                   td{
                       span("$bill.paidWith")
                   }
		        }
		        if(bill.code){
		          tr{
		           td(style:"text-align:right;vertical-align:top;width : 100px;white-space : nowrap;padding-right : 2px"){
		               span("Code :")
		           }
                   td{
                       span("$bill.code")
                   }
		          }   
		       }
		    }
		   }
		  }
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service $bill.service en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([bill:bill])
		template.toString()
	}
   
}