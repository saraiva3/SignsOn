package example.naoki.SignOn;

import java.util.ArrayList;

public class GestureDetectMethod {
    //Change to add new Gestures
    private final static int COMPARE_NUM = 26;
    private final static int STREAM_DATA_LENGTH = 5;
    private final static Double THRESHOLD = 0.01;

    private final ArrayList<EmgData> compareGesture;

    private int streamCount = 0;
    private EmgData streamingMaxData;
    private Double detect_distance;
    private int detect_Num;

    private NumberSmoother numberSmoother = new NumberSmoother();

    public GestureDetectMethod(ArrayList<EmgData> gesture) {
        compareGesture = gesture;
    }

    public enum GestureState {
        Nothing,
        A, B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,X,Z,W,Y
    }

    private GestureState getEnum(int i_gesture) {

        switch (i_gesture) {
            case 0:
                return GestureState.A;
            case 1:
                return GestureState.B;
            case 2:
                return GestureState.C;
            case 3:
                return GestureState.D;
            case 4:
                return GestureState.E;
            case 5:
                return GestureState.F;
            case 6:
                return GestureState.G;
            case 7:
                return GestureState.H;
            case 8:
                return GestureState.I;
            case 9:
                return GestureState.J;
            case 10:
                return GestureState.K;
            case 11:
                return GestureState.L;
            case 12:
                return GestureState.M;
            case 13:
                return GestureState.N;
            case 14:
                return GestureState.O;
            case 15:
                return GestureState.P;
            case 16:
                return GestureState.Q;
            case 17:
                return GestureState.R;
            case 18:
                return GestureState.S;
            case 19:
                return GestureState.T;
            case 20:
                return GestureState.U;
            case 21:
                return GestureState.V;
            case 22:
                return GestureState.X;
            case 23:
                return GestureState.Z;
            case 24:
                return GestureState.W;
            case 25:
                return GestureState.Y;
            default:
                return GestureState.Nothing;
        }
    }

    public GestureState getDetectGesture(byte[] data) {
        EmgData streamData = new EmgData(new EmgCharacteristicData(data));
        streamCount++;
        if (streamCount == 1){
            streamingMaxData = streamData;
        } else {
            for (int i_element = 0; i_element < 8; i_element++) {
                if (streamData.getElement(i_element) > streamingMaxData.getElement(i_element)) {
                    streamingMaxData.setElement(i_element, streamData.getElement(i_element));
                }
            }
            if (streamCount == STREAM_DATA_LENGTH){
                detect_distance = getThreshold();
                detect_Num = -1;
                for (int i_gesture = 0;i_gesture < COMPARE_NUM ;i_gesture++) {
                    EmgData compData = compareGesture.get(i_gesture);
                    double distance = distanceCalculation(streamingMaxData, compData);
                    if (detect_distance > distance) {
                        detect_distance = distance;
                        detect_Num = i_gesture;
                    }
                }
                numberSmoother.addArray(detect_Num);
                streamCount = 0;
            }
        }
        return getEnum(numberSmoother.getSmoothingNumber());
    }

    private double getThreshold() {
        return THRESHOLD;
//        return 0.9;
    }

	// 2 vectors distance devied from each vectors norm.
    private double distanceCalculation(EmgData streamData, EmgData compareData){
        double return_val = streamData.getDistanceFrom(compareData)/streamData.getNorm()/compareData.getNorm();
        return return_val;
    }

	// Mathematical [sin] value of 2 vectors' inner angle.
    private double distanceCalculation_sin(EmgData streamData, EmgData compareData){
        double return_val = streamData.getInnerProductionTo(compareData)/streamData.getNorm()/compareData.getNorm();
        return return_val;
    }

	// Mathematical [cos] value of 2 vectors' inner angle from low of cosines.
    private double distanceCalculation_cos(EmgData streamData, EmgData compareData){
        double streamNorm  = streamData.getNorm();
        double compareNorm = compareData.getNorm();
        double distance    = streamData.getDistanceFrom(compareData);
        return (Math.pow(streamNorm,2.0)+Math.pow(compareNorm,2.0)-Math.pow(distance,2.0))/streamNorm/compareNorm/2;
    }


}
