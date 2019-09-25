package com.trustcommerce.ipa.dal.device.emv;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.device.IngenicoUtil;
import com.trustcommerce.ipa.dal.emvconstants.EmvTags;
import com.trustcommerce.ipa.dal.emvconstants.EmvTagsConst;

public class EMVDataHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(EMVDataHandler.class);

	// private static final Map<String, String> map = new HashMap<String,
	// String>();

	private static final List<String> constructConfirmRespTags = new ArrayList<String>();
	private static final List<String> loggableTags = new ArrayList<String>();

	static {
		// map.put("4F", "emv_4f_applicationidentifiericc");
		// map.put("50", "emv_50_applicationlabel");
		//
		// map.put("57", "track2");
		// map.put("82", "emv_82_applicationinterchangeprofile");
		// map.put("84", "emv_84_dedicatedfilename");
		// map.put("8A", EmvTagsConst.EMV_8A);
		// map.put("8E", "emv_8e_cardholderverificationmethodlist");
		//
		// map.put("95", "emv_95_terminalverificationresults");
		// map.put("9A", "emv_9a_transactiondate");
		// map.put("9B", "emv_9b_transactionstatusinformation");
		// map.put("9C", "emv_9c_transactiontype");
		//
		// map.put("5F20", "name");
		// map.put("5F24", "emv_5f24_applicationexpirationdate");
		// map.put("5F28", "emv_5f28_issuercountrycode");
		// map.put("5F2A", "emv_5f2a_transactioncurrencycode");
		// map.put("5F2D", "emv_5f2d_languagepreference");
		// map.put("5F30", "emv_5f30_servicecode");
		// map.put("5F34", "emv_5f34_cardsequenceterminalnumber");
		// map.put("5F39", "emv_5f39_posentrymode");
		//
		// map.put("9F02", EmvTagsConst.EMV_9F02);
		// map.put("9F03", "emv_9f03_amountother");
		// map.put("9F06", "emv_9f06_applicationidentifierterminal");
		// map.put("9F07", "emv_9f07_applicationusagecontrol");
		// map.put("9F08", "emv_9f08_applicationversionnumbericc");
		// map.put("9F09", "emv_9f09_applicationversionnumberterminal");
		// map.put("9F0D", "emv_9f0d_issueractioncodedefault");
		// map.put("9F0E", "emv_9f0e_issueractioncodedenial");
		// map.put("9F0F", "emv_9f0f_issueractioncodeonline");
		// map.put("9F10", "emv_9f10_issuerapplicationdata");
		// map.put("9F11", "emv_9f11_issuercodetableindex");
		// map.put("9F12", "emv_9f12_applicationpreferredname");
		// map.put("9F14", "emv_9f14_lowerconsecutiveofflinelimit");
		// map.put("9F17", "emv_9f17_personalidentificationnumbertrycounter");
		// map.put("9F1A", "emv_9f1a_terminalcountrycode");
		// map.put("9F1E", "emv_9f1e_interfacedeviceserialnumber");
		// map.put("9F21", "emv_9f21_transactiontime");
		// map.put("9F26", "emv_9f26_applicationcryptogram");
		// map.put("9F27", "emv_9f27_cryptograminformationdata");
		// map.put("9F33", "emv_9f33_terminalcapabilities");
		// map.put("9F34", "emv_9f34_cardholderverificationmethodresults");
		// map.put("9F35", "emv_9f35_terminaltype");
		// map.put("9F36", "emv_9f36_applicationtransactioncounter");
		// map.put("9F37", "emv_9f37_unpredictablenumber");
		// map.put("9F39", "emv_9f39_posentrymode");
		// map.put("9F40", "emv_9f40_additionalterminalcapabilities");
		// map.put("9F41", "emv_9f41_transactionsequencecounter");
		// map.put("9F53", "emv_9f53_transactioncategorycode");
		//
		// map.put("DF03", "emv_tac_default");
		// map.put("DF04", "emv_tac_denial");
		// map.put("DF05", "emv_tac_online");

		// map.put("FF21", "encryptedtrack");
		// map.put("DF21", "encryptedtrack");

		// Use for reconstruction of confirmation response on 2nd GEN AC
		constructConfirmRespTags.add("emv_50_applicationlabel");
		constructConfirmRespTags.add(EmvTags.T8A.tagName());
		constructConfirmRespTags.add(EmvTags.T9B.tagName());
		constructConfirmRespTags.add(EmvTags.T95.tagName());
		constructConfirmRespTags.add("emv_9f03_amountother");
		constructConfirmRespTags.add("emv_9f07_applicationusagecontrol");
		constructConfirmRespTags.add("emv_9f08_applicationversionnumbericc");
		constructConfirmRespTags.add("emv_9f0d_issueractioncodedefault");
		constructConfirmRespTags.add("emv_9f0e_issueractioncodedenial");
		constructConfirmRespTags.add("emv_9f0f_issueractioncodeonline");
		constructConfirmRespTags.add("emv_9f26_applicationcryptogram");
		constructConfirmRespTags.add("emv_9f27_cryptograminformationdata");
		constructConfirmRespTags.add("emv_9f37_unpredictablenumber");
		constructConfirmRespTags.add("emv_9f41_transactionsequencecounter");

		// Values of these tags are safe to display
		loggableTags.add("emv_50_applicationlabel");
		loggableTags.add(EmvTags.T8A.tagName());
		loggableTags.add("emv_95_terminalverificationresults");
		loggableTags.add("emv_9c_transactiontype");
		loggableTags.add(EmvTags.T9F02.tagName());
		loggableTags.add("emv_9f03_amountother");
		loggableTags.add("emv_9f27_cryptograminformationdata");
		loggableTags.add("emv_9f33_terminalcapabilities");
		loggableTags.add("emv_9f34_cardholderverificationmethodresults");
		loggableTags.add("emv_9f35_terminaltype");
	}

	/**
	 * Use by the IngenicoMsr to process the string return by the terminal.
	 * Called from handleEmvTransactionPreparationReady() in the DeviceBridge to
	 * process cashback. Called from processFirstApplicationCryptogram() to
	 * build the ARQC.
	 * 
	 * @param tag
	 *            String Value generated by the terminal after reading the chip.
	 * @return
	 */
	public static Map<String, String> parseEMVDataMessage(String tag) {
		LOGGER.debug("-> parseEMVDataMessage() ");
		String[] tagArray = tag.substring(1, tag.length()).split(EmvTagsConst.EMV_DELIM);
		Map<String, String> tagMap = new HashMap<String, String>();

		// At this stage, just collect the data so we don't have to worry about
		// dealing with relying on certain data being available to process a
		// given EMV tag
		for (String tagData : tagArray) {
			// LOGGER.debug(" tagData: {}", tagData);
			if (tagData.equalsIgnoreCase("null")) {
				// it is possible to see a "null" embedded in the String ...
				continue;
			}
			if (tagData.isEmpty()) {
				LOGGER.warn(String.format("Received 0-length tag data"));
				continue;
			}
			String[] tagInfo = tagData.split(":", 3);
			// String tagNum = tagInfo[0].substring(1, tagInfo[0].length());
			// String tagName = map.get(tagNum);
			EmvTags tag0 = null;
			try {
				tag0 = EmvTags.valueOf(tagInfo[0]);
			} catch (IllegalArgumentException e) {
				LOGGER.debug(" tagData not processed: {}", tagData);
				continue;
			}
			String tagName = tag0.tagName();
			if (tagName.isEmpty()) {
				LOGGER.warn("tag : {} is ignored ", tag0.name());
				continue;
			}
			String tagValue = "";
			// TODO
			if (tagInfo.length > 2) {
				// Added by Luisa: getting java.lang.ArrayIndexOutOfBoundsException: 2
				tagValue = tagInfo[2].substring(1, tagInfo[2].length());
			}
			if (tagName != null) {
				// 20180905-jb: 19.2.6 path for HEX-formatted string
                if(tagName.equals("emv_9f12_applicationpreferredname"))
                {
                	// tagValue first character is either 'a' (ASCII) or 'h' (HEX)
                	if(tagInfo[2].substring(0, 1).equals("h"))
                	{
                		// Check tagValue length
                		if(tagValue.length() / 2 == Integer.parseInt(tagInfo[1], 16))
                		{
                			tagValue = IngenicoUtil.hexToAscii(tagValue);
                			LOGGER.debug("Modified tag value : {} : xxxxxx ", tagName);
                		}
                	}
                }
                tagMap.put(tagName, tagValue);
				if (loggableTags.contains(tagName)) {
					LOGGER.debug("Added tag : {} : {}", tagName, tagValue);
				} else {
					LOGGER.debug("Added tag : {} : xxxxxx ", tagName);
				}
			}
		}
		return tagMap;
	}

	/**
	 * 
	 * @param dataString
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Map<String, String> emvDataToJSONString(final String dataString) throws IllegalArgumentException {

		LOGGER.debug("-> emvDataToJSONString dataString:");
		Map<String, String> reqMap = parseEMVDataMessage(dataString);

		if (reqMap.containsKey(GlobalConstants.TRACK2)) {
			String val = reqMap.get(GlobalConstants.TRACK2);
			val = val.replace('D', '=');
			reqMap.put(GlobalConstants.TRACK2, ';' + val + '?');

			// Ingenico-special flag for encrypted tag 57
			if (reqMap.containsKey("encryptedtrack")) {
				LOGGER.debug("Generating encrypted track value");
				reqMap.put("encryptedtrack", "|" + val + "|" + reqMap.get("encryptedtrack"));
				reqMap.remove(GlobalConstants.TRACK2);
			}
		}

		return reqMap;
	}

	private static Map<String, String> constructJsonData(Map<String, String> sources, List<String> selectors) {
		Map<String, String> retMap = new HashMap<String, String>();
		for (String tag : selectors) {
			if (!sources.containsKey(tag))
				continue;
			retMap.put(tag, sources.get(tag));
		}
		return retMap;
	}

	/**
	 * Data use to call TcLink.
	 * 
	 * @param arqc
	 * @return
	 */
	public static String constructGetData(Map<String, String> arqc) {

		StringBuilder sb = new StringBuilder();
		Iterator it = arqc.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			sb.append("&");
			try {
				sb.append(URLEncoder.encode((String) pair.getKey(), "UTF-8"));
				sb.append("=");
				sb.append(URLEncoder.encode((String) pair.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			it.remove(); // avoids a ConcurrentModificationException
		}
		return sb.toString();
	}

	/**
	 * Use for EMV invalid data.
	 * 
	 * @param arqc
	 * @param arpc
	 * @return
	 */
	public static Map<String, String> constructConfirmRespData(Map<String, String> arqc, Map<String, String> arpc) {
		Map<String, String> combinedMap = new HashMap<String, String>(arqc);
		combinedMap.putAll(arpc);
		return constructJsonData(combinedMap, constructConfirmRespTags);
	}

}
