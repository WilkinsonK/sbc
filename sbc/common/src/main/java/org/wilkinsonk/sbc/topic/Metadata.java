package org.wilkinsonk.sbc.topic;

import org.wilkinsonk.sbc.SoulardiganBackyard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Metadata {
    protected final String Namespace = SoulardiganBackyard.NAME;
    protected final String Identity;

    @Override
    public String toString() {
        return GetFullyQualifiedName();
    }

    public final String GetFullyQualifiedName() {
        return String.format("%s:%s", Namespace, Identity);
    }
}
