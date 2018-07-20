package edu.stanford.bmir.protege.web.client.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import edu.stanford.bmir.protege.web.client.library.common.HasPlaceholder;
import edu.stanford.bmir.protege.web.shared.DirtyChangedEvent;
import edu.stanford.bmir.protege.web.shared.DirtyChangedHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10/04/16
 */
public class ValueListFlexEditorImpl<O> extends Composite implements ValueListEditor<O>, HasPlaceholder, HasEnabled {

    private ValueListFlexEditorDirection direction = ValueListFlexEditorDirection.COLUMN;

    interface ValueListInlineEditorImplUiBinder extends UiBinder<HTMLPanel, ValueListFlexEditorImpl> {

    }

    private static ValueListInlineEditorImplUiBinder ourUiBinder = GWT.create(ValueListInlineEditorImplUiBinder.class);


    private ValueEditorFactory<O> valueEditorFactory;

    private List<ValueEditor<O>> currentEditors = new ArrayList<>();

    private ValueChangeHandler<Optional<O>> valueChangeHandler;

    private DirtyChangedHandler dirtyChangedHandler;

    private boolean dirty = false;

    private boolean enabled = false;

    private String placeholder = "";

    // Don't prompt by default
    private DeleteConfirmationPrompt<O> deleteConfirmationPrompt = (value, callback) -> callback.deleteValue(true);

    @UiField
    HTMLPanel container;

    public ValueListFlexEditorImpl(ValueEditorFactory<O> valueEditorFactory) {
        this.valueEditorFactory = valueEditorFactory;
        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
        valueChangeHandler = event -> handleValueEditorValueChanged();
        dirtyChangedHandler = event -> handleValueEditorDirtyChanged(event);
        updateEnabled();
    }

    @Override
    public void setDeleteConfirmationPrompt(@Nonnull DeleteConfirmationPrompt<O> prompt) {
        this.deleteConfirmationPrompt = checkNotNull(prompt);
    }

    @Override
    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public void setPlaceholder(String placeholder) {
        this.placeholder = checkNotNull(placeholder);
    }

    @Override
    public Widget getWidget() {
        return this;
    }


    @Override
    public void setValue(List<O> object) {
        clearInternal();
        for(O value : object) {
            ValueEditor<O> editor = addValueEditor(true);
            editor.setValue(value);
        }
        ensureBlank();
        updateEnabled();
        dirty = false;
    }

    private void clearInternal() {
        container.clear();
        currentEditors.clear();
        dirty = false;
    }

    @Override
    public void clearValue() {
        clearInternal();
        ensureBlank();
        dirty = false;
    }

    @Override
    public Optional<List<O>> getValue() {
        List<O> editedValues = new ArrayList<O>();
        for(ValueEditor<O> editor : currentEditors) {
            Optional<O> value = editor.getValue();
            if(value.isPresent() && editor.isWellFormed()) {
                editedValues.add(value.get());
            }
        }
        return Optional.of(editedValues);
    }


    @Override
    public boolean isWellFormed() {
        for(ValueEditor<O> editor : currentEditors) {
            if(!editor.isWellFormed()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDirty() {
        for(ValueEditor<O> editor : currentEditors) {
            if(editor.isDirty()) {
                return true;
            }
        }
        return dirty;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateEnabled();
    }

    private void updateEnabled() {
        for(int i = 0; i < container.getWidgetCount(); i++) {
            ValueListFlexEditorContainer editorContainer = (ValueListFlexEditorContainer) container.getWidget(i);
            editorContainer.setEnabled(enabled);

            // Don't enabled the delete button for the last row if it is a blank row
            if (i < container.getWidgetCount() - 1 || currentEditors.get(i).getValue().isPresent() || !enabled) {
                editorContainer.setDeleteButtonVisible(enabled);
            }
        }
        ensureBlank();
    }

    @Override
    public HandlerRegistration addDirtyChangedHandler(DirtyChangedHandler handler) {
        return addHandler(handler, DirtyChangedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Optional<List<O>>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }



    private void ensureBlank() {
        if (isEnabled()) {
            if(currentEditors.isEmpty() || currentEditors.get(currentEditors.size() - 1).getValue().isPresent()) {
                addValueEditor(false);
            }
        }
    }

    private ValueEditor<O> addValueEditor(boolean deleteVisible) {
        final ValueEditor<O> editor = getFreshValueEditor();
        currentEditors.add(editor);
        ValueListFlexEditorContainer<O> editorContainer = new ValueListFlexEditorContainer<>(editor);
        container.add(editorContainer);
        editorContainer.setDirection(direction);

        // TODO: Add delete button handler
        editor.addDirtyChangedHandler(dirtyChangedHandler);
        editor.addValueChangeHandler(valueChangeHandler);
        editorContainer.setDeleteButtonVisible(deleteVisible);
        editorContainer.setDeleteButtonEnabled(enabled);
        editorContainer.setDeleteButtonClickedHandler(event -> handleDelete(editor));


        if(editor instanceof HasEnabled) {
            ((HasEnabled) editor).setEnabled(enabled);
        }
        if(editor instanceof HasPlaceholder) {
            ((HasPlaceholder) editor).setPlaceholder(placeholder);
        }
        return editor;
    }

    public void setDirection(ValueListFlexEditorDirection direction) {
        this.direction = direction;
        Style style = getElement().getStyle();
        if(direction == ValueListFlexEditorDirection.COLUMN) {
            style.setProperty("flexDirection", "column");
            style.clearProperty("flexFlow");
            style.clearMargin();
        }
        else {
            style.setProperty("flexDirection", "row");
            style.setProperty("flexFlow", "wrap");
            style.setMargin(-5, Style.Unit.PX);
        }
        for(int i = 0; i < container.getWidgetCount(); i++) {
            ValueListFlexEditorContainer<O> editorContainer = (ValueListFlexEditorContainer<O>) container.getWidget(i);
            editorContainer.setDirection(direction);
        }
    }

    private void handleDelete(ValueEditor<O> editor) {
        Optional<O> value = editor.getValue();
        if(!value.isPresent()) {
            return;
        }
        deleteConfirmationPrompt.shouldDeleteValue(value.get(), delete -> {
            if (delete) {
                Optional<List<O>> before = getValue();
                removeEditor(editor);
                ensureBlank();
                dirty = true;
                fireEvent(new DirtyChangedEvent());
                Optional<List<O>> after = getValue();
                if (!after.equals(before)) {
                    ValueChangeEvent.fire(this, after);
                }
            }
        });
    }

    private void removeEditor(ValueEditor<O> editor) {
        int index = currentEditors.indexOf(editor);
        if(index == -1) {
            return;
        }
        currentEditors.remove(editor);
        container.remove(index);
    }

    private ValueEditor<O> getFreshValueEditor() {
        return valueEditorFactory.createEditor();
    }


    private void handleValueEditorValueChanged() {
        dirty = true;
        ValueChangeEvent.fire(this, getValue());
        updateEnabled();
    }

    private void handleValueEditorDirtyChanged(DirtyChangedEvent event) {
        this.fireEvent(event);
        ensureBlank();
    }

}