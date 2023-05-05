package com.ice.mq;

import com.ice.mq.MQPublishConnection;
import java.util.Date;
import java.util.Map;

import com.ibm.mq.MQQueueManager;
import com.ice.configuration.MQConfiguration;
import com.ice.util.MQConstant;


/**
 * Clase se encarga de gestionar los encoladores
 * 
 * @author: Roger Masis
 * @version: 1.0
 */

public class MQPublisher {
	
	MQPublishConnection publisher;
	
	private Date lastRun = null;
	
	/**
	 * Se encarga de encolar los mensajes
	 * @param data Mensaje a encolar
	 * @param connection Conexion MQ
	 * @param configuation Archivo de configuracion MQ
	 * @param startTime Fecha/hora de recibo de mensaje
	 * @throws Exception
	 */
	public void sendMessage(Map<String, Object> data, MQQueueManager connection, MQConfiguration configuation, Date startTime) throws Exception {
		Object queue = data.getOrDefault(MQConstant.MQ_QUEUE_OUT, configuation.getQueueOut());
		if(queue == null)
			throw new Exception("No se ha configurado encolador");
		this.publisher = getPublishConnection(data.get(MQConstant.MQ_IDSTRING).toString(), 
				(Boolean.parseBoolean(data.getOrDefault(MQConstant.MQ_CREATEMANGER, true).toString()) ? null : connection), configuation, 
				queue.toString(), data.getOrDefault(MQConstant.MQ_MANAGER, configuation.getManager()).toString(), startTime);
		
		this.publisher.sendMessage(data);
		this.publisher.stop();
		setLastRun(new Date());
	}

	/**
	 * Obtiene e inicializa la conexion al encolador
	 * @param id Identificador del mensaje a encolar
	 * @param connection Conexion MQ
	 * @param configuation Archivo de configuracion MQ
	 * @param startTime Fecha/hora de recibo de mensaje
	 * @return Retorna la conexion al encolador
	 * @throws Exception
	 */
	private MQPublishConnection getPublishConnection(String id, MQQueueManager connection, MQConfiguration configuation, String queue, String manager, Date startTime) throws Exception {
		MQPublishConnection publisher;
		if(connection == null)
			publisher = new MQPublishConnection(id, configuation, queue, manager, startTime);
		else{
			publisher = new MQPublishConnection(id, connection, queue, manager, startTime);
		}
		publisher.start();
		return publisher;
	}

	/**
	 * @return the publisher
	 */
	public MQPublishConnection getPublisher() {
		return publisher;
	}
	
	/**
	 * Obtiene última fecha/hora de ejecucion del encolador
	 * 
	 * @return Date
	 */
	public Date getLastRun() {
		return this.lastRun;
	}

	/**
	 * Indica la última fecha/hora de ejecucon del encolador
	 * 
	 * @param lastRun
	 */
	public synchronized void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
	}
}
