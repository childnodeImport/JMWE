package com.innovalog.jmwe.plugins.functions;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserManager;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.googlecode.jsu.util.WorkflowUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

/**
 *
 */
public class SetAssigneeFromFieldFunction extends AbstractJiraFunctionProvider {
	private static final Logger log = Logger.getLogger(SetAssigneeFromFieldFunction.class);
    private final WorkflowUtils workflowUtils;
    private final UserManager userManager;

    public SetAssigneeFromFieldFunction(WorkflowUtils workflowUtils, UserManager userManager) {
        this.workflowUtils = workflowUtils;
        this.userManager = userManager;
    }

    @SuppressWarnings("unchecked")
	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		log.debug("");
		String sourceFieldKey = (String) args.get(WorkflowSetAssigneeFromFieldFunction.FIELD);
		Field fieldFrom = (Field) workflowUtils.getFieldFromKey(sourceFieldKey);
		if (fieldFrom == null) {
			log.warn(String.format("Unable to find field with key [%s]", sourceFieldKey));
			return;
		}

		try {
			MutableIssue issue = getIssue(transientVars);
			Object sourceValue = workflowUtils.getFieldValueFromIssue(issue, fieldFrom);
			if (sourceValue == null) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Value of field [%s] is null, setting Assignee to Unassigned on issue [%s]", fieldFrom.getName(), issue.getKey()));
				}
				issue.setAssigneeId(null);
			} else {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Value of field [%s] is [%s], setting Assignee to user [%s] on issue [%s]", fieldFrom.getName(), sourceValue.toString(), sourceValue.toString(), issue.getKey()));
				}
				User user = userManager.getUser(sourceValue.toString());
				if (user == null) {
					if (log.isDebugEnabled()) {
						log.warn(String.format("Unable to find user [%s]", sourceValue.toString()));
					}
					return;
				}
				issue.setAssignee(user);
			}

			if (log.isDebugEnabled()) {
				log.debug("Value was successfully copied");
			}
		} catch (Exception e) {
			final String message = "Unable to copy value from " + fieldFrom.getName() + " to Assignee";

			log.error(message, e);

			throw new WorkflowException(message);
		}
	}
}
