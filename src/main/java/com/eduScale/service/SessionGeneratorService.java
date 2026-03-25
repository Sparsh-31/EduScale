package com.eduScale.service;

import com.eduScale.domain.Session;
import java.util.List;

public interface SessionGeneratorService {

    Session startSession(String userId, String objectiveId);

    List<String> generateActivitySequence(String userId, String objectiveId);
}

