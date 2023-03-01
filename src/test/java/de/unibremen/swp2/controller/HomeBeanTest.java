package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Role;
import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.*;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import lombok.Setter;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HomeBeanTest
{
    @Mock
     UserService userService;
    @Mock
     MeetingService meetingService;
    @Mock
     Principal principal;
    @Mock
     FacesContext facesContext;
    @InjectMocks
    HomeBean homeBean;
    @Mock
    Meeting meeting;
    @Mock
    private List<Meeting> meetings;



    @Test
    void initTest() throws EntityNotFoundException, OutdatedException
    {
        User u = new User();
        u.setRole(Role.A);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(u);
        new Mirror().on(homeBean).invoke().method("init").withoutArgs();
        User u1 = new User();
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(u1);
        new Mirror().on(homeBean).invoke().method("init").withoutArgs();

        verify(userService,times(2)).getUsersByEmail(principal.getName());

    }
    @Test
    public void entityAlreadyInsertedTest() throws  EntityNotFoundException, OutdatedException
    {
        doThrow(new EntityNotFoundException()).when(meetingService).updateMeetingOnly(any());
        homeBean.hideMeeting(any());

        verify(meetingService, times(1)).updateMeetingOnly(any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }
    @Test
    public void OutdatedExceptionTest() throws  EntityNotFoundException, OutdatedException
    {
        doThrow(new OutdatedException()).when(meetingService).updateMeetingOnly(any());
        homeBean.hideMeeting(any());

        verify(meetingService, times(1)).updateMeetingOnly(any());
        verifyNoMoreInteractions(meetingService);
        verify(facesContext, times(1)).addMessage(anyObject(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void hideMeetingTest() throws EntityNotFoundException, OutdatedException
    {
       doNothing().when(meetingService).updateMeetingOnly(any());
       Meeting m = new Meeting();
       lenient().when(meetingService.getById(meeting.getId())).thenReturn(m);
       lenient().when(meetings.indexOf(meeting)).thenReturn(anyInt());
       homeBean.hideMeeting(meeting);

       verify(meetingService,times(1)).updateMeetingOnly(any());

    }
    
}