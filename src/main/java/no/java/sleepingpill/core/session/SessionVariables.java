package no.java.sleepingpill.core.session;

public class SessionVariables {
    public static final String DATA_OBJECT = "data";
    public static final String SPEAKER_ARRAY = "speakers";
    public static final String POSTED_BY_MAIL = "postedBy";
    public static final String VALUE_KEY = "value";
    public static final String PRIVATE_FLAG = "privateData";
    public static final String SESSION_STATUS = "status";
    public static final String SESSION_ID = "sessionId";
    public static final String CONFERENCE_ID = "conferenceId";
    public static final String SPEAKER_NAME = "name";
    public static final String SPEAKER_EMAIL = "email";

    private SessionVariables() {
        throw new IllegalAccessError("No instance allowed");
    }
}
