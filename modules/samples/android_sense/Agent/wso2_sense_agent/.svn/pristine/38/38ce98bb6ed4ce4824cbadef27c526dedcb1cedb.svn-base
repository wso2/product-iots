package org.wso2.carbon.iot.android.sense.events.input.Sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.util.DataMap;
import org.wso2.carbon.iot.android.sense.events.input.DataReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


public class SensorDataReader extends DataReader implements SensorEventListener {
    private SensorManager mSensorManager;
    private List<Sensor> mSensors;
    private Map<String, SensorData> senseDataStruct = new HashMap<String, SensorData>();
    private Vector<SensorData> sensorVector = new Vector<SensorData>();
    Context ctx;



    public SensorDataReader(Context context) {
        ctx=context;
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mSensors= mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : mSensors)
        {
            mSensorManager.registerListener((SensorEventListener) this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void collectSensorData(){

        Log.d(this.getClass().getName(), "Sensor Type");
        for (Sensor sensor : mSensors)
        {
            try{
                if (senseDataStruct.containsKey(sensor.getName())){

                    SensorData sensorInfo=senseDataStruct.get(sensor.getName());
                    sensorVector.add(sensorInfo);
                    Log.d(this.getClass().getName(),"Sensor Name "+sensor.getName()+", Type "+ sensor.getType() +  " " +
                            ", sensorValue :" + sensorInfo.getSensorValues());
                }
            }catch(Throwable e){
                Log.d(this.getClass().getName(),"error on sensors");
            }

        }
        mSensorManager.unregisterListener(this);


    }

    public Vector<SensorData> getSensorData(){
        try {
            TimeUnit.MILLISECONDS.sleep(10000);
        } catch (InterruptedException e) {
            Log.e(SensorDataReader.class.getName(),e.getMessage());
        }
        collectSensorData();
        return sensorVector;




    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        senseDataStruct.put(event.sensor.getName(), new SensorData(event));
    }

    @Override
    public void run() {
        Log.d(this.getClass().getName(),"running -sensor");
        Vector<SensorData> sensorDatas=getSensorData();
        for( SensorData data : sensorDatas){
            DataMap.getSensorDataMap().add(data);
        }

    }


}
