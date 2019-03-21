package com.synconset;

import android.os.Bundle;

public class ResultIPC {
    
  private static ResultIPC instance;

  public synchronized static ResultIPC get() {
      if (instance == null) {
          instance = new ResultIPC ();
      }
      return instance;
  }

  private int sync = 0;

  private Bundle largeData;
  public int setLargeData(Bundle largeData) {
      this.largeData = largeData;
      return ++sync;
  }
  
  public Bundle getLargeData(int request) {
      return (request == sync) ? largeData : null;
  }
}