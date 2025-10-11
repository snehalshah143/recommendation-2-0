package tech.algofinserve.recommendation.alerts.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitters {
    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();
    private final long TIMEOUT = 60 * 60 * 1000L; // 1 hour

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public void broadcast(Object event) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("alert").data(event));
            } catch (Exception ex) {
                dead.add(e);
            }
        }
        emitters.removeAll(dead);
    }
}
