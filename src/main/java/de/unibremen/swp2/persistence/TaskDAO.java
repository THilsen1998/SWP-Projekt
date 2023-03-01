package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.Submission;
import de.unibremen.swp2.model.Task;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
/*
@Author Martin
 * Process date from and into the data base
 */

@RequestScoped
public class TaskDAO implements DAO<Task> {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private static final ReentrantLock lock = new ReentrantLock();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    /**
     * Inserts the given task.
     *
     * @param task Task to insert.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    public void insert(final Task task) throws NullPointerException, EntityNotFoundException {
        if (task.getSubmission() != null) {
            final Submission submission = entityManager.find(Submission.class, task.getSubmission().getId(),
                    LockModeType.PESSIMISTIC_WRITE);
            if (submission != null) {
                entityManager.persist(task);
            } else {
                throw new EntityNotFoundException();
            }
        } else {
            final Task parent = entityManager.find(Task.class, task.getTask().getId(), LockModeType.PESSIMISTIC_WRITE);
            if (parent != null) {
                entityManager.persist(task);
            } else {
                throw new EntityNotFoundException();
            }
        }
    }

    /**
     * Updates the given task.
     *
     * @param task Task to update.
     * @throws NullPointerException If {@code task} is {@code null}.
     */
    public void update(final Task task) throws NullPointerException, EntityNotFoundException {
        final Task currentTask = entityManager.find(Task.class, task.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (currentTask == null) {
            throw new EntityNotFoundException();
        } else {
            entityManager.merge(task);
        }
    }

    /**
     * Deletes the given task. Does not fail if {@code task} is {@code null}
     * or unknown.
     *
     * @param task Task to delete.
     */
    public void delete(final Task task) throws EntityNotFoundException, HasEvaluationsException {
        Query q = entityManager.createQuery("SELECT COUNT(e) FROM Evaluation e WHERE e.task = :task").setParameter(
                "task", task);
        final Long i = (Long) q.getResultList().get(0);
        if (i == 0) {
            final Task currentTask = entityManager.find(Task.class, task.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (currentTask != null) {
                if (currentTask.getTask() != null) {
                    currentTask.getTask().getTasks().remove(currentTask);
                    currentTask.setTask(null);
                }
                entityManager.remove(currentTask);
            } else {
                throw new EntityNotFoundException();
            }
        } else {
            throw new HasEvaluationsException();
        }
    }

    /**
     * Returns the task with given id.
     *
     * @param id Id of the task in question.
     * @return Task with given id.
     */
    public Task getById(final String id) {
        return entityManager.find(Task.class, id);
    }

    /**
     * Returns the tasks with given submission.
     *
     * @param submission Submission of the tasks in question.
     * @return Tasks with given submission.
     */
    public List<Task> getTasksBySubmission(final Submission submission) {
        return null;
    }

    /**
     * calculates the Evaluations by giving task.
     *
     * @param task the given task .
     * @return long number.
     */
    public Long countEvaluationsByTask(final Task task) {
        Query q = entityManager.createQuery("SELECT COUNT(e) FROM Evaluation e WHERE e.task = :task").setParameter("task", task);
        return (Long) q.getResultList().get(0);
    }

}
