package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.companywiseprep.notes.LessonCatalog.PlanWeek;
import com.companywiseprep.notes.LessonCatalog.Snapshot;

/** Plan weeks are numbered as on the Plan page: week 1 runs from the start to the first Sunday, then Monday to Sunday. */
class PlanWeekTest {

	private static Snapshot plan(LocalDate start, Map<String, LocalDate> firstPlanned) {
		return new Snapshot(Map.of(), Map.of(), Map.of(), firstPlanned, start);
	}

	@Test
	void aPlanStartingOnAMondayHasFullWeeksFromDayOne() {
		Snapshot s = plan(LocalDate.of(2026, 11, 23), Map.of("A", LocalDate.of(2026, 11, 29), "B", LocalDate.of(2026, 11, 30)));
		assertThat(s.week(1)).isEqualTo(new PlanWeek(1, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 29)));
		assertThat(s.week(2)).isEqualTo(new PlanWeek(2, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 6)));
		assertThat(s.weekOf("A")).map(PlanWeek::number).contains(1);
		assertThat(s.weekOf("B")).map(PlanWeek::number).contains(2);
	}

	@Test
	void aMidWeekStartMakesAShortFirstWeek() {
		Snapshot s = plan(LocalDate.of(2026, 11, 22), Map.of(
				"SUN", LocalDate.of(2026, 11, 22),
				"MON", LocalDate.of(2026, 11, 23),
				"NEXT_SUN", LocalDate.of(2026, 11, 29),
				"NEXT_MON", LocalDate.of(2026, 11, 30)));
		assertThat(s.week(1)).isEqualTo(new PlanWeek(1, LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 22)));
		assertThat(s.week(2)).isEqualTo(new PlanWeek(2, LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 29)));
		assertThat(s.weekOf("SUN")).map(PlanWeek::number).contains(1);
		assertThat(s.weekOf("MON")).map(PlanWeek::number).contains(2);
		assertThat(s.weekOf("NEXT_SUN")).map(PlanWeek::number).contains(2);
		assertThat(s.weekOf("NEXT_MON")).map(PlanWeek::number).contains(3);
		assertThat(s.lessonsIn(2)).containsExactlyInAnyOrder("MON", "NEXT_SUN");
	}

	@Test
	void aSessionBeforeThePlanStartsHasNoWeek() {
		Snapshot s = plan(LocalDate.of(2026, 11, 25), Map.of("EARLY", LocalDate.of(2026, 11, 20)));
		assertThat(s.weekOf("EARLY")).isEmpty();
		assertThat(s.lessonsIn(1)).isEmpty();
	}

	@Test
	void withoutAPlanThereAreNoWeeks() {
		Snapshot s = plan(null, Map.of());
		assertThat(s.weekOf("A")).isEmpty();
		assertThat(s.lessonsIn(1)).isEqualTo(List.of());
	}
}
