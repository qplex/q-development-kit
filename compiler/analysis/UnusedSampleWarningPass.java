package analysis;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import compiler.Symbol;

/**
 * Warns about samples that are drawn but never used.
 *
 * A sample is used if its value is read -- directly, or through a chain of
 * later sampling statements -- by a return statement, a condition, or any
 * other expression outside of sampling statements. A never-used draw does not
 * change the returned distribution (the probabilities of its branches sum to
 * one, so it marginalizes away wherever it sits), but it multiplies the
 * number of enumerated states by the support size of the sampled pmf, so it
 * is pure cost.
 *
 * The warning is suppressed in functions that call branchProbability(); see
 * {@link FunctionSamplingAnalysis#_callsBranchProbability}.
 *
 * For joint draws the warning is positional. A joint pmf is stored as the
 * marginal of its first component plus a chain of conditionals, so a valid
 * extraction must fix every preceding component: for Pmf{Z,L}, mu{Z} is a
 * valid extraction but mu{L} is not. Obtaining the distribution of a later
 * component therefore REQUIRES drawing the earlier ones and discarding them
 * (see "Pmfs" in the Q Development Guide) -- an unused sample at or before
 * the last used coordinate is necessary and must not be flagged. Only an
 * unused TRAILING run is wasteful: there the prefix marginal is a valid
 * extraction, and sampling the full joint enumerates the trailing
 * components needlessly.
 */
public class UnusedSampleWarningPass {

	public static void run(String sourceName, Symbol functionSymbol) {
		FunctionSamplingAnalysis samplingAnalysis = new FunctionSamplingAnalysis(functionSymbol);
		if (samplingAnalysis._samplingSites.isEmpty())
			return;
		if (samplingAnalysis._callsBranchProbability)
			return;

		LinkedHashSet<String> liveNames = samplingAnalysis.computeLiveSampleNames();

		for (SamplingSite samplingSite : samplingAnalysis._samplingSites) {
			int lastLiveSampleIndex = -1;
			for (int sampleIndex = 0; sampleIndex < samplingSite._sampleNames.size(); sampleIndex++)
				if (liveNames.contains(samplingSite._sampleNames.get(sampleIndex)))
					lastLiveSampleIndex = sampleIndex;

			if (lastLiveSampleIndex == samplingSite._sampleNames.size() - 1)
				continue;

			if (lastLiveSampleIndex == -1) {
				Diagnostics.warn(sourceName, samplingSite.lineNumber(), //
						"In function '" + functionSymbol._name + "': "
								+ describeSamples(samplingSite._sampleNames)
								+ " never used; the sampling statement can be removed");
				continue;
			}

			// Unused samples at or before the last used coordinate are necessary
			// to reach the later components and are not reported. The unused
			// trailing run is avoidable: the prefix marginal is a valid
			// extraction.
			ArrayList<String> unusedTrailingSampleNames = new ArrayList<String>(samplingSite._sampleNames
					.subList(lastLiveSampleIndex + 1, samplingSite._sampleNames.size()));
			Diagnostics.warn(sourceName, samplingSite.lineNumber(), //
					"In function '" + functionSymbol._name + "': trailing "
							+ describeSamples(unusedTrailingSampleNames)
							+ " never used; consider sampling from the marginal pmf of the components up to '"
							+ samplingSite._sampleNames.get(lastLiveSampleIndex) + "'");
		}
	}

	private static String describeSamples(java.util.List<String> sampleNames) {
		if (sampleNames.size() == 1)
			return "sample '" + sampleNames.get(0) + "' is";
		StringBuilder description = new StringBuilder("samples ");
		for (int sampleNameIndex = 0; sampleNameIndex < sampleNames.size(); sampleNameIndex++) {
			if (sampleNameIndex > 0)
				description.append(sampleNameIndex == sampleNames.size() - 1 ? " and " : ", ");
			description.append("'").append(sampleNames.get(sampleNameIndex)).append("'");
		}
		return description.append(" are").toString();
	}
}
