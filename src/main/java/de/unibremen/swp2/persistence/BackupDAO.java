package de.unibremen.swp2.persistence;

import de.unibremen.swp2.persistence.Interceptors.*;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.DatabaseMetaData;

/**@Author Theo, Martin
 * restores an existing backup
 */
@RequestScoped
public class BackupDAO {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    /*
     * creates a backup
     */
    public void createBackup(){
        entityManager.createNativeQuery("SCRIPT DROP TO '~/IGradeBook/backup/backup.sql'").getResultList();
    }
    /*
     * restores an existing backup
     */
    public void insertBackup() {
        entityManager.createNativeQuery("RUNSCRIPT FROM '~/IGradeBook/backup/backup.sql'").executeUpdate();
    }

}
