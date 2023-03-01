package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.InvalidGradeException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.*;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

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
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * @Author Martin
 * Allows the user to evaluate a participant.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class ParticipantEvaluationBean implements Serializable {

    /**
     * The chosen submission loaded from SubmissionDAO.
     */
    @Getter
    private Submission submission;

    /**
     * The tasks in chosen submission loaded from TaskDAO.
     */
    private List<Task> tasks;

    /**
     * The participant in chosen submission to evaluate loaded from ParticipantDAO.
     */
    private Participant participant;

    /**
     * The evaluation to the chosen submission and for the participant loaded from EvaluationDAO.
     */
    @Getter
    private Evaluation evaluation;

    /**
     * User currently logged in
     */
    @Inject
    private Principal principal;

    /**
     * Copied Evaluation
     */

    private Evaluation copiedEvaluation;

    /**
     * Select Evaluation
     */
    @Getter
    @Setter
    private Evaluation selectedEvaluation;

    /**
     * Role of an User
     */
    @Getter
    private Role role;

    /**
     * status of an participant
     */
    private ParticipantStatus participantStatus;

    /**
     * Beginning of a tree
     */
    @Getter
    private DefaultTreeNode root;

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
     * Allows to create, update and other operations
     */
    @Inject
    private SubmissionService submissionService;

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
    private UserService userService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private EvaluationService evaluationService;

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private FinalGradeService finalGradeService;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentification (in case of success)
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Initialzes ParticipantEvaluationBean
     */
    @PostConstruct
    private void init() {
        final String subId = parameterMap.get("submission-Id");
        final String parId = parameterMap.get("participant-Id");
        if (subId != null && parId != null) {
            submission = submissionService.getById(subId);
            if (submission != null && !submission.getGroupWork()) {
                participant = participantService.getById(parId);
                if (participant != null) {
                    User user = null;
                    Meeting meeting = null;
                    try {
                        meeting = meetingService.getMeetingBySubmission(submission);
                        participantStatus =
                                participantService.getParticipantStatusByParticipantAndMeeting(participant, meeting);
                        user = userService.getUsersByEmail(principal.getName());
                        if (user.getRole().equals(Role.A)) {
                            role = Role.A;
                        } else {
                            final UserMeetingRole userMeetingRole =
                                    userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                            role = userMeetingRole.getRole();
                        }
                    } catch (NoResultException ignored) {
                    }
                    if (role != null && (role.equals(Role.D) || role.equals(Role.A) || (meeting.getVisible() && participantService.checkIfParticipantInUserTutorial(user, participant, meeting)))) {
                        evaluation = evaluationService.getEvaluationBySubmissionsAndParticipant(submission,
                                participantStatus);
                        try {
                            List<EvaluationToTask> evaluationToTasks =
                                    evaluationService.getEvaluationToTaskBySubmissionAndParticipantStatus(submission,
                                            participantStatus);
                            root = new DefaultTreeNode(new EvaluationToTask(), null);
                            createTree(evaluationToTasks, root);
                        } catch (EntityNotFoundException ignored) {
                        }
                    }
                }
            }
        }
    }

    /**
     * create a tree
     * @param list
     * @param node
     */
    private void createTree(List<EvaluationToTask> list, DefaultTreeNode node) {
        for (EvaluationToTask e : list) {
            DefaultTreeNode treeNode = new DefaultTreeNode(e, node);

            if (e.getEvaluationToTasks().size() != 0) {
                createTree(e.getEvaluationToTasks(), treeNode);
            }
        }
    }

    /**
     * CopyEvaluation
     * @param evaluationToTask
     */
    public void copyEvaluation(EvaluationToTask evaluationToTask) {
        selectedEvaluation = evaluationToTask.getEvaluation();
        copiedEvaluation = new Evaluation(evaluationToTask.getEvaluation());
    }

    /**
     * Restore
     */
    public void restoreWithCopy() {

        DefaultTreeNode node = (DefaultTreeNode) searchNode(root, selectedEvaluation);
        EvaluationToTask evaluationToTask = (EvaluationToTask) node.getData();
        evaluationToTask.setEvaluation(new Evaluation(copiedEvaluation));
        node.setData(evaluationToTask);
    }

    /**
     * Fail to edit
     */

    private void failedEdit() {
        restoreWithCopy();
        TreeNode node = searchNode(root, selectedEvaluation);
        selectedEvaluation = ((EvaluationToTask) node.getData()).getEvaluation();
        copiedEvaluation = new Evaluation(selectedEvaluation);
    }

    /**
     * Dialog of Edit
     */
    public void dialogEdit() {
        if (selectedEvaluation.getPoints().compareTo(BigDecimal.ZERO) < 0) {
            failedEdit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Punkte dürfen nicht kleiner 0 "
                    + "sein.");
            facesContext.addMessage(null, msg);
        } else if (selectedEvaluation.getPoints().compareTo(selectedEvaluation.getTask().getPoints()) > 0) {
            failedEdit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Punkte dürfen die " +
                    "Maximalpunktzahl nicht überschreiten.");
            facesContext.addMessage(null, msg);
        } else {
            TreeNode node = searchNode(root, selectedEvaluation);
            calculatePoints(node.getParent());
            PrimeFaces.current().executeScript("PF('task').hide()");
        }
    }

    /**
     * Calculated points
     * @param node
     */
    private void calculatePoints(TreeNode node) {
        if (node != root) {
            EvaluationToTask eval = (EvaluationToTask) node.getData();
            eval.getEvaluation().setPoints(new BigDecimal("0"));
            for (EvaluationToTask e : eval.getEvaluationToTasks()) {
                Evaluation ev = e.getEvaluation();
                eval.getEvaluation().setPoints(eval.getEvaluation().getPoints().add(ev.getPoints().multiply(ev.getTask().getWeighting())));
            }
            calculatePoints(node.getParent());
        } else {
            evaluation.setPoints(new BigDecimal("0"));
            List<TreeNode> children = root.getChildren();
            for (TreeNode n : children) {
                Evaluation ev = ((EvaluationToTask) n.getData()).getEvaluation();
                evaluation.setPoints(evaluation.getPoints().add(ev.getPoints().multiply(ev.getTask().getWeighting())));
            }
        }
    }

    /**
     * Node collapse
     * @param event
     */
    public void onNodeCollapse(NodeCollapseEvent event) {
        if (event != null && event.getTreeNode() != null) {
            event.getTreeNode().setExpanded(false);
        }
    }

    /**
     * Search a node of a tree
     * @param current
     * @param evaluation
     * @return
     */
    private TreeNode searchNode(TreeNode current, Evaluation evaluation) {
        if (((EvaluationToTask) current.getData()).getEvaluation() != null && ((EvaluationToTask) current.getData()).getEvaluation().equals(evaluation)) {
            return current;
        }
        for (TreeNode node : current.getChildren()) {
            TreeNode result = searchNode(node, evaluation);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * calculate a grade
     */
    public void calculateGrade() {
        try {
            evaluation.setGrade(finalGradeService.calculateSingleSubmissionGrade(evaluation.getPoints(), submission.getMaxGrade()));
        } catch (InvalidGradeException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Nicht durch 0 teilbar.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * create a evaluation
     * @throws IOException
     */
    public void createEvaluations() throws IOException {
        try {
            evaluationService.createParticipantEvaluation(evaluation, root);
            externalContext.redirect("submission.xhtml?submission-Id=" + evaluation.getSubmission().getId());
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Daten veraltet. Seite bitte " + "neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Abgabe nicht gefunden oder " + "Teilnehmer nicht in der Veranstaltuing.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Delete subevaluation
     * @param evaluationToTask
     */
    public void deleteSubEvaluation(EvaluationToTask evaluationToTask) {
        try {
            evaluationService.delete(evaluationToTask.getEvaluation());
            Evaluation evaluation = new Evaluation();
            evaluation.setParticipantStatus(participantStatus);
            evaluation.setTask(evaluationToTask.getTask());
            evaluationToTask.setEvaluation(evaluation);
            calculatePoints(root);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "Bewertung aus der Datenbank gelöscht.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Bewertung nicht gefunden.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Delete all Evaluations
     * @throws IOException
     */
    public void deleteAllEvaluations() throws IOException {
        try {
            evaluationService.deleteAllEvaluationsOfParticipantOfSubmission(submission, participantStatus);
            externalContext.redirect("submission.xhtml?submission-Id=" + submission.getId());
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Seite bitte neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
            facesContext.addMessage(null, msg);
        }
    }
}
