package de.unibremen.swp2.service;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.PersistenceException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Interceptors.LockEvaluationDAO;
import de.unibremen.swp2.persistence.Interceptors.LockMeetingDAO;
import de.unibremen.swp2.persistence.Interceptors.LockSubmissionDAO;
import de.unibremen.swp2.persistence.Interceptors.LockTaskDAO;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @Author Martin
 *Allows to edit,update,delete,creat and to perform other operations
 *  on a Task (stored in {@link #taskDAO}).
 */
@RequestScoped
@Transactional(rollbackOn = PersistenceException.class)
public class TaskService
{

    @Inject
    private TaskDAO taskDAO;

    /**
     * creates the given task.
     *
     * @param task
     *  the Tasks to create
     */
    @LockSubmissionDAO
    @LockTaskDAO
    @LockMeetingDAO
    public void create(final Task task) throws EntityNotFoundException {
        taskDAO.insert(task);
    }

    /**
     * Updates the given task.
     *
     * @param task
     *      Task to update.
     * @throws NullPointerException
     *      If {@code task} is {@code null}.
     */
    @LockMeetingDAO
    @LockTaskDAO
    @LockSubmissionDAO
    public void update(final Task task) throws NullPointerException, EntityNotFoundException {
        taskDAO.update(task);
    }

    /**
     * Deletes the given task. Does not fail if {@code task} is {@code null}
     * or unknown.
     *
     * @param task
     *      Task to delete.
     */
    @LockMeetingDAO
    @LockTaskDAO
    @LockSubmissionDAO
    @LockEvaluationDAO
    public void delete(final Task task) throws HasEvaluationsException, EntityNotFoundException {
        taskDAO.delete(task);
    }

    /**
     * Returns the task with given id.
     *
     * @param id
     *      Id of the task in question.
     * @return
     *      Task with given id.
     */
    public Task getById(final String id){
        return taskDAO.getById(id);
    }

    /**
     * Counts the number of evaluations to a task.
     * @param task the task
     * @return the count.
     */
    public Long countEvaluationsByTask(final Task task) {
        return taskDAO.countEvaluationsByTask(task);
    }

}