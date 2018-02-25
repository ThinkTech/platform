jQuery(document).ready(function( $ ) {
	page.details.bind = function(container,user) {
		if(user.active == "oui"){
			$("a.invite",container).hide();
		}else{
			$("a.invite",container).show();
		}
		$("a.lock",container).click(function(event){
			const link = $(this);
			const url = link.attr("href");
			$.ajax({
				  type: "POST",
				  url: url,
				  data: JSON.stringify(user),
				  contentType : "application/json",
				  success: function(response) {
					  if(response.status){
						  link.hide();
						  $("a.unlock",container).show();
						  link.prev().html("&nbsp;oui");
						  const tr = $(".table tr[id="+user.id+"]");
						  $("i.fa-lock",tr).show();
					  }
				  },
				  error : function(){
					  alert("erreur lors de la connexion au serveur");
				  },
				  dataType: "json"
			});
			return false;
		});
		$("a.unlock",container).click(function(event){
			const link = $(this);
			const url = link.attr("href");
			$.ajax({
				  type: "POST",
				  url: url,
				  data: JSON.stringify(user),
				  contentType : "application/json",
				  success: function(response) {
					  if(response.status){
						  link.hide();
						  $("a.lock",container).show();
						  link.prev().prev().html("&nbsp;non");
						  const tr = $(".table tr[id="+user.id+"]");
						  $("i.fa-lock",tr).hide();
					  }
				  },
				  error : function(){
					  alert("erreur lors de la connexion au serveur");
				  },
				  dataType: "json"
			});
			return false;
		});
		if(user.locked == "oui") {
			$("a.lock",container).hide();
			$("a.unlock",container).show();
		}else{
			$("a.lock",container).show();
			$("a.unlock",container).hide();
		}
		$("a.invite",container).click(function(){
			const link = $(this);
			const url = link.attr("href");
			$.ajax({
				  type: "POST",
				  url: url,
				  data: JSON.stringify(user),
				  contentType : "application/json",
				  success: function(response) {
					  if(response.status){
						 alert("la demande de collaboration a &edot;t&edot; bien renvoy&edot;e")
					  }
				  },
				  error : function(){
					  alert("erreur lors de la connexion au serveur");
				  },
				  dataType: "json"
			});
			return false;
		});
	};
	$(".password-form").submit(function(event){
		const form = $(this);
		const user = {};
		user.password = form.find("input[name=password]").val();
		user.confirm =  form.find("input[name=confirm]").val();
		if(user.password != user.confirm) {
			alert("les deux mots de passe ne sont pas identiques",function(){
				form.find("input[name=password]").focus();
			});
			return false;
		}
		page.wait({top : form.offset().top});
		$.ajax({
			  type: "POST",
			  url: form.attr("action"),
			  data: JSON.stringify(user),
			  contentType : "application/json",
			  success: function(response) {
				  if(response.status){
					  form.find("input[type=password]").val("");
					  alert("votre mot de passe a &edot;t&edot; bien modifi&edot;");
				  }
				  page.release();
			  },
			  error : function(){
				  page.release();
				  alert("erreur lors de la connexion au serveur");
			  },
			  dataType: "json"
		});
		return false;
	});
	
	$(".user a").click(function(event){
		$(".profile-details").hide();
		$(".profile-edition").show();
		$(this).hide();
		return false;
	});
	
	$(".profile-edition input[type=button]").click(function(event){
		$(".profile-details").show();
		$(".profile-edition").hide();
		$(".user a").show();
	});
	
	$(".profile form").submit(function(event){
		const form = $(this);
		const user = {};
		user.structure = {};
		user.name = form.find("input[name=name]").val().trim();
		user.email = form.find("input[name=email]").val().trim();
		user.telephone = form.find("input[name=telephone]").val().trim();
		user.profession = form.find("input[name=profession]").val().trim();
		user.structure.name = form.find("input[name=structure]").val().trim();
		user.structure.business = form.find("input[name=business]").val().trim();
		user.structure.ninea = form.find("input[name=ninea]").val().trim();
		page.wait({top : form.offset().top});
		$.ajax({
			  type: "POST",
			  url: form.attr("action"),
			  data: JSON.stringify(user),
			  contentType : "application/json",
			  success: function(response) {
				  if(response.status){
					  form.find("input[type=password]").val("");
					  alert("votre profil a &edot;t&edot; bien modifi&edot;");
					  $(".profile-details #name").html("&nbsp;"+user.name);
					  $(".profile-details #email").html("&nbsp;"+user.email);
					  $(".profile-details #telephone").html("&nbsp;"+user.telephone);
					  $(".profile-details #profession").html("&nbsp;"+user.profession);
					  $(".profile-details #structure").html("&nbsp;"+user.structure.name);
					  $(".profile-details #business").html("&nbsp;"+user.structure.business);
					  $(".profile-details #ninea").html("&nbsp;"+user.structure.ninea);
					  $(".profile-details").show();
					  $(".profile-edition").hide();
					  $(".user a").show();
					  if(page.updateUserName) page.updateUserName(user.name);
				  }else{
					  alert("cet email est d&edot;ja utilis&edot; par un autre utilisateur",function(){
						  form.find("input[name=email]").select().focus();
					  });
				  }
				  page.release();
			  },
			  error : function(){
				  page.release();
				  alert("erreur lors de la connexion au serveur");
			  },
			  dataType: "json"
		});
		return false;
	});
	
	$("legend a").click(function(event){
		const window = $(".window.form").show();
		$("form input[type=email]",window).val("");
	});
	
	$(".window form").submit(function(event){
		const window = $(".window.form").hide();
		const form = $(this);
		const user = {};
		user.email = form.find("input[name=email]").val().trim();
		page.wait({top : form.offset().top});
		$.ajax({
			  type: "POST",
			  url: form.attr("action"),
			  data: JSON.stringify(user),
			  contentType : "application/json",
			  success: function(response) {
				  if(response.id){
					  form.find("input[type=password]").val("");
					  user.id = response.id;
					  page.table.addRow(user,function(row){
						  page.release();
						  alert("votre collaborateur a &edot;t&edot; bien ajout&edot;");
						  $("a",row).click(function(event){
								page.details.removeCollaborator($(this).attr("href"));
								return false;
						  });
					  });
				  }else{
					  alert("cet email est d&edot;ja utilis&edot; par un autre utilisateur",function(){
						  window.show();
						  form.find("input[name=email]").select().focus();
					  });
				  }
				  page.release();
			  },
			  error : function(){
				  page.release();
				  alert("erreur lors de la connexion au serveur");
			  },
			  dataType: "json"
		});
		return false;
	});
	
	$(".table a").click(function(event){
		page.details.removeCollaborator($(this).attr("href"));
		return false;
	});
	
	page.details.removeCollaborator = function(url){
		confirm("&ecirc;tes vous s&ucirc;r de vouloir supprimer ce collaborateur?",function(){
			$.ajax({
				  type: "GET",
				  url: url,
				  success: function(response) {
					  if(response.id){
						  alert("ce collaborateur a &edot;t&edot; bien supprim&edot;");
						  $(".table tr[id="+response.id+"]").remove();
					  }
				  },
				  error : function(){
					  alert("erreur lors de la connexion au serveur");
				  },
				  dataType: "json"
			});
		});
	};
});
