package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.BackupService;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @Author Martin
 * Allows configure the application.
 */
@Named
@FacesConfig
@ViewScoped
@GlobalSecure(roles = {Role.A})
public class ConfigBean implements Serializable {

    /**
     * File used for Backup.
     */
    private static final Path file = Paths.get(System.getProperty("user.home"))
            .resolve("IGradeBook")
            .resolve("backup")
            .resolve("backup.sql");

    /**
     * attriubutes of the file.
     */
    @Getter
    private BasicFileAttributes attributes;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a BackUp
     */
    @Inject
    private BackupService backupService;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        try {
            attributes = Files.readAttributes(file, BasicFileAttributes.class);
        } catch (IOException ignored) {
        }
    }

    /**
     * Creates a backUp.
     */
    public void createBackUp() {
        backupService.createBackup();
        init();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Backup erfolgreich erstellt.");
        facesContext.addMessage(null, msg);
    }

    /**
     * Inserts a backUp.
     */
    public void insertBackUp() {
        if (Files.exists(file)) {
            backupService.insertBackup();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Erfolg", "Backup erfolgreich eingespielt.");
            facesContext.addMessage(null, msg);
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Es wurde noch kein Backup erstellt.");
            facesContext.addMessage(null, msg);
        }
    }

}
