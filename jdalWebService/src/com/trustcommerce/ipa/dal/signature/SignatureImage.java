package com.trustcommerce.ipa.dal.signature;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.trustcommerce.ipa.dal.constants.device.TerminalModel;
import com.trustcommerce.ipa.dal.constants.global.GlobalConstants;
import com.trustcommerce.ipa.dal.constants.paths.TcipaFiles;

public class SignatureImage {

	private static final Logger logger = Logger.getLogger(SignatureImage.class);

	

	private Point[] sigPoints;

	private TerminalModel deviceName;
	/** imageAsHex will be saved in the database. */
	private String imageAsHex;
	
	private Graphics2D graphic;

	private Point sigDimension;
	
	private BufferedImage bufferedImage;
	

	public SignatureImage(final TerminalModel deviceName, Point[] sigPoints) {
		// sigPoints = signaturePoints;
		this.deviceName = deviceName;
		this.sigPoints = sigPoints;

		doDrawing();
	}
	
	public Graphics2D getGraphic() {
		return graphic;
	}
	
	public TerminalModel getTerminalModel() {
		return deviceName;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public String getImageAsHex() {
		return imageAsHex;
	}

	public void doDrawing() {
		logger.debug("-> doDrawing()");
		sigDimension = SignatureConstants.SURFACE_DIMENSION;

		float stroke = 0;
		if (deviceName == TerminalModel.iSC250) {
			stroke = 1.7f;
			sigDimension = new Point(410, 75);
		} else if (deviceName == TerminalModel.iSC480) {
			stroke = 3.4f;
			sigDimension = new Point(685, 150);
		}
		final BufferedImage sigImage = new BufferedImage(sigDimension.x, sigDimension.y, BufferedImage.TYPE_BYTE_GRAY);
		graphic = (Graphics2D) sigImage.getGraphics();
		if (stroke != 0) {
			graphic.setStroke(new BasicStroke(stroke));
		}

		// Graphics2D graphic = (Graphics2D) g;

		// Fills the space
		graphic.setColor(Color.WHITE);
		graphic.fillRect(0, 0, sigDimension.x, sigDimension.y); // 600 x 200

		graphic.setPaint(Color.blue);

		for (int i = 1; i < sigPoints.length; i++) {
			final Point prevPoint = sigPoints[i - 1];
			Point curPoint = sigPoints[i];

			if (curPoint.x == -1 && curPoint.y == -1) {
				curPoint = prevPoint;
			} else if (prevPoint.x == -1 && prevPoint.y == -1) {
				// logger.debug("Skipping previous point");
			} else {
				// logger.debug(" Draw line from " + prevPoint.toString() + " to
				// " + curPoint.toString());
				graphic.drawLine(prevPoint.x, prevPoint.y, curPoint.x, curPoint.y);
			}
		}
		try {
			//String tempImageAsHex;
			if (deviceName == TerminalModel.iSC480) {
				// resize the iSC480 signature size
				bufferedImage = resizeImage(sigImage, sigDimension, SignatureConstants.SURFACE_DIMENSION);
			} else {
				bufferedImage = sigImage;
			}
			if (GlobalConstants.LOCAL_TEST) {
				// Write the signature to the log folder
				final File saveFile = new File(TcipaFiles.getLogsPath() + "\\" + TcipaFiles.IMAGE_NAME);
				ImageIO.write(bufferedImage, TcipaFiles.IMAGE_EXTENSION, saveFile);
			}

			imageAsHex = getSignatureImageAsHex();
			logger.trace(imageAsHex);

		} catch (IOException e) {
			
		}
		logger.debug("<- doDrawing()");
	}

	/**
	 * takes an image and scales it based on the required dimensions.
	 * 
	 * @param image
	 *            the image to be resized
	 * @param original
	 *            the original dimensions of the image
	 * @param required
	 *            the resized image dimensions
	 * @return image
	 */
	private BufferedImage resizeImage(final BufferedImage image, final Point original, final Point required) {
		logger.debug("-> resizeImage()");
		BufferedImage scaledSig = null;
		if (image != null) {
			scaledSig = new BufferedImage(required.x, required.y, BufferedImage.TYPE_BYTE_GRAY);
			final Graphics2D g = scaledSig.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			final float scaleX = ((float) required.x) / original.x;
			final float scaleY = ((float) required.y) / original.y;
			final int newWidth = (int) (original.x * scaleX);
			final int newHeight = (int) (original.y * scaleY);
			g.drawImage(image, 0, 0, newWidth, newHeight, null);
		}
		return scaledSig;
	}

	/**
	 * Returns the signature in a Hex format.
	 * @param bufferedImage BufferedImage
	 * @return strBuf
	 * @throws IOException Exception
	 */
	private String getSignatureImageAsHex() throws IOException {
		logger.debug("-> getSigImageHex()");
		if (bufferedImage == null) {
			logger.error("bufferedImage is NULL");
			return null;
		}
		BufferedImage image = null;
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, TcipaFiles.IMAGE_EXTENSION, outStream);
		final StringBuffer strBuf = new StringBuffer();
		logger.debug("Signature Size Before Reducing" + outStream.size());

		if (outStream.size() > 4500) {

			final double percentage = calculateSignatureSize(outStream.size());

			image = signatureResize(bufferedImage, sigDimension, SignatureConstants.SURFACE_DIMENSION,
			        percentage);
			outStream = new ByteArrayOutputStream();
			ImageIO.write(image, TcipaFiles.IMAGE_EXTENSION, outStream);

			logger.debug("Signature Size After Reducing" + outStream.size());
		}
		for (byte b : outStream.toByteArray()) {
			int val = (int) b;
			if (val < 0) {
				val += 256;
			}
			if (val < 16) {
				strBuf.append("0");
			}
			strBuf.append(Integer.toHexString(val));
		}
		return strBuf.toString();
	}

	/**
	 * Returns the resized signature , if the size is more than 4500.
	 * 
	 * @param image BufferedImage
	 * @param original Point
	 * @param required Point
	 * @param percentage is the amount upto which the signature needs to be reduced
	 * @return scaledSig
	 */
	private BufferedImage signatureResize(final BufferedImage image, final Point original, final Point required, 
			final double percentage) {
		logger.debug("-> signatureResize()");
		BufferedImage scaledSig = null;
		if (image != null) {

			final double width = SignatureConstants.SURFACE_DIMENSION.x / percentage;
			final double height = SignatureConstants.SURFACE_DIMENSION.y / percentage;
			BigDecimal bdWidth;
			BigDecimal bdHeight;
			bdWidth = new BigDecimal(width);

			bdWidth = bdWidth.setScale(0, RoundingMode.FLOOR);
			bdHeight = new BigDecimal(height);

			bdHeight = bdHeight.setScale(0, RoundingMode.FLOOR);

			final int newWidth = bdWidth.intValueExact();
			final int newHeight = bdHeight.intValueExact();
			scaledSig = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);

			final Graphics2D g = scaledSig.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

			g.drawImage(image, 0, 0, newWidth, newHeight, null);

		}

		return scaledSig;

	}

	/**
	 * Calculate the percentage by which the size needs to be reduced.
	 * 
	 * @param size int
	 * @return percentage
	 */
	private double calculateSignatureSize(final int size) {

		final double percentage = (double) size / SignatureConstants.MAX_SIZE;

		logger.debug("Percent decreased " + percentage);
		return percentage;

	}

}
