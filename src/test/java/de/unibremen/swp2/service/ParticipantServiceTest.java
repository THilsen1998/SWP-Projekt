package de.unibremen.swp2.service;

import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotFoundException;
import de.unibremen.swp2.persistence.ParticipantDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private ParticipantDAO participantDAO;

    @InjectMocks
    ParticipantService participantService = PowerMockito.spy(new ParticipantService());;

    @Mock
    Participant participant;

    @Test
    void create() {
    }

    @Test
    void update() throws OutdatedException, ParticipantNotFoundException, DuplicateEmailException {

        doNothing().when(participantDAO) .update(any());
        participantDAO.update(any());
        verify(participantDAO,times(1)).update(any());
    }





    @Test
    void updateParticipantStatus() throws OutdatedException, EntityNotFoundException {
        doNothing().when(participantDAO) .updateParticipantStatus(any());
        participantDAO.updateParticipantStatus(any());
        verify(participantDAO,times(1)).updateParticipantStatus(any());
    }

    @Test
    void getById() {
        String s = "";
        Participant p = Mockito.mock( Participant.class);
        when(participantDAO.getById(s)).thenReturn(p);
        participantService.getById(s);
        verify(participantDAO,times(1)).getById(s);
    }



    @Test
    void getAllParticipants() {

            Meeting m = Mockito.mock( Meeting.class);
            when(participantDAO.getAllParticipants()).thenReturn(new ArrayList<>());
            participantService.getAllParticipants();
            verify(participantDAO,times(1)).getAllParticipants();

    }

    @Test
    void getAllParticipantsByTutorial()
    {/*
        when(participantDAO.getAllParticipantsByTutorial(any())).thenReturn(new Participant());
        participantService.getAllParticipantsByTutorial(any());
        verify(participantDAO,times(1)).getAllParticipantsByTutorial(any());
    */}


    @Test
    void delete() {
    }



/*
    @Test
    void updateMeetingOnlyTest() throws EntityNotFoundException, OutdatedException
    {
        doNothing().when(meetingDAO) .update(any());
        meetingService.updateMeetingOnly(any());
        verify(meetingDAO,times(1)).update(any());
    }
   */
}