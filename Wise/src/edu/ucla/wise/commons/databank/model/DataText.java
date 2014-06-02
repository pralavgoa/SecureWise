package edu.ucla.wise.commons.databank.model;

public class DataText extends MainData {

    private final String answer;

    public DataText(long id, String survey, long inviteeId, String questionId, int level, String answer) {
        super(id, survey, inviteeId, questionId, level);
        this.answer = answer;
    }

    @Override
    public String getAnswer() {
        return this.answer;
    }

    @Override
    public DataType getDataType() {
        return DataType.TEXT;
    }
}
