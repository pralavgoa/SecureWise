package edu.ucla.wise.commons.databank.model;

import com.google.gson.Gson;

public abstract class MainData {

    public enum DataType {
        TEXT, INTEGER
    }

    private final long id;
    private final String survey;
    private final long inviteeId;
    private final String questionId;
    private final int level;

    public long getId() {
        return this.id;
    }

    public String getSurvey() {
        return this.survey;
    }

    public long getInviteeId() {
        return this.inviteeId;
    }

    public String getQuestionId() {
        return this.questionId;
    }

    public int getLevel() {
        return this.level;
    }

    public abstract Object getAnswer();

    public abstract DataType getDataType();

    public MainData(long id, String survey, long inviteeId, String questionId, int level) {
        super();
        this.id = id;
        this.survey = survey;
        this.inviteeId = inviteeId;
        this.questionId = questionId;
        this.level = level;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
