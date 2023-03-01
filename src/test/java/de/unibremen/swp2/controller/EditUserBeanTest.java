package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.User;
import de.unibremen.swp2.persistence.Exceptions.DuplicateEmailException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.UserNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.UserNotPermittedException;
import de.unibremen.swp2.service.UserService;
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
class EditUserBeanTest {
    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @InjectMocks
    EditUserBean editUserBean;

    @Mock
    UserService userService;

    @Mock
    User user;

    @Mock
    @RequestParameterMap
    private Map<String, String> parameterMap;

    @Test
    void updateTest() throws IOException, UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException {

        doNothing().when(userService).update(any());
        doNothing().when(externalContext).redirect(any());
        editUserBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(externalContext, times(1)).redirect("users.xhtml");
        verifyNoMoreInteractions(externalContext);
        verifyNoInteractions(facesContext);


    }


    @Test
    public void UserNotFoundExceptionTest() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new UserNotFoundException()).when(userService).update(any());
        editUserBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);


    }

    @Test
    public void OutdatedExceptionTest() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new OutdatedException()).when(userService).update(any());
        editUserBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);


    }
    @Test
    public void DuplicateEmailExceptionTest() throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        doThrow(new DuplicateEmailException()).when(userService).update(any());
        editUserBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);


    }

    @Test
    public void UserNotPermittedExceptionTest () throws UserNotFoundException, OutdatedException, UserNotPermittedException, DuplicateEmailException, IOException {

        //doThrow(new UserNotPermittedException()  ).when(userService).update(any());
        //editUserBean.update();
        //Exception e = spy(new Exception());

        //verify(e).printStackTrace();
        doThrow(new UserNotPermittedException()).when(userService).update(any());
        editUserBean.update();
        verify(userService, times(1)).update(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void resetPwTest() throws UserNotFoundException, OutdatedException, DuplicateEmailException {

        doNothing().when(userService).resetPw(any());
        editUserBean.resetPassword();
        verify(userService, times(1)).resetPw(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
    }

    @Test
    public void resetPwUserNotFoundExceptionnTest () throws UserNotFoundException, OutdatedException, DuplicateEmailException {

        //doThrow(new UserNotPermittedException()  ).when(userService).update(any());
        //editUserBean.update();
        //Exception e = spy(new Exception());

        //verify(e).printStackTrace();
        doThrow(new UserNotFoundException()).when(userService).resetPw(any());
        editUserBean.resetPassword();
        verify(userService, times(1)).resetPw(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void resetPwOutdatedExceptionTest () throws UserNotFoundException, OutdatedException, DuplicateEmailException {

        //doThrow(new UserNotPermittedException()  ).when(userService).update(any());
        //editUserBean.update();
        //Exception e = spy(new Exception());

        //verify(e).printStackTrace();
        doThrow(new OutdatedException()).when(userService).resetPw(any());
        editUserBean.resetPassword();
        verify(userService, times(1)).resetPw(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }

    @Test
    public void resetPwDuplicateEmailExceptionTest () throws UserNotFoundException, OutdatedException, DuplicateEmailException {

        //doThrow(new UserNotPermittedException()  ).when(userService).update(any());
        //editUserBean.update();
        //Exception e = spy(new Exception());

        //verify(e).printStackTrace();
        doThrow(new DuplicateEmailException()).when(userService).resetPw(any());
        editUserBean.resetPassword();
        verify(userService, times(1)).resetPw(any());
        verifyNoMoreInteractions(userService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
        verifyNoMoreInteractions(facesContext);
    }


    @Test
    void initTest () {

        assertNotNull(editUserBean.getUser());
        parameterMap.put("user-Id", null);
        new Mirror().on(editUserBean).invoke().method("init").withoutArgs();
        assertNull(userService.getById(parameterMap.get("user-Id")));
        parameterMap.put("user-Id", user.getId());
        assertNull(userService.getById(parameterMap.get("meeting-Id")));

    }


    @Test
    void initTest2()
    {
        User u = new User();
        String  value= "819cb31c-3b38-4706-b3d5-b7235e59df99";
        when(parameterMap.get(anyString())).thenReturn(value);
        when(userService.getById(any())).thenReturn(u);
        new Mirror().on(editUserBean).invoke().method("init").withoutArgs();

    }


}