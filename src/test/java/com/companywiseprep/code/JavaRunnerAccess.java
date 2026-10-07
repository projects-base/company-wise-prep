package com.companywiseprep.code;

import java.util.Map;

/** Test-only bridge to JavaRunner's package-private environment filter. */
public final class JavaRunnerAccess {

	private JavaRunnerAccess() {
	}

	public static void restrict(Map<String, String> env) {
		JavaRunner.restrictEnvironment(env);
	}
}
