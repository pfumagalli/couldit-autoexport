package it.could.confluence.autoexport.actions;

import it.could.confluence.autoexport.engine.Notifiable;
import it.could.confluence.autoexport.ExportManager;
import org.apache.log4j.Logger;

import java.util.Date;

/**
     * <p>The {@link RebuildTask} class is used by the
     * {@link RebuildAction} to export {@link com.atlassian.confluence.spaces.Space}s in background.</p>
     */
public  final class RebuildTask implements Runnable, Notifiable
{
        /** <p>The {@link org.apache.log4j.Logger} of the class specified at construction.</p> */
        private final Logger log = Logger.getLogger(RebuildTask.class);
        /** <p>The {@link it.could.confluence.autoexport.ExportManager} used to export spaces.</p> */
        private final ExportManager exportManager;
        /** <p>The full log of this task.</p> */
        private final StringBuffer previousLog = new StringBuffer();
        /** <p>The data added to the full log.</p> */
        private final StringBuffer currentLog = new StringBuffer();
        /** <p>The list of {@link com.atlassian.confluence.spaces.Space}s to export.</p> */
        private final String spaceKeys[];
        /** <p>The names of the {@link com.atlassian.confluence.spaces.Space}s to export.</p> */
        private final String spaceNames[];
        /** <p>A flag indicating whether this task is running or not.</p> */
        private boolean started = false;
        /** <p>A flag indicating whether this task is running or not.</p> */
        private boolean running = true;

        /**
         * <p>Create a new {@link RebuildTask} instance.</p>
         */
        RebuildTask(String spaceKeys[], String spaceNames[],
                     ExportManager exportManager) {
            this.notify("Starting export task for the following spaces:");
            this.spaceKeys = spaceKeys;
            this.spaceNames = spaceNames;
            this.exportManager = exportManager;

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
                    this.currentLog.append("*** EXCEPTION *** ");
                    this.currentLog.append(object.getClass().getName());
                    this.currentLog.append(": ");
                    this.currentLog.append(((Throwable)object).getMessage());
                    this.log.warn("Exporting: Exception", (Throwable) object);
                } else {
                    this.currentLog.append("Exporting: " + object.toString());
                    this.log.info(object);
                }
                this.currentLog.append('\n');
            }
        }

        /**
         * <p>Export all the spaces specified at construction.</p>
         */
        public void run() {
            this.started = true;
            try {
                for (int x = 0; x < this.spaceKeys.length; x ++) {
                    this.notify(" - " + this.spaceNames[x] +  " [key=" +
                                this.spaceKeys[x] + "]");
                }
                this.exportManager.export(this.spaceKeys, this, true);

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

    public boolean isRunning()
    {
        return running;
    }

    public boolean isStarted()
    {
        return started;
    }
}