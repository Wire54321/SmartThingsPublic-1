
metadata {
	definition (name: "HaasTI ZigBee Switch", namespace: "drandyhaas", author: "Andy Haas", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Health Check"
        
        command "sendtext"

		// Generic
		fingerprint profileId: "C05E", deviceId: "0000", inClusters: "0006", deviceJoinName: "Generic On/Off Light", ocfDeviceType: "oic.d.light"
		fingerprint profileId: "0104", deviceId: "0103", inClusters: "0006", deviceJoinName: "Generic On/Off Switch"
		fingerprint profileId: "0104", deviceId: "010A", inClusters: "0006", deviceJoinName: "Generic On/Off Plug", ocfDeviceType: "oic.d.smartplug"

        fingerprint profileId: "0104", manufacturer: "HaasTI", model: "HaasTI Switch", deviceJoinName: "HaasTI Switch"
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
        standardTile("sendtexty", "device.sendtexty", inactiveLabel: false) {
			state "default", label:"sendtext", action:"sendtext"
		}
		main "switch"
		details(["switch", "refresh", "sendtexty"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	if (event) {
		sendEvent(event)
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
    log.info "refresh, write attr 6"
	//zigbee.onOffRefresh() + zigbee.onOffConfig()
    //zigbee.writeAttribute(0x000, 0x0010, 0x42, "0102030405060708090a0b0c0d0e0f10") // string_char location attribute
    String mystr = "0123456789abcdef"
    def packed = mystr.reverse().encodeAsHex() // must reverse since little-endian(?)
    log.info "packed is: "+packed
    def hexbytes = "0102030405060708090a0b0c0d0e0f" // must be 16 bytes
    hexbytes = packed
    "st wattr 0x${device.deviceNetworkId} 8 0x000 0x010 0x42 {"+hexbytes+"10}" // SAMPLELIGHT_ENDPOINT is defined as 8 in device code // the 10 on the end means 16 bytes length
}

def sendtext(){
    log.info "sendtext, read"
	//zigbee.smartShield(text: "Hello world!").format()
    zigbee.readAttribute(0x000, 0x0010)
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	log.debug "Configuring Reporting and Bindings."
	zigbee.onOffRefresh() + zigbee.onOffConfig()
}