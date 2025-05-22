package com.davidrandoll.automation.engine.todo_example.automation_flow.actions.send_email;

import com.davidrandoll.automation.engine.core.actions.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SendEmailActionContext implements IActionContext {
    private String alias;
    private String to;
    private String subject;
    private String body;
}
