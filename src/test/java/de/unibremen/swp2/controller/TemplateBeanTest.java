package de.unibremen.swp2.controller;

import de.unibremen.swp2.model.User;
import de.unibremen.swp2.service.SessionService;
import net.vidageek.mirror.dsl.Mirror;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import java.io.IOException;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateBeanTest
{
    @Mock
    Principal principal;
    @Mock
    SessionService sessionService;
    @Mock
    ExternalContext externalContext;
    @InjectMocks
    TemplateBean templateBean;


    @Test
    void Testlogout() throws IOException
    {
        doNothing().when(sessionService).logout(any());
        doNothing().when(externalContext).redirect(any());
        new Mirror().on(templateBean).invoke().method("logout").withoutArgs();
        verify(sessionService, times(1)).logout(any());
        verifyNoMoreInteractions(sessionService);
        verify(externalContext, times(1)).redirect("login.xhtml");
        verifyNoMoreInteractions(externalContext);

    }
}