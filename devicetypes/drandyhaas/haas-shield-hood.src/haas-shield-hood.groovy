metadata {
	definition (name: "Haas Shield Hood", namespace: "drandyhaas", author: "Andy Haas") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        
        command "hello"
        command "goodbye"
        command "up"
        command "down"
        command "bottom" 
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
			state "on", label: 'light on', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: 'light off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

		standardTile("greeting", "device.greeting", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'hello', action: "hello", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		}  
        
		valueTile("message", "device.greeting", inactiveLabel: false) {
			state "greeting", label:'${currentValue}', unit:""
		}
        
        standardTile("up", "device.up", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'up', action: "up", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		}  
        standardTile("down", "device.down", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'down', action: "down", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		} 
        standardTile("bottom", "device.bottom", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'bottom', action: "bottom", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		} 
        
		main "message"
		details(["switch","greeting","message","up","down","bottom"])
	}
}

Map parse(String description) {

	def value = zigbee.parse(description)?.text
	def linkText = getLinkText(device)
	def descriptionText = getDescriptionText(description, linkText, value)
	def handlerName = value
	def isStateChange = value != "ping"
	def displayed = value && isStateChange

	def result = [
		value: value,
		name: value in ["on","off"] ? "switch" : (value && value != "ping" ? "greeting" : null),
		handlerName: handlerName,
		linkText: linkText,
		descriptionText: descriptionText,
		isStateChange: isStateChange,
		displayed: displayed
	]

	log.debug result.descriptionText
	result
}

def on() {
    log.debug "On"
    zigbee.smartShield(text: "on").format()
}

def off() {
    log.debug "Off"
	zigbee.smartShield(text: "off").format()
}

def up() {
	log.debug "Up"
    zigbee.smartShield(text: "up").format()
}

def down() {
	log.debug "Down"
	zigbee.smartShield(text: "down").format()
}

def bottom() {
	log.debug "Bottom"
	zigbee.smartShield(text: "bottom").format()
}

def hello() {
	log.debug "Hello World!"
	zigbee.smartShield(text: "hello").format()
}

def goodbye() {
	log.debug "Bye Bye!"
	zigbee.smartShield(text: "goodbye").format()
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
//	def result = createEvent(name: "greeting", value: text as Double)
	def result = createEvent(name: "greeting", value: text)
    log.debug result?.descriptionText
    return result
}
