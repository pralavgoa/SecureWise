package edu.ucla.wsie.studyspacewizard.security;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Test;

public class EncryptionDecryptionTest {

	@Test
	public void encryptDecryptTest(){
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("password");
		String myEncryptedText = textEncryptor.encrypt("Hello");
		System.out.println(myEncryptedText);
		String plainText = textEncryptor.decrypt(myEncryptedText);
		System.out.println(plainText);
	}
	
}
