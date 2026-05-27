package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.util.I18n;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

/**
 * Abstract Base Class that unifies TableView and Search box boilerplate across data tabs.
 * Supports overridable layout extensions for stats cards, custom search bars, and goal bars.
 */
public abstract class BaseDataTab<T> extends BorderPane {
    protected final DashboardStage parent;
    protected final TableView<T> tableView;
    protected final ObservableList<T> masterList = FXCollections.observableArrayList();
    protected final FilteredList<T> filteredList;
    protected final TextField txtSearch;

    public BaseDataTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);

        txtSearch = DashboardLayout.newSearchField();
        tableView = DashboardLayout.createTable();

        filteredList = new FilteredList<>(masterList, p -> true);
        tableView.setItems(filteredList);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(item -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return matchesSearch(item, newVal.toLowerCase().trim());
            });
        });
    }

    /**
     * Builds the unified tab layout. Call this at the end of the child tab constructor
     * once child fields have been fully initialized.
     */
    protected void buildLayout(String titleKey, String subtitleKey, String searchPromptKey, Node... rightHeaderNodes) {
        VBox container = DashboardLayout.createPageContainer();

        // 1. Page Header with dynamic right action nodes (e.g. stat cards)
        container.getChildren().add(DashboardLayout.createPageHeader(
                I18n.get(titleKey),
                I18n.get(subtitleKey),
                rightHeaderNodes
        ));

        // 2. Custom header extension (e.g. Likes ProgressBar Goal bar)
        Node headerExtension = buildHeaderExtension();
        if (headerExtension != null) {
            container.getChildren().add(headerExtension);
        }

        // 3. Search Bar / Filter Bar (overridable for custom filters)
        container.getChildren().add(buildSearchBar(searchPromptKey));

        // 4. TableView
        container.getChildren().add(tableView);

        // 5. Footer Actions
        Pane footer = buildFooterActions();
        if (footer != null) {
            container.getChildren().add(footer);
        }

        setCenter(container);
    }

    // --- Default Overridable Layout Hook methods ---

    /**
     * Override to inject custom layouts (e.g. Goal bar) right under the header.
     */
    protected Node buildHeaderExtension() {
        return null;
    }

    /**
     * Override to customize the search bar wrapping (e.g. HBox with ComboBox category filters).
     */
    protected Node buildSearchBar(String searchPromptKey) {
        return DashboardLayout.createSearchBox(txtSearch, I18n.get(searchPromptKey));
    }

    // --- Abstract Hooks for child tabs ---
    protected abstract void setupTableColumns();
    protected abstract boolean matchesSearch(T item, String query);
    protected abstract Pane buildFooterActions();
}
