package de.unibremen.swp2.controller;


import de.unibremen.swp2.service.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginBeanTest
{

    @Mock
    SecurityContext securityContext;

    @Mock
    ExternalContext externalContext;

    @Mock
    FacesContext facesContext;

    @Mock
    SessionService sessionService;

    @InjectMocks
    LoginBean loginBean;

    /**
     * If {@link LoginBean#login()} is called explicitly (i.e., no login to
     * continue) with invalid credentials, only a faces message should be added
     * to {@link FacesContext}. In particular, no redirection should be
     * registered ({@link ExternalContext#redirect(String)}) and
     * {@link SessionService#loggedIn(String, HttpSession)} should not be
     * called.
     */
    @Test
    public void explicitCallFailure() throws IOException {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(externalContext.getRequest()).thenReturn(request);
        when(externalContext.getResponse()).thenReturn(response);
        when(securityContext.authenticate(any(), any(), any()))
                .thenReturn(AuthenticationStatus.SEND_FAILURE);

        // When
        loginBean.login();

        // Then
        //verify that the method getRequest() is been only called for one time
        verify(externalContext, times(1)).getRequest();
        verify(externalContext, times(1)).getResponse();
        verifyNoMoreInteractions(externalContext);
        verify(securityContext, times(1)).authenticate(
                eq(request), eq(response), notNull());
        verifyNoInteractions(sessionService);
        verify(facesContext, times(1)).addMessage(isNull(), notNull());
    }
}
