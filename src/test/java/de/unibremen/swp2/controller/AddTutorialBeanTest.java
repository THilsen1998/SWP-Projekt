package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.MeetingNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.ParticipantNotInMeetingException;
import de.unibremen.swp2.persistence.Exceptions.TutorialNotFoundException;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.service.TutorialService;
import de.unibremen.swp2.service.UserService;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AddTutorialBeanTest {

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @InjectMocks
    AddTutorialBean addTutorialBean;

    @Mock
    MeetingService meetingService;

    @Mock
    UserService userService;
    
    @Mock
    List<User> users;

    @Mock
    List<Participant> participants;

    @Mock
    ParticipantService participantService;

    @Mock
    TutorialService tutorialService;

    @Mock
    Meeting meeting;

    @Mock
    List<Participant> participantsToAdd;

    @Mock
    List<Participant> filteredParticipants;

    @Mock
    List<Participant> filteredParticipantsToAdd;

    @Mock
    List<User> usersToAdd;

    @Mock
    List<User> filteredUsersToAdd;

    @Mock
    List<User> filteredUsers;

    @Mock
    Principal principal;


    @Test
    void initParaNullTest()
    {
        List <User>Users = Mockito.mock(List.class);
        List <Participant>participants = Mockito.mock(List.class);
        parameterMap.put("meeting-Id", null);
        new Mirror().on(addTutorialBean).invoke().method("init").withoutArgs();

        assertNull(meetingService.getById(parameterMap.get("meeting-Id")));
        verify(parameterMap, times(1)).put(any(), any());
    }

    @Test
    void initAdminTest()
    {
        User user = new User();
        user.setRole(Role.A);
        List <User>Users = Mockito.mock(List.class);
        List <Participant>participants = Mockito.mock(List.class);
        Meeting meeting = Mockito.mock(Meeting.class);;
        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        UserMeetingRole userMeetingRole = new UserMeetingRole();

        //parameterMap.put("meeting-Id", anyString());
        userMeetingRole.setRole(Role.A);
        when(parameterMap.get(anyString())).thenReturn(value);
        when(meetingService.getById(any())).thenReturn(meeting);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(participantService.getAllParticipantsNotInTutorial(any())).thenReturn(participants);

        new Mirror().on(addTutorialBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(any());
        verify(meetingService, times(1)).getById( any());
        verify(userService, times(1)).getUsersByEmail( any());
        verify(participantService, times(1)).getAllParticipantsNotInTutorial( any());

    }

    @Test
    void initNotAdminTest()
    {
        User user = new User();
        user.setRole(Role.T);
        List <User>Users = Mockito.mock(List.class);
        List <Participant>participants = Mockito.mock(List.class);
        UserMeetingRole userMeetingRole = new UserMeetingRole();
        Meeting meeting = Mockito.mock(Meeting.class);;
        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        //parameterMap.put("meeting-Id", anyString());
        when(parameterMap.get(anyString())).thenReturn(value);
        when(meetingService.getById(any())).thenReturn(meeting);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);

        new Mirror().on(addTutorialBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(any());
        verify(meetingService, times(1)).getById( any());
        verify(userService, times(1)).getUsersByEmail( any());
        verify(userService, times(1)).getUserMeetingRoleByUserAndMeeting(any(), any());
    }



    @Test
    void createTutorial() throws TutorialNotFoundException, ParticipantNotInMeetingException, MeetingNotFoundException, OutdatedException, IOException {
        doNothing().when(tutorialService).create(any(), any(), any());
        doNothing().when(externalContext).redirect(any());

        addTutorialBean.createTutorial();

        verify(tutorialService, times(1)).create(any(), any(), any());
        verifyNoMoreInteractions(tutorialService);
        verify(externalContext, times(1)).redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }

    @Test
    void tutorialNotFoundExceptionTest() throws IOException, TutorialNotFoundException, ParticipantNotInMeetingException, MeetingNotFoundException, OutdatedException {
        doThrow(new TutorialNotFoundException()).when(tutorialService).create(any(), any(), any());

        addTutorialBean.createTutorial();

        verify(tutorialService, times(1)).create(any(), any() , any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void outdatedExceptionTest() throws IOException, TutorialNotFoundException, ParticipantNotInMeetingException, MeetingNotFoundException, OutdatedException {
        doThrow(new OutdatedException()).when(tutorialService).create(any(), any(), any());

        addTutorialBean.createTutorial();

        verify(tutorialService, times(1)).create(any(), any() , any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void participantNotInMeetingExceptionTest() throws IOException, TutorialNotFoundException, ParticipantNotInMeetingException, MeetingNotFoundException, OutdatedException {
        doThrow(new ParticipantNotInMeetingException()).when(tutorialService).create(any(), any(), any());

        addTutorialBean.createTutorial();

        verify(tutorialService, times(1)).create(any(), any() , any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void MeetingNotFoundExceptionTest() throws IOException, TutorialNotFoundException, ParticipantNotInMeetingException, MeetingNotFoundException, OutdatedException {
        doThrow(new MeetingNotFoundException()).when(tutorialService).create(any(), any(), any());

        addTutorialBean.createTutorial();

        verify(tutorialService, times(1)).create(any(), any() , any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void addParticipantTest()       // 1 if Zweig
    {

        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(true);
        lenient().when(participants.remove(participant)).thenReturn(true);

        addTutorialBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participants, times(1)).remove(any());
    }

    @Test
    void addParticipant2Test()  // else Teil
    {

        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(true);

        addTutorialBean.addParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addParticipant3Test()  // 2 if Zweig und else Zweig
    {
        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participants.remove(participant)).thenReturn(false);
        addTutorialBean.setFilteredParticipants(filteredParticipants);
        lenient().when(filteredParticipants.remove(participant)).thenReturn(true);

        addTutorialBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participants, times(1)).remove(any());
        verify(filteredParticipants, times(1)).remove(any());
    }


    @Test
    void addParticipant4Test()  // 3 if Zweig
    {
        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participants.remove(participant)).thenReturn(false);
        addTutorialBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
        lenient().when(filteredParticipantsToAdd.remove(participant)).thenReturn(true);

        addTutorialBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participants, times(1)).remove(any());
        verify(filteredParticipantsToAdd, times(1)).add(any());
    }



    @Test
    void deleteParticipantTest() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participants.add(participant)).thenReturn(true);

        addTutorialBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participants, times(1)).add(any());
    }

    @Test
    void deleteParticipant2Test() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(false);

        addTutorialBean.deleteParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void deleteParticipant3Test() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participants.add(participant)).thenReturn(true);
        addTutorialBean.setFilteredParticipants(filteredParticipants);
        lenient().when(filteredParticipants.add(participant)).thenReturn(true);

        addTutorialBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participants, times(1)).add(any());
        verify(filteredParticipants, times(1)).add(any());
    }

    @Test
    void deleteParticipant4Test() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participants.add(participant)).thenReturn(true);
        addTutorialBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
        lenient().when(filteredParticipantsToAdd.remove(participant)).thenReturn(true);

        addTutorialBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participants, times(1)).add(any());
        verify(filteredParticipantsToAdd, times(1)).remove(any());
    }

    @Test
    void addTutorTest() {
        User tutor = Mockito.mock(User.class);
        when(!usersToAdd.contains(tutor)).thenReturn(false);
        lenient().when(usersToAdd.add(tutor)).thenReturn(true);
        lenient().when(users.remove(tutor)).thenReturn(true);

        addTutorialBean.addTutor(tutor);

        verify(usersToAdd, times(1)).add(any());
        verify(users, times(1)).remove(any());
    }

    @Test
    void addTutor2Test()  // else Teil
    {

        User tutor = Mockito.mock(User.class);
        when(!usersToAdd.contains(tutor)).thenReturn(true);

        addTutorialBean.addTutor(tutor);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addTutor3Test()  // 2 if Zweig und else Zweig
    {
        User tutor = Mockito.mock(User.class);
        when(!usersToAdd.contains(tutor)).thenReturn(false);
        lenient().when(usersToAdd.add(tutor)).thenReturn(true);
        lenient().when(users.remove(tutor)).thenReturn(true);
        addTutorialBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsers.remove(tutor)).thenReturn(true);

        addTutorialBean.addTutor(tutor);

        verify(usersToAdd, times(1)).add(any());
        verify(users, times(1)).remove(any());
        verify(filteredUsers, times(1)).remove(any());
    }

    @Test
    void addTutor4Test()  // 3 if Zweig
    {
        User tutor = Mockito.mock(User.class);
        when(!usersToAdd.contains(tutor)).thenReturn(false);
        lenient().when(usersToAdd.add(tutor)).thenReturn(true);
        lenient().when(users.remove(tutor)).thenReturn(true);
        addTutorialBean.setFilteredUsers(filteredUsersToAdd);
        lenient().when(filteredUsersToAdd.add(tutor)).thenReturn(true);

        addTutorialBean.addTutor(tutor);

        verify(usersToAdd, times(1)).add(any());
        verify(users, times(1)).remove(any());
        verify(filteredUsersToAdd, times(1)).add(any());
    }


    @Test
    void deleteTutorTest() {
        User tutor = Mockito.mock(User.class);
        when(usersToAdd.contains(tutor)).thenReturn(true);
        lenient().when(usersToAdd.remove(tutor)).thenReturn(true);
        lenient().when(users.add(tutor)).thenReturn(true);

        addTutorialBean.deleteTutor(tutor);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
    }

    @Test
    void deleteTutor2Test(){
        User tutor = Mockito.mock(User.class);
        when(usersToAdd.contains(tutor)).thenReturn(false);

        addTutorialBean.deleteTutor(tutor);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void deleteTutor3Test(){
        User tutor = Mockito.mock(User.class);
        when(usersToAdd.contains(tutor)).thenReturn(true);
        lenient().when(usersToAdd.remove(tutor)).thenReturn(true);
        lenient().when(users.add(tutor)).thenReturn(true);
        addTutorialBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsers.add(tutor)).thenReturn(true);

        addTutorialBean.deleteTutor(tutor);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
        verify(filteredUsers, times(1)).add(any());
    }


    @Test
    void deleteTutor4Test(){
        User tutor = Mockito.mock(User.class);
        when(usersToAdd.contains(tutor)).thenReturn(true);
        lenient().when(usersToAdd.remove(tutor)).thenReturn(true);
        lenient().when(users.add(tutor)).thenReturn(true);
        addTutorialBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsersToAdd.remove(tutor)).thenReturn(true);

        addTutorialBean.deleteTutor(tutor);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
        verify(filteredUsersToAdd, times(1)).remove(any());
    }
}