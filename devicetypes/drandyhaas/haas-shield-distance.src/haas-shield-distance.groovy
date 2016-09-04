metadata {
	definition (name: "Haas Shield Distance", namespace: "drandyhaas", author: "Andy Haas") {
		capability "Actuator"
		capability "Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"

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
        
        		valueTile("temperature","device.temperature") {
            	state "temperature",label:'${currentValue}°F',backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
			}
		valueTile(
        	"humidity","device.humidity") {
            	state "humidity",label:'RH ${currentValue}${unit}',unit:"%"
			}
		valueTile(
        	"illuminance","device.illuminance") {
            	state "luminosity",label:'${currentValue}${unit}', unit:"mm", backgroundColors:[
                	[value: 0, color: "#000000"],
                    [value: 1, color: "#060053"],
                    [value: 3, color: "#3E3900"],
                    [value: 12, color: "#8E8400"],
					[value: 24, color: "#C5C08B"],
					[value: 36, color: "#DAD7B6"],
					[value: 128, color: "#F3F2E9"],
                    [value: 1000, color: "#FFFFFF"]
				]
			}
            
        
		main "message"
		details(["greeting","getdist","message","temperature","humidity","illuminance"])
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
	log.debug "Get temperature and humidity, with hello..."
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
        return
    }

	def tunit = text.substring(text.length() - 1, text.length())
    log.debug("unit is $tunit")
    if (tunit=="m"){
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
    else if (tunit=="%"){
	    def tempF = text.substring(0, 5)
    	def humid = text.substring(7, 12)
    	log.debug "got temp and humidity: '$tempF' '$humid' "
        sendEvent(name: "humidity", value: humid.toFloat() )
        sendEvent(name: "temperature", value: tempF.toFloat() )
        sendEvent(name: "illuminance", value: state.level )
        return
    }
    
	def result = createEvent(name: "greeting", value: text)
    log.debug result?.descriptionText
    return result
}
