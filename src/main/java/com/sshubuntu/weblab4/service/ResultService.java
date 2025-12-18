package com.sshubuntu.weblab4.service;

import com.sshubuntu.weblab4.dto.PointResponse;
import com.sshubuntu.weblab4.entity.PointResult;
import com.sshubuntu.weblab4.entity.UserAccount;
import com.sshubuntu.weblab4.exception.InvalidPointException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class ResultService {

    private static final Set<Double> ALLOWED_X = Arrays.stream(new double[]{-3, -2, -1, 0, 1, 2, 3, 4, 5})
            .boxed().collect(Collectors.toSet());
    private static final Set<Double> ALLOWED_R = Arrays.stream(new double[]{-3, -2, -1, 0, 1, 2, 3, 4, 5})
            .boxed().collect(Collectors.toSet());

    @PersistenceContext(unitName = "PointsPU")
    private EntityManager entityManager;

    @Inject
    private PointAreaService pointAreaService;

    public PointResult registerPoint(UserAccount user, double x, double y, double r) {
        validateInput(x, y, r);
        boolean hit = pointAreaService.isHit(x, y, r);
        PointResult entity = new PointResult(user, x, y, r, hit);
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    public List<PointResult> fetchAllOrdered(UserAccount user) {
        return entityManager.createQuery(
                        "SELECT p FROM PointResult p WHERE p.user = :user ORDER BY p.creationTime DESC",
                        PointResult.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<PointResponse> toDto(List<PointResult> results) {
        return results.stream().map(PointResponse::from).toList();
    }

    private void validateInput(double x, double y, double r) {
        if (Double.isNaN(x) || Double.isInfinite(x)) throw new InvalidPointException("Введите X");
        if (!ALLOWED_X.contains(x)) throw new InvalidPointException("X должен быть значением из {-3..5}");

        if (Double.isNaN(y) || Double.isInfinite(y)) throw new InvalidPointException("Введите Y");
        if (y < -3 || y > 3) throw new InvalidPointException("Y должен быть в диапазоне [-3; 3]");

        if (Double.isNaN(r) || Double.isInfinite(r)) throw new InvalidPointException("Введите R");
        if (!ALLOWED_R.contains(r)) throw new InvalidPointException("R должен быть одним из {-3..5}");
        if (r <= 0) throw new InvalidPointException("Радиус должен быть положительным");
    }

    @Transactional
    public void deleteAllPoints(UserAccount user) {
        entityManager.createQuery("DELETE FROM PointResult p WHERE p.user = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    @Transactional
    public void deletePoint(UserAccount user, Long id) {
        entityManager.createQuery("DELETE FROM PointResult p WHERE p.user = :user AND p.id = :id")
                .setParameter("user", user)
                .setParameter("id", id)
                .executeUpdate();
    }
}




