import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import app.FileManager
import groovy.text.markup.MarkupTemplateEngine

class Service extends ActionSupport {
    
	def subscribe(subscription) {	    
   	  order(subscription.order)
    }
    
    def order(order) {
         order.priority = order.priority ? order.priority : "normal";
         def tasks
	     def bill = createBill(order)
	     if(bill.amount){
	          def params = [order.domain,order.extension,order.plan,order.price,order.year,order.action,order.eppCode,user.id,user.structure_id]
   	          def result = connection.executeInsert 'insert into domains(name,extension,plan,price,year,action,eppCode,user_id,structure_id) values (?,?,?,?,?,?,?,?,?)', params
		      params = [order.subject,order.priority,"webdev",order.plan,order.description,user.id,user.structure_id,result[0][0]]
		      result = connection.executeInsert 'insert into projects(subject,priority,service,plan,description,user_id,structure_id,domain_id) values (?,?,?,?,?,?,?,?)', params
	     	  order.id = result[0][0]
		      params = [bill.fee,"webdev",bill.amount,order.id,user.structure_id]
		      connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
		      sendMail(user.name,user.email,"${order.subject} pour le domaine ${order.domain}",getBillTemplate(order))
			  tasks = getTasks(true)
		  }else{
		   	  order.price = order.price/order.year         
           	  order.year = 1
              def params = [order.domain,order.extension,order.plan,order.price,order.year,order.action,order.eppCode,user.id,user.structure_id]
   	          def result = connection.executeInsert 'insert into domains(name,extension,plan,price,year,action,eppCode,user_id,structure_id) values (?,?,?,?,?,?,?,?,?)', params
		      params = [order.subject,order.priority,"webdev",order.plan,order.description,user.id,user.structure_id,result[0][0]]
		      result = connection.executeInsert 'insert into projects(subject,priority,service,plan,description,user_id,structure_id,domain_id) values (?,?,?,?,?,?,?,?)', params
	     	  order.id = result[0][0]
              tasks = getTasks(false)
		  }
		  def query = 'insert into projects_tasks(name,description,project_id) values (?, ?, ?)'
	      connection.withBatch(query){ ps ->
	         tasks.each{
	            ps.addBatch(it.name,it.description,order.id)
	         } 
	      }
	}
    
    def createBill(order){
	   def bill = new Expando()
	   bill.fee = "caution"
	   if(order.plan == "business") {
	      bill.amount = 20000 * 3
	   }else if(order.plan == "corporate") {
	      bill.amount = 15000 * 3
	   }else if(order.plan == "personal") {
	      bill.amount = 10000 * 3
	   }
	   bill
	}
	
	def getTasks(caution){
	   def tasks = []
	   def task = new Expando(name : caution ? "Contrat et Caution" : "Contrat",description :"cette phase intiale &eacute;tablit la relation l&eacute;gale qui vous lie &agrave; ThinkTech")
	   tasks << task
	   task = new Expando(name :"Traitement",description : "cette phase d'approbation est celle o&ugrave; notre &eacute;quipe technique prend en charge votre projet")
       tasks << task
       task = new Expando(name :"Analyse du projet",description : "cette phase est celle de l'analyse de votre projet pour une meilleure compr&eacute;hension des objectifs")
	   tasks << task
	   task = new Expando(name :"D&eacute;finition des fonctionnalit&eacute;s",description : "cette phase est celle de la d&eacute;finition des fonctionnalit&eacute;s du produit")
	   tasks << task
	   task = new Expando(name :"Conception de l'interface",description : "cette phase est celle de la conception de l'interface utilisateur")
       tasks << task
       task = new Expando(name :"D&eacute;veloppement des fonctionnalit&eacute;s",description : "cette phase est celle du d&eacute;veloppement des fonctionnalit&eacute;s du produit")
       tasks << task
       task = new Expando(name :"Tests",description : "cette phase permet de tester les fonctionnalit&eacute;s du produit")
       tasks << task
       task = new Expando(name :"Validation",description : "cette phase est celle de la validation des fonctionnalit&eacute;s du produit")
       tasks << task
       task = new Expando(name :"Livraison du produit",description : "cette phase est celle du deploiement du produit final")
       tasks << task
       task = new Expando(name :"Formation",description : "cette phase finale est celle de la formation pour une prise en main du produit")
	   tasks << task
	   tasks
	}
	
	def pay(bill){
	  if(bill.fee == "caution"){
	  	connection.executeUpdate "update projects set status = 'in progress', startedOn = NOW(), progression = 10 where id = ?", [bill.project_id]
	  	def project = connection.firstRow("select * from projects  where id = ?", [bill.project_id])
	  	def info = "le paiement de la caution a &eacute;t&eacute; &eacute;ffectu&eacute; et le contrat vous liant &aacute; ThinkTech a &eacute;t&eacute; g&eacute;n&eacute;r&eacute; et ajout&eacute; aux documents du projet"
	  	connection.executeUpdate "update projects_tasks set startedOn = NOW(), status = 'finished', info = ? , progression = 100 where name = ? and project_id = ?", [info,"Contrat et Caution",bill.project_id]
	  	connection.executeUpdate "update projects_tasks set startedOn = NOW(), status = 'in progress' where name = ? and project_id = ?", ["Traitement",bill.project_id]
	  	def params = ["contrat.doc",50000,bill.project_id,bill.user.id]
	    connection.executeInsert 'insert into documents(name,size,project_id,createdBy) values (?,?,?,?)',params
	  	generateContract(bill.user,project)
	  }
    }
   
    def generateContract(user,project) {
      def folder =  module.folder.absolutePath + "/contracts/"
      Thread.start{
          def file = project.plan.replace(' ','-')+".doc"
	      def document = new HWPFDocument(new POIFSFileSystem(new File(folder+file)))
	      document.range.replaceText("structure_name",user.structure.name)
	      document.range.replaceText("date_contract",new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()))
	      def out = new ByteArrayOutputStream()
	      document.write(out)
	      def dir = "structure_"+user.structure.id+"/"+"project_"+project.id
	      def manager = new FileManager()
	      manager.upload(dir+"/contrat.doc",new ByteArrayInputStream(out.toByteArray()))
      }
    }
    
    def getBillTemplate(order) {
		MarkupTemplateEngine engine = new MarkupTemplateEngine()
		def text = '''\
		 div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#05d2ff") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Votre projet est en attente de traitement")
		      }
		       p(style : "font-size:100%;color:#fff"){
			        span("cliquer sur le bouton en bas pour effectuer le paiement de la caution")
			   }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 30px;margin-bottom:30px") {
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Plan : $order.plan")
		     }
		     h5(style : "font-size: 90%;color: rgb(0, 0, 0);margin-bottom: 0px") {
		         span("Domaine : $order.domain")
		     }
		     p("Vous devez maintenant effectuer le paiement de vos factures pour le traitement de votre projet par notre &eacute;quipe technique.")
		    }
		    div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/billing",style : "font-size:150%;width:180px;margin:auto;text-decoration:none;background: #05d2ff;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Payer")
			    }
			}
		  }
		  
		  div(style :"margin: 10px;margin-top:10px;font-size : 80%;text-align:center") {
		      p("Vous recevez cet email parce que vous (ou quelqu\'un utilisant cet email)")
		      p("a souscrit au service webdev en utilisant cette adresse")
		  }
		  
		 }
		'''
		def template = engine.createTemplate(text).make([order:order,url : "https://thinktech-app.herokuapp.com"])
		template.toString()
	}
		
}