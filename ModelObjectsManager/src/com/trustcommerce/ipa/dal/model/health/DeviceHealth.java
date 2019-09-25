package com.trustcommerce.ipa.dal.model.health;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the health data received from an Ingenico Device.
 * @author luisa.lamore
 *
 */
public class DeviceHealth {
/** log4j logger.*/
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceHealth.class);

	/** Version of the firmware. */
	private String appVers = "";
	/** Serial number of the device. */
	private String serialNo;
	/** model no: of device.*/
	private String model;
	/** model reference no: of device. Firmware 16.0 and above */
	private String modelReference = "";
	/**Manufacturer name.*/
	private String manufName;
	/** manufacturing data.*/
	private String manufDate;
	/** */
	private String appName;
	/** firmware version.*/
	private String teliumVersion = "";
	/** */
	private String interfaceType = "";
	/** */
	private String bootCount = "";
	/** */
	private String upTime = "";
	/** */
	private String msrSwipes = "";
	/** */
	private String msrErrorsTrack1 = "";
	/** */
	private String msrErrorsTrack2 = "";
	/** */
	private String msrErrorsTrack3 = "";
	/** */
	private String resets = "";
	/** */
	private String signatureReads = "";
	/** */
	private String contactlessReads = "";
	/** */
	private String contactlessErrors = "";
	/** */
	private String osVersion = "";
	
	private String originalSerialValue;

	/**
	 * 
	 * @param healthData String
	 */
	public DeviceHealth(final String healthData) {
		parseHealthData(healthData);
	}


	/**
	 * Return the version of the Terminal Firmware.
	 * @return String
	 */
	public final String getApplicationVersion() {
		return appVers;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getSerialNo() {
		return serialNo;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getModel() {
		return model;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getModelReference() {
		return modelReference;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getManufName() {
		return manufName;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getManufDate() {
		return manufDate;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getAppName() {
		return appName;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getTeliumVersion() {
		return teliumVersion;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getInterfaceType() {
		return interfaceType;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getBootCount() {
		return bootCount;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getUpTime() {
		return upTime;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getMsrSwipes() {
		return msrSwipes;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getMsrErrorsTrack1() {
		return msrErrorsTrack1;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getMsrErrorsTrack2() {
		return msrErrorsTrack2;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getMsrErrorsTrack3() {
		return msrErrorsTrack3;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getResets() {
		return resets;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getSignatureReads() {
		return signatureReads;
	}
	/**
	 * 
	 * @return String
	 */
	public final String getContactlessReads() {
		return contactlessReads;
	}
	
    /**
     * 
     * @return String
     */
	public final String getContactlessErrors() {
		return contactlessErrors;
	}
	
	/**
	 * 
	 * @return String
	 */
	public final String getOsVersion() {
		return osVersion;
	}
	
	public String getOriginalSerialValue() {
		return originalSerialValue;
	}

	/**
	 * 
	 * @param healthData String
	 */

	private void parseHealthData(String healthData) {
		final String[] healthParms = healthData.split("\\n");

		for (int i = 0; i < healthParms.length; i++) {
			final String[] nameValue = healthParms[i].split("=");
			final String name = nameValue[0];
			if (nameValue.length < 2) {
				continue;
			}
			final String value = nameValue[1];
			if (name.equals("APP_VERS")) {
				appVers = value;
			} else if (name.equals("APP_BIN_NAME")) {
				appName = value;
			} else if (name.equals("TERM_SERIAL#")) {
				originalSerialValue = value;
				serialNo = value;
			} else if (name.equals("MODEL")) {
				model = value;
			} else if (name.equals("MODEL_REFERENCE#")) {	
				modelReference = value;
			} else if (name.equals("MANUF_DATE")) {
				manufDate = value;
			} else if (name.equals("MANUF_NAME")) {
				manufName = value;
			} else if (name.equals("OS_VERSION")) {
				osVersion = value;
			} else if (name.equals("TELIUM_VERS")) {
				teliumVersion = value;
			} else if (name.equals("HOST_CONFIG")) { // Also see HOST_PORT
				interfaceType = value;
			} else if (name.equals("STARTUPS")) {
				bootCount = value;
			} else if (name.equals("UP_TIME")) {
				upTime = value;
			} else if (name.equals("MSR_SWIPES1")) { // See MSR_SWIPES2,
				                                     // MSR_SWIPES3
				msrSwipes = value;
			} else if (name.equals("MSR_ERROR1")) {
				msrErrorsTrack1 = value;
			} else if (name.equals("MSR_ERROR2")) {
				msrErrorsTrack2 = value;
			} else if (name.equals("MSR_ERROR3")) {
				msrErrorsTrack3 = value;
			} else if (name.equals("RESET")) {
				resets = value;
			} else if (name.equals("SIG")) { // I assume so
				signatureReads = value;
			} else if (name.equals("CLESS_READS")) {
				contactlessReads = value;
			} else if (name.equals("CLESS_ERRORS")) {
				contactlessErrors = value;
			}
		}
		
		// Correct the following values
		serialNo = updateSerialNumber(serialNo, manufDate, model);
		manufDate = formatDateMDY(manufDate);
	}

	/**Return a date/time in a format that TC-IPA webservices are happy with.
	 Sep 04 2014
	 *@param dateStr String
	 * @return String
	 */
	@SuppressWarnings("deprecation")
	private static String formatDateMDY(final String dateStr) {
		java.text.SimpleDateFormat ft = new java.text.SimpleDateFormat(
				"MMM dd yyyy");

		java.util.Date date;

		try {
			date = ft.parse(dateStr);
		} catch (Exception e) {
			date = new java.util.Date(2000, 1, 1);
			System.out.println("DATE FORMAT FAILURE! Date=" + dateStr);
		}
		ft = new java.text.SimpleDateFormat("MM/dd/yyyy");
		return ft.format(date);
	}

	/**
	 * The serial number received from the terminal is only the last section of the
	 * complete serial number. This methods build the entire serial number as it
	 * is displayed in the device.
	 * 
	 * @param termSerial String
	 * @param manufDate String
	 * @param model String
	 * @return String
	 */
	private static String updateSerialNumber(final String termSerial,
			final String manufDate, final String model) {

		assert termSerial != null;
		assert manufDate != null;
		assert model != null;
		final StringBuilder sb = new StringBuilder();
		// Manufacturer date
		String serialSection1 = null;
		try {
			serialSection1 = getYearAndDayInTheYear(manufDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb.append(serialSection1);
		sb.append(model.substring(1, 3));
		sb.append(termSerial);
		LOGGER.debug("<- updateSerialNumber() {} ", sb.toString());
		return sb.toString();
	}

	/**
	 * 
	 * @param manufDate
	 *            String with the format MANUF_DATE=Jan 02 2015
	 * @return String format: last 2 digits of the year plus the days in the
	 *         year padded with zeroes to mske 3 characters.
	 * 
	 * @throws ParseException occur whne parsing the date
	 */
	private static String getYearAndDayInTheYear(final String manufDate)
			throws ParseException {
		final StringBuilder sb = new StringBuilder();
		final String[] temp = manufDate.split(" ");
		if (temp.length == 3) {
			sb.append(temp[2].subSequence(2, 4));
			final DateFormat df = new SimpleDateFormat("MMM dd yyyy");
			final Date date = df.parse(manufDate);
			final Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			final int numberofDaysPassed = cal.get(Calendar.DAY_OF_YEAR);
			final String numberofDaysFormatted = String.format("%03d", numberofDaysPassed);
			sb.append(numberofDaysFormatted);
		}
		return sb.toString();
	}



	@Override
	public final String toString() {
	    final StringBuilder builder = new StringBuilder();
	    builder.append("DeviceHealth [appVers=");
	    builder.append(appVers);
	    builder.append(", serialNo=");
	    builder.append(serialNo);
	    builder.append(", model=");
	    builder.append(model);
	    builder.append(", manufName=");
	    builder.append(manufName);
	    builder.append(", manufDate=");
	    builder.append(manufDate);
	    builder.append(", appName=");
	    builder.append(appName);
	    builder.append(", firmwareVersion=");
	    builder.append(teliumVersion);
	    builder.append(", interfaceType=");
	    builder.append(interfaceType);
	    builder.append(", bootCount=");
	    builder.append(bootCount);
	    builder.append(", upTime=");
	    builder.append(upTime);
	    builder.append(", msrSwipes=");
	    builder.append(msrSwipes);
	    builder.append(", msrErrorsTrack1=");
	    builder.append(msrErrorsTrack1);
	    builder.append(", msrErrorsTrack2=");
	    builder.append(msrErrorsTrack2);
	    builder.append(", msrErrorsTrack3=");
	    builder.append(msrErrorsTrack3);
	    builder.append(", resets=");
	    builder.append(resets);
	    builder.append(", signatureReads=");
	    builder.append(signatureReads);
	    builder.append(", contactlessReads=");
	    builder.append(contactlessReads);
	    builder.append(", contactlessErrors=");
	    builder.append(contactlessErrors);
	    builder.append(", osVersion=");
	    builder.append(osVersion);
	    builder.append("]");
	    return builder.toString();
    }




}
