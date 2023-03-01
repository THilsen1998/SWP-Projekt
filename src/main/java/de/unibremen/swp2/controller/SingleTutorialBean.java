package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.*;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.RequestParameterMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.ExcludeClassInterceptors;
import javax.persistence.NoResultException;

/**
 * @Author Tommy, Martin
 * Allows to load, delete, and update a tutorial.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class SingleTutorialBean implements Serializable {

    /**
     * Role of an user
     */
    @Getter
    private Role role;

    /**
     * Chosen tutorial.
     */
    @Getter
    private Tutorial tutorial;

    /**
     * Chosen meeting.
     */
    @Getter
    private Meeting meeting;

    /**
     * The groups in tutorial loaded from GroupDAO.
     */
    @Getter
    private List<TGroup> groups;

    /**
     * The participants in tutorial loaded from ParticipantDAO.
     */
    @Getter
    private List<Participant> participants;

    /**
     * The submissions in tutorial loaded from SubmissionDAO.
     */
    @Getter
    private List<Submission> submissions;

    /**
     * The tutors in tutorial loaded from UserDAO.
     */
    @Getter
    private List<User> tutors;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * Used to redirection
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentification (in case of success)
     */
    @Inject
    private FacesContext facesContext;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private TutorialService tutorialService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private GroupService groupService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private SubmissionService submissionService;

    /**
     * Initializes SingleTutorialBean
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
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByTutorialAndUser(tutorial, user);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException e) {
                        try {
                            final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                            role = userMeetingRole.getRole();
                            if (!role.equals(Role.T)) {
                                role = userMeetingRole.getRole();
                            }
                        } catch (NoResultException ignored) {
                        }
                    }
                }
                if (role != null && (meeting.getVisible() || role.equals(Role.D) || role.equals(Role.A))) {
                    participants = participantService.getAllParticipantsByTutorial(tutorial);
                    tutors = userService.getUsersByTutorial(tutorial);
                    groups = groupService.getGroupsByTutorial(tutorial);
                    submissions = submissionService.getSubmissionByMeeting(meeting);
                }
            }
        }
    }

    /**
     * Deletes the current tutorial.
     *
     */
    public void deleteTutorial() throws IOException {
        try {
            tutorialService.delete(tutorial);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Das Tutorium wurde nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Seite bitte neu laden.");
            facesContext.addMessage(null, msg);
        }
    }

}