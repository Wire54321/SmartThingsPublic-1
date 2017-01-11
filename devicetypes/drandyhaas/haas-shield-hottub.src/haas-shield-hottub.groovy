metadata {
	definition (name: "Haas Shield Hottub", namespace: "drandyhaas", author: "Andy Haas") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
		capability "Temperature Measurement"

        command "quickSetHeat"
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

		standardTile("greeting", "device.greeting", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'hello', action: "hello", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		}  
        
		valueTile("message", "device.greeting", inactiveLabel: false) {
			state "greeting", label:'${currentValue}', unit:""
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
        valueTile("phval", "device.pH", inactiveLabel: false) {
			state "default", label:'pH:${currentValue}'
		}
        valueTile("orpval", "device.ORP", inactiveLabel: false) {
			state "default", label:'ORP:${currentValue}'
		}
        
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setHeatingSetpoint", action:"quickSetHeat", backgroundColor:"#d04e00"
		}
        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'pos: ${currentValue}', backgroundColor:"#ffffff"
		}
        
        standardTile("ph", "device.greeting", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'get ph', action: "getph", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		}  
        standardTile("orp", "device.greeting", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "default", label: 'get orp', action: "getorp", icon: "st.switches.switch.off", backgroundColor: "#ccccff"
		}
        
        valueTile("setpoint", "device.setpoint", inactiveLabel: false) {
			state "default", label:'setpoint: ${currentValue}', unit:""
		}
        
		main "temperature"
		details(["temperature","outertemp","innertemp","heatSliderControl","heatingSetpoint","ph","phval","setpoint","switch","greeting","message","orp","orpval"])
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
    sendEvent(name: "heatingSetpoint", value: degrees)
	degrees=(int)(1.8*(100-degrees));//to go from 0-180, to actually turn knob
    //180 is coldest, 0 is hotest
	log.debug "set heat knob pos at $degrees "
    zigbee.smartShield(text: "servopos_${degrees}").format()
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
        def hp = device.currentValue("heatingSetpoint") 
    	log.debug "got ping, heatingSetpoint is ${hp}"
        
        sendEvent(name:"setpoint", value:"$hp") // really this should be sent back by the arduino
        
        //remind the device of its setpoint sometimes
        def ten = (new Date()).time % 10 
        log.debug "random mod is $ten "
        if (ten == 1) quickSetHeat(hp)
        return
    }
    
    if (text == "on") sendEvent(name:"switch", value:"on")
    if (text == "off") sendEvent(name:"switch", value:"off")  
    
    def result = createEvent(name: "greeting", value: text)

	def theunit = text.substring(text.length() - 1, text.length())
    log.debug("theunit is $Funit")
    if (theunit=="F"){
    	//it's a temperature reading, find out which one
		def id = text.substring(0, text.lastIndexOf("_"))
    	log.debug("current id is $id")
        def temp = text.substring(text.lastIndexOf("_")+1,text.length() - 1)
    	log.debug("current temp is $temp")
        if (temp!="185.0"){
        if (id=="21635") {
        //temp="92.4"
        result = createEvent(name: "temperature", value: temp.toFloat())
        }
        if (id=="248134") result = createEvent(name: "outertemp", value: temp.toFloat())
        if (id=="208173") result = createEvent(name: "innertemp", value: temp.toFloat())
        }
    }
    else if (theunit=="H"){
    	//it's a pH reading
		def ph = (text.substring(0, text.length()-2)).toFloat()// -PH
        log.debug "got ph $ph"
        def phc = (0.0178 * (ph) - 1.889).round(2);
       	log.debug "got phconst $phc"
		def tempc=((device.currentValue("temperature").toFloat()-32.0)/1.8).round(2);
		def pht = (7.0 - (2.5 - ph/204.8) / (0.257179 + 0.000941468 * tempc)).round(2);
        log.debug "got phtemp $pht for temp ${tempc}C"
        def calib = 0.05 // -0.96
        def phtc = (calib + pht).round(2)
        log.debug "got phtempcalib $phtc for calib $calib"
        result = createEvent(name: "pH", value: phtc)
    }
    else if (theunit=="P"){
    	//it's an ORP reading
		def orp = (text.substring(0, text.length()-3)).toFloat()// -ORP
        log.debug "got orp  $orp "
        def orpc = ((2.5 - orp / 204.8) / 1.037).round(2);// in V
       	log.debug "got orpc $orpc "
        result = createEvent(name: "ORP", value: orpc)
    }
	
    log.debug result?.descriptionText
    return result
}
