package org.jsmpp.sample.springboot.jsmpp;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.PDUReader;
import org.jsmpp.PDUSender;
import org.jsmpp.bean.DataCoding;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SubmitSmResp;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.sample.springboot.connection.ConnectionConstants;
import org.jsmpp.sample.springboot.connection.ConnectionProperties;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SubmitSmCommandTask;
import org.jsmpp.session.connection.ConnectionFactory;

public class SMPPSessionCustom extends SMPPSession {

  private static final AtomicReference<Long> currentTime = new AtomicReference<>(System.currentTimeMillis());

  private final ConnectionProperties connectionProperties;
  private final int timeout;
  private final long id;

  public SMPPSessionCustom(ConnectionProperties connectionProperties) {
    super();
    this.connectionProperties = connectionProperties;
    this.timeout = this.connectionProperties.get(ConnectionConstants.OPERATOR_SUMBIT_SM_TIMEOUT, 35000);
    this.id = currentTime.accumulateAndGet(System.currentTimeMillis(),
        (prev, next) -> next > prev ? next : prev + 1);
  }

  public SMPPSessionCustom(PDUSender pduSender, PDUReader pduReader, ConnectionFactory connFactory,
                           ConnectionProperties connectionProperties) {
    super(pduSender, pduReader, connFactory);
    this.connectionProperties = connectionProperties;
    this.timeout = this.connectionProperties.get(ConnectionConstants.OPERATOR_SUMBIT_SM_TIMEOUT, 35000);
    this.id = currentTime.accumulateAndGet(System.currentTimeMillis(),
        (prev, next) -> next > prev ? next : prev + 1);
  }

  public SMPPSessionCustom(String host, int port, BindParameter bindParam, PDUSender pduSender, PDUReader pduReader,
                           ConnectionFactory connFactory, ConnectionProperties connectionProperties) throws IOException {
    super(host, port, bindParam, pduSender, pduReader, connFactory);
    this.connectionProperties = connectionProperties;
    this.timeout = this.connectionProperties.get(ConnectionConstants.OPERATOR_SUMBIT_SM_TIMEOUT, 35000);
    this.id = currentTime.accumulateAndGet(System.currentTimeMillis(),
        (prev, next) -> next > prev ? next : prev + 1);

  }

  public SMPPSessionCustom(String host, int port, BindParameter bindParam, ConnectionProperties connectionProperties)
      throws IOException {
    super(host, port, bindParam);
    this.connectionProperties = connectionProperties;
    this.timeout = this.connectionProperties.get(ConnectionConstants.OPERATOR_SUMBIT_SM_TIMEOUT, 35000);
    this.id = currentTime.accumulateAndGet(System.currentTimeMillis(),
        (prev, next) -> next > prev ? next : prev + 1);

  }

  public SubmitSmResp submitShortMessageGetResp(String serviceType, TypeOfNumber sourceAddrTon,
                                                NumberingPlanIndicator sourceAddrNpi, String sourceAddr, TypeOfNumber destAddrTon,
                                                NumberingPlanIndicator destAddrNpi, String destinationAddr, ESMClass esmClass, byte protocolId,
                                                byte priorityFlag, String scheduleDeliveryTime, String validityPeriod,
                                                RegisteredDelivery registeredDelivery, byte replaceIfPresentFlag, DataCoding dataCoding,
                                                byte smDefaultMsgId, byte[] shortMessage, OptionalParameter... optionalParameters) throws PDUException,
      ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException {

    ensureTransmittable("submitShortMessage");

    SubmitSmCommandTask submitSmTask = new SubmitSmCommandTask(pduSender(), serviceType, sourceAddrTon,
        sourceAddrNpi, sourceAddr, destAddrTon, destAddrNpi, destinationAddr, esmClass, protocolId,
        priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery, replaceIfPresentFlag,
        dataCoding, smDefaultMsgId, shortMessage, optionalParameters);

    SubmitSmResp resp = (SubmitSmResp) executeSendCommand(submitSmTask, timeout);
    return resp;
  }

  public long getId() {
    return id;
  }

}
