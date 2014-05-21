package edu.ucla.wise.persistence.data;

public class Answer {

    public static enum Type {
        TEXT, INTEGER, DECIMAL
    };

    private final Object answer;
    private final Type type;

    public Answer(Object answer, Type type) {

        if (type == Type.TEXT) {
            String answerAsString = (String) answer;
            this.answer = answerAsString;
        } else {
            int answerAsInt = (int) answer;
            this.answer = answerAsInt;
        }

        this.type = type;

    }

    public Object getAnswer() {
        return this.answer;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        if (this.type == Type.TEXT) {
            return (String) this.answer;
        } else {
            return "" + this.answer;
        }
    }

    public static Answer getAnswer(Object answer, String type) {
        if ("text".equals(type) || "textarea".equals(type)) {
            return new Answer(answer, Type.TEXT);
        } else {
            return new Answer(answer, Type.INTEGER);
        }
    }
}
