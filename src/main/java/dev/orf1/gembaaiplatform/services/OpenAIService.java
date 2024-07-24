package dev.orf1.gembaaiplatform.services;

import io.github.stefanbratanov.jvm.openai.*;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service @Getter
public class OpenAIService {
    OpenAI openAI = OpenAI.newBuilder(System.getenv("OPENAI_API_KEY"))
            .organization("org-VGXfEPOgl3hQS3rfvbKRQkFd")
            .project("proj_GtnWc5VOFFyUmHvSEeOUmxm5")
            .build();

    AssistantsClient assistantsClient = openAI.assistantsClient();
    ThreadsClient threadsClient = openAI.threadsClient();
    MessagesClient messagesClient = openAI.messagesClient();
    RunsClient runsClient = openAI.runsClient();
    VectorStoresClient vectorStoresClient = openAI.vectorStoresClient();
    FilesClient filesClient = openAI.filesClient();
    VectorStoreFileBatchesClient vectorStoreFileBatchesClient = openAI.vectorStoreFileBatchesClient();

    Assistant assistant = assistantsClient.retrieveAssistant("asst_t8OXStmsu7QZFxGSt7GL1qCM");
    VectorStore vectorStore = vectorStoresClient.retrieveVectorStore("vs_Z0AC1UqfdohuJcmklHVQcYxx");

    public OpenAIService() {

    }
}
