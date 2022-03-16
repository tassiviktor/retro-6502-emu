package hu.tassiviktor.emulator.retro6502.component.wireing;

import java.util.ArrayList;
import java.util.List;

import static hu.tassiviktor.emulator.retro6502.common.GenericConstants.LOW;

public class Line {

    private boolean state = LOW;

    private final List<Runnable> lowListeners = new ArrayList<>();
    private final List<Runnable> highListeners = new ArrayList<>();

    public void setLineListener(boolean state, Runnable o) {
        if (state){
          highListeners.add(o);
        } else {
            lowListeners.add(o);
        }
    }

    public boolean isHigh() {
        return state;
    }

    public boolean isLow() {
        return !state;
    }

    public boolean setState(boolean newState) {
        boolean currentState = state;
        state = newState;
        if (state){
            for (Runnable highListener : highListeners) {
                highListener.run();
            }
        } else {
            for (Runnable lowListener : lowListeners) {
                lowListener.run();
            }
        }
        return currentState;
    }

}
