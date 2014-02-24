/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.console.client.shared.patching.wizard;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.domain.model.SimpleCallback;
import org.jboss.as.console.client.shared.patching.PatchInfo;
import org.jboss.as.console.client.shared.patching.PatchManager;
import org.jboss.as.console.client.shared.patching.Patches;

/**
 * @author Harald Pehl
 */
public class ApplyingPatchStep extends WizardStep {

    private final PatchManager patchManager;
    private HandlerRegistration handlerRegistration;

    public ApplyingPatchStep(final ApplyPatchWizard wizard, PatchManager patchManager) {
        super(wizard, Console.CONSTANTS.patch_manager_applying_patch_title());
        this.patchManager = patchManager;
    }

    @Override
    protected IsWidget body() {
        return new Pending(Console.CONSTANTS.patch_manager_applying_patch_body());
    }

    @Override
    void onShow(final WizardContext context) {
        // reset old state
        context.restartToUpdate = true;
        context.patchInfo = PatchInfo.NO_PATCH;
        context.conflict = false;
        context.patchFailed = false;
        context.patchFailedDetails = null;
        context.overrideConflict = false;

        if (handlerRegistration == null) {
            handlerRegistration = wizard.context.form.addSubmitCompleteHandler(new PatchAppliedHandler());
        }
    }

    class PatchAppliedHandler implements FormPanel.SubmitCompleteHandler {

        @Override
        public void onSubmitComplete(final FormPanel.SubmitCompleteEvent event) {
            String html = event.getResults();
            String json = html;
            try {
                if (!GWT.isScript()) {
                    // Formpanel weirdness
                    json = html.substring(html.indexOf(">") + 1, html.lastIndexOf("<"));
                }
            } catch (StringIndexOutOfBoundsException e) {
                // if I get this exception it means I shouldn't strip out the html
                // this issue still needs more research
                Log.debug("Failed to strip out HTML.  This should be preferred?");
            }
            JSONObject response = JSONParser.parseLenient(json).isObject();
            JSONString outcome = response.get("outcome").isString();
            if (outcome != null && "success".equalsIgnoreCase(outcome.stringValue())) {
                patchManager.getPatches(new SimpleCallback<Patches>() {
                    @Override
                    public void onSuccess(final Patches result) {
                        wizard.context.patchInfo = result.getLatest();
                        wizard.next();
                    }
                });
            } else {
                wizard.context.patchFailedDetails = stringify(response.getJavaScriptObject(), 2);
                if (wizard.context.patchFailedDetails.contains("conflicts")) {
                    wizard.context.conflict = true;
                } else {
                    wizard.context.patchFailed = true;
                }
                wizard.next();
            }
        }

        private native String stringify(JavaScriptObject json, int indent) /*-{
            return JSON.stringify(json, null, indent);
        }-*/;
    }
}
