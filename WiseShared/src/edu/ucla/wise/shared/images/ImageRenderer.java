package edu.ucla.wise.shared.images;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

public class ImageRenderer extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Logger log = Logger.getLogger(ImageRenderer.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();

			String imageName = request.getParameter("img");

			if (Strings.isNullOrEmpty(imageName)) {
				out.write("Please provide a name for the image");
				out.close();
				return;
			}

			String studySpaceName = request.getParameter("study");

			InputStream imageInputStream;

			if (Strings.isNullOrEmpty(studySpaceName)) {

				// return an image
				imageInputStream = DatabaseConnector
						.getImageFromDatabase(imageName);
			} else {
				imageInputStream = DatabaseConnector.getImageFromDatabase(
						imageName, studySpaceName);
				if (imageInputStream == null) {
					imageInputStream = DatabaseConnector
							.getImageFromDatabase(imageName);
				}
			}

			int buffer_size = 2 << 3;

			if (imageInputStream != null) {
				response.reset();
				response.setContentType("image/gif");
				int count = 0;// initializing to a value > 0
				while (count >= 0) {
					byte[] byte_buffer = new byte[buffer_size];
					count = imageInputStream.read(byte_buffer, 0, buffer_size);
					response.getOutputStream().write(byte_buffer, 0,
							buffer_size);
				}
				response.getOutputStream().flush();
			} else {
				log.error("Cound not fetch the image " + imageName);
			}

		} catch (IOException e) {
			log.error("Unexpected IO error", e);
		}

	}

}
