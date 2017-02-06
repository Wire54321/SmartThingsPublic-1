metadata {
	definition (name: "Haas Shield Coffee", namespace: "drandyhaas", author: "Andy Haas") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        
        command "hello"
        command "mode"
        attribute "greeting","string"
	}

	// Simulator metadata
	simulator {
		status "on":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		status "off": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"

		// reply messages
		reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
		reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
	}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: 'on', action: "switch.off", icon: "st.vents.vent-open", backgroundColor: "#79b821"
			state "off", label: 'off', action: "switch.on", icon: "st.vents.vent", backgroundColor: "#ffffff"
		}
		standardTile("hello", "device.hello", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'hello', action: "hello", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		}        
		valueTile("greeting", "device.greeting", inactiveLabel: false) {
			state "greeting", label:'${currentValue}', unit:""
		}        
        standardTile("mode", "device.mode", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'mode', action: "mode", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		}        
		main "switch"
		details(["switch","mode","hello","greeting"])
	}
}

def on() {
	log.debug "on"
    zigbee.smartShield(text: "on").format()
}
def off() {
	log.debug "off"
    zigbee.smartShield(text: "off").format()
}

def mode() {
	log.debug "mode"
    zigbee.smartShield(text: "ph").format()
}

def hello() {
	log.debug "Hello World!"
	zigbee.smartShield(text: "orp").format()
}

// Parse incoming device messages to generate events
def parse(String description){
	def text = zigbee.parse(description)?.text
	log.debug "Parsing [$text]"
    if (text.length()<2){
    	log.debug "too short"
    	return
    }
	if (text == "ping"){
    	log.debug "got ping"
        return
    }
    
	def result = createEvent(name: "greeting", value: text)
    if (text == "on" || text == "off"){
    	result = createEvent(name: "switch", value: text, state: text)
    }
    log.debug result?.descriptionText
    return result
}