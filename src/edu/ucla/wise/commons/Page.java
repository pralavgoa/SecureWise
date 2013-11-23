package edu.ucla.wise.commons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Page class represents a page in the survey and its various attributes.
 * 
 * @author Douglas Bell
 * @author Pralav
 * @author Vijay
 * @version 1.0  
 */
public class Page {

    /** Instance Variables */
    public String id;
    public String title;
    public String instructions;
    public Survey survey;

    public PageItem[] items;
    public String[] allFieldNames;
    public char[] allValueTypes; // just 'a' for string, 'n' for numeric; may add dates, PRN

    /* adding this to create tables when new survey is loaded */
    public ArrayList<RepeatingItemSet> repeatingItems = new ArrayList<RepeatingItemSet>();

    // public boolean blank_page = true; same as fieldCount=0
    public boolean finalPage = false;
    public String nextPage;
    
    // public String meta_charset=null;
    public Condition cond;
    int fieldCount; // for item count, get items.length

    Logger log = Logger.getLogger(Page.class);
    
    /** CLASS FUNCTIONS */
    /**
     * Constructor - parse out the survey page node from xml and create a page
     * with its attributes.
     * 
     * @param n		DOM node for the page.
     * @param s		Survey to which this page should be linked to.
     */
    public Page(Node n, Survey s) {
		try {
		    
			/* assign the survey object */
		    survey = s;
		    
		    /*
		     *  parse the page node with attributes: page ID, title, instruction,
		     *  ID of next page & final page
		     */
		    id = n.getAttributes().getNamedItem("ID").getNodeValue();
		    title = n.getAttributes().getNamedItem("Title").getNodeValue();
		    Node node1 = n.getAttributes().getNamedItem("Instructions");
		    if (node1 != null) {
		    	instructions = node1.getNodeValue();
		    } else {
		    	instructions = "NONE";
		    }
		    node1 = n.getAttributes().getNamedItem("nextPage");
		    if (node1 != null) {
		    	nextPage = node1.getNodeValue();
		    } else {
		    	nextPage = "NONE";
		    }
		    node1 = n.getAttributes().getNamedItem("finalPage");
		    if (node1 != null) {
				String fp = node1.getNodeValue();
				if (fp.equalsIgnoreCase("true")) {
				    finalPage = true;
				} else {
				    finalPage = false;
				}
		    }
		    
		    /* initialize the number of form field on the page */
		    NodeList nodelist = n.getChildNodes();
		    
		    /* count the number of page items */
		    int pageItemCount = 0;
		    fieldCount = 0;
		    for (int i = 0; i < nodelist.getLength(); i++) {
				if (PageItem.IsPageItemNode(nodelist.item(i))) {
				    pageItemCount++;
				}
		    }
		    items = new PageItem[pageItemCount];
	
		    /* parse & store the page items and any precondition */
		    for (int i = 0, k = 0; i < nodelist.getLength(); i++) {
				node1 = nodelist.item(i);
				if (PageItem.IsPageItemNode(node1)) {
				    
					// Pralav- adding repeating item code here to keep the
				    // object reference constant
				    if (RepeatingItemSet.IsRepeatingItemSetNode(node1)) {
						RepeatingItemSet repeatingSet = RepeatingItemSet
								.MakeNewItem(node1);
						if (repeatingSet == null) {
						    throw new Exception("Error parsing repeating item"
							    + k);
						}
						repeatingItems.add(repeatingSet);
						items[k++] = repeatingSet;
				    } else {
						PageItem pi = PageItem.MakeNewItem(node1);
						if (pi == null) {
						    throw new Exception("Null item parse at " + k);
						}
						items[k++] = pi;
				    }
		
				} else if (node1.getNodeName().equalsIgnoreCase("Precondition")) {
				    
					/* create the condition object */
				    cond = new Condition(node1);
				}
		    }
		    // Study_Util.email_alert("Page "+ title
		    // +" in survey "+survey.id+" has field count "+field_count);
	
		    // get the meta charset for the current page (for translations)
		    // meta_charset = this.required_charset();
	
		    /*
		     * iterate over items & knit references also collect full list of 
		     * fieldnames; consider also collecting main names & ss refs separately
		     */
		    for (int i = 0; i < pageItemCount; i++) {
		    	
		    	/*
		    	 * note this req here as MultiSelect Q field counts depend on refs being resolved
		    	 */
				items[i].knitRefs(survey); 
				fieldCount += items[i].countFields();
		    }
		    allFieldNames = new String[fieldCount];
		    allValueTypes = new char[fieldCount];
		    for (int i = 0, allStart = 0; i < pageItemCount; i++) {
	
				if (items[i] instanceof RepeatingItemSet) {
				    // ignore these
				} else {
				    String[] fieldnames = items[i].listFieldNames();
				    char valType = items[i].getValueType();
				    int j = 0;
				    for (; j < fieldnames.length; j++) {
				    	
				    	/* if multiple fields, copy same valType across all */
						String fn = fieldnames[j];
						allFieldNames[allStart + j] = fn;
						allValueTypes[allStart + j] = valType; 
				    }
				    allStart += j;
				}
		    }
		    // Study_Util.email_alert("Completing Page "+ title
		    // +" in survey "+survey.id+" in ss "+survey.study_space.location);
		} catch (DOMException e) {
		    WISEApplication.logError("WISE - survey parse failure at PAGE ["
			    + id + "] " + e.toString() + "\n" + this.toString(), null);
		    return;
		} catch (Exception e) {
		    WISEApplication.logError("WISE - survey parse failure at PAGE ["
				    + id + "] " + e.toString() + "\n" + this.toString(), null);
			    return;
			}
    }

    /**
     * Returns all the field names related to the questions in this page of the survey.
     *  
     * @return Array	Array of string which contains the field names.
     */
    public String[] getFieldList() {

		/* modifications to add repeating questions */
		return allFieldNames;
    }

    /**
     * Returns the number of the fields in this page of the survey.
     * 
     * @return	int		Number of the fields.
     */
    public int getItemCount() {
	return allFieldNames.length;
    }

    /**
     * Returns the type of the response for the questions in the page of the survey.
     * 
     * @return	Array	character array which contains the response type of each question
     */
    public char[] getValueTypeList() {
    	return allValueTypes;
    }

    /**
     * Gets the charset from the item on this page if there is a translation item.
     * 
     * @return String	The charset of the item in this page.
     */
    public String requiredCharset() {
		String currentCharset = null;
		for (int i = 0; i < items.length; i++) {
		    if (items[i].translationId != null) {
			TranslationItem translatedItem = this.survey
					.getTranslationItem(items[i].translationId);
			currentCharset = translatedItem.charset;
			break;
		    }
		}
		return currentCharset;
    }

    /**
     * Creates a string including all the required fields which is used for
     * JavaScript.
     * 
     * @return	String 	All the required feilds in this page.
     */
    public String requiredFields() {
		int flag = 0;
		String str = "{";
		for (int i = 0; i < items.length; i++) {
		    if (items[i].isRequired()) {
		    	
			/* {REQUIRED_FIELD_NAME:A, ... etc.} */
			str = str + "'" + items[i].name.toUpperCase() + "':'"
				+ items[i].getRequiredStem() + "',";
			flag = 1;
		    }
		}
		if (flag == 1) {
		    /* eliminate the last comma */
		    int len = str.length();
		    str = str.substring(0, len - 1);
		}
		str = str + "}";
		return str;
    }

    /**
     * Returns repeating sets, used for database table creation.
     * 
     * @return	ArrayList	List of the repeating items in this page.
     */
    public ArrayList<RepeatingItemSet> getRepeatingItemSets() {
    	return repeatingItems;
    }

    /**
     * create the table creation syntax for all the page items public String
     * create_table() { String sql = ""; for (int i = 0; i < items.length; i++)
     * sql += items[i].create_table(); return sql; }
     */

    /**
     * Render a survey page as an html form for the user to fill out.
     * 
     * @param 	theUser		The user to whom the page has to be rendered.
     * @return	String		HTML format of this page.
     */
	public String renderPage(User theUser) {

		/* initialize the string of html code */
		String s = "";
		
		/* check the precondition, if this page has the precondition node -*/
		if (cond != null) {
			
			/* check if the user's current input meets the required preconditions */
		    boolean writePage = cond.checkCondition(theUser);
		    
		    /* if it doesn't meet the precondition, then skip writing this whole
		     * page by return an empty string
		     */
	
		    log.info("Precondition for page is " + writePage);
		    // if (!write_page)
		    // return s;
		}
	
		/* get the field name:value pair for JavaScript */
		String fieldVals = theUser.getJSValues();
		
		/* get the JavaScript string for those required fields */
		String reqFields = requiredFields();
	
		/* display html header part */
		if (!title.equalsIgnoreCase("NONE")) {
		    s += "<title>" + title + "</title>\n";
		}
		s += "<script type='text/javascript' language='JavaScript1.1'>";
		
		/* display the string of required fields */
		if (reqFields.equalsIgnoreCase("{}")) {
		    s += "top.requiredFields = null;";
		} else {
		    s += "top.requiredFields = " + reqFields + ";";
		}
		
		/* display the string of general fields */
		if (fieldVals.equalsIgnoreCase("{}")) {
		    s += "top.fieldVals = null;";
		} else {
		    s += "top.fieldVals = " + fieldVals + ";";
		}
		s += "</script>";
	
		/********************* PralavCode ******************************/
		s += "<script src='div_effects.js' type='text/javascript'>";
		s += "</script>";
		/*************************************************************/
	
		// get the meta tag for translated page
		// if(meta_charset != null)
		// {
		// s +="<script>\n";
		// s +=
		// "var temp=\"<META http-equiv=Content-type content='text/html; charset="+meta_charset+"'>\";\n";
		// s += "if (navigator.userAgent.indexOf(\"MSIE\") != -1)\n";
		// s += " {document.write(temp);}\n";
		// s += "</script>";
		// }
		// remove s += "</head>";
		s += "<LINK href='" + "styleRender?app="
				+ survey.studySpace.studyName + "&css=style.css"
				+ "' type=text/css rel=stylesheet>";
		// display html body part
		// s +=
		// "<body text='#000000' bgcolor='#FFFFCC' onload='javascript: setFields();'>";
		// remove and use jquery s +=
		// "<body onload='javascript: setFields(); check_preconditions();'>";
		// s += "<table cellpadding=5 width='100%'><tr><td>";
	
		if (!instructions.equalsIgnoreCase("NONE")) {
		   	s += "<h4>" + StudySpace.font + instructions + "</font></h4>";
		}
		
		/* display the form part */
		s += "<form name='mainform' method='post' action='readform'>";
		
		/* The action field is set to indicate the info of
		 * interrupt/abort/done/interview etc.
		 */
		s += "<input type='hidden' name='action' value=''>";
		if ((survey.isLastPage(id)) || (finalPage)) {
		    s += "<input type='hidden' name='nextPage' value='DONE'>";
		} else {
		    if (nextPage.equalsIgnoreCase("NONE")) {
				s += "<input type='hidden' name='nextPage' value='"
					+ survey.nextPage(id).id + "'>";
		    } else {
				s += "<input type='hidden' name='nextPage' value='" + nextPage
					+ "'>";
		    }
		}
		
		/******************** PRALAV ********************/
		/* DISPLAY the ITEMS */
		for (int i = 0; i < items.length; i++) {
	
		    // don't know why we need to check for null item types
		    // if (items[i].item_type==null)
		    // s += items[i].render_form();
		    // P s += items[i].render_form(theUser) + "\n";
		    // P s += "<p>";
		    s += items[i].renderForm(theUser, i) + "\n";
		}
	
		s += "<center>";
		s += "<a href=\"javascript:document.forms['mainform'].action.value='interrupt';document.forms['mainform'].submit();";
		s += "\">";
		s += "<img src='"
			+ "imageRender?img=save.gif' alt='save' style='margin:1ex'>";
		s += "</a>";
		s += "<a href='javascript:check_and_submit();'>";
		/* display the next/done image */
		if ((survey.isLastPage(id)) || (finalPage)) {
		    if (survey.eduModule != null
		    		&& !survey.eduModule.equalsIgnoreCase("")) {
			
		    	/* Servlet to render */
				s += "<img src='"
					+ "imageRender?img=proceed.gif' style='margin:1ex'>";
		    } else {
				/* Servlet to render */
				s += "<img src='"
					+ "imageRender?img=done.gif' style='margin:1ex'>";
		    }
		} else {
		    /* Servlet to render */
		    s += "<img src='"
			    + "imageRender?img=save_and_next_page.gif' alt='save and next page' style='margin:1ex'>";
		}
		s += "</a>";
		s += "</center>";
		s += "</form>";
		// s += "</td></tr></table>";
		// s += "</body>";
		// s += "</html>";
		return s;
	}

    /** render survey result page for completers' review */
    // public String render_results(User theUser, String whereclause)
    // {
    // String s = "<html>";
    // //display html header
    // s += "<head>";
    // if (!title.equalsIgnoreCase("NONE"))
    // s += "<title>"+title+"</title>";
    // s +="<LINK href='"+ survey.study_space.style_path
    // +"style.css' rel=stylesheet>";
    // s +="<script type='text/javascript' language='javascript'>";
    // s +="function open_help_win(){";
    // s +=" var helpwin=window.open('"+Study_Space.file_path
    // +"result_help.htm', 'help_win', 'height=500, width=500, scrollbars=yes, toolbar=no');";
    // s +=" if (helpwin.opener==null) helpwin.opener = self; }";
    // s +=" </script>";
    // if (!title.equalsIgnoreCase("NONE"))
    // s += "<title>"+title+"</title>";
    // s += "</head>";
    // s += "<body text='#000000' bgcolor='#FFFFCC'>";
    // s +=
    // "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>";
    // s += "<tr><td width=160 align=center>";
    // s +="<img src='"+Study_Space.file_path +"images/somlogo.gif' border=0>";
    // s +="</td><td width=400 align=center>";
    // s +="<img src='"+Study_Space.file_path
    // +"images/title.gif' border=0><br><br>";
    // s
    // +="<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>";
    // s +="</td><td width=160 align=center>";
    // s +="</td></tr></table></center><br><br>";
    // //display the help info
    // s += "<table cellpadding=5><tr><td>";
    // s += "For each question, ";
    // s +=
    // "the graphs below show the <b>percentage</b> of people choosing each answer. ";
    // s += "Percentages may not sum to 100 because of rounding. ";
    // s += "Click <a href='javascript: open_help_win()'>";
    // s += "here</a> for more explanation of results.";
    // s +=
    // "<p><b><font color=green>"+get_pagedone_numb(whereclause)+" </font></b>people have completed this page.<p>";
    // //display the main body
    // if (!instructions.equalsIgnoreCase("NONE"))
    // s += "<h4><i>"+instructions+"</i></h4>";
    // //get the survey data within the scope of whereclause
    // //the default whereclause is an empty string - means view all the users'
    // results
    // Hashtable data = new Hashtable();
    // if (field_count > 0)
    // {
    // //data = get_survey_data(whereclause);
    // data = theUser.get_data();
    // for (int i = 0; i < items.length; i++)
    // {
    // s += items[i].render_results(data, whereclause);
    // s += "<p>";
    // }
    // }
    // else
    // {
    // for (int i = 0; i < items.length; i++)
    // {
    // s += items[i].render_results(data, whereclause);
    // s += "<p>";
    // }
    // }
    // s += "<center>";
    // //display the image link
    // if (survey.is_last_page(survey.get_page_index(id)))
    // s +="<a href='"+Study_Space.file_path + "thanks" +
    // Study_Space.html_ext+"'><img src='"+Study_Space.file_path
    // +"images/done.gif' border='0'></a>";
    // else
    // s +=
    // "<a href='view_results?page="+survey.next_page(id).id+"'><img src='"+Study_Space.file_path
    // +"images/next.gif' border='0'></a>";
    //
    // s += "</center>";
    // s += "</td></tr></table>";
    // s += "</body>";
    // s += "</html>";
    //
    // return s;
    // }

   	/**
	 * Counts the number of users within the whereclause scope who completed the
     * current page
	 * @param whereclause
	 * @return
	 */
    public int getPagedoneNumb(String whereclause) {
		if (allFieldNames.length > 0) {
		    int doneNumb = 0;
		    try {
		    	
				/* connect to the database */
				Connection conn = survey.getDBConnection();
				Statement stmt = conn.createStatement();
				
				/* count the total number of users who have done this page */
				String sql = "select count(*) from " + survey.id
						+ "_data where status not in(";
				for (int k = 0; k < survey.pages.length; k++) {
				    if (!id.equalsIgnoreCase(survey.pages[k].id)) {
				    	sql += "'" + survey.pages[k].id + "', ";
				    } else {
				    	break;
				    }
				}
				sql += "'" + id + "') or status is null";
				if (!whereclause.equalsIgnoreCase(""))
				    sql += " and " + whereclause;
				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				if (rs.next())
				    doneNumb = rs.getInt(1);
				rs.close();
				stmt.close();
				conn.close();
		    } catch (SQLException e) {
			WISEApplication.logError(
				"WISE - GET PAGE DONE NUMBER: " + e.toString(), null);
		    }
		    return doneNumb;
		} else {
		    return 0;
		}
    }

    /**
     * get the survey data (hash table) within the range delimited by
     * whereclause selection
     */
    /*
     * public Hashtable get_survey_data(String whereclause) { Hashtable h = new
     * Hashtable(); try { //connect to the database Connection conn =
     * survey.getDBConnection(); Statement stmt = conn.createStatement(); // get
     * data from database for subject String sql = "select * from "+
     * survey.id+"_data"; if(!whereclause.equalsIgnoreCase("")) sql +=
     * " where "+whereclause;
     * 
     * boolean dbtype = stmt.execute(sql); ResultSet rs = stmt.getResultSet();
     * ResultSetMetaData metaData = rs.getMetaData(); int columns =
     * metaData.getColumnCount(); //the data hash table takes the column name as
     * the key //and the user's anwser as its value if(rs.next()) { String
     * col_name, ans; for (int i = 1; i <= columns; i++) { col_name =
     * metaData.getColumnName(i); ans = rs.getString(col_name); //input a string
     * called null if the column value is null //to avoid the hash table has the
     * null value if (ans == null) ans = "null"; h.put(col_name,ans); } }
     * rs.close(); stmt.close(); conn.close(); } catch (Exception e) {
     * Study_Util.email_alert("PAGE GET SURVEY DATA: "+e.toString()); } return
     * h; }
     */
 
    /**
     * Renders a survey page for view - admin tool: view survey
     * 
     * @param 	ss		StudySpace whose page has to be rendered.
     * @return	String	HTML format of this page to view.
     */
    public String renderAdminPage(StudySpace ss) {
		String s = "<html>";
		
		/* form the html header */
		s += "<head>";
		if (!title.equalsIgnoreCase("NONE")) {
		    s += "<title>" + title + "</title>";
		}
		s += "<LINK href='" + "styleRender?app=" + ss.studyName
			+ "&css=style.css" + "' type=text/css rel=stylesheet>";
		s += "<script type='text/javascript' language='JavaScript1.1' src='"
			+ SurveyorApplication.sharedFileUrl + "survey.js'></script>";
		s += "</head>";
	
		/* form the html body */
		s += "<body>";
		s += "<table cellpadding=5 width='100%'><tr><td>";
		if (!instructions.equalsIgnoreCase("NONE")) {
		    s += "<h4>" + instructions + "</h4>";
		}
		for (int i = 0; i < items.length; i++) {
		    s += items[i].renderForm();
		    s += "<p>";
		}
		s += "<p>";
		s += "<center>";
		
		/* form the image link */
		if (survey.isLastPage(id)) {
		    
			/* Servlet to render */
		    s += "<a href='" + WISEApplication.adminServer
			    + "/tool.jsp'><img src='" + WISEApplication.rootURL
			    + "/WISE/survey/imageRender?img=done.gif' border='0'></a>";
		} else {
		    
			/* Servlet to render */
		    s += "<a href='admin_view_form'><img src='"
			    + WISEApplication.rootURL
			    + "/WISE/survey/imageRender?img=next.gif' border='0'></a>";
		}
		s += "</center>";
		s += "</td></tr></table>";
		s += "</body>";
		s += "</html>";
		return s;
    }

    /**
     * Prints survey page - admin tool: print survey.
     * 
     * @return	String	HTML format of the this page to print the survey.
     */
    public String printSurveyPage() {
		String s = "";
		
		/* print body header */
		s += "<table cellpadding=5 width=100%>";
		s += "<tr><td align=left width=50%><font color=#003399 size=-2><b>"
			+ survey.title + "</b></font></td>";
		s += "<td align=right width=50%><font color=#003399 size=-2>Page "
			+ (survey.getPageIndex(id) + 1) + " of "
			+ survey.pages.length + "</font></td>";
		s += "</tr></table><p><p>";
	
		/* print main body */
		s += "<table cellpadding=5 width='100%'><tr><td>";
		if (!instructions.equalsIgnoreCase("NONE")) {
		    s += "<b>" + instructions + "</b>";
		}
		for (int i = 0; i < items.length; i++) {
		    s += items[i].printSurvey();
		    s += "<p>";
		}
		
		/* print image link */
		s += "<p>";
		s += "<center><table><tr><td align=center valign=middle height=30>";
		if (survey.isLastPage(id)) {
		    s += "<br><a href='" + WISEApplication.adminServer
			    + "/tool.jsp'><b>DONE</b></a><br>";
		    s += "<p align=center><b>Thank you for completing the survey.</b></p>";
		} else {
			/* Servlet to render */
		    s += "<a href='admin_print_survey'><b>Go To Next Page </b><img src='"
			    + WISEApplication.rootURL
			    + "/WISE/survey/imageRender?img=nextpg.gif' border='0'></a>";
		}
		s += "</td></tr></table></center>";
		s += "</td></tr></table>";
		return s;
    }

    /**
     * Renders survey results - admin tool: view results.
     * 
     * @param 	whereclause		Invitees of whom the result has to be displayed.	
     * @return	String			HTML format of the results of the selected invitees.
     */
    public String renderAdminResults(String whereclause) {
		String s = "<html>";
		
		/* display header part */
		s += "<head>";
		if (!title.equalsIgnoreCase("NONE")) {
		    s += "<title>" + title + "</title>";
		}
		s += "<LINK href='" + "styleRender?app="
			+ this.survey.studySpace.studyName + "&css=style.css"
			+ "' type=text/css rel=stylesheet>";
		s += "<script type='text/javascript' language='javascript' src='"
			+ SurveyorApplication.sharedFileUrl
			+ "openhelpwin.js'></script>";
		s += "</head>";
		
		/* display body part */
		s += "<body>";
		s += "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>";
		s += "<tr><td width=160 align=center>";
		
		/* Servlet to render */
		s += "<img src='" + WISEApplication.rootURL
			+ "/WISE/survey/imageRender?img=somlogo.gif' border=0>";
		s += "</td><td width=400 align=center>";
		
		/* Servlet to render */
		s += "<img src='" + WISEApplication.rootURL
			+ "/WISE/survey/imageRender?img=title.jpg' border=0><br><br>";
		s += "<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>";
		s += "</td><td width=160 align=center>";
		s += "<a href='javascript: history.go(-1)'>";
		
		/* Servlet to render */
		s += "<img src='" + WISEApplication.rootURL
			+ "/WISE/survey/imageRender?img=back.gif' border=0></a>";
		s += "</td></tr></table></center><br><br>";
		
		/* display the help info */
		s += "<table cellpadding=5><tr><td>";
		s += "For each question, ";
		s += "the graphs below show the <b>percentage</b> of people choosing each answer. ";
		s += "Percentages may not sum to 100 because of rounding. ";
		s += "Click <a href='javascript: open_helpwin()'>";
		s += "here</a> for more explanation of results.";
		// TODO: DEBUG INACCURATE count algo; (try using page_submit table)
		// s +=
		// "<p><b><font color=green>"+get_pagedone_numb(whereclause)+" </font></b>people have completed this page.<p>";
		
		/* display the results of questions */
		if (!instructions.equalsIgnoreCase("NONE"))
		    s += "<h4><i>" + instructions + "</i></h4>";
	
		/* get the survey data conducted by users within the scope of whereclause */
		for (int i = 0; i < items.length; i++) {
		    // TODO: Help!
		    s += items[i].renderResults(this, survey.getDB(), whereclause,
			    null);
		    s += "<p>";
		}
		s += "<center>";
		
		/* display the image link */
		if (survey.isLastPage(survey.getPageIndex(id))) {
			/* Servlet to render */
		    s += "<a href='" + WISEApplication.adminServer
			    + "/view_result.jsp?s=" + survey.id + "'><img src='"
			    + WISEApplication.rootURL
			    + "/WISE/survey/imageRender?img=done.gif' border='0'></a>";
		} else {
		    s += "<a href='admin_view_results'><img src='"
			    + WISEApplication.rootURL
			    + "/WISE/survey/imageRender?img=next.gif' border='0'></a>";
		}
		s += "</center>";
		s += "</td></tr></table>";
		s += "</body>";
		s += "</html>";
	
		return s;
    }

    /**
     * Reads paramaeters passed from data source that apply to field names;
     * delegate value processing to each PageItem that the page contains.
     * 
     * @param 	params		The Http parameters to this page.
     * @return	Hashtable	Contains the answers of the survey so far for this page.
     */
    public Hashtable<String, Hashtable<String, String>> readForm(
    		Hashtable<String, String> params) {
		Hashtable<String, Hashtable<String, String>> pageAnswerSets = new Hashtable<String, Hashtable<String, String>>();
		
		/* don't bother if page is only directivespage */
		if (allFieldNames.length > 0) {
		    Hashtable<String, String> mainAnswers = new Hashtable<String, String>();
		    for (int i = 0; i < items.length; i++) {
				Hashtable<String, String> itemAnswers = items[i]
					.readForm(params);
				
				/* subjectsets push their id into hash */
				String ssid = itemAnswers.get("__SubjectSet_ID__"); 
				if (ssid != null) {
				    itemAnswers.remove("__SubjectSet_ID__");
				    pageAnswerSets.put(ssid, itemAnswers);
				} else {
				    mainAnswers.putAll(itemAnswers);
				}
		    }
		    pageAnswerSets.put("__WISEMAIN__", mainAnswers);
		}
		return pageAnswerSets;
    }

    /** query database to get the total number of respondents for a page */
    /*
     * public int get_total() { if (!blank_page) { int total = 0; try {
     * Connection conn = survey.getDBConnection(); Statement stmt =
     * conn.createStatement(); // print total number who have answered this page
     * String sql = "select count(*) from "+survey.id+"_data"; boolean dbtype =
     * stmt.execute(sql); ResultSet rs = stmt.getResultSet(); rs.next(); total =
     * rs.getInt(1); rs.close(); stmt.close(); conn.close(); } catch (Exception
     * e) { Study_Util.email_alert("WISE - PAGE GET TOTAL: "+e.toString()); }
     * return total; } else return 0; }
     */
    /** render a page as results */
    /*
     * public String render_results(User theUser, String whereclause) { String s
     * = "<html>"; s += "<head>"; if (!title.equalsIgnoreCase("NONE")) s +=
     * "<title>"+title+"</title>"; s +="<LINK href='"+
     * theUser.currentSurvey.study_space.style_path
     * +"style.css' rel=stylesheet>"; s
     * +="<script type='text/javascript' language='javascript'>"; s
     * +="function open_help_win(){"; s
     * +=" var helpwin=window.open('"+Study_Space.file_path +
     * "result_help.htm', 'help_win', 'height=500, width=500, scrollbars=yes, toolbar=no');"
     * ; s +=" if (helpwin.opener==null) helpwin.opener = self; }"; s
     * +=" </script>"; if (!title.equalsIgnoreCase("NONE")) s +=
     * "<title>"+title+"</title>"; s += "</head>"; s +=
     * "<body text='#000000' bgcolor='#FFFFCC'>"; s +=
     * "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>"; s
     * += "<tr><td width=160 align=center>"; s
     * +="<img src='"+Study_Space.file_path +"images/somlogo.gif' border=0>"; s
     * +="</td><td width=400 align=center>"; s
     * +="<img src='"+Study_Space.file_path
     * +"images/title.gif' border=0><br><br>"; s +=
     * "<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>"
     * ; s +="</td><td width=160 align=center>"; s
     * +="</td></tr></table></center><br><br>";
     * 
     * s += "<table cellpadding=5><tr><td>"; s += "For each question, "; s +=
     * "the graphs below show the <b>percentage</b> of people choosing each answer. "
     * ; s += "Percentages may not sum to 100 because of rounding. "; s +=
     * "Click <a href='javascript: open_help_win()'>"; s +=
     * "here</a> for more explanation of results."; s +=
     * "<p><b><font color=green>"+get_pagedone_numb(whereclause)+
     * " </font></b>people have completed this page.<p>";
     * 
     * if (!instructions.equalsIgnoreCase("NONE")) s +=
     * "<h4><i>"+instructions+"</i></h4>";
     * 
     * if (!blank_page) { Hashtable data = theUser.get_data(); for (int i = 0; i
     * < items.length; i++) { s += items[i].render_results(data, whereclause); s
     * += "<p>"; } } else { Hashtable data = new Hashtable(); for (int i = 0; i
     * < items.length; i++) { s += items[i].render_results(data, whereclause); s
     * += "<p>"; } }
     * 
     * s += "<center>";
     * 
     * if (survey.is_last_page(survey.get_page_index(id))) s
     * +="<a href='"+Study_Space.file_path + "thanks" + Study_Space.html_ext
     * +"'><img src='"+Study_Space.file_path
     * +"images/done.gif' border='0'></a>"; else s +=
     * "<a href='view_results?page="
     * +survey.next_page(id).id+"'><img src='"+Study_Space.file_path
     * +"images/next.gif' border='0'></a>";
     * 
     * s += "</center>";
     * 
     * s += "</td></tr></table>";
     * 
     * s += "</body>"; s += "</html>";
     * 
     * return s; }
     */
    /** render an admin page as results */
    /*
     * public String render_admin_results(String whereclause) { String s =
     * "<html>"; s += "<head>"; if (!title.equalsIgnoreCase("NONE")) s +=
     * "<title>"+title+"</title>"; s +="<LINK href='"+
     * this.survey.study_space.style_path +"style.css' rel=stylesheet>"; s
     * +="<script type='text/javascript' language='javascript' src='"
     * +Study_Space.file_path +"openhelpwin.js'></script>"; s += "</head>";
     * 
     * s += "<body text='#000000' bgcolor='#FFFFCC'>"; s +=
     * "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>"; s
     * += "<tr><td width=160 align=center>"; s
     * +="<img src='"+Study_Space.file_path +"images/somlogo.gif' border=0>"; s
     * +="</td><td width=400 align=center>"; s
     * +="<img src='"+Study_Space.file_path
     * +"images/title.jpg' border=0><br><br>"; s +=
     * "<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>"
     * ; s +="</td><td width=160 align=center>"; s
     * +="<a href='javascript: history.go(-1)'>"; s
     * +="<img src='"+Study_Space.file_path +"images/back.gif' border=0></a>"; s
     * +="</td></tr></table></center><br><br>";
     * 
     * s += "<table cellpadding=5><tr><td>"; s += "For each question, "; s +=
     * "the graphs below show the <b>percentage</b> of people choosing each answer. "
     * ; s += "Percentages may not sum to 100 because of rounding. "; s +=
     * "Click <a href='javascript: open_helpwin()'>"; s +=
     * "here</a> for more explanation of results."; s +=
     * "<p><b><font color=green>"+get_pagedone_numb(whereclause)+
     * " </font></b>people have completed this page.<p>";
     * 
     * if (!instructions.equalsIgnoreCase("NONE")) s +=
     * "<h4><i>"+instructions+"</i></h4>";
     * 
     * Hashtable data = new Hashtable(); // data could hold averages if
     * wanted... for (int i = 0; i < items.length; i++) { s +=
     * items[i].render_results(data, whereclause); s += "<p>"; } s +=
     * "<center>";
     * 
     * if (survey.is_last_page(survey.get_page_index(id))) s +=
     * "<a href='"+WISE_Application
     * .admin_server+"tool.jsp'><img src='"+Study_Space.file_path
     * +"images/done.gif' border='0'></a>"; else //s +=
     * "<a href='admin.view_results?a=NEXTPAGE&s="
     * +survey.id+"&p="+survey.next_page
     * (id).id+"'><img src='"+Study_Space.file_path
     * +"images/next.gif' border='0'></a>"; s +=
     * "<a href='admin.view_results'><img src='"+Study_Space.file_path
     * +"images/next.gif' border='0'></a>"; s += "</center>";
     * 
     * s += "</td></tr></table>";
     * 
     * s += "</body>"; s += "</html>";
     * 
     * return s; }
     */

   
    /**
     * Prints overview of the page contents.
     * 
     * @return	String	Printable format of this page.
     */
    @Override
    public String toString() {
	String s = "<B>-=Page=-  </B> ";
	s += "ID: " + id + "; ";
	s += "Title: " + title + "<br>";
	// s += "Instructions: "+instructions+"<br>";
	s += "Fields ";
	String[] fields = getFieldList();
	for (int i = 0; i < fieldCount; i++)
	    s += fields[i] + "; ";
	s += " (n=" + fieldCount + "):<br>";

	for (int i = 0; i < items.length; i++)
	    s += items[i].toString();
	s += "Types: ";
	for (int i = 0; i < allValueTypes.length; i++)
	    s += allValueTypes[i] + ";";
	s += "<br> ";
	return s;
    }

}
