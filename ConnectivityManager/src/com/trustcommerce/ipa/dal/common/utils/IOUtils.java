package com.trustcommerce.ipa.dal.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class contains methods that deal with input and output of data
 * either to the file system or remotely.
 * 
 * @author luisa.lamore
 *
 */
public class IOUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);


	/**
	 * This method is not used at the moment, but this is one option for the
	 * form download.
	 * 
	 * @param formurl
	 * @param formLocalName
	 * @return
	 */
	public static boolean downloadForm(String formurl, String formLocalName, int msrTimeout) {

		LOGGER.debug("-> downloadForm() " + formurl + " " + formLocalName);
		boolean formDownloadDone = false;
		try {
			// Block redirects so we only download the original form
			HttpURLConnection.setFollowRedirects(false);
			final URL url = new URL(formurl);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Set the timeouts
			connection.setConnectTimeout(msrTimeout);
			connection.setReadTimeout(msrTimeout);
			// Connect
			connection.connect();
			if (connection.getResponseCode() != 200) {
				LOGGER.warn(
				        "Got HTTP code " + connection.getResponseCode() + " not proceeding with download");
				return false;
			} else {
				LOGGER.debug("Start download");

				final InputStream in = connection.getInputStream();
				final FileOutputStream writer = new FileOutputStream(formLocalName);

				final int bufSize = 1024;
				byte buf[] = new byte[bufSize];
				int retVal;
				LOGGER.trace("Begin form download");
				do {
					retVal = in.read(buf);
					LOGGER.debug("Value of retVal: " + retVal);
					if (retVal >= 1) {
						writer.write(buf, 0, retVal);
					}
				} while (retVal != -1);

				writer.close();
				connection.disconnect();
				formDownloadDone = true;
				LOGGER.debug("Dowload complete");
			}
		} catch (MalformedURLException e) {
			LOGGER.error("URL to download form is considered malformed");

		} catch (SocketTimeoutException e) {
			LOGGER.warn("Encountered form download connection exception");
		} catch (IOException e) {
			LOGGER.error("Failed to save form locally: " + e.getMessage());
		}

		return formDownloadDone;
	}

	public static void deleteFile(final String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			// Should not happen
			return;
		}
		final File file = new File(filePath);
		file.deleteOnExit();

	}

}
