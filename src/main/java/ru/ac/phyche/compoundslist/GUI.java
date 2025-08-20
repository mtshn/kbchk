package ru.ac.phyche.compoundslist;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.openscience.cdk.exception.CDKException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;

public class GUI extends Application {

	private static class B1 {
		private boolean b = true;

		public boolean isB() {
			return b;
		}

		public void setB(boolean b) {
			this.b = b;
		}
	}

	public class JSJavaCall {
		public String smiles = "X";
		public String oldSmiles = "X";

		public void jsjavacall(String smiles) {
			String canonicalSmiles;
			try {
				if (smiles.length() > 0) {
					canonicalSmiles = ChemUtils.canonical(smiles, true);
					this.smiles = canonicalSmiles;
				}
			} catch (CDKException e) {
			}
		}
	}

	private static String concatNewLines(String[] a) {
		if(a==null) {
			return "";
		}
		if(a.length==0) {
			return "";
		}
		String r = "";
		try {
		for (int i = 0; i < a.length - 1; i++) {
			r = r + a[i].trim() + "\n";
		}
		r = r + a[a.length - 1];}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return r;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Locale.setDefault(Locale.US);

		primaryStage.setTitle("Molecules and names");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(new File("./main.fxml").toURI().toURL());
		VBox vBox = loader.<VBox>load();
		Scene scene = new Scene(vBox, 1000, 720);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image((new File("./bitmap2.png")).toURI().toURL().toString()));
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> {
			System.exit(0);
		});

		Button button1 = (Button) vBox.lookup("#button1");
		Button button2 = (Button) vBox.lookup("#button2");
		Button button3 = (Button) vBox.lookup("#button3");
		Button button4 = (Button) vBox.lookup("#button4");
		Button buttonxlsx = (Button) vBox.lookup("#buttonxlsx");
		Button button5 = (Button) vBox.lookup("#button5");
		Button button6 = (Button) vBox.lookup("#button6");

		ImageView img = new ImageView(new Image((new File("./bitmap2.png")).toURI().toString()));
		img.setFitHeight(148);
		img.setPreserveRatio(true);
		button1.setGraphic(img);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ComboBox<String> cbInp = (ComboBox) vBox.lookup("#cbinp");
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ComboBox<String> cbOutp = (ComboBox) vBox.lookup("#cboutp");
		TextArea taInp = (TextArea) vBox.lookup("#einp");
		TextArea taOutp = (TextArea) vBox.lookup("#eoutp");
		CheckBox cbPreferCactus = (CheckBox) vBox.lookup("#cb1");
		CheckBox cbDoNotAlter = (CheckBox) vBox.lookup("#cb2");
		CheckBox cbNoStereo = (CheckBox) vBox.lookup("#cb3");
		CheckBox cbNumbers = (CheckBox) vBox.lookup("#cb4");
		TextField tfTO = (TextField) vBox.lookup("#timeout");
		TextField tfSmiles = (TextField) vBox.lookup("#editsmiles");
		TextField tfXlsxFileName = (TextField) vBox.lookup("#xlsximagesfilename");
		TextField tfSizeInPixels = (TextField) vBox.lookup("#sizeinpixels");

		TextArea taA = (TextArea) vBox.lookup("#smiles_a");
		TextArea taB = (TextArea) vBox.lookup("#smiles_b");
		TextArea taOnlyA = (TextArea) vBox.lookup("#smiles_only_a");
		TextArea taOnlyB = (TextArea) vBox.lookup("#smiles_only_b");
		TextArea taAB = (TextArea) vBox.lookup("#smiles_ab");
		TextArea taDraw = (TextArea) vBox.lookup("#draw_out");
		TextArea taSmilesForImages = (TextArea) vBox.lookup("#smilesforimages");
		TextArea taMfA = (TextArea) vBox.lookup("#mf_dataset_a");
		TextArea taMfB = (TextArea) vBox.lookup("#mf_dataset_b");
		TextArea taMfOut = (TextArea) vBox.lookup("#mf_out");

		WebView webView = (WebView) vBox.lookup("#webview");
		String path = new java.io.File(".").getCanonicalPath();
		webView.getEngine().load("file://" + path + "/moledit.html");
		final JSJavaCall jsJavaCall = new JSJavaCall();
		webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(@SuppressWarnings("rawtypes") ObservableValue observable, Worker.State oldValue,
					Worker.State newValue) {
				if (newValue != Worker.State.SUCCEEDED) {
					return;
				}
				JSObject window = (JSObject) webView.getEngine().executeScript("window");
				window.setMember("o", jsJavaCall);
			}
		});

		String[] rep = new String[] { "Auto detect", "Name", "CAS", "SMILES", "InChIKey", "InChI", "CID" };
		for (String q : rep) {
			cbInp.getItems().add(q);
			if (!q.equals("Auto detect")) {
				cbOutp.getItems().add(q);
			} else {
				cbOutp.getItems().add("");
			}
		}
		cbInp.getSelectionModel().select(0);
		cbOutp.getSelectionModel().select(1);

		final B1 b = new B1();

		EventHandler<ActionEvent> runStop = (new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				b.setB(false);
			}
		});

		EventHandler<ActionEvent> runConversion = (new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						button1.setDisable(true);
						b.setB(true);
						taOutp.setText("");
						String[] lines = taInp.getText().split("\\n");
						String inpRep = rep[cbInp.getSelectionModel().getSelectedIndex()];
						String outpRep = rep[cbOutp.getSelectionModel().getSelectedIndex()];
						if (!inpRep.equals("Auto detect")) {
							int bad = 0;
							int all = 0;
							for (String q : lines) {
								if (!q.trim().equals("")) {
									all++;
									if (!Convert.checkInput(q.trim(), inpRep)) {
										bad++;
									}
								}
							}
							if ((bad * 1.0 / all) > 0.5) {
								b.setB(false);
								Platform.runLater(new Runnable() {
									public void run() {
										Alert a = new Alert(AlertType.ERROR);
										a.setContentText(
												"Check that the input representation provided is correct. More than half of the lines are incorrect.");
										a.showAndWait();
									}
								});
							}
						}

						for (String q : lines) {
							if (b.isB()) {
								String o = "";
								if (!q.trim().equals("")) {
									inpRep = rep[cbInp.getSelectionModel().getSelectedIndex()];
									boolean autodetect = false;
									if (inpRep.equals("Auto detect")) {
										inpRep = Convert.autoDetectInput(q.trim());
										autodetect = true;
									}
									System.out.println(inpRep);
									if (inpRep.equals("CAS") || inpRep.equals("CID")) {
										int i = 0;
										while (q.trim().charAt(i) == '0') {
											i++;
										}
										String w = "";
										for (int j = i; j < q.trim().length(); j++) {
											w = w + q.trim().charAt(j);
										}
										q = w;
										if (cbNumbers.selectedProperty().get() && (autodetect)) {
											if (q.trim().chars().allMatch(Character::isDigit)) {
												try {
													int casPlain = Integer.parseInt(q.trim());
													if (casPlain > 1000) {
														int x = casPlain % 10;
														casPlain = (casPlain - x) / 10;
														int y = casPlain % 100;
														casPlain = (casPlain - y) / 100;
														String ys = y + "";
														if (y < 10) {
															ys = "0" + y;
														}
														q = casPlain + "-" + ys + "-" + x;
													}
												} catch (Exception e) {
												}
												inpRep = "CAS";
											}
										}
									}
									o = Convert.convert(q.trim(), inpRep, outpRep, cbPreferCactus.isSelected(),
											cbDoNotAlter.isSelected(), cbNoStereo.isSelected(),
											Integer.parseInt(tfTO.getText().trim()));
								}
								String o1 = o;
								Platform.runLater(new Runnable() {
									public void run() {
										String x1 = "\n";
										if (taOutp.getText().length() == 0) {
											x1 = "";
										}
										taOutp.setText(taOutp.getText() + x1 + o1);
										if ((o1.trim().equals("")) && (taOutp.getText().length() == 0)) {
											taOutp.setText(taOutp.getText() + " ");
										}
									}
								});
							}
						}
						b.setB(false);
						button1.setDisable(false);
					}
				});
				thread.start();
			}
		});

		EventHandler<ActionEvent> runIntersection = (new EventHandler<ActionEvent>() {
			private HashMap<String, String> canonicalize(String[] lines) {
				HashMap<String, String> result = new HashMap<String, String>();
				String[] allSmilesCanonical = new String[lines.length];
				AtomicBoolean b = new AtomicBoolean();
				b.set(false);
				Arrays.stream(ArUtls.intsrnd(lines.length)).parallel().forEach(i -> {
					if (!b.get()) {
						if (!lines[i].trim().equals("")) {
							String smilesCan = null;
							try {
								smilesCan = ChemUtils.canonical(ChemUtils.canonical(lines[i], true), true);
							} catch (Exception e) {
								Platform.runLater(new Runnable() {
									public void run() {
										Alert a = new Alert(AlertType.ERROR);
										a.setContentText("Bad SMILES \"" + lines[i] + "\"!!!"
												+ "This function only works with valid SMILES strings. If your molecules are in the form of names etc. - convert them to SMILES.");
										a.showAndWait();
									}
								});
								b.set(true);
								throw new RuntimeException("");
							}
							allSmilesCanonical[i] = smilesCan;
						} else {
							allSmilesCanonical[i] = "";
						}
					}
				});
				if (b.get()) {
					return null;
				}
				for (int i = 0; i < lines.length; i++) {
					if (!lines[i].trim().equals("")) {
						String smilesCan = null;
						smilesCan = allSmilesCanonical[i];
						result.put(lines[i], smilesCan);
					}
				}
				return result;
			}

			private HashMap<String, String> linesByCanonicalSmiles(String[] lines,
					HashMap<String, String> canonicalByNonCanonical) {
				HashMap<String, String> result = new HashMap<String, String>();
				for (int i = 0; i < lines.length; i++) {
					if (!lines[i].trim().equals("")) {
						String smilesCan = canonicalByNonCanonical.get(lines[i]);
						result.put(smilesCan, lines[i]);
					}
				}
				return result;
			}

			private HashMap<String, int[]> lineNumbers(String[] lines,
					HashMap<String, String> canonicalByNonCanonical) {
				HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
				for (int i = 0; i < lines.length; i++) {
					if (!lines[i].trim().equals("")) {
						String smilesCan = canonicalByNonCanonical.get(lines[i]);
						ArrayList<Integer> x = null;
						if (result.get(smilesCan) == null) {
							x = new ArrayList<Integer>();
						} else {
							x = result.get(smilesCan);
						}
						x.add(i);
						result.put(smilesCan, x);
					}
				}
				HashMap<String, int[]> resultA = new HashMap<String, int[]>();
				for (String s : result.keySet()) {
					ArrayList<Integer> al = result.get(s);
					int[] x = new int[al.size()];
					for (int i = 0; i < x.length; i++) {
						x[i] = al.get(i);
					}
					resultA.put(s, x);
				}
				return resultA;
			}

			@Override
			public void handle(ActionEvent actionEvent) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						String[] linesA = taA.getText().split("\\n");
						String[] linesB = taB.getText().split("\\n");
						for (int i = 0; i < linesA.length; i++) {
							linesA[i] = linesA[i].trim();
						}
						for (int i = 0; i < linesB.length; i++) {
							linesB[i] = linesB[i].trim();
						}
						HashMap<String, String> canonicalByNonCanonical = canonicalize(
								ArrayUtils.addAll(linesA, linesB));
						HashMap<String, String> linesFromAByCanonicalSmiles = linesByCanonicalSmiles(linesA,
								canonicalByNonCanonical);
						HashMap<String, String> linesFromBByCanonicalSmiles = linesByCanonicalSmiles(linesB,
								canonicalByNonCanonical);
						Set<String> a0 = linesFromAByCanonicalSmiles.keySet();
						Set<String> b0 = linesFromBByCanonicalSmiles.keySet();
						HashSet<String> onlyA = new HashSet<String>();
						HashSet<String> onlyB = new HashSet<String>();
						onlyA.addAll(a0);
						onlyA.removeAll(b0);
						onlyB.addAll(b0);
						onlyB.removeAll(a0);
						HashSet<String> ab = new HashSet<String>();
						ab.addAll(a0);
						ab.retainAll(b0);

						HashMap<String, int[]> lineNumbersAByCanonicalSmiles = lineNumbers(linesA,
								canonicalByNonCanonical);
						HashMap<String, int[]> lineNumbersBByCanonicalSmiles = lineNumbers(linesB,
								canonicalByNonCanonical);

						String outA = "";
						for (int i = 0; i < linesA.length; i++) {
							if (!linesA[i].trim().equals("")) {
								outA = outA + linesA[i] + " ";
								String can = canonicalByNonCanonical.get(linesA[i]);

								if (onlyA.contains(can)) {
									outA = outA + "(ONLY_A)";
								} else {
									outA = outA + "(BOTН_A,_B)";
								}

								if (lineNumbersAByCanonicalSmiles.get(can).length > 1) {
									outA = outA + " The_same_compound_at_lines: ";
									for (int j = 0; j < lineNumbersAByCanonicalSmiles.get(can).length; j++) {
										int num = lineNumbersAByCanonicalSmiles.get(can)[j];
										if (i != num) {
											outA = outA + (num + 1) + " ";
										}
									}
								}
								outA = outA + "\n";
							} else {
								outA = outA + "\n";
							}
						}

						String outB = "";
						for (int i = 0; i < linesB.length; i++) {
							if (!linesB[i].trim().equals("")) {
								outB = outB + linesB[i] + " ";
								String can = canonicalByNonCanonical.get(linesB[i]);

								if (onlyB.contains(can)) {
									outB = outB + "(ONLY_B)";
								} else {
									outB = outB + "(BOTH_A,_B)";
								}
								if (lineNumbersBByCanonicalSmiles.get(can).length > 1) {
									outB = outB + " The_same_compound_at_lines: ";
									for (int j = 0; j < lineNumbersBByCanonicalSmiles.get(can).length; j++) {
										int num = lineNumbersBByCanonicalSmiles.get(can)[j];
										if (i != num) {
											outB = outB + (num + 1) + " ";
										}
									}
								}
								outB = outB + "\n";
							} else {
								outB = outB + "\n";
							}
						}
						String outOnlyA = "";
						for (String s : onlyA) {
							outOnlyA = outOnlyA + linesFromAByCanonicalSmiles.get(s) + "\n";
						}
						String outOnlyB = "";
						for (String s : onlyB) {
							outOnlyB = outOnlyB + linesFromBByCanonicalSmiles.get(s) + "\n";
						}
						String outAB = "";
						for (String s : ab) {
							outAB = outAB + linesFromAByCanonicalSmiles.get(s) + " "
									+ linesFromBByCanonicalSmiles.get(s) + "\n";
						}
						outAB = outAB
								+ "For molecules contained simultaneously in A and B, two SMILES lines are given. As given in A and as given in B.";
						String outA0 = outA;
						String outB0 = outB;
						String outOnlyA0 = outOnlyA;
						String outOnlyB0 = outOnlyB;
						String outAB0 = outAB;

						Platform.runLater(new Runnable() {
							public void run() {
								taA.setText(outA0);
								taB.setText(outB0);
								taOnlyA.setText(outOnlyA0);
								taOnlyB.setText(outOnlyB0);
								taAB.setText(outAB0);
							}
						});
					}
				});
				thread.start();

			}
		});

		EventHandler<ActionEvent> runAddSmiles = (new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				taDraw.setText(taDraw.getText() + "\n" + jsJavaCall.smiles);
			}
		});

		EventHandler<ActionEvent> runImagesXLSX = (new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					String filename = tfXlsxFileName.getText();
					Workbook wb = new XSSFWorkbook();
					Sheet sheet = wb.createSheet("Compounds");
					String[] lines = taSmilesForImages.getText().split("\\n");
					for (int i = 0; i < lines.length; i++) {
						String smiles = null;
						String smiles0 = lines[i];
						try {
							smiles = ChemUtils.canonical(smiles0, true);
						} catch (Exception e) {
							Platform.runLater(new Runnable() {
								public void run() {
									Alert a = new Alert(AlertType.ERROR);
									a.setContentText("Bad SMILES \"" + smiles0 + "\"!!!"
											+ " This function only works with valid SMILES strings."
											+ " If your molecules are in the form of names etc. - convert them to SMILES.");
									a.showAndWait();
									try {
										wb.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							});
							throw new RuntimeException();
						}
						if (smiles != null) {
							Row row1 = sheet.createRow(i);
							row1.createCell(0).setCellValue(smiles);
							BufferedImage bi = ChemUtils.smilesToImageAsBufferedImage(smiles);
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							ImageIO.write(bi, "png", os);
							byte[] img1 = os.toByteArray();
							int id = wb.addPicture(img1, Workbook.PICTURE_TYPE_PNG);
							XSSFDrawing d = (XSSFDrawing) sheet.createDrawingPatriarch();
							XSSFClientAnchor anchor = new XSSFClientAnchor();
							anchor.setCol1(1);
							anchor.setCol2(2);
							anchor.setRow1(i);
							anchor.setRow2(i + 1);
							d.createPicture(anchor, id);
							int size = Integer.parseInt(tfSizeInPixels.getText());
							sheet.setColumnWidth(1, Math.round(size / Units.DEFAULT_CHARACTER_WIDTH * 256f));
							row1.setHeight((short) Math.round(size / 12.75f * 256f));
						}
					}

					FileOutputStream saveExcel = new FileOutputStream(filename);
					wb.write(saveExcel);
					saveExcel.close();
					wb.close();
				} catch (Exception e) {
					e.printStackTrace();
					throw (new RuntimeException("XLSX error"));
				}
			}
		});

		EventHandler<ActionEvent> runMolecularFormulae = (new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String[] linesA = taMfA.getText().split("\\n");
				String[] linesB = taMfB.getText().split("\\n");
				for (int i = 0; i < linesA.length; i++) {
					linesA[i] = linesA[i].trim();
				}
				for (int i = 0; i < linesB.length; i++) {
					linesB[i] = linesB[i].trim();
				}
				String result = "Data set A\n";
				result = result
						+ concatNewLines(MolecularFormula.lineNumbersAsInAWithFormulasFromB(linesB, linesA, "B"))
						+ "\n\n";
				result = result + "Data set B\n";
				result = result
						+ concatNewLines(MolecularFormula.lineNumbersAsInAWithFormulasFromB(linesA, linesB, "A"))
						+ "\n\n";
				result = result + "Isomers A\n";
				result = result
						+ concatNewLines(MolecularFormula.lineNumbersAsInAWithFormulasFromB(linesA, linesA, "A"))
						+ "\n\n";
				result = result + "Isomers B\n";
				result = result
						+ concatNewLines(MolecularFormula.lineNumbersAsInAWithFormulasFromB(linesB, linesB, "B"))
						+ "\n\n";
				result = result + "Molecular formulae ONLY A\n";
				result = result + concatNewLines(MolecularFormula.onlyA(linesA, linesB)) + "\n\n";
				result = result + "Molecular formulae ONLY B\n";
				result = result + concatNewLines(MolecularFormula.onlyA(linesB, linesA)) + "\n\n";
				result = result + "Molecular formulae BOTH A, B\n";
				result = result + concatNewLines(MolecularFormula.overlap(linesB, linesA));
				taMfOut.setText(result);
			}
		});

		EventHandler<ActionEvent> runFileSelectXLSX = (new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialFileName("compounds.xlsx");
				File file = fileChooser.showSaveDialog(primaryStage);
				if (file != null) {
					tfXlsxFileName.setText(file.getAbsolutePath());
				}
			}
		});

		Timeline timer = new Timeline(new KeyFrame(Duration.seconds(0.5), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (!jsJavaCall.smiles.equals(jsJavaCall.oldSmiles)) {
					jsJavaCall.oldSmiles = jsJavaCall.smiles;
					tfSmiles.setText(jsJavaCall.smiles);
				}
			}
		}));

		timer.setCycleCount(Timeline.INDEFINITE);
		timer.play();

		webView.setOnKeyPressed(e -> {
			if ((e.getCode() == KeyCode.ENTER) || (e.getCode() == KeyCode.SPACE) || (e.getCode() == KeyCode.SHIFT)) {
				taDraw.setText(taDraw.getText() + "\n" + jsJavaCall.smiles);
			}
		});

		button1.setOnAction(runConversion);
		button2.setOnAction(runStop);
		button3.setOnAction(runIntersection);
		button4.setOnAction(runAddSmiles);
		button5.setOnAction(runImagesXLSX);
		button6.setOnAction(runMolecularFormulae);
		buttonxlsx.setOnAction(runFileSelectXLSX);

	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
