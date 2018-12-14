import groovy.json.JsonSlurper

metadata {
	definition (name: "HTTP Device", author: "JZ/drandyhaas", namespace:"drandyhaas") {
		capability "Switch"
		capability "Temperature Measurement"
		capability "Polling"
		capability "Refresh"
        
        command "get"
	}

	preferences {
		input("DeviceIP", "string", title:"Device IP Address", description: "Please enter your device's IP Address", required: true, displayDuringSetup: true)
		input("DevicePort", "string", title:"Device Port", description: "Empty assumes port 80.", required: false, displayDuringSetup: true)
		section() {
			input("HTTPAuth", "bool", title:"Requires User Auth?", description: "Choose if the HTTP requires basic authentication", defaultValue: false, required: true, displayDuringSetup: true)
			input("HTTPUser", "string", title:"HTTP User", description: "Enter your basic username", required: false, displayDuringSetup: true)
			input("HTTPPassword", "string", title:"HTTP Password", description: "Enter your basic password", required: false, displayDuringSetup: true)
		}
	}
	
	simulator {
	}

	tiles(scale: 2) {
		standardTile("RefreshTrigger", "device.refreshswitch", width: 2, height: 2, decoration: "flat") {
			state "default", label:'REFRESH', action: "refresh.refresh", icon: "st.secondary.refresh-icon", backgroundColor:"#53a7c0"
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("default", label:'Temp\n ${currentValue}',
				backgroundColors:[
					[value: 32, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
			)
		}
        
		main "RefreshTrigger"
		details(["RefreshTrigger", "temperature"])
	}
}

def get(String name, String val){
	log.debug "get $name $val "
    sendEvent(name: name, value: val)
}

def get(String name, BigDecimal val){
	log.debug "get $name $val "
    sendEvent(name: name, value: val)
}

def refresh() {
	def FullCommand = 'Refresh'
    def FullPath = '/'
	runCmd(FullCommand,FullPath)
}
def poll() {
	refresh()
}

def runCmd(String varCommand, DevicePath) {
	def host = DeviceIP
	def hosthex = convertIPtoHex(host).toUpperCase()
	def LocalDevicePort = ''
	if (DevicePort==null) { LocalDevicePort = "80" } else { LocalDevicePort = DevicePort }
	def porthex = convertPortToHex(LocalDevicePort).toUpperCase()
	device.deviceNetworkId = "$hosthex:$porthex"
	def userpassascii = "${HTTPUser}:${HTTPPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()

	log.debug "The device id configured is: $device.deviceNetworkId"

	def headers = [:] 
	headers.put("HOST", "$host:$LocalDevicePort")
	headers.put("Content-Type", "application/x-www-form-urlencoded")
	if (HTTPAuth) {
		headers.put("Authorization", userpass)
	}
	log.debug "The Header is $headers"

	def path = ''
	def body = ''
	def method = "POST"
	path = DevicePath
	body = varCommand 
	log.debug "POST body is: $body"

	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers
			)
		hubAction.options = [outputMsgToS3:false]
		log.debug hubAction
		hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}

def parse(String description) {
    def map = [:]
    def descMap = parseDescriptionAsMap(description)
    log.debug "descMap: ${descMap}"
    if (!descMap.containsKey("body")) return;
    
    def body = new String(descMap["body"].decodeBase64())
    log.debug "body: ${body}"
    
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    log.debug "result: ${result}"
    
    if (result.containsKey("temp")) {
    	def temp = result.temp.toDouble().round()
    	log.debug "temp: $temp "
    	sendEvent(name: "temperature", value: temp)
    }
    
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private String convertIPtoHex(ipAddress) { 
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	//log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
	return hex
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	//log.debug hexport
	return hexport
}
private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
	//log.debug("Convert hex to ip: $hex") 
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}