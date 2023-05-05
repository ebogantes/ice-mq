/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ice.test;

import com.ice.mq.MQGateway;
import com.ice.util.MqReceive;

import java.util.Properties;

/**
 *
 * @author Esteban Bogantes
 */
public class App {
    
    public static void main(String... args) {
        
        try {
            MQGateway mqGateway = null;

        String str = "host=10.129.20.134,port=1416,user=usatv,password=usatv,channel=C.SERVER.ATV,queuemanegername=SPEMQDESA,queuein=ESTEBAN.ATV,transacted=yes,poolSize=2,transactionTimeout=60,debug=true";
        Properties props = new Properties();

        String[] _properties = str.split(",");
        for (int e = 0; e < _properties.length; e++) {
            String[] values = _properties[e].split("=");
            props.put(values[0], values[1]);
        }
        
        mqGateway = new MQGateway(props);
        mqGateway.setSendClass(new MqReceive());
        mqGateway.setSendMethod("receiveMQMessage");
        mqGateway.startGateway();
        System.out.println("Inicio del jar");
        System.out.println(mqGateway.toString());
        System.out.println("Fin del jar");
        
        } catch (Exception e) {
            System.err.println(e);
        }
        

    }

    public MQGateway getMQOrders() {
        MQGateway mq = null;
        try {
            mq = new MQGateway();
        } catch (Exception e) {
        }
        return mq;
    }

}
