package br.com.fragomeni.scan_to_pdf.svc;

import java.util.Observable;

/**
 * Barramento de eventos para notificação entre activities
 */
public class EventBus extends Observable {

    public enum Event {
        INDEX_UPDATED,
        FILE_UPDATED;
    }

    private static EventBus instance = new EventBus();

    private EventBus() {}

    public static EventBus getInstance() {
        return instance;
    }

    @Override
    public void notifyObservers(Object arg) {
        setChanged();
        super.notifyObservers(arg);
    }
}
