package wtf.nebula.client;

import me.bush.eventbuskotlin.Event;

public class NebulaCompatibility {
    public static boolean post(Event event) {
        return Nebula.Companion.getBUS().post(event);
    }
}
