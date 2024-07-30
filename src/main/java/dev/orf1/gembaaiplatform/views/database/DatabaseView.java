package dev.orf1.gembaaiplatform.views.database;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.component.html.H2;
import dev.orf1.gembaaiplatform.services.OpenAIService;
import io.github.stefanbratanov.jvm.openai.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

@PageTitle("Database")
@Menu(icon = "line-awesome/svg/box-open-solid.svg", order = 1)
@Route(value = "database")
@PermitAll
public class DatabaseView extends Composite<VerticalLayout> {
    OpenAIService openAIService;
    Grid<io.github.stefanbratanov.jvm.openai.File> grid = new Grid<>();

    @Autowired
    public DatabaseView(OpenAIService openAIService) {
        this.openAIService = openAIService;

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.addClassName(Gap.MEDIUM);
        topRow.setWidth("100%");
        topRow.setHeight("30%");
        topRow.getStyle().set("flex-grow", "4");

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setWidth("100%");
        leftColumn.getStyle().set("flex-grow", "1");

        H2 leftHeading = new H2("Upload files, documents, books");
        leftColumn.add(leftHeading);

        Upload docUpload = buildDocumentUpload();

        leftColumn.add(docUpload);

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.getStyle().set("flex-grow", "1");

        H2 rightHeading = new H2("Upload Miro boards (Under construction)");
        rightColumn.add(rightHeading);

        Upload miroUpload = buildMiroUpload();
        rightColumn.add(miroUpload);

        topRow.add(leftColumn);
        topRow.add(rightColumn);

        HorizontalLayout bottomRow = new HorizontalLayout();
        bottomRow.setSizeFull();


        grid.setAllRowsVisible(true);
        grid.setHeight("95%");
        grid.addColumn(io.github.stefanbratanov.jvm.openai.File::filename).setHeader("Name").setFlexGrow(5);
        grid.addColumn(io.github.stefanbratanov.jvm.openai.File::bytes).setHeader("Size (Bytes)").setFlexGrow(3);
        grid.addColumn(
                new ComponentRenderer<>(com.vaadin.flow.component.button.Button::new, (button, file) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> {
                        openAIService.getFilesClient().deleteFile(file.id());
                        updateGrid();
                    });
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setHeader("Manage").setFlexGrow(1);

        updateGrid();

        bottomRow.add(grid);

        layout.add(topRow, bottomRow);

        getContent().add(layout);
    }

    private void updateGrid() {
        grid.setItems(openAIService.getFilesClient().listFiles());
    }

    private Upload buildDocumentUpload() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();

        Upload upload = new Upload(buffer);
        upload.setSizeFull();
        upload.setMaxFiles(1);

        upload.setAcceptedFileTypes(
                "text/x-c", "text/x-csharp", "text/x-c++", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/html", "text/x-java",
                "application/json", "text/markdown", "application/pdf", "text/x-php",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/x-python",
                "text/x-script.python", "text/x-ruby", "text/x-tex", "text/plain",
                "text/css", "text/javascript", "application/x-sh", "application/typescript"
        );

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            try (InputStream inputStream = buffer.getInputStream(fileName)) {
                File tempFile = saveToFile(inputStream, fileName);

                UploadFileRequest uploadFileRequest = UploadFileRequest.newBuilder()
                        .file(Paths.get(tempFile.getAbsolutePath()))
                        .purpose(Purpose.ASSISTANTS)
                        .build();
                io.github.stefanbratanov.jvm.openai.File uploadedFile = openAIService.getFilesClient().uploadFile(uploadFileRequest);

                CreateVectorStoreFileBatchRequest createVectorStoreFileBatchRequest = CreateVectorStoreFileBatchRequest.newBuilder()
                        .fileIds(List.of(uploadedFile.id()))
                        .build();

                VectorStoreFileBatch batch = openAIService
                        .getVectorStoreFileBatchesClient()
                        .createVectorStoreFileBatch(openAIService.getVectorStore().id(), createVectorStoreFileBatchRequest);

                openAIService
                        .getVectorStoreFileBatchesClient()
                        .retrieveVectorStoreFileBatch(openAIService.getVectorStore().id(), batch.id());

                tempFile.delete();
                updateGrid();
            } catch (IOException e) {
                Notification.show("File upload failed: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        return upload;
    }

    private Upload buildMiroUpload() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();

        Upload upload = new Upload(buffer);
        upload.setSizeFull();

        return upload;
    }


    private File saveToFile(InputStream inputStream, String fileName) throws IOException {
        File tempFile = File.createTempFile("upload-", "-" + fileName);
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            inputStream.transferTo(outputStream);
        }
        return tempFile;
    }
}
