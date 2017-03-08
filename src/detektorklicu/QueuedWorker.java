/*
 * The MIT License
 *
 * Copyright 2017 zikmuto2.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package detektorklicu;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

/**
 *
 * @author zikmuto2
 */
public class QueuedWorker{
    
    Queue<TaskIface> queue = new ConcurrentLinkedQueue<>();
    String progressName = "backgroundOperation";
    int progressValue = 0;
    ExceptionMessage exceptionMessage;
    PropertyChangeSupport changes = new PropertyChangeSupport(this);
    SwingWorker<Void, Void> worker;
    
    void addPropertyChangeListener(PropertyChangeListener pcl){
        changes.addPropertyChangeListener(pcl);
    }
    
    protected void startNext() {
        if(worker != null && worker.getState() != StateValue.DONE) return;
        if(hasDone()) return;
        TaskIface task = queue.poll();
        worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    task.action();
                }catch(ExceptionMessage e){
                    setExceptionMessage(e);
                }
                return null;
            }
        };
        worker.addPropertyChangeListener(e->{
            changes.firePropertyChange(e);
            if("state".equals(e.getPropertyName()) && e.getNewValue() == StateValue.DONE) {
                done();
            }
        });
        worker.execute();
    }
    
    void cancelJob(){
        if(worker != null) worker.cancel(true);
    }
    
    protected void done(){
        execute();
    }
    
    boolean hasDone() {
        return queue.isEmpty();
    }
    
    void execute(){
        if(hasDone()) return;
        if(worker != null && worker.getState() == StateValue.PENDING) 
            worker.execute();
        startNext();
        
        setProgressName("taskStarted");
        setProgressVal(0);
    }
    
    void runInBackground(TaskIface task) {
        queue.add(task);
        execute();
    }
    
    void setProgressVal(int i){
        changes.firePropertyChange("progressValue", progressValue, i);
        progressValue = i;
    }
    int getProgressVal(){return progressValue;}
    void setProgressName(String s){
        changes.firePropertyChange("progressName", progressName, s);
        progressName = s;
    }
    String getProgressText(){
        return getProgressText(progressName);
    }
    String getProgressText(String textBundle){
        try{
            return ResourceBundle.getBundle("texts/QueuedWorker").getString(textBundle);
        }catch(MissingResourceException ex){
            return progressName;
        }
    }
    protected void setExceptionMessage(ExceptionMessage e){
        changes.firePropertyChange("exceptionMessage", exceptionMessage, e);
        exceptionMessage = e;
    }
            
    
    static abstract interface TaskIface{
        public abstract void action() throws ExceptionMessage;
    }
    
    static class Progress{
        QueuedWorker worker;
        void setWorker(QueuedWorker worker){this.worker = worker;}
        void setValue(int i){if(worker != null) worker.setProgressVal(i);}
        void setName(String s){
            if(worker != null){
                worker.setProgressName(s);
                worker.setProgressVal(0);
            }
        }
    }
}
