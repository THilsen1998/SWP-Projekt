package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.service.ParticipantService;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;


@ExtendWith(MockitoExtension.class)
class ParticipantsBeanTest {



    @Mock
    ParticipantService participantService;

    @InjectMocks
    ParticipantsBean participantsBean;


    @Test
    void initTest() {
        List<Participant> participants = Mockito.mock(List.class);
        assertNotNull(participantService.getAllParticipants());
        new Mirror().on(participantsBean).invoke().method("init").withoutArgs();
        //doNothing().when(participantService).init(any());

    }

}