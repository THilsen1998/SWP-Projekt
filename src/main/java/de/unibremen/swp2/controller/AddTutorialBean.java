package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.MeetingNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotInMeetingException;
import de.unibremen.swp2.persistence.Exceptions.TutorialNotFoundException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.service.TutorialService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.RequestParameterMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author Khaled, Dennis
 * Allows to add a new tutorial.
 */

@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class AddTutorialBean implements Serializable {

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Tutorial
     */
    @Inject
    private TutorialService tutorialService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Meeting
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Participant
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a User
     */
    @Inject
    private UserService userService;

    /**
     * Tutorial to add.
     */
    @Getter
    private Tutorial tutorial;

    /**
     Meeting of Tutorial
     */
    @Getter
    private Meeting meeting;

    /**
     * The participants loaded from ParticipantDAO in the System.
     */
    @Getter
    private List<Participant> participants;

    /**
     * The participants loaded from ParticipantDAO in the System filtered.
     */
    @Setter
    @Getter
    private List<Participant> filteredParticipants;

    /**
     Participants that are added to the Meeting
     */
    @Getter
    private List<Participant> participantsToAdd;

    /**
     Participants that are added to the Meeting filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsToAdd;


    /**
     * The user loaded from userDAO in the System.
     */
    @Getter
    private List<User> users;

    /**
     * The user loaded from userDAO in the System filtered.
     */
    @Setter
    @Getter
    private List<User> filteredUsers;

    /**
     Users that are added to the Meeting
     */
    @Getter
    private List<User> usersToAdd;

    /**
     Users that are added to the Meeting filtered
     */
    @Getter
    @Setter
    private List<User> filteredUsersToAdd;

    @Getter
    private List<User> allUsers;

    /**
     All Users filtered
     */
    @Getter
    @Setter
    private List<User> filteredAllUsers;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

    /**
     * Global roles of a user
     */
    @Getter
    private Role role;


    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("meeting-Id");
        if (id != null) {
            meeting = meetingService.getById(id);
            if (meeting != null) {
                final User user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    try {
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {}
                }
                if (role != null && !role.equals(Role.T) && (meeting.getVisible() || role.equals(Role.D) || role.equals(Role.A))) {
                    tutorial = new Tutorial();
                    tutorial.setMeeting(meeting);
                    usersToAdd = new ArrayList<>();
                    participantsToAdd = new ArrayList<>();
                    users = userService.getAllUsers();
                    participants = participantService.getAllParticipantsNotInTutorial(meeting);
                }
            }
        }
    }

    /**
     * Creates a tutorial.
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void createTutorial() throws IOException {

        try {
            tutorialService.create(tutorial, participantsToAdd, usersToAdd);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (TutorialNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Das Tutorium konnte nicht gefunden werden");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Einer der User ist outdated");
            facesContext.addMessage(null, msg);
        } catch (ParticipantNotInMeetingException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Teilnehmer ist nicht in der Veranstaltung zu finden");
            facesContext.addMessage(null, msg);
        } catch (MeetingNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Veranstaltung nicht gefunden");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Adds a participant.
     *
     * @param participant participant to add
     */
    public void addParticipant(Participant participant) {

        if (!participantsToAdd.contains(participant)) {
            participantsToAdd.add(participant);
            participants.remove(participant);
            if (filteredParticipants != null) {
                filteredParticipants.remove(participant);
            }
            if (filteredParticipantsToAdd != null) {
                filteredParticipantsToAdd.add(participant);
            }
        } else {

            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer ist bereits in der Veranstaltung");
            facesContext.addMessage(null, msg);
        }

    }

    /**
     * Deletes a participant.
     *
     * @param participant participant to delete
     */
    public void deleteParticipant(Participant participant) {

        if (participantsToAdd.contains(participant)) {
            participantsToAdd.remove(participant);
            participants.add(participant);
            if (filteredParticipants != null) {
                filteredParticipants.add(participant);
            }
            if (filteredParticipantsToAdd != null) {
                filteredParticipantsToAdd.remove(participant);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Teilnehmer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }

    }

    /**
     * Adds a tutor.
     *
     * @param tutor tutor to add
     */
    public void addTutor(User tutor) {
        if (!usersToAdd.contains(tutor)) {
            usersToAdd.add(tutor);
            users.remove(tutor);
            if (filteredUsers != null) {
                filteredUsers.remove(tutor);
            }
            if (filteredUsersToAdd != null) {
                filteredUsersToAdd.add(tutor);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Nutzer ist bereits in der Veranstaltung");
            facesContext.addMessage(null, msg);
        }

    }


    /**
     * Deletes a tutor.
     *
     * @param tutor tutor to delete
     */
    public void deleteTutor(User tutor) {

        if (usersToAdd.contains(tutor)) {
            usersToAdd.remove(tutor);
            users.add(tutor);
            if (filteredUsers != null) {
                filteredUsers.add(tutor);
            }
            if (filteredUsersToAdd != null) {
                filteredUsersToAdd.remove(tutor);
            }
        } else {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Der Nutzer wurde bereits aus der Veranstaltung entfernt");
            facesContext.addMessage(null, msg);
        }
    }

}

