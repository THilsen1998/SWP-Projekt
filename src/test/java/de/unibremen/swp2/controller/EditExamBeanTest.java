package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.service.ExamService;
import de.unibremen.swp2.service.MeetingService;
import de.unibremen.swp2.service.UserService;
import lombok.Getter;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.primefaces.event.RowEditEvent;

import javax.faces.annotation.RequestParameterMap;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EditExamBeanTest {
    @Mock
    private Exam exam;

    @Mock
    private Meeting meeting;

    @Mock
    private Principal principal;

    @Mock
    private FacesContext facesContext;

    @Mock
    private ExternalContext externalContext;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Mock
    private ExamService examService;

    @Mock
    private UserService userService;

    @Mock
    private MeetingService meetingService;

    @InjectMocks
    EditExamBean editExamBean;

    @Test
    void initTestRoleA() {
        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        Exam exam1 = Mockito.mock(Exam.class);
        when(examService.getById(value)).thenReturn(exam1);
        User user = new User();
        user.setRole(Role.A);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);

        new Mirror().on(editExamBean).invoke().method("init").withoutArgs();
        verify(userService, times(1)).getUsersByEmail(principal.getName());
        verify(parameterMap, times(1)).get(anyString());
        verify(examService, times(1)).getById(value);

    }

    @Test
    void initTestRole() {
        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        Exam exam1 = Mockito.mock(Exam.class);
        when(examService.getById(value)).thenReturn(exam1);
        User user = new User();
        user.setRole(Role.D);
        when(userService.getUsersByEmail(principal.getName())).thenReturn(user);

        Meeting meeting1 = new Meeting();
        when(meetingService.getMeetingsByExam(any())).thenReturn(meeting1);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        when(userService.getUserMeetingRoleByUserAndMeeting(user, meeting1)).thenReturn(userMeetingRole);

        new Mirror().on(editExamBean).invoke().method("init").withoutArgs();

        verify(userService, times(1)).getUsersByEmail(principal.getName());
        verify(parameterMap, times(1)).get(anyString());
        verify(examService, times(1)).getById(value);
        verify(meetingService, times(1)).getMeetingsByExam(any());

    }

    @Test
    void initAsNonTest() {
        User user = new User();
        user.setRole(Role.T);

        UserMeetingRole userMeetingRole = new UserMeetingRole();
        userMeetingRole.setRole(Role.T);

        String value = "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(examService.getById(any())).thenReturn(exam);
        lenient().when(userService.getUsersByEmail(principal.getName())).thenReturn(user);
        lenient().when(meetingService.getMeetingsByExam(any())).thenReturn(meeting);
        lenient().when(userService.getUserMeetingRoleByUserAndMeeting(any(), any())).thenReturn(userMeetingRole);

        new Mirror().on(editExamBean).invoke().method("init").withoutArgs();

        verify(userService, times(1)).getUsersByEmail(principal.getName());
        verify(parameterMap, times(1)).get(anyString());
        verify(examService, times(1)).getById(value);
        verify(meetingService, times(1)).getMeetingsByExam(any());
    }

    @Test
    void update() throws EntityNotFoundException, OutdatedException, IOException {
        doNothing().when(examService).update(exam);
        editExamBean.update();
        verify(externalContext).redirect("exam-evaluation.xhtml?exam-Id=" + exam.getId());
    }

    @Test
    void updateTestAllExeptions() throws EntityNotFoundException, OutdatedException, IOException {
        doThrow(new OutdatedException()).when(examService).update(exam);
        editExamBean.update();

        doThrow(new EntityNotFoundException()).when(examService).update(exam);
        editExamBean.update();

        verify(examService, times(2)).update(exam);
    }

    @Test
    void deleteExamTest() throws IOException, EntityNotFoundException, HasEvaluationsException {
        doNothing().when(examService).delete(exam);
        editExamBean.deleteExam();
        verify(externalContext).redirect("meeting.xhtml?meeting-Id=" + meeting.getId());
    }

    @Test
    void deleteAllExceptionsTest() throws EntityNotFoundException, HasEvaluationsException, IOException {
        doThrow(new HasEvaluationsException()).when(examService).delete(exam);
        editExamBean.deleteExam();

        doThrow(new EntityNotFoundException()).when(examService).delete(exam);
        editExamBean.deleteExam();

        verify(examService, times(2)).delete(exam);
    }




}