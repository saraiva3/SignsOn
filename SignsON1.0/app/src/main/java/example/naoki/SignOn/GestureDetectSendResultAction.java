package example.naoki.SignOn;


public class GestureDetectSendResultAction implements IGestureDetectAction {
    MainActivity activity;

    public GestureDetectSendResultAction(MainActivity mainActivity){
        activity = mainActivity;
    }

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
