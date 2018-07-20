package edu.stanford.bmir.protege.web.server.project;

import edu.stanford.bmir.protege.web.server.dispatch.impl.ProjectActionHandlerRegistry;
import edu.stanford.bmir.protege.web.shared.project.NewProjectSettings;
import edu.stanford.bmir.protege.web.shared.project.ProjectAlreadyExistsException;
import edu.stanford.bmir.protege.web.shared.project.ProjectDocumentNotFoundException;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 07/03/2012
 */
public class ProjectManager {

    private final ProjectCache projectCache;

    private final ProjectAccessManager projectAccessManager;

    @Inject
    public ProjectManager(@Nonnull ProjectCache projectCache,
                          @Nonnull ProjectAccessManager projectAccessManager) {
        this.projectCache = projectCache;
        this.projectAccessManager = projectAccessManager;
    }


    @Nonnull
    public ProjectActionHandlerRegistry getActionHandlerRegistry(@Nonnull ProjectId projectId) {
        return projectCache.getActionHandlerRegistry(checkNotNull(projectId));
    }
    
    public Project getProject(@Nonnull ProjectId projectId,
                              @Nonnull UserId requestingUser) throws ProjectDocumentNotFoundException {
        long currentTime = System.currentTimeMillis();
        projectAccessManager.logProjectAccess(projectId, requestingUser, currentTime);
        return projectCache.getProject(projectId);
    }

    public Optional<Project> getProjectIfActive(@Nonnull ProjectId projectId) throws ProjectDocumentNotFoundException {
        return projectCache.getProjectIfActive(projectId);
    }

    public boolean isActive(@Nonnull ProjectId projectId) {
        return projectCache.isActive(projectId);
    }
    
    public ProjectId createNewProject(@Nonnull NewProjectSettings newProjectSettings) throws ProjectAlreadyExistsException, OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        return projectCache.getProject(newProjectSettings);
    }
}
