definition(
    name: "DryerHaas",
    namespace: "drandyhaas",
    author: "Andrew Haas",
    description: "Warn when the dryer fan turns on",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
	section("Choose dryer switches to montior on and off... ") {
		input "myswitchon",  "capability.switch"
		input "myswitchoff", "capability.switch"
	}
/*
	section("Warning level...") {
		input "warnlevel", "number", title: "Warning level at least?"
	}
*/
}

def checkforchanges(){
    log.debug("checking for changes")
}

def installed(){
    log.debug("installed")
    state.warned = 0
    subscribe(myswitchon,  "switch.on",  handler)
    subscribe(myswitchoff, "switch.off", handler)
}

def updated(){
    unsubscribe()
    installed()
}

def handler(evt){
    log.debug "dryer switch warning app: $evt.name, $evt.value, $settings"
    //log.debug("warnlevel is $warnlevel")
    def level = evt.value
    log.debug("current level is $level")
    //if (level.toInteger() > warnlevel){
    if (level=="on"){
       log.debug("switch on and warned = $state.warned")
       if (state.warned==0) sendPush("dryer switch is : $level")
       state.warned = 1 // record that we already warned about this
    }
    else{
       log.debug("switch off")
       //if (level.toInteger() < (warnlevel-50)){
	   if (state.warned==1) sendPush("dryer switch is now : $level")
       state.warned = 0 // record that we have not already warned about this now
    }
}