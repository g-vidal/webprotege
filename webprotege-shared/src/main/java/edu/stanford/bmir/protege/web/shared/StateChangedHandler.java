package edu.stanford.bmir.protege.web.shared;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10/02/16
 */
public interface StateChangedHandler<T> {

    void handleStateChanged(T value);
}
