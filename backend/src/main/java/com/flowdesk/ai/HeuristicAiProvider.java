package com.flowdesk.ai;

import com.flowdesk.dto.AiAnalysis;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Offline fallback used when no LLM API key is configured. */
public class HeuristicAiProvider implements AiProvider {

    private static final Map<String, String[]> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("Bug", new String[] {"error", "crash", "bug", "broken", "fails", "exception"});
        KEYWORDS.put("Billing", new String[] {"invoice", "refund", "charge", "payment", "billing"});
        KEYWORDS.put("Account", new String[] {"password", "login", "account", "access", "locked"});
        KEYWORDS.put("Feature Request", new String[] {"feature", "would be nice", "could you add", "support for"});
    }

    @Override
    public AiAnalysis analyze(String title, String description) {
        String category = categorise((title + " " + description).toLowerCase(Locale.ROOT));
        return new AiAnalysis(summarise(title, description), category, draftReply(category));
    }

    private String categorise(String text) {
        return KEYWORDS.entrySet().stream()
                .filter(e -> {
                    for (String word : e.getValue()) {
                        if (text.contains(word)) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("General Inquiry");
    }

    private String summarise(String title, String description) {
        String firstSentence = description.strip().split("(?<=[.!?])\\s+", 2)[0];
        if (firstSentence.length() > 240) {
            firstSentence = firstSentence.substring(0, 237) + "...";
        }
        return title.strip() + ": " + firstSentence;
    }

    private String draftReply(String category) {
        String body = switch (category) {
            case "Bug" -> "Thanks for reporting this. We are investigating the problem and will update you once we have a fix.";
            case "Billing" -> "Thanks for getting in touch about billing. We are reviewing your account and will confirm the details shortly.";
            case "Account" -> "Thanks for letting us know. We are looking into your account access and will follow up with next steps.";
            case "Feature Request" -> "Thanks for the suggestion. We have logged it with the team and will share any updates.";
            default -> "Thanks for reaching out. We have received your request and will reply soon.";
        };
        return "Hi,\n\n" + body + "\n\nBest regards,\nSupport Team";
    }
}
