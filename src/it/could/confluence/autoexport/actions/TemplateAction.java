/* ========================================================================== *
 *   Copyright (c) 2006, Pier Paolo Fumagalli <mailto:pier@betaversion.org>   *
 *                            All rights reserved.                            *
 * ========================================================================== *
 *                                                                            * 
 * Redistribution and use in source and binary forms, with or without modifi- *
 * cation, are permitted provided that the following conditions are met:      *
 *                                                                            * 
 *  - Redistributions of source code must retain the  above copyright notice, *
 *    this list of conditions and the following disclaimer.                   *
 *                                                                            * 
 *  - Redistributions  in binary  form  must  reproduce the  above  copyright *
 *    notice,  this list of conditions  and the following  disclaimer  in the *
 *    documentation and/or other materials provided with the distribution.    *
 *                                                                            * 
 *  - Neither the name of Pier Fumagalli, nor the names of other contributors *
 *    may be used to endorse  or promote products derived  from this software *
 *    without specific prior written permission.                              *
 *                                                                            * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" *
 * AND ANY EXPRESS OR IMPLIED WARRANTIES,  INCLUDING, BUT NOT LIMITED TO, THE *
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE *
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER  OR CONTRIBUTORS BE *
 * LIABLE  FOR ANY  DIRECT,  INDIRECT,  INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR *
 * CONSEQUENTIAL  DAMAGES  (INCLUDING,  BUT  NOT LIMITED  TO,  PROCUREMENT OF *
 * SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS;  OR BUSINESS *
 * INTERRUPTION)  HOWEVER CAUSED AND ON  ANY THEORY OF LIABILITY,  WHETHER IN *
 * CONTRACT,  STRICT LIABILITY,  OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) *
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                                *
 * ========================================================================== */
package it.could.confluence.autoexport.actions;

import it.could.confluence.ActionSupport;
import it.could.confluence.LocalizedException;
import it.could.confluence.autoexport.TemplatesManager;

import java.io.IOException;

import bucket.container.ContainerManager;

import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.spaces.SpaceManager;

/**
 * <p>An action managing the templates used by the AutoExport plugin.</p>
 */
public class TemplateAction extends ActionSupport
implements Administrative {

    /** <p>The {@link TemplatesManager} managing AutoExport templates.</p> */
    private final TemplatesManager templatesManager = TemplatesManager.INSTANCE;
    /** <p>The {@link SpaceManager} used to validate spaces.</p> */
    private SpaceManager spaceManager = null;
    /** <p>The current space key or <b>null</b> for all spaces.</p> */
    private String space = null;
    /** <p>The current template data.</p> */
    private String data = null;

    /**
     * <p>Create a new {@link TemplateAction} instance.</p>
     */
    public TemplateAction() {
        ContainerManager.autowireComponent(this);
    }

    /* ====================================================================== */
    /* BEAN SETTER METHODS FOR SPRING AUTO-WIRING                             */
    /* ====================================================================== */

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setSpaceManager(SpaceManager spaceManager) {
        this.spaceManager = spaceManager;
    }

    /* ====================================================================== */
    /* ACTION METHODS                                                         */
    /* ====================================================================== */

    /**
     * <p>Read and display the default AutoExport template.</p>
     */
    public String execute() {
        try {
            this.data = this.templatesManager.readDefaultTemplate();
        } catch (LocalizedException exception) {
            this.log.warn(exception.getMessage(), exception);
            this.addActionError(exception.getMessage());
        }
        return SUCCESS;
    }

    /**
     * <p>Read and display the current template.</p>
     */
    public String read() {
        try {
            this.data = this.templatesManager.readCustomTemplate(this.space);
            if (this.data == null) {
                this.data = this.templatesManager.readDefaultTemplate();
            }
        } catch (LocalizedException exception) {
            this.log.warn(exception.getMessage(), exception);
            this.addActionError(exception.getMessage());
        } 
        return SUCCESS;
    }

    /**
     * <p>Save the current template.</p>
     */
    public String save()
    throws IOException {
        if (this.data == null) {
            this.addActionError(this.getText("err.nodata"));
        } else try {
            this.templatesManager.writeCustomTemplate(this.space, this.data);
            this.data = this.templatesManager.readCustomTemplate(this.space);
            this.addActionMessage(this.getText("msg.saved"));
        } catch (LocalizedException exception) {
            this.log.warn(exception.getMessage(), exception);
            this.addActionError(exception.getMessage());
        }
        return SUCCESS;
    }

    /**
     * <p>Delete the current template and restore the default one.</p>
     */
    public String restore()
    throws IOException {
        if (this.templatesManager.removeCustomTemplate(this.space)) {
            this.addActionMessage(this.getText("msg.deleted"));
        } else {
            this.addActionMessage(this.getText("msg.nodelete"));
        }
        return SUCCESS;
    }
    
    /* ====================================================================== */
    /* SETTERS AND GETTERS FOR PARAMETER VALUES                               */
    /* ====================================================================== */

    /**
     * <p>Parameter value setter.</p>
     */
    public void setSpace(String space) {
        if ("".equals(space)) space = null;
        this.space = space;
    }
    
    /**
     * <p>Parameter value getter.</p>
     */
    public String getSpace() {
        if (this.space == null) return "";
        return this.space;
    }

    /**
     * <p>Parameter value setter.</p>
     */
    public void setData(String data) {
        if ("".equals(data)) data = "";
        this.data = data;
    }

    /**
     * <p>Parameter value getter.</p>
     */
    public String getData() {
        if (this.data == null) return "";
        return this.data;
    }

    /* ====================================================================== */
    /* OTHER TEMPLATE METHODS                                                 */
    /* ====================================================================== */

    /**
     * <p>Return the name of the current space.</p>
     */
    public String getSpaceName() {
        try {
            return this.spaceManager.getSpace(this.space).getName();
        } catch (Exception exception) {
            return null;
        }
    }
}
