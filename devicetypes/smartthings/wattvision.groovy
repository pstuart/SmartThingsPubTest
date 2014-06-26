/**
 *  Wattvision
 *
 *  Author: steve
 *  Date: 2014-02-13
 */
// for the UI
metadata {

	definition(name: "Wattvision", namespace: "smartthings", author: "Steve Vlaminck") {
		capability "Power Meter"
		capability "Refresh"
		attribute "powerContent", "string"
	}

	simulator {
		// define status and reply messages here
	}

	tiles {

		valueTile("power", "device.power") {
			state "power", label: '${currentValue} W'
		}

		tile(name: "powerChart", attribute: "powerContent", type: "HTML", content: '${currentValue}', width: 3, height: 2) {
			state "powerContent", label: ''
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "power"
		details(["powerChart", "power", "refresh"])

	}
}

def refresh() {
	setGraphUrl(parent.getGraphUrl(device.deviceNetworkId))
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

public setGraphUrl(graphUrl) {

	log.trace "setting url for Wattvision graph"

	sendEvent([
		date           : new Date(),
		value          : graphUrl,
		name           : "powerContent",
		displayed      : false,
		isStateChange  : true,
		description    : "Graph updated",
		descriptionText: "Graph updated"
	])
}

public addWattvisionData(json) {

	log.trace "Adding data from Wattvision"

	def data = json.data
	def units = json.units ?: "watts"

	if (data) {
		def latestData = data[-1]
		sendPowerEvent(latestData.t, latestData.v, units)
	}
}

private sendPowerEvent(time, value, units) {
	def wattvisionDateFormat = parent.wattvisionDateFormat()

	def eventData = [
		date           : new Date().parse(wattvisionDateFormat, time),
		value          : value,
		name           : "power",
		displayed      : true,
		isStateChange  : true,
		description    : "${value} ${units}",
		descriptionText: "${value} ${units}"
	]

	log.debug "sending event: ${eventData}"
	sendEvent(eventData)

}
