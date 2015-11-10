/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.agent.firealarm.virtual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentConstants;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentUtilOperations;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.sidhdhi.SidhdhiQuery;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.virtual.ui.AgentUI;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class use to emulate virtual hardware functionality
 */
public class VirtualHardwareManager {

    private static final Log log = LogFactory.getLog(VirtualHardwareManager.class);

    private static VirtualHardwareManager virtualHardwareManager;

    private AgentUI agentUI;
    private Sequencer sequencer = null;

    private int temperature = 30, humidity = 30;
    private int temperatureMin = 20, temperatureMax = 50, humidityMin = 20, humidityMax = 50;
    private int temperatureSVF = 50, humiditySVF = 50;
    private boolean isTemperatureRandomized, isHumidityRandomized;
    private boolean isTemperatureSmoothed, isHumiditySmoothed;

    private VirtualHardwareManager(){
    }

    public static VirtualHardwareManager getInstance(){
        if (virtualHardwareManager == null){
            virtualHardwareManager = new VirtualHardwareManager();
        }
        return virtualHardwareManager;
    }

    public void init(){
        try {
            // Set System L&F for Device UI
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            log.error(
                    "'UnsupportedLookAndFeelException' error occurred whilst initializing the" +
                    " Agent UI.");
        } catch (ClassNotFoundException e) {
            log.error(
                    "'ClassNotFoundException' error occurred whilst initializing the Agent UI.");
        } catch (InstantiationException e) {
            log.error(
                    "'InstantiationException' error occurred whilst initializing the Agent UI.");
        } catch (IllegalAccessException e) {
            log.error(
                    "'IllegalAccessException' error occurred whilst initializing the Agent UI.");
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                agentUI = new AgentUI();
                agentUI.setVisible(true);
            }
        });
        setAudioSequencer();
    }

    /**
     * Get temperature from emulated device
     * @return Temperature
     */
    public int getTemperature() {
        if (isTemperatureRandomized) {
            temperature = getRandom(temperatureMax, temperatureMin, temperature,
                                    isTemperatureSmoothed, temperatureSVF);
            agentUI.updateTemperature(temperature);
        }
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    /**
     * Get humidity from emulated device
     * @return Humidity
     */
    public int getHumidity() {
        if (isHumidityRandomized) {
            humidity = getRandom(humidityMax, humidityMin, humidity, isHumiditySmoothed,
                                 humiditySVF);
            agentUI.updateHumidity(humidity);
        }
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setTemperatureMin(int temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public void setTemperatureMax(int temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public void setHumidityMin(int humidityMin) {
        this.humidityMin = humidityMin;
    }

    public void setHumidityMax(int humidityMax) {
        this.humidityMax = humidityMax;
    }

    public void setIsHumidityRandomized(boolean isHumidityRandomized) {
        this.isHumidityRandomized = isHumidityRandomized;
    }

    public void setIsTemperatureRandomized(boolean isTemperatureRandomized) {
        this.isTemperatureRandomized = isTemperatureRandomized;
    }

    public void setTemperatureSVF(int temperatureSVF) {
        this.temperatureSVF = temperatureSVF;
    }

    public void setHumiditySVF(int humiditySVF) {
        this.humiditySVF = humiditySVF;
    }

    public void setIsTemperatureSmoothed(boolean isTemperatureSmoothed) {
        this.isTemperatureSmoothed = isTemperatureSmoothed;
    }

    public void setIsHumiditySmoothed(boolean isHumiditySmoothed) {
        this.isHumiditySmoothed = isHumiditySmoothed;
    }

    public void changeAlarmStatus(boolean isOn) {
        agentUI.setAlarmStatus(isOn);

        if (isOn) {
            sequencer.start();
        } else {
            sequencer.stop();
        }
    }

    public void addToPolicyLog(String policy) {
        agentUI.addToPolicyLog(policy);
    }


    private int getRandom(int max, int min, int current, boolean isSmoothed, int svf) {

        if (isSmoothed) {
            int offset = (max - min) * svf / 100;
            double mx = current + offset;
            max = (mx > max) ? max : (int) Math.round(mx);

            double mn = current - offset;
            min = (mn < min) ? min : (int) Math.round(mn);
        }

        double rnd = Math.random() * (max - min) + min;
        return (int) Math.round(rnd);

    }

    private void setAudioSequencer() {
        InputStream audioSrc = AgentUtilOperations.class.getResourceAsStream(
                "/" + AgentConstants.AUDIO_FILE_NAME);
        Sequence sequence;

        try {
            sequence = MidiSystem.getSequence(audioSrc);
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(sequence);
        } catch (InvalidMidiDataException e) {
            log.error("AudioReader: Error whilst setting MIDI Audio reader sequence");
        } catch (IOException e) {
            log.error("AudioReader: Error whilst getting audio sequence from stream");
        } catch (MidiUnavailableException e) {
            log.error("AudioReader: Error whilst openning MIDI Audio reader sequencer");
        }

        sequencer.setLoopCount(Clip.LOOP_CONTINUOUSLY);
    }

}
