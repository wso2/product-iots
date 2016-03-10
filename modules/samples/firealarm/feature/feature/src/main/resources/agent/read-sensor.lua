DHT= require("dht_lib")

dht_data = 2
buzzer = 1
gpio.mode(buzzer, gpio.OUTPUT)
client_connected = false
m = mqtt.Client("ESP8266-"..node.chipid(), 120, "", "")

tmr.alarm(0,10000,1,function()
    DHT.read(dht_data)

    t = DHT.getTemperature()
    h = DHT.getHumidity()

    if t == nil then
        print("Error reading from DHTxx")
    else
        if (client_connected) then
            m:publish("wso2/iot/${DEVICE_OWNER}/firealarm/${DEVICE_ID}/publisher", "Temperature:"..t..":Humidity:"..h, 0, 0, function(client)
                print("Published> Temperature: "..t.."C  Humidity: "..h.."%")
            end)
        else
            connectMQTTClient()
        end
    end
end)

function connectMQTTClient()
    ip = wifi.sta.getip()
    if ip == nil then
        print("Waiting for network")
    else
        print("Client IP: "..ip)
        print("Trying to connect MQTT client")
        m:connect("${SERVER_IP}", 1883, 0, function(client)
            client_connected = true
            print("MQTT client connected")
            subscribeToMQTTQueue()
        end)
    end
end

function subscribeToMQTTQueue()
    m:subscribe("wso2/iot/${DEVICE_OWNER}/firealarm/${DEVICE_ID}/subscriber", 0, function(client, topic, message)
        print("Subscribed to MQTT Queue")
    end)
    m:on("message", function(client, topic, message)
        print("MQTT message received")
        print(message)
        buzz(message == "buzzer:ON")
    end)
    m:on("offline", function(client)
        print("Disconnected")
        client_connected = false
    end)
end

function buzz(status)
    buzzerOn=true
    if (status) then
        tmr.alarm(1,500,1,function()
            if buzzerOn then
                buzzerOn=false
                gpio.write(buzzer, gpio.HIGH)
            else
                buzzerOn=true
                gpio.write(buzzer, gpio.LOW)
            end
        end)
    else
        tmr.stop(1)
        gpio.write(buzzer, gpio.LOW)
    end
end