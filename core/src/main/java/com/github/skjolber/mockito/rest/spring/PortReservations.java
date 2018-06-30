package com.github.skjolber.mockito.rest.spring;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.net.ServerSocketFactory;

public class PortReservations {
	
	private static final int PORT_RANGE_MAX = 65535;
	private static final int PORT_RANGE_START = 1024+1;
	private static final int PORT_RANGE_END = PORT_RANGE_MAX;

	private final int portRangeStart;
	private final int portRangeEnd;
	
	private List<PortReservation> reservations = new ArrayList<>();

	public PortReservations() {
		this(PORT_RANGE_START, PORT_RANGE_END);
	}
	
	public PortReservations(String ... portNames) {
		this(PORT_RANGE_START, PORT_RANGE_END, portNames);
	}
	
	public PortReservations(int portRangeStart, int portRangeEnd, String ... portNames) {
		if(portRangeStart <= 0) {
			throw new IllegalArgumentException("Port range start must be greater than 0.");
		}
		if(portRangeEnd < portRangeStart) {
			throw new IllegalArgumentException("Port range end must not be lower than port range end.");
		}
		if(portRangeEnd > PORT_RANGE_MAX) {
			throw new IllegalArgumentException("Port range end must not be larger than " + PORT_RANGE_MAX + ".");
		}
		if(portNames != null && portNames.length > (portRangeEnd - portRangeStart + 1)) {
			throw new IllegalArgumentException("Cannot reserve " + portNames.length + " in range " + portRangeStart + "-" + portRangeEnd + ".");
		}

		this.portRangeStart = portRangeStart;
		this.portRangeEnd = portRangeEnd;
		
		if(portNames != null) {
			for(String portName : portNames) {
				reservations.add(new PortReservation(portName));
			}
		}
	}
	
	/**
	 * Get reserved ports.
	 * 
	 * @return map of portName and port value; &gt; 1 if a port has been reserved, -1 otherwise
	 */
	
	public Map<String, Integer> getPorts() {
		HashMap<String, Integer> ports = new HashMap<>(reservations.size());
		for (PortReservation portReservation : reservations) {
			ports.put(portReservation.getPropertyName(), portReservation.getPort());
		}
		return ports;
	}
	
	
	private class PortReservation {
		public PortReservation(String portName) {
			this.propertyName = portName;
		}
		private final String propertyName;
		private int port = -1;
		private ServerSocket serverSocket;
		
		public void reserved(int port, ServerSocket serverSocket) {
			this.port = port;
			this.serverSocket = serverSocket;
			
			System.setProperty(propertyName, Integer.toString(port));
		}
		
		public void stop() {
			System.clearProperty(propertyName);
			
			this.port = -1;
			
			release();
		}

		public void start(Set<Integer> reserved) {
			// check if the property already exists, if so then reserve capture that port
			String property = System.getProperty(propertyName);
			if(property != null) {
				if(reserve(reserved, Integer.parseInt(property))) {
					return;
				} else {
					throw new RuntimeException("Unable to reserve previously set port " + property + " for port name " + propertyName);
				}
			}
			// systematically try ports in range
			// starting at random offset
			int portRange = portRangeEnd - portRangeStart + 1;
			
			int offset = new Random().nextInt(portRange);

			for(int i = 0; i < portRange; i++) {
				int candidatePort = portRangeStart + (offset + portRange) % portRange;
				if(reserved.contains(candidatePort)) {
					continue;
				}
				if(reserve(reserved, candidatePort)) {
					return;
				}
			}
			throw new RuntimeException("Unable to reserve port for " + propertyName);
			
		}

		private boolean reserve(Set<Integer> reserved, int candidatePort) {
			try {
				ServerSocket result = capturePort(candidatePort);
				if(result != null) {
					reserved(candidatePort, result);
					
					reserved.add(candidatePort);
					
					return true;
				}
			} catch(Exception e) {
				// continue
			}
			return false;
		}

		public int getPort() {
			return port;
		}

		public String getPropertyName() {
			return propertyName;
		}
		
		public boolean capture() {
			if(serverSocket == null) {
				serverSocket = capturePort(port);
			}
			
			return serverSocket != null;
		}
		
		public void release() {
			ServerSocket serverSocket = this.serverSocket;
			if(serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					// ignore
				}
				
				this.serverSocket = null;
			}
		}
		
	}   
	
	protected static ServerSocket capturePort(int port) {
		try {
			return ServerSocketFactory.getDefault().createServerSocket(port, 1, InetAddress.getByName("localhost"));
		}
		catch (Exception ex) {
			return null;
		}
	}

	public void start() throws Exception {
		// reserve ports for all ports 
		Set<Integer> reserved = new HashSet<>();
		for(PortReservation reservation : reservations) {
			reservation.start(reserved);
		}
	}

	public void stop() {
		for(PortReservation reservation : reservations) {
			reservation.stop();
		}
	}
	
	public void release() {
		for(PortReservation reservation : reservations) {
			reservation.release();
		}
	}
	
	public void capture() {
		for(PortReservation reservation : reservations) {
			reservation.capture();
		}
	}

}
