package org.jsmpp.sample.springboot.connection;

public class ConnectionProperties {

  public int get(final String key, final int defaultValue) {
    // get the value from external or return default
    String property = System.getProperty(key);
    return property == null ? defaultValue : Integer.parseInt(property);
  }
}
