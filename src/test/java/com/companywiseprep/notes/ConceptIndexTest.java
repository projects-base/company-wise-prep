package com.companywiseprep.notes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.companywiseprep.notes.ConceptIndex.Concept;
import com.companywiseprep.notes.ConceptIndex.Item;

/** [[Concept]] links parsed from note text: normalisation, counts, related concepts, and the glossary that finds gaps. */
class ConceptIndexTest {

	private static Item item(long id, String... texts) {
		return new Item("HIGHLIGHT", id, "J1", List.of(texts));
	}

	@Test
	void linksAreFoundInAnyTextOfAnItem() {
		assertThat(ConceptIndex.links("Buckets: see [[HashMap]] and [[equals / hashCode contract]].", null))
				.containsExactly("HashMap", "equals / hashCode contract");
	}

	@Test
	void keysIgnoreCaseAndExtraSpaces() {
		assertThat(ConceptIndex.key("  Hash   Map ")).isEqualTo(ConceptIndex.key("hash map"));
		assertThat(ConceptIndex.key("HASHMAP")).isEqualTo("hashmap");
	}

	@Test
	void emptyOrUnclosedBracketsAreNotLinks() {
		assertThat(ConceptIndex.links("[[ ]] and [[open and [x] and [[a\nb]]", "")).isEmpty();
	}

	@Test
	void oneItemCountsOnceForAConceptEvenWhenItRepeatsIt() {
		ConceptIndex idx = ConceptIndex.of(List.of(item(1, "[[HashMap]] then [[hashmap]]", "[[ HashMap ]]"), item(2, "[[HashMap]]")));
		assertThat(idx.concepts()).containsExactly(new Concept("hashmap", "HashMap", 2));
	}

	@Test
	void conceptsAreListedStrongestFirstThenByName() {
		ConceptIndex idx = ConceptIndex.of(List.of(
				item(1, "[[Kafka]] [[Transactions]]"),
				item(2, "[[Transactions]]"),
				item(3, "[[Bloom filter]]")));
		assertThat(idx.concepts()).extracting(Concept::name).containsExactly("Transactions", "Bloom filter", "Kafka");
	}

	@Test
	void theFirstSpellingSeenIsTheDisplayName() {
		ConceptIndex idx = ConceptIndex.of(List.of(item(1, "[[LRU cache]]"), item(2, "[[lru   CACHE]]")));
		assertThat(idx.concepts()).containsExactly(new Concept("lru cache", "LRU cache", 2));
	}

	@Test
	void relatedConceptsAreThoseInTheSameItemsMostSharedFirst() {
		ConceptIndex idx = ConceptIndex.of(List.of(
				item(1, "[[HashMap]] [[equals and hashCode]] [[LRU cache]]"),
				item(2, "[[hashmap]] [[LRU Cache]]"),
				item(3, "[[Kafka]]")));
		assertThat(idx.related("HASHMAP")).containsExactly(new Concept("lru cache", "LRU cache", 2),
				new Concept("equals and hashcode", "equals and hashCode", 1));
		assertThat(idx.related("kafka")).isEmpty();
	}

	@Test
	void itemsLinkingAConceptAreFoundByAnySpelling() {
		ConceptIndex idx = ConceptIndex.of(List.of(item(1, "[[HashMap]]"), item(2, "nothing"), item(3, "[[hash map]]")));
		assertThat(idx.itemsFor(" hashmap ")).extracting(Item::id).containsExactly(1L);
		assertThat(idx.itemsFor("Hash Map")).extracting(Item::id).containsExactly(3L);
		assertThat(idx.linked()).containsExactlyInAnyOrder("hashmap", "hash map");
	}

	@Test
	void aTermIsMentionedOnlyAsAWholeWord() {
		assertThat(ConceptIndex.mentions("A HashMap stores buckets.", "hashmap")).isTrue();
		assertThat(ConceptIndex.mentions("ConcurrentHashMap is different.", "HashMap")).isFalse();
		assertThat(ConceptIndex.mentions("Use the equals / hashCode contract", "equals / hashCode contract")).isTrue();
		assertThat(ConceptIndex.mentions("C++ and C#", "C++")).isTrue();
		assertThat(ConceptIndex.mentions(null, "x")).isFalse();
	}

	// ── The Reference glossary (lesson R1) ────────────────────────────────────────────────────

	@Test
	void glossaryTermsComeFromTheFirstColumnAndLessonsFromTheLast() {
		String md = """
				## Java

				| Term | Plain meaning | Why it matters | Lesson |
				|---|---|---|---|
				| JVM (Java Virtual Machine) | Runs bytecode. | Portability. | JAVA-FUND-1 |
				| Heap | Shared memory. | `-Xmx`. | JAVA-JVM-2, JAVA-JVM-5 |
				| Kafka rebalancing | Moves partitions. | Pauses. | — |

				Some text | with a pipe.
				""";
		assertThat(ConceptIndex.glossary(md)).containsExactly(
				new ConceptIndex.Term("JVM (Java Virtual Machine)", List.of("JAVA-FUND-1")),
				new ConceptIndex.Term("Heap", List.of("JAVA-JVM-2", "JAVA-JVM-5")),
				new ConceptIndex.Term("Kafka rebalancing", List.of()));
	}

	@Test
	void aGlossaryTermIsLinkedByItsFullNameOrItsShortNameBeforeTheBracket() {
		ConceptIndex.Term jvm = new ConceptIndex.Term("JVM (Java Virtual Machine)", List.of("JAVA-FUND-1"));
		assertThat(jvm.keys()).containsExactly("jvm (java virtual machine)", "jvm");
		assertThat(new ConceptIndex.Term("Heap", List.of()).keys()).containsExactly("heap");
	}
}
