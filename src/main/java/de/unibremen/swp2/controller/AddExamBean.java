package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.MeetingNotFoundException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.ExamService;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.UserService;
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
import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

/**
 * @Author Theo
 * Allows to add a new exam.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.D, Role.A})
public class AddExamBean implements Serializable {

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Allows to edit,update,delete,creat and other operations on an Exam
     */
    @Inject
    private ExamService examService;

    /**
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Meeting
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a User
     */
    @Inject
    private UserService userService;

    /**
     * Global roles of a user
     */
    @Getter
    private Role role;

    /**
     * Exam to add.
     */
    @Getter
    private Exam exam;

    /**
     * Meeting where the exam takes place
     */
    @Getter
    private Meeting meeting;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

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
                        final UserMeetingRole userMeetingRole = userService.getUserMeetingRoleByUserAndMeeting(user,
                                meeting);
                        role = userMeetingRole.getRole();
                    } catch (NoResultException ignored) {
                    }
                }
                if (role != null && (role.equals(Role.A) || role.equals(Role.D))) {
                    exam = new Exam();
                    exam.setMeeting(meeting);
                }
            }
        }
    }


    /**
     * Creates an exam.
     */
    @MeetingRole(allowedRoles = {Role.D, Role.A})
    public void createExam() throws IOException {
        try {
            examService.create(exam);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (MeetingNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die Veranstaltung existiert " +
                    "nicht mehr");
            facesContext.addMessage(null, msg);
        }
    }

}
