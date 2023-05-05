/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ice.configuration;

import com.ice.util.MQConstant;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Esteban Bogantes
 */
public class MQConfiguration {

	// ~ Instance fields
	// ================================================================================================
	private String codeMq;
	private String host;
	private String username;
	private String password;
	private String queueIn;
	private String queueOut;
	private String manager;
	private String channel;
	private Integer port;
	private Integer consumer;
	private Integer expirationTimeMessage;
	private Integer waittimethreadstate;
	private Integer maxAttempts;
	private Integer pauseAttempts;
	private Integer ccsid;
	private Integer timeSleep;
	private Boolean createManager;

	// ~ Constructors
	// ===================================================================================================
	public MQConfiguration() {
	}

	public MQConfiguration(Properties properties) {

		this.codeMq = properties.getProperty(MQConstant.MQ_CODE);
		this.host = properties.getProperty(MQConstant.MQ_HOST);
		try {
			this.port = Integer.valueOf(properties.getProperty(MQConstant.MQ_PORT));
		} catch (NumberFormatException e) {
			this.port = null;
		}
		this.username = properties.getProperty(MQConstant.MQ_USER);
		this.password = properties.getProperty(MQConstant.MQ_PASS);
		this.queueIn = properties.getProperty(MQConstant.MQ_QUEUE_IN);
		this.queueOut = properties.getProperty(MQConstant.MQ_QUEUE_OUT);
		this.manager = properties.getProperty(MQConstant.MQ_MANAGER);
		this.channel = properties.getProperty(MQConstant.MQ_CHANNEL);
		String prop = properties.getProperty(MQConstant.MQ_EXPIRATIONTIME);
		if (prop == null || !isNumeric(prop)) {
			this.expirationTimeMessage = MQConstant.MQ_DEFAULT_EXPIRATIONTIME;
		} else {
			this.expirationTimeMessage = Integer.parseInt(prop) * 10;
		}
		prop = properties.getProperty(MQConstant.MQ_WAITTIME);
		if (prop == null || !isNumeric(prop)) {
			this.waittimethreadstate = MQConstant.MQ_DEFAULT_WAITTIME;
		} else {
			this.waittimethreadstate = Integer.valueOf(prop);
		}
		prop = properties.getProperty(MQConstant.MQ_CREATEMANGER);
		if (prop == null) {
			this.createManager = false;
		} else {
			this.createManager = Boolean.valueOf(prop);
		}
		prop = properties.getProperty(MQConstant.MQ_CONSUMER);
		if (prop == null || !isNumeric(prop)) {
			this.consumer = MQConstant.MQ_DEFAULT_CONSUMER;
		} else {
			this.consumer = Integer.valueOf(prop);
		}
		prop = properties.getProperty(MQConstant.MQ_MAXATTEMPTS);
		if (prop == null || !isNumeric(prop)) {
			this.maxAttempts = MQConstant.MQ_DEFUALT_MAXATTEMPTS;
		} else {
			this.maxAttempts = Integer.valueOf(prop);
		}
		prop = properties.getProperty(MQConstant.MQ_PAUSEATTEMPTS);
		if (prop == null || !isNumeric(prop)) {
			this.pauseAttempts = MQConstant.MQ_DEFUALT_PAUSEATTEMPTS;
		} else {
			this.pauseAttempts = Integer.valueOf(prop);
		}
		prop = properties.getProperty(MQConstant.MQ_CCSID);
		if (prop == null || !isNumeric(prop)) {
			this.ccsid = MQConstant.MQ_DEFAULT_CCSID;
		} else {
			this.ccsid = Integer.valueOf(prop);
		}
		prop = properties.getProperty(MQConstant.MQ_TIMESLEEP);
		if (prop == null || !isNumeric(prop)) {
			this.timeSleep = MQConstant.MQ_DEFAULT_TIMESLEEP;
		} else {
			this.timeSleep = Integer.valueOf(prop);
		}
	}

	// ~ Methods
	// ========================================================================================================
	public static boolean isNumeric(String value) {
		if (value == null) {
			return false;
		}
		try {
			NumberFormat.getInstance().parse(value);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

	public List<String> validConfig() {
		List<String> errors = new LinkedList<String>();
		if (getHost() == null || getHost().length() == 0) {
			errors.add(codeMq + "->" + MQConstant.MQ_HOST);
		}
		if (getUsername() == null || getUsername().length() == 0) {
			errors.add(codeMq + "->" + MQConstant.MQ_USER);
		}
		if (getPassword() == null || getPassword().length() == 0) {
			errors.add(codeMq + "->" + MQConstant.MQ_PASS);
		}
		if ((getQueueIn() == null || getQueueIn().length() == 0)
				&& (getQueueOut() == null || getQueueOut().length() == 0)) {
			errors.add(codeMq + "->" + MQConstant.MQ_QUEUE_IN + " o " + MQConstant.MQ_QUEUE_OUT);
		}
		if (getManager() == null || getManager().length() == 0) {
			errors.add(codeMq + "->" + MQConstant.MQ_MANAGER);
		}
		if (getChannel() == null || getChannel().length() == 0) {
			errors.add(codeMq + "->" + MQConstant.MQ_CHANNEL);
		}
		if (getPort() == null) {
			errors.add(codeMq + "->" + MQConstant.MQ_PORT);
		}
		return errors;
	}

	public String getCodeMq() {
		return codeMq;
	}

	public void setCodeMq(String codeMq) {
		this.codeMq = codeMq;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getQueueIn() {
		return queueIn;
	}

	public void setQueueIn(String queueIn) {
		this.queueIn = queueIn;
	}

	public String getQueueOut() {
		return queueOut;
	}

	public void setQueueOut(String queueOut) {
		this.queueOut = queueOut;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getConsumer() {
		return consumer;
	}

	public void setConsumer(Integer consumer) {
		this.consumer = consumer;
	}

	public Integer getExpirationTimeMessage() {
		return expirationTimeMessage;
	}

	public void setExpirationTimeMessage(Integer expirationTimeMessage) {
		this.expirationTimeMessage = expirationTimeMessage;
	}

	public Integer getWaittimethreadstate() {
		return waittimethreadstate;
	}

	public void setWaittimethreadstate(Integer waittimethreadstate) {
		this.waittimethreadstate = waittimethreadstate;
	}

	public Integer getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(Integer maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public Integer getPauseAttempts() {
		return pauseAttempts;
	}

	public void setPauseAttempts(Integer pauseAttempts) {
		this.pauseAttempts = pauseAttempts;
	}

	public Integer getCcsid() {
		return ccsid;
	}

	public void setCcsid(Integer ccsid) {
		this.ccsid = ccsid;
	}

	public Integer getTimeSleep() {
		return timeSleep;
	}

	public void setTimeSleep(Integer timeSleep) {
		this.timeSleep = timeSleep;
	}

	public Boolean getCreateManager() {
		return createManager;
	}

	public void setCreateManager(Boolean createManager) {
		this.createManager = createManager;
	}

	public int getPoolSize() {
		return this.consumer;
	}

	public int getWaitTimeThreadState() {
		return this.expirationTimeMessage;
	}
	
	

}
