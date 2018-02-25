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
 <meta name="twitter:title" property="og:title" content="ThinkTech - Portail"> 
 <meta name="twitter:description" property="og:description" content="Bienvenue sur la plateforme de ThinkTech"> 
 <meta name="twitter:image" property="og:image" content="${baseUrl}/templates/invent/images/banner.jpeg">
<style type="text/css">
 <%@include file="/templates/invent/css/bootstrap.css"%>
 <%@include file="/css/metamorphosis.css"%>
</style>
<link href="templates/invent/css/template.css" rel="stylesheet"> 
<link href="templates/invent/css/font-awesome.css" rel="stylesheet"> <!-- font-awesome icons -->
<!-- //Custom Theme files --> 
<!-- js -->
<script src="templates/invent/js/jquery-2.2.3.min.js"></script>  
<!-- //js -->
<!-- web-fonts -->  
<link href="//fonts.googleapis.com/css?family=Open+Sans+Condensed:300,300i,700" rel="stylesheet"> 
<link href='//fonts.googleapis.com/css?family=Roboto:400,100,100italic,300,300italic,400italic,500,500italic,700,700italic,900,900italic' rel='stylesheet' type='text/css'>
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
					<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
						<span class="sr-only">Toggle navigation</span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button> 
					<h1><a href="index.html">ThinkTech Platform</a></h1> 
				</div>
				<!-- Collect the nav links, forms, and other content for toggling -->
				<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
					<div class="w3menu navbar-left">
						<ul class="nav navbar">
							<li><a href="index.html" class="active">Accueil</a></li>
							<li><a href="about.html">Services</a></li> 
							<li><a href="contact.html">Contact</a></li>
						</ul>
					</div>
					<div class="w3ls-bnr-icons social-icon navbar-right">
						<a href="#" class="social-button twitter"><i class="fa fa-twitter"></i></a>
						<a href="#" class="social-button facebook"><i class="fa fa-facebook"></i></a> 
					</div> 
					<div class="clearfix"> </div>  
				</div>
			</div>	
		</div>	
		<!-- //navigation --> 
		<div class="banner-w3text w3layouts">
			<h2>Nos <span>Services</span></h2> 
			<h6>Selon votre besoin, souscrivez à nos services </h6> 
		</div>  
	</div>
	<!-- //banner --> 
	<!-- services -->
	<div class="services">
		<div class="container">
			<h3 class="agileits-title">Services</h3> 
			<div class="services-row-agileinfo">
				<div class="col-sm-4 col-xs-6 services-w3grid">
					<span class="glyphicon glyphicon-heart hi-icon" aria-hidden="true"></span>
					<h5>Cum soluta nobis</h5>
					<p>Itaque earum rerum hic a sapiente delectus</p>
				</div>
				<div class="col-sm-4 col-xs-6 services-w3grid">
					<span class="glyphicon glyphicon-list-alt hi-icon" aria-hidden="true"></span>
					<h5>Soluta vum nobis</h5>
					<p>Itaque earum rerum hic a sapiente delectus</p>
				</div>
				<div class="col-sm-4 col-xs-6 services-w3grid">
					<span class="glyphicon glyphicon-leaf hi-icon" aria-hidden="true"></span>
					<h5>Nobis cum soluta</h5>
					<p>Itaque earum rerum hic a sapiente delectus</p>
				</div>
				<div class="col-sm-4 col-xs-6 services-w3grid bottom-grids wow fadeInUp animated" data-wow-delay=".5s">
					<span class="glyphicon glyphicon-globe hi-icon" aria-hidden="true"></span>
					<h5>Taaum solu nobis</h5>
					<p>Itaque earum rerum hic a sapiente delectus</p>
				</div>
				<div class="col-sm-4 col-xs-6 services-w3grid bottom-grids">
					<span class="glyphicon glyphicon-cutlery hi-icon" aria-hidden="true"></span>
					<h5>Vum soluta nobis</h5>
					<p>Itaque earum rerum hic a sapiente delectus</p>
				</div>
				<div class="col-sm-4 col-xs-6 services-w3grid bottom-grids">
					<span class="glyphicon glyphicon-th-large hi-icon" aria-hidden="true"></span>
					<h5>Bisno cum soluta</h5>
					<p>Itaque earum rerum hic a sapiente delectus</p>
				</div>
				<div class="clearfix"> </div>
			</div>
			<div class="clearfix"> </div>
		</div>
	</div>
	<!-- //services -->
	<!-- footer -->
	<div class="footer">
		<div class="container">
			<div class="footer-row w3l-newsletter"> 
				<div class="col-md-6 w3_agile_newsletter_left">
					<h4>ABONNEZ-VOUS AUX NOUVELLES</h4>
				</div>
				<div class="col-md-6 w3_agile_newsletter_right">
					<form action="#" method="post">
						<input type="email" name="Email" placeholder="email..." required="">
						<input type="submit" value="ANNULER">
					</form>
				</div>
				<div class="clearfix"> </div> 
			</div> 
			<div class="footer-bottom">
				<div class="col-sm-4 col-xs-4 footer-grids footer-mdl">
					<div class="col-md-6 col-xs-6 grid-left">
						<ul>
							<li><a href="index.html"><i class="fa fa-angle-right"></i>Accueil</a></li>
							<li><a href="about.html"><i class="fa fa-angle-right"></i>Nos Services</a></li>
							<li><a href="gallery.html"><i class="fa fa-angle-right"></i>Contact</a></li>
						</ul>	
					</div>
					<div class="clearfix"> </div>
				</div>
				<div class="col-sm-6 col-xs-4 footer-grids">
					<ul>
						<li class="phn"><i class="fa fa-phone"></i>+(221) 33 855 10 91</li>
						<li><a href="mailto:info@thinktech.sn"><i class="fa fa-envelope-o"></i> info@thinktech.sn</a></li>
					</ul>
					<p>© 2017 Tous droits réservés | Concu par <a href="https://www.thinktech.sn/">ThinkTech</a></p>
				</div>
				<div class="clearfix"> </div>
			</div>
		</div>
	</div>
	<!-- footer --> 
	<!-- Stats -->
	<script src="templates/invent/js/waypoints.min.js"></script> 
	<script src="templates/invent/js/counterup.min.js"></script> 
	<script>
		jQuery(document).ready(function( $ ) {
			$('.counter').counterUp({
				delay: 10,
				time: 2000
			});
		});
	</script>
	<!-- //Stats --> 
	<!-- start-smooth-scrolling -->
	<script src="templates/invent/js/SmoothScroll.min.js"></script> 
	<script type="text/javascript" src="templates/invent/js/move-top.js"></script>
	<script type="text/javascript" src="templates/invent/js/easing.js"></script>	
	<script type="text/javascript">
			jQuery(document).ready(function($) {
				$(".scroll").click(function(event){		
					event.preventDefault();
			
			$('html,body').animate({scrollTop:$(this.hash).offset().top},1000);
				});
			});
	</script>
	<!-- //end-smooth-scrolling -->	
	<!-- smooth-scrolling-of-move-up -->
	<script type="text/javascript">
		$(document).ready(function() {
			/*
			var defaults = {
				containerID: 'toTop', // fading element id
				containerHoverID: 'toTopHover', // fading element hover id
				scrollSpeed: 1200,
				easingType: 'linear' 
			};
			*/
			
			$().UItoTop({ easingType: 'easeOutQuart' });
			
		});
	</script>
	<!-- //smooth-scrolling-of-move-up -->    
	<!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="templates/invent/js/bootstrap.js"></script>
</body>
</html>