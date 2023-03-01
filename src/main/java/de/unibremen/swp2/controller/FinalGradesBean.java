package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.*;
import lombok.Getter;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.RequestParameterMap;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author Martin
 * Allows the user to give an finalgrade.
 */
@Named
@FacesConfig
@ViewScoped
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class FinalGradesBean implements Serializable {

    /**
     * Role of an User
     */
    @Getter
    private Role role;

    /**
     * The chosen Meeting.
     */
    @Getter
    private Meeting meeting;

    /**
     * The chosen exam.
     */
    private Exam exam;

    /**
     * List of Bool
     */
    @Getter
    private List<Boolean> boolList;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * The chosen participant loaded from ParticipantDAO
     */
    @Getter
    private List<Participant> participants;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Allows to addCEOToMeeting, create, update, delete and other operations of an Meeting
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Allows to  update, reset a password, create and other operations
     */
    @Inject
    private UserService userService;

    /**
     * Allows to create, update and other operations of an Participant
     */
    @Inject
    private ParticipantService participantService;

    /**
     * Allows to create, update and other operations of an finalgrade
     */
    @Inject
    private FinalGradeService finalGradeService;

    /**
     * Allows to createParticipantEvaluation, createGroupEvaluation, createEvaluationHelperForGroup, update, delete and other operations of an Evaluation
     */
    @Inject
    private EvaluationService evaluationService;

    /**
     * Allows to create, update, delete and other operations of an Exam
     */
    @Inject
    private ExamService examService;

    /**
     * Initializes the FinalGradeBean
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("meeting-Id");
        if (id != null) {
            meeting = meetingService.getById(id);
            try {
                exam = examService.getExamByMeeting(meeting);
            } catch (NoResultException ignored) {}
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
                if (role != null) {
                    boolList = Arrays.asList(true, true, true, false, true, true, true);
                    if (!role.equals(Role.T)) {
                        participants = participantService.getAllParticipantsByMeeting(meeting);
                    } else if (meeting.getVisible()) {
                        participants = participantService.getParticipantsByMeetingAndUser(meeting, user);
                    }
                }
            }
        }
    }

    /**
     *Get the finalgrade of an participant
     */
    public FinalGrade getFinalGrade(Participant participant) {
        return finalGradeService.getFinalGrade(participant, meeting);
    }

    /**
     *Get the Evaluation of an participant
     */
    public BigDecimal getEvaluation(Participant participant) {
        if (exam != null) {
            try {
                return evaluationService.getExamEvaluation(exam, participant).getGrade();
            } catch (NoResultException e) {
                return BigDecimal.ZERO;
            }
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Toggle visibility
     * @param event
     */
    public void onToggle(ToggleEvent event) {
        boolList.set((Integer) event.getData(), event.getVisibility() == Visibility.VISIBLE);
    }

}
