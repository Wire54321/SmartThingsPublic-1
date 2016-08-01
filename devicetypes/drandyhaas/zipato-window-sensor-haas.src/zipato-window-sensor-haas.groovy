/*
 * Philio PSM02 4-in-1 Multi Sensor Device Type
 * Based on My PSM01 Sensor created by SmartThings/Paul Spee
 * which is based on SmartThings' Aeon Multi Sensor Reference Device Type, Philio PHI_PSM02 Sensor, eyeonall, AJ
 */
 
 metadata {
	definition (name: "Zipato Window Sensor Haas", namespace: "drandyhaas", author: "Andy Haas") {
		capability "Contact Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
        capability "Refresh"
		fingerprint deviceId: "0x2001", inClusters: "0x80,0x85,0x70,0x72,0x86,0x30,0x31,0x84,0xEF,0x20"
	}

	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state "closed", label: 'Closed', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
			state "open", label: 'Open', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
		}
       
        valueTile("temperature", "device.temperature", inactiveLabel: false) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}

		valueTile("illuminance", "device.illuminance", inactiveLabel: false) {
			state "luminosity", label:'${currentValue} ${unit}', unit:"lux"
		}
        
        valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue} ${unit}humid', unit:"%"
		}
        
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        
        standardTile("tamper","device.tamper") {
			state "tamper",label:'tamper',icon:"st.motion.motion.active",backgroundColor:"#ff0000"
            state "clear",label:'clear',icon:"st.motion.motion.inactive",backgroundColor:"#00ff00"
		}
        
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh", icon:"st.secondary.refresh"
		}

		main(["contact", "temperature", "illuminance"])
		details(["contact", "temperature", "illuminance", "tamper", "battery", "humidity", "configure", "refresh"])
		}

		preferences {
			input description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		}
}

def installed() {
	log.debug "PSM02: Installed with settings: ${settings}"
	configure()
}

def updated() {
	log.debug "PSM02: Updated with settings: ${settings}"
    configure()
}

// Parse incoming device messages to generate events
def parse(String description)
{
    log.debug "PSM02 Parse called with ${description}"
	def result = []
	def cmd = zwave.parse(description, [0x20: 1, 0x30: 2, 0x31: 5, 0x70: 1, 0x72: 2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1])
	log.debug "PSM02 Parsed CMD: ${cmd.toString()}"
    if (cmd) { 
		if( cmd.CMD == "8407" ) { result << new physicalgraph.device.HubAction(zwave.wakeUpV2.wakeUpNoMoreInformation().format()) }
		def evt = zwaveEvent(cmd)
        result << createEvent(evt)
	}
    def statusTextmsg = "" //Door is ${device.currentState('contact').value}, temp is ${device.currentState('temperature').value}°, and illuminance is ${device.currentState('illuminance').value} LUX."
    sendEvent("name":"statusText", "value":statusTextmsg)
    //log.debug statusTextmsg
	log.debug "PSM02 Parse returned ${result}"
	return result
}

// Devices that support the Security command class can send messages in an
// encrypted form; they arrive wrapped in a SecurityMessageEncapsulation
// command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1])
        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	log.debug "PSM02: WakeUpNotification ${cmd.toString()}}"
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	log.debug "PSM02: SensorMultilevel ${cmd.toString()}"
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
            if (tempOffset) {
                log.debug "temp offset $tempOffset "
				def offset = tempOffset as int
				def v = map.value as int
				map.value = v + offset
			}
            log.debug "Adjusted temp value ${map.value}"
			break;
		case 3:
			// luminance
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			map.name = "illuminance"
			break;
 		case 5:
			// humidity
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "%"
			map.name = "humidity"
			break;
	}
	//map
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.debug "PSM02: BatteryReport ${cmd.toString()}}"
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = false
	//map
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
    log.debug "notification report: $cmd "
	def map = [:]
	if (cmd.notificationType == 6) {//magnet reed switch
    	map.name = "contact"
		if (cmd.event == 22) {
			log.debug "got 22/open for contact"
            map.value = "open"
        }
        else if (cmd.event == 23) {
			log.debug "got 23/closed for contact"
            map.value = "closed"
        }   
    }
    else if (cmd.notificationType == 7) {//tamper switch
		map.name = "tamper"
    	if (cmd.event == 3) {
			log.debug "got 3 for tamper"
            map.value = "tamper"
        }
        else if (cmd.event == 0) {
			log.debug "got 0 for tamper"
            map.value = "clear"
        }   
    }
    createEvent(map)        
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "PSM02: Catchall reached for cmd: ${cmd.toString()}}"
	[:]
}

def configure() {
    log.debug "configure() called"
    
	delayBetween([
		
        //1 tick = 30 minutes
        zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: 70).format(), // PIR Sensitivity 1-100
        zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: 3).format(), // PIR Redetect Interval. Applies only if in security mode
		zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: 1).format(), // Auto report Battery time 1-127, default 12
		zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: 2).format(), // Auto report Door/Window state time 1-127, default 12
		zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: 2).format(), // Auto report Illumination time 1-127, default 12
        zwave.configurationV1.configurationSet(parameterNumber: 13, size: 1, scaledConfigurationValue: 2).format(), // Auto report Temperature time 1-127, default 12
        zwave.wakeUpV1.wakeUpIntervalSet(seconds: 1 * 3600, nodeid:zwaveHubNodeId).format(),						// Wake up every hour

    ])
}

def refresh() {
        
        /*
        delayBetween([
                zwave.switchBinaryV1.switchBinaryGet().format(),
                zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1).format(),  // get temp in Fahrenheit
                zwave.batteryV1.batteryGet().format(),
                zwave.basicV1.basicGet().format()
        ], 1200)
        */
        
        log.debug "refresh called"
        state.sec = 1
        secure(zwave.batteryV1.batteryGet())
}

def secure(cmd) {
	if (state.sec) {
        log.debug "secure command"
    	return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        log.debug "command"
    	return cmd.format()
    }
}

