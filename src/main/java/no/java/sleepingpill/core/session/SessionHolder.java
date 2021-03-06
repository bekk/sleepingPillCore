package no.java.sleepingpill.core.session;

import no.java.sleepingpill.core.ServiceLocator;
import no.java.sleepingpill.core.event.Event;
import no.java.sleepingpill.core.event.EventListener;
import no.java.sleepingpill.core.event.EventType;
import no.java.sleepingpill.core.exceptions.InternalError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SessionHolder implements EventListener {

    private final List<Session> sessions = new ArrayList<>();

    public static SessionHolder instance() {
        return ServiceLocator.sessionHolder();
    }

    @Override
    public void eventAdded(Event event) {

        if (event.eventType == EventType.NEW_SESSION) {
            handleNewSession(event);
        }
        if (event.eventType == EventType.UPDATE_SESSION) {
            handleUpdateSession(event);
        }
        if (event.eventType == EventType.DELETE_SESSION) {
            handleDeleteSession(event);
        }
    }

    private void handleDeleteSession(Event event) {
        String sessionId = event.data.requiredString(SessionVariables.SESSION_ID);
        synchronized (sessions) {
            Session session = sessions.stream()
                    .filter(se -> se.getId().equals(sessionId))
                    .findAny()
                    .orElseThrow(() -> new InternalError("Unknown session id in update " + sessionId));
            sessions.remove(session);
        }
    }

    public void clear() {
        sessions.clear();
    }

    private void handleUpdateSession(Event event) {
        synchronized (sessions) {
            String sessionId = event.data.requiredString(SessionVariables.SESSION_ID);
            Session session = sessions.stream()
                    .filter(se -> se.getId().equals(sessionId))
                    .findAny()
                    .orElseThrow(() -> new InternalError("Unknown session id in update " + sessionId));
            session.addData(event.data);
            event.data.stringValue(SessionVariables.SESSION_STATUS)
                    .map(SessionStatus::valueOf)
                    .ifPresent(sessionStatus -> {
                        session.setSessionStatus(sessionStatus);
                        if (sessionStatus == SessionStatus.SUBMITTED) {
                            session.setSubmittedTime(event.index);
                        }
                    });
        }
    }

    private void handleNewSession(Event event) {
        String sessionId = event.data.requiredString(SessionVariables.SESSION_ID);
        String conferenceId = event.data.requiredString(SessionVariables.CONFERENCE_ID);
        Optional<String> addedByEmail = event.data.stringValue(SessionVariables.POSTED_BY_MAIL);
        Session session = new Session(sessionId, conferenceId, addedByEmail);
        event.data.stringValue(SessionVariables.SESSION_STATUS)
                .map(SessionStatus::valueOf)
                .ifPresent(sessionStatus -> {
                    session.setSessionStatus(sessionStatus);
                    if (sessionStatus == SessionStatus.SUBMITTED) {
                        session.setSubmittedTime(event.index);
                    }
                });
        session.addData(event.data);

        synchronized (sessions) {
            sessions.add(session);
        }
    }

    public List<Session> allSessions() {
        synchronized (sessions) {
            return new ArrayList<>(sessions);
        }
    }



    public Optional<Session> sessionFromId(String sessionId) {
        synchronized (sessions) {
            return sessions.stream()
                    .filter(se -> se.getId().equals(sessionId))
                    .findAny();
        }
    }

    public List<Session> sessionsByEmail(String email) {
        if (email == null) {
            return Collections.emptyList();
        }
        synchronized (sessions) {
            return sessions.stream()
                    .filter(se -> se.isRelatedToEmail(email))
                    .collect(Collectors.toList());
        }
    }

}
