package com.aiinterview.ai.service;

import java.util.List;

public interface OpenAiService {

    List<String> generateInterviewQuestions(String interviewTitle);
}
