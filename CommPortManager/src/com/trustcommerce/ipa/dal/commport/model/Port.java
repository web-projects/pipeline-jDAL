package com.trustcommerce.ipa.dal.commport.model;

public class Port {

	private String name;

	private String owner;
	
	private String deviceName;
	
	private int portNumber;
	
	private int portType;
	
	private String pid;
	
	
	/**
	 * Constructor use for the Existing Com ports.
	 * @param name
	 * @param owner
	 * @param portType
	 */
	public Port(final String name, String owner, int portType) {
		this.name = name;
		this.owner = owner;
		portNumber = Integer.parseInt(name.substring(3));
		this.portType = portType;
	}
	
	
	/**
	 * Config constructor
	 * @param deviceName
	 * @param portName
	 */
	public Port(final String deviceName, String portName) {
		this.deviceName = deviceName;
		this.name = portName;
		portNumber = Integer.parseInt(name.substring(3));
		final PID id = PID.valueOf(deviceName);
		this.pid = id.getPid();
		this.owner = "Ingenico";
	}
	


	public String getName() {
		return name;
	}
	
	public String getPid() {
		return pid;
	}

	
	public int getPortNumber() {
		return portNumber;
	}

	public void setName(String name) {
		this.name = name;
		portNumber = Integer.parseInt(name.substring(3));
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}


	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public int getPortType() {
		return portType;
	}

	public void setPortType(int portType) {
		this.portType = portType;
	}

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Port [name=");
		builder.append(name);
		builder.append(", owner=");
		builder.append(owner);
		if (deviceName != null) {
			builder.append(", deviceName=");
			builder.append(deviceName);
		}
		builder.append(", portType=");
		builder.append(portType);
		if (pid != null) {
			builder.append(", pid=");
			builder.append(pid);
		}
		builder.append("]");
		return builder.toString();
	}


}
