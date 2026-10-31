package com.companywiseprep.insights;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companywiseprep.web.NotFoundException;

/** The behavioural story bank: STAR stories linked to the questions they answer. */
@Service
public class StoryService {

	private final StoryRepository stories;
	private final Clock clock;

	public StoryService(StoryRepository stories, Clock clock) {
		this.stories = stories;
		this.clock = clock;
	}

	public record Draft(String title, String situation, String task, String action, String result, String learned,
			List<String> questions) {
	}

	public record StoryView(long id, String title, String situation, String task, String action, String result,
			String learned, List<String> questions, Instant createdAt, Instant updatedAt) {
	}

	@Transactional(readOnly = true)
	public List<StoryView> list() {
		return stories.findAll().stream().sorted(Comparator.comparing(Story::getUpdatedAt).reversed()).map(StoryService::view)
				.toList();
	}

	@Transactional
	public StoryView create(Draft d) {
		Story s = new Story();
		s.setCreatedAt(Instant.now(clock));
		return view(stories.save(apply(s, d)));
	}

	@Transactional
	public StoryView update(long id, Draft d) {
		return view(stories.save(apply(find(id), d)));
	}

	@Transactional
	public void delete(long id) {
		stories.delete(find(id));
	}

	private Story find(long id) {
		return stories.findById(id).orElseThrow(() -> new NotFoundException("No story " + id));
	}

	private Story apply(Story s, Draft d) {
		s.setTitle(d.title().strip());
		s.setSituation(d.situation());
		s.setTask(d.task());
		s.setAction(d.action());
		s.setResult(d.result());
		s.setLearned(d.learned());
		s.setQuestions(d.questions() == null ? "" : String.join(",", d.questions().stream().map(String::strip).distinct().toList()));
		s.setUpdatedAt(Instant.now(clock));
		return s;
	}

	static List<String> slugs(Story s) {
		return s.getQuestions() == null || s.getQuestions().isBlank() ? List.of() : Arrays.asList(s.getQuestions().split(","));
	}

	private static StoryView view(Story s) {
		return new StoryView(s.getId(), s.getTitle(), s.getSituation(), s.getTask(), s.getAction(), s.getResult(),
				s.getLearned(), slugs(s), s.getCreatedAt(), s.getUpdatedAt());
	}
}
