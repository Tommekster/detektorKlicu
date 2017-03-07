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

import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author zikmuto2
 */
public class QueuedWorker extends SwingWorker<Void, String>{
    
    Queue<ActionIface> queue = new ConcurrentLinkedQueue<>();
    String progressName = "backgroundOperation";
    int progressValue = 0;
    ExceptionMessage exceptionMessage;

    @Override
    protected Void doInBackground() throws Exception {
        if(!queue.isEmpty()){
            ActionIface action = queue.poll();
            try{
                action.action();
            }catch(ExceptionMessage e){
                setExceptionMessage(e);
            }
        }
        return null;
    }
    
    void cancelJob(){
        cancel(true);
    }
    
    @Override
    protected void done(){
        if(!queue.isEmpty()) execute();
    }
    
    boolean hasDone() {
        return queue.isEmpty();
    }
    
    void runInBackground(ActionIface action) {
        queue.add(action);
        if(getState() != StateValue.STARTED) execute();
        //if(getState() == StateValue.DONE) stat
    }
    
    void setProgressVal(int i){
        firePropertyChange("progressValue", progressValue, i);
        progressValue = i;
    }
    int getProgressVal(){return progressValue;}
    void setProgressName(String s){
        firePropertyChange("progressName", progressName, s);
        progressName = s;
    }
    String getProgressText(){
        try{
            return ResourceBundle.getBundle("texts/QueuedWorker").getString(progressName);
        }catch(MissingResourceException ex){
            return progressName;
        }
    }
    protected void setExceptionMessage(ExceptionMessage e){
        firePropertyChange("exceptionMessage", exceptionMessage, e);
        exceptionMessage = e;
    }
            
    
    static abstract interface ActionIface{
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
