package com.ice.mq;

import java.util.Date;
import java.util.Map;



import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ice.configuration.MQConfiguration;
import com.ice.util.MQConstant;

/**
 * Clase de encolador
 * 
 * @author: Roger Masis
 * @version: 1.0
 */

public class MQPublishConnection {

	private MQQueueManager connection = null;
	private MQQueue producer = null;
	private MQConfiguration configuration;
	private String queue;
	private String manager;
	private Date startTime;
	private boolean closeConnection = true;
	private String id;

	/**
	 * Inicializa encolador reutilizando conexion MQ de desencolador
	 * 
	 * @param id         Identificador del mensaje a encolar
	 * @param connection Conexion MQ
	 * @param queue      Cola de respuesta
	 * @param startTime  Fecha/hora de recibido de mensaje
	 * @throws Exception
	 */
	MQPublishConnection(String id, MQQueueManager connection, String queue, String manager, Date startTime)
			throws Exception {
		this.queue = queue;
		this.manager = manager;
		this.connection = connection;
		this.startTime = startTime;
		this.closeConnection = false;
		this.id = id;
	}

	/**
	 * Inicializa encolador con nueva conexion MQ
	 * 
	 * @param id            Identificador de mensaje a encolar
	 * @param configuration Archivo de configuracion MQ
	 * @param startTime     Fecha/Hora de recibido de mensaje
	 * @throws Exception
	 */
	MQPublishConnection(String id, MQConfiguration configuration, String queue, String manager, Date startTime)
			throws Exception {
		this.configuration = configuration;
		this.queue = queue;
		this.manager = manager;
		this.startTime = startTime;
		this.id = id;
	}

	/**
	 * Se encarga de encolar el mensaje
	 * 
	 * @param data Mensaje a encolar
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected void sendMessage(Map data) throws Exception {
		if (this.producer == null) {
			start();
		}
		try {
			Date responseTime = new Date();
			Object textOrMap = data.get(MQConstant.MQ_MSG);
			byte[] messageId = (byte[]) data.get(MQConstant.MQ_ID);
			MQMessage message = new MQMessage();
			message.messageType = MQConstants.MQMT_REPLY;
			message.characterSet = 1208 ;
			message.write(((String) textOrMap).getBytes("UTF-8"));
			message.correlationId = messageId;
			message.messageId = messageId;
			Object expiry = data.get(MQConstant.MQ_EXPIRATIONTIME);
			message.expiry = (expiry != null && (Integer) expiry > 0) ? (Integer) data.get(MQConstant.MQ_EXPIRATIONTIME)
					: MQConstants.MQEI_UNLIMITED;
			setLogTime(this.id, "Configura mensaje", this.startTime, responseTime);
			responseTime = new Date();
			this.producer.put(message, new MQPutMessageOptions());
			setLogTime(data.get(MQConstant.MQ_IDSTRING).toString(), "Encola mensaje", this.startTime, responseTime);
			responseTime = new Date();
			this.connection.commit();
			setLogTime(data.get(MQConstant.MQ_IDSTRING).toString(), "Commit", this.startTime, responseTime);
		} catch (Exception e) {
			stop();
			throw e;
		}
	}

	/**
	 * Prmite generar log
	 * 
	 * @param id           Identificar de mensaje
	 * @param mensaje      Mensaje
	 * @param startTime    Fecha/hora de recibido de mensaje
	 * @param responseTime Fecha/hora de procesamiento de mensaje
	 */
	public void setLogTime(String id, String mensaje, Date startTime, Date responseTime) {
		System.out.println("(" + id + ") " + mensaje + ": " + (new Date().getTime() - responseTime.getTime()) + ":"
				+ (new Date().getTime() - startTime.getTime()));
	}

	/**
	 * Inicia la configuracion del encolador
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void start() throws Exception {
		Date responseTime = new Date();
		if (this.connection == null) {
			MQEnvironment.hostname = this.configuration.getHost();
			MQEnvironment.channel = this.configuration.getChannel();
			MQEnvironment.port = this.configuration.getPort();
			MQEnvironment.properties.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES);
			MQEnvironment.userID = this.configuration.getUsername();
			MQEnvironment.password = this.configuration.getPassword();
			MQEnvironment.CCSID = 1208;
			this.connection = new MQQueueManager(this.manager);
			setLogTime(this.id, "Crea Manager", this.startTime, responseTime);
		}
		int openOptions = MQConstants.MQOO_OUTPUT;
		responseTime = new Date();
		this.producer = this.connection.accessQueue(this.queue, openOptions, this.manager, null, null);
		setLogTime(this.id, "Crea queue", this.startTime, responseTime);
	}

	/**
	 * Detiene el encolador
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception {
		Date responseTime = new Date();
		if (this.producer != null) {
			this.producer.close();
			this.producer = null;
		}

		if (this.closeConnection && this.connection != null) {
			this.connection.disconnect();
			this.connection = null;
		}
		setLogTime(this.id, "Cierra publisher", this.startTime, responseTime);
	}
}
