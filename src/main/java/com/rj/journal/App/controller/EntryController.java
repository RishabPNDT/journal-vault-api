package com.rj.journal.App.controller;

import com.rj.journal.App.dto.EntryRequest;
import com.rj.journal.App.entity.JournalEntry;
import com.rj.journal.App.service.JournalEntryService;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
public class EntryController {
    private final JournalEntryService entries;

    public EntryController(JournalEntryService entries) { this.entries = entries; }

    @GetMapping
    public List<JournalEntry> all(@AuthenticationPrincipal UserDetails user) {
        return entries.findAllForUser(user.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntry create(@Valid @RequestBody EntryRequest request, @AuthenticationPrincipal UserDetails user) {
        return entries.create(request, user.getUsername());
    }

    @PutMapping("/{id}")
    public JournalEntry update(@PathVariable String id, @Valid @RequestBody EntryRequest request,
                               @AuthenticationPrincipal UserDetails user) {
        return entries.update(parseId(id), request, user.getUsername());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, @AuthenticationPrincipal UserDetails user) {
        entries.delete(parseId(id), user.getUsername());
    }

    private ObjectId parseId(String id) {
        if (!ObjectId.isValid(id)) throw new IllegalArgumentException("Invalid entry id");
        return new ObjectId(id);
    }
}
