package edu.ucla.wise.commons.databank.model;

public class DataRepeatSetInstanceToQuestionId {
    private final long id;
    private final long repeatInstanceId;
    private final long questionId;
    private final String questionType;

    public DataRepeatSetInstanceToQuestionId(long id, long repeatInstanceId, long questionId, String questionType) {
        this.id = id;
        this.repeatInstanceId = repeatInstanceId;
        this.questionId = questionId;
        this.questionType = questionType;
    }

    public long getId() {
        return this.id;
    }

    public long getRepeatInstanceId() {
        return this.repeatInstanceId;
    }

    public long getQuestionId() {
        return this.questionId;
    }

    public String getQuestionType() {
        return this.questionType;
    }

}
