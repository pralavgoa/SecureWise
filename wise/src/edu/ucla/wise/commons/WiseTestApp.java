/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.commons;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * WiseTestApp class is used to test the creation of survey and user object
 * along with some methods.
 */
public class WiseTestApp {

    public static void main(String[] args) {
        try {

            String file_loc = "file:///home/manoj/workspace/JBOSS_WISE/lib/Enrollmt.xml";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setCoalescing(true);
            factory.setExpandEntityReferences(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            Document xml_doc = factory.newDocumentBuilder().parse(file_loc);
            Survey s = new Survey(xml_doc, null);
            System.out.println(s.toString());

            HashMap<String, Object> params = new HashMap<>();
            String[][] input = { { "PRIOR_CME_1", "1" }, { "PRIOR_CME_4", "2" }, { "COMP_ATTITUDES_8", "2" },
                    { "NP_SPECIALTY_1", "1" } };
            for (int i = 0; i < input.length; i++) {
                params.put(input[i][0], input[i][1]);
            }
            User testUser = new User(s);
            System.out.println("Got user" + testUser.getId());
            System.out.println("Before advancing");
            testUser.readAndAdvancePage(params, false);
            System.out.println("After advancing");
            System.out.println(testUser.getFieldValue("subj_type"));
            String p_output = testUser.getCurrentPage().renderPage(testUser);
            System.out.println(p_output);

        } catch (FactoryConfigurationError e) {
            System.out.println("Exception handling");
            System.out.println(e);
        } catch (SAXException e) {
            System.out.println("Exception handling");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Exception handling");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.out.println("Exception handling");
            e.printStackTrace();
        }
    }
}
