package de.unibremen.swp2.service;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAO;
import de.unibremen.swp2.persistence.Interceptors.LockParticipantDAO;
import de.unibremen.swp2.persistence.Interceptors.LockTutorialDAO;
import de.unibremen.swp2.persistence.Interceptors.LockUserDAO;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @Author Tommy
 * Allows to edit,update,delete,creat and to perform other operations
 * on a Meeting (stored in {@link #meetingDAO}).
 */
@RequestScoped
@Transactional(rollbackOn = PersistenceException.class)
public class MeetingService {

    @Inject
    private MeetingDAO meetingDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ParticipantDAO participantDAO;

    @Inject
    private EvaluationDAO evaluationDAO;

    @Inject
    private FinalGradeDAO finalGradeDAO;

    @Inject
    private TutorialDAO tutorialDAO;

    /**
     * creates the given meeting.
     *
     * @param meeting Meeting to create.
     */
    @LockUserDAO
    @LockParticipantDAO
    public void create(final Meeting meeting, final List<Participant> participants, final List<User> lecturers,
                       final User ceo) throws ParticipantNotFoundException, ParticipantAlreadyInMeetingException,
            UserNotPermittedException, UserNotFoundException, UserAlreadyInMeetingException, MeetingNotFoundException
            , UserAlreadyInOtherRoleException {
        meetingDAO.insert(meeting);
        meetingDAO.flush();
        if (ceo != null) {
            if (lecturers.contains(ceo)) {
                throw new UserAlreadyInOtherRoleException();
            }
            UserMeetingRole userMeetingRole = new UserMeetingRole();
            userMeetingRole.setRole(Role.CEO);
            userMeetingRole.setMeeting(meeting);
            userMeetingRole.setUser(ceo);
            meetingDAO.addUserToMeeting(userMeetingRole);
        }
        for (Participant p : participants) {
            ParticipantStatus status = new ParticipantStatus();
            status.setParticipant(p);
            status.setMeeting(meeting);
            meetingDAO.addParticipantToMeeting(status);
        }
        for (User u : lecturers) {
            if (u.getRole() != Role.A && u.getRole() != Role.D) {
                throw new UserNotPermittedException();
            }
            UserMeetingRole userMeetingRole = new UserMeetingRole();
            userMeetingRole.setRole(Role.D);
            userMeetingRole.setUser(u);
            userMeetingRole.setMeeting(meeting);
            meetingDAO.addUserToMeeting(userMeetingRole);
        }
    }

    /**
     * Updates the given meeting.
     *
     * @param meeting Meeting to update.
     * @throws NullPointerException If {@code meeting} is {@code null}.
     */
    @LockMeetingDAO
    @LockParticipantDAO
    @LockUserDAO
    public void update(final Meeting meeting, final List<User> lecturers, final List<Participant> participants,
                       final User ceo) throws NullPointerException, OutdatedException, EntityNotFoundException,
            UserAlreadyInOtherRoleException, UserNotInMeetingException, UserAlreadyInMeetingException,
            UserNotPermittedException, ParticipantAlreadyInMeetingException {
        meetingDAO.update(meeting);
        final List<User> currentLecturers = userDAO.getLecturersByMeeting(meeting);
        final List<Participant> currentParticipants = participantDAO.getAllParticipantsByMeeting(meeting);
        try {
            final User currentCEO = userDAO.getCeoByMeeting(meeting);
            if (!currentCEO.equals(ceo)) {
                meetingDAO.deleteUserFromMeeting(currentCEO, meeting);
            }
            if (ceo != null) {
                this.addCEOToMeeting(ceo, meeting, lecturers);
            }
        } catch (NoResultException e) {
            if (ceo != null) {
                this.addCEOToMeeting(ceo, meeting, lecturers);
            }
        }
        for (User u : lecturers) {
            if (!currentLecturers.contains(u)) {
                if (u.getRole() != Role.D && u.getRole() != Role.A) {
                    throw new UserNotPermittedException();
                }
                final UserMeetingRole userMeetingRole = new UserMeetingRole();
                userMeetingRole.setUser(u);
                userMeetingRole.setMeeting(meeting);
                userMeetingRole.setRole(Role.D);
                meetingDAO.addUserToMeeting(userMeetingRole);
            }
        }
        for (User u : currentLecturers) {
            if (!lecturers.contains(u)) {
                meetingDAO.deleteUserFromMeeting(u, meeting);
            }
        }
        for (Participant p : participants) {
            if (!currentParticipants.contains(p)) {
                final ParticipantStatus participantStatus = new ParticipantStatus();
                participantStatus.setMeeting(meeting);
                participantStatus.setParticipant(p);
                meetingDAO.addParticipantToMeeting(participantStatus);
            }
        }
    }

    /**
     * Adds a user as CEO to a meeting.
     * @param ceo the new ceo.
     * @param meeting the meeting where the CEO is added.
     * @param lecturers
     * @throws UserAlreadyInOtherRoleException
     * @throws UserNotFoundException
     * @throws MeetingNotFoundException
     * @throws UserAlreadyInMeetingException
     */
    @LockMeetingDAO
    @LockUserDAO
    private void addCEOToMeeting(final User ceo, final Meeting meeting, final List<User> lecturers) throws UserAlreadyInOtherRoleException, UserNotFoundException, MeetingNotFoundException, UserAlreadyInMeetingException {
        if (lecturers.contains(ceo)) {
            throw new UserAlreadyInOtherRoleException();
        } else {
            try {
                final UserMeetingRole currentRole = userDAO.getUserMeetingRoleByUserAndMeeting(ceo, meeting);
                currentRole.setRole(Role.CEO);
                meetingDAO.updateUserMeetingRole(currentRole);
            } catch (NoResultException | UserNotInMeetingException | OutdatedException e) {
                final UserMeetingRole userMeetingRole = new UserMeetingRole();
                userMeetingRole.setUser(ceo);
                userMeetingRole.setMeeting(meeting);
                userMeetingRole.setRole(Role.CEO);
                meetingDAO.addUserToMeeting(userMeetingRole);
            }
        }
    }

    /**
     * Updated only the meeting, without the lists.
     * @param meeting the meeting
     * @throws OutdatedException
     * @throws EntityNotFoundException
     */
    @LockMeetingDAO
    public void updateMeetingOnly(final Meeting meeting) throws OutdatedException, EntityNotFoundException {
        meetingDAO.update(meeting);
    }

    /**
     * Deletes the given meeting. Does not fail if {@code meeting} is {@code null}
     * or unknown.
     *
     * @param meeting Meeting to delete.
     */
    @LockMeetingDAO
    public void delete(final Meeting meeting) throws HasEvaluationsException {
        final Long evs = evaluationDAO.countEvaluationsByMeeting(meeting);
        final Long fin = finalGradeDAO.countFinalGradesByMeeting(meeting);
        if (evs == 0 && fin == 0) {
            meetingDAO.delete(meeting);
        } else {
            throw new HasEvaluationsException();
        }
    }

    /**
     * Returns the meeting with given id.
     *
     * @param id Id of the meeting in question.
     * @return Meeting with given id.
     */
    public Meeting getById(final String id) {
        return meetingDAO.getById(id);
    }

    /**
     * Returns all meetings.
     *
     * @return All Meetings.
     */
    public List<Meeting> getAllMeetings() {
        return meetingDAO.getAllMeetings();
    }

    /**
     * Returns the meetings with given user.
     *
     * @param user User of Meetings in question.
     * @return Meetings with given user.
     */
    public List<Meeting> getMeetingsByUser(final User user) {
        if (!user.getRole().equals(Role.D)) {
            return meetingDAO.getVisibleMeetingsByUser(user);
        } else {
            return meetingDAO.getMeetingsByUser(user);
        }
    }

    /**
     * Gets the meeting of a tutorial.
     * @param tutorial the tutorial
     * @return the meeting
     * @throws NoResultException
     */
    public Meeting getMeetingByTutorial(final Tutorial tutorial) throws NoResultException {
        return meetingDAO.getMeetingByTutorial(tutorial);
    }

    /**
     * Gets the meeting by a submission.
     * @param submission the submission
     * @return the meeting
     * @throws NoResultException
     */
    public Meeting getMeetingBySubmission(final Submission submission) throws NoResultException {
        return meetingDAO.getMeetingBySubmission(submission);
    }

    /**
     * Gets the meeting by an exam.
     * @param exam the exam.
     * @return the meeting
     * @throws NoResultException
     */
    public Meeting getMeetingsByExam(final Exam exam) throws NoResultException {
        return meetingDAO.getMeetingsByExam(exam);
    }

    /**
     * Adds a participants to a meeting.
     * @param status the status of the participant
     * @throws ParticipantNotFoundException
     * @throws ParticipantAlreadyInMeetingException
     * @throws MeetingNotFoundException
     */
    @LockMeetingDAO
    @LockParticipantDAO
    @LockTutorialDAO
    public void addParticipantToMeeting(final ParticipantStatus status) throws ParticipantNotFoundException,
            ParticipantAlreadyInMeetingException, MeetingNotFoundException {
        Tutorial tutorial = tutorialDAO.getById(status.getTutorial().getId());
        if (tutorial != null) {
            meetingDAO.addParticipantToMeeting(status);
        }
    }
}