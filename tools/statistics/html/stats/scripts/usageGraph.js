//set theme
var theme = "dojox.charting.themes.PlotKit.blue";

//Include dojo libraries
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.Dialog");
dojo.require("dijit.form.Slider")
dojo.require("dijit.ProgressBar");
dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.action2d.Tooltip");
dojo.require("dojox.charting.action2d.Highlight");;
dojo.require(theme);

//Global fields
var chartDiv = "dcnChart";
var tableDiv = "dcnTableDiv";
var chart = null;
var offsetX = 0;
var offsetY = 0;
var scaleX = 1;
var scaleY = 1;
var panTracker = null;
var tableHeaders = ["# of Resv", "BW Sum", "BW Avg.", 
					"BW Max", "BW Min", "BW Std Dev", "Dur. Sum", 
					"Dur. Avg.", "Dur. Max", "Dur. Min", "Dur. Std Dev",
					"Avg Hops", "Max Hops","Min Hops","Hop Std Dev"];				
//must match Y_AXIS_TYPE keys in CGI
var tableFieldKeys = ["resvCount", "bwSum", "bwAvg", "bwMax","bwMin","bwStd", 
					  "durSum", "durAvg", "durMax", "durMin", "durStd",
					  "hopCountAvg", "hopCountMax", "hopCountMin", "hopCountStd"];
					  
//Global form elements
var edgeMenu = "";
var timeIncrementMenu = "";

function init(){
	var d = new Date();
	var newYearsDay = new Date();
	newYearsDay.setFullYear(d.getFullYear(), 0, 1);
	dijit.byId("timeStart").attr('value', newYearsDay);
	dijit.byId("timeEnd").attr('value', d);
	edgeMenu = dojo.byId("edgeMenu");
	timeIncrementMenu = dojo.byId("timeIncrementMenu");
	enableXTypeFields();
	requestFormMenus();
	
	//set events
	dojo.connect(dijit.byId("zoomXSlider"), "onChange", zoomX);
	dojo.connect(dijit.byId("zoomYSlider"), "onChange", zoomY);
	dojo.connect(dojo.byId(chartDiv), "onmousedown", panMouseDown);
	dojo.connect(dojo.byId(chartDiv), "onmouseup", panMouseUp);
	dojo.connect(dojo.byId(chartDiv), "onmousemove", panMouseMove);
}

function requestChart(formId){
	if(!validateChartRequest()){
		return;
	}
	dojo.byId("reportButton").disabled = true;
	dojo.byId("reportProgress").style.visibility = "visible";
	dijit.byId("reportProgress").update({indeterminate: true});
	dojo.xhrPost({
		url: "/cgi-bin/dcn_usage.pl",
		form: formId,
		handleAs: "json",
		timeout: 30000,
		load: drawChart,
		error: handleChartError
	});
}

function requestFormMenus(){
	dojo.xhrGet({
		url: "/cgi-bin/dcn_usage_form_builder.pl?listType=links-users",
		handleAs: "json",
		timeout: 30000,
		load: loadFormMenus,
		error: function(response, ioArgs){
			alert("Error: Unable to initialize forms because encountered '"+response.message + "'");
		}
	});
}

function loadFormMenus(response, ioArgs){
	var userSettings = document.getElementById("userSettings");
	for(var i = 0; i < response.users.length; i++){
		var check = document.createElement("input");
		addAttr("type", "checkbox", check);
		addAttr("name", "xUser", check);
		addAttr("value", response.users[i], check);
		userSettings.appendChild(check);
		userSettings.appendChild(document.createTextNode(response.users[i]));
		userSettings.appendChild(document.createElement("br"));
	}
	var linkSettings = document.getElementById("linkSettings");
	for(var i = 0; i < response.links.length; i++){
		var check = document.createElement("input");
		addAttr("type", "checkbox", check);
		addAttr("name", "xLink", check);
		addAttr("value", response.links[i].label + "=>"+ response.links[i].urn, check);
		linkSettings.appendChild(check);
		linkSettings.appendChild(document.createTextNode(response.links[i].label));
		linkSettings.appendChild(document.createElement("br"));
	}
	var edgeSettings = childById(edgeMenu, "edgeSettings");
	for(var i = 0; i < response.edges.length; i++){
		var check = document.createElement("input");
		addAttr("type", "checkbox", check);
		addAttr("name", "xEdge", check);
		addAttr("value", response.edges[i].label + "=>"+ response.edges[i].urn, check);
		edgeSettings.appendChild(check);
		edgeSettings.appendChild(document.createTextNode(response.edges[i].label));
		edgeSettings.appendChild(document.createElement("br"));
	}
	
	if (document.getElementsByName("userRadio")[0].checked) {
		disableAll("xUser");
	}
	if (document.getElementsByName("linkRadio")[0].checked) {
		disableAll("xLink");
	}
	if (document.getElementsByName("edgeRadio") != null && 
		document.getElementsByName("edgeRadio").length > 0 &&
		document.getElementsByName("edgeRadio")[0].checked) {
		disableAll("xEdge");
	}
	requestChart("chartParams");
}

function drawChart(response, ioArgs){
	dojo.byId(chartDiv).innerHTML = "";
		var chart1 = new dojox.charting.Chart2D(chartDiv);
	if(response.error != null){
		alert("Error: " + response.error);
		dojo.byId("reportButton").disabled = false;
		dojo.byId("reportProgress").style.visibility = "hidden";
		dijit.byId("reportProgress").update({indeterminate: false});
		return;
	}
	
	chart1.addPlot("default", {type: "Columns", gap: 5});
	//x:
	chart1.addAxis("x", {labels: response.labels, 
		majorLabels: true, 
		stroke: {
			color: "#CCCCCC",
			width: 2
		},
		majorTick: { stroke: "#777777", length: 4}
	});
	//y:
	chart1.addAxis("y", {vertical: true, 
		majorLabels: true, 
		stroke: {
			color: "#CCCCCC",
			width: 2
		},
		majorTick: { stroke: "#777777", length: 4 },
		min: 0
	});
	
	//actions
	var tooltip = new dojox.charting.action2d.Tooltip(chart1, "default");
	var highlight = new dojox.charting.action2d.Highlight(chart1, "default");
	//var shake = new dojox.charting.action2d.Shake(chart1, "default");
	chart1.addSeries("Series 1", response.graph_data);
	chart1.setTheme(eval(theme));
	chart1.setAxisWindow("x", scaleX, 0);
	chart1.setAxisWindow("y", scaleY, 0);
	chart1.render();
	
	//set global chart
	chart = chart1;
	
	//set title to visible
	dojo.byId("titleGraphType").innerHTML = dojo.byId("yType").options[dojo.byId("yType").selectedIndex].text;
	if(dojo.byId("xType").value == "time"){
		dojo.byId("titleX").innerHTML = dojo.byId("xTimeIncrement").options[dojo.byId("xTimeIncrement").selectedIndex].text;
	}else{
		dojo.byId("titleX").innerHTML = dojo.byId("xType").options[dojo.byId("xType").selectedIndex].text;
	}
	dojo.byId("titleStart").innerHTML = dojo.byId("timeStart").value;
	dojo.byId("titleEnd").innerHTML = dojo.byId("timeEnd").value;
	dojo.byId("chartTitle").style.visibility = "visible";
	
	drawTable(response.labels, response.report_data);
	dojo.byId("reportButton").disabled = false;
	dojo.byId("reportProgress").style.visibility = "hidden";
	dijit.byId("reportProgress").update({indeterminate: false});
	dojo.byId("tableKey").style.visibility = "visible";
	dijit.byId('settingsDialog').hide()
}

function handleChartError(response, ioArgs){
	dojo.byId("reportButton").disabled = false;
	dojo.byId("reportProgress").style.visibility = "hidden";
	dijit.byId("reportProgress").update({indeterminate: false});
	alert("Error: " + response.message);
}

function validateChartRequest(){
	if(document.getElementsByName("userRadio")[1].checked == true){
		var checks = document.getElementsByName("xUser");
		var checked = false;
		for(var i = 0; i < checks.length; i++){
			if(checks[i].checked){
				checked = true;
				break;
			}
		}
		if(!checked){
			alert("Please select a user");
			return false;
		}
	}
	
	if(document.getElementsByName("linkRadio")[1].checked == true){
		var checks = document.getElementsByName("xLink");
		var checked = false;
		for(var i = 0; i < checks.length; i++){
			if(checks[i].checked){
				checked = true;
				break;
			}
		}
		if(!checked){
			alert("Please select a link");
			return false;
		}
	}
	
	if(dojo.byId("timeStart").value == ""){
		alert("Please enter a end date");
		return false;
	}
	
	if(dojo.byId("timeEnd").value == ""){
		alert("Please enter a start date");
		return false;
	}
	
	return true;
}

function drawTable(labels, data){
	var div = document.getElementById(tableDiv);
	div.innerHTML = "";
	
	var table = document.createElement("table");
	var thead = document.createElement("thead");
	var tbody = document.createElement("tbody");
	
	addAttr("class", "sortable", table);
	addAttr("id", "dcnTable", table);
	
	/* create header */
	var headTR = document.createElement("tr");
	//add empty col for label
	var labelTh = document.createElement("th");
	labelTh.innerHTML = "X";
	headTR.appendChild(labelTh);
	for(var i = 0; i < tableHeaders.length; i++){
		var th = document.createElement("th");
		th.innerHTML = tableHeaders[i];
		headTR.appendChild(th);
	}
	
	/* create body */
	for(var i = 0; i < labels.length; i++){
		var tr = document.createElement("tr");
		//add label
		var labelTd = document.createElement("td")
		labelTd.innerHTML = labels[i].text;
		var evenOddRow = "Odd";
		if((i % 2) == 0){
			evenOddRow ="Even";
		}
		addAttr("class", "dcnTableLabel", labelTd);
		tr.appendChild(labelTd);
		//add data
		for(var j = 0; j < tableFieldKeys.length; j++) {
			var td = document.createElement("td");
			td.innerHTML = eval("data." + tableFieldKeys[j] + "[i]");
			tr.appendChild(td);
		}
		tbody.appendChild(tr);
	}
	thead.appendChild(headTR);
	table.appendChild(thead);
	table.appendChild(tbody);
	
	div.appendChild(table);
	sortables_init()
}

function childById(parent, id){
	for(var i = 0; i < parent.childNodes.length; i++){
		if(parent.childNodes[i].id == id){
			return parent.childNodes[i];
		}
	}
	return null;
}

function addAttr(name, value, elem){
	var attr = document.createAttribute(name);
	attr.nodeValue = value;
	elem.setAttributeNode(attr);
	return elem;
}
function panMouseDown(e){
	panTracker = {x: e.clientX, y: e.clientY, ox: offsetX, oy: offsetY };
	dojo.stopEvent(e);
}

function panMouseUp(e){
	panTracker = null;
	dojo.stopEvent(e);
}

function panMouseMove(e){
	if(panTracker){
		var dx = e.clientX - panTracker.x;
		var dy = e.clientY - panTracker.y;
		offsetX = panTracker.ox - dx;
		offsetY = panTracker.oy + dy;
		chart.setWindow(scaleX, scaleY, offsetX, offsetY).render();
		dojo.stopEvent(e);
	}
}

function zoomX(value){
	scaleX = value;
	chart.setWindow(scaleX, scaleY, offsetX, offsetY).render();
}

function zoomY(value){
	scaleY = value;
	chart.setWindow(scaleX, scaleY, offsetX, offsetY).render();
}

function disableAll(name){
	var checkboxes = document.getElementsByName(name);
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].disabled = true;
	}
}

function enableAll(name){
	var checkboxes = document.getElementsByName(name);
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].disabled = false;
	}
}

function enableXTypeFields(){
	var xType = dojo.byId("xType");
	var timeIncrLabel = dojo.byId("timeIncrementLabel");
	var timeIncrMenu = dojo.byId("timeIncrementMenu");
	var xParamRow = dojo.byId("xParamRow");
	var xParamLabel = dojo.byId("xParamLabel");
	
	if(xType.value == "time"){
		xParamLabel.innerHTML = "Increment Time By:";
		xParamRow.appendChild(timeIncrementMenu);
	}else if(childById(xParamRow, "timeIncrementMenu") != null){
		xParamRow.removeChild(timeIncrementMenu);
	}
	
	if(xType.value == "links"){
		xParamLabel.innerHTML = "Only show reservations that use:";
		xParamRow.appendChild(edgeMenu);
		if (childById(edgeMenu, "edgeRadioAll").checked) {
			disableAll("xEdge");
		}
	}else if(childById(xParamRow, "edgeMenu") != null){
		xParamRow.removeChild(edgeMenu);
	}
	
	if(xType.value == "users"){
		xParamLabel.innerHTML = "";
	}
}

function selectAllChecks(name, radioName, radioIndex){
	var checkboxes = document.getElementsByName(name);
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].checked = true;
	}
	var radios = document.getElementsByName(radioName);
	radios[radioIndex].checked = true;
	enableAll(name);
}

function unselectAllChecks(name, radioName, radioIndex){
	var checkboxes = document.getElementsByName(name);
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].checked = false;
	}
	var radios = document.getElementsByName(radioName);
	radios[radioIndex].checked = true;
	enableAll(name);
}