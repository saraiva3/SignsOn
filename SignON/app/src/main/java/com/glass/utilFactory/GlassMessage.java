package com.glass.utilFactory;

import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.Proto;
import com.google.googlex.glass.common.proto.TimelineNano;

import java.util.ArrayList;
import java.util.List;


import java.util.ArrayList;
import java.util.List;

import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.Proto.Envelope;
import com.google.glass.companion.Proto.MotionEvent;
import com.google.glass.companion.Proto.MotionEvent.PointerCoords;
import com.google.glass.companion.Proto.MotionEvent.PointerProperties;
import com.google.googlex.glass.common.proto.TimelineNano;
import com.google.googlex.glass.common.proto.TimelineNano.SourceType;
import com.google.googlex.glass.common.proto.TimelineNano.TimelineItem;

public class GlassMessage {

    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_UP = 1;

    public static Proto.MotionEvent convertMouseEvent2MotionEvent(int action, float x, float y, long downTime) {
        Proto.MotionEvent me = new Proto.MotionEvent();
        me.downTime = downTime;
        me.eventTime = System.currentTimeMillis();
        me.action = action;
        me.metaState = 0;
        me.buttonState = 0;
        me.pointerCount = 1;
        me.xPrecision = 1.0007813f;
        me.yPrecision = 1.0013889f;
        me.deviceId = 1;
        me.edgeFlags = 0;
        me.source = 4098;
        me.flags = 0;
        Proto.MotionEvent.PointerProperties prop = new Proto.MotionEvent.PointerProperties();
        prop.toolType = 1;
        Proto.MotionEvent.PointerCoords point = new Proto.MotionEvent.PointerCoords();
        point.orientation = 1.5f;
        point.pressure = 1.0f;
        point.size = 0.045f;
        point.toolMajor = 9.0f;
        point.toolMinor = 9.0f;
        point.touchMajor = 10.0f;
        point.touchMinor = 9.0f;
        point.x = normalize(x);
        point.y = normalize(y);
        me.pointerProperties = new Proto.MotionEvent.PointerProperties[] { prop };
        me.pointerCoords = new Proto.MotionEvent.PointerCoords[] { point };
        return me;
    }

    private static float normalize(float f) {
        if (f < 0.0F) {
            return 0.001F;
        } else if (f > 100.0F) {
            return 99.999F;
        } else {
            return f;
        }
    }

    public static final Proto.Envelope newMotionEventEnvelope(Proto.MotionEvent e) {
        Proto.Envelope envelope = CompanionMessagingUtil.newEnvelope();
        envelope.motionC2G = e;
        return envelope;
    }

    private static final int SWIPE_STEP_COUNT = 2;
    private static final long SWIPE_DURATION = 700;
    private static final long SWIPE_STEP_DURATION = (long) ((float) SWIPE_DURATION / (float) SWIPE_STEP_COUNT);

    public static final List<Proto.Envelope> getSwipeEvents(float startX, float startY, float endX, float endY) {
        List<Proto.Envelope> res = new ArrayList<Proto.Envelope>();
        float x = startX;
        float y = startY;
        float stepX = (endX - startX) / (float) SWIPE_STEP_COUNT;
        float stepY = (endY - startY) / (float) SWIPE_STEP_COUNT;

        long downTime = System.currentTimeMillis() - SWIPE_DURATION;
        long eventTime = downTime;
        Proto.MotionEvent downEvent = convertMouseEvent2MotionEvent(ACTION_DOWN, x, y, downTime);
        res.add(newMotionEventEnvelope(downEvent));
        for (int i = 0; i < SWIPE_STEP_COUNT - 1; i++) {
            x += stepX;
            y += stepY;
            eventTime += SWIPE_STEP_DURATION;
            Proto.MotionEvent moveEvent = convertMouseEvent2MotionEvent(ACTION_MOVE, x, y, downTime);
            moveEvent.eventTime = eventTime;
            res.add(newMotionEventEnvelope(moveEvent));
        }
        x += stepX;
        y += stepY;
        eventTime += SWIPE_STEP_DURATION;
        Proto.MotionEvent upEvent = convertMouseEvent2MotionEvent(ACTION_UP, x, y, downTime);
        upEvent.eventTime = eventTime;
        res.add(newMotionEventEnvelope(upEvent));
        return res;
    }

    public static final List<Proto.Envelope> getSwipeDownEvents() {
        return getSwipeEvents(33.3F, 0.001F, 33.3F, 99.999F);
    }

    public static final List<Proto.Envelope> getSwipeLeftEvents() {
        return getSwipeEvents(30.000F, 50.0F, 50.000F, 50.0F);
    }

    public static final List<Proto.Envelope> getSwipeRightEvents() {
        return getSwipeEvents(50.000F, 50.0F, 30.000F, 50.0F);
    }

    public static final List<Proto.Envelope> getTapEvents() {
        List<Proto.Envelope> res = new ArrayList<Proto.Envelope>();
        float x = 33.3F;
        float y = 50.0F;
        long downTime = System.currentTimeMillis();
        Proto.MotionEvent downEvent = convertMouseEvent2MotionEvent(ACTION_DOWN, x, y, downTime);
        res.add(newMotionEventEnvelope(downEvent));
        Proto.MotionEvent upEvent = convertMouseEvent2MotionEvent(ACTION_UP, x, y, downTime);
        res.add(newMotionEventEnvelope(upEvent));
        return res;
    }

    public static Proto.Envelope createTimelineMessage(String text) {
        long now = System.currentTimeMillis();
        Proto.Envelope envelope = CompanionMessagingUtil.newEnvelope();
        TimelineNano.TimelineItem timelineItem = new TimelineNano.TimelineItem();
        timelineItem.id = "com.polysfactory.glassremote.timeline.sample";
        timelineItem.title = "From ";
        timelineItem.text = text;
        timelineItem.creationTime = now;
        timelineItem.modifiedTime = now;
        timelineItem.sourceType = TimelineNano.SourceType.COMPANIONWARE;
        timelineItem.source = "T";
        timelineItem.isDeleted = false;
        envelope.timelineItem = new TimelineNano.TimelineItem[] { timelineItem };
        return envelope;
    }
}