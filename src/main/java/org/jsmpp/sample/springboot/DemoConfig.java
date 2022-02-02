package org.jsmpp.sample.springboot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "demo")
public class DemoConfig {

  /*
   * These are default values, see application.properties to override
   */

  private int numberOfClientSessions = 5;
  private int minMessagesPerSession = 1;
  private int maxMessagesPerSession = 10;

}
