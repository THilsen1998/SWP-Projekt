package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.service.TutorialService;
import de.unibremen.swp2.service.UserService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.RequestParameterMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;


/**
 * @Author Theo, Dennis
 * Allows to edit a tutorial stored tutorial.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class EditTutorialBean implements Serializable {

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
     * Tutorial to edit.
     */
    @Getter
    private Tutorial tutorial;

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
     Meeting of Tutorial
     */
    @Getter
    private Meeting meeting;

    /**
     * The participants loaded from ParticipantDAO in the Meeting of Tutorial.
     */
    @Getter
    private List<Participant> participantsInMeeting;

    /**
     * The participants loaded from ParticipantDAO in the Meeting of Tutorial filtered.
     */
    @Setter
    @Getter
    private List<Participant> filteredParticipantsInMeeting;

    /**
     Participants that are freshly added to the Tutorial
     */
    @Getter
    private List<Participant> participantsToAdd;

    /**
     Participants that are freshly added to the Tutorial filtered
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipantsToAdd;


    /**
     * The users loaded from userDAO in the System that are capabl to be Tutor.
     */
    @Getter
    private List<User> users;

    /**
     * The users loaded from userDAO in the System that are capabl to be Tutor filtered.
     */
    @Setter
    @Getter
    private List<User> filteredUsers;

    /**
     Users that are freshly added to the Tutorial
     */
    @Getter
    private List<User> usersToAdd;

    /**
     Users that are freshly added to the Tutorial
     */
    @Getter
    @Setter
    private List<User> filteredUsersToAdd;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /*
      Inizilaizes this Bean
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("tutorial-Id");
        if (id != null) {
            tutorial = tutorialService.getById(id);
            if (tutorial != null) {
                meeting = meetingService.getMeetingByTutorial(tutorial);
                final User user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    try {
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {
                    }
                }
                if (role != null && (meeting.getVisible() || role.equals(Role.D) || role.equals(Role.A))) {
                    users = userService.getAllUsersNotInTutorial(tutorial);
                    usersToAdd = userService.getUsersByTutorial(tutorial);
                    participantsInMeeting = participantService.getAllParticipantsNotInTutorial(meeting);
                    participantsToAdd = participantService.getAllParticipantsByTutorial(tutorial);
                }
            }
        }
    }

    /**
     * Updates {@link #tutorial}. On success, a redirect to 'tutorial.xhtml' is
     * registered.
     */
    public void update() throws IOException {

        try {
            tutorialService.update(tutorial, usersToAdd, participantsToAdd);
            externalContext.redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage("Das Tutorium ist veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException | TutorialNotFoundException e) {
            FacesMessage msg = new FacesMessage("Das Tutorium konnte nicht gefunden werden.");
            facesContext.addMessage(null, msg);
        } catch (UserNotInMeetingException e) {
            FacesMessage msg = new FacesMessage("Benutzer konnte in der Veranstaltung nicht gefunden werden.");
            facesContext.addMessage(null, msg);
        } catch (UserAlreadyInMeetingException e) {
            FacesMessage msg = new FacesMessage("Benutzer ist bereits in der Veranstaltung.");
            facesContext.addMessage(null, msg);
        } catch (ParticipantNotInMeetingException e) {
            FacesMessage msg = new FacesMessage("Teilnehmer konnte in der Veranstaltung nicht gefunden werden.");
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
            participantsInMeeting.remove(participant);
            if (filteredParticipantsInMeeting != null) {
                filteredParticipantsInMeeting.remove(participant);
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
            participantsInMeeting.add(participant);
            if (filteredParticipantsInMeeting != null) {
                filteredParticipantsInMeeting.add(participant);
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
     * Deletes a Tutor.
     *
     * @param tutor to add
     */
    @MeetingRole(allowedRoles = {Role.D, Role.CEO, Role.A})
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
     * Deletes a Tutor.
     *
     * @param tutor to delete
     */
    @MeetingRole(allowedRoles = {Role.D, Role.CEO, Role.A})
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
