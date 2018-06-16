package edu.stanford.bmir.protege.web.client.match;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import edu.stanford.bmir.protege.web.shared.match.criteria.EntityTypeOneOfCriteria;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 15 Jun 2018
 */
public class EntityTypeCriteriaPresenter implements CriteriaPresenter<EntityTypeOneOfCriteria> {

    @Nonnull
    private final EntityTypeCriteriaView view;

    @Inject
    public EntityTypeCriteriaPresenter(@Nonnull EntityTypeCriteriaView view) {
        this.view = checkNotNull(view);
    }

    @Override
    public void start(@Nonnull AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void stop() {

    }

    @Override
    public Optional<? extends EntityTypeOneOfCriteria> getCriteria() {
        return Optional.of(EntityTypeOneOfCriteria.get(ImmutableSet.copyOf(view.getEntityTypes())));
    }
}
