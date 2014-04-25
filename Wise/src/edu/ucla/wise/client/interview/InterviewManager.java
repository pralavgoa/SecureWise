/**
 * 
 */
package edu.ucla.wise.client.interview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.StudySpace;

/**
 * This class represents functionality around Interviewer. For. ex.
 * Add/Modify/Get/Delete an interviewer. This is a singleton class.
 * 
 * @author ssakdeo
 * @author dbell
 * @version 1.0
 */
public class InterviewManager {

    private static InterviewManager interviewManager = null;
    public static final Logger LOGGER = Logger
	    .getLogger(InterviewManager.class);

    private InterviewManager() {
    }

    /**
     * Checks if the InterviewManager class is instantiated, if yes returns the
     * it else creates a new object and returns it.
     * 
     * @return a singleton instance of {@link InterviewManager}
     */
    public synchronized static InterviewManager getInstance() {
	if (interviewManager == null) {
	    interviewManager = new InterviewManager();
	}
	return interviewManager;
    }

    /**
     * This function get Maximum ID that can be assigned to new
     * {@link Interviewer}.
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @return id string maximum ID in the database.
     */
    public synchronized String getNewId(StudySpace studySpace) {
	String id = null;
	Connection conn = null;
	PreparedStatement statement = null;
	try {
	    conn = studySpace.getDBConnection();
	    String sql = "SELECT MAX(id) from interviewer";
	    statement = conn.prepareStatement(sql);
	    ResultSet rs = statement.executeQuery();
	    if (rs.next()) {
		id = Integer.toString(rs.getInt(1) + 1);
	    }
	} catch (SQLException e) {
	    LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
	    LOGGER.error("SQL Error getting new ID", e);
	} finally {
	    if (statement != null) {
		try {
		    statement.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	}
	return id;
    }

    /**
     * Add a new interviewer by creating a new record in the interviewer table.
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @param interviewer
     *            Interview to be added to the table
     * @return id of the newly added interviewer
     */
    public synchronized String addInterviewer(StudySpace studySpace,
	    Interviewer interviewer) {
	Connection conn = null;
	PreparedStatement statement = null;
	PreparedStatement statement1 = null;
	ResultSet rs = null;
	String sql = null;
	String returnId = null;

	try {
	    conn = studySpace.getDBConnection();

	    sql = "insert into interviewer(username, firstname, lastname, salutation, email, submittime)"
		    + " values(?,?,?,?,?,?)";
	    statement = conn.prepareStatement(sql);

	    statement.setString(1, interviewer.getUserName());
	    statement.setString(2, interviewer.getFirstName());
	    statement.setString(3, interviewer.getLastName());
	    statement.setString(4, interviewer.getSalutation());
	    statement.setString(5, interviewer.getEmail());
	    statement
		    .setTimestamp(6, new Timestamp(System.currentTimeMillis()));

	    statement.executeUpdate();

	    /*
	     * Now get the ID of the last inserted value, this needs the method
	     * to be synchronized.
	     */
	    sql = "SELECT LAST_INSERT_ID() from interviewer";
	    statement1 = conn.prepareStatement(sql);

	    rs = statement1.executeQuery();
	    if ((rs != null) && rs.next()) {
		returnId = rs.getString(1);
	    }
	} catch (SQLException e) {
	    LOGGER.error("Add interviewer ID:" + e.toString(), e);
	    LOGGER.error("SQL Error adding new ID", e);
	    return null;
	} finally {
	    if (statement != null) {
		try {
		    statement.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	    if (statement1 != null) {
		try {
		    statement1.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	}
	return returnId;
    }

    /**
     * Update the profile of the interviewer
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @param interviewer
     *            Interview to be added to the table
     * 
     * @return id of the updated interviewer
     */
    public String saveProfile(StudySpace studySpace, Interviewer interviewer) {

	Connection conn = null;
	PreparedStatement statement = null;
	String sql = null;

	try {
	    conn = studySpace.getDBConnection();

	    sql = "UPDATE interviewer SET username=" + "? , firstname="
		    + "? , lastname=" + "? , salutation=" + "? , email="
		    + "? WHERE id = ?";

	    statement = conn.prepareStatement(sql);
	    statement.setString(1, interviewer.getUserName());
	    statement.setString(2, interviewer.getFirstName());
	    statement.setString(3, interviewer.getLastName());
	    statement.setString(4, interviewer.getSalutation());
	    statement.setString(5, interviewer.getEmail());
	    statement.setInt(6, Integer.valueOf(interviewer.getId()));

	    statement.executeUpdate();

	} catch (NumberFormatException e) {
	    LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
	    LOGGER.error("SQL Error updating new ID", e);
	    return null;
	} catch (SQLException e) {
	    LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
	    LOGGER.error("SQL Error updating new ID", e);
	    return null;
	} finally {
	    if (statement != null) {
		try {
		    statement.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	}
	return interviewer.getId();
    }

    /**
     * Search by interviewer ID to assign the attributes
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @param interviewId
     *            Interviewer ID to assign parameters.
     * 
     * @return Interviewer object
     */
    public Interviewer getInterviewer(StudySpace studySpace, String interviewId) {
	Interviewer interviewer = new Interviewer(studySpace);
	Connection conn = null;
	PreparedStatement statement = null;
	String sql;

	try {
	    conn = studySpace.getDBConnection();

	    sql = "select id, username, firstname, lastname, salutation, email, submittime from interviewer where id="
		    + "?";
	    statement = conn.prepareStatement(sql);

	    statement.setInt(1, Integer.valueOf(interviewId));
	    ResultSet rs = statement.executeQuery();

	    if (rs.wasNull()) {
		return null;
	    }
	    if (rs.next()) {
		interviewer.setId(rs.getString("id"));
		interviewer.setUserName(rs.getString("username"));
		interviewer.setFirstName(rs.getString("firstname"));
		interviewer.setLastName(rs.getString("lastname"));
		interviewer.setSalutation(rs.getString("salutation"));
		interviewer.setEmail(rs.getString("email"));
		interviewer.setLoginTime(rs.getString("submittime"));
	    }

	} catch (NumberFormatException e) {
	    LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
	    LOGGER.error("SQL Error updating new ID", e);
	    return null;
	} catch (SQLException e) {
	    LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
	    LOGGER.error("SQL Error getting new ID", e);
	    return null;
	} finally {
	    if (statement != null) {
		try {
		    statement.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	    if (conn != null) {
		try {
		    conn.close();
		} catch (SQLException e) {
		    LOGGER.error("SQL Statement failure", e);
		}
	    }
	}
	return interviewer;
    }
}
