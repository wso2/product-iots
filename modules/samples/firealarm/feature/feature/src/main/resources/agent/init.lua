tmr.alarm(0, 1000, 0, function()
    dofile("wifi-connect.lua");
    dofile("read-sensor.lua");
end)
