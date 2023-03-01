package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.security.MeetingRole;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.SubmissionService;
import de.unibremen.swp2.service.TaskService;
import de.unibremen.swp2.service.UserService;
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
import java.util.*;

/**
 * @Author Martin
 * Allows to edit a submission .
 */

@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class EditSubmissionBean implements Serializable {

    /**
     * Submission to be edit.
     */
    @Getter
    private Submission submission;

    /**
     * Submission without tasks.
     */
    @Getter
    private Submission submissionWithoutTasks;

    /**
     * Root of the Tree.
     */
    @Getter
    private DefaultTreeNode root;

    /**
     * The Task selected.
     */
    @Getter
    @Setter
    private Task selectedTask;

    /**
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

    /**
     * A Task copied.
     */
    private Task copiedTask;

    /**
     * Global roles of a user
     */
    @Getter
    private Role role;

    /**
     * User currently locked in
     */
    @Inject
    private Principal principal;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Submission
     */
    @Inject
    private SubmissionService submissionService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a User
     */
    @Inject
    private UserService userService;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Task
     */
    @Inject
    private TaskService taskService;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;

    /**
     * Meeting of the Submission
     */
    private Meeting meeting;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Meeting
     */
    @Inject
    private MeetingService meetingService;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("submission-Id");
        if (id != null) {
            submissionWithoutTasks = submissionService.getById(id);
            submission = submissionService.getByIdWithTasks(id);
            if (submission != null && submissionWithoutTasks != null) {
                try {
                    meeting = meetingService.getMeetingBySubmission(submission);
                    final User user = userService.getUsersByEmail(principal.getName());
                    if (user.getRole().equals(Role.A)) {
                        role = Role.A;
                    } else {
                        try {
                            final UserMeetingRole userMeetingRole =
                                    userService.getUserMeetingRoleByUserAndMeeting(user, meeting);
                            role = userMeetingRole.getRole();
                        } catch (NoResultException ignored) {
                        }
                    }
                } catch (NoResultException ignored) {
                }
                if (role != null && !role.equals(Role.T)) {
                    Set<Task> tasks = submission.getTasks();
                    root = new DefaultTreeNode(new Task(), null);
                    List<Task> taskList = new ArrayList<>(tasks);
                    Collections.sort(taskList);
                    sortTaskByNumber(taskList, root);
                }
            }
        }
    }


    /**
     * sort tasks by number.
     *
     * @param taskList tasklist, node node
     */
    private void sortTaskByNumber(List<Task> taskList, DefaultTreeNode node) {

        for (Task t : taskList) {
            DefaultTreeNode node1 = new DefaultTreeNode(t, node);

            if (t.getTasks().size() != 0) {
                List<Task> listWTasks = new ArrayList<>(t.getTasks());

                Collections.sort(listWTasks);

                sortTaskByNumber(listWTasks, node1);

            }

        }

    }

    /**
     * copies a Task.
     *
     * @param task task to be copied
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void copyTask(Task task) {
        selectedTask = task;
        copiedTask = new Task(task);
    }

    /**
     * restores the old copy.
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void restoreWithCopy() {
        DefaultTreeNode node = (DefaultTreeNode) searchTask(root, selectedTask);
        Task task = (Task) node.getData();
        node.setData(copiedTask);
        if (task.getSubmission() != null) {
            submission.getTasks().remove(selectedTask);
            submission.getTasks().add(copiedTask);
            for (Task t : task.getTasks()) {
                t.setTask(copiedTask);
            }
        } else {
            task.getTask().getTasks().remove(selectedTask);
            task.getTask().getTasks().add(copiedTask);
            for (Task t : task.getTasks()) {
                t.setTask(copiedTask);
            }
        }
    }

    /**
     * restores the failed edit.
     */
    private void failedEdit() {
        restoreWithCopy();
        TreeNode node = searchTask(root, selectedTask);
        selectedTask = (Task) node.getData();
        copiedTask = new Task(selectedTask);
    }

    /**
     * Updates the task of the added submission.
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void dialogEdit() {
        if (selectedTask.getPoints().compareTo(BigDecimal.ZERO) < 0) {
            failedEdit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Punkte dürfen nicht kleiner 0 "
                    + "sein.");
            facesContext.addMessage(null, msg);
        } else if (selectedTask.getWeighting().compareTo(BigDecimal.ZERO) < 0) {
            failedEdit();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Gewichtung darf nicht kleiner "
                    + "0 sein.");
            facesContext.addMessage(null, msg);
        } else {
            try {
                submissionService.update(submissionWithoutTasks);
                if (selectedTask.getTask() == null) {
                    taskService.update(selectedTask);
                }
                Task task = selectedTask.getTask();
                calculatePoints(task);
                PrimeFaces.current().executeScript("PF('task').hide()");
            } catch (OutdatedException e) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte " +
                        "Seite neu laden.");
                facesContext.addMessage(null, msg);
            } catch (EntityNotFoundException e) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
                facesContext.addMessage(null, msg);
            }
        }
    }

    /**
     * calculates the points.
     *
     * @param task task
     */
    private void calculatePoints(Task task) {
        if (task != null) {
            task.setPoints(new BigDecimal("0"));
            for (Task t : task.getTasks()) {
                task.setPoints(task.getPoints().add(t.getPoints().multiply(t.getWeighting())));
            }
            if (task.getSubmission() != null) {
                try {
                    taskService.update(task);
                } catch (EntityNotFoundException e) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
                    facesContext.addMessage(null, msg);
                }
            }
            calculatePoints(task.getTask());
        } else {
            submissionWithoutTasks.setMaxGrade(new BigDecimal("0"));
            for (Task t : submission.getTasks()) {
                submissionWithoutTasks.setMaxGrade(submissionWithoutTasks.getMaxGrade().add(t.getPoints().multiply(t.getWeighting())));
            }
            try {
                submissionService.update(submissionWithoutTasks);
            } catch (OutdatedException e) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte " +
                        "Seite neu laden.");
                facesContext.addMessage(null, msg);
            } catch (EntityNotFoundException e) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
                facesContext.addMessage(null, msg);
            }
        }
    }

    /**
     * Adds a task.
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void addTask() {
        try {
            submissionService.update(submissionWithoutTasks);
            Task task = new Task();
            task.setSubmission(submission);
            final int i = submission.getTasks().size();
            task.setNumber(1.0 + i);
            task.setName("Aufgabe " + task.getNumber());
            submission.getTasks().add(task);
            taskService.create(task);
            new DefaultTreeNode(task, root);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte Seite " +
                    "neu laden.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Deletes a task.
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void deleteTask(Task task) {
        try {
            if (taskService.countEvaluationsByTask(task) > 0) {
                throw new HasEvaluationsException();
            }
            submissionService.update(submissionWithoutTasks);
            TreeNode node = searchTask(root, task);
            List<TreeNode> renumberList = new ArrayList<>();
            if (node.getParent() == root) {
                for (TreeNode n : node.getParent().getChildren()) {
                    Task child = (Task) n.getData();
                    if (child.getNumber() > task.getNumber()) {
                        child.setNumber(child.getNumber() - 1);
                        taskService.update(child);
                        renumberList.add(n);
                    }
                }
            } else {
                for (TreeNode n : node.getParent().getChildren()) {
                    Task child = (Task) n.getData();
                    if (child.getNumber() > task.getNumber()) {
                        String s = String.valueOf(child.getNumber());
                        char c = s.charAt(s.length() - 1);
                        int i = Character.getNumericValue(c);
                        i -= 1;
                        s = s.substring(0, s.length() - 1) + i;
                        child.setNumber(Double.parseDouble(s));
                        taskService.update(task);
                        renumberList.add(n);
                    }
                }
            }
            if (task.getSubmission() != null) {
                submission.getTasks().remove(task);
                taskService.delete(task);
            } else {
                task.getTask().getTasks().remove(task);
                taskService.delete(task);
                TreeNode parent = node.getParent();
                while (parent != root) {
                    Task tParent = (Task) parent.getData();
                    tParent = taskService.getById(tParent.getId());
                    tParent.setPoints(tParent.getPoints().subtract(task.getPoints()));
                    taskService.update(tParent);
                    parent = parent.getParent();
                }
            }
            TreeNode parentNode = node.getParent();
            node.getParent().getChildren().remove(node);
            for (TreeNode n : renumberList) {
                reNumber(n);
                Task task1 = (Task) n.getData();
                taskService.update(task1);
            }
            if (node.getParent() != root) {
                Task nodeParent = (Task) parentNode.getData();
                nodeParent = taskService.getById(nodeParent.getId());
                calculatePoints(nodeParent);
            } else {
                calculatePoints(null);
            }
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (HasEvaluationsException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die Aufgabe enthält schon " +
                    "Bewertungen.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte Seite " +
                    "neu laden.");
            facesContext.addMessage(null, msg);
        }
    }


    /**
     * Adds an subTask.
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void addSubTask(Task task) {
        try {
            submissionService.update(submissionWithoutTasks);
            TreeNode node = searchTask(root, task);
            Task child = new Task();
            Task parent = (Task) node.getData();
            final Double num = parent.getNumber();
            final int size = parent.getTasks().size();
            child.setNumber(appendDouble(num, size));
            child.setName("Aufgabe " + child.getNumber());
            parent.getTasks().add(child);
            child.setTask(parent);
            if (parent.getTasks().size() == 1) {
                calculatePoints(parent);
            } else {
                taskService.create(child);
            }
            new DefaultTreeNode(child, node);
            node.setExpanded(true);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte Seite " +
                    "neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
            facesContext.addMessage(null, msg);
        }
    }

    private double appendDouble(double num, int size) {
        String s = String.valueOf(num);
        if (s.charAt(s.length() - 1) == '0') {
            s = s.substring(0, s.length() - 1);
        }
        s += size + 1;
        return Double.parseDouble(s);
    }

    /**
     * sort the tasks.
     *
     * @param task task
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void sortUp(Task task) {
        try {
            submissionService.update(submissionWithoutTasks);
            DefaultTreeNode node = (DefaultTreeNode) searchTask(root, task);
            if (node.getParent() == root && task.getNumber() != 1.0) {
                Task other;
                for (TreeNode n : root.getChildren()) {
                    DefaultTreeNode dn = (DefaultTreeNode) n;
                    other = (Task) dn.getData();
                    if (other.getNumber() + 1 == task.getNumber()) {
                        other.setNumber(other.getNumber() + 1);
                        task.setNumber(task.getNumber() - 1);
                        node.setData(other);
                        dn.setData(task);
                        rearrangeSubTasks(node, dn);
                        reNumber(node);
                        reNumber(dn);
                        Task first = (Task) node.getData();
                        Task second = (Task) dn.getData();
                        taskService.update(first);
                        taskService.update(second);
                        break;
                    }
                }
            } else if (node.getParent() != root) {
                String s = String.valueOf(task.getNumber());
                char c = s.charAt(s.length() - 1);
                if (c != '1') {
                    s = s.substring(0, s.length() - 1);
                    int i = Character.getNumericValue(c);
                    i -= 1;
                    s += i;
                    double num = Double.parseDouble(s);
                    Task other;
                    for (TreeNode n : node.getParent().getChildren()) {
                        DefaultTreeNode dn = (DefaultTreeNode) n;
                        other = (Task) dn.getData();
                        if (other.getNumber() == num) {
                            other.setNumber(task.getNumber());
                            task.setNumber(num);
                            node.setData(other);
                            dn.setData(task);
                            rearrangeSubTasks(node, dn);
                            reNumber(node);
                            reNumber(dn);
                            Task first = (Task) node.getData();
                            Task second = (Task) dn.getData();
                            taskService.update(first);
                            taskService.update(second);
                            break;
                        }
                    }
                }
            }
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte Seite " +
                    "neu laden.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * sort tasks.
     *
     * @param task task
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void sortDown(Task task) {
        try {
            submissionService.update(submissionWithoutTasks);
            DefaultTreeNode node = (DefaultTreeNode) searchTask(root, task);
            if (node.getParent() == root && task.getNumber() != root.getChildren().size()) {
                Task other;
                for (TreeNode n : root.getChildren()) {
                    DefaultTreeNode dn = (DefaultTreeNode) n;
                    other = (Task) dn.getData();
                    if (other.getNumber() - 1 == task.getNumber()) {
                        other.setNumber(other.getNumber() - 1);
                        task.setNumber(task.getNumber() + 1);
                        node.setData(other);
                        dn.setData(task);
                        rearrangeSubTasks(node, dn);
                        reNumber(node);
                        reNumber(dn);
                        Task first = (Task) node.getData();
                        Task second = (Task) dn.getData();
                        taskService.update(first);
                        taskService.update(second);
                        break;
                    }
                }
            } else if (node.getParent() != root) {
                String s = String.valueOf(task.getNumber());
                char c = s.charAt(s.length() - 1);
                if (c != node.getChildCount()) {
                    s = s.substring(0, s.length() - 1);
                    int i = Character.getNumericValue(c);
                    i += 1;
                    s += i;
                    double num = Double.parseDouble(s);
                    Task other;
                    for (TreeNode n : node.getParent().getChildren()) {
                        DefaultTreeNode dn = (DefaultTreeNode) n;
                        other = (Task) dn.getData();
                        if (other.getNumber() == num) {
                            other.setNumber(task.getNumber());
                            task.setNumber(num);
                            node.setData(other);
                            dn.setData(task);
                            rearrangeSubTasks(node, dn);
                            reNumber(node);
                            reNumber(dn);
                            Task first = (Task) node.getData();
                            Task second = (Task) dn.getData();
                            taskService.update(first);
                            taskService.update(second);
                            break;
                        }
                    }
                }
            }
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte Seite " +
                    "neu laden.");
            facesContext.addMessage(null, msg);
        }
    }

    private void rearrangeSubTasks(DefaultTreeNode node1, DefaultTreeNode node2) {
        List<TreeNode> list = new ArrayList<>(node1.getChildren());
        List<TreeNode> list2 = new ArrayList<>(node2.getChildren());
        node1.getChildren().clear();
        node2.getChildren().clear();
        for (TreeNode n : list) {
            DefaultTreeNode newNode = new DefaultTreeNode(n.getData(), node2);
            if (n.getChildCount() != 0) {
                rearrangeSubTasks((DefaultTreeNode) n, newNode);
            }
        }
        for (TreeNode n : list2) {
            DefaultTreeNode newNode = new DefaultTreeNode(n.getData(), node1);
            if (n.getChildCount() != 0) {
                rearrangeSubTasks((DefaultTreeNode) n, newNode);
            }
        }
    }

    private void reNumber(TreeNode node1) {
        if (node1.getChildCount() != 0) {
            Task parent = (Task) node1.getData();
            for (TreeNode n : node1.getChildren()) {
                Task child = (Task) n.getData();
                String pString = String.valueOf(parent.getNumber());
                if (pString.charAt(pString.length() - 1) == '0') {
                    char parentNumber = String.valueOf(parent.getNumber()).charAt(0);
                    String s = String.valueOf(child.getNumber()).substring(1);
                    s = parentNumber + s;
                    child.setNumber(Double.parseDouble(s));
                } else {
                    String s = String.valueOf(child.getNumber());
                    char c = s.charAt(s.length() - 1);
                    pString += c;
                    child.setNumber(Double.parseDouble(pString));
                }
                reNumber(n);
            }
        }
    }

    private TreeNode searchTask(TreeNode current, Task task) {
        if (current.getData().equals(task)) {
            return current;
        }
        for (TreeNode node : current.getChildren()) {
            TreeNode result = searchTask(node, task);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public void onNodeCollapse(NodeCollapseEvent event) {
        if (event != null && event.getTreeNode() != null) {
            event.getTreeNode().setExpanded(false);
        }
    }

    /**
     * updates the submission.
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void update() throws IOException {
        try {
            submissionService.update(submissionWithoutTasks);
            externalContext.redirect("submission.xhtml?submission-Id=" + submission.getId());
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Bitte Seite " +
                    "neu laden.");
            facesContext.addMessage(null, msg);
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe nicht gefunden.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * deletes the submission.
     *
     */
    @MeetingRole(allowedRoles = {Role.CEO, Role.D, Role.A})
    public void deleteSubmission() throws IOException {
        try {
            submissionService.delete(submission);
            externalContext.redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        } catch (EntityNotFoundException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Abgabe wurde bereits gelöscht.");
            facesContext.addMessage(null, msg);
        } catch (HasEvaluationsException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Es existieren schon " +
                    "Bewertungen für diese Abgabe.");
            facesContext.addMessage(null, msg);
        }
    }

}
