**Short answer:** LLM cost is mostly tokens (input + output) times the model's price, so you cut it in layers: don't call the model when you already have the answer (exact-match response cache, then a semantic cache for near-duplicate questions), make each call cheaper (provider prompt caching for the long, repeated prefix; shorter prompts; capped output; a smaller model for easy requests), and run non-urgent work through batch APIs. Observability means recording, for every call, the model, prompt version, input/output/cached tokens, cost, latency (including time to first token), errors and a trace id, and evaluating answer quality, because a cheaper setup that quietly gets worse is not a saving.

## Explanation

**Caching layers, cheapest first**

1. **Exact response cache.** Key = hash of (model, prompt template version, normalised input, parameters). Store in Redis with a TTL. Good for repeated identical requests (FAQ, classification of the same item). Only safe when the output does not need to vary and does not depend on per-user data the key ignores.
2. **Semantic cache.** Embed the query, look up the nearest previous query in a vector index; if similarity is above a threshold, return its answer. Saves more but can return a wrong answer for a question that *looks* similar ("cancel my order" vs "cancel my subscription"). Tune the threshold on real data, scope it per tenant, and log every semantic hit so you can audit.
3. **Provider prompt caching.** Put the stable part first (system prompt, tool definitions, large documents) and the variable part last. Providers can reuse the processed prefix; cached input tokens are billed at a fraction of the normal rate and also reduce latency. It only helps if the prefix is byte-identical between calls, so do not put timestamps or user ids at the top.
4. **Cache intermediate results**: embeddings of documents, retrieval results, tool call results.

**Other cost levers:** route simple requests to a smaller model (with a classifier or rules) and escalate on low confidence; trim retrieved context to what is relevant; limit `max_tokens` and ask for concise structured output; use batch processing for offline jobs; set per-user and per-feature budgets with alerts.

**Observability**
- **Per call metrics:** tokens in/out/cached, cost, latency p50/p95/p99, time to first token for streaming, error and retry counts, stop reason (was the output truncated?), cache hit ratios for each layer.
- **Tracing:** one trace per user request with spans for retrieval, each model call and each tool call. OpenTelemetry has semantic conventions for generative-AI spans.
- **Content logging:** prompts and responses for debugging, with PII redaction and access control.
- **Quality:** offline eval sets run on each prompt or model change; online signals (thumbs up/down, retries, escalations); LLM-as-judge with human spot checks.
- **Dashboards by dimension:** cost per feature, per tenant, per prompt version, so a regression is attributable.

## Example

```java
public String answer(String question) {
    String key = "llm:v3:" + sha256(normalise(question));
    String cached = redis.opsForValue().get(key);
    if (cached != null) { meter.counter("llm.cache", "layer", "exact", "result", "hit").increment(); return cached; }

    Timer.Sample t = Timer.start(registry);
    var resp = llm.call(SYSTEM_PROMPT /* stable prefix, cacheable */, question);
    t.stop(registry.timer("llm.latency", "model", resp.model(), "prompt", "v3"));
    registry.counter("llm.tokens", "type", "input").increment(resp.inputTokens());
    registry.counter("llm.tokens", "type", "output").increment(resp.outputTokens());

    redis.opsForValue().set(key, resp.text(), Duration.ofHours(6));
    return resp.text();
}
```

(`llm` is your gateway wrapper around the provider SDK; the point is one place where caching and metrics happen.)

## Pitfalls and follow-ups

- **Cache invalidation:** include the prompt version and model in the key; changing either must miss.
- **Personalised or time-sensitive answers** should not be cached across users; scope keys.
- **Semantic cache false hits** are a correctness bug, not a cost saving.
- **Non-determinism:** caching makes answers stable, which may or may not be desired.
- **How do you evaluate an AI app (R2)?** Define task-specific metrics, build a labelled eval set, run it automatically on every change, and track quality alongside cost and latency.

Go deeper: [G2 · Prompting, structured output, tools, caching](../academy/lessons/G2.md), [G6 · Evals and safety](../academy/lessons/G6.md), [G3 · RAG](../academy/lessons/G3.md).
