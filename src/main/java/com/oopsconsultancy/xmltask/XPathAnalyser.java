package com.oopsconsultancy.xmltask;

import org.w3c.dom.*;

import com.oopsconsultancy.xmltask.ant.Param;

public interface XPathAnalyser {

  public void registerClient(XPathAnalyserClient client, Param callback);

  public int analyse(Node node, String xpath) throws Exception;
}
