/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.device.mgt.iot.agent.firealarm.virtual.ui;

import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentConstants;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentManager;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentUtilOperations;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.virtual.VirtualHardwareManager;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class AgentUI extends javax.swing.JFrame {

    private boolean isTemperatureRandomized, isHumidityRandomized;
    private boolean isTemperatureSmoothed, isHumiditySmoothed;

    private volatile boolean isAlarmOn = false;

    private final Object _lock = new Object();

    private JLabel picLabelBulbOn, picLabelBulbOff;

    private volatile java.util.List<String> policyLogs = new ArrayList<>();

    // Variables declaration - do not modify
    private javax.swing.JButton btnControl;
    private javax.swing.JButton btnView;
    private javax.swing.JCheckBox chkbxEmulate;
    private javax.swing.JCheckBox chkbxHumidityRandom;
    private javax.swing.JCheckBox chkbxHumiditySmooth;
    private javax.swing.JCheckBox chkbxTemperatureRandom;
    private javax.swing.JCheckBox chkbxTemperatureSmooth;
    private javax.swing.JComboBox cmbInterface;
    private javax.swing.JComboBox cmbPeriod;
    private javax.swing.JComboBox cmbProtocol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JLabel lblAgentName;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JPanel pnlBulbStatus;
    private javax.swing.JSpinner spinnerHumidity;
    private javax.swing.JSpinner spinnerInterval;
    private javax.swing.JSpinner spinnerTemperature;
	private javax.swing.JTextArea txtAreaLogs;
    private javax.swing.JTextField txtHumidityMax;
    private javax.swing.JTextField txtHumidityMin;
    private javax.swing.JTextField txtHumiditySVF;
    private javax.swing.JTextField txtTemperatureMax;
    private javax.swing.JTextField txtTemperatureMin;
    private javax.swing.JTextField txtTemperatureSVF;
    // End of variables declaration

    //Update UI from AgentManager changes
    private Runnable uiUpdater = new Runnable() {
        @Override
        public void run() {
            while (true) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pnlBulbStatus.removeAll();
                        pnlBulbStatus.add(isAlarmOn ? picLabelBulbOn : picLabelBulbOff);
                        pnlBulbStatus.updateUI();
                        lblStatus.setText(AgentManager.getInstance().getAgentStatus());
                        String policy = getPolicyLog();
                        if (policy != null){
                            txtAreaLogs.append("\n" + policy);
	                        txtAreaLogs.append("\n--------------------------------------------------\n");
                        }
                        if (isTemperatureRandomized) {
                            txtTemperatureMinActionPerformed(null);
                            txtTemperatureMaxActionPerformed(null);
                            if (isTemperatureSmoothed) {
                                txtTemperatureSVFActionPerformed(null);
                            }
                        }
                        if (isHumidityRandomized) {
                            txtHumidityMinActionPerformed(null);
                            txtHumidityMaxActionPerformed(null);
                            if (isHumiditySmoothed) {
                                txtHumiditySVFActionPerformed(null);
                            }
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };


    /**
     * Creates new form AgentUI
     */
    public AgentUI() {
	    initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        lblAgentName = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        pnlBulbStatus = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        chkbxTemperatureRandom = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtTemperatureMin = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtTemperatureMax = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtTemperatureSVF = new javax.swing.JTextField();
        spinnerTemperature = new javax.swing.JSpinner();
        chkbxTemperatureSmooth = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        btnView = new javax.swing.JButton();
        btnControl = new javax.swing.JButton();
        lblStatus = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        chkbxHumidityRandom = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        jPanel9 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        txtHumidityMin = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtHumidityMax = new javax.swing.JTextField();
        txtHumiditySVF = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        spinnerHumidity = new javax.swing.JSpinner();
        chkbxHumiditySmooth = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        spinnerInterval = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        cmbProtocol = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        cmbInterface = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaLogs = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        chkbxEmulate = new javax.swing.JCheckBox();
        cmbPeriod = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Fire Alarm Emulator");
        setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - 650 / 2, dim.height / 2 - 440 / 2);

        lblAgentName.setFont(new java.awt.Font("Cantarell", 1, 24)); // NOI18N
        lblAgentName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAgentName.setText("Device Name: " + AgentManager.getInstance().getDeviceName());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Copyright (c) 2015, WSO2 Inc.");

        jPanel1.setBackground(new java.awt.Color(220, 220, 220));

        jLabel3.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Alarm Status");

        pnlBulbStatus.setBackground(new java.awt.Color(220, 220, 220));

        javax.swing.GroupLayout pnlBulbStatusLayout = new javax.swing.GroupLayout(pnlBulbStatus);
        pnlBulbStatus.setLayout(pnlBulbStatusLayout);
        pnlBulbStatusLayout.setHorizontalGroup(
                pnlBulbStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlBulbStatusLayout.setVerticalGroup(
                pnlBulbStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 167, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                  .addComponent(pnlBulbStatus,
                                                                javax.swing.GroupLayout
                                                                        .DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE)
                                                  .addComponent(jLabel3,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                190, Short.MAX_VALUE))
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addComponent(jLabel3)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(pnlBulbStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(220, 220, 220));

        jLabel4.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Temperature");

        chkbxTemperatureRandom.setText("Randomize Data");
        chkbxTemperatureRandom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkbxTemperatureRandomActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jPanel7.setBackground(new java.awt.Color(220, 220, 220));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("Min");

        txtTemperatureMin.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTemperatureMin.setText("20");
        txtTemperatureMin.setEnabled(false);
        txtTemperatureMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTemperatureMinActionPerformed(evt);
            }
        });

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Max");

        txtTemperatureMax.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTemperatureMax.setText("50");
        txtTemperatureMax.setEnabled(false);
        txtTemperatureMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTemperatureMaxActionPerformed(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("SV %");

        txtTemperatureSVF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtTemperatureSVF.setText("50");
        txtTemperatureSVF.setEnabled(false);
        txtTemperatureSVF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTemperatureSVFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                          .addComponent(jLabel5)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(txtTemperatureMin, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jLabel6)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(txtTemperatureMax, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jLabel10)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(txtTemperatureSVF, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                           Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout
                                .createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)
                                .addGroup(jPanel7Layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                  .addComponent(txtTemperatureMin,
                                                                javax.swing.GroupLayout
                                                                        .PREFERRED_SIZE,
                                                                javax.swing.GroupLayout
                                                                        .DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                  .addComponent(txtTemperatureMax,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                  .addComponent(jLabel6)
                                                  .addComponent(jLabel5)
                                                  .addComponent(jLabel10)
                                                  .addComponent(txtTemperatureSVF,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35))
        );

        spinnerTemperature.setFont(new java.awt.Font("Cantarell", 1, 24)); // NOI18N
        spinnerTemperature.setModel(new javax.swing.SpinnerNumberModel(30, 0, 100, 1));
        spinnerTemperature.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerTemperatureStateChanged(evt);
            }
        });

        chkbxTemperatureSmooth.setText("Smooth Variation");
        chkbxTemperatureSmooth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkbxTemperatureSmoothActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                            .addComponent(spinnerTemperature))
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                                              .addComponent(chkbxTemperatureRandom)
                                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                              .addComponent(chkbxTemperatureSmooth)))
                                          .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jSeparator1)
                                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                                              .addGroup(jPanel2Layout.createParallelGroup(
                                                                                      javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(
                                                                                                        chkbxTemperatureRandom)
                                                                                                .addComponent(
                                                                                                        chkbxTemperatureSmooth))
                                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                              .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                              .addGap(0, 0, Short.MAX_VALUE))
                                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                                              .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                              .addComponent(spinnerTemperature)))
                                          .addContainerGap())
        );

        jPanel6.setBackground(new java.awt.Color(253, 254, 209));

        jLabel20.setText("Connection Status:");
        jLabel20.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        btnView.setText("View Device Data");
        btnView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnViewMouseClicked(evt);
            }
        });

        btnControl.setText("Control Device");
        btnControl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnControlMouseClicked(evt);
            }
        });

        lblStatus.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        lblStatus.setText("Not Connected");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addComponent(jLabel20)
                                          .addPreferredGap(javax.swing.LayoutStyle
                                                                   .ComponentPlacement.RELATED)
                                          .addComponent(lblStatus, javax.swing.GroupLayout
                                                  .DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(btnControl)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(btnView)
                                          .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)
                                .addGroup(jPanel6Layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING, false)
                                                  .addComponent(jLabel20,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE)
                                                  .addGroup(jPanel6Layout.createParallelGroup(
                                                          javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(btnView,
                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                  Short.MAX_VALUE)
                                                                    .addComponent(btnControl)
                                                                    .addComponent(lblStatus)))
                                .addContainerGap())
        );

        jPanel8.setBackground(new java.awt.Color(220, 220, 220));

        jLabel23.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("Humidity");

        chkbxHumidityRandom.setText("Randomize Data");
        chkbxHumidityRandom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkbxHumidityRandomActionPerformed(evt);
            }
        });

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jPanel9.setBackground(new java.awt.Color(220, 220, 220));

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Min");

        txtHumidityMin.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtHumidityMin.setText("20");
        txtHumidityMin.setEnabled(false);
        txtHumidityMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHumidityMinActionPerformed(evt);
            }
        });

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Max");

        txtHumidityMax.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtHumidityMax.setText("50");
        txtHumidityMax.setEnabled(false);
        txtHumidityMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHumidityMaxActionPerformed(evt);
            }
        });

        txtHumiditySVF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtHumiditySVF.setText("50");
        txtHumiditySVF.setEnabled(false);
        txtHumiditySVF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHumiditySVFActionPerformed(evt);
            }
        });

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("SV %");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                          .addComponent(jLabel24)
                                          .addPreferredGap(javax.swing.LayoutStyle
                                                                   .ComponentPlacement.RELATED)
                                          .addComponent(txtHumidityMin, javax.swing.GroupLayout
                                                  .PREFERRED_SIZE, 45, javax.swing.GroupLayout
                                                  .PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle
                                                                   .ComponentPlacement.RELATED)
                                          .addComponent(jLabel25)
                                          .addPreferredGap(javax.swing.LayoutStyle
                                                                   .ComponentPlacement.RELATED)
                                          .addComponent(txtHumidityMax, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jLabel11)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(txtHumiditySVF, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)
                                .addGroup(jPanel9Layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                  .addGroup(jPanel9Layout.createParallelGroup(
                                                          javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(jLabel11)
                                                                    .addComponent(txtHumiditySVF,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE))
                                                  .addGroup(jPanel9Layout.createParallelGroup(
                                                          javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(txtHumidityMin,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(txtHumidityMax,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(jLabel25)
                                                                    .addComponent(jLabel24)))
                                .addGap(35, 35, 35))
        );

        spinnerHumidity.setFont(new java.awt.Font("Cantarell", 1, 24)); // NOI18N
        spinnerHumidity.setModel(new javax.swing.SpinnerNumberModel(30, 0, 100, 1));
        spinnerHumidity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerHumidityStateChanged(evt);
            }
        });

        chkbxHumiditySmooth.setText("Smooth Variation");
        chkbxHumiditySmooth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkbxHumiditySmoothActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(spinnerHumidity))
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addGroup(jPanel8Layout.createSequentialGroup()
                                                                              .addComponent(chkbxHumidityRandom)
                                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                              .addComponent(chkbxHumiditySmooth)))
                                          .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jSeparator5)
                                                            .addGroup(jPanel8Layout.createSequentialGroup()
                                                                              .addGroup(jPanel8Layout.createParallelGroup(
                                                                                      javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(
                                                                                                        chkbxHumidityRandom)
                                                                                                .addComponent(
                                                                                                        chkbxHumiditySmooth))
                                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                              .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                              .addGap(0, 1, Short.MAX_VALUE))
                                                            .addGroup(jPanel8Layout.createSequentialGroup()
                                                                              .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                              .addPreferredGap
                                                                                      (javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                              .addComponent
                                                                                      (spinnerHumidity)))
                                          .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(207, 233, 234));

        jLabel7.setText("Data Push Interval:");

        spinnerInterval.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer
                .valueOf(1), null, Integer.valueOf(1)));
        spinnerInterval.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerIntervalStateChanged(evt);
            }
        });

        jLabel8.setText("Seconds");

        jLabel9.setText("Protocol:");

        cmbProtocol.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MQTT", "XMPP",
                "HTTP" }));
        cmbProtocol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbProtocolActionPerformed(evt);
            }
        });

        jLabel12.setText("Interface:");

        cmbInterface.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "eth0" }));
        cmbInterface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbInterfaceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addComponent(jLabel7)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(spinnerInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jLabel8)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addComponent(jLabel12)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(cmbInterface, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jLabel9)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(cmbProtocol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout
                                .createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                  .addGroup(jPanel3Layout.createParallelGroup(
                                                          javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(jLabel12)
                                                                    .addComponent(cmbInterface,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE))
                                                  .addGroup(jPanel3Layout.createParallelGroup(
                                                          javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(jLabel7)
                                                                    .addComponent(spinnerInterval,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(jLabel8)
                                                                    .addComponent(jLabel9)
                                                                    .addComponent(cmbProtocol,
                                                                                  javax.swing
                                                                                          .GroupLayout.PREFERRED_SIZE,
                                                                                  javax.swing
                                                                                          .GroupLayout.DEFAULT_SIZE,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        txtAreaLogs.setBackground(new java.awt.Color(1, 1, 1));
        txtAreaLogs.setColumns(20);
	    txtAreaLogs.setFont(new java.awt.Font("Courier 10 Pitch", Font.BOLD, 9)); // NOI18N
        txtAreaLogs.setForeground(new java.awt.Color(0, 255, 0));
        txtAreaLogs.setRows(5);
        jScrollPane1.setViewportView(txtAreaLogs);

        jPanel4.setBackground(new java.awt.Color(169, 253, 173));

        chkbxEmulate.setText("Emulate data");
        chkbxEmulate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkbxEmulateActionPerformed(evt);
            }
        });

        cmbPeriod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 hour", "1 day", "1 week", "1 month " }));
        cmbPeriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPeriodActionPerformed(evt);
            }
        });

        jLabel1.setText("Emulation Period");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addComponent(chkbxEmulate)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addComponent(jLabel1)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(cmbPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                  .addComponent(chkbxEmulate)
                                                  .addComponent(cmbPeriod,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                  .addComponent(jLabel1))
                                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                          .addContainerGap()
                                          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jScrollPane1)
                                                            .addComponent(lblAgentName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                            .addGroup(layout.createSequentialGroup()
                                                                              .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                .addComponent(
                                                                                                        jPanel8,
                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                        Short.MAX_VALUE)
                                                                                                .addComponent(
                                                                                                        jPanel2,
                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                        Short.MAX_VALUE)))
                                                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                          .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                          .addComponent(lblAgentName, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(layout.createSequentialGroup()
                                                                              .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                              .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                          .addContainerGap())
        );

        pack();

        chkbxTemperatureSmooth.setEnabled(false);
        chkbxTemperatureSmooth.setEnabled(false);

        cmbInterface.removeAllItems();
        for (String item : AgentManager.getInstance().getInterfaceList()){
            cmbInterface.addItem(item);
        }
        cmbInterface.setEnabled(false);

        cmbProtocol.removeAllItems();
        for (String item : AgentManager.getInstance().getProtocolList()){
            cmbProtocol.addItem(item);
        }
        cmbProtocol.setSelectedItem(AgentConstants.DEFAULT_PROTOCOL);

        URL urlAlarmOn = this.getClass().getResource("/alarm-on.gif");
        ImageIcon imageIconAlarmOn = new ImageIcon(urlAlarmOn);

        URL urlAlarmOff = this.getClass().getResource("/alarm-off.gif");
        ImageIcon imageIconAlarmOff = new ImageIcon(urlAlarmOff);

        picLabelBulbOn = new JLabel(imageIconAlarmOn);
        picLabelBulbOn.setSize(pnlBulbStatus.getSize());

        picLabelBulbOff = new JLabel(imageIconAlarmOff);
        picLabelBulbOff.setSize(pnlBulbStatus.getSize());

        addToPolicyLog(AgentUtilOperations.formatMessage(AgentManager.getInstance().getInitialPolicy()));
        new Thread(uiUpdater).start();

	    AgentManager.getInstance().setDeviceReady(true);
    }

    private void btnControlMouseClicked(java.awt.event.MouseEvent evt) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                URI uri = new URI(AgentManager.getInstance().getDeviceMgtControlUrl());
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void btnViewMouseClicked(java.awt.event.MouseEvent evt) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                URI uri = new URI(AgentManager.getInstance().getDeviceMgtAnalyticUrl());
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void chkbxTemperatureRandomActionPerformed(java.awt.event.ActionEvent evt) {
        isTemperatureRandomized = chkbxTemperatureRandom.isSelected();
        VirtualHardwareManager.getInstance().setIsTemperatureRandomized(isTemperatureRandomized);
        spinnerTemperature.setEnabled(!isTemperatureRandomized);
        txtTemperatureMax.setEnabled(isTemperatureRandomized);
        txtTemperatureMin.setEnabled(isTemperatureRandomized);
        chkbxTemperatureSmooth.setEnabled(isTemperatureRandomized);
        txtTemperatureSVF.setEnabled(isTemperatureRandomized && isTemperatureSmoothed);
    }

    private void chkbxHumidityRandomActionPerformed(java.awt.event.ActionEvent evt) {
        isHumidityRandomized = chkbxHumidityRandom.isSelected();
        VirtualHardwareManager.getInstance().setIsHumidityRandomized(isHumidityRandomized);
        spinnerHumidity.setEnabled(!isHumidityRandomized);
        txtHumidityMax.setEnabled(isHumidityRandomized);
        txtHumidityMin.setEnabled(isHumidityRandomized);
        chkbxHumiditySmooth.setEnabled(isHumidityRandomized);
        txtTemperatureSVF.setEnabled(isHumidityRandomized && isHumiditySmoothed);
    }

    private void spinnerTemperatureStateChanged(javax.swing.event.ChangeEvent evt) {
        if (!isTemperatureRandomized) {
            try {
                int temperature = Integer.parseInt(spinnerTemperature.getValue().toString());
                VirtualHardwareManager.getInstance().setTemperature(temperature);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid temperature value", "Error", JOptionPane.ERROR_MESSAGE);
                spinnerTemperature.setValue(VirtualHardwareManager.getInstance().getTemperature());
            }
        }
    }

    private void spinnerHumidityStateChanged(javax.swing.event.ChangeEvent evt) {
        if (!isHumidityRandomized) {
            try {
                int humidity = Integer.parseInt(spinnerHumidity.getValue().toString());
                VirtualHardwareManager.getInstance().setHumidity(humidity);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid humidity value", "Error", JOptionPane.ERROR_MESSAGE);
                spinnerHumidity.setValue(VirtualHardwareManager.getInstance().getHumidity());
            }
        }
    }

    private void txtTemperatureMinActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int temperature = Integer.parseInt(txtTemperatureMin.getText());
            VirtualHardwareManager.getInstance().setTemperatureMin(temperature);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid temperature value", "Error", JOptionPane.ERROR_MESSAGE);
            txtTemperatureMin.setText("20");
        }
    }

    private void txtTemperatureMaxActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int temperature = Integer.parseInt(txtTemperatureMax.getText());
            VirtualHardwareManager.getInstance().setTemperatureMax(temperature);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid temperature value", "Error", JOptionPane.ERROR_MESSAGE);
            txtTemperatureMax.setText("50");
        }
    }

    private void txtHumidityMinActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int humidity = Integer.parseInt(txtHumidityMin.getText());
            VirtualHardwareManager.getInstance().setHumidityMin(humidity);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid humidity value", "Error", JOptionPane.ERROR_MESSAGE);
            txtHumidityMin.setText("20");
        }
    }

    private void txtHumidityMaxActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int humidity = Integer.parseInt(txtHumidityMax.getText());
            VirtualHardwareManager.getInstance().setHumidityMax(humidity);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid humidity value", "Error", JOptionPane.ERROR_MESSAGE);
            txtHumidityMax.setText("50");
        }
    }

    private void spinnerIntervalStateChanged(javax.swing.event.ChangeEvent evt) {
        try {
            int interval = Integer.parseInt(spinnerInterval.getValue().toString());
            AgentManager.getInstance().setPushInterval(interval);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid time interval value", "Error", JOptionPane.ERROR_MESSAGE);
            spinnerInterval.setValue(5);
        }
    }

    private void cmbInterfaceActionPerformed(java.awt.event.ActionEvent evt) {
        AgentManager.getInstance().setInterface(cmbInterface.getSelectedIndex());
    }

    private void cmbProtocolActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmbProtocol.getSelectedIndex() != -1 && cmbProtocol.getItemAt(
                cmbProtocol.getSelectedIndex()).equals(AgentConstants.HTTP_PROTOCOL)) {
            cmbInterface.setEnabled(true);
        } else {
            cmbInterface.setEnabled(false);
        }

        AgentManager.getInstance().setProtocol(cmbProtocol.getSelectedIndex());

    }

    private void txtTemperatureSVFActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int temperatureSVF = Integer.parseInt(txtTemperatureSVF.getText());
            VirtualHardwareManager.getInstance().setTemperatureSVF(temperatureSVF);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid value", "Error", JOptionPane.ERROR_MESSAGE);
            txtTemperatureSVF.setText("50");
        }
    }

    private void txtHumiditySVFActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int humiditySVF = Integer.parseInt(txtHumiditySVF.getText());
            VirtualHardwareManager.getInstance().setHumiditySVF(humiditySVF);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid value", "Error", JOptionPane.ERROR_MESSAGE);
            txtHumiditySVF.setText("50");
        }
    }

    private void chkbxTemperatureSmoothActionPerformed(java.awt.event.ActionEvent evt) {
        isTemperatureSmoothed = chkbxTemperatureSmooth.isSelected();
        txtTemperatureSVF.setEnabled(isTemperatureSmoothed);
        VirtualHardwareManager.getInstance().setIsTemperatureSmoothed(isTemperatureSmoothed);
    }

    private void chkbxHumiditySmoothActionPerformed(java.awt.event.ActionEvent evt) {
        isHumiditySmoothed = chkbxHumiditySmooth.isSelected();
        txtHumiditySVF.setEnabled(isHumiditySmoothed);
        VirtualHardwareManager.getInstance().setIsHumiditySmoothed(isHumiditySmoothed);
    }

    private void cmbPeriodActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void chkbxEmulateActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    public void setAlarmStatus(boolean isAlarmOn) {
        this.isAlarmOn = isAlarmOn;
    }

    public void updateTemperature(int temperature) {
        spinnerTemperature.setValue(temperature);
        spinnerTemperature.updateUI();
    }

    public void updateHumidity(int humidity) {
        spinnerHumidity.setValue(humidity);
        spinnerHumidity.updateUI();
    }

    public void addToPolicyLog(String policy) {
        synchronized (this._lock) {
            policyLogs.add(policy);
        }
    }

    private String getPolicyLog() {
        synchronized (this._lock) {
            if (policyLogs.size() > 0) {
                String policy = policyLogs.get(0);
                policyLogs.remove(0);
                return policy;
            } else {
                return null;
            }
        }
    }

}
