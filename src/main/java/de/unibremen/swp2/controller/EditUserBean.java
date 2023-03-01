package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.UserNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.UserNotPermittedException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.RequestParameterMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * @Author Dennis, Theo
 * Allows to edit a user.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.A})
public class EditUserBean implements Serializable {

    /**
     * User to edit
     */
    @Getter
    private User user;

    /**
     * Used to add faces messages (in case of errors) or to complete
     * authentification (in case of success)
     */
    @Inject
    private FacesContext facesContext;


    /**
     * Allows to  update and reset a password of an User
     */
    @Inject
    private UserService userService;

    /**
     * Used to redirection
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String, String> parameterMap;


    /**
     * Initializes this EditUserBean
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("user-Id");
        if (id == null) {
            user = null;
        } else {
            user = userService.getById(id);
            System.out.println(user.getId());
        }
    }


    /**
     * Updates {@link #user}. On success, a redirect to 'users.xhtml' is
     * registered.
     */
    public void update() throws IOException {
        try {
            userService.update(user);
            externalContext.redirect("users.xhtml");
        } catch (UserNotFoundException e) {
            FacesMessage msg = new FacesMessage("Der User ist nicht mehr im System.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage("Der User ist veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        } catch (DuplicateEmailException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die E-Mail Adresse wird " +
                    "bereits verwendet");
            facesContext.addMessage(null, msg);
        } catch (UserNotPermittedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Nicht berechtigt für " +
                    "diese Aktion.");
            facesContext.addMessage(null, msg);
        }
    }

    /**
     * Resets the password.
     */
    public void resetPassword() {
        try {
            userService.resetPw(user);
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "Passwort zurückgesetzt.");
            facesContext.addMessage(null, msg);
        } catch (UserNotFoundException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Nutzer nicht gefunden.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Daten veraltet. Seite " +
                    "bitte neu laden.");
            facesContext.addMessage(null, msg);
        } catch (DuplicateEmailException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Email bereits vergeben.");
            facesContext.addMessage(null, msg);
        }
    }
}
