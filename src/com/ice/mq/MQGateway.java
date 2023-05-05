/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ice.mq;

import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueueManager;
import com.ice.configuration.MQConfiguration;
import com.ice.util.MQConstant;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

/**
 *
 * @author Esteban Bogantes
 */
public class MQGateway {

	private MQConsumer consumer = null;
	private MQPublisher publisher = null;
	private final UUID uniqueKey = UUID.randomUUID();
	private MQConfiguration mqConfig = null;
	private Boolean startPublisher = false;
	private List<String> errors = null;
	private Object sendClass = null;
	private String sendMethod = null;

	public MQGateway() {
	}

	public MQGateway(Properties properties) throws Exception {
		this.mqConfig = new MQConfiguration(properties);
		errors = this.mqConfig.validConfig();
		if (errors.size() > 0) {
			System.out.println("Se requieren los siguietes variables de entorno: " + errors);
			return;
		}
		this.consumer = new MQConsumer(this, mqConfig);
		this.publisher = new MQPublisher();
	}

	/**
	 * Inicia los desencoladores
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("SizeReplaceableByIsEmpty")
	public void startGateway() throws Exception {
		if (errors != null && errors.size() > 0) {
			System.out.println("Se requieren los siguietes variables de entorno: " + errors);
			return;
		}
		this.startPublisher = true;
		System.out.println(" Iniciando encolador.");
		if (mqConfig.getQueueIn() == null || mqConfig.getQueueIn().trim().length() == 0) {
			return;
		}
		try {
			System.out.println(" Iniciando hilos de eschucha.");
			this.consumer.start();
		} catch (Exception e) {
			System.out.println("Failed to start gateway: " + e);
		}
	}

	/**
	 * Detiene los desencoladores
	 *
	 * @throws Exception
	 */
	public void stopGateway() throws Exception {
		this.startPublisher = false;
		this.consumer.stop();
		this.consumer.getThreadGroup().interrupt();
	}

	/**
	 * Obtiene el estado de los desencoladores
	 *
	 * @return true=corriendo, false=detenido
	 */
	public Boolean isRunning() {
		if (this.consumer != null && this.consumer.isStarted()) {
			return true;// Corriendo
		}
		return this.startPublisher;// Validar publisher
	}

	/**
	 * Obtiene el identificador del gestor
	 *
	 * @return
	 */
	protected String getGatewayID() {
		return uniqueKey.toString();
	}

	/**
	 * Guarda en log el mensaje indicado, con el tiempo tomado por el proceso y el
	 * tiempo total transcurrido
	 *
	 * @param id           Identificador del mensaje
	 * @param mensaje      Mensaje
	 * @param startTime    Tiempo de inicio del proceso
	 * @param responseTime Tiempo de inicio de desencolamiento
	 */
	public void setLogTime(String id, String mensaje, Date startTime, Date responseTime) {
		System.out.println("(" + id + ") " + mensaje + ": " + (new Date().getTime() - responseTime.getTime()) + ":"
				+ (new Date().getTime() - startTime.getTime()));
	}

	/**
	 * Permite pasar el arreglo de by a String
	 *
	 * @param a
	 * @return
	 */
	public String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	/**
	 * Procesa el mensaje recibido disparando las validaciones y encolando la
	 * respuesta
	 *
	 * @param message          Mensaje
	 * @param connection       Conexion MQ
	 * @param destinationQueue Cola de respuesta
	 * @param startTime        Tiempo de inicio de desencolamiento
	 */
	protected void handleMessage(MQMessage message, MQQueueManager connection, Date startTime) {
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			Date responseTime = new Date();
			String messageIdString = byteArrayToHex(message.messageId);
			setLogTime(messageIdString, "Inicia", startTime, responseTime);
			responseTime = new Date();
			data.put(MQConstant.MQ_IDSTRING, messageIdString);
			data.put(MQConstant.MQ_ID, message.messageId);
			data.put(MQConstant.MQ_EXPIRATIONTIME, mqConfig.getExpirationTimeMessage());
			data.put(MQConstant.MQ_CREATEMANGER, (mqConfig.getCreateManager() ? true : false));
			data.put(MQConstant.MQ_MANAGER,
					(message.replyToQueueManagerName.trim().length() > 0 ? message.replyToQueueManagerName
							: mqConfig.getManager()));
			data.put(MQConstant.MQ_QUEUE_IN,
					(message.replyToQueueName.trim().length() > 0 ? message.replyToQueueName : mqConfig.getQueueIn()));
			setLogTime(messageIdString, "Queue:" + message.replyToQueueName + ":" + message.replyToQueueManagerName,
					startTime, responseTime);
			String xmlAsString = message.readStringOfByteLength(message.getMessageLength());
			setLogTime(messageIdString, "Lee mensaje", startTime, responseTime);
			responseTime = new Date();

			Method method = sendClass.getClass().getMethod(this.sendMethod, String.class);
			method.invoke(sendClass, xmlAsString);

			setLogTime(messageIdString, "TOTAL", startTime, new Date());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error al encolar mensaje" + e);
		}
	}

	public void sendMessage(Map<String, Object> data) throws Exception {
		this.publisher.sendMessage(data, null, mqConfig, new Date());
	}

	public void sendMessage(String message) throws Exception {
		byte[] nbyte = new byte[24];
		Random randomno = new Random();
		randomno.nextBytes(nbyte);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(MQConstant.MQ_IDSTRING, new String(nbyte));
		data.put(MQConstant.MQ_ID, nbyte);
		data.put(MQConstant.MQ_MSG, message);
		data.put(MQConstant.MQ_EXPIRATIONTIME, this.getMqConfig().getExpirationTimeMessage());
		this.publisher.sendMessage(data, null, mqConfig, new Date());
	}

	/**
	 * @return the mqConfig
	 */
	public MQConfiguration getMqConfig() {
		return mqConfig;
	}

	/**
	 * @return the consumer
	 */
	public MQConsumer getConsumer() {
		return consumer;
	}

	/**
	 * @return the publisher
	 */
	public MQPublisher getPublisher() {
		return publisher;
	}

	/**
	 * @return the sendClass
	 */
	public Object getSendClass() {
		return sendClass;
	}

	/**
	 * @param sendClass the sendClass to set
	 */
	public void setSendClass(Object sendClass) {
		this.sendClass = sendClass;
	}

	/**
	 * @return the sendMethod
	 */
	public String getSendMethod() {
		return sendMethod;
	}

	/**
	 * @param sendMethod the sendMethod to set
	 */
	public void setSendMethod(String sendMethod) {
		this.sendMethod = sendMethod;
	}

	public Map<String, Object> getMQDetails() throws Exception {
		Map<String, Object> mqDetail = new HashMap<>();

		String codeMQ = this.getMqConfig().getCodeMq();
		mqDetail.put("name", codeMQ);

		Map<String, Object> config = new HashMap<>();
		if (this.getMqConfig().getQueueIn() != null && this.getMqConfig().getQueueIn().length() > 0) {
			Map<String, Object> configConsumer = new HashMap<>();
			configConsumer.put("name", this.getMqConfig().getQueueIn());
			configConsumer.put("lastRun", this.getConsumer().getLastRun());
			if (this.isRunning()) {
				List<Map<String, Object>> listThreads = new LinkedList<Map<String, Object>>();
				for (MQConsumeSession mqSession : this.getConsumer().getMQSession()) {
					Map<String, Object> configConsumerThread = new HashMap<>();
					configConsumerThread.put("name", mqSession.getName());
					configConsumerThread.put("status", mqSession.getStatus());
					listThreads.add(configConsumerThread);
				}
				configConsumer.put("threads", listThreads);
			}
			config.put("consumer", configConsumer);
		}
		if (this.getMqConfig().getQueueOut() != null && this.getMqConfig().getQueueOut().length() > 0) {
			Map<String, Object> configPublisher = new HashMap<>();
			configPublisher.put("name", this.getMqConfig().getQueueOut());
			configPublisher.put("lastRun", this.getPublisher().getLastRun());
			config.put("publisher", configPublisher);
		}
		config.put("status", this.isRunning());
		mqDetail.put("info", config);
		return mqDetail;
	}

}
