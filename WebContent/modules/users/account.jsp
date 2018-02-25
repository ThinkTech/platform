<%@ taglib prefix="s" uri="/struts-tags"%>
<div class="inner-block">
 <div class="logo-name">
	<h1><i class="fa fa-${activeItem.icon}" aria-hidden="true"></i>Votre Compte</h1> 								
 </div>
<!--web-forms-->
			    <div class="web-forms">
				 <!--first-one-->
				 <div class="col-md-4 first-one">
				  <div class="first-one-inner password-area">
				     <h3 class="tittle"><i class="fa fa-key"></i> Mot de Passe</h3>
					<form class="password-form" action="${url}/password/change">
						<input type="password" name="password" class="text" required>
						<input type="password" name="confirm" required>
						<div class="submit"><input type="submit" value="Changer" ></div>
					</form>
				   </div>
				   <fieldset style="display:${user.role == 'administrateur' ? 'block' : 'none'}">
					   <legend>
					     <i class="fa fa-${activeItem.icon}"></i> Vos Collaborateurs <a class="user-add"><i class="fa fa-plus" aria-hidden="true"></i></a>
					   </legend>
					   <div class="table-responsive">
					      <table data-url="${url}/collaborators/info" class="table table-hover">
                              <tbody> 
                             	 <s:iterator value="#request.collaborators" var="collaborator" status="status">
	                                <tr id="${collaborator.properties.id}">
	                                   <td><span class="number">${status.index+1}</span></td>
	                                   <td>${collaborator.properties.name}
	                                     <span>
	                                   	 <i class="fa fa-check" style="display:${collaborator.properties.active ? 'inline-block' : 'none'}"></i>
	                                   	 <i class="fa fa-envelope" style="display:${collaborator.properties.active ? 'none' : 'inline-block'}"></i>
	                                   	 <a href="${url}/collaborators/remove?id=${collaborator.properties.id}"><i class="fa fa-remove" style="display:${collaborator.properties.active ? 'none' : 'inline-block'}"></i></a>
	                                   	 <i class="fa fa-lock" style="display:${collaborator.properties.locked ? 'inline-block' : 'none'}"></i>
	                                   	 </span>
	                                   </td>
	                          	   </tr>
	                          	</s:iterator>             
                              <template>
							     {#.}
							      <tr id="{id}">
							            <td><span class="number"></span></td>
							   	        <td>{email}
								   	        <span>
								   	        <i class="fa fa-envelope"></i>
								   	        <a href="${url}/collaborators/remove?id={id}"><i class="fa fa-remove"></i></a>
								   	        </span>
							   	        </td>
							   	    </tr>
							     {/.}
							   </template>
                          </tbody>
                      </table>
                      <div class="empty"><span>aucun collaborateur</span></div>
				</div>
				<div class="window form">
				  <div>
					<span title="fermer" class="close">X</span>
	 				<h1><i class="fa fa-${activeItem.icon}-plus" aria-hidden="true"></i> Nouveau Collaborateur</h1>
	 				<form action="${url}/collaborators/add">
						<fieldset>
	  						<span class="text-right"><i class="fa fa-envelope" aria-hidden="true"></i> Email </span>
	  						<input type="email" name="email" required>
	  	                 </fieldset>
	  	                 <div class="submit"><input type="submit" value="Ajouter"></div>
	  	             </form>
	              </div>
				</div>
				<div class="window details">
				     <div>
						<span title="fermer" class="close">X</span>
						<section>
						 <template>
						 <h1><i class="fa fa-${activeItem.icon}" aria-hidden="true"></i> Collaborateur</h1>
						<fieldset>
							<span class="text-right">Nom </span> <span>&nbsp;{name}</span>
							<span class="text-right">Email </span> <span>&nbsp;{email}</span>
							<span class="text-right">Role </span> <span>&nbsp;{role}</span>
							<span class="text-right">Compte Actif </span> <span>&nbsp;{active}</span>
							<a class="invite" href="${url}/collaborators/invite" style="display:none"><i class="fa fa-envelope"></i></a>
							<span class="text-right">Compte Bloqué </span> <span>&nbsp;{locked}</span>
							<a class="lock" href="${url}/account/lock" style="display:none"><i class="fa fa-lock"></i></a>
							<a class="unlock" href="${url}/account/unlock" style="display:none"><i class="fa fa-unlock"></i></a>
						</fieldset>
						</template>
						</section>
					</div>
				</div>
				   </fieldset>
			      </div>
				 
				   <!--/third-one-->
				   <div class="col-md-5 first-one">
					    <div class="first-one-inner lost">
						    <div class="user profile">
								<div class="profile-bottom">
									<i class="fa fa-${activeItem.icon}" aria-hidden="true"></i>
								</div>
								<div>
								   <fieldset class="profile-details">
								        <span class="text-right">&nbsp;Prénom et Nom </span>
										<span id="name">&nbsp;${user.name}</span>
									    <span class="text-right">&nbsp;Email </span>
										<span id="email">&nbsp;${user.email}</span>
										<span class="text-right">&nbsp;Profession </span>
   										<span id="profession">&nbsp;${user.profession}</span>
										<span class="text-right">&nbsp;Téléphone </span>
										<span id="telephone">&nbsp;${user.telephone}</span>
										<span class="text-right">&nbsp;Role </span>
   										<span id="role">&nbsp;${user.role}</span>
   										<span class="text-right" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">&nbsp;Structure </span>
   										<span id="structure" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">&nbsp;${user.structure.name}</span>
   										<span class="text-right" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">&nbsp;Activité Principale </span>
   										<span id="business" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">&nbsp;${user.structure.business}</span>
   										<span class="text-right" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">&nbsp;Ninea </span>
   										<span id="ninea" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">&nbsp;${user.structure.ninea}</span>
   								 </fieldset>
   								 <a class="text-center">[ modifier ]</a>
   								 <form action="${url}/profile/update">
   								 <fieldset class="profile-edition">
   								    	<span class="text-right">Prénom et Nom </span>
										<input type="text" name="name" value="${user.name}" required>
									    <span class="text-right">Email </span>
										<input type="email" name="email" value="${user.email}" required>
										<span class="text-right">Profession </span>
   										<input name="profession" type="text" value="${user.profession}">
										<span class="text-right">Téléphone </span>
										<input name="telephone" value="${user.telephone}" type="text">
										<span class="text-right" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">Structure </span>
   										<input name="structure" value="${user.structure.name}" type="text" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">
   										<span class="text-right" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">Activité Principale </span>
   										<input name="business" value="${user.structure.business}" type="text" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">
   										<span class="text-right" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">Ninea </span>
   										<input name="ninea" value="${user.structure.ninea}" type="text" style="display:${user.role == 'administrateur' ? 'inline-block' : 'none'}">
   								    <div class="submit">
   								      <input type="button" value="Annuler">
   								      <input type="submit" value="Modifier">
   								    </div>
   								 </fieldset>
   								</form>
								</div>
							</div>
					     </div>
				      </div>
					  	<div class="clearfix"></div>
				   <!--//third-one-->
			    </div>
</div>
<script src="${js}/account.js" defer></script>