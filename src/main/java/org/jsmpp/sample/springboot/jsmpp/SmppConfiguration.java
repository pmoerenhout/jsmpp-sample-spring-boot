package org.jsmpp.sample.springboot.jsmpp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "smpp")
public class SmppConfiguration {

  private String host;
  private int port;
  private boolean ssl;
  private String charset;
  private Long bindTimeout;
  private Integer enquireLinkTimer;
  private Integer transactionTimer;

  public SmppConfiguration() {
    this.host = "localhost";
    this.port = 2775;
    this.ssl = false;
    this.bindTimeout = 60000L;
    this.enquireLinkTimer = 0;
    this.transactionTimer = 60000;
  }

  public String getHost() {
    return host;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public boolean isSsl() {
    return ssl;
  }

  public void setSsl(final boolean ssl) {
    this.ssl = ssl;
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(final String charset) {
    this.charset = charset;
  }

  public Long getBindTimeout() {
    return bindTimeout;
  }

  public void setBindTimeout(final Long bindTimeout) {
    this.bindTimeout = bindTimeout;
  }

  public Integer getEnquireLinkTimer() {
    return enquireLinkTimer;
  }

  public void setEnquireLinkTimer(final Integer enquireLinkTimer) {
    this.enquireLinkTimer = enquireLinkTimer;
  }

  public Integer getTransactionTimer() {
    return transactionTimer;
  }

  public void setTransactionTimer(final Integer transactionTimer) {
    this.transactionTimer = transactionTimer;
  }
}
