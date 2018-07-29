package eptf.solver.jms;

import org.springframework.jms.annotation.JmsListener;

import eptf.solver.ServerEvent;

public class Listener {

	  @JmsListener(destination = "Queue", containerFactory = "myFactory")
	  public void receiveMessage(ServerEvent transaction) {
	    System.out.println("Received <" + transaction + ">");
	  }
}
