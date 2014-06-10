package edu.ucla.wise.persistence.data;

public class WiseTables {

    private static final String SURVEY_USER_PAGE_STATUS_TABLE = "survey_user_page";
    private static final String MAIN_DATA_TEXT_TABLE = "data_text";
    private static final String MAIN_DATA_INTEGER_TABLE = "data_integer";
    private static final String INVITEE_TABLE = "invitee";
    private static final String DATA_RPT_INS_TO_QUES_ID_TABLE = "data_rpt_ins_id_to_ques_id";
    private static final String DATA_REPEAT_SET_INSTANCE_TABLE = "data_repeat_set_instance";
    private static final String SURVEYS_TABLE = "surveys";
    private static final String SURVEY_MESSAGE_USE_TABLE = "survey_message_use";
    private static final String INTERVIEW_ASSIGNMENT_TABLE = "interview_assignment";
    private static final String CONSENT_RESPONSE_TABLE = "consent_response";
    private static final String SURVEY_USER_STATE_TABLE = "survey_user_state";
    private static final String PAGE_SUBMIT_TABLE = "page_submit";
    private static final String PENDING_TABLE = "pending";

    private final String schemaName;

    public WiseTables(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSurveyUserPageStatus() {
        return this.getSchemaPrefix() + SURVEY_USER_PAGE_STATUS_TABLE;
    }

    public String getMainDataText() {
        return this.getSchemaPrefix() + MAIN_DATA_TEXT_TABLE;
    }

    public String getMainDataInteger() {
        return this.getSchemaPrefix() + MAIN_DATA_INTEGER_TABLE;
    }

    public String getInvitee() {
        return this.getSchemaPrefix() + INVITEE_TABLE;
    }

    public String getDataRepeatInstanceToQuestionId() {
        return this.getSchemaPrefix() + DATA_RPT_INS_TO_QUES_ID_TABLE;
    }

    public String getDataRepeatSetToInstance() {
        return this.getSchemaPrefix() + DATA_REPEAT_SET_INSTANCE_TABLE;
    }

    private String getSchemaPrefix() {
        return this.schemaName + ".";
    }

    public String getSurveys() {
        return this.getSchemaPrefix() + SURVEYS_TABLE;
    }

    public String getSurveyMessageUse() {
        return this.getSchemaPrefix() + SURVEY_MESSAGE_USE_TABLE;
    }

    public String getInterviewAssignment() {
        return this.getSchemaPrefix() + INTERVIEW_ASSIGNMENT_TABLE;
    }

    public String getConsentResponse() {
        return this.getSchemaPrefix() + CONSENT_RESPONSE_TABLE;
    }

    public String getSurveyUserState() {
        return this.getSchemaPrefix() + SURVEY_USER_STATE_TABLE;
    }

    public String getPageSubmit() {
        return this.getSchemaPrefix() + PAGE_SUBMIT_TABLE;
    }

    public String getPending() {
        return this.getSchemaPrefix() + PENDING_TABLE;
    }
}
