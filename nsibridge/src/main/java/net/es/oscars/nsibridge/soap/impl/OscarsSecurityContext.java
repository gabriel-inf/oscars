package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.common.soap.gen.SubjectAttributes;

public class OscarsSecurityContext {
    private OscarsSecurityContext() {

    }
    private static OscarsSecurityContext instance;

    public static OscarsSecurityContext getInstance() {
        if (instance == null) instance = new OscarsSecurityContext();
        return instance;
    }

    private SubjectAttributes subjectAttributes = null;

    public SubjectAttributes getSubjectAttributes() {
        return subjectAttributes;
    }

    public void setSubjectAttributes(SubjectAttributes subjectAttributes) {
        this.subjectAttributes = subjectAttributes;
    }
}
