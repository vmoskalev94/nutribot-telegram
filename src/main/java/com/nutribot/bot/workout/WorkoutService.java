package com.nutribot.bot.workout;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final StrengthExerciseRepository strengthExerciseRepository;
    private final StrengthSetRepository strengthSetRepository;
    private final CardioWorkoutDetailsRepository cardioDetailsRepository;

    // ===== Создание черновика тренировки (по типу) =====

    @Transactional
    public Workout createWorkout(Long userId, WorkoutType type) {
        Workout workout = Workout.builder()
                .userId(userId)
                .type(type)
                .startedAt(OffsetDateTime.now())
                .status(WorkoutStatus.DRAFT)
                .build();
        return workoutRepository.save(workout);
    }

    // ===== Силовая: добавить упражнение =====

    @Transactional
    public StrengthExercise addStrengthExercise(Long workoutId, String name) {
        Workout workout = getWorkoutRequired(workoutId);
        if (workout.getType() != WorkoutType.STRENGTH) {
            throw new IllegalStateException("Workout " + workoutId + " is not STRENGTH");
        }

        int maxOrder = strengthExerciseRepository.findMaxOrderIndexByWorkoutId(workoutId);
        int nextOrder = maxOrder + 1;

        StrengthExercise exercise = StrengthExercise.builder()
                .workoutId(workoutId)
                .name(name)
                .orderIndex(nextOrder)
                .build();

        return strengthExerciseRepository.save(exercise);
    }

    // ===== Силовая: добавить подход =====

    @Transactional
    public StrengthSet addStrengthSet(Long exerciseId, double weight, int reps, Integer rir) {
        StrengthExercise exercise = strengthExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + exerciseId));

        Workout workout = getWorkoutRequired(exercise.getWorkoutId());
        if (workout.getType() != WorkoutType.STRENGTH) {
            throw new IllegalStateException("Workout " + workout.getId() + " is not STRENGTH");
        }

        int maxOrder = strengthSetRepository.findMaxOrderIndexByExerciseId(exerciseId);
        int nextOrder = maxOrder + 1;

        StrengthSet set = StrengthSet.builder()
                .exerciseId(exerciseId)
                .weight(weight)
                .reps(reps)
                .rir(rir)
                .orderIndex(nextOrder)
                .build();

        return strengthSetRepository.save(set);
    }

    // ===== Кардио: заполнить детали =====

    @Transactional
    public CardioWorkoutDetails fillCardioDetails(Long workoutId,
                                                  String activityType,
                                                  int durationMin,
                                                  Double distanceKm,
                                                  String intensity,
                                                  Integer rpe) {

        Workout workout = getWorkoutRequired(workoutId);
        if (workout.getType() != WorkoutType.CARDIO) {
            throw new IllegalStateException("Workout " + workoutId + " is not CARDIO");
        }

        CardioWorkoutDetails details = cardioDetailsRepository.findByWorkoutId(workoutId)
                .orElseGet(() -> CardioWorkoutDetails.builder()
                        .workoutId(workoutId)
                        .build());

        details.setActivityType(activityType);
        details.setDurationMin(durationMin);
        details.setDistanceKm(distanceKm);
        details.setIntensity(intensity);
        details.setRpe(rpe);

        return cardioDetailsRepository.save(details);
    }

    // ===== Завершить тренировку (сохранить) =====

    @Transactional
    public void finishWorkout(Long workoutId) {
        Workout workout = getWorkoutRequired(workoutId);
        if (workout.getStatus() == WorkoutStatus.DELETED) {
            throw new IllegalStateException("Workout already deleted: " + workoutId);
        }
        if (workout.getStatus() == WorkoutStatus.ACTIVE) {
            // уже завершена — можно просто игнорировать
            return;
        }
        workout.setStatus(WorkoutStatus.ACTIVE);
        workoutRepository.save(workout);
    }

    // ===== Удалить тренировку (soft-delete) =====

    @Transactional
    public void deleteWorkout(Long workoutId) {
        Workout workout = getWorkoutRequired(workoutId);
        if (workout.getStatus() == WorkoutStatus.DELETED) {
            return;
        }
        workout.setStatus(WorkoutStatus.DELETED);
        workoutRepository.save(workout);
    }

    // ===== Список тренировок пользователя (с пагинацией) =====

    @Transactional(readOnly = true)
    public List<Workout> listUserWorkouts(Long userId, int page, int pageSize) {
        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid page/size: " + page + "/" + pageSize);
        }

        long offset = (long) page * pageSize;
        int limit = pageSize + 1; // на 1 больше — чтобы понять, есть ли следующая страница

        return workoutRepository.findPageByUserAndStatusOrderByStartedAtDesc(
                userId,
                WorkoutStatus.ACTIVE,
                limit,
                offset
        );
    }


    // ===== Подробности тренировки =====

    @Transactional(readOnly = true)
    public WorkoutDetails getWorkoutDetails(Long userId, Long workoutId) {
        Workout workout = getWorkoutRequired(workoutId);

        if (!workout.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Workout " + workoutId + " does not belong to user " + userId);
        }
        if (workout.getStatus() == WorkoutStatus.DELETED) {
            throw new IllegalStateException("Workout deleted: " + workoutId);
        }

        if (workout.getType() == WorkoutType.STRENGTH) {
            List<StrengthExercise> exercises =
                    strengthExerciseRepository.findByWorkoutIdOrderByOrderIndex(workoutId);

            List<StrengthExerciseWithSets> res = exercises.stream()
                    .map(ex -> StrengthExerciseWithSets.builder()
                            .exercise(ex)
                            .sets(strengthSetRepository.findByExerciseIdOrderByOrderIndex(ex.getId()))
                            .build())
                    .toList();

            return WorkoutDetails.builder()
                    .workout(workout)
                    .strengthExercises(res)
                    .cardioDetails(null)
                    .build();
        } else {
            CardioWorkoutDetails details = cardioDetailsRepository.findByWorkoutId(workoutId)
                    .orElse(null);

            return WorkoutDetails.builder()
                    .workout(workout)
                    .strengthExercises(List.of())
                    .cardioDetails(details)
                    .build();
        }
    }

    // ===== Вспомогательный метод =====

    private Workout getWorkoutRequired(Long workoutId) {
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> new IllegalArgumentException("Workout not found: " + workoutId));
    }
}
