package edu.ucla.wise.shared.images;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertImages {

	public static boolean insertImagesFromFolder(String folderPath)
			throws SQLException {

		PreparedStatement psmnt = null;
		FileInputStream fis;
		Connection connection = null;

		try {

			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/wise_shared", "root",
					"d4t4b4se!");

			File folder = new File(folderPath);
			File[] listOfFiles = folder.listFiles();
			
			for (File file : listOfFiles) {
				psmnt = connection
						.prepareStatement("INSERT INTO wise_shared.images(filename,filecontents)"
								+ "VALUES (?,?)");
				psmnt.setString(1, file.getName());
				fis = new FileInputStream(file);
				psmnt.setBinaryStream(2, fis, (int) (file.length()));

				int s = psmnt.executeUpdate();

				if (s > 0) {
					System.out.println("Upload Successful:" + file.getName());
				} else {
					System.out.println("Unsuccessful to upload:"
							+ file.getName());
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			connection.close();
			psmnt.close();
		}
	}

	public static void main(String[] args) {
		try {
			insertImagesFromFolder("C:\\_GSR_Related\\SurveysLaunched\\CERP2\\images\\");
		} catch (SQLException e) {
			System.out.println("SQL exception prevented image insert");
		}
	}
}
