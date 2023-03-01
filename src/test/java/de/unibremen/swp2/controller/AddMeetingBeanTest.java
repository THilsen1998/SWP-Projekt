package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.ParticipantService;
import de.unibremen.swp2.service.UserService;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddMeetingBeanTest {

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    UserService userService;

    @InjectMocks
    AddMeetingBean addMeetingBean;

    @Mock
    ParticipantService participantService;

    @Mock
    List<User> usersToAdd;

    @Mock
    Principal principal;

    @Mock
    MeetingService meetingService;

    @Mock
    User ceoToAdd;

    @Mock
    List<User> allUsers;

    @Mock
    List<Participant> participantsToAdd;

    @Mock
    List<Participant> filteredParticipants;

    @Mock
    List<Participant> filteredParticipantsToAdd;

    @Mock
    List<Participant> participants;

    @Mock
    List<User> users;

    @Mock
    List<User> filteredUsersToAdd;

    @Mock
    List<User> filteredUsers;

    @Test
    void initTest()
    {
        //List<User> users = Mockito.mock(List.class);
        List<Participant> participants = Mockito.mock(List.class);
        User user = new User();
        lenient().when(userService.getAllUsers()).thenReturn(allUsers);
        //lenient().when(allUsers.stream().filter(any())).thenReturn(any());
        // users = allUsers.stream().filter(u -> u.getRole() == Role.D || u.getRole() == Role.A).collect(Collectors.toList()); fehlt
        lenient().when(participantService.getAllParticipants()).thenReturn(participants);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);

        new Mirror().on(addMeetingBean).invoke().method("init").withoutArgs();

        verify(participantService, times(1)).getAllParticipants();
        verify(userService, times(1)).getUsersByEmail(any());
    }

    @Test
    void createMeeting() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doNothing().when(meetingService).create(any(), any(), any(), any());
        doNothing().when(externalContext).redirect(any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(),any(), any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(externalContext, times(1)).redirect("home.xhtml");
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }


    @Test
    void ParticipantNotFoundExceptionTest() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doThrow(new ParticipantNotFoundException()).when(meetingService).create(any(), any(),any(), any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(), any() , any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void ParticipantAlreadyInMeetingExceptionTest() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doThrow(new ParticipantAlreadyInMeetingException()).when(meetingService).create(any(), any(),any(), any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(), any() , any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void UserNotPermittedExceptionTest() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doThrow(new UserNotPermittedException()).when(meetingService).create(any(), any(),any(), any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(), any() , any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void UserNotFoundExceptionTest() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doThrow(new UserNotFoundException()).when(meetingService).create(any(), any(),any(), any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(), any() , any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void UserAlreadyInMeetingExceptionTest() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doThrow(new UserAlreadyInMeetingException()).when(meetingService).create(any(), any(),any(), any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(), any() , any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void MeetingNotFoundExceptionTest() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doThrow(new MeetingNotFoundException()).when(meetingService).create(any(), any(),any(), any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(), any() , any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void UserAlreadyInOtherRoleExceptionTest() throws MeetingNotFoundException, ParticipantAlreadyInMeetingException, UserAlreadyInOtherRoleException, UserNotFoundException, UserAlreadyInMeetingException, UserNotPermittedException, ParticipantNotFoundException, IOException {
        doThrow(new UserAlreadyInOtherRoleException()).when(meetingService).create(any(), any(),any(), any());

        addMeetingBean.createMeeting();

        verify(meetingService, times(1)).create(any(), any() , any(), any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addCEO() {
        User ceo = new User();
        // Wenn in Methode gemockt dann ist null wenn bei Attribut gemockt dann != null
        lenient().when(allUsers.remove(any())).thenReturn(true);

        addMeetingBean.addCEO(ceo);

        verify(allUsers, times(1)).remove(any());
    }

    @Test
    void addCEO2() {
        User ceo = new User();
        lenient().when(allUsers.add(any())).thenReturn(true);
        lenient().when(allUsers.remove(any())).thenReturn(true);

        addMeetingBean.addCEO(ceo);

        verify(allUsers, times(1)).remove(any());
        verify(allUsers, times(1)).add(any());
    }

    @Test
    void deleteCEO() {
        lenient().when(allUsers.add(any())).thenReturn(true);

        addMeetingBean.deleteCEO();

        verify(allUsers, times(1)).add(any());
    }

    @Test
    void addParticipantTest()       // 1 if Zweig
    {

        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(false);
        lenient().when(participantsToAdd.add(participant)).thenReturn(true);
        lenient().when(participants.remove(participant)).thenReturn(true);

        addMeetingBean.addParticipant(participant);

        verify(participantsToAdd, times(1)).add(any());
        verify(participants, times(1)).remove(any());
    }

    @Test
    void addParticipant2Test()  // else Teil
    {

        Participant participant = Mockito.mock(Participant.class);
        when(!participantsToAdd.contains(participant)).thenReturn(true);

        addMeetingBean.addParticipant(participant);

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
        addMeetingBean.setFilteredParticipants(filteredParticipants);
        lenient().when(filteredParticipants.remove(participant)).thenReturn(true);

        addMeetingBean.addParticipant(participant);

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
        addMeetingBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
        lenient().when(filteredParticipantsToAdd.remove(participant)).thenReturn(true);

        addMeetingBean.addParticipant(participant);

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

        addMeetingBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participants, times(1)).add(any());
    }

    @Test
    void deleteParticipant2Test() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(false);

        addMeetingBean.deleteParticipant(participant);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void deleteParticipant3Test() {
        Participant participant = Mockito.mock(Participant.class);
        when(participantsToAdd.contains(participant)).thenReturn(true);
        lenient().when(participantsToAdd.remove(participant)).thenReturn(true);
        lenient().when(participants.add(participant)).thenReturn(true);
        addMeetingBean.setFilteredParticipants(filteredParticipants);
        lenient().when(filteredParticipants.add(participant)).thenReturn(true);

        addMeetingBean.deleteParticipant(participant);

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
        addMeetingBean.setFilteredParticipantsToAdd(filteredParticipantsToAdd);
        lenient().when(filteredParticipantsToAdd.remove(participant)).thenReturn(true);

        addMeetingBean.deleteParticipant(participant);

        verify(participantsToAdd, times(1)).remove(any());
        verify(participants, times(1)).add(any());
        verify(filteredParticipantsToAdd, times(1)).remove(any());
    }


    @Test
    void addUserTest() {
        User user = Mockito.mock(User.class);
        when(!usersToAdd.contains(user)).thenReturn(false);
        lenient().when(usersToAdd.add(user)).thenReturn(true);
        lenient().when(users.remove(user)).thenReturn(true);

        addMeetingBean.addUser(user);

        verify(usersToAdd, times(1)).add(any());
        verify(users, times(1)).remove(any());
    }

    @Test
    void addUser2Test()  // else Teil
    {

        User user = Mockito.mock(User.class);
        when(!usersToAdd.contains(user)).thenReturn(true);

        addMeetingBean.addUser(user);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void addUser3Test()  // 2 if Zweig und else Zweig
    {
        User user = Mockito.mock(User.class);
        when(!usersToAdd.contains(user)).thenReturn(false);
        lenient().when(usersToAdd.add(user)).thenReturn(true);
        lenient().when(users.remove(user)).thenReturn(true);
        addMeetingBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsers.remove(user)).thenReturn(true);

        addMeetingBean.addUser(user);

        verify(usersToAdd, times(1)).add(any());
        verify(users, times(1)).remove(any());
        verify(filteredUsers, times(1)).remove(any());
    }

    @Test
    void addUser4Test()  // 3 if Zweig
    {
        User user = Mockito.mock(User.class);
        when(!usersToAdd.contains(user)).thenReturn(false);
        lenient().when(usersToAdd.add(user)).thenReturn(true);
        lenient().when(users.remove(user)).thenReturn(true);
        addMeetingBean.setFilteredUsers(filteredUsersToAdd);
        lenient().when(filteredUsersToAdd.add(user)).thenReturn(true);

        addMeetingBean.addUser(user);

        verify(usersToAdd, times(1)).add(any());
        verify(users, times(1)).remove(any());
        verify(filteredUsersToAdd, times(1)).add(any());
    }

    @Test
    void deleteUserTest() {
        User User = Mockito.mock(User.class);
        when(usersToAdd.contains(User)).thenReturn(true);
        lenient().when(usersToAdd.remove(User)).thenReturn(true);
        lenient().when(users.add(User)).thenReturn(true);

        addMeetingBean.deleteUser(User);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
    }

    @Test
    void deleteUser2Test(){
        User User = Mockito.mock(User.class);
        when(usersToAdd.contains(User)).thenReturn(false);

        addMeetingBean.deleteUser(User);

        verify(facesContext, times(1)).addMessage(any(), notNull());    // war vorher bei unseren Test anyObject()(deprecated), null nimmt er ansonsten nicht an
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void deleteUser3Test(){
        User User = Mockito.mock(User.class);
        when(usersToAdd.contains(User)).thenReturn(true);
        lenient().when(usersToAdd.remove(User)).thenReturn(true);
        lenient().when(users.add(User)).thenReturn(true);
        addMeetingBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsers.add(User)).thenReturn(true);

        addMeetingBean.deleteUser(User);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
        verify(filteredUsers, times(1)).add(any());
    }


    @Test
    void deleteTutor4Test(){
        User User = Mockito.mock(User.class);
        when(usersToAdd.contains(User)).thenReturn(true);
        lenient().when(usersToAdd.remove(User)).thenReturn(true);
        lenient().when(users.add(User)).thenReturn(true);
        addMeetingBean.setFilteredUsers(filteredUsers);
        lenient().when(filteredUsersToAdd.remove(User)).thenReturn(true);

        addMeetingBean.deleteUser(User);

        verify(usersToAdd, times(1)).remove(any());
        verify(users, times(1)).add(any());
        verify(filteredUsersToAdd, times(1)).remove(any());
    }
}