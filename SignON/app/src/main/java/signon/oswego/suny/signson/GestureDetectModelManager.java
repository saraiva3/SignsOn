package signon.oswego.suny.signson;


import signon.oswego.suny.signson.IGestureDetectModel;
import signon.oswego.suny.signson.NopModel;

public class GestureDetectModelManager {
    private static final Object LOCK = new Object();
    private static IGestureDetectModel currentModel = new NopModel();

    public static IGestureDetectModel getCurrentModel(){
        synchronized (LOCK) {
            return currentModel;
        }
    }

    public static void setCurrentModel(IGestureDetectModel model){
        synchronized (LOCK) {
            currentModel = model;
        }
    }

}
