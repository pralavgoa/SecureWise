package edu.ucla.wise.web;

/**
 * Use this class to hold web response messages.
 */
public class WebResponseMessage {

    /**
     * Type of response: ERROR, SUCCESS, FAILURE.
     */
    private WebResponseMessageType type;

    /**
     * Actual response as string.
     */
    private String response;

    /**
     * Constructor.
     * 
     * @param type
     *            Type of response: ERROR, SUCCESS, FAILURE.
     * @param response
     *            Actual response as string.
     */
    public WebResponseMessage(WebResponseMessageType type, String response) {
        super();
        this.type = type;
        this.response = response;
    }

    public WebResponseMessageType getType() {
        return this.type;
    }

    public void setType(WebResponseMessageType type) {
        this.type = type;
    }

    public String getResponse() {
        return this.response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isError() {
        return this.type == WebResponseMessageType.ERROR;
    }

    public boolean isFailure() {
        return this.type == WebResponseMessageType.FAILURE;
    }

    public boolean isSuccess() {
        return this.type == WebResponseMessageType.SUCCESS;
    }

}
