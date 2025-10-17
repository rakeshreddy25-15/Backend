package com.studyleague.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Client {
    // Public CE instance (rate-limited)
    private static final String BASE_URL = "https://ce.judge0.com";
    private static final String SUBMISSIONS = BASE_URL + "/submissions/?base64_encoded=false&wait=true";

    private static final Map<String, Integer> LANGUAGE_TO_ID = Map.of(
            "cpp", 54, // C++ (GCC 9.2.0)
            "c", 50,
            "java", 62, // Java (OpenJDK 13)
            "python", 71 // Python (3.8.1)
    );

    public static Map<String, Object> run(String language, String sourceCode, String stdin) {
        Integer langId = LANGUAGE_TO_ID.get(language);
        if (langId == null) {
            Map<String, Object> m = new HashMap<>();
            m.put("stderr", "Unsupported language: " + language);
            return m;
        }
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("language_id", langId);
        body.put("source_code", sourceCode);
        body.put("stdin", stdin == null ? "" : stdin);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> resp = rt.postForEntity(SUBMISSIONS, req, Map.class);
            Map<String, Object> res = new HashMap<>();
            if (resp.getBody() != null) {
                Object stdout = resp.getBody().get("stdout");
                Object stderr = resp.getBody().get("stderr");
                res.put("stdout", stdout == null ? "" : stdout);
                res.put("stderr", stderr == null ? "" : stderr);
            }
            return res;
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("stderr", e.getMessage());
            return err;
        }
    }
}



