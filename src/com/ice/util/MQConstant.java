/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ice.util;

import com.ibm.mq.constants.MQConstants;

/**
 *
 * @author Esteban Bogantes
 */
public class MQConstant {
    
    
    // MQ
	public static final String MQ_CODE = "code";
	public static final String MQ_HOST = "host";
	public static final String MQ_PORT = "port";
	public static final String MQ_USER = "user";
	public static final String MQ_PASS = "password";
	public static final String MQ_QUEUE_IN = "queuein";
	public static final String MQ_QUEUE_OUT = "queueout";
	public static final String MQ_MANAGER = "queuemanegername";
	public static final String MQ_CHANNEL = "channel";
	public static final String MQ_CONSUMER = "consumer";
	public static final Integer MQ_DEFAULT_CONSUMER = 2;
	public static final String MQ_EXPIRATIONTIME = "expirationTimeMessage";
	public static final Integer MQ_DEFAULT_EXPIRATIONTIME = MQConstants.MQEI_UNLIMITED;
	public static final String MQ_WAITTIME = "waittime";
	public static final Integer MQ_DEFAULT_WAITTIME = 1;
	public static final String MQ_CREATEMANGER = "createmanager";
	public static final String MQ_MAXATTEMPTS = "maxattempts";
	public static final Integer MQ_DEFUALT_MAXATTEMPTS = 0;
	public static final String MQ_PAUSEATTEMPTS = "pauseattempts";
	public static final Integer MQ_DEFUALT_PAUSEATTEMPTS = 5;
	public static final String MQ_TIMESLEEP = "timeSleep";
	public static final Integer MQ_DEFAULT_TIMESLEEP = 200;
	public static final String MQ_PERSINTENT = "persistent";
	public static final String MQ_CHARSET = "charset";
	public static final String MQ_USE_CACHE = "useCache";
	public static final String MQ_CHARSET_DEFAULT = "UTF-8";
	public static final String MQ_CCSID = "ccsid";
	public static final Integer MQ_DEFAULT_CCSID = 1208;
	public static final String MQ_WAITINTERVAL = "waitinterval";
	public static final Integer MQ_DEFAULT_WAITINTERVAL = 5000;
	public static final Integer MQ_SUCCESS = 2033;
	public static final String MQ_MSG = "msg";
	public static final String MQ_ID = "id";
	public static final String MQ_IDSTRING = "idString";
    
}
