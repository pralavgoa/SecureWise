package edu.ucla.wise.initializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WisePropertiesTest {

	
	@Test
	public void testPropertiesFile(){
		AbstractWiseProperties properties = new AbstractWiseProperties("conf/dev/wise.properties","WISE");
		
		assertEquals("",properties.getStringProperty("admin.server"),"http://localhost/WISE/admin");
	}
}
