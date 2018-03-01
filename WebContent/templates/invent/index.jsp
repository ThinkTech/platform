<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE HTML>
<html>
<head>
<base href="${path}"/>
<title>ThinkTech - Platform</title>
<!-- Meta tag Keywords -->
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="theme-color" content="#08ccf7"> 
<meta property="og:type" content="website">
<meta name="description" content="Bienvenue sur la plateforme de ThinkTech"> 
<meta name="twitter:card" content="summary">
 <meta name="twitter:site" content="@thinktech">
 <meta name="twitter:domain" property="og:site_name" content="platform.thinktech.sn">
 <meta name="twitter:url" property="og:url" content="${baseUrl}">
 <meta name="twitter:title" property="og:title" content="ThinkTech - Platform"> 
 <meta name="twitter:description" property="og:description" content="Bienvenue sur la plateforme de ThinkTech"> 
 <meta name="twitter:image" property="og:image" content="${baseUrl}/templates/invent/images/banner.jpeg">
<style type="text/css">
 <%@include file="/css/metamorphosis.css"%>
</style>
<link href="templates/invent/css/bootstrap.css" rel="stylesheet">
<link href="templates/invent/css/template.css" rel="stylesheet"> 
<link href="templates/invent/css/font-awesome.css" rel="stylesheet"> <!-- font-awesome icons -->
<!-- //Custom Theme files --> 
<!-- web-fonts -->  
<link href="//fonts.googleapis.com/css?family=Open+Sans+Condensed:300,300i,700" rel="stylesheet"> 
<!-- //web-fonts -->
<link rel="icon" href="images/favicon.png" sizes="32x32">
<link rel="manifest" href="manifest.json">
</head>
<body> 
	<!-- banner -->
	<div class="agileits-banner">  
		<!-- navigation -->
		<div class="top-nav w3-agiletop">
			<div class="container">
				<div class="navbar-header w3llogo">
					<h1><a href="index.html">ThinkTech Platform</a></h1> 
				</div>
				<!-- Collect the nav links, forms, and other content for toggling -->
				<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
					<div class="w3menu navbar-left">
						<ul class="nav navbar">
							<li><a href="index.html" class="active">Nos Services</a></li> 
						</ul>
					</div>
					<div class="clearfix"> </div>  
				</div>
			</div>	
		</div>	
		<!-- //navigation --> 
		<div class="banner-w3text w3layouts">
			<h2>Nos <span>Services</span></h2> 
			<h6>Selon votre besoin, souscrivez à un de nos services </h6> 
		</div>  
	</div>
	<!-- //banner --> 
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
	<script type="text/javascript">
	    <%@include file="/js/jquery-3.1.1.min.js"%>
	    <%@include file="/js/metamorphosis.js"%>
		<%@include file="/templates/invent/js/template.js"%>
	</script>  
</body>
</html>