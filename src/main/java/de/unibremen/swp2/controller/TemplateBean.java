package de.unibremen.swp2.controller;


import de.unibremen.swp2.model.User;
import de.unibremen.swp2.service.SessionService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;

/**
 * @Author Khaled, Navy
 * Template.
 */
@Named
@RequestScoped
public class TemplateBean {

    /**
     * Provides the username (email) of the logged in user.
     */
    @Inject
    private Principal principal;

    /**
     * Logged in user.
     */
    private User user;

    @Inject
    private SessionService sessionService;


    /**
     * Used for redirection.
     */
    @Inject
    private ExternalContext externalContext;

    /**
     * Initializes this bean.
     *
    @PostConstruct
    private void init() {
        user = userDAO.findByEmail(principal.getName())
                .orElseThrow(SecurityException::new);
    }/

    /**
     * Returns the full name of the logged in user.
     *
     * @return
     *      Pattern: 'firstName lastName'
     *
     *  public String getFullName() {
        return String.format("%s %s",
                user.getFirstName(),
                user.getLastName());
    }

    /**
     * Logs out the user and registers a redirection to 'login.xhtml'.
     *
     * @throws IOException
     *      If redirection failed.
     */
    public void logout() throws IOException {
        sessionService.logout(principal.getName());
        externalContext.redirect("login.xhtml");
    }
}
