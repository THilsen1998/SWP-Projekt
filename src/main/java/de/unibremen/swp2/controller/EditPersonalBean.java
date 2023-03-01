package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.UserService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
import java.security.Principal;

/**
 * @Author Khaled
 * Allows to edit a user.
 */
@Data
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class EditPersonalBean implements Serializable {

    /**
     * User currently locked in
     */
    @Inject
    Principal principal;

    /**
     * User to edit.
     */
    @Getter
    private User user;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentication (in case of success).
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a User
     */
    @Inject
    private UserService userService;

    /**
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * resetedPassword.
     */
    @Getter
    private String resetedPassword = "";

    /**
     * Old password.
     */
    @Getter
    @Setter
    private String oldPassword = "";

    /**
     * New Password.
     */
    @Getter
    @Setter
    private String newPassWord1 = "";

    /**
     * New Password .
     */
    @Setter
    @Getter
    private String newPassWord2 = "";

    /**
     * If the password can be disabled.
     */
    @Setter
    @Getter
    private Boolean disableSetNewPasswordButton = false;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        this.user = userService.getUsersByEmail(principal.getName());
    }

    /**
     * Updates {@link #user}. On success, a redirect to 'users.xhtml' is
     * registered.
     */
    public void update() throws IOException {
        try {
            userService.update(user);
            FacesMessage msg = new FacesMessage("Das neue Password wird gespeichert");
            facesContext.addMessage(null, msg);
        } catch (UserNotFoundException e) {
            FacesMessage msg = new FacesMessage("Der User ist nicht mehr im System.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage("Der User ist veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        } catch (UserNotPermittedException | DuplicateEmailException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the password.
     */
    public void resetPassword(User user) throws IOException {
        try {
            userService.resetPw(user);
            externalContext.redirect("login.xhtml");
        } catch (UserNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Nutzer nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Seite bitte neu laden.");
            facesContext.addMessage(null, msg);
        } catch (DuplicateEmailException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die Email existiert bereits.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Resets the password.
     */
    public void restPw() throws IOException {
        this.user = userService.getUsersByEmail(principal.getName());
        resetPassword(user);

    }

    /**
     * Sets the new password.
     */
    public void setNewPassword() throws IOException {
        try {
            userService.changePassWord(user, oldPassword, newPassWord1, newPassWord2);
            externalContext.redirect("login.xhtml");
        } catch (DoesntMatchOldPsWrdException e) {

            FacesMessage msg = new FacesMessage("Das aktuelle password ist nicht richtig,Bitte erneut versuchen ");
            facesContext.addMessage(null, msg);
        } catch (TwoPaswordsArentIdentical e) {
            FacesMessage msg = new FacesMessage("Die Passwörter sind nicht identisch,Bitte erneut versuchen");
            facesContext.addMessage(null, msg);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (DuplicateEmailException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die Email existiert bereits.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Seite bitte neu laden.");
            facesContext.addMessage(null, msg);
        }
    }

}
