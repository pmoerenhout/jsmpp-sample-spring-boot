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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jsmpp.bean.CancelSm;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.QuerySm;
import org.jsmpp.bean.ReplaceSm;
import org.jsmpp.bean.SubmitMulti;
import org.jsmpp.bean.SubmitMultiResult;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.QuerySmResult;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMessageReceiverListenerImpl implements ServerMessageReceiverListener {

  private static final Logger LOG = LoggerFactory.getLogger(ServerMessageReceiverListenerImpl.class);

  private MessageIDGenerator messageIDGenerator;
  private Charset charset;

  public ServerMessageReceiverListenerImpl() {
    this.charset = StandardCharsets.ISO_8859_1;
    messageIDGenerator = new RandomMessageIDGenerator();
    LOG.info("SMSC charset is {}", charset.name());
  }

  public MessageId onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession source) throws ProcessRequestException {
    final MessageId messageId = messageIDGenerator.newMessageId();
    LOG.info("Session {} received '{}' message id:{}", source.getSessionId(), new String(submitSm.getShortMessage(), charset), messageId);
    return messageId;
  }

  public DataSmResult onAcceptDataSm(final DataSm dataSm, final Session source) throws ProcessRequestException {
    LOG.info("Received data_sm");
    throw new ProcessRequestException("The data_sm is not implemented", STAT_ESME_RSYSERR);
  }

  public SubmitMultiResult onAcceptSubmitMulti(final SubmitMulti submitMulti, final SMPPServerSession source) throws ProcessRequestException {
    LOG.info("Received submit_multi");
    final MessageId messageId = messageIDGenerator.newMessageId();
    return new SubmitMultiResult(messageId.getValue());
  }

  public QuerySmResult onAcceptQuerySm(QuerySm querySm, SMPPServerSession source) throws ProcessRequestException {
    LOG.info("Received query_sm");
    throw new ProcessRequestException("The replace_sm is not implemented", STAT_ESME_RSYSERR);
  }

  public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession source) throws ProcessRequestException {
    LOG.info("Received replace_sm");
    throw new ProcessRequestException("The replace_sm is not implemented", STAT_ESME_RSYSERR);
  }

  public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession source)
      throws ProcessRequestException {
    LOG.info("Received cancelsm");
    throw new ProcessRequestException("The cancel_sm is not implemented", STAT_ESME_RSYSERR);
  }

}
