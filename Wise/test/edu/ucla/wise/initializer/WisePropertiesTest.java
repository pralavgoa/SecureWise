package edu.ucla.wise.initializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.ucla.wise.shared.properties.AbstractWiseProperties;

public class WisePropertiesTest {

	
	@Test
	public void testPropertiesFile(){
		AbstractWiseProperties properties = new AbstractWiseProperties("conf/dev/wise.properties","WISE");
		
		assertEquals("Check if properties file is read",properties.getStringProperty("admin.server"),"http://localhost/WISE/admin");
	}
}
