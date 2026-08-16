package com.example.catchcompass.catchlog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatchService {

    private final CatchRepository catchRepository;

    public CatchService(CatchRepository catchRepository) {
        this.catchRepository = catchRepository;
    }

    /**
     * All catches belonging to the given user, newest first.
     */
    public List<Catch> findJournal(Long userId) {
        return catchRepository.findJournal(userId);
    }

    /**
     * A single catch, but only if the given user owns it.
     *
     * @throws CatchNotFoundException if it does not exist or belongs to someone else
     */
    public Catch findOwned(Long id, Long userId) {
        return catchRepository.findOwned(id, userId)
                .orElseThrow(() -> new CatchNotFoundException(id));
    }
}
