package org.jsmpp.sample.springboot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Future;

import org.apache.commons.lang3.RandomUtils;
import org.jsmpp.sample.springboot.jsmpp.SmppClientService;
import org.jsmpp.sample.springboot.jsmpp.SmppServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DemoApplication.class);
  private static final int NUMBER_OF_CLIENT_SESSIONS = 200;
  private static final int MIN_MESSAGES_PER_SESSION = 2;
  private static final int MAX_MESSAGES_PER_SESSION = 250;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private SmppServerService smppServerService;

  @Autowired
  private SmppClientService smppClientService;

  public static void main(String args[]) {

    final SpringApplication springApplication = new SpringApplication(DemoApplication.class);
    springApplication.setHeadless(true);
    LOG.info("Starting the Spring context");
    springApplication.run(args).close();
    LOG.info("The Spring context has been running and closed");
  }

  public void run(String args[]) throws IOException, InterruptedException {
    LOG.info("**********************************************");
    LOG.info("Starting the main thread");
    LOG.info("**********************************************");
    LOG.info("java.version      : {}", System.getProperty("java.version"));
    LOG.info("java.vendor       : {}", System.getProperty("java.vendor"));
    LOG.info("java.home         : {}", System.getProperty("java.home"));
    LOG.info("java.class.path   : {}", System.getProperty("java.class.path"));
    LOG.info("user.dir          : {}", System.getProperty("user.dir"));
    LOG.info("user.name         : {}", System.getProperty("user.name"));
    LOG.info("default timezone  : {}", TimeZone.getDefault().getDisplayName());
    LOG.info("default locale    : {}", Locale.getDefault().getDisplayName());
    for (int i = 0; i < args.length; i++) {
      LOG.info("cmdline argument  : " + args[i]);
    }
    LOG.info("context enviroment: {}", applicationContext.getEnvironment());
    LOG.info("context start     : {}", applicationContext.getStartupDate());

    smppServerService.start();
    final List<Future<Long>> futureList = new ArrayList<>();
    // Start 200 client sessions asynchronously
    for (int i = 0; i < NUMBER_OF_CLIENT_SESSIONS; i++) {
      // Start client sessions asynchronously with random number of messages
      futureList.add(smppClientService.start(RandomUtils.nextInt(MIN_MESSAGES_PER_SESSION, MAX_MESSAGES_PER_SESSION)));
    }

    final long futureListSize = futureList.size();
    while (true) {
      long done = futureList.stream().filter(s -> s.isDone()).count();
      long cancelled = futureList.stream().filter(s -> s.isCancelled()).count();
      LOG.info("done: {}/{} cancelled:{}", done, futureListSize, cancelled, futureListSize);
      if (done == futureListSize) {
        break;
      } else if (cancelled != 0) {
        LOG.error("A task was cancelled!");
        break;
      }
      Thread.sleep(1000);
    }
    smppServerService.stop();

    LOG.info("All sessions are done, quit");
  }

  /*
  ** TaskExecutor for BindTask in SMPP server
   */
  @Bean
  @Qualifier("smppTaskExecutor")
  public TaskExecutor getSmppTaskExecutor() {
    final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(100);
    threadPoolTaskExecutor.setMaxPoolSize(100);
    threadPoolTaskExecutor.setQueueCapacity(NUMBER_OF_CLIENT_SESSIONS);
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
    threadPoolTaskExecutor.setQueueCapacity(NUMBER_OF_CLIENT_SESSIONS);
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
    threadPoolTaskExecutor.setQueueCapacity(NUMBER_OF_CLIENT_SESSIONS);
    threadPoolTaskExecutor.setThreadNamePrefix("async-task-");
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