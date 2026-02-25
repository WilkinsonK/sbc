package org.wilkinsonk.sbc.payload;

import org.wilkinsonk.sbc.topic.Topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Channel<T extends Body> {
    protected final Topic topic;
    protected final Class<T> body;

    @SuppressWarnings("unchecked")
    public final T From(Object... args) {
        try {
            return (T) body.getConstructors()[0].newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final T FromJson(String json) throws Exception {
        return BodyBase.FromJson(json, body);
    }

    public final String GetFullyQualifiedName() {
        return topic.GetFullyQualifiedName();
    }
}
