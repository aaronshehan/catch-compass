package com.example.catchcompass.lure;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LureService {

    private final LureRepository lureRepository;

    public LureService(LureRepository lureRepository) {
        this.lureRepository = lureRepository;
    }

    public List<Lure> findTackleBox(Long userId) {
        return lureRepository.findByUserIdAndActiveTrueOrderByLureTypeAscBrandAsc(userId);
    }

    /**
     * @throws LureNotFoundException if it does not exist or belongs to someone else
     */
    public Lure findOwned(Long id, Long userId) {
        return lureRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new LureNotFoundException(id));
    }

    @Transactional
    public Lure create(Long userId, LureForm form) {
        Lure lure = new Lure(userId, form.getLureType());
        lure.setBrand(form.getBrand());
        lure.setModel(form.getModel());
        lure.setColor(form.getColor());
        lure.setSize(form.getSize());
        lure.setWeightGrams(form.getWeightGrams());
        lure.setPresentation(form.getPresentation());
        lure.setNotes(form.getNotes());
        return lureRepository.save(lure);
    }
}
