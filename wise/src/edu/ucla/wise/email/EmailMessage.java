package edu.ucla.wise.email;

public class EmailMessage {

    private final String toEmail;
    private final String salutation;
    private final String lastname;

    public EmailMessage(String toEmail, String salutation, String lastname) {
        this.toEmail = toEmail;
        this.salutation = salutation;
        this.lastname = lastname;
    }

    public String getToEmail() {
        return this.toEmail;
    }

    public String getSalutation() {
        return this.salutation;
    }

    public String getLastname() {
        return this.lastname;
    }
}
