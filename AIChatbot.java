import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CodeAlpha Task: Artificial Intelligence Chatbot
 * 
 * Features:
 * 1. Natural Language Processing (NLP) Pipeline:
 *    - Text Normalization & Punctuation Stripping
 *    - Word Tokenization
 *    - Stop-Word Filtering
 *    - Suffix-Stripping Stemmer (Porter-inspired)
 * 2. Hybrid Intelligence Engine:
 *    - Conversational Rule-Based Pattern & Entity Extraction (Name memory, Math, Time/Date)
 *    - Machine Learning Information Retrieval via TF-IDF Vectorization & Cosine Similarity
 * 3. Live Interactive Training:
 *    - Add new FAQ intents and train/re-vectorize the model in real-time
 * 4. Interactive Graphical User Interface (GUI):
 *    - Modern dark/light-styled Swing interface with live NLP telemetry inspection
 *    - Quick-reply suggestion chips & Training Dialog
 */
public class AIChatbot extends JFrame {

    // --- UI Components ---
    private JTextPane chatPane;
    private StyledDocument chatDoc;
    private JTextField inputField;
    private JButton sendButton;
    private JButton trainButton;
    private JButton clearButton;
    
    // Telemetry Sidebar Components
    private JLabel intentValueLabel;
    private JProgressBar confidenceBar;
    private JTextArea tokensArea;
    private JLabel engineMethodLabel;
    private JLabel knowledgeCountLabel;

    // --- Core AI & NLP Engine ---
    private ChatbotEngine engine;

    // --- Color Palette (Modern Slate UI) ---
    private static final Color BG_DARK = new Color(15, 23, 42);       // Slate 900
    private static final Color BG_PANEL = new Color(30, 41, 59);      // Slate 800
    private static final Color BG_CHAT = new Color(248, 250, 252);    // Slate 50
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);  // Blue 600
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);// Emerald 500
    private static final Color TEXT_LIGHT = new Color(241, 245, 249); // Slate 100
    private static final Color TEXT_MUTED = new Color(148, 163, 184); // Slate 400

    public AIChatbot() {
        super("CodeAlpha - AI NLP Chatbot");
        this.engine = new ChatbotEngine();
        initGUI();
        postWelcomeMessage();
    }

    /**
     * Initializes the Swing Graphical User Interface
     */
    private void initGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setMinimumSize(new Dimension(820, 550));
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // 1. Top Header Bar
        JPanel headerPanel = createHeaderPanel();
        getContentPane().add(headerPanel, BorderLayout.NORTH);

        // 2. Center Chat & Quick Chips Area
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(BG_CHAT);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG_CHAT);
        chatPane.setMargin(new Insets(16, 16, 16, 16));
        chatDoc = chatPane.getStyledDocument();
        initDocumentStyles();

        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setBorder(null);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        centerContainer.add(chatScroll, BorderLayout.CENTER);

        // Quick-reply suggestion bar
        JPanel suggestionBar = createSuggestionBar();
        centerContainer.add(suggestionBar, BorderLayout.SOUTH);

        getContentPane().add(centerContainer, BorderLayout.CENTER);

        // 3. Right NLP Telemetry & Diagnostics Sidebar
        JPanel sidebarPanel = createTelemetrySidebar();
        getContentPane().add(sidebarPanel, BorderLayout.EAST);

        // 4. Bottom Input Bar
        JPanel bottomPanel = createInputPanel();
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Builds the Top Header Bar with status and action buttons
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel("CodeAlpha Neural & Rule-Based AI Assistant");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel subtitleLabel = new JLabel("● Online | Hybrid TF-IDF Cosine Similarity + NLP Stemming Engine");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(ACCENT_GREEN);

        titleBox.add(titleLabel);
        titleBox.add(subtitleLabel);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        trainButton = createStyledButton("+ Train Bot (Add FAQ)", ACCENT_BLUE, Color.WHITE);
        trainButton.addActionListener(e -> openTrainingDialog());

        clearButton = createStyledButton("Clear Chat", BG_PANEL, TEXT_LIGHT);
        clearButton.addActionListener(e -> {
            chatPane.setText("");
            postWelcomeMessage();
        });

        buttonGroup.add(trainButton);
        buttonGroup.add(clearButton);

        header.add(titleBox, BorderLayout.WEST);
        header.add(buttonGroup, BorderLayout.EAST);
        return header;
    }

    /**
     * Builds the Quick-Reply Suggestion Chips Bar
     */
    private JPanel createSuggestionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(new Color(241, 245, 249));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(226, 232, 240)));

        JLabel promptLabel = new JLabel("Try asking:");
        promptLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        promptLabel.setForeground(new Color(100, 116, 139));
        panel.add(promptLabel);

        String[] samplePrompts = {
            "What is CodeAlpha?",
            "How does your NLP work?",
            "What is Java?",
            "Calculate 45 * 12",
            "My name is Alex",
            "Tell me a joke"
        };

        for (String prompt : samplePrompts) {
            JButton chip = new JButton(prompt);
            chip.setFont(new Font("SansSerif", Font.PLAIN, 11));
            chip.setFocusPainted(false);
            chip.setBackground(Color.WHITE);
            chip.setForeground(new Color(51, 65, 85));
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 10, 4, 10)
            ));
            chip.addActionListener(e -> {
                inputField.setText(prompt);
                handleUserSend();
            });
            panel.add(chip);
        }
        return panel;
    }

    /**
     * Builds the Right Sidebar displaying live NLP tokenization, intent, and ML confidence score
     */
    private JPanel createTelemetrySidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_PANEL);
        sidebar.setPreferredSize(new Dimension(275, 0));
        sidebar.setBorder(new EmptyBorder(18, 16, 18, 16));

        JLabel header = new JLabel("NLP & ML TELEMETRY");
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(TEXT_MUTED);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(header);
        sidebar.add(Box.createVerticalStrut(15));

        // Matched Intent
        sidebar.add(createSidebarLabel("Detected Intent / Tag:"));
        intentValueLabel = new JLabel("Awaiting input...");
        intentValueLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        intentValueLabel.setForeground(ACCENT_GREEN);
        intentValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(intentValueLabel);
        sidebar.add(Box.createVerticalStrut(14));

        // Resolution Method
        sidebar.add(createSidebarLabel("Resolution Engine:"));
        engineMethodLabel = new JLabel("None");
        engineMethodLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        engineMethodLabel.setForeground(TEXT_LIGHT);
        engineMethodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(engineMethodLabel);
        sidebar.add(Box.createVerticalStrut(14));

        // Confidence Score Progress Bar
        sidebar.add(createSidebarLabel("Cosine Similarity / Confidence:"));
        sidebar.add(Box.createVerticalStrut(4));
        confidenceBar = new JProgressBar(0, 100);
        confidenceBar.setValue(0);
        confidenceBar.setStringPainted(true);
        confidenceBar.setForeground(ACCENT_BLUE);
        confidenceBar.setBackground(BG_DARK);
        confidenceBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        confidenceBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(confidenceBar);
        sidebar.add(Box.createVerticalStrut(16));

        // Processed NLP Tokens & Stems
        sidebar.add(createSidebarLabel("NLP Pipeline (Tokens -> Stems):"));
        sidebar.add(Box.createVerticalStrut(6));
        tokensArea = new JTextArea(7, 18);
        tokensArea.setEditable(false);
        tokensArea.setLineWrap(true);
        tokensArea.setWrapStyleWord(true);
        tokensArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        tokensArea.setBackground(BG_DARK);
        tokensArea.setForeground(new Color(125, 211, 252));
        tokensArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        tokensArea.setText("Tokens will appear here when you send a message...");

        JScrollPane tokenScroll = new JScrollPane(tokensArea);
        tokenScroll.setBorder(new LineBorder(new Color(51, 65, 85), 1));
        tokenScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        tokenScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        sidebar.add(tokenScroll);
        sidebar.add(Box.createVerticalStrut(16));

        // Knowledge Base Stats
        sidebar.add(createSidebarLabel("Trained Knowledge Base:"));
        knowledgeCountLabel = new JLabel(engine.getIntentCount() + " Intents | " + engine.getVocabularySize() + " Stems");
        knowledgeCountLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        knowledgeCountLabel.setForeground(TEXT_LIGHT);
        knowledgeCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(knowledgeCountLabel);

        sidebar.add(Box.createVerticalGlue());

        JLabel footerHint = new JLabel("<html><body style='width: 190px; color: #94a3b8; font-size: 9px;'>"
                + "Tip: Click <b>+ Train Bot</b> above to teach the AI custom Q&A pairs dynamically during runtime.</body></html>");
        footerHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footerHint);

        return sidebar;
    }

    private JLabel createSidebarLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Builds the bottom message input bar
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        inputField.addActionListener(e -> handleUserSend());

        sendButton = createStyledButton("Send Message ➤", ACCENT_BLUE, Color.WHITE);
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendButton.setPreferredSize(new Dimension(150, 42));
        sendButton.addActionListener(e -> handleUserSend());

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        return panel;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    /**
     * Configures text styles for chat messages
     */
    private void initDocumentStyles() {
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style userHeader = chatDoc.addStyle("UserHeader", defaultStyle);
        StyleConstants.setFontFamily(userHeader, "SansSerif");
        StyleConstants.setFontSize(userHeader, 11);
        StyleConstants.setBold(userHeader, true);
        StyleConstants.setForeground(userHeader, ACCENT_BLUE);

        Style botHeader = chatDoc.addStyle("BotHeader", defaultStyle);
        StyleConstants.setFontFamily(botHeader, "SansSerif");
        StyleConstants.setFontSize(botHeader, 11);
        StyleConstants.setBold(botHeader, true);
        StyleConstants.setForeground(botHeader, new Color(15, 118, 110));

        Style bodyStyle = chatDoc.addStyle("MessageBody", defaultStyle);
        StyleConstants.setFontFamily(bodyStyle, "SansSerif");
        StyleConstants.setFontSize(bodyStyle, 14);
        StyleConstants.setForeground(bodyStyle, new Color(30, 41, 59));
    }

    /**
     * Appends a formatted chat message to the transcript pane
     */
    private void appendChatMessage(String sender, String message, boolean isUser) {
        try {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String headerStyle = isUser ? "UserHeader" : "BotHeader";
            String prefix = isUser ? "You (" + time + ")\n" : "CodeAlpha AI (" + time + ")\n";

            chatDoc.insertString(chatDoc.getLength(), prefix, chatDoc.getStyle(headerStyle));
            chatDoc.insertString(chatDoc.getLength(), message + "\n\n", chatDoc.getStyle("MessageBody"));
            chatPane.setCaretPosition(chatDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void postWelcomeMessage() {
        String welcome = "Hello! I am your CodeAlpha Artificial Intelligence Chatbot.\n"
                + "I combine Natural Language Processing (tokenization, stop-word filtering, stemming) "
                + "with a TF-IDF Machine Learning vector model and rule-based entity extraction.\n\n"
                + "Ask me about Java, AI, CodeAlpha internships, math calculations, or click '+ Train Bot' to teach me new FAQs!";
        appendChatMessage("CodeAlpha AI", welcome, false);
    }

    /**
     * Processes user input, queries the AI Engine, and updates both Chat and Telemetry UI
     */
    private void handleUserSend() {
        String userText = inputField.getText().trim();
        if (userText.isEmpty()) {
            return;
        }
        inputField.setText("");
        appendChatMessage("You", userText, true);

        // Query the AI Engine
        BotResponse response = engine.generateResponse(userText);

        // Append AI Response
        appendChatMessage("CodeAlpha AI", response.reply, false);

        // Update Telemetry Sidebar
        intentValueLabel.setText(response.intentTag);
        engineMethodLabel.setText(response.engineType);
        int pct = (int) Math.round(response.confidence * 100);
        confidenceBar.setValue(pct);
        confidenceBar.setString(pct + "% (" + String.format("%.2f", response.confidence) + ")");

        if (pct >= 65) {
            confidenceBar.setForeground(ACCENT_GREEN);
        } else if (pct >= 30) {
            confidenceBar.setForeground(ACCENT_BLUE);
        } else {
            confidenceBar.setForeground(new Color(239, 68, 68));
        }

        tokensArea.setText(response.nlpTrace);
        knowledgeCountLabel.setText(engine.getIntentCount() + " Intents | " + engine.getVocabularySize() + " Stems");
    }

    /**
     * Opens a custom Swing modal dialog allowing the user to train the bot with new FAQ knowledge
     */
    private void openTrainingDialog() {
        JDialog dialog = new JDialog(this, "Train AI Chatbot - Add New FAQ Intent", true);
        dialog.setSize(480, 390);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel tagLbl = new JLabel("Intent Tag Name (e.g., 'certificate_info'):");
        JTextField tagField = new JTextField(20);
        tagField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel patternsLbl = new JLabel("Training Questions / Patterns (separate with commas):");
        JTextArea patternsArea = new JTextArea(3, 20);
        patternsArea.setLineWrap(true);
        patternsArea.setWrapStyleWord(true);
        JScrollPane pScroll = new JScrollPane(patternsArea);

        JLabel responseLbl = new JLabel("Bot Response for this FAQ:");
        JTextArea responseArea = new JTextArea(4, 20);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        JScrollPane rScroll = new JScrollPane(responseArea);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(220, 38, 38));

        form.add(tagLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(tagField);
        form.add(Box.createVerticalStrut(12));
        form.add(patternsLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(pScroll);
        form.add(Box.createVerticalStrut(12));
        form.add(responseLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(rScroll);
        form.add(Box.createVerticalStrut(8));
        form.add(statusLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Train & Re-Vectorize Model", ACCENT_BLUE, Color.WHITE);
        saveBtn.addActionListener(e -> {
            String tag = tagField.getText().trim();
            String patternsRaw = patternsArea.getText().trim();
            String reply = responseArea.getText().trim();

            if (tag.isEmpty() || patternsRaw.isEmpty() || reply.isEmpty()) {
                statusLabel.setText("Please fill in all fields to train the model.");
                return;
            }

            List<String> patterns = new ArrayList<>();
            for (String p : patternsRaw.split(",")) {
                if (!p.trim().isEmpty()) {
                    patterns.add(p.trim());
                }
            }

            engine.learnNewIntent(tag, patterns, Collections.singletonList(reply));
            knowledgeCountLabel.setText(engine.getIntentCount() + " Intents | " + engine.getVocabularySize() + " Stems");
            appendChatMessage("CodeAlpha AI", "✅ Model Retrained! I have learned the new intent '"
                    + tag + "' with " + patterns.size() + " training patterns.", false);
            dialog.dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // =========================================================================
    // INNER CLASSES: NLP PIPELINE, TF-IDF ML VECTORIZER, AND RULE ENGINE
    // =========================================================================

    /**
     * Encapsulates the Chatbot's response along with explainable AI telemetry
     */
    private static class BotResponse {
        final String reply;
        final String intentTag;
        final double confidence;
        final String engineType;
        final String nlpTrace;

        BotResponse(String reply, String intentTag, double confidence, String engineType, String nlpTrace) {
            this.reply = reply;
            this.intentTag = intentTag;
            this.confidence = confidence;
            this.engineType = engineType;
            this.nlpTrace = nlpTrace;
        }
    }

    /**
     * Represents an FAQ Intent Category with training patterns and candidate responses
     */
    private static class Intent {
        final String tag;
        final List<String> patterns;
        final List<String> responses;

        Intent(String tag, List<String> patterns, List<String> responses) {
            this.tag = tag;
            this.patterns = new ArrayList<>(patterns);
            this.responses = new ArrayList<>(responses);
        }
    }

    /**
     * Natural Language Processing (NLP) Utility Class
     * Handles text normalization, tokenization, stop-word removal, and stemming.
     */
    private static class NLPPipeline {
        private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "am", "be", "been", "being",
            "in", "on", "at", "to", "for", "with", "by", "of", "and", "or", "but",
            "please", "can", "could", "would", "should", "do", "does", "did", "has", "have"
        ));

        private static final Map<String, String> SYNONYM_MAP = new HashMap<>();
        static {
            SYNONYM_MAP.put("hi", "hello");
            SYNONYM_MAP.put("hey", "hello");
            SYNONYM_MAP.put("greetings", "hello");
            SYNONYM_MAP.put("cost", "price");
            SYNONYM_MAP.put("fee", "price");
            SYNONYM_MAP.put("paid", "price");
            SYNONYM_MAP.put("bot", "chatbot");
            SYNONYM_MAP.put("ai", "artificialintelligence");
            SYNONYM_MAP.put("ml", "machinelearning");
            SYNONYM_MAP.put("bye", "goodbye");
        }

        /**
         * Cleans, tokenizes, removes non-essential stop words, and stems a sentence.
         */
        public static List<String> preprocess(String text) {
            String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
            String[] rawTokens = cleaned.trim().split("\\s+");
            List<String> stems = new ArrayList<>();

            for (String token : rawTokens) {
                if (token.isEmpty()) continue;
                String normalized = SYNONYM_MAP.getOrDefault(token, token);
                // Keep all words if the query is very short, otherwise filter stop words
                if (rawTokens.length > 2 && STOP_WORDS.contains(normalized)) {
                    continue;
                }
                stems.add(stem(normalized));
            }
            return stems;
        }

        /**
         * Lightweight rule-based English Stemmer (inspired by Porter Stemmer)
         */
        public static String stem(String word) {
            if (word.length() <= 3) return word;
            if (word.endsWith("ing") && word.length() > 5) {
                return word.substring(0, word.length() - 3);
            }
            if (word.endsWith("ed") && word.length() > 4) {
                return word.substring(0, word.length() - 2);
            }
            if (word.endsWith("ies") && word.length() > 4) {
                return word.substring(0, word.length() - 3) + "y";
            }
            if (word.endsWith("es") && word.length() > 4) {
                return word.substring(0, word.length() - 2);
            }
            if (word.endsWith("s") && !word.endsWith("ss") && word.length() > 3) {
                return word.substring(0, word.length() - 1);
            }
            if (word.endsWith("ment") && word.length() > 6) {
                return word.substring(0, word.length() - 4);
            }
            if (word.endsWith("tion") && word.length() > 6) {
                return word.substring(0, word.length() - 4);
            }
            return word;
        }
    }

    /**
     * Core Hybrid AI Chatbot Engine:
     * 1. Rule-Based Conversational Memory & Math/Time Evaluators
     * 2. TF-IDF Vectorizer & Cosine Similarity Classifier over Trained FAQ Corpus
     */
    private static class ChatbotEngine {
        private final List<Intent> intents = new ArrayList<>();
        private final Map<String, Double> idfScores = new HashMap<>();
        private final List<PatternVector> trainedVectors = new ArrayList<>();
        private final Random random = new Random();
        private String rememberedUserName = null;

        private static class PatternVector {
            final String intentTag;
            final String rawPattern;
            final Map<String, Double> tfidfVector;

            PatternVector(String intentTag, String rawPattern, Map<String, Double> tfidfVector) {
                this.intentTag = intentTag;
                this.rawPattern = rawPattern;
                this.tfidfVector = tfidfVector;
            }
        }

        public ChatbotEngine() {
            seedDefaultKnowledgeBase();
            trainTfIdfModel();
        }

        /**
         * Loads the initial FAQ dataset and conversational intents
         */
        private void seedDefaultKnowledgeBase() {
            intents.add(new Intent("greeting",
                Arrays.asList("hello", "hi there", "good morning", "hey chatbot", "greetings", "howdy"),
                Arrays.asList(
                    "Hello! How can I assist you today?",
                    "Hi there! Ask me anything about Java, NLP, or CodeAlpha.",
                    "Greetings! I am ready to answer your questions."
                )
            ));

            intents.add(new Intent("how_are_you",
                Arrays.asList("how are you", "how are you doing", "how is it going", "are you okay"),
                Arrays.asList(
                    "I'm functioning at 100% efficiency! My TF-IDF vectors are calibrated and ready. How are you?",
                    "Doing great! Always excited to process natural language queries."
                )
            ));

            intents.add(new Intent("codealpha_info",
                Arrays.asList("what is codealpha", "tell me about codealpha", "codealpha internship", "about codealpha company"),
                Collections.singletonList(
                    "CodeAlpha is a leading software development & educational platform that empowers students through hands-on virtual internships in Java, AI, Web Development, and Cyber Security."
                )
            ));

            intents.add(new Intent("nlp_explanation",
                Arrays.asList("how does your nlp work", "what nlp techniques do you use", "explain natural language processing", "how do you understand me"),
                Collections.singletonList(
                    "My NLP pipeline processes your input in 4 stages:\n"
                    + "1. Normalization & Regex Cleaning\n"
                    + "2. Word Tokenization & Synonym Mapping\n"
                    + "3. Stop-Word Filtering & Suffix Stemming\n"
                    + "4. TF-IDF Vectorization & Cosine Similarity scoring against trained FAQs."
                )
            ));

            intents.add(new Intent("java_programming",
                Arrays.asList("what is java", "tell me about java programming", "why use java", "features of java"),
                Collections.singletonList(
                    "Java is a high-level, class-based, object-oriented programming language designed to be platform-independent ('Write Once, Run Anywhere' via the JVM). It is widely used for enterprise backends, Android apps, and AI systems."
                )
            ));

            intents.add(new Intent("machine_learning",
                Arrays.asList("what is machine learning", "explain artificial intelligence", "how does ml work", "ai vs ml"),
                Collections.singletonList(
                    "Artificial Intelligence (AI) is the broad science of mimicking human cognitive abilities, while Machine Learning (ML) is a subset of AI that enables systems to learn patterns from data (like my TF-IDF vector space model) without being explicitly hardcoded for every sentence."
                )
            ));

            intents.add(new Intent("internship_perks",
                Arrays.asList("what are the benefits of internship", "do i get a certificate", "internship perks", "price of internship"),
                Collections.singletonList(
                    "Completing your CodeAlpha tasks builds real-world portfolio projects, sharpens your software engineering skills, and earns you a verified Completion Certificate and Letter of Recommendation!"
                )
            ));

            intents.add(new Intent("bot_identity",
                Arrays.asList("who are you", "what is your name", "who created you", "are you a human or robot"),
                Collections.singletonList(
                    "I am the CodeAlpha AI Assistant—a Java-based hybrid NLP Chatbot built using Swing, custom word stemming, rule-based entity extraction, and TF-IDF Cosine Similarity."
                )
            ));

            intents.add(new Intent("joke",
                Arrays.asList("tell me a joke", "make me laugh", "funny joke", "programming joke"),
                Arrays.asList(
                    "Why do Java developers wear glasses? Because they can't C#! 😄",
                    "There are 10 types of people in the world: those who understand binary, and those who don't!",
                    "A SQL query walks into a bar, walks up to two tables and asks: 'Can I join you?'"
                )
            ));

            intents.add(new Intent("gratitude",
                Arrays.asList("thank you", "thanks a lot", "appreciate it", "helpful"),
                Arrays.asList(
                    "You're very welcome! Let me know if you have any more questions.",
                    "Happy to help! Feel free to test my NLP engine with more queries."
                )
            ));

            intents.add(new Intent("goodbye",
                Arrays.asList("goodbye", "bye", "see you later", "exit", "quit"),
                Arrays.asList(
                    "Goodbye! Best of luck with your CodeAlpha Internship project!",
                    "See you later! Keep coding and innovating."
                )
            ));
        }

        /**
         * Dynamically adds a new FAQ Intent and retrains the TF-IDF vector space
         */
        public void learnNewIntent(String tag, List<String> patterns, List<String> responses) {
            for (Intent existing : intents) {
                if (existing.tag.equalsIgnoreCase(tag)) {
                    existing.patterns.addAll(patterns);
                    existing.responses.addAll(responses);
                    trainTfIdfModel();
                    return;
                }
            }
            intents.add(new Intent(tag, patterns, responses));
            trainTfIdfModel();
        }

        /**
         * Computes Inverse Document Frequency (IDF) and builds normalized TF-IDF vectors for every pattern
         */
        private void trainTfIdfModel() {
            idfScores.clear();
            trainedVectors.clear();

            List<List<String>> allDocs = new ArrayList<>();
            List<String> docTags = new ArrayList<>();
            List<String> rawPatterns = new ArrayList<>();

            for (Intent intent : intents) {
                for (String pattern : intent.patterns) {
                    List<String> stems = NLPPipeline.preprocess(pattern);
                    if (!stems.isEmpty()) {
                        allDocs.add(stems);
                        docTags.add(intent.tag);
                        rawPatterns.add(pattern);
                    }
                }
            }

            int totalDocs = allDocs.size();
            Map<String, Integer> docFreq = new HashMap<>();

            for (List<String> doc : allDocs) {
                Set<String> uniqueTerms = new HashSet<>(doc);
                for (String term : uniqueTerms) {
                    docFreq.put(term, docFreq.getOrDefault(term, 0) + 1);
                }
            }

            // Smooth IDF formula: log((1 + N) / (1 + df)) + 1.0
            for (Map.Entry<String, Integer> entry : docFreq.entrySet()) {
                double idf = Math.log((1.0 + totalDocs) / (1.0 + entry.getValue())) + 1.0;
                idfScores.put(entry.getKey(), idf);
            }

            // Build TF-IDF vector for each training pattern
            for (int i = 0; i < allDocs.size(); i++) {
                Map<String, Double> vec = computeTfIdfVector(allDocs.get(i));
                trainedVectors.add(new PatternVector(docTags.get(i), rawPatterns.get(i), vec));
            }
        }

        private Map<String, Double> computeTfIdfVector(List<String> stems) {
            Map<String, Integer> tfMap = new HashMap<>();
            for (String stem : stems) {
                tfMap.put(stem, tfMap.getOrDefault(stem, 0) + 1);
            }
            Map<String, Double> tfidf = new HashMap<>();
            for (Map.Entry<String, Integer> entry : tfMap.entrySet()) {
                String term = entry.getKey();
                double tf = (double) entry.getValue() / stems.size();
                double idf = idfScores.getOrDefault(term, 1.0);
                tfidf.put(term, tf * idf);
            }
            return tfidf;
        }

        /**
         * Computes Cosine Similarity between two sparse TF-IDF vectors
         */
        private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
            if (v1.isEmpty() || v2.isEmpty()) return 0.0;
            double dotProduct = 0.0;
            double normA = 0.0;
            double normB = 0.0;

            for (Map.Entry<String, Double> e : v1.entrySet()) {
                double valA = e.getValue();
                normA += valA * valA;
                if (v2.containsKey(e.getKey())) {
                    dotProduct += valA * v2.get(e.getKey());
                }
            }
            for (double valB : v2.values()) {
                normB += valB * valB;
            }
            if (normA == 0.0 || normB == 0.0) return 0.0;
            return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        }

        /**
         * Generates a response using a Hybrid Rule-Based + Machine Learning TF-IDF approach
         */
        public BotResponse generateResponse(String userInput) {
            List<String> stems = NLPPipeline.preprocess(userInput);
            String nlpTrace = "Raw Input: \"" + userInput + "\"\n"
                    + "Normalized Stems: " + stems.toString();

            // 1. Check Rule-Based Conversational Memory & Utility Evaluators first
            BotResponse ruleMatch = evaluateRuleBasedLogic(userInput, nlpTrace);
            if (ruleMatch != null) {
                return ruleMatch;
            }

            // 2. Machine Learning TF-IDF Vector Space Cosine Similarity Search
            Map<String, Double> queryVector = computeTfIdfVector(stems);
            String bestIntentTag = "unknown";
            String bestMatchedPattern = "";
            double highestSimilarity = 0.0;

            for (PatternVector pv : trainedVectors) {
                double sim = cosineSimilarity(queryVector, pv.tfidfVector);
                if (sim > highestSimilarity) {
                    highestSimilarity = sim;
                    bestIntentTag = pv.intentTag;
                    bestMatchedPattern = pv.rawPattern;
                }
            }

            // Threshold check for confidence
            if (highestSimilarity >= 0.28) {
                String reply = pickResponseForIntent(bestIntentTag);
                if (rememberedUserName != null && bestIntentTag.equals("greeting")) {
                    reply = "Hello again, " + rememberedUserName + "! How can I help you today?";
                }
                String detailedTrace = nlpTrace + "\nClosest Pattern: \"" + bestMatchedPattern + "\"";
                return new BotResponse(reply, bestIntentTag, highestSimilarity, "TF-IDF Cosine Similarity (ML)", detailedTrace);
            }

            // 3. Fallback when confidence is below threshold
            String fallbackReply = "I'm not quite sure I understand that yet (Confidence: "
                    + String.format("%.0f%%", highestSimilarity * 100) + ").\n"
                    + "You can ask me about CodeAlpha, Java, AI/NLP, math expressions, or click '+ Train Bot' to teach me the answer!";
            return new BotResponse(fallbackReply, "fallback_unknown", highestSimilarity, "Fallback Handler", nlpTrace);
        }

        /**
         * Rule-Based Engine for Entity Extraction (Names), Arithmetic Math, and Live Date/Time
         */
        private BotResponse evaluateRuleBasedLogic(String input, String nlpTrace) {
            String lower = input.toLowerCase().trim();

            // Rule A: Name Entity Extraction ("My name is Alex" / "Call me Sarah")
            Pattern namePattern = Pattern.compile("(?:my name is|i am|call me)\\s+([a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
            Matcher nameMatcher = namePattern.matcher(input);
            if (nameMatcher.find()) {
                String extracted = nameMatcher.group(1);
                if (! Arrays.asList("fine", "good", "okay", "great", "asking", "here").contains(extracted.toLowerCase())) {
                    rememberedUserName = extracted.substring(0, 1).toUpperCase() + extracted.substring(1).toLowerCase();
                    return new BotResponse(
                        "Nice to meet you, " + rememberedUserName + "! I have stored your name in my conversational memory.",
                        "entity_name_capture", 1.0, "Rule-Based Regex NER", nlpTrace + "\nEntity: USER_NAME=" + rememberedUserName
                    );
                }
            }

            // Rule B: Recall User Name ("What is my name?")
            if (lower.contains("what is my name") || lower.contains("do you know my name") || lower.contains("who am i")) {
                String reply = (rememberedUserName != null)
                        ? "Your name is " + rememberedUserName + "!"
                        : "You haven't told me your name yet! Try saying 'My name is [Your Name]'.";
                return new BotResponse(reply, "memory_name_recall", 1.0, "Rule-Based Memory Lookup", nlpTrace);
            }

            // Rule C: Arithmetic Expression Evaluator ("Calculate 45 * 12", "what is 120 / 4")
            Pattern mathPattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*([+\\-*/^])\\s*(-?\\d+(?:\\.\\d+)?)");
            Matcher mathMatcher = mathPattern.matcher(input);
            if (mathMatcher.find()) {
                try {
                    double a = Double.parseDouble(mathMatcher.group(1));
                    String op = mathMatcher.group(2);
                    double b = Double.parseDouble(mathMatcher.group(3));
                    double result = 0;
                    switch (op) {
                        case "+": result = a + b; break;
                        case "-": result = a - b; break;
                        case "*": result = a * b; break;
                        case "/":
                            if (b == 0) return new BotResponse("Division by zero is undefined!", "math_evaluation", 1.0, "Rule-Based Math Engine", nlpTrace);
                            result = a / b;
                            break;
                        case "^": result = Math.pow(a, b); break;
                    }
                    String formatted = (result == (long) result) ? String.format("%d", (long) result) : String.format("%.4f", result);
                    return new BotResponse(
                        "🧮 Calculation Result: " + mathMatcher.group(0) + " = " + formatted,
                        "math_evaluation", 1.0, "Rule-Based Math Parser", nlpTrace
                    );
                } catch (NumberFormatException ignored) {}
            }

            // Rule D: Current Date & Time Queries
            if (lower.contains("what time") || lower.contains("current time")) {
                String now = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                return new BotResponse("⌚ The current system time is " + now + ".", "system_time", 1.0, "Rule-Based Temporal Rule", nlpTrace);
            }
            if (lower.contains("what date") || lower.contains("today's date") || lower.contains("what day is today")) {
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
                return new BotResponse("📅 Today's date is " + today + ".", "system_date", 1.0, "Rule-Based Temporal Rule", nlpTrace);
            }

            return null;
        }

        private String pickResponseForIntent(String tag) {
            for (Intent intent : intents) {
                if (intent.tag.equals(tag) && !intent.responses.isEmpty()) {
                    return intent.responses.get(random.nextInt(intent.responses.size()));
                }
            }
            return "I found a matching intent (" + tag + "), but no response is configured.";
        }

        public int getIntentCount() {
            return intents.size();
        }

        public int getVocabularySize() {
            return idfScores.size();
        }
    }

    // =========================================================================
    // APPLICATION ENTRY POINT
    // =========================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AIChatbot app = new AIChatbot();
            app.setVisible(true);
        });
    }
}