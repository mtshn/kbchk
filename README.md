## Download here:
https://github.com/mtshn/kbchk/releases

## Very simple software for managing lists of molecules
Software for converting names (both IUPAC and trivial), SMILES strings, CAS numbers, CID numbers (PubChem ids), InChI strings into each other. You can take a list of molecule names and turn it into a list of SMILES strings or vice versa. The software accesses PubChem via API and the Cactus server (https://cactus.nci.nih.gov/). Internet access is required, does not work without the Internet.

You can also draw structures and make a list of them (JSME javascript-based editor is used), look for intersections in lists of molecules, calculate molecular formulas and find isomers.

JSME is included: 3-clause BSD licensed, https://github.com/jsme-editor/jsme-editor.github.io
Bienfait B., Ertl P. JSME: a free molecule editor in JavaScript //Journal of cheminformatics. – 2013. – Т. 5. – №. 1. – С. 24.

Can be compiled via Maven (mvn package). JAVA-based software. Windows release includes Java Runtime Environment. Full list of dependencies: see pom.xml.


