package org.jsmpp.sample.springboot.jsmpp;

import org.jsmpp.session.SMPPSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmppBeanConfig {

  @Bean
  @Qualifier("messageReceiverListener")
  public MessageReceiverListenerImpl getMessageReceiverListenerImpl() {
    return new MessageReceiverListenerImpl();
  }

  @Bean
  @Qualifier("smppSession")
  public SMPPSession getSMPPSession() {
    return new SMPPSession();
  }

  @Bean
  @Qualifier("serverMessageReceiverListener")
  public ServerMessageReceiverListenerImpl getServerMessageReceiverListener() {
    return new ServerMessageReceiverListenerImpl();
  }

  @Bean
  @Qualifier("serverResponseDeliveryListener")
  public ServerResponseDeliveryListenerImpl getServerResponseDeliveryListener() {
    return new ServerResponseDeliveryListenerImpl();
  }

  @Bean
  @Qualifier("sessionStateListener")
  public SessionStateListenerImpl getSessionStateListener() {
    return new SessionStateListenerImpl();
  }

}
