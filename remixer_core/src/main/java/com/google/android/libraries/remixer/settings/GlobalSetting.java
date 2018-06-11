package com.google.android.libraries.remixer.settings;

import java.util.List;

public class GlobalSetting {

  private List<ColorSetting> colors;

  public List<ColorSetting> getColors() {
    return colors;
  }

  public static class ColorSetting {
    private String title;//全局颜色",
    private String defColors;//#ff0000,#00ff00,#0000ff",
    private String type;//Primary"

    public String getTitle() {
      return title;
    }

    public String getDefColors() {
      return defColors;
    }

    public String getType() {
      return type;
    }
  }
}
