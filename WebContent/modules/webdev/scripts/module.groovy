import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.poifs.filesystem.POIFSFileSystem

class Service extends ActionSupport {
    
	def subscribe(subscription) {	    
   	  order(subscription.order)
    }
    
    def order(order) {
         order.priority = order.priority ? order.priority : "normal"
         order.subject =  order.subject + " " + order.domain 
         def params,result,tasks
         def email = user.email.substring(0,user.email.indexOf("@"))
         def bill = createBill(order)
         def count = connection.firstRow("select count(*) AS count from domains where plan = 'free' and structure_id = $user.structure_id").count
         if(!order.domainCreated){
             order.price = order.price/order.year         
             order.year = 1 
             if(count==0){
                params = [order.domain,order.extension,order.price,order.year,order.action,order.eppCode,true,email,"free",user.id,user.structure_id]
                result = connection.executeInsert 'insert into domains(name,extension,price,year,action,eppCode,emailOn,email,plan,user_id,structure_id) values (?,?,?,?,?,?,?,?,?,?,?)', params
             }else{
                params = [order.domain,order.extension,order.price,order.year,order.action,order.eppCode,user.id,user.structure_id]
                result = connection.executeInsert 'insert into domains(name,extension,price,year,action,eppCode,user_id,structure_id) values (?,?,?,?,?,?,?,?)', params
             }
             params = [order.subject,order.priority,"webdev",order.plan,order.description,user.id,user.structure_id,result[0][0]]
	         result = connection.executeInsert 'insert into projects(subject,priority,service,plan,description,user_id,structure_id,domain_id) values (?,?,?,?,?,?,?,?)', params
     	     order.id = result[0][0]
	     }else {
	          if(count==0) connection.executeUpdate "update domains set plan = if(emailOn = false, 'free', plan), email = if(emailOn = false,?,email), emailOn = if(emailOn = false,true, emailOn) where id = ?", [email,order.domain_id]
              params = [order.subject,order.priority,"webdev",order.plan,order.description,user.id,user.structure_id,order.domain_id]
	          result = connection.executeInsert 'insert into projects(subject,priority,service,plan,description,user_id,structure_id,domain_id) values (?,?,?,?,?,?,?,?)', params
     	      order.id = result[0][0]
	     }
	     if(bill.amount){
	          params = [bill.fee,"webdev",bill.amount,order.id,user.structure_id]
		      connection.executeInsert 'insert into bills(fee,service,amount,product_id,structure_id) values (?,?,?,?,?)', params
		      sendMail(user.name,user.email,"${order.subject}",parseTemplate("order",[order:order,url : "https://app.thinktech.sn"]))
			  tasks = getTasks(true)
		  }else{
		      sendMail(user.name,user.email,"${order.subject}",parseTemplate("custom",[order:order,url : "https://app.thinktech.sn"]))
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
	   def bill = new Expando(fee : "caution "+order.subject)
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
	   def task = new Expando(name : caution ? "Contrat et Caution" : "Contrat",description :"cette phase intiale &eacute;tablit la relation l&eacute;gale qui vous lie &agrave; notre structure ThinkTech")
	   tasks << task
	   task = new Expando(name :"Traitement",description : "cette phase d'approbation est celle o&ugrave; notre &eacute;quipe de d&eacute;veloppement prend en charge votre projet")
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
	  if(bill.fee.indexOf("caution")!=-1){
	    connection.executeUpdate "update projects set status = 'in progress', startedOn = NOW(), progression = 10 where id = ?", [bill.product_id]
	  	def info = "le paiement de la caution a &eacute;t&eacute; &eacute;ffectu&eacute; et le contrat vous liant &aacute; notre structure ThinkTech a &eacute;t&eacute; g&eacute;n&eacute;r&eacute; et ajout&eacute; aux documents du projet"
	  	connection.executeUpdate "update projects_tasks set startedOn = NOW(), status = 'finished', info = ? , progression = 100 where name = ? and project_id = ?", [info,"Contrat et Caution",bill.product_id]
	  	connection.executeUpdate "update projects_tasks set startedOn = NOW(), status = 'in progress' where name = ? and project_id = ?", ["Traitement",bill.product_id]
	  	def params = ["contrat.doc",50000,bill.product_id,user.id]
	    connection.executeInsert 'insert into documents(name,size,project_id,createdBy) values (?,?,?,?)',params
	    def order = connection.firstRow("select p.*, d.name as domain from projects p, domains d where p.domain_id = d.id and p.id = ?", [bill.product_id])
	    connection.executeUpdate "update domains set status = if(status = 'stand by', 'in progress', status) where id = ?", [order.domain_id]
	    generateContract(order) 
	    sendMail(user.name,user.email,"${order.subject} en cours",parseTemplate("confirmation",[order:order,url : "https://app.thinktech.sn"]))
	    sendMail("ThinkTech Dev","dev@thinktech.sn","${order.subject} en cours",parseTemplate("support",[order:order,user : user,url : "https://thinktech-crm.herokuapp.com"]))
	  }
    }
   
    def generateContract(project) {
      def folder =  module.folder.absolutePath + "/contracts/"
      def file = new File(folder+project.plan+".doc")
      if(file.exists()){
		  def document = new HWPFDocument(new POIFSFileSystem(file))
		  document.range.replaceText("structure_name",user.structure)
		  document.range.replaceText("date_contract",new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()))
		  def out = new ByteArrayOutputStream() 
		  def dir = "structure_"+user.structure_id+"/"+"project_"+project.id   
	      Thread.start{
	          document.write(out)
		      def manager = new FileManager()
		      manager.upload(dir+"/contrat.doc",new ByteArrayInputStream(out.toByteArray()))
	      }
      }
    }
}