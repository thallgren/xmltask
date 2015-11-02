package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;

import com.oopsconsultancy.xmltask.ant.Param;

public interface XPathAnalyserClient {

  public void applyNode(Node n, Param callback) throws Exception;
  public void applyNode(String str, Param callback) throws Exception;
}

