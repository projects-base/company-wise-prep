package com.companywiseprep.notes;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.companywiseprep.notes.CardService.CardView;

/** The daily review: the queue of due cards, grading them through {@link Scheduler}, and the streak and stats. */
@Service
public class ReviewService {

	/** "Cards you know well": the interval has reached three weeks. */
	static final long KNOWN_WELL_MINUTES = 21 * Scheduler.DAY;
	static final int WEEK_DAYS = 7;

	private final ReviewCardRepository cards;
	private final ReviewLogRepository logs;
	private final CardService cardViews;
	private final Clock clock;
	private final TransactionTemplate tx;

	public ReviewService(ReviewCardRepository cards, ReviewLogRepository logs, CardService cardViews, Clock clock,
			PlatformTransactionManager transactions) {
		this.cards = cards;
		this.logs = logs;
		this.cardViews = cardViews;
		this.clock = clock;
		this.tx = new TransactionTemplate(transactions);
	}

	public record DayDue(LocalDate date, int count) {
	}

	/**
	 * @param streak consecutive days with at least one review, ending today, or yesterday when today has none yet
	 * @param week cards due on each of the next seven days; today's count includes the overdue ones
	 */
	public record Stats(int dueNow, int streak, boolean reviewedToday, int knownWell, int active, List<DayDue> week) {
	}

	/** Active cards due now, the longest-waiting first. */
	@Transactional(readOnly = true)
	public List<CardView> queue() {
		return cardViews.views(cards.findBySuspendedFalseAndDueLessThanEqualOrderByDueAscIdAsc(Instant.now(clock)));
	}

	/**
	 * Grades a due card. A card that is not due (already graded, or suspended) is refused with 409, so a double click
	 * cannot schedule it twice; the version column catches two grades racing.
	 */
	public CardView grade(long id, Grade grade) {
		try {
			return tx.execute(status -> {
				ReviewCard c = cardViews.find(id);
				Instant now = Instant.now(clock);
				if (c.isSuspended() || c.getDue().isAfter(now)) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Card " + id + " is not due");
				}
				c.apply(Scheduler.next(c.state(), grade, now));
				c.setUpdatedAt(now);
				ReviewCard saved = cards.saveAndFlush(c);
				logs.save(new ReviewLog(saved.getId(), grade, now, saved.getIntervalMinutes(), saved.getEase()));
				return cardViews.view(saved);
			});
		} catch (OptimisticLockingFailureException raced) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Card " + id + " was graded at the same time");
		}
	}

	@Transactional(readOnly = true)
	public Stats stats() {
		Instant now = Instant.now(clock);
		ZoneId zone = clock.getZone();
		LocalDate today = LocalDate.ofInstant(now, zone);
		List<ReviewCard> active = cards.findBySuspendedFalse();
		int dueNow = (int) active.stream().filter(c -> !c.getDue().isAfter(now)).count();
		int knownWell = (int) active.stream().filter(c -> c.getIntervalMinutes() >= KNOWN_WELL_MINUTES).count();
		List<DayDue> week = new ArrayList<>();
		for (int i = 0; i < WEEK_DAYS; i++) {
			LocalDate day = today.plusDays(i);
			Instant from = day.atStartOfDay(zone).toInstant();
			Instant to = day.plusDays(1).atStartOfDay(zone).toInstant();
			boolean first = i == 0;
			week.add(new DayDue(day, (int) active.stream()
					.filter(c -> c.getDue().isBefore(to) && (first || !c.getDue().isBefore(from))).count()));
		}
		Set<LocalDate> days = logs.reviewTimes().stream().map(t -> LocalDate.ofInstant(t, zone)).collect(Collectors.toSet());
		boolean reviewedToday = days.contains(today);
		int streak = 0;
		for (LocalDate d = reviewedToday ? today : today.minusDays(1); days.contains(d); d = d.minusDays(1)) streak++;
		return new Stats(dueNow, streak, reviewedToday, knownWell, active.size(), week);
	}
}
