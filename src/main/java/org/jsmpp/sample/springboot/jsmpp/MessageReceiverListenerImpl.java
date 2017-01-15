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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReceiverListenerImpl implements MessageReceiverListener {

  private static final Logger LOG = LoggerFactory.getLogger(MessageReceiverListenerImpl.class);

  public void onAcceptDeliverSm(final DeliverSm deliverSm) throws ProcessRequestException {
    LOG.info("deliver_sm seq:{} src:{} {}/{} dst:{} {}/{}",
        deliverSm.getSequenceNumber(),
        deliverSm.getSourceAddr(), deliverSm.getSourceAddrTon(), deliverSm.getSourceAddrNpi(),
        deliverSm.getDestAddress(), deliverSm.getDestAddrTon(), deliverSm.getDestAddrNpi());
    LOG.debug("deliver_sm ESM           {}", Util.bytesToHex(deliverSm.getEsmClass()));
    LOG.debug("deliver_sm sequence      {}", deliverSm.getSequenceNumber());
    LOG.debug("deliver_sm service type  {}", deliverSm.getServiceType());
    LOG.debug("deliver_sm priority flag {}", deliverSm.getPriorityFlag());
    final OptionalParameter[] optionalParameters = deliverSm.getOptionalParameters();
    for (final OptionalParameter optionalParameter : optionalParameters) {
      final byte[] content = optionalParameter.serialize();
      LOG.debug("Optional Parameter {}: [{}]", optionalParameter.tag, Util.bytesToHex(content));
    }

    if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass()) || MessageType.SME_DEL_ACK
        .containedIn(deliverSm.getEsmClass())) {
      // this message is delivery receipt
      try {

        LOG.info("deliver_sm sm : {}", new String(deliverSm.getShortMessage()));
        final OptionalParameter.OctetString jmr = (OptionalParameter.OctetString) deliverSm.getOptionalParameter((short) 8192);
        if (jmr != null) {
          final String jmrId = jmr.getValueAsString();
          LOG.info("deliver_sm jmr: '{}'", jmrId);
        }
        final OptionalParameter.OctetString unknown = (OptionalParameter.OctetString) deliverSm.getOptionalParameter((short) 1542);
        if (unknown != null) {
          LOG.info("deliver_sm ???: [{}]", Util.bytesToHex(unknown.getValue()));
        }
        // MessageBird networkMccMnc
        final OptionalParameter.OctetString networkMccMnc = (OptionalParameter.OctetString) deliverSm.getOptionalParameter((short) 5472);
        if (networkMccMnc != null) {
          final String networkMccMncHex = Util.bytesToHex(networkMccMnc.getValue());
          LOG.info("deliver_sm networkMccMnc: '{}'", networkMccMncHex);
          LOG.info("deliver_sm networkMccMnc: '{}'", networkMccMnc.getValueAsString());
        }
        // MessageBird networkMccMnc
        final OptionalParameter.Network_error_code networkErrorCode = (OptionalParameter.Network_error_code) deliverSm.getOptionalParameter(OptionalParameter.Tag.NETWORK_ERROR_CODE);
        if (networkErrorCode != null) {
          LOG.info("deliver_sm network ErrorCode: '{}'", networkErrorCode.getErrorCode());
          LOG.info("deliver_sm network Type: '{}'", networkErrorCode.getNetworkType().name());
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
        LOG.error("Runtime exception", e);
      }
    } else {
      // this message is regular short message
      LOG.info("Receiving message: {}", new String(deliverSm.getShortMessage()));
    }
  }

  public void onAcceptAlertNotification(AlertNotification alertNotification) {
    LOG.info("onAcceptAlertNotification: {} {}", alertNotification.getSourceAddr(), alertNotification.getEsmeAddr());
  }

  public DataSmResult onAcceptDataSm(final DataSm dataSm, final Session source)
      throws ProcessRequestException {
    LOG.info("onAcceptDataSm: {} {} {}", source.getSessionId(), dataSm.getSourceAddr(), dataSm.getDestAddress());
    return null;
  }

}
