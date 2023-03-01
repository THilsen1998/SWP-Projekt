package de.unibremen.swp2.persistence.Interceptors;

import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAO;
import de.unibremen.swp2.persistence.MeetingDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@LockMeetingDAO
public class LockMeetingDAOInterceptor implements Serializable {

    @Inject
    private MeetingDAO meetingDAO;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        meetingDAO.lock();
        try {
            return ctx.proceed();
        } finally {
            meetingDAO.unlock();
        }
    }
}
