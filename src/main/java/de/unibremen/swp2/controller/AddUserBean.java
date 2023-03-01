package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.UserNotPermittedException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.UserService;
import lombok.Data;
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


/**
 * @Author Martin, Theo
 * Allows to add a new user.
 */
@Named
@ViewScoped
@FacesConfig
@Data
@GlobalSecure(roles = {Role.A})
public class AddUserBean implements Serializable {

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
     * Allows to edit,update,delete,creat and to perform other operations
     * on a User
     */
    @Inject
    private UserService userService;

    /**
     * User to add.
     */
    @Getter
    private User user;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
    user = new User();
    }

    /**
     * Creates an user.
     */
    public void createUser() throws IOException {
        try {
            userService.create(user);
            externalContext.redirect("users.xhtml");
        } catch (DuplicateEmailException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Die Email ist bereits vergeben.");
            facesContext.addMessage(null, msg);
        } catch (UserNotPermittedException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Nutzer kann nicht als CEO eigetragen werden.");
            facesContext.addMessage(null, msg);
        }

    }


}
