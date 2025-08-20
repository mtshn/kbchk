package ru.ac.phyche.compoundslist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class MolecularFormula {
	public static String fromSmiles(String smiles) {
		try {
			return MolecularFormulaManipulator.getString(
					MolecularFormulaManipulator.getMolecularFormula(ChemUtils.smilesToAtomContainer(smiles)));
		} catch (Exception e) {
			return null;
		}
	}

	public static String canonical(String molecularFormula) {
		try {
			return MolecularFormulaManipulator
					.getString(MolecularFormulaManipulator.getMolecularFormula(MolecularFormulaManipulator
							.getAtomContainer(molecularFormula, DefaultChemObjectBuilder.getInstance())));
		} catch (Exception e) {
			return null;
		}
	}

	public static String parseSmilesOrFormula(String input) {
		String o = null;
		o = fromSmiles(input);
		if (o == null) {
			o = canonical(input);
		}
		if (o == null) {
			o = canonical(smartUpperCase(input));
		}

		return o;
	}

	public static String smartUpperCase(String input) {
		String s = input.toUpperCase();
		s = s.replace("CL", "Cl").replace("BR", "Br").replace("SI", "Si").replace("NA", "Na").replace("CA", "Ca")
				.replace("SE", "Se").replace("TE", "Te").replace("AS", "As");
		return s;
	}

	public static String[] lineNumbersAsInAWithFormulasFromB(String[] a, String[] b, String aName) {
		HashMap<String, ArrayList<Integer>> aa = new HashMap<String, ArrayList<Integer>>();
		for (int i = 0; i < a.length; i++) {
			String s = a[i];
			if (!s.trim().equals("")) {
				String f = parseSmilesOrFormula(s.trim());
				if (f != null) {
					ArrayList<Integer> al = aa.get(f);
					if (al == null) {
						al = new ArrayList<Integer>();
					}
					al.add(i);
					aa.put(f, al);
				}
			}
		}

		String[] result = new String[b.length];
		for (int i = 0; i < b.length; i++) {
			result[i] = (i + 1) + " " + b[i];
			if (!b[i].trim().equals("")) {
				String f = parseSmilesOrFormula(b[i].trim());
				if (f != null) {
					result[i] = result[i] + " " + f;
				}
				ArrayList<Integer> al = aa.get(f);
				if (al != null) {
					if (a == b) { // isomers in the same data set!
						if (al.size() == 1) {
							al = null; // trivial case!
						}
					}
				}
				if (al != null) {
					result[i] = result[i] + " the_same_molecular_formula_in_data_set_" + aName;
					if (a != b) {
						for (int j : al) {
							result[i] = result[i] + " " + (j + 1);
						}
					} else {
						for (int j : al) {
							if (i != j) {
								result[i] = result[i] + " " + (j + 1);
							}
						}
					}
				}
			}
		}
		return result;
	}

	public static HashSet<String> formulas(String[] a) {
		HashSet<String> result = new HashSet<String>();
		for (String s : a) {
			if (!s.trim().equals("")) {
				String f = parseSmilesOrFormula(s.trim());
				if (f != null) {
					result.add(f);
				}
			}
		}
		return result;
	}

	public static String[] onlyA(String[] a, String[] b) {
		HashSet<String> a1 = formulas(a);
		HashSet<String> a2 = formulas(b);
		a1.removeAll(a2);
		return a1.toArray(new String[a1.size()]);
	}

	public static String[] overlap(String[] a, String[] b) {
		HashSet<String> a1 = formulas(a);
		HashSet<String> a2 = formulas(b);
		a1.retainAll(a2);
		return a1.toArray(new String[a1.size()]);
	}

}
