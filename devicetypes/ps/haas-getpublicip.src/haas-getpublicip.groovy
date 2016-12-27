metadata {
	definition (name: "Haas_GetPublicIP", namespace: "ps", author: "patrick@patrickstuart.com") {
		capability "Polling"
		capability "Water Sensor"        
        
		attribute "publicIp", "string"
        attribute "updated", "number"
    }

	simulator {
    }
    
    preferences {
    }

	tiles {
        valueTile("publicIp", "device.publicIp", inactiveLabel: false, decoration: "flat", columns:2) {
        	state "default", label:'${currentValue}', unit:"Public IP"
        }
        standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}
        standardTile("refresh", "device.poll", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        valueTile("updatedlast", "device.updated", decoration: "flat", inactiveLabel: false) {
			state "default", label:'${currentValue} updated'
		}
        
        main "publicIp"
        details(["publicIp", "water", "refresh", "updatedlast"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def map = stringToMap(description)
    def bodyString = new String(map.body.decodeBase64())
    log.debug bodyString
	def body = new XmlSlurper().parseText(bodyString)
    log.debug body
    def publicip2 = body.toString().replace("Current IP CheckCurrent IP Address: ","")
    log.debug publicip2
    sendEvent(name: 'publicIp', value: publicip2)
    def values=publicip2.tokenize(".")
    log.debug values[0]
    log.debug values[1]
    if (values[0]!="68") {
    log.debug "wet"
    sendEvent(name: "water", value: "wet")
    }
    else {
    log.debug "dry"
    sendEvent(name: "water", value: "dry")
    }
    
    if (!device.currentValue("updated")) {
        sendEvent(name:"updated", value: 1)
    }
    else{
        if (device.currentValue("updated") > 1000) sendEvent(name:"updated", value: 1)
    	else sendEvent(name:"updated", value: device.currentValue("updated")+1)
	}
    def upd = device.currentValue("updated")
    log.debug "updated is now $upd "
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    login()
}

def login() {   
               
        def method = "GET"
        def host = "216.146.38.70"
        def hosthex = convertIPtoHex(host)
        def porthex = convertPortToHex(80)
        device.deviceNetworkId = "$hosthex:$porthex" 
         def headers = [:]
        headers.put("HOST", "$host:80")
        def path = "/"

        def hubAction = new physicalgraph.device.HubAction(
        method: method,
        path: path,
        headers: headers
        )
        log.debug hubAction
        hubAction
}


private String convertIPtoHex(ipAddress) {
String hex = ipAddress.tokenize( '.' ).collect { String.format( '%02x', it.toInteger() ) }.join()
log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
return hex
}
private String convertPortToHex(port) {
String hexport = port.toString().format( '%04x', port.toInteger() )
log.debug hexport
return hexport
}
private Integer convertHexToInt(hex) {
Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
log.debug("Convert hex to ip: $hex")
[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
private getHostAddress() {
def parts = device.deviceNetworkId.split(":")
log.debug device.deviceNetworkId
def ip = convertHexToIP(parts[0])
def port = convertHexToInt(parts[1])
return ip + ":" + port
}