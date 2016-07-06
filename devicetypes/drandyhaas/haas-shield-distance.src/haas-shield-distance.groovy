metadata {
	definition (name: "Haas Shield Distance", namespace: "drandyhaas", author: "Andy Haas") {
		capability "Actuator"
		capability "Sensor"
		capability "Relative Humidity Measurement"
        
        command "hello"
        command "getdist"
        attribute "greeting","string"
	}

	// Simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("greeting", "device.greeting", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'hello', action: "hello", backgroundColor: "#ccccff"
		}  
        
		valueTile("message", "device.greeting", inactiveLabel: false) {
			state "greeting", label:'${currentValue}', unit:""
		}
        
        standardTile("getdist", "device.getdist", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'getdist', action: "getdist", backgroundColor: "#ccccff"
		}
        
		main "message"
		details(["greeting","getdist","message"])
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

def getdist() {
	log.debug "Getdist"
	zigbee.smartShield(text: "getdist").format()
}

def hello() {
	log.debug "Hello World!"
	zigbee.smartShield(text: "hello").format()
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
        sendEvent(name: "humidity", value: state.level )
        return
    }

	def mmunit = text.substring(text.length() - 2, text.length())
    log.debug("mmunit is $mmunit")
    if (mmunit=="mm"){
    	def level = text.substring(0, text.length() - 2)
    	log.debug("current level is $level")   
    
    	/*
    	//round it to nearest 5mm
    	def rlevel = Math.round(level.toInteger()/10)*10
        if (level.toInteger()<6 && level.toInteger()>0){
        	//don't round down to 0 (which is a sign of error reading)
            rlevel=5
        }
    	log.debug("rounded level is $rlevel")
        text="${rlevel}mm"
        //
        */
        
        if (level.toInteger()>1000){
        log.debug "level too high to make sense"
        return
        }
        
        //don't change unless it is significant
        if (!state.level) state.level = level.toInteger()
        if ( (level.toInteger() - state.level).abs() < 10 ){
        	text="${state.level}mm"
        }
        else state.level = level.toInteger()
        
    }
    
	def result = createEvent(name: "greeting", value: text)
    log.debug result?.descriptionText
    return result
}
