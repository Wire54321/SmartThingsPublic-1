/**
 *  Turn It On When It Opens
 *
 *  Author: SmartThings
 */
definition(
    name: "Turn On When Unocked",
    namespace: "drandyhaas",
    author: "Andy Haas",
    description: "Turn something on or off when a door is unlocked",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("When a door opens..."){
		input "locks", "capability.lock", title: "Which locks?", multiple:true
	}
    section("With this code index..."){
		input "lockindex", "number"
	}
	section("Turn on switches (and then turn off in 900s) ..."){
		input "switcheson", "capability.switch", multiple: true, required: false
	}
    section("Turn off switches..."){
		input "switchesoff", "capability.switch", multiple: true, required: false
	}
}

def installed()
{
	subscribe(lock1, "lock.unlock", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	installed()
}

def contactOpenHandler(evt) {
     log.debug "$evt.value: $evt, $settings"
     log.trace "Turning on switches: $switcheson"
     switcheson.on()
     log.trace "Turning off switches: $switchesoff"
     switchesoff.off()
     runIn(900, "turnEmOff")
     sendPush "Unlocked with code $lockindex so disarming!"
     sendLocationEvent(name: "alarmSystemStatus", value: "off")
}

def turnEmOff() {
     switcheson.off()
}