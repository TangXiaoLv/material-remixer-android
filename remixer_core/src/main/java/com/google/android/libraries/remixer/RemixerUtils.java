package com.google.android.libraries.remixer;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RemixerUtils {

  public static int parseColor(String colorString) {
    if (colorString.charAt(0) == '#') {
      // Use a long to avoid rollovers on #ffXXXXXX
      long color = Long.parseLong(colorString.substring(1), 16);
      if (colorString.length() == 7) {
        // Set the alpha value
        color |= 0x00000000ff000000;
      } else if (colorString.length() != 9) {
        throw new IllegalArgumentException("Unknown color");
      }
      return (int) color;
    }

    throw new IllegalArgumentException("Unknown color");
  }

  public static void close(Closeable c) {
    if (c != null) {
      try {
        c.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static boolean isEmpty(CharSequence c) {
    return c == null || c.length() == 0;
  }

  public static boolean equals(CharSequence a, CharSequence b) {
    if (a == b) return true;
    int length;
    if (a != null && b != null && (length = a.length()) == b.length()) {
      if (a instanceof String && b instanceof String) {
        return a.equals(b);
      } else {
        for (int i = 0; i < length; i++) {
          if (a.charAt(i) != b.charAt(i)) return false;
        }
        return true;
      }
    }
    return false;
  }

  public static void copy(File src, File dst) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(src);
      out = new FileOutputStream(dst);
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      close(in);
      close(out);
    }
  }

  public static void copy(InputStream src, File dst) throws IOException {
    OutputStream out = null;
    try {
      out = new FileOutputStream(dst);
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = src.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      close(src);
      close(out);
    }
  }
}
