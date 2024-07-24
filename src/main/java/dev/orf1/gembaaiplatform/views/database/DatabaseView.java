package dev.orf1.gembaaiplatform.views.database;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;

@PageTitle("Database")
@Menu(icon = "line-awesome/svg/box-open-solid.svg", order = 1)
@Route(value = "database")
@AnonymousAllowed
public class DatabaseView extends Composite<VerticalLayout> {

    public DatabaseView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.addClassName(Gap.MEDIUM);
        topRow.setWidth("100%");
        topRow.getStyle().set("flex-grow", "1");

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setWidth("100%");
        leftColumn.getStyle().set("flex-grow", "1");

        H2 leftHeading = new H2("Upload files, documents, books");
        leftColumn.add(leftHeading);

        MultiFileMemoryBuffer bufferA = new MultiFileMemoryBuffer();
        Upload uploadA = new Upload(bufferA);
        uploadA.setSizeFull();
        leftColumn.add(uploadA);

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.getStyle().set("flex-grow", "1");

        H2 rightHeading = new H2("Upload Miro boards");
        rightColumn.add(rightHeading);

        MultiFileMemoryBuffer bufferB = new MultiFileMemoryBuffer();
        Upload uploadB = new Upload(bufferB);
        uploadB.setSizeFull();
        rightColumn.add(uploadB);

        topRow.add(leftColumn);
        topRow.add(rightColumn);

        HorizontalLayout bottomRow = new HorizontalLayout();
        bottomRow.setSizeFull();

        layout.add(topRow, bottomRow);

        getContent().add(layout);

    }
}
