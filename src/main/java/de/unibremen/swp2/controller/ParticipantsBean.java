package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotFoundException;
import de.unibremen.swp2.security.GlobalSecure;
import de.unibremen.swp2.service.ParticipantService;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import javax.faces.annotation.FacesConfig;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.Interceptors;
import java.io.Serializable;
import java.util.List;

/**
 * @Author Dennis, Khaled
 * Allows to load and delete participants.
 */
@Named
@ViewScoped
@FacesConfig
@GlobalSecure(roles = {Role.T, Role.D, Role.A})
public class ParticipantsBean implements Serializable {

    /**
     * Allows to create, update and other operations
     */
    @Inject
    private ParticipantService participantService;

    /**
     * The participants loaded from ParticipantDAO.
     */
    @Getter
    private List<Participant> participants;

    /**
     * The participants loaded from ParticipantDAO filtered.
     */
    @Getter
    @Setter
    private List<Participant> filteredParticipants;

    /**
     * Initializes ParticipantsBean
     */
    @PostConstruct
    private void init() {
        participants = participantService.getAllParticipants();
    }

    /**
     * Initial filter value.
     */
    @Getter
    @Setter
    private String value = "";
}