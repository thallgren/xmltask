package com.oopsconsultancy.xmltask.ant;

import com.oopsconsultancy.xmltask.*;
import org.apache.tools.ant.*;
import java.io.*;

/**
 * the Ant replacement task
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id: Replace.java,v 1.5 2006/11/01 22:40:38 bagnew Exp $
 */
public class Replace implements Instruction {

  private XmlTask task = null;

  private Action action = null;

  private String path = null;

  private boolean expandProperties = true;

  /**
   * the raw text to insert
   */
  private String text;

  /**
   * the file to insert
   */
  private File file;

  /**
   * the explicit XML to insert
   */
  private String xml;

  /**
   * the buffer to insert
   */
  private String buffer;

  private String ifProperty;

  private String unlessProperty;

  public void setPath(String path) {
    this.path = path;
  }

  public void setWithtext(final String to) throws Exception {
    action = new TextAction(to);
  }

  public void setWithxml(final String xml) throws Exception {
    this.xml = xml;
  }

  public void setExpandProperties(final boolean expandProperties) {
    this.expandProperties = expandProperties;
  }

  /**
   * used to insert literal text placed within the build.xml under the replace
   * element
   * 
   * @param text
   * @throws Exception
   */
  public void addText(final String text) throws Exception {
    this.text = text;
  }

  public void setWithfile(final File file) throws Exception {
    this.file = file;
  }

  public void setWithBuffer(final String buffer) throws Exception {
    this.buffer = buffer;
  }

  private void register() {
    try {
      if (buffer != null) {
		    action = XmlAction.xmlActionfromBuffer(buffer, task);
      }
      if (xml != null) {
		    action = XmlAction.xmlActionfromString(xml, task);
      }
      else if (file != null) {
        action = XmlAction.xmlActionfromFile(file, task);
      }
      else if (text != null) {
        if (expandProperties) {
          PropertyHelper ph = PropertyHelper.getPropertyHelper(task.getProject());
          text = ph.replaceProperties(null, text, task.getProject().getProperties());
        }
        action = XmlAction.xmlActionfromString(text, task);
      }
    }
    catch (Exception e) {
      throw new BuildException("Failed to specify text in replace", e);
    }
    if (path != null && action != null) {
      XmlReplace xmlReplace = new XmlReplace(path, action);
      xmlReplace.setIf(ifProperty);
      xmlReplace.setUnless(unlessProperty);
      task.add(xmlReplace);
    }
  }

  public void process(final XmlTask task) {
    this.task = task;
    register();
  }

  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;
  }

  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;
  }
}
