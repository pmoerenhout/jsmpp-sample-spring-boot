/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jsmpp.sample.springboot.jsmpp;

import static org.jsmpp.SMPPConstant.STAT_ESME_RSYSERR;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.sample.springboot.misc.Util;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageReceiverListenerImpl implements MessageReceiverListener {

  public void onAcceptDeliverSm(final DeliverSm deliverSm) throws ProcessRequestException {
    log.info("deliver_sm seq:{} src:{} {}/{} dst:{} {}/{}",
        deliverSm.getSequenceNumber(),
        deliverSm.getSourceAddr(), deliverSm.getSourceAddrTon(), deliverSm.getSourceAddrNpi(),
        deliverSm.getDestAddress(), deliverSm.getDestAddrTon(), deliverSm.getDestAddrNpi());
    log.debug("deliver_sm ESM           {}", Util.bytesToHex(deliverSm.getEsmClass()));
    log.debug("deliver_sm sequence      {}", deliverSm.getSequenceNumber());
    log.debug("deliver_sm service type  {}", deliverSm.getServiceType());
    log.debug("deliver_sm priority flag {}", deliverSm.getPriorityFlag());
    final OptionalParameter[] optionalParameters = deliverSm.getOptionalParameters();
    for (final OptionalParameter optionalParameter : optionalParameters) {
      final byte[] content = optionalParameter.serialize();
      log.debug("Optional Parameter {}: [{}]", optionalParameter.tag, Util.bytesToHex(content));
    }

    if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass()) || MessageType.SME_DEL_ACK
        .containedIn(deliverSm.getEsmClass())) {
      // this message is delivery receipt
      try {

        log.info("deliver_sm sm : {}", new String(deliverSm.getShortMessage()));
        final OptionalParameter.OctetString jmr = (OptionalParameter.OctetString) deliverSm.getOptionalParameter((short) 8192);
        if (jmr != null) {
          final String jmrId = jmr.getValueAsString();
          log.info("deliver_sm jmr: '{}'", jmrId);
        }
        final OptionalParameter.OctetString unknown = (OptionalParameter.OctetString) deliverSm.getOptionalParameter((short) 1542);
        if (unknown != null) {
          log.info("deliver_sm 1542: [{}]", Util.bytesToHex(unknown.getValue()));
        }
        // MessageBird networkMccMnc
        final OptionalParameter.OctetString networkMccMnc = (OptionalParameter.OctetString) deliverSm.getOptionalParameter((short) 5472);
        if (networkMccMnc != null) {
          final String networkMccMncHex = Util.bytesToHex(networkMccMnc.getValue());
          log.info("deliver_sm networkMccMnc: '{}'", networkMccMncHex);
          log.info("deliver_sm networkMccMnc: '{}'", networkMccMnc.getValueAsString());
        }
        // MessageBird networkMccMnc
        final OptionalParameter.Network_error_code networkErrorCode = (OptionalParameter.Network_error_code) deliverSm.getOptionalParameter(OptionalParameter.Tag.NETWORK_ERROR_CODE);
        if (networkErrorCode != null) {
          log.info("deliver_sm network ErrorCode: '{}'", networkErrorCode.getErrorCode());
          log.info("deliver_sm network Type: '{}'", networkErrorCode.getNetworkType().name());
        }

//        //final DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
//        final MyDeliveryReceipt delReceipt = deliverSm.getDeliveryReceipt(STRIPPER);
//        //final DeliveryReceipt delReceipt = deliverSm.getDeliveryReceipt(SECOND_STRIPPER);
//
//        final String messageId;
//        if (delReceipt.getId().length() == 10) {
//          messageId = StringUtils.stripStart(delReceipt.getId(), "0");
//        } else {
//          messageId = delReceipt.getId();
//        }

//        LOG.info("msgid:{} submitted:{} delivered:{}", messageId, delReceipt.getSubmitted(), delReceipt.getDelivered());
//        LOG.info("submitdate:{} donedate:{}", delReceipt.getSubmitDate(), delReceipt.getDoneDate());
//        LOG.info("final state:{} error:{}", delReceipt.getFinalStatus(), delReceipt.getError());
//        LOG.info("text:'{}'", delReceipt.getText());
//
//        LOG.info("Received delivery receipt for message '{}' from {} to {}: {}",
//            messageId, deliverSm.getSourceAddr(), deliverSm.getDestAddress(), delReceipt);
//
//        LOG.info("dr id {}", delReceipt.getId());
//        LOG.info("dr delivered {}", delReceipt.getDelivered());
//        LOG.info("dr submitted {}", delReceipt.getSubmitted());
//        LOG.info("dr submit date {}", delReceipt.getSubmitDate());
//        LOG.info("dr done date {}", delReceipt.getDoneDate());
//        LOG.info("dr final status {}", delReceipt.getFinalStatus());
//        LOG.info("dr error {}", delReceipt.getError());
//        LOG.info("dr text {}", delReceipt.getText());

//        // delReceipt properties
//        dr.setMessageId(messageId); // the altered messageId
//        dr.setDelivered((byte) delReceipt.getDelivered());
//        dr.setSubmitted((byte) delReceipt.getSubmitted());
//        dr.setSubmitDate(delReceipt.getSubmitDate().toInstant());
//        dr.setDoneDate(delReceipt.getDoneDate().toInstant());
//        if (delReceipt.getFinalStatus() != null) {
//          dr.setState((byte) delReceipt.getFinalStatus().value());
//        }
//        if (delReceipt.getError() != null) {
//          dr.setError(delReceipt.getError());
//        }
//        dr.setText(delReceipt.getText().getBytes());
//        drRepository.saveAndFlush(dr);

//      } catch (InvalidDeliveryReceiptException e) {
//        LOG.error("Invalid delivery receipt", e);

      } catch (RuntimeException e) {
        log.error("Runtime exception", e);
        throw new ProcessRequestException(e.getMessage(), STAT_ESME_RSYSERR);
      }
    } else {
      // this message is regular short message
      log.info("Receiving message: {}", new String(deliverSm.getShortMessage()));
    }
  }

  @Override
  public void onAcceptAlertNotification(AlertNotification alertNotification) {
    log.info("onAcceptAlertNotification: {} {}", alertNotification.getSourceAddr(), alertNotification.getEsmeAddr());
  }

  @Override
  public DataSmResult onAcceptDataSm(final DataSm dataSm, final Session source)
      throws ProcessRequestException {
    log.info("onAcceptDataSm: {} {} {}", source.getSessionId(), dataSm.getSourceAddr(), dataSm.getDestAddress());
    throw new ProcessRequestException("The data_sm is not implemented", STAT_ESME_RSYSERR);
  }

}
