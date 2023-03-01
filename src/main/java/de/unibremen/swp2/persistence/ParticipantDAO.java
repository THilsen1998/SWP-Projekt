package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotFoundException;
import de.unibremen.swp2.persistence.Interceptors.LockParticipantDAO;
import de.unibremen.swp2.persistence.Interceptors.LockParticipantDAOInterceptor;
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
public class ParticipantDAO implements DAO<Participant> {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private final static ReentrantLock lock = new ReentrantLock();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    /**
     * Inserts the given participant.
     *
     * @param participant Participant to insert.
     * @throws NullPointerException If {@code participant} is {@code null}.
     */
    public void insert(final @NonNull Participant participant) throws NullPointerException, DuplicateEmailException {
        if (!participant.getEmail().equals("")) {
            try {
                getByEmail(participant.getEmail());
                throw new DuplicateEmailException();
            } catch (NoResultException e) {
                entityManager.persist(participant);
            }
        } else {
            entityManager.persist(participant);
        }
    }

    /**
     * Updates the given participant.
     *
     * @param participant Participant to update.
     * @throws NullPointerException If {@code participant} is {@code null}.
     */
    public void update(final Participant participant) throws NullPointerException, OutdatedException,
            DuplicateEmailException, ParticipantNotFoundException {
        final Participant current = entityManager.find(Participant.class, participant.getId(),
                LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new ParticipantNotFoundException();
        } else if (current.getVersion() == null || !participant.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        }
        if (!participant.getEmail().equals("")) {
            try {
                final Participant other = getByEmail(participant.getEmail());
                if (!participant.equals(other)) {
                    throw new DuplicateEmailException();
                }
            } catch (NoResultException ignored) {
            }
        }
        try {
            entityManager.merge(participant);
        } catch (final OptimisticLockException e) {
            throw new OutdatedException();
        }
    }

    public void updateParticipantStatus(final ParticipantStatus participantStatus) throws EntityNotFoundException,
            OutdatedException {
        final ParticipantStatus currentStatus = entityManager.find(ParticipantStatus.class, participantStatus.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (currentStatus == null) {
            throw new EntityNotFoundException();
        }
        if (currentStatus.getVersion() == null || !participantStatus.getVersion().equals(currentStatus.getVersion())) {
            throw new OutdatedException();
        }

        try {
            entityManager.merge(participantStatus);
        } catch (final OptimisticLockException e) {
            throw new OutdatedException();
        }
    }

    public void delete(final Participant participant) {}

    /**
     * Returns the participant with given id.
     *
     * @param id Id of the participant in question.
     * @return Participant with given id.
     */
    public Participant getById(final String id) {
        return entityManager.find(Participant.class, id);
    }

    private Participant getByEmail(final String email) throws NoResultException {
        return entityManager.
                createQuery("SELECT p FROM Participant p WHERE p.email = :email", Participant.class).
                setParameter("email", email).
                getSingleResult();
    }

    /**
     * Returns all participants.
     *
     * @return All Participants.
     */
    public List<Participant> getAllParticipants() {
        return entityManager.createQuery("SELECT p FROM Participant p", Participant.class).getResultList();
    }

    /**
     * Returns the participants with given tutorial.
     *
     * @param tutorial Tutorial of Participants in question.
     * @return Participants with given and tutorial.
     */
    public List<Participant> getAllParticipantsByTutorial(final Tutorial tutorial) {
        return entityManager.
                createQuery("SELECT p FROM Participant p JOIN p.participantStatuses s WHERE s.tutorial = :Tutorial",
                        Participant.class).
                setParameter("Tutorial", tutorial).
                getResultList();
    }

    /**
     * Returns the participants with given Meeting.
     *
     * @param meeting Meeting of Participants in question.
     * @return Participants with given meeting.
     */
    public List<Participant> getAllParticipantsByMeeting(final Meeting meeting) {
        return entityManager.
                createQuery("SELECT p FROM Participant p JOIN p.participantStatuses s WHERE s.meeting = :Meeting",
                        Participant.class).
                setParameter("Meeting", meeting).
                getResultList();
    }

    /**
     * Returns the participants with given group.
     *
     * @param tGroup Group of Participants in question.
     * @return Participants with given group.
     */
    public List<Participant> getParticipantsByGroup(final TGroup tGroup) {
        return entityManager.createQuery("SELECT p FROM Participant p JOIN p.participantStatuses s WHERE s.group = " + ":gr", Participant.class).setParameter("gr", tGroup).getResultList();

    }

    /**
     * Returns the participants with given exam.
     *
     * @param exam Exam of Participants in question.
     * @return Participants with given exam.
     */
    public List<Participant> getParticipantsByExam(final Exam exam) {
        return null;
    }

    /**
     * Returns the participants with given submision.
     *
     * @param submission Submission of Participants in question.
     * @return Participants with given submission.
     */
    public List<Participant> getParticipantsBySubmission(final Submission submission) {
        return null;
    }

    /**
     * Returns the participants with given submision.
     *
     * @param meeting meeting of Participants in question.
     * @return Participants with given submission.
     */
    public List<ParticipantStatus> getParticipantStatusByMeeting(final Meeting meeting) {
        return entityManager.createQuery("SELECT s FROM ParticipantStatus s WHERE s.meeting = :meeting",
                ParticipantStatus.class).
                setParameter("meeting", meeting).getResultList();
    }
    /**
     * Returns ParticipantStatus with given participant and meeting
     * @param meeting meeting of Participants in question.
     *@param participant  participant of meeting in question.
     * @return ParticipantStatus .
     */
    public ParticipantStatus getParticipantStatusByMeetingAndParticipant(final Meeting meeting,
                                                                         final Participant participant) throws NoResultException {
        return entityManager.
                createQuery("SELECT p FROM ParticipantStatus p WHERE p.meeting = :meeting AND p.participant = " +
                        ":participant", ParticipantStatus.class).
                setParameter("meeting", meeting).setParameter("participant", participant).
                getSingleResult();
    }
    /**
     * Returns ParticipantStatus with given id
     * @param id id of Participant in question.
     * @return ParticipantStatus .
     */
    public ParticipantStatus getParticipantStatusById(final String id) {
        return entityManager.find(ParticipantStatus.class, id);
    }
    /**
     * Returns Participants with given meeting
     * @param meeting in question.
     * @return Participants .
     */
    public List<Participant> getParticipantsNotInMeeting(final Meeting meeting) {
        return entityManager.
                createQuery("SELECT p FROM Participant p WHERE NOT EXISTS (SELECT 1 FROM ParticipantStatus s WHERE s" + ".meeting = :meeting AND s.participant = p)", Participant.class).
                setParameter("meeting", meeting).
                getResultList();
    }
    /**
     * Returns Participants with given participant and tutorial
     * @param tutorial in question.
     * @param participant in question.
     * @throws NoResultException
     * @return ParticipantStatus .
     */
    public ParticipantStatus getParticipantStatusByTutorialAndParticipant(final Tutorial tutorial,
                                                                          final Participant participant) throws NoResultException {
        return entityManager.createQuery("SELECT p FROM ParticipantStatus p WHERE p.participant = :participant AND p" + ".tutorial = :tutorial", ParticipantStatus.class).setParameter("participant", participant).setParameter("tutorial", tutorial).getSingleResult();
    }
    /**
     * Returns Participants with given tutorial and participant
     * @param tutorial in question.
     * @param tGroup in question.
     * @return Participants .
     */
    public List<Participant> getParticipantsNotInGroup(final Tutorial tutorial, final TGroup tGroup) {
        return entityManager.createQuery("SELECT p FROM Participant p JOIN p.participantStatuses s WHERE s.tutorial " +
                "=" + " :tutorial AND NOT EXISTS (SELECT 1 FROM ParticipantStatus st WHERE st.group = :tGroup AND st" + ".participant = p)", Participant.class).
                setParameter("tutorial", tutorial).setParameter("tGroup", tGroup).getResultList();
    }
    /**
     * Returns Participants with given meeting and participant
     * @param participant in question.
     * @param meeting in question.
     *@throws NoResultException
     * @return ParticipantStatus .
     */
    public ParticipantStatus getParticipantStatusByParticipantAndMeeting(final Participant participant,
                                                                         final Meeting meeting) throws NoResultException {
        return entityManager.createQuery("SELECT s FROM ParticipantStatus s WHERE s.participant = :participant AND s" + ".meeting = :meeting", ParticipantStatus.class).setParameter("participant", participant).setParameter("meeting", meeting).getSingleResult();
    }
    /**
     * Returns Participants with given tGroup and participant
     * @param tGroup in question.
     * @param participant in question.
     * @return ParticipantStatus .
     */
    public ParticipantStatus getParticipantStatusByParticipantAndGroup(final Participant participant,
                                                                       final TGroup tGroup) {
        return entityManager.createQuery("SELECT s FROM ParticipantStatus s WHERE s.participant = :participant AND s" + ".group = :tGroup", ParticipantStatus.class).setParameter("participant", participant).setParameter("tGroup", tGroup).getSingleResult();
    }
    /**
     * Returns Participants with given tGroup and participant
     * @param status status in question.
     * @return Participants .
     */
    public Participant getParticipantByParticipantStatus(final ParticipantStatus status) throws NoResultException {
        return entityManager.createQuery("SELECT p FROM Participant p JOIN p.participantStatuses ps WHERE ps = " +
                ":status", Participant.class).setParameter("status", status).getSingleResult();
    }
    /**
     * Returns Participants with given meeting
     * @param meeting status in question.
     * @return Participants .
     */
    public List<Participant> getAllParticipantsNotInTutorial(final Meeting meeting) {
        return entityManager.createQuery("SELECT p FROM Participant p JOIN p.participantStatuses s WHERE s.meeting = "
                + ":meeting AND s.tutorial IS NULL", Participant.class).setParameter("meeting", meeting).getResultList();
    }

    /**
     * Returns number of participant in specific Tutorial
     * @param meeting meeting in question.
     * @param participant participant in question.
     * @param user user in question.
     * @return number long .
     */
    public Long getCountOfParticipantInUserTutorial(final User user, final Participant participant,
                                                    final Meeting meeting) {
        Query q = entityManager.createQuery("SELECT COUNT(t) FROM Tutorial t JOIN t.participantStatuses ps JOIN t" +
                ".userMeetingRoles ur WHERE t.meeting = :meeting AND ps.participant = :participant AND ur.user = " +
                ":user").setParameter("meeting", meeting).setParameter("participant", participant).setParameter("user"
                , user);
        return (Long) q.getResultList().get(0);
    }
    /**
     * Returns list of participant in specific meeting
     * @param meeting meeting in question.
     * @param user user in question.
     * @return Participants.
     */
    public List<Participant> getParticipantsByMeetingAndUser(final Meeting meeting, final User user) {
        return entityManager.createQuery("SELECT p FROM Participant p JOIN p.participantStatuses s JOIN s.tutorial" + ".userMeetingRoles u WHERE s.meeting = :meeting AND u.user = :user", Participant.class).setParameter("meeting", meeting).setParameter("user", user).getResultList();
    }
    /**
     * Returns a list of participant which have no group in a specific tutorial
     * @param tutorial tutorial in question.
     * @return Participants.
     */
    public List<Participant> getParticipantsNotInAnyGroup(final Tutorial tutorial) {
        return entityManager.createQuery("SELECT p FROM Participant p JOIN p.participantStatuses s WHERE s.tutorial =" +
                " :tutorial AND s.group IS NULL", Participant.class).setParameter("tutorial", tutorial).getResultList();
    }
    /**
     * Returns a list of participant  in a specific tutorial
     * @param tutorial tutorial in question.
     * @return Participants.
     */
    public List<ParticipantStatus> getParticipantStatusesByTutorial(final Tutorial tutorial) {
        return entityManager.createQuery("SELECT s FROM ParticipantStatus s WHERE s.tutorial = :tutorial",
                ParticipantStatus.class).setParameter("tutorial", tutorial).getResultList();
    }
}