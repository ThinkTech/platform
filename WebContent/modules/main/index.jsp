<%@ taglib prefix="s" uri="/struts-tags"%>
<!-- services -->
	<div class="services">
		<div class="container">
			<div class="services-row-agileinfo">
			    <s:iterator value="#application.moduleManager.getVisibleModules('front-end')" var="it">
		           <div class="col-sm-4 col-xs-6 services-w3grid">
					<span class="glyphicon glyphicon-heart hi-icon" aria-hidden="true"></span>
					<h5><a href="${it.url}">${it.name}</a></h5>
					<p>${it.description}</p>
				</div>
		        </s:iterator>
				<div class="clearfix"> </div>
			</div>
			<div class="clearfix"> </div>
		</div>
	</div>
	<!-- //services -->