package edu.ucla.wise.commons.databank.model;

import com.google.gson.Gson;

public class DataRepeatSetInstance {

    private final long id;
    private final String repeatSetName;
    private final String instancePseudoId;
    private final long inviteeId;
    private final String survey;

    public DataRepeatSetInstance(long id, String repeatSetName, String instancePseudoId, long inviteeId, String survey) {
        this.id = id;
        this.repeatSetName = repeatSetName;
        this.instancePseudoId = instancePseudoId;
        this.inviteeId = inviteeId;
        this.survey = survey;
    }

    public long getId() {
        return this.id;
    }

    public String getRepeatSetName() {
        return this.repeatSetName;
    }

    public String getInstancePseudoId() {
        return this.instancePseudoId;
    }

    public long getInviteeId() {
        return this.inviteeId;
    }

    public String getSurvey() {
        return this.survey;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
