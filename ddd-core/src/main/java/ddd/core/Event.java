package ddd.core;

import java.io.Serializable;

public interface Event extends Serializable {
    String eventId();
}
