package de.unibremen.swp2.persistence;


import de.unibremen.swp2.persistence.Exceptions.*;

/**
 * Allows to insert, update, delete, and load objects form Database.
 */
public interface DAO<T> {

    /**
     * Inserts the given Object.
     *
     * @param t
     *      Object to insert.
     * @throws NullPointerException
     *      If {@code object} is {@code null}.
     */
    public void insert(T t) throws NullPointerException, DuplicateEmailException, DuplicatedTutorialException, EntityNotFoundException, OutdatedException;

    /**
     * Updates the given Object.
     *
     * @param t
     *      Object to update.
     * @throws NullPointerException
     *      If {@code object} is {@code null}.
     */
    public void update(T t) throws NullPointerException, EntityNotFoundException, OutdatedException, DuplicateEmailException, TutorialNotFoundException;

    /**
     * Deletes the given object. Does not fail if {@code object} is {@code null}
     * or unknown.
     *
     * @param t
     *      Object to delete.
     */
    public void delete(T t) throws EntityNotFoundException, HasEvaluationsException;

    /**
     * Returns the object with given id.
     *
     * @param id
     *      Id of the object in question.
     * @return
     *      Object with given id.
     */
    public Object getById(String id) throws NullPointerException;
}
