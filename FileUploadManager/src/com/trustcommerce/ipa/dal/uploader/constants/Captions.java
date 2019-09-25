package com.trustcommerce.ipa.dal.uploader.constants;

import java.awt.Color;
import java.io.File;

import com.trustcommerce.ipa.dal.configuration.client.ClientConfigurationUtil;

public class Captions {

    // GUI
	/** Title for the frame.*/
    public static final String GUI_PROCESS_PAYMENT_TITLE = "TC IPA - File Upload Manager   v. ";
    
    /** Setting height of the frame.*/
    public static final int FRAME_HEIGHT = 500;
    /** Setting width of the frame.*/

    public static final int FRAME_WIDTH = 500;
    /** width of the user panel and signature panel. */
    public static final int CENTER_PANEL_WIDTH = 250;
    
    /** Icon to be shown on the frame.*/
    public static final String ICON = "TC-IPA-Shield.png";
    
    /** Icon to be shown on the frame.*/
    public static final String PROGRESSBAR = "ProgressBar.gif";
    
    

    /**Image Logo to be shown on the frame. */
    public static final String LOGO = "TC-IPA-Logo.png";
    // public static final String LOGO = "BLOGO.GIF";

    /** Make it configurable. */
    public static final String JDAL_FOLDER = "jDAL";
    
    /** Setting the background color for the frame.*/
    
    public static final Color BG_COLOR = Color.WHITE;

    // EpicConfiguration.getTCIPAHome() + Paths.INGENICO_FOLDER +
    // "temp\\Dal.gif";
    /**Getting the Resource path.
     *  @return sb StringBuilder
     */
    public static final String getJdalResourcesPath() {
        final StringBuilder sb = new StringBuilder();
        sb.append(ClientConfigurationUtil.getTCIPAHome());
        sb.append(File.separator);
        sb.append(JDAL_FOLDER);
        sb.append(File.separator);
        sb.append("resources");
        sb.append(File.separator);
        return sb.toString();
    }
    /** 
     * Getting icon from the resource path.
     * @return icon
     */
    public static final String getJdalIcon() {
        return getJdalResourcesPath() + ICON;
    }

    /** 
     * Getting logo from the resource path.
     * @return logo
     */
    public static final String getTcIPALogo() {
        return getJdalResourcesPath() + LOGO;
    }
    
    public static final String getProgressIcon() {
        return getJdalResourcesPath() + PROGRESSBAR;
    }

}
