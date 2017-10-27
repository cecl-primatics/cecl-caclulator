$(document).ready(function() {

	$("#btnCalc").click(function(event) {

		//stop submit the form, we will post it manually.
		event.preventDefault();
		fire_ajax_calc();

	});

});

function fire_ajax_calc() {

	// Get form
	var form = $('#calcForm')[0];
	var scenario = $("#dropScenario").find(":selected").text();

	$("#btnCalc").prop("disabled", true);
	
	$.ajax({
		type : "POST",
		enctype : 'form-data',
		url : "/home/calculate",
		data : scenario,
		//http://api.jquery.com/jQuery.ajax/
		//https://developer.mozilla.org/en-US/docs/Web/API/FormData/Using_FormData_Objects
		processData : false, //prevent jQuery from automatically transforming the data into a query string
		contentType : false,
		cache : false,
		timeout : 600000,
		success : function(data) {
			$('#scenarioName').text(scenarioName);
			$("#result1").text(data);
			$('#myCal').remove();
			$('#calculator').load('home.html');
			$("#calculator").hide().fadeIn('slow');
			console.log("SUCCESS : ", data);
			$("#btnCalc").prop("disabled", true);
		},
		error : function(e) {
			$('#scenarioName').text(scenarioName);
			$("#result1").text(e.responseText);
			$('#myCal').remove();
			$('#calculator').load('home.html');
			$("#calculator").hide().fadeIn('slow');
			console.log("ERROR : ", e);
			$("#btnCalc").prop("disabled", false);
		}
	});
}