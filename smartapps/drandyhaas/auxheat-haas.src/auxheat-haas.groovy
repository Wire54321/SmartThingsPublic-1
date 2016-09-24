
definition(
    name: "AuxHeat Haas",
    namespace: "drandyhaas",
    author: "Andrew Haas",
    description: "Changes your thermostat mode automatically in response to an outdoor temperature change.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat"
	}
	section("Heat pump temp...") {
		input "heatpumpSetpoint", "number", title: "Turn on at degrees?"
	}
    section("Choose other thermostats to update too... ") {
		input "otherthermostats", "capability.thermostat", multiple: true
	}
    
    section("Choose hottub temp... ") {
		input "hottub", "capability.temperatureMeasurement", multiple: false
	}
    section("Choose the weather to update... ") {
    	input name: "weatherDevices", type: "device.smartweatherStationTile", title: "Select Weather Device(s)", description: "Select the Weather Tiles to update", required: true, multiple: true
    }
}

def checkforchanges(){
    log.debug("checking for changes")
    
    // http://www.wunderground.com/weather/api/d/docs?d=data/conditions
    def data = getWeatherFeature( "conditions" )//, zipcode )
    def temper = data.current_observation.temp_f
    log.debug("temper is $temper")
    
    log.debug("heatpumpSetpoint is $heatpumpSetpoint")
    
    def mode = thermostat.currentThermostatMode
    log.debug("current mode is $mode")
    
    if (temper > heatpumpSetpoint){
       log.debug("above heatpumpSetpoint")
       if (mode == "emergencyHeat"){
          log.debug("mode == emergencyHeat, setting to heat")
          thermostat.heat() //setThermostatMode("heat")
       }
    }
    else{
       log.debug("not above heatpumpSetpoint")
       if (mode == "heat"){
          log.debug("mode == heat, setting to emergencyHeat")
          thermostat.emergencyHeat() //setThermostatMode("emergencyHeat")
       }
    }
    
}

def installed()
{
	subscribe(thermostat, "temperature", handler)
    subscribe(thermostat, "battery", handler)
    subscribe(thermostat, "humidity", handler)
    subscribe(thermostat, "heatingSetpoint", handler)
    subscribe(thermostat, "coolingSetpoint", handler)
    subscribe(hottub, "temperature", handler)
	subscribe(app, appTouch)
}

def updated()
{
	unsubscribe()
	installed()
}

def handler(evt)
{
	def now = new Date().time //milliseconds
    log.debug "${now} : ${state.lastRun}"
    if (!state.lastRun) state.lastRun = now
    def timedif = (now - state.lastRun)/1000 //seconds
    log.debug "timedif: $timedif s and handler: ${evt.name}, $settings"
	if (timedif>10*60){//10 minutes
    	state.lastRun = now
    	appTouch(evt)
    }
}

def appTouch(evt)
{
	log.debug "appTouch: ${evt.displayName}, $settings"
    
    def now = new Date().time //milliseconds

	//thermostat.setHeatingSetpoint(heatingSetpoint)
	//thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
    
    //update the heater settings
    checkforchanges()
    
    //update other thermostats (they are in battery saving mode)
    def myit=0
    state.pollnum=0
    for (th in otherthermostats){
       def myits = 5+ myit*5;
       log.debug "schedule polling ${myit} in ${myits}s"
       runIn(myits, pollit, [overwrite: false]) // will run th.poll()
       myit=myit+1
    }
    
    //update the weather
    weatherDevices.refresh()
    
    //update hottub stuff
    runIn(2, ph)
    runIn(7, orp)
    
    state.lastRun = now //milliseconds
}

def pollit(){
  log.debug "pollit ${state.pollnum}"
  otherthermostats[state.pollnum].poll()
  state.pollnum = state.pollnum + 1
}

def ph(){
	log.debug "update ph"
    hottub.getph()
    }
def orp(){
    log.debug "update orp"
    hottub.getorp()
}

// catchall
def event(evt)
{
	log.debug "value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
}
