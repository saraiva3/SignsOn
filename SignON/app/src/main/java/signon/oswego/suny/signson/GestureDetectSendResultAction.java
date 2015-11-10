package signon.oswego.suny.signson;

import com.myo.EMGHack.MainActivity;

import signon.oswego.suny.signson.IGestureDetectAction;

public class GestureDetectSendResultAction implements IGestureDetectAction {
    MainActivity activity;

    public GestureDetectSendResultAction(MainActivity mainActivity){
        activity = mainActivity;
    }
    // Here I can change the screen once that is saved
    @Override
    public void action(String Tag ) {
        switch (Tag) {
            case "SAVE":
                activity.setGestureText("Teach Me Another");
                activity.startNopModel();
                break;
            case "SAVED":
                activity.setGestureText("Detect Ready");
                activity.startNopModel();
                break;
            default:
                activity.setGestureText(Tag);
                break;
        }
    }
}
