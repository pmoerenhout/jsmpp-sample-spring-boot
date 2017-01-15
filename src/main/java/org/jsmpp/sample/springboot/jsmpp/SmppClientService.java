package org.jsmpp.sample.springboot.jsmpp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.SubmitSmResp;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.sample.springboot.connection.ConnectionProperties;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SessionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class SmppClientService {

  private static final Logger LOG = LoggerFactory.getLogger(SmppClientService.class);

  private SmppConfiguration configuration;
  private MessageReceiverListener messageReceiverListener;
  private SessionStateListener sessionStateListener;
  private Charset charset;

  @Autowired
  public SmppClientService(
      @Qualifier("smppConfiguration") final SmppConfiguration smppConfiguration,
      @Qualifier("messageReceiverListener") final MessageReceiverListenerImpl messageReceiverListener,
      @Qualifier("sessionStateListener") final SessionStateListenerImpl sessionStateListener
  ) {
    this.configuration = smppConfiguration;
    this.messageReceiverListener = messageReceiverListener;
    this.sessionStateListener = sessionStateListener;
    this.charset = StandardCharsets.ISO_8859_1;
  }

  @Async("asyncTaskExecutor")
  public Future<Long> start(final int messagesToSend) throws InterruptedException {
    final long start = System.currentTimeMillis();
    connect(Thread.currentThread().getName(), messagesToSend);
    return new AsyncResult<>(Long.valueOf(System.currentTimeMillis() - start));
  }

  public void connect(final String taskIdentifier, final int messagesToSend) throws InterruptedException {
    LOG.info("Connect on task '{}' with {} messages to send", taskIdentifier, messagesToSend);
    SMPPSessionCustom session = new SMPPSessionCustom(new ConnectionProperties());
    session.setMessageReceiverListener(messageReceiverListener);
    session.addSessionStateListener(sessionStateListener);
    final String host = configuration.getHost();
    final int port = configuration.getPort();
    LOG.debug("SMPP session with id {} started on port {}", session.getId(), port);
    try {
      LOG.warn("Connecting session with id {} on task {}", session.getId(), taskIdentifier);
      String systemId = session.connectAndBind(host, port,
          new BindParameter(BindType.BIND_TX, "j", "jpwd", "cp", TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null));
      LOG.info("Connected session with id {} with SMSC with system id {} on task {}", session.getId(), systemId, taskIdentifier);

      sendMessages(session, taskIdentifier, messagesToSend);

    } catch (IOException e) {
      LOG.error("Failed connect and bind to host: {}", e.getMessage());
    }
    LOG.debug("Session unbind and close");
    session.unbindAndClose();
    LOG.debug("Session unbind and close, done");
  }

  public void sendMessages(final SMPPSessionCustom session, final String taskIdentifier, final int messageCount) {
    try {
      for (int i = 0; i < messageCount; i++) {

        SubmitSmResp submitSmResp = session.submitShortMessageGetResp("CMT",
            TypeOfNumber.ABBREVIATED, NumberingPlanIndicator.UNKNOWN, "1616",
            TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, "628176504657",
            new ESMClass(), (byte) 0, (byte) 1, null, null,
            new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte) 0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false),
            (byte) 0,
            String.format("message session %d task %s - #%d/%d", session.getId(), taskIdentifier, i+1, messageCount).getBytes(charset));

//        String messageId = session.submitShortMessage("CMT",
//            TypeOfNumber.ABBREVIATED, NumberingPlanIndicator.UNKNOWN, "1616",
//            TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, "628176504657",
//            new ESMClass(), (byte) 0, (byte) 1, null, null,
//            new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte) 0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false),
//            (byte) 0,
//            String.format("message session %d task %s - #%d/%d", session.getId(), taskIdentifier, i+1, messageCount).getBytes(charset));

        LOG.debug("Message submitted, message_id is {}", submitSmResp.getMessageId());
      }
    } catch (PDUException e) {
      // Invalid PDU parameter
      LOG.error("Invalid PDU parameter", e);
    } catch (ResponseTimeoutException e) {
      // Response timeout
      LOG.error("Response timeout", e);
    } catch (InvalidResponseException e) {
      // Invalid response
      LOG.error("Receive invalid response", e);
    } catch (NegativeResponseException e) {
      // Receiving negative response (non-zero command_status)
      LOG.error("Receive negative response, e");
    } catch (IOException e) {
      LOG.error("IO error occured", e);
    }
  }

}
