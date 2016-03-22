metadata {
	definition (name: "Haas Shield Dryer", namespace: "drandyhaas", author: "Andy Haas") {
    	capability "Temperature Measurement"
		capability "Switch"
		capability "Sensor"
        command    "hello"
        attribute  "greeting","string"
        attribute  "weatherpressure", "string"
        attribute  "temp","number"
        attribute  "pressure","number"
        attribute  "ontime","number"
        attribute  "freeram", "number"
	}
	tiles {
        valueTile("temperature", "device.temperature", width: 1, height: 1) {
			state("temperature", label:'${currentValue}°F', backgroundColors:[ [value: 31, color: "#153591"],[value: 44, color: "#1e9cbb"],[value: 59, color: "#90d2a7"],[value: 74, color: "#44b621"],[value: 84, color: "#f1d801"],[value: 95, color: "#d04e00"],[value: 96, color: "#bc2323"] ] )
		}
        valueTile("pressure", "device.pressure", inactiveLabel: false) {
			state "pressure", label:'${currentValue} inHg'
		}
        valueTile("ontime", "device.ontime", inactiveLabel: false) {
			state "ontime", label:'${currentValue} s'
		}

        standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: 'on', action: "switch.off", icon: "st.vents.vent-open", backgroundColor: "#79b821"
			state "off", label: 'off', action: "switch.on", icon: "st.vents.vent", backgroundColor: "#ffffff"
		}
        valueTile("weatherpressure", "device.weatherpressure", decoration: "flat") {
			state "default", label:'weather: ${currentValue} inHg'
		}
		valueTile("greeting", "device.greeting", inactiveLabel: false, decoration: "flat") {
			state "default", label:'${currentValue}', action: "hello"
		}
        valueTile("freeram", "device.freeram", inactiveLabel: false) {
			state "default", label:'RAM ${currentValue} B free'
		}
        
		main "temperature"
		details(["temperature","pressure","ontime","switch","weatherpressure","greeting","freeram"])
	}
}

def hello(){
    log.debug "sending hello"
	zigbee.smartShield(text: "hello").format()
}

def on() {
    log.debug "sending on"
	zigbee.smartShield(text: "on").format()
}

def off() {
    log.debug "sending off"
	zigbee.smartShield(text: "off").format()
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
        //hello()
        return
    }

    def result = createEvent(name: "greeting", value: text)

  def values = text.split(":")
  def vs = values.size()
  //log.debug "values size is $vs "
  if (vs>1){
    log.debug "got values ${values[0]} and ${values[1]} "
    def val = values[1].toFloat()
    if (values[0]=="pressure"){
      //log.debug "pressure val is $val "
      result = createEvent(name: "pressure", value: (val+0.05).round(2) ) //calibration
      
      //get weather pressure
      def obs = getWeatherFeature("conditions", "10516")?.current_observation
      log.debug "Got weather pressure: ${obs.pressure_in} inHg"
      sendEvent(name: "weatherpressure", value: obs.pressure_in )
      //list of variables: http://www.wunderground.com/weather/api/d/docs?d=data/conditions&MR=1
    }
    else if (values[0]=="tempF"){
      //log.debug "temp val is $val "
      result = createEvent(name: "temperature", value: val )
    }
    else if (values[0]=="ontime"){
      //log.debug "ontime val is $val "
      result = createEvent(name: "ontime", value: val )
    }
    else if (values[0]=="freeram"){
      //log.debug "freeram val is $val "
      result = createEvent(name: "freeram", value: val.round() )
    }
  }//values.size()>1
  else if (text=="on" || text=="off"){
    result = createEvent(name: "switch", value: text)
  }
    
  log.debug result?.descriptionText
  return result
}
