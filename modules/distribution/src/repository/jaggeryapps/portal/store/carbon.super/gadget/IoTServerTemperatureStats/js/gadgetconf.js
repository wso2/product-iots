var gadgetConfig = {
	"id": "IoTServerTemperatureStats",
	"title": "IoTServerTemperatureStats",
	"datasource": "ORG_WSO2_IOT_DEVICES_TEMPERATURE",
	"type": "batch",
	"columns": [
		{"name": "TIMESTAMP", "type": "time"},
		{"name": "owner", "type": "string"},
		{"name": "deviceType", "type": "string"},
		{"name": "deviceId", "type": "string"},
		{"name": "time", "type": "long"},
		{"name": "temperature", "type": "float"}
	],
	"chartConfig": {
		"x": "time",
		"maxLength": "10",
		"padding": {"top": 30, "left": 45, "bottom": 38, "right": 55},
		"charts": [{"type": "line", "y": "temperature"}]
	},
	"domain": "carbon.super",
	/* timeFrom and timeTo need not be mentioned here since they will be automatically fetched
																 if found in the QueryString of the URL	*/
	"params": ["owner", "deviceId"]
};
