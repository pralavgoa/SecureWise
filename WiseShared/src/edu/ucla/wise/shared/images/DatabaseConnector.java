package edu.ucla.wise.shared.images;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.wise.shared.properties.WiseSharedProperties;

public class DatabaseConnector {

	private static final Logger log = Logger.getLogger(DatabaseConnector.class);

	WiseSharedProperties properties = new WiseSharedProperties();



	static {
		try {
			Class.forName(WiseSharedProperties.DATABASE_DRIVER_NAME)
					.newInstance();
			log.info("*** Driver loaded");
		} catch (Exception e) {
			log.error("*** Error : ", e);
		}

	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(WiseSharedProperties.DATABASE_URL,
				WiseSharedProperties.getDatabaseUsername(),
				WiseSharedProperties.getDatabasePassword());
	}

	public static Connection getConnection(String databaseName)
			throws SQLException {
		return DriverManager.getConnection(WiseSharedProperties.DATABASE_URL
				+ databaseName, WiseSharedProperties.getDatabaseUsername(),
				WiseSharedProperties.getDatabasePassword());
	}

	public static InputStream getImageFromDatabase(String imageName) {
		return getImageFromDatabase(imageName, null);
	}

	public static InputStream getImageFromDatabase(String imageName,
			String studyName) {
		Connection conn = null;
		PreparedStatement pstmnt = null;
		InputStream is = null;

		try {
			conn = getConnection("wise_shared");
			String querySQL = "SELECT filecontents FROM images WHERE filename = '"
					+ imageName + "'";

			if (studyName != null) {
				querySQL = querySQL + " AND studyname='" + studyName + "'";
			}

			pstmnt = conn.prepareStatement(querySQL);
			ResultSet rs = pstmnt.executeQuery();

			while (rs.next()) {
				is = rs.getBinaryStream(1);
			}
		} catch (SQLException e) {
			log.error("Error while retrieving file from database", e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return is;
	}

	public static InputStream getStyleSheetFromDatabase(String stylesheetName,
			String studyName) {
		Connection conn = null;
		PreparedStatement pstmnt = null;
		InputStream is = null;

		try {
			conn = getConnection("wise_shared");
			String querySQL = "SELECT filecontents FROM stylesheets WHERE filename = '"
					+ stylesheetName + "'";

			if (studyName != null) {
				querySQL = querySQL + " AND studyname='" + studyName + "'";
			}

			pstmnt = conn.prepareStatement(querySQL);
			ResultSet rs = pstmnt.executeQuery();

			while (rs.next()) {
				is = rs.getBinaryStream(1);
			}
		} catch (SQLException e) {
			log.error("Error while retrieving file from database", e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return is;
	}

	public static List<String> getNamesOfImagesInDatabase() {

		List<String> listOfImageNames = new ArrayList<String>();
		
		Connection conn = null;
		PreparedStatement pstmnt = null;
		
		try{
			conn = getConnection("wise_shared");
			String querySQL = "SELECT filename FROM images";
			pstmnt = conn.prepareStatement(querySQL);
			ResultSet rs = pstmnt.executeQuery();
			
			while(rs.next()){
				listOfImageNames.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return listOfImageNames;
	}

}
