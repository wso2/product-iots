trig = 5 --IO14
echo = 1 --IO5
relay = 0 --IO16
is_relay_on = "false"
pulse_time = 0
water_level = -1
relay_on = 10
relay_off = 80
tank_height = 100
client_connected = false

m = mqtt.Client("ESP8266-" .. node.chipid(), 120, "${DEVICE_TOKEN}", "")

function save_config()
    file.open("config", "w+")
    file.writeline(relay_on .. "," .. relay_off .. "," .. tank_height)
    file.close()
    print("Configs saved")
end

function read_config()
    if (file.open("config") ~= nil) then
        local result = string.sub(file.readline(), 1, -2) -- to remove newline character
        file.close()
        local v1, v2, v3 = result:match("([^,]+),([^,]+),([^,]+)")
        relay_on = tonumber(v1)
        relay_off = tonumber(v2)
        tank_height = tonumber(v3)
        print("Loaded configs:" .. relay_on .. "," .. relay_off .. "," .. tank_height)
    else
        print("Using default configs")
    end
end

gpio.mode(relay, gpio.OUTPUT)
gpio.mode(trig, gpio.OUTPUT)
gpio.mode(echo, gpio.INT)
gpio.write(relay, gpio.HIGH)

read_config()

gpio.trig(echo, "both", function(level)
    du = tmr.now() - pulse_time
    if (level == 1) then
        pulse_time = tmr.now()
    else
        -- 1cm ==> 40
        local level = tank_height - (du / 40);
        if (level < relay_on) then
            gpio.write(relay, gpio.LOW)
            is_relay_on = "true"
        elseif (level > relay_off) then
            gpio.write(relay, gpio.HIGH)
            is_relay_on = "false"
        end
        water_level = level * 100 / tank_height
    end
end)

tmr.alarm(0, 5000, 1, function()
    collectgarbage()
    gpio.write(trig, gpio.HIGH)
    tmr.delay(10)
    gpio.write(trig, gpio.LOW)
    if client_connected then
        if (water_level > -1) then
            local payload = "{event:{metaData:{owner:\"${DEVICE_OWNER}\",deviceId:\"${DEVICE_ID}\"},payloadData:{relay:" .. is_relay_on .. ", waterlevel:" .. water_level .. "}}}"
            m:publish("carbon.super/watertank/${DEVICE_ID}/data", payload, 0, 0, function(client)
                print("Published> Water Level: " .. water_level .. "%  Relay: " .. is_relay_on)
            end)
        end
    else
        connectMQTTClient()
    end
end)

function connectMQTTClient()
    local ip = wifi.sta.getip()
    if ip == nil then
        print("Waiting for network")
    else
        print("Client IP: " .. ip)
        print("Trying to connect MQTT client")
        m:connect("${MQTT_EP}", ${MQTT_PORT}, 0, function(client)
            client_connected = true
            print("MQTT client connected")
            subscribeToMQTTQueue()
        end)
    end
end

function subscribeToMQTTQueue()
    m:subscribe("carbon.super/watertank/${DEVICE_ID}/command", 0, function(client, topic, message)
        print("Subscribed to MQTT Queue")
    end)
    m:on("message", function(client, topic, message)
        print("MQTT message received")
        print(message)
        local v1, v2, v3 = message:match("([^,]+),([^,]+),([^,]+)")
        relay_on = tonumber(v1)
        relay_off = tonumber(v2)
        tank_height = tonumber(v3)
        print("Received configs:" .. relay_on .. "," .. relay_off .. "," .. tank_height)
        save_config();
    end)
    m:on("offline", function(client)
        print("Disconnected")
        client_connected = false
    end)
end
