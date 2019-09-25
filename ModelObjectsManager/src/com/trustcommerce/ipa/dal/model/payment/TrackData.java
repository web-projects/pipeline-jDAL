package com.trustcommerce.ipa.dal.model.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.model.exceptions.BadDataException;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class TrackData {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(TrackData.class);

	protected String track1Data;

	protected String track2Data;
	// Track 3 in the actual encrypted track
	protected String track3Data;

	protected String encryptedTracks;

	/**
	 * Data comes from the IngenicoMSR.
	 * 
	 * @param track1
	 * @param track2
	 * @param track3
	 */
	public final void setTracks(final String track1, final String track2, final String track3) {
		track1Data = track1;
		track2Data = track2;
		track3Data = track3;
		setEncryptedTracksFormat();
	}

	private void setEncryptedTracksFormat() {
		final StringBuilder sb = new StringBuilder();
		sb.append(track1Data);
		sb.append("|");
		sb.append(track2Data);
		sb.append("|");
		sb.append(track3Data);
		encryptedTracks = sb.toString();
	}



	/**
	 * The encrypted tracks (1, 2 and 3), bar delimited, that are not saved to
	 * DB (PCI)
	 * 
	 * @return
	 */
	public final String getEncryptedTracks() {
		return encryptedTracks;
	}
	

	/**
	 * returns the mechanism of how the encrypted track been composed 1 : track
	 * 1 only 2 : track 2 only 3 : manual entry 4 : track 1 and track 2
	 * 
	 * @param String
	 *            encryptedTrack
	 * @param String
	 *            track1
	 * @param String
	 *            track2
	 * @return boolean
	 * @throws BadDataException
	 */
	public final void validateTracks() throws BadDataException {
		final String[] encryptedTrackInfo = track3Data.split(":");
		switch (Integer.parseInt(encryptedTrackInfo[1])) {
		case 1:
			if (track2Data.isEmpty()) {
				throw new BadDataException("Track 2 is Missing or damage in this card");
			}
			break;
		case 2:
			if (track1Data.isEmpty()) {
				// Debit cards seem to have the track 1 unavailable .... so this
				// should be a warning?
				LOGGER.warn("Track 1 is Missing or damage in this card, is this a DEBIT card?");
			}
			break;
		case 3:
			break;
		case 4:
			if (track2Data.isEmpty() || track1Data.isEmpty()) {
				throw new BadDataException("Track 2 is Missing or damage in this card");
			}
			break;
		default:
			break;
		}
	}


	/**
	 * Encrypted track data not saved to DB(PCI). This data is not generated in
	 * EMV transactions.
	 * 
	 * @return String
	 */
	public final String getTrack1Data() {
		if (track1Data == null) {
			return "";
		}
		return track1Data;
	}

	/**
	 * EMV.
	 * 
	 * @param value
	 *            String
	 */
	@JsonProperty("Track1Data")
	public final void setTrack1(final String value) {
		this.track1Data = value;
	}

	/**
	 * Encrypted track data not saved to DB(PCI).
	 * 
	 * @return String
	 */
	public final String getTrack2Data() {
		if (track2Data == null) {
			return "";
		}
		return track2Data;
	}

	
	/**
	 * EMV.
	 * 
	 * @param value
	 *            String
	 */
	@JsonProperty("Track2Data")
	public final void setTrack2(final String value) {
		this.track2Data = value;
	}
	
	
	/**
	 * Encrypted track data not saved to DB(PCI).
	 * 
	 * @return
	 */
	public String getTrack3Data() {
		if (track3Data == null) {
			return "";
		}
		return track3Data;
	}
	
	/**
	 * EMV.
	 * 
	 * @param value
	 *            String
	 */
	@JsonProperty("Track3Data")
	public final void setTrack3(final String value) {
		this.track3Data = value;
	}
	
	/**
	 * EMV.
	 * 
	 * @param value
	 *            String
	 */
	@JsonProperty("EncryptedTracks")
	public final void setEncryptedTracks(final String value) {
		this.encryptedTracks = value;
	}
	

	public String tracksToString() {
		// C# components should receive empty string instead of null
		return "\"Track1Data\" : \"" + getTrack1Data() + "\"," + "\"Track2Data\" : \"" + getTrack2Data() + "\","
		        + "\"Track3Data\" : \"" + getTrack3Data() + "\"," + "\"EncryptedTracks\" : \"" + encryptedTracks + "\",";
	}
	
	
	/**
	 * Use to remove unused data for EMV transactions.
	 * TODO improve, this fields should be set on EMV
	 */
	public final void cleanUpForEMV() {
		track1Data = "";
		track2Data = "";
		track3Data = "";
		encryptedTracks = "";
	}

}
