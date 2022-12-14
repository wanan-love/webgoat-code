package com.wanan.webgoat.container.service;

import com.wanan.webgoat.container.i18n.Messages;
import com.wanan.webgoat.container.session.WebSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class SessionService {
    private final WebSession webSession;
    private final RestartLessonService restartLessonService;
    private final Messages messages;
    @RequestMapping(path = "/service/enable-security.mvc",produces = "application/json")
    @ResponseBody
    public String applySecurity(){
        webSession.toggleSecurity();
        restartLessonService.restartLesson();

        var msg = webSession.isSecurityEnable() ? "security.enabled":"security.disabled";
        return messages.getMessage(msg);
    }
}
