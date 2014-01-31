package edu.ucla.wise.commons;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is a subclass of PageItem and represents a question block on the page.
 * 
 * @author Douglas Bell
 * @version 1.0  
 */
public class QuestionBlock extends PageItem {
    public static String sqlDatatype = "int(6)";

    /** Instance Variables */
    public String instructions = "NONE";
    public ResponseSet responseSet;
    public String responseSetID;
    // public Subject_Set subject_set;
    String subjectSetName;

    public ArrayList<StemDifferentiator> stems = new ArrayList<StemDifferentiator>();
    public ArrayList<String> stemFieldNames = new ArrayList<String>();
    // P public String[] stems;
    // P public String[] stem_fieldNames;

    /* hasPrecondition is a flag to check the precondition attribute of a
     * subject set reference.
     */
    public boolean hasPrecondition = false;
    public boolean hasSubjectSetRef = false;
    public Condition cond;

    /**
     * Constructor: Parse a question block node from the XML DOM node
     * 
     * @param n		DOM node for this question.
     */
    public QuestionBlock(Node n) {
		
    	/* assign the attributes of the page item */
		super(n);
		try {
		    NodeList nodelist = n.getChildNodes();
		    // P int num_stems = 0;
	
		    /*
		     * P // parse subject stem // count the total number of the subject
		     * stems for (int i = 0; i < nodelist.getLength(); i++) { if
		     * (nodelist.item(i).getNodeName().equalsIgnoreCase("Sub_Stem"))
		     * num_stems++; } // declare the string array for stem name & value
		     * based on the // subject stem size stems = new String[num_stems];
		     * stem_fieldNames = new String[num_stems];
		     */
	
		    /* get the sub stem name & value and assign them to the two arrays */
		    for (int i = 0, j = 0; i < nodelist.getLength(); i++) {
				if (nodelist.item(i).getNodeName().equalsIgnoreCase("Sub_Stem")
						|| nodelist.item(i).getNodeName()
							.equalsIgnoreCase("Sub_Head")) {
				    Node node = nodelist.item(i);
				    Transformer transformer = TransformerFactory.newInstance()
					    .newTransformer();
				    StringWriter sw = new StringWriter();
				    transformer.transform(new DOMSource(node),
					    new StreamResult(sw));
				    String stemType = nodelist.item(i).getNodeName()
					    .toUpperCase();
				    // P stems[j] = sw.toString();
				    stems.add(this.new StemDifferentiator(stemType, sw
					    .toString()));
				    
				    /* each stem name is the question name plus the index number */
				    // P stem_fieldNames[j] = name + "_" + (j + 1);
				    stemFieldNames.add(name + "_" + (j + 1));
				    j++;
				}
		    }
		    
		    /* parse other nodes: response set, response set ref, subject set ref, stem etc. */
		    for (int i = 0; i < nodelist.getLength(); i++) {
				
		    	/* parse the response set */
				if (nodelist.item(i).getNodeName()
						.equalsIgnoreCase("Response_Set")) {
				    responseSetID = nodelist.item(i).getAttributes()
					    .getNamedItem("ID").getNodeValue();
				}
				
				/* parse the response set reference */
				if (nodelist.item(i).getNodeName()
						.equalsIgnoreCase("Response_Set_Ref")) {
				    responseSetID = nodelist.item(i).getAttributes()
					    .getNamedItem("Response_Set").getNodeValue();
				}
				
				/* parse the stem */
				if (nodelist.item(i).getNodeName().equalsIgnoreCase("Stem")) {
				    Node node = nodelist.item(i);
				    Transformer transformer = TransformerFactory.newInstance()
					    .newTransformer();
				    StringWriter sw = new StringWriter();
				    transformer.transform(new DOMSource(node),
					    new StreamResult(sw));
				    instructions = sw.toString();
				}
				
				/* parse the precondition set for the question block
				 * note: this precondition is not the precondition set for child
				 * node - subject set reference
				 */
				if (nodelist.item(i).getNodeName()
						.equalsIgnoreCase("Precondition")) {
				    
					/* create the condition object */
				    cond = new Condition(nodelist.item(i));
				}
		    }
		} catch (TransformerConfigurationException e) {
		    WISEApplication.logError(
			    "WISE - QUESTION BLOCK: " + e.toString(), null);
		    return;
		} catch (DOMException e) {
		    WISEApplication.logError(
				    "WISE - QUESTION BLOCK: " + e.toString(), null);
			    return;
		} catch (TransformerException e) {
		    WISEApplication.logError(
			    "WISE - QUESTION BLOCK: " + e.toString(), null);
		    return;
		}
    }

    /**
     * Initializes the response set and html specific to this question.
     * 
     * @param	mySurvey	the survey to which this question is linked.
     */
    public void knitRefs(Survey mySurvey) {
		responseSet = mySurvey.getResponseSet(responseSetID);
		html = makeHtml();
    }

    /**
     * Counts number of fields/options in the question block
     * 
     * @return	int	number of the fields(stems) pertaining to this question.
     */
    public int countFields() {
    	
		/* the number of fields is the total number of subject stems */
		return stems.size();
    }

    /**
     * Returns all the stem names related to this question, each stem name is
     * name of the question + index.
     * 
     * @return	Array	Array of strings which contains the names of all the 
     * 					stems related to this question
     * 
     */
    public String[] listFieldNames() {
		
    	// P return stem_fieldNames;
		return (String[]) stemFieldNames.toArray(new String[stemFieldNames
			.size()]);
    }

    /**
     * get the table creation syntax (the series subject set table) for subject
     * set references
     */
    // public void create_subjectset_table()
    // {
    // try
    // {
    // //connect to the database
    // Connection conn = page.survey.getDBConnection();
    // Statement stmt = conn.createStatement();
    // String sql="";
    //
    // //first check if this subject set table already exists
    // boolean found=false;
    // ResultSet rs=stmt.executeQuery("show tables");
    // while(rs.next())
    // {
    // if(rs.getString(1).equalsIgnoreCase(page.survey.id +
    // "_"+SubjectSet_name+"_data"))
    // {
    // found=true;
    // break;
    // }
    // }
    // if(!found)
    // {
    // //if the subject set table doesn't exist, then create the new table
    // sql = "CREATE TABLE "+ page.survey.id + "_"+SubjectSet_name+ "_data (";
    // sql += "invitee int(6) not null,";
    // sql += "subject int(6) not null,";
    // sql += " "+name+" int(6),";
    // sql += "INDEX (invitee),";
    // sql += "FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE";
    // sql += ") TYPE=INNODB ";
    // boolean dbtype = stmt.execute(sql);
    // }
    // else
    // {
    // //if the table already exists, then check if the column (question block
    // name) exists
    // sql="describe "+ page.survey.id + "_"+SubjectSet_name+ "_data ";
    // boolean result = stmt.execute(sql);
    // rs = stmt.getResultSet();
    // boolean column_exist=false;
    // while(rs.next())
    // {
    // if(name.equalsIgnoreCase(rs.getString(1)))
    // {
    // //find the column
    // column_exist=true;
    // break;
    // }
    // }
    // //if the column doesn't exist
    // //then alter the current table to append a new column representing this
    // question_block
    // if(!column_exist)
    // {
    // sql = "ALTER TABLE "+ page.survey.id + "_"+SubjectSet_name+ "_data ";
    // sql += "ADD "+name+" int(6)";
    // boolean dbtype = stmt.execute(sql);
    // }
    // }
    // stmt.close();
    // conn.close();
    // }
    // catch (Exception e)
    // {
    // Study_Util.email_alert("WISE - QUESTION BLOCK CREATE SUBJECT SET TABLE: "+e.toString());
    // }
    // return;
    // }

    /**
     * Renders the {@link QuestionBlock} HTML {@link PageItem}. Renders a
     * static html at the time of loading the survey.
     * 
     * @return	String	HTML format of the question Block.
     */
    public String makeHtml() {
		String s = "";
		int len = responseSet.getSize();
		int startV = Integer.parseInt(responseSet.startvalue);
		int num = startV;
		//String t1, t2;
		int levels = Integer.parseInt(responseSet.levels);
		
		/* Print the instruction above the table top */
		s += "<p rowspan=2'>";
		if (!instructions.equalsIgnoreCase("NONE")) {
		    s += "<br />" + instructions;
		} else {
		    s += "&nbsp;";
		}
		s += "</p>";
	
		/* cells for question block that doesnt require classified levels */
		String noClassifiedLevelColumns = "";
		for (int j = startV, k = 0; j < len + startV; j++, k++) {
		    noClassifiedLevelColumns += "<td class=\"header-row\"><center>"
			    + responseSet.responses.get(k) + "</center></td>";
		}
		
		/* cells for question block that requires classified levels */
		String classifiedLevelColumns = "";
		classifiedLevelColumns += "<td class=\"header-row\" colspan="
			+ levels + " width='60%'>";
		classifiedLevelColumns += "<table cellpadding='3' border='0' width='100%' cellspacing='0'>";
		classifiedLevelColumns += "<tr class='shaded-bg'>";// make one row 'stead of many
		if (responseSet.responses.size() == 2 && levels > 2) {
		    classifiedLevelColumns += "<td class='header-row' align='left'>";
		    classifiedLevelColumns += startV + ". "
			    + responseSet.responses.get(0);
		    classifiedLevelColumns += "<td class=\"header-arrows\"align='center' width='10%'>&larr;&rarr;</td>";
		    classifiedLevelColumns += "<td class='header-row' align='right'>";
		    classifiedLevelColumns += (startV + levels - 1) + ". "
			    + responseSet.responses.get(1);
	
		} else {
		    int step = Math.round((levels - 1) / (len - 1));
		    for (int j = 1, k = 0, currentLevel = startV; j <= levels; j++, currentLevel++) {
			int det = (j - 1) % step;
			if (det == 0) {
			    // classified_level_columns += "<tr>";
			    if (j == 1)
				classifiedLevelColumns += "<td align='left'>";
			    else if (j == levels)
				classifiedLevelColumns += "<td align='right'>";
			    else
				classifiedLevelColumns += "<td align='center'>";
			    classifiedLevelColumns += currentLevel + ". "
				    + responseSet.responses.get(k);
			    classifiedLevelColumns += "</td>";
			    // classified_level_columns += "</tr>";
			    k++;
			}
		    }
		}
		classifiedLevelColumns += "</tr>";// moved row closing to here
		classifiedLevelColumns += "</table>";
		classifiedLevelColumns += "</td>";
		classifiedLevelColumns += "</tr>";
		classifiedLevelColumns += "<tr class=\"header-row shaded-bg\">";
		classifiedLevelColumns += "<td class=\"header-row\">";
		classifiedLevelColumns += "&nbsp;";
		classifiedLevelColumns += "</td>";
		for (int j = startV; j < levels + startV; j++)
		    classifiedLevelColumns += "<td class=\"header-row\"><center>" + j
			    + "</center></td>";
	
		/* to specify background of a row
		 * render row for each stem of the question block
		 */				
		int rowBackgroundColorIndex = 0; 	
		for (int i = 0; i < stems.size(); i++) {
		    boolean isSubHead = stems.get(i).stemType
			    .equalsIgnoreCase("Sub_Head");
		    boolean isSubStem = stems.get(i).stemType
			    .equalsIgnoreCase("Sub_Stem");
			if (i == 0) {
				
				/* open the question block table */
				s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
		
				/* render header row if the question block doesn't require classified level */
				if (levels == 0) {
				    s += "<tr class=\"shaded-bg\">";
				    s += "<td class=\"header-row sub_head\">";
				    if (isSubHead) {
				    	s += stems.get(i).stemValue;
				    } else {
				    	s += "&nbsp;";
				    }
				    s += "</td>";
				    s += noClassifiedLevelColumns;
				    s += "</tr>";
				    rowBackgroundColorIndex++;
					if (isSubStem) {
						if (rowBackgroundColorIndex++ % 2 == 0) {
						    s += "<tr class=\"shaded-bg\">";
						} else {
						    s += "<tr class=\"unshaded-bg\">";
						}
						s += "<td>" + stems.get(i).stemValue + "</td>";
						num = startV;			
						for (int j = startV, k = 0; j < len + startV; j++, k++) {
						    if (((String) responseSet.values.get(k))
						    		.equalsIgnoreCase("-1")) {
								s += "<td><center>";
								s += "<input type='radio' name='"
									+ stemFieldNames.get(i).toUpperCase()
									+ "' value='" + num + "'>";
								s += "</center></td>";
								num = num + 1;
						    } else {
								s += "<td><center>";
								s += "<input type='radio' name='"
									+ stemFieldNames.get(i).toUpperCase()
									+ "' value='"
									+ responseSet.values.get(k) + "'>";
								s += "</center></td>";
								num = num + 1;
						    }
						}
						s += "</tr>";
					}
				} // if classified level is required for the question block
				else {
				    if (rowBackgroundColorIndex++ % 2 == 0) {
				    	s += "<tr class=\"shaded-bg\">";
				    } else {
				    	s += "<tr class=\"unshaded-bg\">";
				    }
				    s += "<td class=\"header-row sub_head\">";
				    if (isSubHead) {
				    	s += stems.get(i).stemValue;
				    } else {
				    	s += "&nbsp;";
				    }
				    s += "</td>";
				    s += classifiedLevelColumns;
				    s += "</tr>";
				    if (isSubStem) {
						if (rowBackgroundColorIndex++ % 2 == 0) {
						    s += "<tr class=\"shaded-bg\">";
						} else {
						    s += "<tr class=\"unshaded-bg\">";
						}
						s += "<td>" + stems.get(i).stemValue + "</td>";
						num = startV;
			
						for (int j = 1; j <= levels; j++) {
						    s += "<td><center>";
						    s += "<input type='radio' name='"
							    + stemFieldNames.get(i).toUpperCase()
							    + "' value='" + num + "'>";
						    s += "</center></td>";
						    num = num + 1;
						}
						s += "</tr>";
				    }		
				}
			 } else {
				if (rowBackgroundColorIndex++ % 2 == 0) {
				    s += "<tr class=\"shaded-bg\">";
				} else {
				    s += "<tr class=\"unshaded-bg\">";
				}
				if (isSubHead) {
				    s += "<td class=\"sub_head\">" + stems.get(i).stemValue
					    + "</td>";
				    if (levels == 0) {
						s += noClassifiedLevelColumns;
						s += "</tr>";
				    } // if classified level is required for the question block
				    else {
						s += classifiedLevelColumns;
						s += "</tr>";
				    }
				} else {
				    s += "<td>" + stems.get(i).stemValue + "</td>";
				    num = startV;
				    
				    /* if the question block doesn't require classified level */
				    if (levels == 0) {
						for (int j = startV, k = 0; j < len + startV; j++, k++) {
						    if (((String) responseSet.values.get(k))
						    		.equalsIgnoreCase("-1")) {
								s += "<td><center>";
								s += "<input type='radio' name='"
									+ stemFieldNames.get(i).toUpperCase()
									+ "' value='" + num + "'>";
								s += "</center></td>";
								num = num + 1;
						    } else {
								s += "<td><center>";
								s += "<input type='radio' name='"
									+ stemFieldNames.get(i).toUpperCase()
									+ "' value='"
									+ responseSet.values.get(k) + "'>";
								s += "</center></td>";
								num = num + 1;
						    }
						}
				    } else {
				    	
				    	/* if classified level is required for the question block */
						for (int j = 1; j <= levels; j++) {
						    s += "<td><center>";
						    s += "<input type='radio' name='"
							    + stemFieldNames.get(i).toUpperCase()
							    + "' value='" + num + "'>";
						    s += "</center></td>";
						    num = num + 1;
						}
				    }
				}
		    }	
		}
		s += "</table>";
		return s;
	}

    /**
     * Prints question block - admin tool: print survey.
     * 
     * @return	String	HTML format of the this question block to print the survey.
     */
    public String printSurvey() {
    	String s = "";
    	int len = responseSet.getSize();
    	int startV = Integer.parseInt(responseSet.startvalue);
    	int num = startV;
    	//String t1, t2;
    	int levels = Integer.parseInt(responseSet.levels);

    	/* render top part of the question block */
    	if (levels == 0) {
    		s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
    		s += "<tr bgcolor=#FFFFFF><td>";
    		if (!instructions.equalsIgnoreCase("NONE"))
    			s += "<b>" + instructions + "</b>";
    		else
    			s += "&nbsp;";
    		s += "</td>";
    		for (int j = startV, i = 0; j < len + startV; j++, i++)
    			s += "<td align=center>" + responseSet.responses.get(i)
    			+ "</td>";
    		s += "</tr>";
    	} else {
    		s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
    		s += "<tr bgcolor=#FFFFFF>";
    		s += "<td rowspan=2 width='70%'>";
    		if (!instructions.equalsIgnoreCase("NONE"))
    			s += "<b>" + instructions + "</b>";
    		else
    			s += "&nbsp;";
    		s += "</td>";

    		s += "<td colspan=" + levels + " width='20%'>";
    		s += "<table cellpadding='0' border='0' width='100%'>";
    		int step = Math.round((levels - 1) / (len - 1));
    		//int k = 1;
    		for (int j = 1, i = 0, l = startV; j <= levels; j++, l++) {
    			int det = (j - 1) % step;
    			if (det == 0) {
    				s += "<tr>";
    				if (j == 1)
    					s += "<td align='left'>";
    				else if (j == levels)
    					s += "<td align='right'>";
    				else
    					s += "<td align='center'>";
    				s += l + ". " + responseSet.responses.get(i);
    				s += "</td></tr>";
    				i++;
    			}
    		}
    		s += "</table>";
    		s += "</td>";
    		s += "</tr>";

    		s += "<tr bgcolor=#FFFFFF>";
    		for (int j = startV; j < levels + startV; j++)
    			s += "<td><center>" + j + "</center></td>";
    		s += "</tr>";
    	}

    	/* render each stem of the question block */
    	for (int i = 0; i < stems.size(); i++) {
    		if (i % 2 == 0)
    			s += "<tr bgcolor=#CCCCCC>";
    		else
    			s += "<tr bgcolor=#FFFFFF>";
    		s += "<td>" + stems.get(i).stemValue + "</td>";
    		num = startV;
    		if (levels == 0) {
    			for (int j = startV; j < len + startV; j++) {
    				s += "<td align=center>";
    				s += "<img src='" + WISEApplication.rootURL + "/WISE"
    						+ "/" + WiseConstants.SURVEY_APP + "/"
    						+ "imageRender?img=checkbox.gif' border='0'></a>";
    				s += "</td>";
    				num = num + 1;
    			}
    		} else {
    			for (int j = 1; j <= levels; j++) {
    				s += "<td align=center>";
    				s += "<img src='" + WISEApplication.rootURL + "/WISE"
    						+ "/" + WiseConstants.SURVEY_APP + "/"
    						+ "imageRender?img=checkbox.gif' border='0'></a>";
    				s += "</td>";
    				num = num + 1;
    			}
    		}
    	}
    	s += "</table>";
    	return s;
    }

    // public Hashtable read_form(Hashtable params)
    // {
    // Hashtable answers = new Hashtable();
    // for (int i = 0; i < stems.length; i++)
    // {
    // String fieldName = stem_fieldNames[i].toUpperCase();
    // String answerVal = (String) params.get(fieldName);
    // if (answerVal.equalsIgnoreCase(""))
    // answerVal = null;
    // answers.put(fieldName, answerVal);
    // }
    // return answers;
    // }

    /**
     * read out the question field name & value from the hashtable and put them
     * into two arrays respectively
     */

    // Old version
    // public int read_form(Hashtable params, String[] fieldNames, String[]
    // fieldValues, int fieldIndex, User theUser)
    // {
    // //check if the question block has the subject set reference
    // int index_len = 0;
    // //if the question block doesn't have the subject set reference
    // //then read the data from the hashtable param and put into the field name
    // & value arrays
    // if(!hasSubjectSetRef)
    // {
    // for (int i = 0; i < stems.length; i++)
    // {
    // fieldNames[fieldIndex] = stem_fieldNames[i].toUpperCase();
    // fieldValues[fieldIndex] = (String)
    // params.get(stem_fieldNames[i].toUpperCase());
    // fieldIndex++;
    // }
    // index_len=stems.length;
    // }
    // //if the question block has the subject set reference, insert or update
    // the table of subject set
    // else
    // {
    // String sql="";
    //
    // try
    // {
    // //connect to the database
    // Connection conn = page.survey.getDBConnection();
    // Statement stmt = conn.createStatement();
    // //firstly check if the user record exists in the table of page_submit
    // sql =
    // "SELECT * from page_submit where invitee = "+theUser.id+" AND survey = '"+page.survey.id+"'";
    // boolean dbtype = stmt.execute(sql);
    // ResultSet rs = stmt.getResultSet();
    // boolean user_data_exists = rs.next();
    //
    // //then check if a user record exists in table of subject set
    // for (int i = 0; i < stems.length; i++)
    // {
    // sql = "SELECT * from "+page.survey.id+"_"+SubjectSet_name+"_data where ";
    // sql += "invitee = " +theUser.id+" and subject=";
    // sql +=
    // stem_fieldNames[i].substring((stem_fieldNames[i].lastIndexOf("_")+1));
    // dbtype = stmt.execute(sql);
    // rs = stmt.getResultSet();
    // user_data_exists = rs.next();
    //
    // Statement stmt2 = conn.createStatement();
    // //read out the user's new data from the hashtable params
    // String s_new = (String) params.get(stem_fieldNames[i].toUpperCase());
    //
    // //note that s_new could be null - seperate the null value with the 0
    // value
    // s_new = Study_Util.fixquotes(s_new);
    // if (s_new.equalsIgnoreCase(""))
    // s_new = "NULL";
    //
    // //if both tables (page_submit & subject set) have the user's data
    // if (user_data_exists)
    // {
    // String s = rs.getString(name);
    // //compare with the new user data, update the subject set data if the old
    // value has been changed
    // if ((s==null && !s_new.equalsIgnoreCase("NULL")) || (s!=null &&
    // !s.equalsIgnoreCase(s_new)))
    // {
    // //create UPDATE statement
    // sql = "update "+page.survey.id+"_"+SubjectSet_name+"_data set ";
    // sql += name + " = " + s_new;
    // sql += " where invitee = "+theUser.id+" and subject=";
    // sql +=
    // stem_fieldNames[i].substring((stem_fieldNames[i].lastIndexOf("_")+1));
    // dbtype = stmt2.execute(sql);
    //
    // String s1;
    // if (s != null)
    // s1 = Study_Util.fixquotes(s);
    // else
    // s1 = "null";
    // //check if the user's record exists in the table of update_trail, update
    // the data there as well
    // sql =
    // "select * from update_trail where invitee="+theUser.id+" and survey='"+page.survey.id;
    // sql +=
    // "' and page='"+page.id+"' and ColumnName='"+stem_fieldNames[i].toUpperCase()+"'";
    // dbtype = stmt2.execute(sql);
    // ResultSet rs2 = stmt2.getResultSet();
    // if(rs2.next())
    // {
    // //update the records in the update trail
    // if(!s1.equalsIgnoreCase(s_new))
    // {
    // sql = "update update_trail set OldValue='"+s1+"', CurrentValue='"+s_new;
    // sql
    // +="', Modified=now() where invitee="+theUser.id+" and survey='"+page.survey.id;
    // sql
    // +="' and page='"+page.id+"' and ColumnName='"+stem_fieldNames[i].toUpperCase()+"'";
    // }
    // }
    // //insert new record if it doesn't exist in the table of update_trail
    // else
    // {
    // sql =
    // "insert into update_trail (invitee, survey, page, ColumnName, OldValue, CurrentValue)";
    // sql += " values ("+theUser.id+",'"+page.survey.id+"','"+page.id+"','";
    // sql += stem_fieldNames[i].toUpperCase()+"','"+s1+"', '"+s_new+"')";
    // }
    // dbtype = stmt2.execute(sql);
    // }
    // }
    // //if no user's record exists in both tables
    // else
    // {
    // //create a insert statement to insert this record in the table of subject
    // set
    // sql = "insert into "+page.survey.id+"_"+SubjectSet_name+"_data ";
    // sql += "(invitee, subject, "+name+") ";
    // sql += "values ("+theUser.id+",'";
    // sql +=
    // Study_Util.fixquotes(stem_fieldNames[i].substring((stem_fieldNames[i].lastIndexOf("_")+1)));
    // sql += "', "+s_new+")";
    // dbtype = stmt2.execute(sql);
    // //and insert record into the table of update_trail as well
    // sql =
    // "insert into update_trail (invitee, survey, page, ColumnName, OldValue, CurrentValue)";
    // sql += " values ("+theUser.id+",'"+page.survey.id+"','"+page.id+"','";
    // sql += stem_fieldNames[i].toUpperCase()+"','null', '"+s_new+"')";
    // dbtype = stmt2.execute(sql);
    // }
    // stmt2.close();
    // } //end of for loop
    // stmt.close();
    // conn.close();
    // } //end of try
    // catch (Exception e)
    // {
    // Study_Util.email_alert("WISE - QUESTION BLOCK ["+page.id+"] READ FORM ("+sql+"): "+e.toString());
    // }
    // } //end of else
    // return index_len;
    // }

    /**
     * Renders the HTML header for this question block.
     * 
     * @return	String	HTML format of the header.
     */
    protected String renderQBheader() {
    	String s = "";
    	int len = responseSet.getSize();
    	int startV = Integer.parseInt(responseSet.startvalue);
    	int levels = Integer.parseInt(responseSet.levels);

    	/* render top part of the question block */
    	if (levels == 0) {
    		s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
    		s += "<tr>";
    		s += "<td class=\"header-row\">";
    		if (!instructions.equalsIgnoreCase("NONE"))
    			s += "<b>" + instructions + "</b>";
    		else
    			s += "&nbsp;";
    		s += "</td>";
    		for (int j = startV, i = 0; j < len + startV; j++, i++)
    			s += "<td class=\"header-row\"><center>"
    					+ responseSet.responses.get(i) + "</center></td>";
    		s += "</tr>";
    	} else {
    		s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
    		s += "<tr>";
    		s += "<td class=\"header-row\" rowspan=2 width='70%'>";
    		if (!instructions.equalsIgnoreCase("NONE"))
    			s += "<b>" + instructions + "</b>";
    		else
    			s += "&nbsp;";
    		s += "</td>";

    		s += "<td class=\"header-row\" colspan=" + levels + " width='20%'>";
    		s += "<table cellpadding='0' border='0' width='100%'>";
    		int step = Math.round((levels - 1) / (len - 1));
    		for (int j = 1, i = 0, l = startV; j <= levels; j++, l++) {
    			int det = (j - 1) % step;
    			if (det == 0) {
    				s += "<tr>";
    				if (j == 1)
    					s += "<td align='left'>";
    				else if (j == levels)
    					s += "<td align='right'>";
    				else
    					s += "<td align='center'>";
    				s += l + ". " + responseSet.responses.get(i);
    				s += "</td></tr>";
    				i++;
    			}
    		}
    		s += "</table>";
    		s += "</td>";
    		s += "</tr>";

    		s += "<tr class=\"header-row\">";
    		for (int j = startV; j < levels + startV; j++)
    			s += "<td><center>" + j + "</center></td>";
    		s += "</tr>";
    	}
    	return s;
    }

    /** 
     * Returns the results for a question block in form of string.
     * 
     * @param 	pg			Page Object for which the results are to be rendered.
     * @param	db			Data Bank object to connect to the database.
     * @param 	whereclause	whereclause to restrict the invitee selection.
     * @param	data		Hashtable which contains results.
     * @return 	String 		HTML format of the results is returned.
     */
    @SuppressWarnings("rawtypes")
    public String renderResults(Page pg, DataBank db, String whereclause,
    		Hashtable data) {

    	int levels = Integer.valueOf(responseSet.levels).intValue();
    	int startValue = Integer.valueOf(responseSet.startvalue).intValue();
    	
    	/* display the ID of the question */
    	String s = "<center><table width=100%><tr><td align=right>";
    	s += "<span class='itemID'>" + this.name
    			+ "</span></td></tr></table><br>";

    	/* display the question block */
    	s += "<table cellspacing='0' cellpadding='1' bgcolor=#FFFFF5 width=600 border='1'>";
    	s += "<tr><td bgcolor=#BA5D5D rowspan=2 width='60%'>";
    	s += "<table><tr><td width='95%'>";
    	
    	/* display the instruction if it has */
    	if (!instructions.equalsIgnoreCase("NONE")) {
    		s += "<b>" + instructions + "</b>";
    	} else {
    		s += "&nbsp;";
    	}

    	s += "</td><td width='5%'>&nbsp;</td></tr></table></td>";
    	String t1, t2;
    	
    	/* display the level based on the size of the question block */
    	if (levels == 0) {
    		s += "<td colspan=" + responseSet.responses.size()
    				+ " width='40%'>";
    		s += "<table bgcolor=#FFCC99 width=100% cellpadding='1' border='0'>";

    		for (int j = 0; j < responseSet.responses.size(); j++) {
    			t2 = String.valueOf(j + startValue);
    			t1 = (String) responseSet.responses.get(j);
    			s += "<tr>";

    			if (j == 0)
    				s += "<td align=left>";
    			else if ((j + 1) == responseSet.responses.size())
    				s += "<td align=right>";
    			else
    				s += "<td align=center>";
    			s += t2 + ". " + t1 + "</td>";
    			s += "</tr>";
    		}
    		s += "</table>";
    		s += "</td>";
    		s += "</tr>";
    		int width = 40 / responseSet.responses.size();
    		for (int j = 0; j < responseSet.responses.size(); j++) {
    			t2 = String.valueOf(j + startValue);
    			s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>"
    					+ t2 + "</center></b></td>";
    		}
    	} else {
    		
    		/* display the classified level */
    		s += "<td colspan=" + levels + " width='40%'>";
    		s += "<table bgcolor=#FFCC99 cellpadding='0' border='0' width='100%'>";
    		
    		/* calculate the step between levels */
    		int step = Math.round((levels - 1)
    				/ (responseSet.responses.size() - 1));

    		for (int j = 1, i = 0, l = startValue; j <= levels; j++, l++) {
    			int det = (j - 1) % step;
    			if (det == 0) {
    				s += "<tr>";
    				if (j == 1) {
    					s += "<td align='left'>";
    				} else if (j == levels) {
    					s += "<td align='right'>";
    				} else {
    					s += "<td align='center'>";
    				}
    				s += l + ". " + responseSet.responses.get(i);
    				s += "</td></tr>";
    				i++;
    			}
    		}
    		s += "</table>";
    		s += "</td>";
    		s += "</tr>";

    		int width = 40 / levels;
    		for (int j = 0; j < levels; j++) {
    			t2 = String.valueOf(j + startValue);
    			s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>"
    					+ t2 + "</center></b></td>";
    		}
    	}
    	s += "</tr>";

    	/* display each of the stems on the left side of the block */
    	for (int i = 0; i < stems.size(); i++) {
    		s += "<tr>";
    		int tnull = 0;
    		int t = 0;
    		float avg = 0;
    		Hashtable<String, String> h1 = new Hashtable<String, String>();
    		
    		/* get the user's conducted data from the hashtable */
    		String subjAns = (String) h1.get(stemFieldNames.get(i)
    				.toUpperCase());

    		try {
    			
    			/* connect to the database */
    			Connection conn = pg.survey.getDBConnection();
    			Statement stmt = conn.createStatement();

    			/* if the question block doesn't have the subject set ref */
    			String sql = "";
    			if (!hasSubjectSetRef) {
    				
    				/* get values from the survey data table
    				 * count total number of the users who have the same answer level
    				 */ 
    				sql = "select " + stemFieldNames.get(i)
    						+ ", count(distinct s.invitee) from "
    						+ pg.survey.getId()
    						+ "_data as s, page_submit as p where ";
    				sql += "p.invitee=s.invitee and p.survey='" + pg.survey.getId()
    						+ "'";
    				sql += " and p.page='" + pg.id + "'";
    				if (!whereclause.equalsIgnoreCase("")) {
    					sql += " and s." + whereclause;
    				}
    				sql += " group by " + stemFieldNames.get(i);
    			} else {
    				
    				/* if the question block has the subject set ref
    				 * get the user's conducted data from the table of subject set
    				 */
    				String user_id = (String) data.get("invitee");
    				if (user_id != null && !user_id.equalsIgnoreCase("")) {
    					sql = "select "
    							+ name
    							+ " from "
    							+ pg.survey.getId()
    							+ "_"
    							+ subjectSetName
    							+ "_data"
    							+ " where subject="
    							+ stemFieldNames.get(i).substring(
    									(stemFieldNames.get(i)
    											.lastIndexOf("_") + 1))
    											+ " and invitee=" + user_id;
    					stmt.execute(sql);
    					ResultSet rs = stmt.getResultSet();
    					if (rs.next()) {
    						subjAns = rs.getString(1);
    					}
    				}
    				
    				/* get values from the subject data table
    				 * count total number of the users who have the same answer
    				 * level
    				 */
    				sql = "select " + name + ", count(*) from " + pg.survey.getId()
    						+ "_" + subjectSetName
    						+ "_data as s, page_submit as p";
    				sql += " where s.invitee=p.invitee and p.survey='"
    						+ pg.survey.getId() + "'";
    				sql += " and p.page='" + pg.id + "'";
    				sql += " and s.subject="
    						+ stemFieldNames.get(i)
    						.substring(
    								(stemFieldNames.get(i)
    										.lastIndexOf("_") + 1));
    				if (!whereclause.equalsIgnoreCase("")) {
    					sql += " and s." + whereclause;
    				}
    				sql += " group by " + name;
    			}
    			stmt.execute(sql);
    			ResultSet rs = stmt.getResultSet();
    			h1.clear();
    			String s1, s2;

    			while (rs.next()) {
    				if (rs.getString(1) == null) {
    					tnull = tnull + rs.getInt(2);
    				} else {
    					s1 = rs.getString(1);
    					s2 = rs.getString(2);
    					h1.put(s1, s2);
    					t = t + rs.getInt(2);
    				}
    			}
    			rs.close();

    			if (subjAns == null)
    				subjAns = "null";

    			/* if the question block doesn't have the subject set ref */
    			if (!hasSubjectSetRef) {
    				
    				/* get values from the survey data table
    				 * calculate the average answer level
    				 */
    				sql = "select round(avg(" + stemFieldNames.get(i)
    						+ "),1) from " + pg.survey.getId()
    						+ "_data as s, page_submit as p"
    						+ " where s.invitee=p.invitee and p.page='" + pg.id
    						+ "' and p.survey='" + pg.survey.getId() + "'";
    				if (!whereclause.equalsIgnoreCase("")) {
    					sql += " and s." + whereclause;
    				}
    			}
    			
    			/* if the question block has the subject set ref */
    			else {
    				
    				/* get values from the subject data table
    				 * calculate the average answer level
    				 */
    				sql = "select round(avg(" + name + "),1) from "
    						+ pg.survey.getId() + "_" + subjectSetName
    						+ "_data as s, page_submit as p";
    				sql += " where s.invitee=p.invitee and p.survey='"
    						+ pg.survey.getId() + "'";
    				sql += " and p.page='" + pg.id + "'";
    				sql += " and s.subject="
    						+ stemFieldNames.get(i)
    						.substring(
    								(stemFieldNames.get(i)
    										.lastIndexOf("_") + 1));
    				if (!whereclause.equalsIgnoreCase("")) {
    					sql += " and s." + whereclause;
    				}
    			}
    			stmt.execute(sql);
    			rs = stmt.getResultSet();
    			if (rs.next()) {
    				avg = rs.getFloat(1);
    			} rs.close();

    			stmt.close();
    			conn.close();
    		} catch (SQLException e) {
    			WISEApplication
    				.logError(
    					"WISE - QUESTION BLOCK RENDER RESULTS: "
    							+ e.toString(), e);
    			return "";
    		} catch (NullPointerException e) {
    			WISEApplication
    				.logError(
    					"WISE - QUESTION BLOCK RENDER RESULTS: "
    							+ e.toString(), e);
    			return "";
    		}

    		/* display the statistic results */
    		String s1;
    		
    		/* if classified level is required for the question block */
    		if (levels == 0) {
    			s += "<td bgcolor=#FFCC99>";
    			s += stems.get(i).stemValue + "<p>";
    			s += "<div align='right'>";
    			s += "mean: </b>" + avg;

    			if (tnull > 0) {
    				s += "&nbsp;<b>unanswered:</b>";
    				
    				/* if the user's answer is null, highlight the answer
    				 * note that if the call came from admin page, this value is
    				 * always highlighted because the user's data is always to be null
    				 */
    				if (subjAns.equalsIgnoreCase("null")) {
    					s += "<span style=\"background-color: '#FFFF77'\">"
    							+ tnull + "</span>";
    				} else {
    					s += tnull;
    				}
    			}

    			s += "</div>";
    			s += "</td>";

    			for (int j = 0; j < responseSet.responses.size(); j++) {
    				t2 = String.valueOf(j + startValue);
    				if (j < responseSet.responses.size()) {
    					t1 = (String) responseSet.responses.get(j);
    				}
    				int num1 = 0;
    				int p = 0;
    				int p1 = 0;
    				float af = 0;
    				float bf = 0;
    				float cf = 0;
    				String ps, ps1;
    				s1 = (String) h1.get(t2);
    				if (s1 == null) {
    					ps = "0";
    					ps1 = "0";
    				} else {
    					num1 = Integer.parseInt(s1);
    					af = (float) num1 / (float) t;
    					bf = af * 50;
    					cf = af * 100;
    					p = Math.round(bf);
    					p1 = Math.round(cf);
    					ps = String.valueOf(p);
    					ps1 = String.valueOf(p1);
    				}
    				
    				/* if the user's answer belongs to this answer level,
    				 * highlight the image
    				 */
    				if (subjAns.equalsIgnoreCase(t2)) {
    					s += "<td bgcolor='#FFFF77'>";
    				} else{
    					s += "<td>";
    				}
    				s += "<center>";
    				s += "<img src='" + "imgs/vertical/bar_" + ps + ".gif' ";
    				s += "width='10' height='50'>";
    				s += "<br>" + ps1;
    				s += "</center>";
    				s += "</td>";
    			}
    		}
    		/* if classified level is required for the question block */
    		else {
    			s += "<td bgcolor=#FFCC99>";
    			s += stems.get(i).stemValue + "<p>";
    			s += "<div align='right'>";
    			s += "mean: </b>" + avg;

    			if (tnull > 0) {
    				s += "&nbsp;<b>unanswered: </b>";
    				
    				/* if the user's answer is null, highlight the answer
    				 * note that if the call came from admin page, this value is
    				 * always highlighted because the user's data is always to be null
    				 */
    				if (subjAns.equalsIgnoreCase("null")) {
    					s += "<span style=\"background-color: '#FFFF77'\">"
    							+ tnull + "</span>";
    				} else {
    					s += tnull;
    				}
    			}

    			s += "</div>";
    			s += "</td>";
    			//int step = Math.round((levels - 1)
    			//		/ (responseSet.responses.size() - 1));
    			for (int j = 0; j < levels; j++) {

    				// t2 = String.valueOf(j);
    				t2 = String.valueOf(j + startValue);
    				if (j < responseSet.responses.size()) {
    					t1 = (String) responseSet.responses.get(j);
    				}
    				int num1 = 0;
    				int p = 0;
    				int p1 = 0;
    				float af = 0;
    				float bf = 0;
    				float cf = 0;
    				String ps, ps1;
    				s1 = (String) h1.get(t2);
    				if (s1 == null) {
    					ps = "0";
    					ps1 = "0";
    				} else {
    					num1 = Integer.parseInt(s1);
    					af = (float) num1 / (float) t;
    					bf = af * 50;
    					cf = af * 100;
    					p = Math.round(bf);
    					p1 = Math.round(cf);
    					ps = String.valueOf(p);
    					ps1 = String.valueOf(p1);
    				}
    				
    				/* if the User's answer belongs to this answer level, highlight the image */
    				if (subjAns.equalsIgnoreCase(t2)) {
    					s += "<td bgcolor='#FFFF77'>";
    				}
    				else {
    					s += "<td>";
    				}
    				s += "<center>";
    				s += "<img src='" + "imgs/vertical/bar_" + ps + ".gif' ";
    				s += "width='10' height='50'>";
    				s += "<br>" + ps1;
    				s += "</center>";
    				s += "</td>";
    			}
    		}
    	}

    	s += "</table></center>";
    	return s;
    }

    /**
     * Renders the HTML header for this question block's result.
     * 
     * @return	String	HTML format of the header.
     */
    protected String renderQBResultHeader() {
    	String s = "";
    	int levels = Integer.valueOf(responseSet.levels).intValue();
    	int startValue = Integer.valueOf(responseSet.startvalue).intValue();
    	s += "<span class='itemID'>" + this.name
    			+ "</span></td></tr></table><br>";

    	/* display the question block */
    	s += "<table cellspacing='0' cellpadding='1' bgcolor=#FFFFF5 width=600 border='1'>";
    	s += "<tr><td bgcolor=#BA5D5D rowspan=2 width='60%'>";
    	s += "<table><tr><td width='95%'>";
    	// display the instruction if it has
    	if (!instructions.equalsIgnoreCase("NONE")) {
    		s += "<b>" + instructions + "</b>";
    	} else {
    		s += "&nbsp;";
    	}
    	s += "</td><td width='5%'>&nbsp;</td></tr></table></td>";

    	String t1, t2;
    	
    	/* display the level based on the size of the question block */
    	if (levels == 0) {
    		s += "<td colspan=" + responseSet.responses.size()
    				+ " width='40%'>";
    		s += "<table bgcolor=#FFCC99 width=100% cellpadding='1' border='0'>";

    		for (int j = 0; j < responseSet.responses.size(); j++) {

    			t2 = String.valueOf(j + startValue);
    			t1 = (String) responseSet.responses.get(j);
    			s += "<tr>";

    			if (j == 0) {
    				s += "<td align=left>";
    			} else if ((j + 1) == responseSet.responses.size()) {
    				s += "<td align=right>";
    			} else {
    				s += "<td align=center>";
    			}
    			s += t2 + ". " + t1 + "</td>";
    			s += "</tr>";
    		}
    		s += "</table>";
    		s += "</td>";
    		s += "</tr>";
    		int width = 40 / responseSet.responses.size();

    		for (int j = 0; j < responseSet.responses.size(); j++) {

    			t2 = String.valueOf(j + startValue);
    			s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>"
    					+ t2 + "</center></b></td>";
    		}
    	} else {
    		
    		/* display the classified level */
    		s += "<td colspan=" + levels + " width='40%'>";
    		s += "<table bgcolor=#FFCC99 cellpadding='0' border='0' width='100%'>";
    		
    		/* calculate the step between levels */
    		int step = Math.round((levels - 1)
    				/ (responseSet.responses.size() - 1));

    		for (int j = 1, i = 0, l = startValue; j <= levels; j++, l++) {
    			int det = (j - 1) % step;
    			if (det == 0) {
    				s += "<tr>";
    				if (j == 1)
    					s += "<td align='left'>";
    				else if (j == levels)
    					s += "<td align='right'>";
    				else
    					s += "<td align='center'>";
    				s += l + ". " + responseSet.responses.get(i);
    				s += "</td></tr>";
    				i++;
    			}
    		}
    		s += "</table>";
    		s += "</td>";
    		s += "</tr>";

    		int width = 40 / levels;
    		for (int j = 0; j < levels; j++) {
    			t2 = String.valueOf(j + startValue);
    			s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>"
    					+ t2 + "</center></b></td>";
    		}
    	}
    	s += "</tr>";
    	return s;
    }

    /** returns a comma delimited list of all the fields on a page */
    /*
     * public String list_fields() { String s = ""; for (int i = 0; i <
     * stems.length; i++) s += stem_fieldNames[i]+","; return s; }
     */

    /**
     * Prints out the question block information
     * 
     * @return	String	Information about question block.
     */
    public String toString() {
    	String s = "QUESTION BLOCK<br>";
    	s += super.toString();

    	s += "Instructions: " + instructions + "<br>";
    	s += "Response Set: " + responseSet.id + "<br>";
    	s += "Stems:<br>";

    	for (int i = 0; i < stems.size(); i++)
    		s += stemFieldNames.get(i) + ":" + stems.get(i).stemValue
    		+ "<br>";
    	if (cond != null) {
    		s += cond.toString();
    	}
    	s += "<p>";
    	return s;
    }

    /**
     * Class to store stemType and stemValue together.
     * 
     * @author Douglas Bell
     * @version 1.0  
     */
    public class StemDifferentiator {
		public String stemType;
		public String stemValue;

		public StemDifferentiator(String type, String value) {
		    stemType = type;
		    stemValue = value;
		}
    }
}
