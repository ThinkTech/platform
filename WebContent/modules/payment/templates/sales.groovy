div(style : "font-family:Tahoma;background:#fafafa;padding-bottom:16px;padding-top: 25px"){
		 div(style : "padding-bottom:12px;margin-left:auto;margin-right:auto;width:80%;background:#fff") {
		    img(src : "https://www.thinktech.sn/images/logo.png", style : "display:block;margin : 0 auto")
		    div(style : "margin-top:10px;padding-bottom:2%;padding-top:2%;text-align:center;background:#3abfdd") {
		      h4(style : "font-size: 120%;color: #fff;margin: 3px") {
		        span("Paiement facture effectu&eacute;")
		      }
		    }
		    div(style : "width:90%;margin:auto;margin-top : 20px;margin-bottom:30px") {
		    p("le client a bien effectu&eacute; le paiement de sa facture avec succ&eacute;s. vous pouvez lire ci-dessous les details de la transaction.")  
		    table {
		        tr{
		           td(style:"text-align:right;vertical-align:top;width : 100px;white-space : nowrap;padding-right : 2px"){
		               span("Client :")
		           }
                   td{
                       span("$user.name")
                   }
		         }
		         tr{
		           td(style:"text-align:right;vertical-align:top;width : 100px;white-space : nowrap;padding-right : 2px"){
		               span("Email :")
		           }
                   td{
                       span("$user.email")
                   }
		         }
		         tr{
		           td(style:"text-align:right;vertical-align:top;width : 100px;white-space : nowrap;padding-right : 2px"){
		               span("Structure :")
		           }
                   td{
                       span("$user.structure")
                   }
		        }
		        tr{
		           td(style:"text-align:right;vertical-align:top;width : 100px;white-space : nowrap;padding-right : 2px"){
		               span("Service :")
		           }
                   td{
                       span("$bill.service")
                   }
		        }
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
		               span("Pay&eacute;e par :")
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
		   div(style : "text-align:center;margin-top:30px;margin-bottom:10px") {
			    a(href : "$url/dashboard/billing",style : "font-size:130%;width:140px;margin:auto;text-decoration:none;background: #3abfdd;display:block;padding:10px;border-radius:2px;border:1px solid #eee;color:#fff;") {
			        span("Voir")
			    }
			}
		  }
		  
}