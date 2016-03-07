var gadgetConfig = {
	"id": "TemperatureChart",
	"title": "TemperatureChart",
	"datasource": "org.wso2.iot.devices.temperature:1.0.0",
	"type": "realtime",
	"columns": [
		{"name": "TIMESTAMP", "type": "time"},
		{"name": "owner", "type": "string"},
		{"name": "deviceType", "type": "string"},
		{"name": "deviceId", "type": "string"},
		{"name": "time", "type": "long"},
		{"name": "temperature", "type": "float"}
	],
	"chartConfig": {
		"x": "TIMESTAMP",
		"maxLength": "10",
		"padding": {"top": 30, "left": 45, "bottom": 38, "right": 55},
		"charts": [{"type": "line", "y": "temperature"}]
	},
	"domain": "carbon.super",
	"params": ["owner", "deviceId"]
};