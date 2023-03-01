package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotFoundException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.ParticipantService;
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
 * @Author Khaled, Martin
 * Allows to edit a participant.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class EditParticipantBean implements Serializable {

    /**
     * Allows to edit,update,delete,creat and to perform other operations
     * on a Particpant
     */
    @Inject
    private ParticipantService participantService;

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
     * Participant to edit.
     */
    @Getter
    private Participant participant;

    /**
     * Parameter-Map which provides the id
     */
    @Inject
    @RequestParameterMap
    private Map<String,String> parameterMap;

    /**
     * Initializes this bean.
     */
    @PostConstruct
    private void init() {
        final String id = parameterMap.get("participant-Id");
        if (id == null) {
            participant = null;
        } else {
            participant = participantService.getById(id);
        }
    }

    /**
     * Updates {@link #participant}. On success, a redirect to 'participant.xhtml' is
     * registered.
     */
    public void update() throws IOException {
        try {
            participantService.update(participant);
            externalContext.redirect("participants.xhtml");
        } catch (ParticipantNotFoundException e) {
            FacesMessage msg = new FacesMessage("Der Teilnehmer ist nicht mehr im System.");
            facesContext.addMessage(null, msg);
        } catch (OutdatedException e) {
            FacesMessage msg = new FacesMessage("Der Teilnehmer ist veraltet. Bitte Seite neu laden.");
            facesContext.addMessage(null, msg);
        } catch (DuplicateEmailException e) {
            final FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Die E-Mail Adresse wird bereits verwendet");
            facesContext.addMessage(null, msg);
        }
    }
}
