package com.trustcommerce.ipa.dal.constants.gui;

import java.io.File;

import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;


/**
 * 
 * @author luisa.lamore
 *
 */
public class CommonGui {

    /** Icon to be shown on the frame.*/
    public static final String ICON = "TC-IPA-Shield.png";

    /**Image Logo to be shown on the frame. */
    public static final String LOGO = "TC-IPA-Logo.png";
    // public static final String LOGO = "BLOGO.GIF";

    /** Make it configurable. */
    public static final String JDAL_FOLDER = "jDAL";

    /** .*/
	private static final String TCIPA_HOME = GlobalConstants.DEFAULT_TC_HOME;
	

    
	/**
	 * Getting the Resource path.
	 * 
	 * @return sb StringBuilder
	 */
	public static final String getJdalResourcesPath() {
		final StringBuilder sb = new StringBuilder();
		sb.append(TCIPA_HOME);
		sb.append(File.separator);
		sb.append(JDAL_FOLDER);
		sb.append(File.separator);
		sb.append("resources");
		sb.append(File.separator);
		return sb.toString();
	}
	/**
	 * Getting icon from the resource path.
	 * 
	 * @return icon
	 */
	public static final String getJdalIcon() {
		return getJdalResourcesPath() + ICON;
	}

	/**
	 * Getting logo from the resource path.
	 * 
	 * @return logo
	 */
	public static final String getTcIPALogo() {
		return getJdalResourcesPath() + LOGO;
	}
	
	
    /** Setting height of the frame.*/
    public static final int FRAME_HEIGHT = 500;
    /** Setting width of the frame.*/

    public static final int FRAME_WIDTH = 500;

}
