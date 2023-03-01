package de.unibremen.swp2.service;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.persistence.Interceptors.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @Author Tommy
 * Allows to edit,update,delete,creat and to perform other operations
 * on a Tutorial (stored in {@link #tutorialDAO}).
 */
@RequestScoped
@Transactional(rollbackOn = PersistenceException.class)
public class TutorialService {


    @Inject
    private TutorialDAO tutorialDAO;

    @Inject
    private ParticipantDAO participantDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private MeetingDAO meetingDAO;

    /**
     * creates the given tutorial.
     *
     * @param tutorial Tutorial to create.
     */
    @LockMeetingDAO
    @LockTutorialDAO
    @LockParticipantDAO
    @LockUserDAO
    public void create(final Tutorial tutorial, List<Participant> participants, List<User> tutors) throws NullPointerException, TutorialNotFoundException, OutdatedException, ParticipantNotInMeetingException, MeetingNotFoundException {
        tutorialDAO.insert(tutorial);
        tutorialDAO.flush();
        for (Participant p : participants) {
            try {
                ParticipantStatus participantStatus = participantDAO.getParticipantStatusByMeetingAndParticipant(tutorial.getMeeting(), p);
                if (participantStatus.getTutorial() != null) {
                    throw new OutdatedException();
                }
                participantStatus.setTutorial(tutorial);
                tutorialDAO.addParticipantToTutorial(participantStatus);
            } catch (NoResultException e) {
                throw new ParticipantNotInMeetingException();
            }
        }

        for (User u : tutors) {
            try {
                UserMeetingRole userMeetingRole = userDAO.getUserMeetingRoleByUserAndMeeting(u, tutorial.getMeeting());
                userMeetingRole.getTutorials().add(tutorial);
                tutorialDAO.addTutorToTutorial(userMeetingRole, tutorial);
            } catch (NoResultException e) {
                UserMeetingRole userMeetingRole = new UserMeetingRole();
                userMeetingRole.getTutorials().add(tutorial);
                userMeetingRole.setUser(u);
                userMeetingRole.setMeeting(tutorial.getMeeting());
                tutorialDAO.addTutorToTutorial(userMeetingRole, tutorial);
            }
        }
    }


    /**
     * Updates the given tutorial.
     *
     * @param tutorial Tutorial to update.
     * @throws NullPointerException If {@code tutorial} is {@code null}.
     */
    @LockMeetingDAO
    @LockTutorialDAO
    @LockUserDAO
    @LockParticipantDAO
    public void update(final Tutorial tutorial, final List<User> usersToAdd, final List<Participant> participants) throws NullPointerException, TutorialNotFoundException, OutdatedException, EntityNotFoundException, UserNotInMeetingException, UserAlreadyInMeetingException, ParticipantNotInMeetingException {
        Meeting meeting;
        try {
            meeting = meetingDAO.getMeetingByTutorial(tutorial);
        } catch (NoResultException e) {
            throw new MeetingNotFoundException();
        }
        final List<User> currentUsers = userDAO.getUsersByTutorial(tutorial);
        final List<Participant> currentParticipant = participantDAO.getAllParticipantsByTutorial(tutorial);
        for (User u : currentUsers) {
            if (!usersToAdd.contains(u)) {

                final UserMeetingRole userMeetingRole = userDAO.getUserMeetingRoleByTutorialAndUser(tutorial, u);
                userMeetingRole.getTutorials().remove(tutorial);
                meetingDAO.updateUserMeetingRole(userMeetingRole);

            } else {
                usersToAdd.remove(u);
            }
        }
        for (User u : usersToAdd) {
            try {
                final UserMeetingRole userMeetingRole = userDAO.getUserMeetingRoleByUserAndMeeting(u, meeting);
                userMeetingRole.getTutorials().add(tutorial);
                meetingDAO.updateUserMeetingRole(userMeetingRole);
            } catch (NoResultException e) {
                final UserMeetingRole userMeetingRole = new UserMeetingRole();
                userMeetingRole.setMeeting(meeting);
                userMeetingRole.setUser(u);
                userMeetingRole.getTutorials().add(tutorial);
                meetingDAO.addUserToMeeting(userMeetingRole);
            }
        }
        for (Participant p : participants) {
            try {
                ParticipantStatus participantStatus = participantDAO.getParticipantStatusByMeetingAndParticipant(tutorial.getMeeting(), p);
                participantStatus.setTutorial(tutorial);
                tutorialDAO.addParticipantToTutorial(participantStatus);
            } catch (NoResultException e) {
                throw new ParticipantNotInMeetingException();
            }
        }
        for (Participant p : currentParticipant) {
            if (!participants.contains(p)) {
                final ParticipantStatus participantStatus = participantDAO.getParticipantStatusByTutorialAndParticipant(tutorial, p);
                participantStatus.setTutorial(null);
                participantStatus.setGroup(null);
                participantDAO.updateParticipantStatus(participantStatus);
            } else {
                participants.remove(p);
            }

        }
        tutorialDAO.update(tutorial);
    }

    /**
     * Deletes the given tutorial. Does not fail if {@code tutorial} is {@code null}
     * or unknown.
     *
     * @param tutorial Tutorial to delete.
     */
    @LockTutorialDAO
    @LockParticipantDAO
    @LockUserDAO
    @LockMeetingDAO
    public void delete(final Tutorial tutorial) throws OutdatedException, EntityNotFoundException {
        final List<ParticipantStatus> statuses = participantDAO.getParticipantStatusesByTutorial(tutorial);
        for (ParticipantStatus s : statuses) {
            s.setTutorial(null);
            s.setGroup(null);
            participantDAO.updateParticipantStatus(s);
        }
        final List<UserMeetingRole> roles = userDAO.getUserMeetingRolesByTutorial(tutorial);
        for (UserMeetingRole r : roles) {
            r.getTutorials().remove(tutorial);
            userDAO.updateUserMeetingRole(r);
        }
        tutorialDAO.delete(tutorial);
    }

    /**
     * Returns the Tutorial with given id.
     *
     * @param id Id of the tutorial in question.
     * @return Tutorial with given id.
     */
    public Tutorial getById(final String id) {
        return tutorialDAO.getById(id);
    }

    /**
     * Returns the tutorials with given meeting.
     *
     * @param meeting Meeting of the tutorials in question.
     * @return Tutorials with given meeting.
     */
    public List<Tutorial> getTutorialsByMeeting(final Meeting meeting) {
        return tutorialDAO.getTutorialsByMeeting(meeting);
    }

    /**
     * Returns the tutorials with given meeting and user.
     *
     * @param meeting Meeting of the tutorials in question.
     * @param user    User of the tutorials in question.
     * @return Tutorials with given meeting.
     */
    public List<Tutorial> getTutorialByUserAndMeeting(final Meeting meeting, final User user) {
        return tutorialDAO.getTutorialsByUserAndMeeting(meeting, user);
    }

    /**
     * gets the tutorial of a
     * @param tGroup
     * @return
     */
    public Tutorial getTutorialByGroup(final TGroup tGroup) {
        return tutorialDAO.getTutorialByGroup(tGroup);
    }


}