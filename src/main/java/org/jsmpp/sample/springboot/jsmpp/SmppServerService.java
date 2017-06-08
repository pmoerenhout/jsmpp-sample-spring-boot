package org.jsmpp.sample.springboot.jsmpp;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.InterfaceVersion;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.ServerResponseDeliveryListener;
import org.jsmpp.session.SessionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmppServerService {

  private static final Logger LOG = LoggerFactory.getLogger(SmppServerService.class);

  private SMPPServerSessionListener sessionListener = null;

  private SmppConfiguration configuration;
  private ServerMessageReceiverListener serverMessageReceiverListener;
  private ServerResponseDeliveryListener serverResponseDeliveryListener;
  private SessionStateListener sessionStateListener;
  private TaskExecutor taskExecutor;

  private boolean running = true;

  @Autowired
  public SmppServerService(
      @Qualifier("smppConfiguration") final SmppConfiguration smppConfiguration,
      @Qualifier("serverMessageReceiverListener") final ServerMessageReceiverListenerImpl serverMessageReceiverListener,
      @Qualifier("serverResponseDeliveryListener") final ServerResponseDeliveryListenerImpl serverResponseDeliveryListener,
      @Qualifier("sessionStateListener") final SessionStateListenerImpl sessionStateListener,
      @Qualifier("smppTaskExecutor") final TaskExecutor taskExecutor
  ) {
    this.configuration = smppConfiguration;
    this.serverMessageReceiverListener = serverMessageReceiverListener;
    this.serverResponseDeliveryListener = serverResponseDeliveryListener;
    this.sessionStateListener = sessionStateListener;
    this.taskExecutor = taskExecutor;
  }

  @Async
  public void start() {
    final int port = configuration.getPort();
    final int transactionTimer = configuration.getTransactionTimer();
    LOG.info("SMPP service started on port {}", port);
    try {
      int connectionCount = 0;
      sessionListener = new SMPPServerSessionListener(port);
      sessionListener.setPduProcessorDegree(5);
      LOG.info("Listening on port {}", port);
      while (running) {
        LOG.info("Waiting for new connection...");
        try {
          final SMPPServerSession serverSession = sessionListener.accept();
          serverSession.addSessionStateListener(sessionStateListener);
          LOG.info("Accepting connection #{} for session {} with transaction timeout {}", ++connectionCount, serverSession.getSessionId(), transactionTimer);
          serverSession.setMessageReceiverListener(serverMessageReceiverListener);
          serverSession.setResponseDeliveryListener(serverResponseDeliveryListener);
          serverSession.setTransactionTimer(transactionTimer);

          taskExecutor.execute(new WaitBindTask(serverSession, configuration.getBindTimeout(), configuration.getEnquireLinkTimer()));
        } catch (final IOException e) {
          if (running) {
            LOG.error("Could not accept connection: {}", e.getMessage());
          } else {
            LOG.info("Accept failed, because socket was closed");
          }
        }
      }
      LOG.info("Close listener port");
      sessionListener.close();
      sessionListener = null;
      sessionStateListener = null;
      serverMessageReceiverListener = null;
      serverResponseDeliveryListener = null;
    } catch (final IOException e) {
      LOG.error("Could not listen on port " + port, e);
    }
    LOG.info("SMPP server stopped");
  }

  public void stop() {
    try {
      running = false;
      if (sessionListener != null) {
        sessionListener.close();
      }
      LOG.info("SMPP service stopped");
    } catch (final IOException e) {
      LOG.error("Could not stop listener", e);
    }
  }

  private class WaitBindTask implements Runnable {

    private final SMPPServerSession serverSession;
    private final long timeout;
    private final int enquireLinkTimer;

    public WaitBindTask(SMPPServerSession serverSession, final long timeout, final int enquireLinkTimer) {
      this.serverSession = serverSession;
      this.timeout = timeout;
      this.enquireLinkTimer = enquireLinkTimer;
    }

    public void run() {
      try {
        LOG.info("Wait for bind request on session {} (timeout {})", serverSession.getSessionId(), timeout);
        final BindRequest bindRequest = serverSession.waitForBind(timeout);
        LOG.info("Accepting bind for session {}, interface version {}", serverSession.getSessionId(), bindRequest.getInterfaceVersion());
        try {
          bindRequest.accept("sys", InterfaceVersion.IF_34);
        } catch (PDUStringException e) {
          LOG.error("PDU string exception", e);
          bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
        }
        serverSession.setEnquireLinkTimer(enquireLinkTimer);
      } catch (final IllegalStateException e) {
        LOG.error("System error", e);
      } catch (final TimeoutException e) {
        LOG.warn("Wait for bind has reached timeout", e);
      } catch (final IOException e) {
        LOG.error("Failed accepting bind request for session", e);
      }
      LOG.info("WaitBindTask ended");
    }
  }

}
