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

import it.could.confluence.autoexport.ActionSupport;
import it.could.confluence.autoexport.ExportManager;
import it.could.confluence.autoexport.SingletonManager;
import it.could.confluence.autoexport.engine.Notifiable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import bucket.container.ContainerManager;

import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;

/**
 * <p>An action dealing with manual export operations with the AutoExport
 * plugin.</p>
 */
public class RebuildAction extends ActionSupport implements Administrative {

    /** <p>The singleton {@link Task} instance (only one running).</p> */
    private static Task TASK_INSTANCE = null;

    /** <p>The {@link SpaceManager} used to validate spaces.</p> */
    private SpaceManager spaceManager = null;
    /** <p>The current {@link Task} instance.</p> */
    private Task executor = TASK_INSTANCE;
    /** <p>The array of {@link Space}s to export manually.</p> */
    private String spaces[] = null;
    /** <p>The current logging data to show in the template.</p> */
    private String data = null;

    /**
     * <p>Create a new {@link RebuildAction} instance.</p>
     */
    public RebuildAction() {
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
     * <p>Execute a manual export operation, creating a new {@link Task}
     * instance and running it.</p>
     */
    public String execute() {
        /* What to do if we already have an executor in the session? */
        if (this.executor != null) {
            /* If the executor is running, just fail */
            if (this.executor.running) {
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
            TASK_INSTANCE = this.executor = new Task(this.spaces, names);
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
        } if (this.executor.started) {
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
        List list = new ArrayList();
        for (int x = 0; x < spaces.length; x++) {
            if ("*".equals(spaces[x])) {
                list = this.spaceManager.getSpaces();
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
        if ((this.executor != null) && (! this.executor.running))
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

    /**
     * <p>The {@link RebuildAction.Task Task} class is used by the
     * {@link RebuildAction} to export {@link Space}s in background.</p>
     */
    public static final class Task implements Runnable, Notifiable {
        /** <p>The full log of this task.</p> */
        private final StringBuffer previousLog = new StringBuffer();
        /** <p>The data added to the full log.</p> */
        private final StringBuffer currentLog = new StringBuffer();
        /** <p>The list of {@link Space}s to export.</p> */
        private final String spaceKeys[];
        /** <p>The names of the {@link Space}s to export.</p> */
        private final String spaceNames[];
        /** <p>A flag indicating whether this task is running or not.</p> */
        private boolean started = false;
        /** <p>A flag indicating whether this task is running or not.</p> */
        private boolean running = true;
        
        /**
         * <p>Create a new {@link Task} instance.</p>
         */
        private Task(String spaceKeys[], String spaceNames[]) {
            this.notify("Starting export task for the following spaces:");
            this.spaceKeys = spaceKeys;
            this.spaceNames = spaceNames;
            
            /* TODO: This will throw a Hibernate Exception */
            //new Thread(new ThreadGroup("wa"), this, "wa").start();
        }

        /**
         * <p>Record an object as a log message.</p>
         */
        public void notify(Object object) {
            if (object == null) return;
            synchronized (this.currentLog) {
                this.currentLog.append('[');
                this.currentLog.append(new Date());
                this.currentLog.append("] ");
                if (object instanceof Throwable) {
                    final StringWriter writer = new StringWriter();
                    final PrintWriter printer = new PrintWriter(writer);
                    ((Throwable) object).printStackTrace(printer);
                    printer.flush();
                    printer.close();
                    writer.flush();
                    this.currentLog.append(writer.getBuffer());
                } else {
                    this.currentLog.append(object.toString());
                }
                this.currentLog.append('\n');
            }
        }

        /**
         * <p>Export all the spaces specified at construction.</p>
         */
        public void run() {
            final ExportManager exporter = SingletonManager.getExportManager();
            this.started = true;
            try {
                for (int x = 0; x < this.spaceKeys.length; x ++) {
                    this.notify(" - " + this.spaceNames[x] +  " [key=" +
                                this.spaceKeys[x] + "]");
                }
                exporter.export(this.spaceKeys, this, true);

            } catch (Throwable throwable) {
                this.notify(throwable);
            } finally {
                this.notify("Finished");
                this.running = false;
            }
        }

        /**
         * <p>Return the full log of the operation up to this point.</p>
         */
        public String getPreviousLog() {
            synchronized (this.previousLog) {
                return this.previousLog.toString();
            }
        }

        /**
         * <p>Return the additions to the current full log and flush them.</p>
         */
        public String getCurrentLog() {
            synchronized (this.currentLog) {
                StringBuffer buffer = new StringBuffer();
                for (int x = 0; x < this.currentLog.length(); x ++) {
                    char character = this.currentLog.charAt(x);
                    switch (character) {
                        case '\"': buffer.append("\\\""); break;
                        case '\'': buffer.append("\\\'"); break;
                        case '\n': buffer.append("<br>");  break;
                        default:   buffer.append(character);
                    }
                }
                this.currentLog.setLength(0);
                synchronized (this.previousLog) {
                    this.previousLog.append(buffer);
                }
                return buffer.toString();
            }
        }
    }
}
