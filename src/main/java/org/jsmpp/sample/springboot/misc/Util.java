package org.jsmpp.sample.springboot.misc;

import java.util.Formatter;

public class Util {

  public static String bytesToHex(final byte[] bytes) {
    if (bytes == null) {
      throw new IllegalArgumentException("The bytes parameter is null");
    }
    final StringBuilder sb = new StringBuilder(bytes.length * 2);
    final Formatter formatter = new Formatter(sb);
    for (byte b : bytes) {
      formatter.format("%02X", b);
    }
    formatter.close();
    return sb.toString();
  }

  public static String bytesToHex(final byte b) {
    final StringBuilder sb = new StringBuilder(2);
    Formatter formatter = new Formatter(sb);
    formatter.format("%02X", b);
    formatter.close();
    return sb.toString();
  }

}
