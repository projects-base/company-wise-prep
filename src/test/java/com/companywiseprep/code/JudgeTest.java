package com.companywiseprep.code;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Judges the real two-sum challenge in data/code, end to end through the API. */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:judgetest;DB_CLOSE_DELAY=-1",
		"prep.data-dir=./data"})
@AutoConfigureMockMvc
class JudgeTest {

	@Autowired
	MockMvc mvc;

	private static String json(String s) {
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t") + "\"";
	}

	private MockHttpServletRequestBuilder run(String code, String mode) {
		return post("/api/code/two-sum/run").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":" + json(code) + ",\"mode\":\"" + mode + "\"}");
	}

	private static String reference() throws Exception {
		return Files.readString(Path.of("data/code/two-sum/reference/Solution.java"));
	}

	@Test
	void theReferencePassesEveryHiddenTest() throws Exception {
		mvc.perform(run(reference(), "SUBMIT"))
				.andExpect(jsonPath("$.verdict", is("ACCEPTED")))
				.andExpect(jsonPath("$.total", greaterThan(3)))
				.andExpect(jsonPath("$.results[3].input", nullValue()));
	}

	@Test
	void runOnlyUsesTheVisibleExamplesAndShowsWrongAnswers() throws Exception {
		String starter = Files.readString(Path.of("data/code/two-sum/Solution.java"));
		mvc.perform(run(starter, "RUN"))
				.andExpect(jsonPath("$.verdict", is("WRONG_ANSWER")))
				.andExpect(jsonPath("$.results", hasSize(3)))
				.andExpect(jsonPath("$.results[0].expected", containsString("[0,1]")))
				.andExpect(jsonPath("$.results[0].actual", containsString("[]")));
	}

	@Test
	void submitRevealsOnlyTheFirstFailingHiddenTest() throws Exception {
		String quadratic = """
				class Solution {
				    public int[] twoSum(int[] nums, int target) {
				        if (nums.length > 1000) return new int[0];
				        for (int i = 0; i < nums.length; i++)
				            for (int j = i + 1; j < nums.length; j++)
				                if (nums[i] + nums[j] == target) return new int[] {i, j};
				        return new int[0];
				    }
				}""";
		mvc.perform(run(quadratic, "SUBMIT"))
				.andExpect(jsonPath("$.verdict", is("WRONG_ANSWER")))
				.andExpect(jsonPath("$.results[6].verdict", is("WRONG_ANSWER")))
				.andExpect(jsonPath("$.results[6].input", containsString("[")));
	}

	@Test
	void compileErrorsPointAtTheLearnersLine() throws Exception {
		mvc.perform(run("class Solution {\n  public int[] twoSum(int[] nums, int target) {\n    return nope;\n  }\n}", "RUN"))
				.andExpect(jsonPath("$.verdict", is("COMPILE_ERROR")))
				.andExpect(jsonPath("$.compileErrors[0].file", is("Solution.java")))
				.andExpect(jsonPath("$.compileErrors[0].line", is(3)));
	}

	@Test
	void anInfiniteLoopIsATimeLimitNotAHang() throws Exception {
		mvc.perform(run("class Solution { public int[] twoSum(int[] n, int t) { while (true) {} } }", "RUN"))
				.andExpect(jsonPath("$.verdict", is("TIME_LIMIT")));
	}

	@Test
	void customInputGetsItsExpectedOutputFromTheReference() throws Exception {
		mvc.perform(post("/api/code/two-sum/run").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":" + json(reference()) + ",\"mode\":\"RUN\",\"customInput\":\"[5,1,4]\\n9\"}"))
				.andExpect(jsonPath("$.verdict", is("ACCEPTED")))
				.andExpect(jsonPath("$.results[0].expected", containsString("[0,2]")));
	}

	@Test
	void thePlaygroundRunsAnyProgramWithStdin() throws Exception {
		String program = "import java.util.*;\npublic class Hello {\n  public static void main(String[] a) {\n"
				+ "    System.out.println(\"hi \" + new Scanner(System.in).nextLine());\n  }\n}";
		mvc.perform(post("/api/playground/run").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":" + json(program) + ",\"stdin\":\"Akhil\"}"))
				.andExpect(jsonPath("$.verdict", is("ACCEPTED")))
				.andExpect(jsonPath("$.stdout", containsString("hi Akhil")));
	}

	@Test
	void draftsAreSavedWithTheQuestionsProgress() throws Exception {
		mvc.perform(put("/api/code/two-sum/draft").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"// wip\"}"))
				.andExpect(status().isOk());
		mvc.perform(get("/api/code/two-sum")).andExpect(jsonPath("$.savedCode", is("// wip")));
	}

	@Test
	void otherWebsitesCannotRunCode() throws Exception {
		mvc.perform(run("class Solution {}", "RUN").header("Origin", "https://evil.example"))
				.andExpect(status().isForbidden());
		mvc.perform(get("/api/questions").header("Host", "rebound.evil.example"))
				.andExpect(status().isForbidden());
		mvc.perform(run(reference(), "RUN").header("Origin", "http://localhost:5180"))
				.andExpect(status().isOk());
	}
}
