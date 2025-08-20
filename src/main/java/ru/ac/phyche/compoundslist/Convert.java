package ru.ac.phyche.compoundslist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import org.openscience.cdk.exception.CDKException;

public class Convert {

	public static String downloadURL(String url) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		String s = reader.readLine();
		reader.close();
		return s.trim();
	}

	public static String downloadURLCAS(String url) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		String s = reader.readLine();
		HashMap<Integer, String> casNums = new HashMap<Integer, String>();
		while (s != null) {
			String[] spl = s.trim().split("\\-");
			if (spl.length == 3) {
				try {
					Integer.parseInt(spl[0]);
					Integer.parseInt(spl[1]);
					Integer.parseInt(spl[2]);
					casNums.put(Integer.parseInt(s.trim().replace("-", "")), s);
				} catch (Exception e) {
				}
			}
			s = reader.readLine();
		}
		reader.close();
		if (casNums.size() == 0) {
			return null;
		} else {
			Integer[] casnumsint = casNums.keySet().toArray(new Integer[casNums.keySet().size()]);
			Arrays.sort(casnumsint);
			return casNums.get(casnumsint[0]);
		}
	}

	public static String websiteConvert(String template, String x, String y, String z) {
		return websiteConvert(template, x, y, z, false);
	}

	public static String websiteConvert(String template, String x, String y, String z, boolean outCas) {
		String s = template;
		s = s.replace("XXXXXX", x);
		s = s.replace("YYYYYY", y);
		s = s.replace("ZZZZZZ", z);
		String result = null;
		try {
			if (outCas) {
				System.out.println(s);
				result = downloadURLCAS(s);
			} else {
				System.out.println(s);
				result = downloadURL(s);
			}
		} catch (IOException e) {
		}
		return result;
	}

	public static String pubchemConvert(String x, String y, String z, boolean outCas) {
		if (z.equals("smiles")) {
			z = "isomeric" + z;
		}
		y = URLEncoder.encode(y, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
		String result = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/XXXXXX/property/ZZZZZZ/TXT?XXXXXX=YYYYYY";
		if (z.equals("synonyms")) {
			result = result.replace("property/ZZZZZZ", "ZZZZZZ");
		}
		if (z.equals("cids")) {
			result = result.replace("property/ZZZZZZ", "ZZZZZZ");
		}
		return websiteConvert(result, x, y, z, outCas);
	}

	public static String pubchemConvert(String x, String y, String z) {
		return pubchemConvert(x, y, z, false);
	}

	public static String cactusConvert(String x, String y) {
		return cactusConvert(x, y, false);
	}

	public static String cactusConvert(String x, String y, boolean outCas) {
		x = x.replaceAll(" ", "%20");
		String template = "https://cactus.nci.nih.gov/chemical/structure/XXXXXX/YYYYYY";
		return websiteConvert(template, x, y, "", outCas);
	}

	public static String nameInChIKeyCASCIDToSmilesInChIInChIKey(String inp, boolean preferCactus, String outStringName,
			int timeout, boolean noAlterOutput, boolean noStereo, boolean isInputInchiKey, boolean isInputCID)
			throws InterruptedException {
		String result = null;
		String n = "name";
		if (isInputInchiKey) {
			inp = inp.replace("UHFFFAOYNA", "UHFFFAOYSA");
			inp = inp.replace("InChIKey=", "");
			inp = inp.toUpperCase();
			n = "inchikey";
		}
		String outStringName1 = outStringName;
		if (outStringName.equals("inchikey")) {
			if (!noAlterOutput) {
				outStringName1 = "inchi";
			}
		}
		if (isInputCID) {
			inp = inp.replace("CID", "").trim();
			inp = inp.replace("CID-", "").trim();
			boolean printed = false;
			for (int i = 0; i < 3; i++) {
				if (result == null) {
					result = pubchemConvert("cid", inp, outStringName1);
					Thread.sleep(timeout);
				}
				if ((result != null) && (!printed)) {
					printed = true;
					System.out.println("Pubchem!");
				}
			}
		} else {
			boolean printed = false;
			if (!preferCactus) {
				for (int i = 0; i < 3; i++) {
					if (result == null) {
						result = pubchemConvert(n, inp, outStringName1);
						Thread.sleep(timeout);
					}
					if ((result != null) && (!printed)) {
						printed = true;
						System.out.println("Pubchem!");
					}
				}
				for (int i = 0; i < 3; i++) {
					if (result == null) {
						result = cactusConvert(inp, outStringName1);
						Thread.sleep(timeout);
					}
					if ((result != null) && (!printed)) {
						printed = true;
						System.out.println("Cactus!");
					}
				}
			} else {
				for (int i = 0; i < 3; i++) {
					if (result == null) {
						result = cactusConvert(inp, outStringName1);
						Thread.sleep(timeout);
					}
					if ((result != null) && (!printed)) {
						printed = true;
						System.out.println("Cactus!");
					}
				}
				for (int i = 0; i < 3; i++) {
					if (result == null) {
						result = pubchemConvert(n, inp, outStringName1);
						Thread.sleep(timeout);
					}
					if ((result != null) && (!printed)) {
						printed = true;
						System.out.println("Pubchem!");
					}
				}
			}
		}
		if (result != null) {
			if (!noAlterOutput) {
				if (outStringName.equals("smiles")) {
					try {
						result = ChemUtils.canonical(result, !noStereo);
						result = ChemUtils.canonical(result, !noStereo);
					} catch (Exception e) {
						result = null;
					}
				}
				if (outStringName.equals("inchi")) {
					try {
						result = ChemUtils.smilesToInchi(ChemUtils.inchiToSmiles(result, !noStereo));
						result = ChemUtils.smilesToInchi(ChemUtils.inchiToSmiles(result, !noStereo));
					} catch (Exception e) {
						result = null;
					}
				}
				if (outStringName.equals("inchikey")) {
					try {
						result = ChemUtils.smilesToInchi(ChemUtils.inchiToSmiles(result, !noStereo));
						result = ChemUtils.smilesToInchiKey(ChemUtils.inchiToSmiles(result, !noStereo));
					} catch (Exception e) {
						result = null;
					}
				}
			}
		}
		return result;
	}

	public static String everythingToNameCAS(String inp, String inpRep, boolean preferCactus, int timeout,
			boolean noStereo, boolean casInsteadOfName) throws InterruptedException {
		String result = null;
		String inp0 = everythigToInchi(inp, inpRep, preferCactus, timeout, noStereo);

		String n = "inchi";
		boolean printed = false;
		if (!preferCactus) {
			for (int i = 0; i < 3; i++) {
				if (result == null) {
					result = pubchemConvert(n, inp0, "synonyms", casInsteadOfName);
					Thread.sleep(timeout);
				}
				if (result == null) {
					result = pubchemConvert(n, inp0, "iupacname", false);
					Thread.sleep(timeout);					
				}
				if ((result != null) && (!printed)) {
					printed = true;
					System.out.println("Pubchem!");
				}
			}
			for (int i = 0; i < 3; i++) {
				if (result == null) {
					result = cactusConvert(inp0, casInsteadOfName ? "cas" : "names", casInsteadOfName);
					Thread.sleep(timeout);
				}
				if ((result != null) && (!printed)) {
					printed = true;
					System.out.println("Cactus!");
				}
			}
		} else {
			for (int i = 0; i < 3; i++) {
				if (result == null) {
					result = cactusConvert(inp0, casInsteadOfName ? "cas" : "names", casInsteadOfName);
					Thread.sleep(timeout);
				}
				if ((result != null) && (!printed)) {
					printed = true;
					System.out.println("Cactus!");
				}
			}
			for (int i = 0; i < 3; i++) {
				if (result == null) {
					result = pubchemConvert(n, inp0, "synonyms", casInsteadOfName);
					Thread.sleep(timeout);
				}
				if (result == null) {
					result = pubchemConvert(n, inp0, "iupacname", false);
					Thread.sleep(timeout);					
				}
				if ((result != null) && (!printed)) {
					printed = true;
					System.out.println("Pubchem!");
				}
			}
		}
		if (!casInsteadOfName) {
			int lc = (int) result.chars().filter((s) -> Character.isLowerCase(s)).count();
			int uc = (int) result.chars().filter((s) -> Character.isUpperCase(s)).count();
			if (1.0 * uc / (uc + lc) > 0.6) {
				result = result.toLowerCase().replace("(z)", "(Z)").replace("(e)", "(e)");
			}
		}
		return result;
	}

	public static String everythigToInchi(String inp, String inpRep, boolean preferCactus, int timeout,
			boolean noStereo) throws InterruptedException {
		String inp0 = null;
		try {
			if (inpRep.equals("SMILES")) {
				inp0 = ChemUtils.smilesToInchi(ChemUtils.canonical(inp, !noStereo));
			}
			if (inpRep.equals("InChI")) {
				inp0 = ChemUtils.smilesToInchi(ChemUtils.inchiToSmiles(inp, !noStereo));
			}
			if (inpRep.equals("CAS") || inpRep.equals("Name") || inpRep.equals("InChIKey") || inpRep.equals("CID")) {
				inp0 = nameInChIKeyCASCIDToSmilesInChIInChIKey(inp.replace("_", " ").toLowerCase(), preferCactus,
						"inchi", timeout, false, noStereo, inpRep.equals("InChIKey"), inpRep.equals("CID"));
				inp0 = ChemUtils.smilesToInchi(ChemUtils.inchiToSmiles(inp0, !noStereo));
				System.out.println(inp0);
			}
			return inp0;
		} catch (CDKException e) {
			return null;
		}
	}

	public static String everythingToCID(String inp, String inpRep, boolean preferCactus, int timeout, boolean noStereo)
			throws InterruptedException {
		String result = null;
		String inp0 = everythigToInchi(inp, inpRep, preferCactus, timeout, noStereo);
		String n = "inchi";
		boolean printed = false;
		for (int i = 0; i < 3; i++) {
			if (result == null) {
				result = pubchemConvert(n, inp0, "cids", false);
				Thread.sleep(timeout);
			}
			if ((result != null) && (!printed)) {
				printed = true;
				System.out.println("Pubchem!");
			}
		}
		return result;
	}

	public static String autoDetectInput(String inp) {
		if (checkInput(inp, "SMILES")) {
			return "SMILES";
		}
		if (checkInput(inp, "InChI")) {
			return "InChI";
		}
		if (checkInput(inp, "InChIKey")) {
			return "InChIKey";
		}
		if (checkInput(inp, "CAS")) {
			return "CAS";
		}
		if (checkInput(inp, "CID")) {
			return "CID";
		}
		return "Name";
	}

	public static boolean checkInput(String inp, String inputRep) {
		if (inputRep.equals("Name")) {
			try {
				String x = ChemUtils.smilesToInchi(inp.trim());
				if (x != null) {
					return false;
				}
			} catch (CDKException e) {
			}
			try {
				String x = ChemUtils.inchiToSmiles(inp.trim(), false);
				if (x != null) {
					return false;
				}
			} catch (CDKException e) {
			}
			return true;
		}
		if (inputRep.equals("CAS")) {
			String[] spl = inp.trim().split("\\-");
			if (spl.length != 3) {
				return false;
			}
			try {
				Integer.parseInt(spl[0]);
				Integer.parseInt(spl[1]);
				Integer.parseInt(spl[2]);
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		if (inputRep.equals("InChIKey")) {
			String[] spl = inp.replace("InChIKey=", "").trim().split("\\-");
			if (spl.length != 3) {
				return false;
			}
			try {
				Integer.parseInt(spl[0]);
				Integer.parseInt(spl[1]);
				Integer.parseInt(spl[2]);
				return false;
			} catch (Exception e) {
			}
			if (spl[0].length() != 14) {
				return false;
			}
			if (spl[1].length() != 10) {
				return false;
			}
			if (spl[2].length() != 1) {
				return false;
			}
			return true;
		}
		if (inputRep.equals("InChI")) {
			try {
				ChemUtils.inchiToSmiles(inp.trim(), true);
				ChemUtils.inchiToSmiles(inp.trim(), false);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		if (inputRep.equals("SMILES")) {
			try {
				ChemUtils.smilesToInchi(inp.trim());
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		if (inputRep.equals("CID")) {
			try {
				Integer.parseInt(inp.replace("CID", "").replace("CID-", "").trim());
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		throw new RuntimeException("Incorrect representation");
	}

	public static String convert(String inp, String inputRep, String outputRep, boolean preferCactus,
			boolean noAlterOutput, boolean noStereo, int timeout) {
		try {
			if (inp == null) {
				return null;
			}

			if (inp.equals("null")) {
				return null;
			}
			
			if ((inputRep.equals("SMILES")) && (outputRep.equals("SMILES"))) {
				String outp = ChemUtils.canonical(inp, !noStereo);
				return outp;
			}

			if (inputRep.equals(outputRep)) {
				String x = convert(inp.trim(), inputRep, "SMILES", preferCactus, noAlterOutput, noStereo, timeout);
				String outp = convert(x, "SMILES", outputRep, preferCactus, noAlterOutput, noStereo, timeout);
				return outp;
			}
			String inp0 = inp.trim();
			if (inputRep.equals("Name")) {
				inp0 = inp.replace("_", " ").toLowerCase();
			}
			if (((inputRep.equals("Name")) || (inputRep.equals("CAS")) || (inputRep.equals("CID"))
					|| (inputRep.equals("InChIKey"))) && (outputRep.equals("SMILES"))) {
				String outp = nameInChIKeyCASCIDToSmilesInChIInChIKey(inp0, preferCactus, "smiles", timeout,
						noAlterOutput, noStereo, inputRep.equals("InChIKey"), inputRep.equals("CID"));
				return outp;
			}
			if (((inputRep.equals("Name")) || (inputRep.equals("CAS")) || (inputRep.equals("CID"))
					|| (inputRep.equals("InChIKey"))) && (outputRep.equals("InChI"))) {
				String outp = nameInChIKeyCASCIDToSmilesInChIInChIKey(inp0, preferCactus, "inchi", timeout,
						noAlterOutput, noStereo, inputRep.equals("InChIKey"), inputRep.equals("CID"));
				return outp;
			}
			if (((inputRep.equals("Name")) || (inputRep.equals("CAS")) || (inputRep.equals("CID")))
					&& (outputRep.equals("InChIKey"))) {
				String outp = nameInChIKeyCASCIDToSmilesInChIInChIKey(inp0, preferCactus, "inchikey", timeout,
						noAlterOutput, noStereo, false, inputRep.equals("CID"));
				return outp;
			}
			if (outputRep.equals("Name")) {
				String outp = everythingToNameCAS(inp0, inputRep, preferCactus, timeout, noStereo, false);
				return outp;
			}
			if (outputRep.equals("CAS")) {
				String outp = everythingToNameCAS(inp0, inputRep, preferCactus, timeout, noStereo, true);
				return outp;
			}
			if (outputRep.equals("CID")) {
				String outp = everythingToCID(inp0, inputRep, preferCactus, timeout, noStereo);
				return outp;
			}

			if ((inputRep.equals("InChI")) && (outputRep.equals("InChIKey"))) {
				String outp = ChemUtils.canonical(ChemUtils.inchiToSmiles(inp0, !noStereo), !noStereo);
				outp = ChemUtils.smilesToInchiKey(outp);
				return outp;
			}
			if ((inputRep.equals("InChI")) && (outputRep.equals("SMILES"))) {
				String outp = ChemUtils.canonical(ChemUtils.inchiToSmiles(inp0, !noStereo), !noStereo);
				return outp;
			}
			if ((inputRep.equals("SMILES")) && (outputRep.equals("InChIKey"))) {
				String outp = ChemUtils.canonical(ChemUtils.canonical(inp0, !noStereo), !noStereo);
				outp = ChemUtils.smilesToInchiKey(outp);
				return outp;
			}
			if ((inputRep.equals("SMILES")) && (outputRep.equals("InChI"))) {
				String outp = ChemUtils.canonical(ChemUtils.canonical(inp0, !noStereo), !noStereo);
				outp = ChemUtils.smilesToInchi(outp);
				return outp;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
}
