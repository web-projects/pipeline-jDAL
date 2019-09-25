package com.trustcommerce.ipa.dal.commport.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.commport.exceptions.PortConnectionException;
import com.trustcommerce.ipa.dal.commport.ports.PortConfig;

public class PortList implements Iterable<Port>, Iterator<Port> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PortList.class);

	private List<Port> list;
	
	private List<Port> portsInUse;

	private int count = 0;

	public PortList() {
		super();
		list = new ArrayList<Port>();
		portsInUse = new ArrayList<Port>();
	}

	public List<Port> list() {
		return list;
	}

	
	/**
	 * Call this method to obtain a COM port that is not in use by a non-ingenico device.
	 * @return
	 */
	public String getAvailablePort() {

		int number = getStartPort();
		boolean notFound = true;
		
		while (notFound) {
			number--;
			notFound = findPort(number);
			
		}
		return "COM" + number;
	}
	
	
	private int getStartPort() {
		List<Integer> myList = new ArrayList<Integer>();
		for (Port port : list) {
			myList.add(port.getPortNumber());
			Collections.sort(myList);
		}
		return myList.indexOf(0);
	}
	
	
	private boolean findPort(int portNumber) {
		boolean found = false;
		for (Port port : portsInUse) {
			if (port.getName().equalsIgnoreCase("COM" + portNumber)) {
				found = true;
				break;
			}
		}
		return found;
	}
	

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return list.isEmpty();
	}
	
	public int size() {
		return list.size();
	}


	public void add(Port e) {
		list.add(e);
	}

	@Override
	public final boolean hasNext() {
		if (count < list.size()) {
			return true;
		}
		return false;
	}

	@Override
	public final Port next() {
		if (count == list.size())
			throw new NoSuchElementException();

		count++;
		return list.get(count - 1);
	}

	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Iterator<Port> iterator() {
		return this;
	}

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PortList [list=");
		builder.append(list);
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * This method is use during installation to update the list of configurable ports.
	 * When using this method all ingenico devices should be unplugged.
	 * 
	 * Compares the list of the configurable ports against the list of current port connected to the
	 * work stations that are currently use by non ingenico devices.
	 * @param portsInUse
	 * @throws PortConnectionException 
	 */
	public final List<Port> updateConfigurableComPorts(final PortList deviceManagerComPorts) throws PortConnectionException {
		// Get the COM ports currently in use
		LOGGER.debug("-> updateConfigurableComPorts() ");
		if (deviceManagerComPorts.isEmpty()) {
			// Nothing to update
			return list;
		}
		portsInUse = deviceManagerComPorts.list();
		
		boolean updated = false;
		// Compare the lists ...
		for (Port port : list) {
			for (Port usePort : portsInUse) {
				if (port.getPortNumber() == usePort.getPortNumber()) {
					LOGGER.warn(port.getName() + " from our configuration is currently in use");
					final String newPort = getAvailablePort();
					port.setName(newPort);
					updated = true;
					break;
				}
			}
		}
		if (updated) {
			// the update of the properties file has to be done at this location.
			// If done 
			final PortConfig pu = new PortConfig();
			pu.saveParamChanges(list);
		}
		return list;
	}

}
