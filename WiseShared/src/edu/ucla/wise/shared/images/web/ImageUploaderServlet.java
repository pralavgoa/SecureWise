package edu.ucla.wise.shared.images.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

public class ImageUploaderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(ImageUploaderServlet.class);

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (isMultipart) {
			System.out.println("The request is a multipartrequest");
		} else {
			System.out.println("The request is not a multipart request");
		}

		String applicationRootPath = this.getServletContext().getRealPath("/");

		System.out.println(applicationRootPath);

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		try {
			List /* FileItem */items = upload.parseRequest(request);

			System.out.println("There are " + items.size() + " items");
			System.out.println("Items: " + items);

			Iterator itemIterator = items.iterator();

			while (itemIterator.hasNext()) {
				FileItem item = (FileItem) itemIterator.next();
				if (item.isFormField()) {
					String name = item.getFieldName();
					System.out.println("name: " + name);
					String value = item.getString();
					System.out.println("value: " + value);
				} else {
					System.out.println("Item is not a form field");
				}
			}

			PrintWriter out = response.getWriter();

			out.write("Dummy response: Detected 3 faces");
		} catch (FileUploadException e) {
			System.out.println("Exceptions parsing request");
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.out.println("Runtime exception");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("End of servlet call");


	}

}
