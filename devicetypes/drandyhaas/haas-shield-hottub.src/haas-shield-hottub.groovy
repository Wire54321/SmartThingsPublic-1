metadata {
	definition (name: "Haas Shield Hottub", namespace: "drandyhaas", author: "Andy Haas") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Water Sensor"

        command "quickSetHeat"
        command "setknob"
        command "hello"
        command "getph"
        command "getorp"
        attribute "greeting","string"
        //attribute "watertemp","string"
        attribute "outertemp","number"
        attribute "innertemp","number"
        attribute "pH","string"
        attribute "ORP","string"
        attribute "heatingSetpoint","number"
        attribute "freeram","number"
        attribute "power","number"
        attribute "heatslider","number"
        attribute "knob","number"
        attribute "wetness", "number"
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
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: 'relay on', action: "off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: 'relay off', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        
		valueTile("message", "device.greeting", inactiveLabel: false) {
			state "greeting", label:'${currentValue}', unit:"", action: "hello"
		}
        
        valueTile("temperature", "device.temperature", inactiveLabel: false) {
			state "temperature", label:'water ${currentValue}', unit:"F",backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
        valueTile("outertemp", "device.outertemp", inactiveLabel: false) {
			state "outertemp", label:'outer ${currentValue}', unit:"F",backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
        valueTile("innertemp", "device.innertemp", inactiveLabel: false) {
			state "innertemp", label:'inner ${currentValue}', unit:"F",backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
        standardTile("phval", "device.pH", inactiveLabel: false) {
			state "default", label:'pH:${currentValue}', action: "getph"
		}
        standardTile("orpval", "device.ORP", inactiveLabel: false) {
			state "default", label:'ORP:${currentValue}', action: "getorp"
		}
        
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
		}
        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'pos: ${currentValue}', backgroundColor:"#ffffff"
		}
        valueTile("knob", "device.knob", inactiveLabel: false, decoration: "flat") {
			state "default", label:'knob: ${currentValue}', backgroundColor:"#ffffff"
		}
        valueTile("freeram", "device.freeram", inactiveLabel: false) {
			state "default", label:'RAM ${currentValue} B free'
		}
        valueTile("power", "device.power", inactiveLabel: false) {
			state "default", label:'${currentValue} W'
		}
        valueTile("wetness", "device.wetness", inactiveLabel: false) {
			state "default", label:'${currentValue} wet'
		}
        standardTile("water", "device.water", width: 1, height: 1) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		}
        
		main "temperature"
		details(["temperature","outertemp","innertemp","heatSliderControl","heatingSetpoint","knob","phval","orpval","switch","message","freeram","power","water","wetness"])
	}
}

Map parseparse(String description) {

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
    log.debug "switch on"
    zigbee.smartShield(text: "on").format()
}

def off() {
    log.debug "switch off"
	zigbee.smartShield(text: "off").format()
}

def hello() {
	log.debug "Hello World!"
	zigbee.smartShield(text: "hello").format()
}
def getph() {
	log.debug "Get pH"
	zigbee.smartShield(text: "ph").format()
}
def getorp() {
	log.debug "Get ORP"
	zigbee.smartShield(text: "orp").format()
}

def quickSetHeat(degrees) {
	log.debug "set heat slider to $degrees "
    sendEvent(name: "heatingSetpoint", value: degrees )
    setknob(degrees)
}
def setknob(degrees) {
	log.debug "set knob at $degrees "
    def pos = (int)(1.8*(100-degrees));//to go from 0-180, to actually turn knob
    //180 is coldest, 0 is hotest
	log.debug "set heat knob pos at $pos "
    //sendEvent(name: "knob", value: degrees )
    zigbee.smartShield(text: "servopos_${pos}").format()
}

// Parse incoming device messages to generate events
def parse(String description){
	def text = zigbee.parse(description)?.text
	log.debug "Parsing [$text]"
    if (text.length()<2){
    	log.debug "too short"
    	return
    }
    if (text == "ping"){//this seems to come in once per minute
        def hp = device.currentValue("knob") 
    	log.debug "got ping, knob is at ${hp}"
        
        //remind the device of its setpoint sometimes
        def mymod = (new Date()).time % 60 
        log.debug "random mymod is $mymod "
        if (mymod == 1 && hp>70) { // about once per hour and if the tub is supposed to be heating
        	log.debug "setting to 50!"
        	setknob(50) // put knob down a bit
            log.debug "setting back to hp!"
        	setknob(hp) // then turn it back up
        }
        return
    }
    
    if (text == "on") sendEvent(name:"switch", value:"on")
    if (text == "off") sendEvent(name:"switch", value:"off")  
    
    def result = createEvent(name: "greeting", value: text)

	def theunit = text.substring(text.length() - 1, text.length())
    log.debug("theunit is $theunit")
    if (theunit=="F"){
    	//it's a temperature reading, find out which one
		def id = text.substring(0, text.lastIndexOf("_"))
    	log.debug("current id is $id")
        def temp = text.substring(text.lastIndexOf("_")+1,text.length() - 1)
    	log.debug("current temp is $temp")
        if (temp!="185.0"){
        if (id=="248134") result = createEvent(name: "temperature", value: temp.toFloat())
        if (id=="21635")  result = createEvent(name: "outertemp", value: temp.toFloat())
        if (id=="208173") result = createEvent(name: "innertemp", value: temp.toFloat())
        }
    }
    else if (theunit=="H"){
    	//it's a pH reading
		def ph = (text.substring(0, text.length()-2)).toFloat()// -PH
        log.debug "got ph $ph"
		//def tempc=((device.currentValue("temperature").toFloat()-32.0)/1.8).round(2);
		def pht = (ph).round(2);
        //log.debug "got phtemp $pht for temp ${tempc}C"        
        sendEvent(name: "humidity", value: pht )
        result = createEvent(name: "pH", value: pht)
    }
    else if (theunit=="P"){
    	//it's an ORP reading
		def orp = (text.substring(0, text.length()-3)).toFloat().round()// -ORP
        log.debug "got orp $orp "
        sendEvent(name: "illuminance", value: orp )
        result = createEvent(name: "ORP", value: orp)
    }
    else if (theunit=="d" && text!="Startup cold"){
    	def pos = (text.substring(0, text.length()-1)).toFloat()// pos of the servo
        log.debug "got pos $pos"
        def degrees=(100.0-(pos/1.8)).round(1);//to go from pos 0-180, to degrees
        log.debug "so heatingSetpoint is $degrees "
        result = createEvent(name: "knob", value: degrees)
    }
    else if (theunit=="B"){
    	def val = (text.substring(0, text.length()-1)).toFloat()// freeram
        log.debug "got freeram $val"
        result = createEvent(name: "freeram", value: val.round() )
	}
    else if (theunit=="W"){
    	def val = (text.substring(0, text.length()-1)).toFloat().round()// power
        log.debug "got power $val"
        result = createEvent(name: "power", value: val*9 )
	}
    else if (theunit=="t"){
    	def val = (text.substring(0, text.length()-3)).toFloat().round()// wetness
        log.debug "got wetness $val"
        if (val>300){
            log.debug "now wet"
        	sendEvent( name: "water", value: "wet" )
		}
        else{
            log.debug "now dry"
        	sendEvent( name: "water", value: "dry" )
		}
        result = createEvent(name: "wetness", value: val )
	}
	
    log.debug result?.descriptionText
    return result
}
