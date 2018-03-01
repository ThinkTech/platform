import groovy.sql.Sql
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import app.FileManager

class ModuleAction extends ActionSupport {
    
	def subscribe(module,subscription) {
	     def connection = getConnection()
	     def user = subscription.user
       	 def params = [subscription.project,subscription.project,subscription.service,subscription.plan,user.id,user.structure_id]
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
	     connection.close()
    }
    
    def createProject() {
	   response.addHeader("Access-Control-Allow-Origin", "*");
       response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
       if(request.method == "POST") { 
		   def project = parse(request) 
		   def connection = getConnection()
		   def params = [project.subject,project.priority,"web dev",project.plan,project.description,project.user,project.structure]
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
	
	def payBill(module,bill){
      def connection = getConnection()
	  connection.executeUpdate "update bills set code = ?, status = 'finished', paidWith = ?, paidOn = NOW(), paidBy = ? where id = ?", [bill.code,bill.paidWith,bill.user.id,bill.id]
	  if(bill.fee == "caution"){
	  	connection.executeUpdate "update projects set status = 'in progress', progression = 10 where id = ?", [bill.project_id]
	  	def project = connection.firstRow("select * from projects  where id = ?", [bill.project_id])
	  	def info = "le paiement de la caution a &edot;t&edot; &edot;ffectu&edot; et le contrat vous liant &aacute; ThinkTech a &edot;t&edot; g&edot;n&edot;r&edot; et ajout&edot; aux documents du projet"
	  	connection.executeUpdate "update projects_tasks set date = ?, status = 'finished', info = ? , progression = 100 where name = ? and project_id = ?", [project.date,info,"Contrat et Caution",bill.project_id]
	  	connection.executeUpdate "update projects_tasks set date = ?, status = 'in progress' where name = ? and project_id = ?", [project.date,"Traitement",bill.project_id]
	  	def params = ["contrat.doc",50000,bill.project_id,bill.user.id]
	    connection.executeInsert 'insert into documents(name,size,project_id,createdBy) values (?,?,?,?)',params
	  	generateContract(module,bill.user,project)
	  }
	  connection.close()
    }
   
    def generateContract(module,user,project) {
      def folder =  module.folder.absolutePath + "/contracts/"
      Thread.start{
          def file = project.plan.replace(' ','-')+".doc"
	      def document = new HWPFDocument(new POIFSFileSystem(new File(folder+file)))
	      document.range.replaceText("structure_name",user.structure.name)
	      document.range.replaceText("user_name",user.name)
	      document.range.replaceText("date_contract",new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()))
	      def out = new ByteArrayOutputStream()
	      document.write(out)
	      def dir = "structure_"+user.structure.id+"/"+"project_"+project.id
	      def manager = new FileManager()
	      manager.upload(dir+"/contrat.doc",new ByteArrayInputStream(out.toByteArray()))
      }
    }
	
	def getConnection() {
		new Sql(dataSource)
	}
	
}

new ModuleAction()