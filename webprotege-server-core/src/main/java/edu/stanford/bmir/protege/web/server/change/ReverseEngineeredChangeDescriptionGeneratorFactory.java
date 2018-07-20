package edu.stanford.bmir.protege.web.server.change;

import com.google.common.collect.ImmutableSet;
import edu.stanford.bmir.protege.web.server.change.matcher.ChangeMatcher;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.inject.ProjectSingleton;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
@ProjectSingleton
public class ReverseEngineeredChangeDescriptionGeneratorFactory {

    @Nonnull
    private Set<ChangeMatcher> changeMatchers;

    @Inject
    public ReverseEngineeredChangeDescriptionGeneratorFactory(@Nonnull Set<ChangeMatcher> changeMatchers) {
        this.changeMatchers = ImmutableSet.copyOf(changeMatchers);
    }

    public <S extends OWLEntityData> ReverseEngineeredChangeDescriptionGenerator<S> get(String defaultDescription) {
        return new ReverseEngineeredChangeDescriptionGenerator<>(defaultDescription, changeMatchers);
    }
}
