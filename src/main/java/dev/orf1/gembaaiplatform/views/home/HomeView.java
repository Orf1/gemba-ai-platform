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
import dev.orf1.gembaaiplatform.services.OpenAIService;
import io.github.stefanbratanov.jvm.openai.*;
import io.github.stefanbratanov.jvm.openai.Thread;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.vaadin.flow.component.progressbar.ProgressBar;

@PageTitle("Home")
@Menu(icon = "line-awesome/svg/home-solid.svg", order = 0)
@Route(value = "")
@RouteAlias(value = "")
@PermitAll
public class HomeView extends Composite<VerticalLayout> {
    MessageList messageList = new MessageList();
    TextArea customInstructions = new TextArea();
    Select modelSelect = new Select();

    String instructions =
            "You are the personal assistant for an executive at Gemba." +
            " When asked about a topic, answer with the best of your ability. " +
            "If a file is relevant to the question, use it." +
            "If there's no relevant file, answer to the best of your knowledge. " +
            "If you don't know, just say you don't know.";

    private final OpenAIService openAIService;
    private final Thread thread;

    @Autowired
    public HomeView(OpenAIService openAIService) {
        this.openAIService = openAIService;

        // Create a thread for the session
        this.thread = createThread();

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

        messageList.setWidth("100%");
        messageList.getStyle().set("flex-grow", "1");
        resetMessageList();

        ProgressBar progressbar = new ProgressBar();
        progressbar.setIndeterminate(true);
        progressbar.setVisible(false);

        MessageInput messageInput = new MessageInput();
        messageInput.setWidth("100%");

        messageInput.addSubmitListener(submitEvent -> {
            progressbar.setVisible(true);
            messageInput.setEnabled(false);
            customInstructions.setEnabled(false);
            modelSelect.setEnabled(false);

            addUserMessage(submitEvent.getValue());

            java.lang.Thread.ofVirtual().name("aiprocess").start(() -> {
                getUI().ifPresent(ui -> ui.access(() -> {
                    CreateRunRequest createRunRequest = CreateRunRequest.newBuilder()
                        .assistantId(openAIService.getAssistant().id())
                        .stream(true)
                        .additionalInstructions(customInstructions.getValue())
                        .build();

                    AtomicReference<MessageListItem> lastMessage = new AtomicReference<>(addAIMessage(""));
                    List<MessageListItem> currentItems = new ArrayList<>(messageList.getItems());

                    openAIService.getRunsClient().createRunAndStream(thread.id(), createRunRequest, new AssistantStreamEventSubscriber() {
                        @Override
                        public void onThread(String event, Thread thread) {}

                        @Override
                        public void onThreadRun(String event, ThreadRun threadRun) {}

                        @Override
                        public void onThreadRunStep(String event, ThreadRunStep threadRunStep) {}

                        @Override
                        public void onThreadRunStepDelta(String event, ThreadRunStepDelta threadRunStepDelta) {}

                        @Override
                        public void onThreadMessage(String event, ThreadMessage threadMessage) {}

                        @Override
                        public void onThreadMessageDelta(String event, ThreadMessageDelta threadMessageDelta) {
                            getUI().ifPresent(ui -> ui.access(() -> lastMessage.updateAndGet(message -> {
                                for (ThreadMessageDelta.Delta.Content content : threadMessageDelta.delta().content()) {
                                    if (content instanceof ThreadMessageDelta.Delta.Content.TextContent textContent) {
                                        message.setText(message.getText() + textContent.text().value());
                                    }
                                }

                                messageList.setItems(currentItems);
                                return message;
                            })));
                        }

                        @Override
                        public void onUnknownEvent(String event, String data) {}

                        @Override
                        public void onException(Throwable ex) {
                            ex.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                            getUI().ifPresent(ui -> ui.access(() -> {
                                messageInput.setEnabled(true);
                                progressbar.setVisible(false);
                            }));
                        }
                    });
                }));
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

        modelSelect.setLabel("Model");
        modelSelect.setWidth("min-content");
        modelSelect.setItems("GPT-4 Full", "GPT-4 Mini");
        modelSelect.setValue("GPT-4 Full");

        customInstructions.setLabel("Custom Instructions");
        customInstructions.setWidth("100%");
        customInstructions.setPlaceholder("How would you like the AI to respond?");

        Button buttonPrimary = new Button();
        buttonPrimary.setText("Save");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        rightColumn.add(h2);
        rightColumn.add(modelSelect);
        rightColumn.add(customInstructions);
        rightColumn.add(buttonPrimary);

        // Combined
        layout.add(leftColumn);
        layout.add(rightColumn);
        getContent().add(layout);
    }

    private Thread createThread() {
        CreateThreadRequest.Message base = CreateThreadRequest.Message.newBuilder()
                .role(Role.USER)
                .content(instructions)
                .build();
        CreateThreadRequest.Message additional = CreateThreadRequest.Message.newBuilder()
                .role(Role.USER)
                .content("Additional Instructions" + customInstructions.getValue())
                .build();
        CreateThreadRequest.Message message = CreateThreadRequest.Message.newBuilder()
                .role(Role.ASSISTANT)
                .content("Hi! How can I help?")
                .build();

        List<CreateThreadRequest.Message> messages = new ArrayList<>();
        if (!customInstructions.isEmpty()) {
            messages.add(base);
            messages.add(additional);
            messages.add(message);
        } else {
            messages.add(base);
            messages.add(message);
        }

        CreateThreadRequest createThreadRequest = CreateThreadRequest.newBuilder()
                .messages(messages)
                .build();
        return openAIService.getThreadsClient().createThread(createThreadRequest);
    }

    public MessageListItem addUserMessage(String text) {
        MessageListItem message = new MessageListItem(text);
        message.setUserName("You");
        message.setUserAbbreviation("Y");
        message.setUserColorIndex(3);

        List<MessageListItem> currentItems = new ArrayList<>(messageList.getItems());
        currentItems.add(message);

        messageList.setItems(currentItems);

        CreateMessageRequest createMessageRequest = CreateMessageRequest.newBuilder()
                .role(Role.USER)
                .content(text)
                .build();

        openAIService.getMessagesClient().createMessage(thread.id(), createMessageRequest);
        return message;
    }

    public MessageListItem addAIMessage(String text) {
        MessageListItem message = new MessageListItem(text);
        message.setUserName("Gemba AI System");
        message.setUserAbbreviation("AI");
        message.setUserColorIndex(2);

        List<MessageListItem> currentItems = new ArrayList<>(messageList.getItems());
        currentItems.add(message);

        messageList.setItems(currentItems);
        return message;
    }

    private void resetMessageList() {
        addAIMessage("Hi! How can I help?");
    }
}
