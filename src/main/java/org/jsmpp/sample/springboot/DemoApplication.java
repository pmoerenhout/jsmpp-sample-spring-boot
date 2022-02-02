package org.jsmpp.sample.springboot;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@Configuration
@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

  @Autowired
  private DemoConfig config;

  @Autowired
  private ApplicationContext applicationContext;

  public static void main(String args[]) {
    final SpringApplication application = new SpringApplicationBuilder(DemoApplication.class)
        .headless(true).web(WebApplicationType.NONE)
        .build();
    log.info("Starting the Spring context");
    application.run(args).close();
    log.info("The Spring context has been closed");
  }

  public void run(String args[]) throws Exception {
    log.info("**********************************************");
    log.info("Starting the commandrunner main thread");
    log.info("**********************************************");
    log.info("java.version       : {}", System.getProperty("java.version"));
    log.info("java.vendor        : {}", System.getProperty("java.vendor"));
    log.info("java.home          : {}", System.getProperty("java.home"));
    log.info("java.class.path    : {}", System.getProperty("java.class.path"));
    log.info("user.dir           : {}", System.getProperty("user.dir"));
    log.info("user.name          : {}", System.getProperty("user.name"));
    log.info("default timezone   : {}", TimeZone.getDefault().getDisplayName());
    log.info("default locale     : {}", Locale.getDefault().getDisplayName());
    for (int i = 0; i < args.length; i++) {
      log.info("cmdline argument   : " + args[i]);
    }
    log.info("context environment: {}", applicationContext.getEnvironment());
    log.info("context start      : {}", new Date(applicationContext.getStartupDate()));

    /*
     * Only static method can be called, as beans will introduce cyclic dependency problems
     */
  }

  /*
   ** TaskExecutor for BindTask in SMPP server
   */
  @Bean
  @Qualifier("smppTaskExecutor")
  public TaskExecutor getSmppTaskExecutor() {
    log.info("NumberOfClientSessions: {}", config.getNumberOfClientSessions());
    final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(100);
    threadPoolTaskExecutor.setMaxPoolSize(100);
    threadPoolTaskExecutor.setQueueCapacity(config.getNumberOfClientSessions());
    threadPoolTaskExecutor.setThreadNamePrefix("smpp-task-");
    return threadPoolTaskExecutor;
  }

  /*
   ** TaskExecutor for Sessions in SMPP client
   */
  @Bean
  @Qualifier("asyncTaskExecutor")
  public TaskExecutor getAsyncTaskExecutor() {
    final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    // Set maximum of 100 sessions active
    threadPoolTaskExecutor.setCorePoolSize(100);
    threadPoolTaskExecutor.setMaxPoolSize(100);
    threadPoolTaskExecutor.setQueueCapacity(config.getNumberOfClientSessions());
    threadPoolTaskExecutor.setThreadNamePrefix("async-task-");
    return threadPoolTaskExecutor;
  }

  /*
   ** TaskExecutor for message sending in SMPP client
   */
  @Bean
  @Qualifier("messageTaskExecutor")
  public TaskExecutor getMessageTaskExecutor() {
    final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    // Set maximum of 100 sessions active
    threadPoolTaskExecutor.setCorePoolSize(100);
    threadPoolTaskExecutor.setMaxPoolSize(100);
    threadPoolTaskExecutor.setQueueCapacity(config.getNumberOfClientSessions());
    threadPoolTaskExecutor.setThreadNamePrefix("message-task-");
    return threadPoolTaskExecutor;
  }

  @Bean
  @Primary
  @Qualifier("taskExecutor")
  public TaskExecutor getTaskExecutor() {
    final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(200);
    threadPoolTaskExecutor.setMaxPoolSize(200);
    threadPoolTaskExecutor.setQueueCapacity(10000);
    threadPoolTaskExecutor.setThreadNamePrefix("task-");
    return threadPoolTaskExecutor;
  }

}