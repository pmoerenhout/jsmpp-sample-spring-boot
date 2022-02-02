package org.jsmpp.sample.springboot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Future;

import org.apache.commons.lang3.RandomUtils;
import org.jsmpp.sample.springboot.jsmpp.SmppClientService;
import org.jsmpp.sample.springboot.jsmpp.SmppServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemoRunner implements ApplicationRunner {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private DemoConfig config;

  @Autowired
  private SmppServerService smppServerService;

  @Autowired
  private SmppClientService smppClientService;


  public void run(final ApplicationArguments applicationArguments) throws Exception {
    log.info("**********************************************");
    log.info("Starting the application runner main thread");
    log.info("**********************************************");
    log.info("java.version       : {}", System.getProperty("java.version"));
    log.info("java.vendor        : {}", System.getProperty("java.vendor"));
    log.info("java.home          : {}", System.getProperty("java.home"));
    log.info("java.class.path    : {}", System.getProperty("java.class.path"));
    log.info("user.dir           : {}", System.getProperty("user.dir"));
    log.info("user.name          : {}", System.getProperty("user.name"));
    log.info("default timezone   : {}", TimeZone.getDefault().getDisplayName());
    log.info("default locale     : {}", Locale.getDefault().getDisplayName());
    applicationArguments.getOptionNames().forEach(optionName -> log.info("Option name {}: {}", optionName, applicationArguments.getOptionValues(optionName)));
    Arrays.stream(applicationArguments.getSourceArgs()).forEach(sourceArg -> log.info("Source arg: {}", sourceArg));
    log.info("context environment: {}", applicationContext.getEnvironment());
    log.info("context start      : {}", new Date(applicationContext.getStartupDate()));

    demoTime();
  }

  public void demoTime() throws InterruptedException {

    smppServerService.start();
    final List<Future<Long>> futureList = new ArrayList<>();
    for (int i = 0; i < config.getNumberOfClientSessions(); i++) {
      // Start client sessions asynchronously with random number of messages
      futureList.add(smppClientService.start(RandomUtils.nextInt(config.getMinMessagesPerSession(), config.getMaxMessagesPerSession())));
    }

    final long futureListSize = futureList.size();
    long done;
    long cancelled;
    while (true) {
      done = futureList.stream().filter(s -> s.isDone()).count();
      cancelled = futureList.stream().filter(s -> s.isCancelled()).count();
      log.info("sessions done:{}/{} cancelled:{}/{}", done, futureListSize, cancelled, futureListSize);
      if (done == futureListSize) {
        break;
      } else if (cancelled != 0) {
        log.error("A task was cancelled!");
        break;
      }
      Thread.sleep(1000);
    }
    smppServerService.stop();
    log.info("sessions done:{}/{} cancelled:{}/{}", done, futureListSize, cancelled, futureListSize);

    log.info("All sessions are done, quit");
  }

}
