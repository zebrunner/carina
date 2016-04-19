package com.qaprosoft.carina.core.foundation.report.webvideorecording;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;

public class SpecializedScreenRecorder extends ScreenRecorder {

	private String name;

	/**
	 * Specialized Screen Recorder extend ScreenRecorder from 'monte' library 
	 * @param cfg GraphicsConfiguration
	 * @param captureArea Rectangle
	 * @param fileFormat Format
	 * @param screenFormat Format
	 * @param mouseFormat Format
	 * @param audioFormat Format
	 * @param movieFolder File
	 * @param name File
	 * @throws IOException
	 * @throws AWTException
	 */
	public SpecializedScreenRecorder(GraphicsConfiguration cfg,
			Rectangle captureArea, Format fileFormat, Format screenFormat,
			Format mouseFormat, Format audioFormat, File movieFolder,
			String name) throws IOException, AWTException {
		super(cfg, captureArea, fileFormat, screenFormat, mouseFormat,
				audioFormat, movieFolder);
		this.name = name;
	}

	@Override
	/**
	 * createMovieFile with specific file format
	 */
	protected File createMovieFile(Format fileFormat) throws IOException {
		if (!movieFolder.exists()) {
			movieFolder.mkdirs();
		} else if (!movieFolder.isDirectory()) {
			throw new IOException("\"" + movieFolder + "\" is not a directory.");
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");

		return new File(movieFolder, name + "_" + dateFormat.format(new Date())
				+ "." + Registry.getInstance().getExtension(fileFormat));
	}

}
