package dev.orf1.gembaaiplatform.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.springframework.security.core.parameters.P;

@PageTitle("Home")
@Menu(icon = "line-awesome/svg/home-solid.svg", order = 0)
@Route(value = "")
@RouteAlias(value = "")
@PermitAll
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setSizeFull();

        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName(Gap.MEDIUM);
        layout.setWidth("100%");
        layout.setHeightFull();

        // Left side
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.getStyle().set("flex-grow", "1");

        MessageList messageList = new MessageList();
        messageList.setWidth("100%");
        messageList.getStyle().set("flex-grow", "1");
        resetMessageList(messageList);
        ProgressBar progressbar = new ProgressBar();
        progressbar.setIndeterminate(true);
        progressbar.setVisible(false);

        MessageInput messageInput = new MessageInput();
        messageInput.setWidth("100%");

        messageInput.addSubmitListener(submitEvent -> {
            messageInput.setEnabled(false);
            progressbar.setVisible(true);

            MessageListItem userMessage = new MessageListItem(submitEvent.getValue());
            userMessage.setUserName("You");
            userMessage.setUserAbbreviation("Y");
            userMessage.setUserColorIndex(3);

            List<MessageListItem> currentItems = new ArrayList<>(messageList.getItems());
            currentItems.add(userMessage);

            messageList.setItems(currentItems);

            Thread.ofVirtual().name("aiprocess").start(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                getUI().ifPresentOrElse(ui -> ui.access(() -> {
                    MessageListItem response = new MessageListItem("{response from the AI here}");
                    response.setUserName("Gemba AI System");
                    response.setUserAbbreviation("AI");
                    response.setUserColorIndex(2);

                    currentItems.add(response);
                    messageList.setItems(currentItems);
                    messageInput.setEnabled(true);
                    progressbar.setVisible(false);
                }), () -> System.out.println("Could not get UI!"));
            });
        });

        leftColumn.add(messageList);
        leftColumn.add(progressbar);
        leftColumn.add(messageInput);

        // Right side
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.getStyle().set("flex-grow", "1");
        rightColumn.setWidth("30%");

        H2 h2 = new H2();
        h2.setText("Options");
        h2.setWidth("max-content");

        Select select = new Select();
        select.setLabel("Model");
        select.setWidth("min-content");
        select.setItems("GPT-4 Full", "GPT-4 Mini");
        select.setValue("GPT-4 Full");

        TextArea textArea = new TextArea();
        textArea.setLabel("Custom Instructions");
        textArea.setWidth("100%");
        textArea.setPlaceholder("How would you like the AI to respond?");

        Button buttonPrimary = new Button();
        buttonPrimary.setText("Save");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        rightColumn.add(h2);
        rightColumn.add(select);
        rightColumn.add(textArea);
        rightColumn.add(buttonPrimary);

        // Combined
        layout.add(leftColumn);
        layout.add(rightColumn);
        getContent().add(layout);
    }

    private void resetMessageList(MessageList messageList) {
        MessageListItem introMessage = new MessageListItem("Hi! How can I help?");
        introMessage.setUserName("Gemba AI System");
        introMessage.setUserAbbreviation("AI");
        introMessage.setUserColorIndex(2);
        messageList.setItems(introMessage);
    }
}
