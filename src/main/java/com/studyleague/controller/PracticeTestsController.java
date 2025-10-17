package com.studyleague.controller;

import com.studyleague.model.PracticeSubmission;
import com.studyleague.model.PracticeTest;
import com.studyleague.model.User;
import com.studyleague.repository.PracticeSubmissionRepository;
import com.studyleague.repository.PracticeTestRepository;
import com.studyleague.repository.UserRepository;
import com.studyleague.service.Judge0Client;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/practice")
public class PracticeTestsController {
    private final PracticeTestRepository practiceTestRepository;
    private final PracticeSubmissionRepository practiceSubmissionRepository;
    private final UserRepository userRepository;

    public PracticeTestsController(PracticeTestRepository practiceTestRepository,
                                   PracticeSubmissionRepository practiceSubmissionRepository,
                                   UserRepository userRepository) {
        this.practiceTestRepository = practiceTestRepository;
        this.practiceSubmissionRepository = practiceSubmissionRepository;
        this.userRepository = userRepository;
    }

    public record TestCase(String input, String expectedOutput) {}
    public record CreatePracticeTest(@NotBlank String courseId, @NotBlank String type, @NotBlank String title,
                                     String prompt, String starterCode, List<String> options, Integer correctOptionIndex,
                                     String language, List<TestCase> testCases) {}
    public record SubmitCoding(String codeAnswer) {}
    public record SubmitMcq(int selectedOptionIndex) {}

    @GetMapping("/courses/{courseId}")
    public List<Map<String, Object>> listByCourse(@PathVariable("courseId") String courseId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (PracticeTest t : practiceTestRepository.findByCourseIdOrderByCreatedAtDesc(courseId)) {
            out.add(testDto(t));
        }
        return out;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreatePracticeTest req) {
        PracticeTest t = new PracticeTest();
        t.setCourseId(req.courseId());
        t.setType(req.type());
        t.setTitle(req.title());
        t.setPrompt(req.prompt());
        t.setStarterCode(req.starterCode());
        if (req.options() != null) t.getOptions().addAll(req.options());
        t.setLanguage(req.language());
        if (req.testCases() != null) {
            for (TestCase c : req.testCases()) {
                t.getTestCases().add(new com.studyleague.model.PracticeTestCase(c.input(), c.expectedOutput()));
            }
        }
        t.setCorrectOptionIndex(req.correctOptionIndex());
        t = practiceTestRepository.save(t);
        return ResponseEntity.ok(testDto(t));
    }

    @PostMapping("/{testId}/submit/coding")
    public ResponseEntity<?> submitCoding(@PathVariable("testId") String testId, @RequestBody SubmitCoding req,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        var tOpt = practiceTestRepository.findById(testId);
        if (tOpt.isEmpty()) return ResponseEntity.notFound().build();
        PracticeTest t = tOpt.get();
        PracticeSubmission s = new PracticeSubmission();
        s.setPracticeTestId(testId);
        s.setCodeAnswer(req.codeAnswer());
        attachStudentFromToken(s, authHeader);
        if (s.getStudentId() == null) return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));

        // Evaluate against test cases using Judge0 CE
        int passed = 0;
        StringBuilder stderrAgg = new StringBuilder();
        if (t.getTestCases() != null && t.getLanguage() != null) {
            for (var c : t.getTestCases()) {
                var res = Judge0Client.run(t.getLanguage(), req.codeAnswer(), c.getInput());
                String stdout = res.getOrDefault("stdout", "").toString().trim();
                String expected = c.getExpectedOutput() == null ? "" : c.getExpectedOutput().trim();
                if (stdout.equals(expected)) passed++;
                if (res.get("stderr") != null) stderrAgg.append(res.get("stderr")).append('\n');
            }
            s.setPassed(passed);
            s.setTotal(t.getTestCases().size());
            s.setStderr(stderrAgg.toString());
        }

        if (s.getTotal() != null) {
            s.setScore(s.getPassed());
            s.setMaxScore(s.getTotal());
        }
        s = practiceSubmissionRepository.save(s);
        return ResponseEntity.ok(Map.of(
                "_id", s.getId(),
                "passed", s.getPassed(),
                "total", s.getTotal(),
                "score", s.getScore(),
                "maxScore", s.getMaxScore()
        ));
    }

    @PostMapping("/{testId}/submit/mcq")
    public ResponseEntity<?> submitMcq(@PathVariable("testId") String testId, @RequestBody SubmitMcq req,
                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<PracticeTest> tOpt = practiceTestRepository.findById(testId);
        if (tOpt.isEmpty()) return ResponseEntity.notFound().build();
        PracticeTest t = tOpt.get();
        PracticeSubmission s = new PracticeSubmission();
        s.setPracticeTestId(testId);
        s.setSelectedOptionIndex(req.selectedOptionIndex());
        attachStudentFromToken(s, authHeader);
        if (s.getStudentId() == null) return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        if (t.getCorrectOptionIndex() != null) {
            s.setCorrect(Objects.equals(t.getCorrectOptionIndex(), req.selectedOptionIndex()));
        }
        s = practiceSubmissionRepository.save(s);
        return ResponseEntity.ok(Map.of("_id", s.getId(), "correct", s.getCorrect()));
    }

    @GetMapping("/{testId}/submissions")
    public List<Map<String, Object>> submissions(@PathVariable("testId") String testId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (PracticeSubmission s : practiceSubmissionRepository.findByPracticeTestId(testId)) {
            out.add(submissionDto(s));
        }
        return out;
    }

    private void attachStudentFromToken(PracticeSubmission s, String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            String userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
            Optional<User> userOpt = userRepository.findById(userId);
            userOpt.ifPresent(user -> {
                s.setStudentId(user.getId());
                s.setStudentName(user.getName());
            });
        }
    }

    private static Map<String, Object> testDto(PracticeTest t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_id", t.getId());
        m.put("courseId", t.getCourseId());
        m.put("type", t.getType());
        m.put("title", t.getTitle());
        m.put("prompt", t.getPrompt());
        m.put("starterCode", t.getStarterCode());
        m.put("language", t.getLanguage());
        m.put("testCases", t.getTestCases());
        m.put("options", t.getOptions());
        m.put("correctOptionIndex", t.getCorrectOptionIndex());
        return m;
    }

    private static Map<String, Object> submissionDto(PracticeSubmission s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_id", s.getId());
        m.put("practiceTestId", s.getPracticeTestId());
        m.put("studentName", s.getStudentName());
        m.put("submittedAt", s.getSubmittedAt());
        m.put("codeAnswer", s.getCodeAnswer());
        m.put("selectedOptionIndex", s.getSelectedOptionIndex());
        m.put("correct", s.getCorrect());
        m.put("passed", s.getPassed());
        m.put("total", s.getTotal());
        m.put("score", s.getScore());
        m.put("maxScore", s.getMaxScore());
        return m;
    }
}


