package signon.oswego.suny.signson;


public interface IGestureDetectModel {
    public void event(long eventTime, byte[] data);
    public void setAction(IGestureDetectAction action);
    public void action();
}
