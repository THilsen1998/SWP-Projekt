package de.unibremen.swp2.service;

import lombok.NonNull;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.*;

/**
 * @Author Martin
 * Same as in the mini project.
 */
@Singleton
public class SessionService {

    private final Map<String, Set<HttpSession>> sessions = new HashMap<>();

    @Lock(LockType.WRITE)
    public void loggedIn(@NonNull final String email, @NonNull final HttpSession session) throws NullPointerException {
        session.setAttribute("principle", email);
        sessions.computeIfAbsent(email, __ -> new  HashSet<>()).add(session);
    }

    @Lock(LockType.WRITE)
    public void loggedOut(@NonNull final HttpSession session) throws NullPointerException {
        final String email = (String) session.getAttribute("principle");
        if (email != null) {
            final Set<HttpSession> sessionOfUser = sessions.get(email);
            if (sessionOfUser != null) {
                sessionOfUser.removeIf(s -> s.getId().equals(session.getId()));
                if (sessionOfUser.isEmpty()) {
                    sessions.remove(email);
                }
            }
        }
    }

    @Lock(LockType.WRITE)
    public void logout(@NonNull final String email) throws NullPointerException {
        final Set<HttpSession> sessionOfUser = sessions.get(email);
        if (sessionOfUser != null) {
            for (final HttpSession session : new ArrayList<>(sessionOfUser)) {
                try {
                    session.invalidate();
                } catch (final IllegalStateException ignored) {
                }
            }
        }
    }

    @WebListener
    public static class SessionListener implements HttpSessionListener {

        @Inject
        private SessionService sessionService;

        @Override
        public void sessionDestroyed(HttpSessionEvent se) {
            sessionService.loggedOut(se.getSession());
        }
    }
}
