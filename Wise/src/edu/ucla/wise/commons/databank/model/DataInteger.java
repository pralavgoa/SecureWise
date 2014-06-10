package edu.ucla.wise.commons.databank.model;

public class DataInteger extends MainData {

    private final int answer;

    public DataInteger(long id, String survey, long inviteeId, String questionId, int level, int answer) {
        super(id, survey, inviteeId, questionId, level);
        this.answer = answer;
    }

    @Override
    public Object getAnswer() {
        return this.answer;
    }

    @Override
    public DataType getDataType() {
        return DataType.INTEGER;
    }

}
