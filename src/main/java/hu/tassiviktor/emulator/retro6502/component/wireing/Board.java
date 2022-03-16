package hu.tassiviktor.emulator.retro6502.component.wireing;

import hu.tassiviktor.emulator.retro6502.component.template.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    private final Map<String,Bus> buses= new HashMap<>();
    private final Map<String,Line> lines = new HashMap<>();

    List<Component> components = new ArrayList<>();

    public Board addBus(String name, Bus bus) {
        buses.put(name, bus);
        return this;
    }

    public Board addLine(String name, Line line) {
        lines.put(name, line);
        return this;
    }

    public Bus getBus(String name) {
        return buses.get(name);
    }

    public Line getLine(String name) {
        return lines.get(name);
    }

    public void addDevice(Component component) {
        components.add(component);
    }

    public void powerUp() {
        for (Component component : components) {
            component.powerUp();
        }
    }

    public void installComponents() {
        for (Component component : components) {
            component.install();
        }
    }
}
