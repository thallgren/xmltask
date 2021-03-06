package com.oopsconsultancy.xmltask;

import java.util.*;
import org.w3c.dom.*;

import com.oopsconsultancy.xmltask.ant.Param;

import org.apache.tools.ant.*;

/**
 * performs the basic task of identifying the qualifying XML nodes via XPath,
 * and then performing an action (e.g. replacement, removal, insertion) on that
 * node. We use the callback mechanism to record the nodes of interest, and once
 * that's completed, we then invoke on each node. This is to prevent infinite
 * loops (see test #95)
 * 
 * @author <a href="mailto:brian@oopsconsultancy.com">Brian Agnew</a>
 * @version $Id: XmlReplace.java,v 1.12 2006/11/01 22:40:37 bagnew Exp $
 */
public class XmlReplace implements XPathAnalyserClient {

  private final String path;

  private final Action action;

  private Task task = null;

  private final List<Node> nodes = new ArrayList<Node>();

  private String ifProperty;

  private String unlessProperty;

  public XmlReplace(final String path, final Action action) {
    this.path = path;
    this.action = action;
  }

  public void setTask(final Task task) {
    this.task = task;
  }

  private void log(final String msg, final int level) {
    // task may not be set sometimes (e.g. during unit tests)
    if (task != null) {
      task.log(msg, level);
    }
    else if(level < Project.MSG_INFO) {
      System.err.println(msg);
    }
  }

  public int apply(final Document doc) throws Exception {
    // some actions don't take a path. In this situation we
    // call them with a null node and return
    if (!isApplicable()) {
      return 0;
    }
    if (path == null) {
      log("Applying " + action, Project.MSG_VERBOSE);
      action.apply(null);
      action.complete();
      log("Applied " + action, Project.MSG_VERBOSE);
      return 1;
    }
    log("Applying " + action + " to " + path, Project.MSG_VERBOSE);

    action.setDocument(doc);

    XPathAnalyser xpa = XPathAnalyserFactory.getAnalyser();
    xpa.registerClient(this, null);

    // clear the nodes from the last invocation
    nodes.clear();
    int count = xpa.analyse(doc, path);

    // and iterate through the nodes returned via the callbacks.
    // We do this otherwise we could get in nasty loop situations
    // with repeated matches, inserts and matches on *those* inserts
    for (Node node : nodes) {
      action.apply(node);
    }

    log("Applied " + action + " - " + count + " match(es)", Project.MSG_VERBOSE);
    action.complete();
    return count;
  }

  /**
   * checks the 'if' and 'unless' properties, and if specified, determines
   * whether stuff gets done! If both 'if' and 'unless' are set then you've only
   * yourself to blame
   * 
   * @return true if processing to take place
   */
  private boolean isApplicable() {
    if (ifProperty != null) {
      // then we only do this if the property given is set/valid
      if (task.getProject().getProperty(ifProperty) != null) {
		    log("Performing action since '" + ifProperty +"' is set" , Project.MSG_VERBOSE);
        return true;
      }
	    log("Not performing action since '" + ifProperty +"' is not set" , Project.MSG_VERBOSE);
      return false;
    }
    if (unlessProperty != null) {
      // then we only do this if the property given is NOT set/valid
      if (task.getProject().getProperty(unlessProperty) == null) {
		    log("Performing action since '" + unlessProperty +"' is not set" , Project.MSG_VERBOSE);
        return true;
      }
	    log("Not performing action since '" + unlessProperty +"' is set" , Project.MSG_VERBOSE);
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return action.toString() + " (" + path + ")";
  }

  /**
   * called by the XPathAnalyser implementations
   * 
   * @param n
   * @param callback
   * @throws Exception
   */
  public void applyNode(final Node n, final Param callback) throws Exception {
    nodes.add(n);
  }

  /**
   * called by the XPathAnalyser implementations
   * 
   * @param str
   * @param callback
   * @throws Exception
   */
  public void applyNode(final String str, final Param callback) throws Exception {
    nodes.add(action.getDocument().createTextNode(str));
  }

  /**
   * sets a property determining execution
   * 
   * @param ifProperty
   */
  public void setIf(final String ifProperty) {
    this.ifProperty = ifProperty;
  }

  /**
   * sets a property determining execution
   * 
   * @param unlessProperty
   */
  public void setUnless(final String unlessProperty) {
    this.unlessProperty = unlessProperty;
  }
}
