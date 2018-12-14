/**
 *  SmartPowerOutletHaas
 *
 *  Copyright 2014 Andrew Haas
 *
 */
metadata {
	definition (name: "SmartPowerOutletHaas", namespace: "drandyhaas", author: "Andrew Haas") {
    
		capability "Actuator"
		capability "Switch"
		capability "Sensor"

		fingerprint profileId: "0104", inClusters: "0000,0003,0006", outClusters: "0019"
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

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}

		main "switch"
		details "switch"
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "Got ${description}"
	
		def name = "switch"
		def value = description?.endsWith("0100") ? "on" : "off"
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	
}

// Commands to device
def on() {
	'zcl on-off on'
}

def off() {
	'zcl on-off off'
}