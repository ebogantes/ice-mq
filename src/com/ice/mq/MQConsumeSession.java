package com.ice.mq;

import java.util.Date;
import java.util.UUID;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ice.util.MQConstant;

/**
 * Clase desencoladores, se encarga de monitorear la cola
 *
 * @author: Roger Masis
 * @version: 1.0
 */
public class MQConsumeSession implements Runnable {

    private boolean active = true, waiting = false;
    private final int sleepTime = 10;
    private final int waitTime = sleepTime * 10;
    private MQQueueManager mqSession = null;
    private MQQueue msgConsumer = null;
    private MQConsumer mqConsumer = null;
    private MQGetMessageOptions gmo = null;
    private int index = 0;
    private String name;
    private Thread managerThread = null;

    public MQConsumeSession(MQConsumer mqConsumer, int index) throws Exception {
        this.mqConsumer = mqConsumer;
        this.index = index;
        this.mqSession = this.mqConsumer.getConnection();
        gmo = new MQGetMessageOptions();
        gmo.waitInterval = 500;
        gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_FAIL_IF_QUIESCING
                | MQConstants.MQGMO_ALL_MSGS_AVAILABLE | MQConstants.MQGMO_COMPLETE_MSG;
        int openOptions = MQConstants.MQOO_INPUT_SHARED | MQConstants.MQOO_FAIL_IF_QUIESCING;
        this.msgConsumer = this.mqSession.accessQueue(this.mqConsumer.getConfiguration().getQueueIn(), openOptions);

        name = this.mqConsumer.getGateway().getGatewayID() + this.index + "_" + UUID.randomUUID().toString();
        managerThread = new Thread(mqConsumer.getThreadGroup(), this, name);
        managerThread.start();

    }

    public Thread getManagerThread() {
        return managerThread;
    }

    public String getStatus() {
        if (this.managerThread == null) {
            return "No iniciado";
        }
        if (this.managerThread.isAlive()) {
            if (waiting) {
                return "Procesando registro";
            }
            if (active) {
                return "Activo";
            }
        }
        return "detenido";
    }

    /**
     * Obtiene el gestor de desencoladores
     *
     * @return MQConsumer
     */
    public MQConsumer getConsumer() {
        return this.mqConsumer;
    }

    /**
     * Constantemente esta revisando la cola de entrada, en caso de encontar
     * mensaje notifica para su procesamiento
     */
    public void run() {
        MQMessage rcvMessage;
        this.mqConsumer.setLastRun(new Date());
        while (this.active) {
            try {
                rcvMessage = new MQMessage();
                this.msgConsumer.get(rcvMessage, this.gmo);
                this.mqConsumer.setLastRun(new Date());
                this.waiting = true;
                this.mqConsumer.getGateway().handleMessage(rcvMessage, this.mqConsumer.getConnection(), new Date());
                this.commit();
            } catch (MQException e) {
                if (e.reasonCode == MQConstant.MQ_SUCCESS) {
                    this.mqConsumer.setLastRun(new Date());
                }
            } catch (Exception e) {
                System.out.println("Error al leer mensaje" + e);
            } finally {
                try {
                    Thread.sleep(this.mqConsumer.getConfiguration().getTimeSleep());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * Cierra las conexiones relacionadas a la cola
     */
    protected void close() {
        this.active = false;
        try {
            Thread.sleep(waitTime + sleepTime + 1);
        } catch (InterruptedException ie) {
            System.out.println(ie);
        }
        try {
            this.msgConsumer.close();
        } catch (MQException e) {
            System.out.println(e);
        }
        this.msgConsumer = null;
        try {
            this.mqSession.disconnect();
        } catch (MQException e) {
            System.out.println(e);
        }
        this.mqSession = null;

    }

    /**
     * Genera commit de la cola
     */
    protected void commit() {
        try {
            this.mqSession.commit();
        } catch (MQException e) {
            System.out.println(e);
        }
        this.waiting = false;
    }

    /**
     * Genera rollback de la cola
     */
    protected void rollback() {
        try {
            this.mqSession.backout();
        } catch (MQException e) {
            System.out.println(e);
        }
        this.waiting = false;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}
