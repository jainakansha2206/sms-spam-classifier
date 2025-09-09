package com.example.sms_spam_classifier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sms_spam_classifier.model.MessageRecord;

@Repository
public interface MessageRecordRepository extends JpaRepository<MessageRecord, Long> {

}
