package com.rj.journal.App.repository;
import com.rj.journal.App.entity.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JournalEntryRepository extends MongoRepository<JournalEntry, ObjectId> {

}
