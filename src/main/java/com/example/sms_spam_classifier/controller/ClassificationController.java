package com.example.sms_spam_classifier.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.sms_spam_classifier.model.MessageRecord;
import com.example.sms_spam_classifier.repository.MessageRecordRepository;
import com.example.sms_spam_classifier.service.WekaModelService;

@RestController
@RequestMapping("/api")
public class ClassificationController {
	
	private final WekaModelService modelService;
	  private final MessageRecordRepository repo;

	  public ClassificationController(WekaModelService modelService, MessageRecordRepository repo) {
	    this.modelService = modelService;
	    this.repo = repo;
	  }
	  
	  @PostMapping("/classify")
	  public ResponseEntity<?> classify(@RequestBody Map<String, String> body) {
	    try {
	      String text = body.get("text");
	      if (text == null || text.isBlank()) return ResponseEntity.badRequest().body(Map.of("error","text missing"));

	      String label = modelService.classify(text);
	      MessageRecord rec = new MessageRecord(text, label, LocalDateTime.now());
	      repo.save(rec);
	      return ResponseEntity.ok(Map.of("label", label));
	    } catch (Exception e) {
	      e.printStackTrace();
	      return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
	    }
	  }

}
