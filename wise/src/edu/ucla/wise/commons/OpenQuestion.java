package edu.ucla.wise.commons;

import org.w3c.dom.Node;

/**
 * This class is a subclass of Question and represents an open ended question on
 * the page
 * 
 * @author Douglas Bell
 * @version 1.0  
 *
 */
public class OpenQuestion extends Question {

	/**
	 * Constructor for an open ended question
	 * 
	 * @param n		XML DOM structure from which the details of the question are obtained.
	 */
    public OpenQuestion(Node n) {
		/* get the attributes for page item & question */
		super(n);
    }

    /**
     * Counts number of field for an open ended question - each has only one
     * field.
     * 
     * @return	int	Always returns one since the open ended question will have just one field.
     */
    @Override
    public int countFields() {
    	return 1;
    }

    /**
     * Just renders html; unlike closed- and question blocks, 
     * no shared-element references to knit.
     * 
     * @param	mySurvey	The survey to which this question is linked.
     */
    public void knitRefs(Survey mySurvey) {
    	html = makeHtml();
    }

    /**
     * Renders a form for an open ended question at the time of loading the survey.
     * 
     * @return	String	HTML format of the open ended question Block.
     */
    public String makeHtml() {
    	String s = "\n<table cellspacing='0' cellpadding='0' width=100%' border='0'><tr><td>"
    			+ "\n<table cellspacing='0' cellpadding='0' width=100%' border='0'><tr><td>";
    	
    	/* add/open the question stem row */
    	s += super.makeStemHtml();
    	
    	/* start new row if it is not requested by one-line layout */
    	if (!oneLine) {
    		s += "\n<tr>";
    		s += "<td width=10>&nbsp;</td>";
    		s += "<td width=20>&nbsp;</td>";
    		s += "<td width=570>";
    	} else {
    		s += "&nbsp;&nbsp;";
    	}
    	s += this.formFieldHtml();
    	s += "</td></tr></table>\n</td></tr></table>";
    	return s;
    }

    /** Placeholder; subclasses need to override
     * 
     * @return	String 	returns empty string here.
     */    
    public String formFieldHtml() {
    	return "";
    }

    /** renders results for an open ended question */
    /*
     * public String render_results(Hashtable data, String whereclause) { return
     * super.render_results(data, whereclause); }
     */

    /** returns a comma delimited list of all the fields on a page */
    /*
     * public String list_fields() { return name+","; }
     */
    /** prints information for an open ended question */
    /*
     * public String print() { return super.print(); }
     */

    /**
     * print survey for an open ended question - used for admin tool: print
     * survey
     */
    /*
     * public String print_survey() { String s = super.render_form(); s +=
     * this.common_render_form(); return s; }
     */
}
