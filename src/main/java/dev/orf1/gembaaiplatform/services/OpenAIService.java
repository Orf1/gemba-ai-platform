package dev.orf1.gembaaiplatform.services;

import io.github.stefanbratanov.jvm.openai.OpenAI;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {
    OpenAI openAI = OpenAI.newBuilder(System.getenv("OPENAI_API_KEY"))
            .organization("org-VGXfEPOgl3hQS3rfvbKRQkFd")
            .project("proj_GtnWc5VOFFyUmHvSEeOUmxm5")
            .build();

    public OpenAI getOpenAI() {
        return openAI;
    }
}
