package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.codons.CodonTables;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Codon tables
 *
 * @author pcingola
 */
public class TestCasesCodonTable {

	public static boolean debug = false;

	/**
	 * Degeneracy test
	 * References: http://en.wikipedia.org/wiki/Genetic_code#Degeneracy
	 *
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		CodonTable codonTable = CodonTables.getInstance().getTable(CodonTables.STANDARD_TABLE_NAME);

		/**
		 * A position of a codon is said to be a fourfold degenerate site if any nucleotide at this position
		 * specifies the same amino acid. For example, the third position of the glycine codons (GGA, GGG, GGC, GGU)
		 * is a fourfold degenerate site, because all nucleotide substitutions at this site are synonymous
		 */
		Assert.assertEquals(4, codonTable.degenerate("GGT", 2));

		/**
		 *  A position of a codon is said to be a twofold degenerate site if only two of four possible
		 *  nucleotides at this position specify the same amino acid. For example, the third position
		 *  of the glutamic acid codons (GAA, GAG) is a twofold degenerate site
		 */
		Assert.assertEquals(2, codonTable.degenerate("GAA", 2));

		/**
		 *  There is only one threefold degenerate site where changing to three of the four nucleotides may
		 *  have no effect on the amino acid (depending on what it is changed to), while changing to the fourth
		 *  possible nucleotide always results in an amino acid substitution. This is the third position of
		 *  an isoleucine codon: AUU, AUC, or AUA all encode isoleucine, but AUG encodes methionine.
		 */
		Assert.assertEquals(3, codonTable.degenerate("ATT", 2));
	}
}
