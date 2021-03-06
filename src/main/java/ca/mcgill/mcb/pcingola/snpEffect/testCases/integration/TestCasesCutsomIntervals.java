package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test Loss of Function prediction
 *
 * @author pcingola
 */
public class TestCasesCutsomIntervals {

	public static boolean debug = false;
	public static boolean verbose = false || debug;
	public static final int NUM_DEL_TEST = 10; // number of random test per transcript

	public TestCasesCutsomIntervals() {
		super();
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		// Load database
		String[] args = { "-classic", "-interval", "tests/custom_intervals_01.gff", "testHg3770Chr22", "tests/custom_intervals_01.vcf" };
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run
		List<VcfEntry> vcfEntries = cmdEff.run(true);

		// Check propper annotations
		VcfEntry ve = vcfEntries.get(0);
		Assert.assertEquals("R02837:N/A", ve.getInfo("custom_intervals_01_type"));
		Assert.assertEquals("TRANSFAC_site", ve.getInfo("custom_intervals_01_source"));
		Assert.assertEquals("R02837", ve.getInfo("custom_intervals_01_siteAcc"));
	}
}
