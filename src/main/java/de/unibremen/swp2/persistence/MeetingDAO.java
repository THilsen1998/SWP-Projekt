package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAOInterceptor;
import lombok.NonNull;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.persistence.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
@Author Martin
 * Process date from and into the data base
 */

@RequestScoped
public class MeetingDAO implements DAO<Meeting> {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private final static ReentrantLock reentrantLock = new ReentrantLock();

    public void lock() {
        reentrantLock.lock();
    }

    public void unlock() {
        reentrantLock.unlock();
    }

    /**
     * Inserts the given meeting.
     *
     * @param meeting
     *      Meeting to insert.
     * @throws NullPointerException
     *      If {@code meeting} is {@code null}.
     */
    public void insert(final @NonNull Meeting meeting) throws NullPointerException {
        entityManager.persist(meeting);
    }

    /**
     * Updates the given meeting.
     *
     * @param meeting
     *      Meeting to update.
     * @throws NullPointerException
     *      If {@code meeting} is {@code null}.
     */
    public void update(final Meeting meeting) throws NullPointerException, de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException, OutdatedException {
        final Meeting current = entityManager.find(Meeting.class, meeting.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new EntityNotFoundException();
        } else if (current.getVersion() == null || !meeting.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(meeting);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }

    /**
     * Deletes the given meeting. Does not fail if {@code meeting} is {@code null}
     * or unknown.
     *
     * @param meeting
     *      Meeting to delete.
     */
    public void delete(final Meeting meeting){
        final Meeting current = entityManager.find(Meeting.class, meeting.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current != null) {
            entityManager.remove(current);
        }
    }

    /**
     * Returns the meeting with given id.
     *
     * @param id
     *      Id of the meeting in question.
     * @return
     *      Meeting with given id.
     */
    public Meeting getById(final String id) {
        return entityManager.find(Meeting.class, id);
    }

    /**
     * Returns all meetings.
     * @return
     *      All Meetings.
     */
    public List<Meeting> getAllMeetings(){
        return entityManager.createQuery("SELECT m FROM Meeting m", Meeting.class).getResultList();
    }

    /**
     * Returns the meetings with given user.
     *
     * @param user
     *      User of Meetings in question.
     * @return
     *      Meetings with given user.
     */
    public List<Meeting> getMeetingsByUser(final User user) {
        return entityManager.createQuery("SELECT m FROM Meeting m JOIN m.userMeetingRoles r WHERE r.user = :user", Meeting.class).
                setParameter("user", user).
                getResultList();
    }
    /**
     * Returns the meetings with given exam.
     *
     * @param exam
     *      exam of Meetings in question.
     * @return
     *      Meetings with given exam.
     */
    public Meeting getMeetingsByExam(final Exam exam) throws NoResultException {
        return entityManager.createQuery("SELECT m FROM Meeting m JOIN m.exams e WHERE e = :exam", Meeting.class).setParameter("exam",exam).getSingleResult();
    }
    /**
     * Returns the visible meetings by given user.
     *
     * @param user
     *      User of Meetings in question.
     * @return a list of
     *      Meetings with given user.
     */

    public List<Meeting> getVisibleMeetingsByUser(final User user) {
        return entityManager.
                createQuery("SELECT m FROM Meeting m JOIN m.userMeetingRoles r WHERE r.user = :user AND m.visible = true", Meeting.class).
                setParameter("user", user).getResultList();
    }

    public void flush() {
        entityManager.flush();
    }
    /**
     * adds a user to a meeting .
     *
     * @param userRole
     *      User in a specific meeting in question.
     */
    public void addUserToMeeting(final UserMeetingRole userRole) throws UserAlreadyInMeetingException, UserNotFoundException, MeetingNotFoundException {
        final Meeting meeting = entityManager.find(Meeting.class, userRole.getMeeting().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (meeting == null) {
            throw new MeetingNotFoundException();
        }
        final User user = entityManager.find(User.class, userRole.getUser().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (user == null) {
            throw new UserNotFoundException();
        }
        try {
            entityManager.
                    createQuery("SELECT r FROM UserMeetingRole r WHERE r.meeting = :Meeting AND r.user = :User", UserMeetingRole.class).
                    setParameter("Meeting", meeting).
                    setParameter("User", user).
                    getSingleResult();
            throw new UserAlreadyInMeetingException();
        } catch (final NoResultException ignored) {
        }
        entityManager.persist(userRole);
    }
    /**
     * adds a participant to a meeting .
     *
     * @param status
     *      participant in a specific meeting in question.
     */
    public void addParticipantToMeeting(final ParticipantStatus status) throws ParticipantNotFoundException, ParticipantAlreadyInMeetingException, MeetingNotFoundException {
        Meeting meeting = entityManager.find(Meeting.class, status.getMeeting().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (meeting == null) {
            throw new MeetingNotFoundException();
        }
        final Participant participant = entityManager.find(Participant.class, status.getParticipant().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (participant == null) {
            throw new ParticipantNotFoundException();
        }
        try {
            entityManager.
                    createQuery("SELECT s FROM ParticipantStatus s WHERE s.meeting = :Meeting AND s.participant = :Participant", ParticipantStatus.class).
                    setParameter("Meeting", meeting).
                    setParameter("Participant", participant).
                    getSingleResult();
            throw new ParticipantAlreadyInMeetingException();
        } catch (final NoResultException ignored) {
        }
        entityManager.persist(status);
    }

    /**
     * deletes an user from a given meeting .
     *
     * @param user
     * @param meeting
     *      participant in a specific meeting in question.
     */
    public void deleteUserFromMeeting(final User user, final Meeting meeting) {
        try {
            final UserMeetingRole role = entityManager.
                    createQuery("SELECT r FROM UserMeetingRole r WHERE r.meeting = :meeting AND r.user = :user", UserMeetingRole.class).
                    setParameter("meeting", meeting).setParameter("user", user).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                    getSingleResult();
            entityManager.remove(role);
        } catch (NoResultException ignored) {
        }
    }
    /**
     * updates the user in a specific meeting  .
     *
     * @param userMeetingRole
     *      user in a specific meeting in question.
     */
    public void updateUserMeetingRole(final UserMeetingRole userMeetingRole) throws UserNotInMeetingException, OutdatedException {
        final UserMeetingRole currentRole = entityManager.find(UserMeetingRole.class, userMeetingRole.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (currentRole == null) {
            throw new UserNotInMeetingException();
        } else if (currentRole.getVersion() == null || !userMeetingRole.getVersion().equals(currentRole.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(userMeetingRole);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }

    /**
     * Returns the meetings with given user.
     *
     * @param tutorial
     *      Tutorial of Meetings in question.
     * @return
     *      Meetings with given tutorial.
     */
    public Meeting getMeetingByTutorial(final Tutorial tutorial) throws NoResultException {
        return entityManager.createQuery("SELECT m FROM Meeting m JOIN m.tutorials t WHERE t = :tutorial", Meeting.class).setParameter("tutorial", tutorial).getSingleResult();
    }
    /**
     * Returns the meetings with given submission.
     *
     * @param submission
     *      Submission of Meetings in question.
     * @return
     *      Meetings with given submission.
     */
    public Meeting getMeetingBySubmission(final Submission submission) throws NoResultException {
        return entityManager.createQuery("SELECT m FROM Meeting m JOIN m.submissions s WHERE s = :submission", Meeting.class).setParameter("submission", submission).getSingleResult();
    }
}