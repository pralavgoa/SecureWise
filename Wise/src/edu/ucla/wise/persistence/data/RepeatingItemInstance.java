package edu.ucla.wise.persistence.data;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class RepeatingItemInstance {

    private final String itemSetName;
    private final String instanceName;
    private final Map<String, Answer> answers;

    public RepeatingItemInstance(String itemSetName, String instanceName) {
        this.instanceName = instanceName;
        this.itemSetName = itemSetName;
        this.answers = new HashMap<>();
    }

    public void addAnswer(String qId, Answer answer) {
        this.answers.put(qId, answer);
    }

    public String getItemSetName() {
        return this.itemSetName;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public Map<String, Answer> getAnswers() {
        return this.answers;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
