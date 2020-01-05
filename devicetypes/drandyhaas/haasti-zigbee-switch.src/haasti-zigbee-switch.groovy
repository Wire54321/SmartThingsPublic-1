
metadata {
	definition (name: "HaasTI ZigBee Switch", namespace: "drandyhaas", author: "Andy Haas", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Health Check"
        
        command "gettext"
        command "butt1"
        command "allon"
        command "on1"
        command "on2"
        command "on3"
        command "on4"
        command "butt2"
        command "alloff"
        command "off1"
        command "off2"
        command "off3"
        command "off4"
        command "getadc0"
        command "getadc1"
        command "getadc4"
        command "getadc5"

        command "sendtext1"

        attribute "text","string"

        fingerprint profileId: "0104", manufacturer: "TexasInstruments", model: "TI0001", deviceJoinName: "HaasTI Thing"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"off"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"on"
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("gettext", "device.gettext", inactiveLabel: false, width: 2, height: 2) {
			state "default", label:"gettext", action:"gettext"
		}
        standardTile("text", "device.text", inactiveLabel: false, width: 2, height: 2) {
			state "text", label:'${currentValue}'
		}
        
        standardTile("butt1", "device.butt1", inactiveLabel: false) {
			state "default", label:"button1", action:"butt1"
		}
        standardTile("allon", "device.allon", inactiveLabel: false) {
			state "default", label:"all on", action:"allon"
		}
        standardTile("on1", "device.on1", inactiveLabel: false) {
			state "default", label:"on1", action:"on1"
		}
        standardTile("on2", "device.on2", inactiveLabel: false) {
			state "default", label:"on2", action:"on2"
		}
        standardTile("on3", "device.on3", inactiveLabel: false) {
			state "default", label:"on3", action:"on3"
		}
        standardTile("on4", "device.on4", inactiveLabel: false) {
			state "default", label:"on4", action:"on4"
		}
        
        standardTile("butt2", "device.butt2", inactiveLabel: false) {
			state "default", label:"button2", action:"butt2"
		}
        standardTile("alloff", "device.alloff", inactiveLabel: false) {
			state "default", label:"all off", action:"alloff"
		}
        standardTile("off1", "device.off1", inactiveLabel: false) {
			state "default", label:"off1", action:"off1"
		}
        standardTile("off2", "device.off2", inactiveLabel: false) {
			state "default", label:"off2", action:"off2"
		}
        standardTile("off3", "device.off3", inactiveLabel: false) {
			state "default", label:"off3", action:"off3"
		}
        standardTile("off4", "device.off4", inactiveLabel: false) {
			state "default", label:"off4", action:"off4"
		}
        
        standardTile("getadc0", "device.getadc0", inactiveLabel: false) {
			state "default", label:"getadc0", action:"getadc0"
		}
        standardTile("getadc1", "device.getadc1", inactiveLabel: false) {
			state "default", label:"getadc1", action:"getadc1"
		}
        standardTile("getadc4", "device.getadc4", inactiveLabel: false) {
			state "default", label:"getadc4", action:"getadc4"
		}
        standardTile("getadc5", "device.getadc5", inactiveLabel: false) {
			state "default", label:"getadc5", action:"getadc5"
		}
        
        standardTile("sendtext1", "device.sendtext1", inactiveLabel: false) {
			state "default", label:"sendtext1", action:"sendtext1"
		}
        
		main "switch"
		details(["switch", "refresh", "gettext", "text", "butt1","allon","on1","on2","on3","on4", "butt2","alloff","off1","off2","off3","off4", "getadc0","getadc1","getadc4","getadc5", "sendtext1"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	//log.debug "description is $description"
    Map map = [:]
	def event = zigbee.getEvent(description)
	if (event) {
		sendEvent(event)
	}
    else if (description?.startsWith("catchall:")) {
    	log.debug "catchall is $description"
    }
    else if (description?.startsWith("read attr -")) {
		def descMap = zigbee.parseDescriptionAsMap(description)
		//log.debug "Desc Map: $descMap"
		if (descMap.clusterInt == 0) {
			def readstring = descMap.value
            byte[] asciireadstring = readstring.decodeHex()
            String text = new String(asciireadstring)
            log.debug "readstring is $readstring, ascii $asciireadstring, text $text"
            return createEvent(name: "text", value: "$text")
		}
        else {
			log.warn "Not an attribute we can decode"
		}
	} 
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
    log.info "refresh"
	zigbee.onOffRefresh() + zigbee.onOffConfig()
}

def gettext(){ // read some attribute string from the device
	log.info "gettext"
    //zigbee.readAttribute(0x000, 0x0006) // gets the last thing the device tried to send to us
    zigbee.readAttribute(0x000, 0x0010) // gets the last command the device heard us send
}

def sendtext1(){ // set the LocationDescription string on the device
    log.debug "sendtext1"
    //sendtodevice("ping") // to say hi
    sendtodevice("arduino1") // to tell the arduino, connected on serial, to do something, like send back a message on serial (it should just make sure it's <16 bytes, and ends with a ".")
}

def allon(){sendtodevice("on")}
def on1(){sendtodevice("on1")}
def on2(){sendtodevice("on2")}
def on3(){sendtodevice("on3")}
def on4(){sendtodevice("on4")}
def alloff(){sendtodevice("off")}
def off1(){sendtodevice("off1")}
def off2(){sendtodevice("off2")}
def off3(){sendtodevice("off3")}
def off4(){sendtodevice("off4")}
def butt1(){sendtodevice("getbutt1")}
def butt2(){sendtodevice("getbutt2")}
def getadc0(){sendtodevice("getadc0")}
def getadc1(){sendtodevice("getadc1")}
def getadc4(){sendtodevice("getadc4")}
def getadc5(){sendtodevice("getadc5")}

def sendtodevice(String mystr){
    mystr=mystr.padRight(16,".") // mystr should be 16 bytes!  
    def packed = mystr.reverse().encodeAsHex() // must reverse since little-endian(?)
    log.info "sending "+mystr+", packed is: "+packed
    "st wattr 0x${device.deviceNetworkId} 8 0x000 0x010 0x42 {"+packed+"10}" // SAMPLELIGHT_ENDPOINT is defined as 8 in device code // the 10 on the end means 16 bytes length
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	log.debug "Configuring Reporting and Bindings."
	zigbee.onOffRefresh() + zigbee.onOffConfig()
}
