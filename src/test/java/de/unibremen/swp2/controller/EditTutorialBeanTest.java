package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.*;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EditTutorialBeanTest {

    @InjectMocks
    EditTutorialBean editTutorialBean;

    @Mock
    TutorialService tutorialService;

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    Tutorial tutorial;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Mock
    UserService userService;

    @Mock
    ParticipantService participantService;

    @Mock
    List<Participant> participantsToAdd;

    @Mock
    List<Participant> participantsInMeeting;

    @Mock
    List<Participant> filteredParticipantsInMeeting;

    @Mock
    List<User> users;

    @Mock
    List<User> usersToAdd;

    @Mock
    List<User> filteredUsersToAdd;

    @Mock
    List<User> filteredUsers;

    @Mock
    Principal principal;

    @Mock
    Meeting meeting;

    @Mock
    MeetingService meetingService;

    @Test
    void initNotAdminTest()
    {
        User user = new User();
        user.setRole(Role.D);
        UserMeetingRole userMeetingRole = new UserMeetingRole();
        Tutorial tutorial1 = Mockito.mock(Tutorial.class);
        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        userMeetingRole.setRole(Role.D);
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial1);

        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);

        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);


        lenient().when(userService.getAllUsersNotInTutorial(any())).thenReturn(users);
        lenient().when(userService.getUsersByTutorial(any())).thenReturn(usersToAdd);
        lenient().when(participantService.getAllParticipantsNotInTutorial(any())).thenReturn(participantsInMeeting);
        lenient().when(participantService.getAllParticipantsByTutorial(any())).thenReturn(participantsToAdd);

        new Mirror().on(editTutorialBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(any());
        verify(tutorialService, times(1)).getById(any());


        verify(userService, times(1)).getUsersByEmail(any());
        verify(userService, times(1)).getUserMeetingRoleByUserAndMeeting(any(), any());

        verify(userService, times(1)).getAllUsersNotInTutorial(any());
        verify(userService, times(1)).getUsersByTutorial(any());
        verify(participantService, times(1)).getAllParticipantsNotInTutorial(any());
        verify(participantService, times(1)).getAllParticipantsByTutorial(any());
    }

    @Test
    void initAdminTest()
    {
        User user = new User();
        user.setRole(Role.A);
        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.A);

        Tutorial tutorial1 = Mockito.mock(Tutorial.class);
        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(tutorialService.getById(any())).thenReturn(tutorial1);

        when(meetingService.getMeetingByTutorial(any())).thenReturn(meeting);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);

        lenient().when(userService.getAllUsersNotInTutorial(any())).thenReturn(users);
        lenient().when(userService.getUsersByTutorial(any())).thenReturn(usersToAdd);
        lenient().when(participantService.getAllParticipantsNotInTutorial(any())).thenReturn(participantsInMeeting);
        lenient().when(participantService.getAllParticipantsByTutorial(any())).thenReturn(participantsToAdd);

        new Mirror().on(editTutorialBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(any());
        verify(tutorialService, times(1)).getById(any());
        verify(userService, times(1)).getUsersByEmail(any());
        verify(userService, times(1)).getAllUsersNotInTutorial(any());
        verify(userService, times(1)).getUsersByTutorial(any());
        verify(participantService, times(1)).getAllParticipantsNotInTutorial(any());
        verify(participantService, times(1)).getAllParticipantsByTutorial(any());
    }


    @Test
    void update() throws EntityNotFoundException, UserNotInMeetingException, ParticipantNotInMeetingException, TutorialNotFoundException, OutdatedException, UserAlreadyInMeetingException, IOException {
        doNothing().when(tutorialService).update(any(), any(),any());
        doNothing().when(externalContext).redirect(any());

        editTutorialBean.update();

        verify(tutorialService, times(1)).update(any(), any(), any());
        verifyNoMoreInteractions(tutorialService);
        verify(externalContext, times(1)).redirect("single-tutorial.xhtml?tutorial-Id=" + tutorial.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);
    }

    @Test
    void OutdatedExceptionTest() throws EntityNotFoundException, UserAlreadyInMeetingException, IOException, UserNotInMeetingException, TutorialNotFoundException, ParticipantNotInMeetingException, OutdatedException {
        doThrow(new OutdatedException()).when(tutorialService).update(any(),any(), any());

        editTutorialBean.update();

        verify(tutorialService, times(1)).update( any() , any(), any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void EntityNotFoundExceptionTest() throws EntityNotFoundException, UserAlreadyInMeetingException, IOException, UserNotInMeetingException, TutorialNotFoundException, ParticipantNotInMeetingException, OutdatedException {
        doThrow(new EntityNotFoundException()).when(tutorialService).update(any(),any(), any());

        editTutorialBean.update();

        verify(tutorialService, times(1)).update( any() , any(), any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void UserNotInMeetingExceptionTest() throws EntityNotFoundException, UserAlreadyInMeetingException, IOException, UserNotInMeetingException, TutorialNotFoundException, ParticipantNotInMeetingException, OutdatedException {
        doThrow(new UserNotInMeetingException()).when(tutorialService).update(any(),any(), any());

        editTutorialBean.update();

        verify(tutorialService, times(1)).update( any() , any(), any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void UserAlreadyInMeetingExceptionTest() throws EntityNotFoundException, UserAlreadyInMeetingException, IOException, UserNotInMeetingException, TutorialNotFoundException, ParticipantNotInMeetingException, OutdatedException {
        doThrow(new UserAlreadyInMeetingException()).when(tutorialService).update(any(),any(), any());

        editTutorialBean.update();

        verify(tutorialService, times(1)).update( any() , any(), any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void ParticipantNotInMeetingExceptionTest() throws EntityNotFoundException, UserAlreadyInMeetingException, IOException, UserNotInMeetingException, TutorialNotFoundException, ParticipantNotInMeetingException, OutdatedException {
        doThrow(new ParticipantNotInMeetingException()).when(tutorialService).update(any(),any(), any());

        editTutorialBean.update();

        verify(tutorialService, times(1)).update( any() , any(), any());
        verifyNoMoreInteractions(tutorialService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void addParticipantTest()       // 1 if Zweig
    {
        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(true);
        lenient().when(participantsInMeeting.remove(participant)).thenReturn(true);

        editTutorialBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInMeeting, times(1)).remove(any());
    }

    @Test
    void addParticipant2Test()  // else Teil
    {

        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(true);

        editTutorialBean.addParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addParticipant3Test()  // 2 if Zweig und else Zweig
    {
        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participantsInMeeting.remove(participant)).thenReturn(false);
        editTutorialBean.setFilteredParticipantsInMeeting(filteredParticipantsInMeeting);
        lenient().when(filteredParticipantsInMeeting.remove(participant)).thenReturn(true);

        editTutorialBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInMeeting, times(1)).remove(any());
        verify(filteredParticipantsInMeeting, times(1)).remove(any());
    }


    @Test
    void addParticipant4Test()  // 3 if Zweig
    {
        Participant participant = new Participant();
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(false);
        lenient().when(participantsInMeeting.remove(participant)).thenReturn(false);
        editTutorialBean.setFilteredParticipantsToAdd(filteredParticipantsInMeeting);
        lenient().when(filteredParticipantsInMeeting.add(participant)).thenReturn(true);

        editTutorialBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participantsInMeeting, times(1)).remove(any());
        verify(filteredParticipantsInMeeting, times(1)).add(any());
    }

    @Test
    void deleteParticipant() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInMeeting.add(participant)).thenReturn(true);

        editTutorialBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInMeeting, times(1)).add(any());
    }

    @Test
    void deleteParticipan2t() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(false);

        editTutorialBean.deleteParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void deleteParticipant3() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInMeeting.add(participant)).thenReturn(true);
        editTutorialBean.setFilteredParticipantsInMeeting(filteredParticipantsInMeeting);
        lenient().when(filteredParticipantsInMeeting.add(participant)).thenReturn(false);

        editTutorialBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInMeeting, times(1)).add(any());
        verify(filteredParticipantsInMeeting, times(1)).add(any());
    }

    @Test
    void deleteParticipant4() {
        Participant participant = new Participant();
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participantsInMeeting.add(participant)).thenReturn(true);
        editTutorialBean.setFilteredParticipantsToAdd(filteredParticipantsInMeeting);
        lenient().when(filteredParticipantsInMeeting.remove(participant)).thenReturn(false);

        editTutorialBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participantsInMeeting, times(1)).add(any());
        verify(filteredParticipantsInMeeting, times(1)).remove(any());
    }

    @Test
    void addTutorTest() {
        User tutor = Mockito.mock(User.class);
        when(!usersToAdd.contains(tutor)).thenReturn(false);
        lenient().when(usersToAdd.add(tutor)).thenReturn(true);
        lenient().when(users.remove(tutor)).thenReturn(true);

        editTutorialBean.addTutor(tutor);

        verify(usersToAdd, times(1)).add(any());
        verify(users, times(1)).remove(any());
    }

    @Test
    void addTutor2Test()  // else Teil
    {

        User tutor = Mockito.mock(User.class);
        when(!usersToAdd.contains(tutor)).thenReturn(true);

        editTutorialBean.addTutor(tutor);

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
        editTutorialBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsers.remove(tutor)).thenReturn(true);

        editTutorialBean.addTutor(tutor);

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
        editTutorialBean.setFilteredUsers(filteredUsersToAdd);
        lenient().when(filteredUsersToAdd.add(tutor)).thenReturn(true);

        editTutorialBean.addTutor(tutor);

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

        editTutorialBean.deleteTutor(tutor);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
    }

    @Test
    void deleteTutor2Test(){
        User tutor = Mockito.mock(User.class);
        when(usersToAdd.contains(tutor)).thenReturn(false);

        editTutorialBean.deleteTutor(tutor);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void deleteTutor3Test(){
        User tutor = Mockito.mock(User.class);
        when(usersToAdd.contains(tutor)).thenReturn(true);
        lenient().when(usersToAdd.remove(tutor)).thenReturn(true);
        lenient().when(users.add(tutor)).thenReturn(true);
        editTutorialBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsers.add(tutor)).thenReturn(true);

        editTutorialBean.deleteTutor(tutor);

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
        editTutorialBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsersToAdd.remove(tutor)).thenReturn(true);

        editTutorialBean.deleteTutor(tutor);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
        verify(filteredUsersToAdd, times(1)).remove(any());
    }
}