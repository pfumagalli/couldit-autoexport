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

import it.could.confluence.autoexport.ExportManager;
import it.could.confluence.localization.LocalizedAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.spaces.SpaceType;

/**
 * <p>An action dealing with manual export operations with the AutoExport
 * plugin.</p>
 */
public class RebuildAction extends LocalizedAction implements Administrative {

    /** <p>The singleton {@link RebuildTask} instance (only one running).</p> */
    private static RebuildTask TASK_INSTANCE = null;

    private ExportManager exportManager;
    private SpaceManager spaceManager;
    private RebuildTask executor = TASK_INSTANCE;

    /** <p>The array of {@link Space}s to export manually.</p> */
    private String spaces[] = null;
    /** <p>The current logging data to show in the template.</p> */
    private String data = null;
    private static final Log log = LogFactory.getLog(RebuildAction.class);


    /* ====================================================================== */
    /* ACTION METHODS                                                         */
    /* ====================================================================== */

    /**
     * <p>Execute a manual export operation, creating a new {@link RebuildTask}
     * instance and running it.</p>
     */
    public String execute() {
        /* Process and normalize the list of spaces */
        final List list = new ArrayList();
        for (int x = 0; x < spaces.length; x++) {
            if ("*".equals(spaces[x])) {
                list.addAll(this.spaceManager.getSpacesByType(SpaceType.GLOBAL));
                list.addAll(this.spaceManager.getSpacesByType(SpaceType.PERSONAL));
                break;
            } else try {
                Space space = this.spaceManager.getSpace(spaces[x]);
                if (space == null) continue;
                if (list.contains(space)) continue;
                list.add(space);
            } catch (Exception exception) {
                this.log.warn("Can't resolve space " + spaces[x], exception);
            }
        }

        if (list.size() != 0) {
            final String spaceKeys[] = new String[list.size()];
            final Iterator iterator = list.iterator();
            for (int x = 0; iterator.hasNext(); x ++) {
                spaceKeys[x] = ((Space)iterator.next()).getKey();
            }

            this.spaces = spaceKeys;
        }

        
        /* What to do if we already have an executor in the session? */
        if (this.executor != null) {
            /* If the executor is running, just fail */
            if (this.executor.isRunning()) {
                this.addActionError(this.getText("err.running"));
            
            /* The executor is not running, this is the last invocation */
            } else {
                this.addActionMessage(this.getText("msg.completed"));
                this.executor.getCurrentLog();
                TASK_INSTANCE = null;
            }

        /* As we have no executor, check if we were given some spaces */
        } else if ((this.spaces == null) || (this.spaces.length == 0)) {
            this.addActionError(this.getText("err.nospaces"));
            this.data = "<br>"; /* Empty log */
            return SUCCESS;

        /* Finally, we now have no executor and a list of spaces */
        } else {
            this.addActionMessage(this.getText("msg.started"));
            final String names[] = new String[this.spaces.length];
            for (int x = 0; x < names.length; x++) {
                final Space space = this.spaceManager.getSpace(this.spaces[x]);
                if (space != null) names[x] = space.getName();
            }
            TASK_INSTANCE = this.executor = new RebuildTask(this.spaces, names,
                                                     this.exportManager);
            this.executor.getCurrentLog();
        }

        /* The data returned here is always the previous log */
        this.data = this.executor.getPreviousLog(); 
        return SUCCESS;
    }

    /**
     * <p>Start the main export operation (loaded by an hidden iframe).</p>
     * 
     * <p>This is a nasty hack but if we execute the export task into a new
     * thread, Confluence throws a Hibernate exception saying <i>Could not
     * initialize proxy - the owning Session was closed</i>.</p>
     */
    public String start() {
        if (this.executor == null) {
            this.addActionError("Executor task does not exist");
        } if (this.executor.isStarted()) {
            this.addActionError("Executor task already started");
        } else {
            this.log.info("Starting executor");
            this.executor.run();
        }
        return SUCCESS;
    }

    /**
     * <p>Prepare a log-update request.</p>
     */
    public String update() {
        /* The data returned here is always the current log (the diff) */
        if (this.executor != null) this.data = this.executor.getCurrentLog();
        return SUCCESS;
    }

    /* ====================================================================== */
    /* SETTERS AND GETTERS FOR PARAMETER VALUES                               */
    /* ====================================================================== */

    /**
     * <p>Parameter value setter.</p>
     */
    public void setSpaces(String spaces[]) {
        this.spaces = spaces;
    }

    /* ====================================================================== */
    /* OTHER TEMPLATE METHODS                                                 */
    /* ====================================================================== */

    /**
     * <p>Return whether the {@link #getData()} call will return something
     * suitable for updates.</p>
     */
    public boolean hasData() {
        /* If the Executor is not running, remove it from the session */
        if ((this.executor != null) && (! this.executor.isRunning()))
            TASK_INSTANCE = null;

        /* Return wether we had an executor at the time we were created */
        return this.executor != null;
    }

    /**
     * <p>Return the log data or the log update data.</p>
     */
    public String getData() {
        /* Return the data from the executor */
        return this.data == null ? "" : this.data;
    }

    /* ====================================================================== */
    /* INNER CLASSES                                                          */
    /* ====================================================================== */


    public void setExportManager(ExportManager exportManager)
    {
        this.exportManager = exportManager;
    }

    public void setSpaceManager(SpaceManager spaceManager)
    {
        this.spaceManager = spaceManager;
    }
}
