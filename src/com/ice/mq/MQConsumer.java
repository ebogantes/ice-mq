package com.ice.mq;

import com.ice.mq.MQConsumeSession;
import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ice.configuration.MQConfiguration;

/**
 * Clase se encarga de gestionar los desencoladores
 *
 * @author: Roger Masis
 * @version: 1.0
 */
public class MQConsumer {

    private boolean isRunning = false;
    private MQConfiguration configuration;
    private MQGateway gateway;
    private MQConsumeSession[] jmsSession;
    private MQQueueManager connection = null;
    private Date lastRun = new Date();
    private boolean killThread;
    private ThreadGroup threadGroup = null;
    private boolean isRestarted = false;

    /**
     * Inicializa las variables
     *
     * @param gateway Gateway
     * @param configuration Archivo de configuracion de MQ
     * @throws Exception
     */
    public MQConsumer(MQGateway gateway, MQConfiguration configuration) throws Exception {
        setGateway(gateway);
        this.threadGroup = new ThreadGroup(this.getGateway().getGatewayID());
        this.configuration = configuration;
    }

    /**
     * Inicializa los desencoladores, se levanta hilo para escucha de
     * desencolares en caso de error reiniciarlos
     *
     * @throws Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes", "UseOfObsoleteCollectionType"})
    protected void start() throws Exception {
        Hashtable mqProps = new Hashtable();
        mqProps.put(MQConstants.CHANNEL_PROPERTY, this.getConfiguration().getChannel());
        mqProps.put(MQConstants.PORT_PROPERTY, this.getConfiguration().getPort());
        mqProps.put(MQConstants.HOST_NAME_PROPERTY, this.getConfiguration().getHost());
        mqProps.put(MQConstants.USER_ID_PROPERTY, this.getConfiguration().getUsername());
        mqProps.put(MQConstants.PASSWORD_PROPERTY, this.getConfiguration().getPassword());
        mqProps.put(MQConstants.CCSID_PROPERTY, this.getConfiguration().getPassword());
        mqProps.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES);
        String qManager = this.getConfiguration().getManager();
        this.connection = new MQQueueManager(qManager, mqProps);
        this.jmsSession = new MQConsumeSession[this.getConfiguration().getPoolSize()];
        for (int i = 0; i < this.jmsSession.length; ++i) {
            this.jmsSession[i] = new MQConsumeSession(this, i);
        }
        System.out.println(gateway.getMqConfig().getCodeMq()+ "  Desencoladores iniciados");
        this.isRunning = true;
        this.setKillThread(false);
        new Thread(this.threadGroup, new ThreadState(), "Rev_Desencoladores_" + UUID.randomUUID().toString()).start();

    }

    /**
     * Detiene los desencoladores
     *
     * @throws Exception
     */
    protected void stop() throws Exception {
        if (this.jmsSession != null) {
            for (int i = 0; i < this.jmsSession.length; ++i) {
                if (this.jmsSession[i] != null) {
                    this.jmsSession[i].close();
                }
            }
        }
        this.setKillThread(true);
        System.out.println(gateway.getMqConfig().getCodeMq()+ "  Desencoladores detenidos");
        this.isRunning = false;
    }

    /**
     * Indica si el desencolador esta activo
     *
     * @return true=activo, false=detenido
     */
    protected boolean isStarted() {
        return this.isRunning;
    }

    /**
     * *
     * Obtiene el archivo de configuracion MQ
     *
     * @return MQConfiguration
     */
    protected MQConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Obtiene la conexion MQ
     *
     * @return MQQueueManager
     */
    protected MQQueueManager getConnection() {
        return connection;
    }

    /**
     * Ingresa el Gateway
     *
     * @param gateway
     */
    private void setGateway(MQGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Obtiene el Gateway
     *
     * @return MQGateway
     */
    protected MQGateway getGateway() {
        return this.gateway;
    }

    /**
     * Obtiene si el desencolador esta siendo detenido
     *
     * @return true=esta siendo detenido, false=activo
     */
    public synchronized boolean getKillThread() {
        return this.killThread;
    }

    public synchronized void setKillThread(boolean killThread) {
        this.killThread = killThread;
    }

    /**
     * Obtiene última fecha/hora de ejecucion del desencolador
     *
     * @return Date
     */
    public Date getLastRun() {
        return this.lastRun;
    }

    /**
     * Indica la última fecha/hora de ejecucon del desencolador
     *
     * @param lastRun
     */
    public synchronized void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }

    /**
     * Obtiene el agrupador de hilos
     *
     * @return
     */
    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    /**
     * Obtiene si los desencoladores estan siendo reiniciados
     *
     * @return true=estan reiniciando, false=activo
     */
    public boolean getIsRestarted() {
        return this.isRestarted;
    }

    /**
     * Indica si los desencoladores deben reiniciarse
     *
     * @param isRestarted true=reiniciar, false=no reiniciar
     */
    public void setIsRestarted(boolean isRestarted) {
        this.isRestarted = isRestarted;
    }

    /**
     * Indica el grupo de hilos
     *
     * @param threadGroup
     */
    public void setThreadGroup(ThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
    }

    /**
     * Permite en caso de error de conexon reiniciar los desencoladores de forma
     * automatica
     */
    public void onException() {
        setIsRestarted(false);
        int nAttempts = 0;
        Integer maxAttempts = configuration.getMaxAttempts();
        Integer pauseTime = configuration.getPauseAttempts();
        while (!getIsRestarted()) {
            ++nAttempts;
            try {
                this.stop();
                setIsRestarted(false);
            } catch (Exception eStop) {
                System.out.println("Detencion desencoladores fallido: " + eStop.toString());
            }
            try {
                this.start();
                setIsRestarted(true);
                break;
            } catch (Exception eStart) {
                System.out.println("Inicio desencolades fallido: " + eStart);
            }
            if (maxAttempts != 0 && !getIsRestarted() && nAttempts == maxAttempts) {
                System.out.println("Reintentos de reinicio desencoladores superado.");
                break;
            }
            try {
                TimeUnit.MINUTES.sleep(pauseTime);
            } catch (Exception eSleep) {
            }
        }
    }

    /**
     * Permite la revision constante de los desencoladores
     *
     * @author R. Masis
     *
     */
    class ThreadState implements Runnable {

        private boolean active;

        public ThreadState() {
            this.active = true;
        }

        /**
         * Valida cada X minutos si los desencoladores estan activos, en caso
         * presentar fallo los reinicia
         */
        public void run() {
            int waitTimeThreadState = getConfiguration().getWaitTimeThreadState();
            long lastRunTime;
            while (this.active) {
                if (getKillThread()) {
                    break;
                }
                lastRunTime = (new Date().getTime() - getLastRun().getTime()) / 1000;
                if (lastRunTime != 0 && lastRunTime > (waitTimeThreadState * 60)) {
                    onException();
                    break;
                }
                try {
                    TimeUnit.MINUTES.sleep(waitTimeThreadState);
                } catch (InterruptedException e) {
                    System.out.println("ThreadState ignore interrupted sleep");
                }
            }
        }
    }

    /**
     * @return the jmsSession
     */
    public MQConsumeSession[] getMQSession() {
        return jmsSession;
    }
}
