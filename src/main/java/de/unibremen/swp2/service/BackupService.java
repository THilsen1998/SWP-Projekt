package de.unibremen.swp2.service;

import de.unibremen.swp2.persistence.BackupDAO;
import de.unibremen.swp2.persistence.Exceptions.PersistenceException;
import de.unibremen.swp2.persistence.Interceptors.*;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.zip.ZipFile;

/**
 * @Author Martin
 * Allows to backup the system.
 */
@RequestScoped
@Transactional(rollbackOn = PersistenceException.class)
public class BackupService
{

    @Inject
    private BackupDAO backupDAO;

    /**
     * creates backup.
     */
    @LockEvaluationDAO
    @LockMeetingDAO
    @LockExamDAO
    @LockParticipantDAO
    @LockTaskDAO
    @LockSubmissionDAO
    @LockTutorialDAO
    @LockUserDAO
    @LockFinalGradeDAO
    @LockGroupDAO
    public void createBackup() {
        backupDAO.createBackup();
    }

    /**
     * Inserts the given backup.
     *
     */
    @LockEvaluationDAO
    @LockMeetingDAO
    @LockExamDAO
    @LockParticipantDAO
    @LockTaskDAO
    @LockSubmissionDAO
    @LockTutorialDAO
    @LockUserDAO
    @LockFinalGradeDAO
    @LockGroupDAO
    public void insertBackup() {
        backupDAO.insertBackup();
    }

}
