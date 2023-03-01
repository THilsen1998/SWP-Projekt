package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.MeetingNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantAlreadyInMeetingException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotFoundException;
import de.unibremen.swp2.security.GlobalSecure;
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
import javax.mail.Part;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

/**
 * @Author Theo, Khaled
 * Allows to add a new participant.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class AddParticipantBean implements Serializable {

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

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
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String,String> parameterMap;

    /**
     * Participant to add.
     */
    @Getter
    private Participant participant;

    /**
     * Tutorial where the Participant is in
     */
    private Tutorial tutorial;

    /**
     * Meeting where the Participant is in
     */
    private Meeting meeting;

    /**
     * Global roles of a user
     */
    @Setter
    private Role role;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        participant = new Participant();
        final String id = parameterMap.get("tutorial-Id");
        if (id != null) {
            tutorial = tutorialService.getById(id);
            if (tutorial != null) {
                meeting = meetingService.getMeetingByTutorial(tutorial);
                final User user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    UserMeetingRole userMeetingRole;
                    try {
                        userMeetingRole = userService.getUserMeetingRoleByTutorialAndUser(tutorial, user);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {
                        try {
                            userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                            role = userMeetingRole.getRole();
                        } catch (NoResultException ignored2) {}
                    }
                }
            }
        }
    }

    /**
     * Creates a participant.
     */
    public void createParticipant() throws IOException {
        try {
            participantService.create(participant);
            if (role != null && (meeting.getVisible() || role.equals(Role.A) || role.equals(Role.D))) {
                final ParticipantStatus status = new ParticipantStatus();
                status.setMeeting(meeting);
                status.setParticipant(participant);
                status.setTutorial(tutorial);
                meetingService.addParticipantToMeeting(status);
                externalContext.redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
            } else {
                externalContext.redirect("participants.xhtml");
            }
        } catch (DuplicateEmailException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die E-Mail Adresse wird bereits verwendet");
            facesContext.addMessage(null, msg);
        } catch (MeetingNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Veranstaltung wurde nicht gefunden");
            facesContext.addMessage(null, msg);
        } catch (ParticipantAlreadyInMeetingException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Der Teilnehmer ist bereits in dieser Veranstaltung");
            facesContext.addMessage(null, msg);
        } catch (ParticipantNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Der Teilnehmer wurde nicht gefunden");
            facesContext.addMessage(null, msg);
        }
    }
}
