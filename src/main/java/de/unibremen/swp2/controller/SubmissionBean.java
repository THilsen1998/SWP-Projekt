package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.*;
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
import java.util.List;
import java.util.Map;

/**
 * @Author Martin
 * Allows to klick on Participant or Group to evaluate.
 */

@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class SubmissionBean implements Serializable {


    /**
     * Chosen Submission.
     */
    @Getter
    private Submission submission;

    /**
     * The chosen meeting loaded from MeetingDAO.
     */
    @Getter
    private Meeting meeting;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Role of an user
     */
    @Getter
    private Role role;


    /**
     * List of participants in Meeting.
     */
    @Getter
    private List<Participant> participants;

    /**
     * List of participants in Meeting filtered.
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipants;


    /**
     * List of Groups.
     */
    @Getter
    private List<TGroup> groups;

    /**
     * Chosen user
     */
    private User user;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private SubmissionService submissionService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private GroupService groupService;

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
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * Initializes SubmissionBean
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("submission-Id");
        if (id != null) {
            submission = submissionService.getById(id);
            if (submission != null) {
                meeting = submission.getMeeting();
                user = userService.getUsersByEmail(principal.getName());
                if (user.getRole().equals(Role.A)) {
                    role = Role.A;
                } else {
                    try {
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user,
                                meeting);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {
                    }
                }
                if (role != null && !role.equals(Role.T)) {
                    if (submission.getGroupWork()) {
                        groups = groupService.getGroupsByMeeting(meeting);
                    } else {
                        participants = participantService.getAllParticipantsByMeeting(meeting);
                    }
                } else if (role != null) {
                    if (submission.getGroupWork()) {
                        groups = groupService.getGroupsByMeetingAndUser(meeting, user);
                    } else {
                        participants = participantService.getParticipantsByMeetingAndUser(meeting, user);
                    }
                }
            }
        }
    }

    /**
     * Delete a submission
     * @throws IOException
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void deleteSubmission() throws IOException {
        try {
            submissionService.delete(submission);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe wurde bereits gelöscht.");
            facesContext.addMessage(null, msg);
        } catch (HasEvaluationsException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Es existieren schon Bewertungen für diese Abgabe.");
            facesContext.addMessage(null, msg);
        }
    }
}
