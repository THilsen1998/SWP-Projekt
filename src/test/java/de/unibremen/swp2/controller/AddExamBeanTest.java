package de.unibremen.swp2.controller;


import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.ExamDAO;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.EntityAlreadyInsertedException;
import de.unibremen.swp2.persistence.Exceptions.MeetingNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.UserNotPermittedException;
import de.unibremen.swp2.service.ExamService;
import de.unibremen.swp2.service.MeetingService;
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
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

//lassen diesen Test mit mockito laufen
@ExtendWith(MockitoExtension.class)
class AddExamBeanTest {


    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    ExamService examService;

    @Mock
    MeetingService meetingService;

    @Mock
    Meeting meeting;

    @Mock
    RequestParameterMap requestParameterMap;

    @Mock
    Exam exam;
    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @InjectMocks
    AddExamBean addExamBean;

    @Mock
    UserService userService;

    @Mock
    Principal principal;

    @Test
    void createExam() throws EntityAlreadyInsertedException, IOException, MeetingNotFoundException {
        doNothing().when(examService).create(any());
        doNothing().when(externalContext).redirect(any());

        addExamBean.createExam();

        verify(examService, times(1)).create(any());
        verifyNoMoreInteractions(examService);
        verify(externalContext, times(1)).redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);

    }

    @Test
    public void meetingNotFoundExceptionTest() throws UserNotPermittedException, DuplicateEmailException, EntityAlreadyInsertedException, IOException, MeetingNotFoundException {
        doThrow(new MeetingNotFoundException()).when(examService).create(any());

        addExamBean.createExam();

        verify(examService, times(1)).create(any());
        verifyNoMoreInteractions(examService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    void initTest() {
        assertNotNull(addExamBean.getExam());
        parameterMap.put("meeting-Id", null);
        new Mirror().on(addExamBean).invoke().method("init").withoutArgs();
        assertNull(meetingService.getById(parameterMap.get("meeting-Id")));
        parameterMap.put("meeting-Id", meeting.getId());
        assertNull(meetingService.getById(parameterMap.get("meeting-Id")));
        verify(parameterMap,times(2)).put(any(),any());

    }

    @Test
    void initTest2()
    {
        User user = new User();
        user.setRole(Role.A);

        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(meetingService.getById(any())).thenReturn(meeting);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        new Mirror().on(addExamBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(anyString());
        verify(meetingService, times(1)).getById(any());
        verify(userService, times(1)).getUsersByEmail(any());
    }

    @Test
    void init3Test() {

        User user = new User();
        user.setRole(Role.CEO);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.CEO);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(meetingService.getById(any())).thenReturn(meeting);

        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);

        new Mirror().on(addExamBean).invoke().method("init").withoutArgs();

        verify(parameterMap, times(1)).get(anyString());
        verify(meetingService, times(1)).getById(any());
        verify(userService, times(1)).getUsersByEmail(any());
        verify(userService, times(1)).getUserMeetingRoleByUserAndMeeting(any(), any());
    }

}