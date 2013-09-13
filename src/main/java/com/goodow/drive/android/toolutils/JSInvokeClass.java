package com.goodow.drive.android.toolutils;

public class JSInvokeClass {
  private final int width;
  private final int heght;
  private final String flashFilePath;

  public JSInvokeClass(int width, int heght, String flashFilePath) {
    super();
    this.width = width;
    this.heght = heght;
    this.flashFilePath = flashFilePath;
  }

  public String getFlashFilePath() {
    return flashFilePath;
  }

  public int getHeght() {
    return heght;
  }

  public int getWidth() {
    return width;
  }

}
