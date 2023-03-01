package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.ParticipantService;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;


@ExtendWith(MockitoExtension.class)
class EditParticipantBeanTest {

    @InjectMocks
    EditParticipantBean editParticipantBean;

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    ParticipantService participantService;

    @Mock
    Participant participant;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;



    @Test
    void initTest() {
        assertNotNull(editParticipantBean.getParticipant());
        parameterMap.put("participant-Id", null);
        new Mirror().on(editParticipantBean).invoke().method("init").withoutArgs();
        assertNull(participantService.getById(parameterMap.get("participant-Id")));
        parameterMap.put("participant-Id", participant.getId());
        assertNull(participantService.getById(parameterMap.get("participant-Id")));

        verify(parameterMap, times(2)).put(any(),any());
    }

    @Test
    void initTest2(){

        Participant p = new Participant();
        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(participantService.getById(any())).thenReturn(p);
        new Mirror().on(editParticipantBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(anyString());
        verify(participantService, times(1)).getById(any());
    }



    @Test
    void update() throws IOException, OutdatedException, ParticipantNotFoundException, DuplicateEmailException {


        doNothing().when(participantService).update(any());
        editParticipantBean.update();

        verify(participantService, times(1)).update(any());
        verifyNoMoreInteractions(participantService);
        verify(externalContext, times(1)).redirect("participants.xhtml");
        verifyNoMoreInteractions(externalContext);
    }

    @Test
    public void ParticipantNotFoundException() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException, ParticipantNotFoundException {

        doThrow(new ParticipantNotFoundException()).when(participantService).update(any());
        editParticipantBean.update();
        verify(participantService, times(1)).update(any());
        verifyNoMoreInteractions(participantService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);

    }


    @Test
    public void OutdatedException() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException, ParticipantNotFoundException {

        doThrow(new OutdatedException()).when(participantService).update(any());
        editParticipantBean.update();
        verify(participantService, times(1)).update(any());
        verifyNoMoreInteractions(participantService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);

    }

    @Test
    public void DuplicateEmailException() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException, ParticipantNotFoundException {

        doThrow(new DuplicateEmailException()).when(participantService).update(any());
        editParticipantBean.update();
        verify(participantService, times(1)).update(any());
        verifyNoMoreInteractions(participantService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);

    }



}