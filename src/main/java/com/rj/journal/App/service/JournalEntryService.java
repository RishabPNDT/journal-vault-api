package com.rj.journal.App.service;

import com.rj.journal.App.dto.EntryRequest;
import com.rj.journal.App.entity.JournalEntry;
import com.rj.journal.App.entity.User;
import com.rj.journal.App.exception.EntryNotFoundException;
import com.rj.journal.App.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JournalEntryService {
    private final JournalEntryRepository entries;
    private final UserService users;

    public JournalEntryService(JournalEntryRepository entries, UserService users) {
        this.entries = entries;
        this.users = users;
    }

    public JournalEntry create(EntryRequest request, String username) {
        User user = requireUser(username);
        JournalEntry entry = new JournalEntry();
        entry.setTitle(request.title());
        entry.setContent(request.content());
        entry.setDate(LocalDateTime.now());
        JournalEntry saved = entries.save(entry);
        user.getJournalEntries().add(saved);
        users.saveUser(user);
        return saved;
    }

    public List<JournalEntry> findAllForUser(String username) {
        return requireUser(username).getJournalEntries().stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate())).toList();
    }

    public JournalEntry update(ObjectId id, EntryRequest request, String username) {
        JournalEntry entry = ownedEntry(id, username);
        entry.setTitle(request.title());
        entry.setContent(request.content());
        return entries.save(entry);
    }

    public void delete(ObjectId id, String username) {
        User user = requireUser(username);
        JournalEntry entry = user.getJournalEntries().stream().filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new EntryNotFoundException("Entry not found"));
        user.getJournalEntries().remove(entry);
        users.saveUser(user);
        entries.deleteById(id);
    }

    private User requireUser(String username) {
        User user = users.findByUserName(username);
        if (user == null) throw new IllegalArgumentException("User not found");
        return user;
    }

    // Deliberately the SAME exception/message whether the entry doesn't exist
    // at all or exists but belongs to someone else - a 404 either way, so a
    // caller probing IDs can't distinguish "not yours" from "doesn't exist".
    private JournalEntry ownedEntry(ObjectId id, String username) {
        return requireUser(username).getJournalEntries().stream().filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new EntryNotFoundException("Entry not found"));
    }
}
