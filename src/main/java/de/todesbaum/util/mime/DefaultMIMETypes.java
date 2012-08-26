/* taken from freenet (http://www.freenetproject.org/) */
package de.todesbaum.util.mime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Holds the default MIME types.
 */
public class DefaultMIMETypes {

	/** Default MIME type - what to set it to if we don't know any better */
	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	/** MIME types: number -> name */
	private static List<String> mimeTypesByNumber = new Vector<String>();

	/** MIME types: name -> number */
	private static Map<String, Short> mimeTypesByName = new HashMap<String, Short>();

	/** MIME types by extension. One extension maps to one MIME type, but not necessarily
	 * the other way around. */
	private static Map<String, Short> mimeTypesByExtension = new HashMap<String, Short>();

	/** Primary extension by MIME type number. */
	private static Map<Short, String> primaryExtensionByMimeNumber = new HashMap<Short, String>();

	/**
	 * Add a MIME type, without any extensions.
	 * @param number The number of the MIME type for compression. This *must not change*
	 * for a given type, or the metadata format will be affected.
	 * @param type The actual MIME type string. Do not include ;charset= etc; these are
	 * parameters and there is a separate mechanism for them.
	 */
	protected static synchronized void addMIMEType(short number, String type) {
		if(mimeTypesByNumber.size() > number) {
			String s = mimeTypesByNumber.get(number);
			if(s != null) throw new IllegalArgumentException("Already used: "+number);
		} else {
			mimeTypesByNumber.add(number, null);
		}
		mimeTypesByNumber.set(number, type);
		mimeTypesByName.put(type, new Short(number));
	}

	/**
	 * Add a MIME type.
	 * @param number The number of the MIME type for compression. This *must not change*
	 * for a given type, or the metadata format will be affected.
	 * @param type The actual MIME type string. Do not include ;charset= etc; these are
	 * parameters and there is a separate mechanism for them.
	 * @param extensions An array of common extensions for files of this type. Must be
	 * unique for the type.
	 */
	protected static synchronized void addMIMEType(short number, String type, String[] extensions, String outExtension) {
		addMIMEType(number, type);
		Short t = new Short(number);
		if(extensions != null) {
			for(int i=0;i<extensions.length;i++) {
				String ext = extensions[i].toLowerCase();
				if(mimeTypesByExtension.containsKey(ext)) {
					// No big deal
					//Short s = mimeTypesByExtension.get(ext);
				} else {
					// If only one, make it primary
					if(outExtension == null && extensions.length == 1)
						primaryExtensionByMimeNumber.put(t, ext);
					mimeTypesByExtension.put(ext, t);
				}
			}
		}
		if(outExtension != null)
			primaryExtensionByMimeNumber.put(t, outExtension);

	}

	/**
	 * Add a MIME type, with extensions separated by spaces. This is more or less
	 * the format in /etc/mime-types.
	 */
	protected static synchronized void addMIMEType(short number, String type, String extensions) {
		addMIMEType(number, type, extensions.split(" "), null);
	}

	/**
	 * Add a MIME type, with extensions separated by spaces. This is more or less
	 * the format in /etc/mime-types.
	 */
	protected static synchronized void addMIMEType(short number, String type, String extensions, String outExtension) {
		addMIMEType(number, type, extensions.split(" "), outExtension);
	}

	/**
	 * Get a known MIME type by number.
	 */
	public static String byNumber(short x) {
		if(x > mimeTypesByNumber.size() || x < 0)
			return null;
		return mimeTypesByNumber.get(x);
	}

	/**
	 * Get the number of a MIME type, or -1 if it is not in the table of known MIME
	 * types, in which case it will have to be sent uncompressed.
	 */
	public static short byName(String s) {
		Short x = mimeTypesByName.get(s);
		if(x != null) return x.shortValue();
		return -1;
	}

	/* From toad's /etc/mime.types
	 * cat /etc/mime.types | sed "/^$/d;/#/d" | tr --squeeze '\t' ' ' |
	 * (y=0; while read x; do echo "$x" |
	 * sed -n "s/^\([^ ]*\)$/addMIMEType\($y, \"\1\"\);/p;s/^\([^ (),]\+\) \(.*\)$/addMIMEType\($y, \"\1\", \"\2\"\);/p;"; y=$((y+1)); done)
	 */

	static {
		addMIMEType((short) 0, "application/activemessage");
		addMIMEType((short) 1, "application/andrew-inset", "ez");
		addMIMEType((short) 2, "application/applefile");
		addMIMEType((short) 3, "application/atomicmail");
		addMIMEType((short) 4, "application/batch-SMTP");
		addMIMEType((short) 5, "application/beep+xml");
		addMIMEType((short) 6, "application/cals-1840");
		addMIMEType((short) 7, "application/commonground");
		addMIMEType((short) 8, "application/cu-seeme", "cu");
		addMIMEType((short) 9, "application/cybercash");
		addMIMEType((short) 10, "application/dca-rft");
		addMIMEType((short) 11, "application/dec-dx");
		addMIMEType((short) 12, "application/docbook+xml");
		addMIMEType((short) 13, "application/dsptype", "tsp");
		addMIMEType((short) 14, "application/dvcs");
		addMIMEType((short) 15, "application/edi-consent");
		addMIMEType((short) 16, "application/edi-x12");
		addMIMEType((short) 17, "application/edifact");
		addMIMEType((short) 18, "application/eshop");
		addMIMEType((short) 19, "application/font-tdpfr");
		addMIMEType((short) 20, "application/futuresplash", "spl");
		addMIMEType((short) 21, "application/ghostview");
		addMIMEType((short) 22, "application/hta", "hta");
		addMIMEType((short) 23, "application/http");
		addMIMEType((short) 24, "application/hyperstudio");
		addMIMEType((short) 25, "application/iges");
		addMIMEType((short) 26, "application/index");
		addMIMEType((short) 27, "application/index.cmd");
		addMIMEType((short) 28, "application/index.obj");
		addMIMEType((short) 29, "application/index.response");
		addMIMEType((short) 30, "application/index.vnd");
		addMIMEType((short) 31, "application/iotp");
		addMIMEType((short) 32, "application/ipp");
		addMIMEType((short) 33, "application/isup");
		addMIMEType((short) 34, "application/java-archive", "jar");
		addMIMEType((short) 35, "application/java-serialized-object", "ser");
		addMIMEType((short) 36, "application/java-vm", "class");
		addMIMEType((short) 37, "application/mac-binhex40", "hqx");
		addMIMEType((short) 38, "application/mac-compactpro", "cpt");
		addMIMEType((short) 39, "application/macwriteii");
		addMIMEType((short) 40, "application/marc");
		addMIMEType((short) 41, "application/mathematica", "nb");
		addMIMEType((short) 42, "application/mathematica-old");
		addMIMEType((short) 43, "application/msaccess", "mdb");
		addMIMEType((short) 44, "application/msword", "doc dot");
		addMIMEType((short) 45, "application/news-message-id");
		addMIMEType((short) 46, "application/news-transmission");
		addMIMEType((short) 47, "application/ocsp-request");
		addMIMEType((short) 48, "application/ocsp-response");
		addMIMEType((short) 49, "application/octet-stream", "bin");
		addMIMEType((short) 50, "application/oda", "oda");
		addMIMEType((short) 51, "application/ogg", "ogg");
		addMIMEType((short) 52, "application/parityfec");
		addMIMEType((short) 53, "application/pdf", "pdf");
		addMIMEType((short) 54, "application/pgp-encrypted");
		addMIMEType((short) 55, "application/pgp-keys", "key");
		addMIMEType((short) 56, "application/pgp-signature", "pgp");
		addMIMEType((short) 57, "application/pics-rules", "prf");
		addMIMEType((short) 58, "application/pkcs10");
		addMIMEType((short) 59, "application/pkcs7-mime");
		addMIMEType((short) 60, "application/pkcs7-signature");
		addMIMEType((short) 61, "application/pkix-cert");
		addMIMEType((short) 62, "application/pkix-crl");
		addMIMEType((short) 63, "application/pkixcmp");
		addMIMEType((short) 64, "application/postscript", "ps ai eps");
		addMIMEType((short) 65, "application/prs.alvestrand.titrax-sheet");
		addMIMEType((short) 66, "application/prs.cww");
		addMIMEType((short) 67, "application/prs.nprend");
		addMIMEType((short) 68, "application/qsig");
		addMIMEType((short) 69, "application/rar", "rar");
		addMIMEType((short) 70, "application/rdf+xml", "rdf");
		addMIMEType((short) 71, "application/remote-printing");
		addMIMEType((short) 72, "application/riscos");
		addMIMEType((short) 73, "application/rss+xml", "rss");
		addMIMEType((short) 74, "application/rtf");
		addMIMEType((short) 75, "application/sdp");
		addMIMEType((short) 76, "application/set-payment");
		addMIMEType((short) 77, "application/set-payment-initiation");
		addMIMEType((short) 78, "application/set-registration");
		addMIMEType((short) 79, "application/set-registration-initiation");
		addMIMEType((short) 80, "application/sgml");
		addMIMEType((short) 81, "application/sgml-open-catalog");
		addMIMEType((short) 82, "application/sieve");
		addMIMEType((short) 83, "application/slate");
		addMIMEType((short) 84, "application/smil", "smi smil");
		addMIMEType((short) 85, "application/timestamp-query");
		addMIMEType((short) 86, "application/timestamp-reply");
		addMIMEType((short) 87, "application/vemmi");
		addMIMEType((short) 88, "application/whoispp-query");
		addMIMEType((short) 89, "application/whoispp-response");
		addMIMEType((short) 90, "application/wita");
		addMIMEType((short) 91, "application/wordperfect", "wpd");
		addMIMEType((short) 92, "application/wordperfect5.1", "wp5");
		addMIMEType((short) 93, "application/x400-bp");
		addMIMEType((short) 94, "application/xhtml+xml", "xhtml xht");
		addMIMEType((short) 95, "application/xml", "xml xsl");
		addMIMEType((short) 96, "application/xml-dtd");
		addMIMEType((short) 97, "application/xml-external-parsed-entity");
		addMIMEType((short) 98, "application/zip", "zip");
		addMIMEType((short) 99, "application/vnd.3M.Post-it-Notes");
		addMIMEType((short) 100, "application/vnd.accpac.simply.aso");
		addMIMEType((short) 101, "application/vnd.accpac.simply.imp");
		addMIMEType((short) 102, "application/vnd.acucobol");
		addMIMEType((short) 103, "application/vnd.aether.imp");
		addMIMEType((short) 104, "application/vnd.anser-web-certificate-issue-initiation");
		addMIMEType((short) 105, "application/vnd.anser-web-funds-transfer-initiation");
		addMIMEType((short) 106, "application/vnd.audiograph");
		addMIMEType((short) 107, "application/vnd.bmi");
		addMIMEType((short) 108, "application/vnd.businessobjects");
		addMIMEType((short) 109, "application/vnd.canon-cpdl");
		addMIMEType((short) 110, "application/vnd.canon-lips");
		addMIMEType((short) 111, "application/vnd.cinderella", "cdy");
		addMIMEType((short) 112, "application/vnd.claymore");
		addMIMEType((short) 113, "application/vnd.commerce-battelle");
		addMIMEType((short) 114, "application/vnd.commonspace");
		addMIMEType((short) 115, "application/vnd.comsocaller");
		addMIMEType((short) 116, "application/vnd.contact.cmsg");
		addMIMEType((short) 117, "application/vnd.cosmocaller");
		addMIMEType((short) 118, "application/vnd.ctc-posml");
		addMIMEType((short) 119, "application/vnd.cups-postscript");
		addMIMEType((short) 120, "application/vnd.cups-raster");
		addMIMEType((short) 121, "application/vnd.cups-raw");
		addMIMEType((short) 122, "application/vnd.cybank");
		addMIMEType((short) 123, "application/vnd.dna");
		addMIMEType((short) 124, "application/vnd.dpgraph");
		addMIMEType((short) 125, "application/vnd.dxr");
		addMIMEType((short) 126, "application/vnd.ecdis-update");
		addMIMEType((short) 127, "application/vnd.ecowin.chart");
		addMIMEType((short) 128, "application/vnd.ecowin.filerequest");
		addMIMEType((short) 129, "application/vnd.ecowin.fileupdate");
		addMIMEType((short) 130, "application/vnd.ecowin.series");
		addMIMEType((short) 131, "application/vnd.ecowin.seriesrequest");
		addMIMEType((short) 132, "application/vnd.ecowin.seriesupdate");
		addMIMEType((short) 133, "application/vnd.enliven");
		addMIMEType((short) 134, "application/vnd.epson.esf");
		addMIMEType((short) 135, "application/vnd.epson.msf");
		addMIMEType((short) 136, "application/vnd.epson.quickanime");
		addMIMEType((short) 137, "application/vnd.epson.salt");
		addMIMEType((short) 138, "application/vnd.epson.ssf");
		addMIMEType((short) 139, "application/vnd.ericsson.quickcall");
		addMIMEType((short) 140, "application/vnd.eudora.data");
		addMIMEType((short) 141, "application/vnd.fdf");
		addMIMEType((short) 142, "application/vnd.ffsns");
		addMIMEType((short) 143, "application/vnd.flographit");
		addMIMEType((short) 144, "application/vnd.framemaker");
		addMIMEType((short) 145, "application/vnd.fsc.weblaunch");
		addMIMEType((short) 146, "application/vnd.fujitsu.oasys");
		addMIMEType((short) 147, "application/vnd.fujitsu.oasys2");
		addMIMEType((short) 148, "application/vnd.fujitsu.oasys3");
		addMIMEType((short) 149, "application/vnd.fujitsu.oasysgp");
		addMIMEType((short) 150, "application/vnd.fujitsu.oasysprs");
		addMIMEType((short) 151, "application/vnd.fujixerox.ddd");
		addMIMEType((short) 152, "application/vnd.fujixerox.docuworks");
		addMIMEType((short) 153, "application/vnd.fujixerox.docuworks.binder");
		addMIMEType((short) 154, "application/vnd.fut-misnet");
		addMIMEType((short) 155, "application/vnd.grafeq");
		addMIMEType((short) 156, "application/vnd.groove-account");
		addMIMEType((short) 157, "application/vnd.groove-identity-message");
		addMIMEType((short) 158, "application/vnd.groove-injector");
		addMIMEType((short) 159, "application/vnd.groove-tool-message");
		addMIMEType((short) 160, "application/vnd.groove-tool-template");
		addMIMEType((short) 161, "application/vnd.groove-vcard");
		addMIMEType((short) 162, "application/vnd.hhe.lesson-player");
		addMIMEType((short) 163, "application/vnd.hp-HPGL");
		addMIMEType((short) 164, "application/vnd.hp-PCL");
		addMIMEType((short) 165, "application/vnd.hp-PCLXL");
		addMIMEType((short) 166, "application/vnd.hp-hpid");
		addMIMEType((short) 167, "application/vnd.hp-hps");
		addMIMEType((short) 168, "application/vnd.httphone");
		addMIMEType((short) 169, "application/vnd.hzn-3d-crossword");
		addMIMEType((short) 170, "application/vnd.ibm.MiniPay");
		addMIMEType((short) 171, "application/vnd.ibm.afplinedata");
		addMIMEType((short) 172, "application/vnd.ibm.modcap");
		addMIMEType((short) 173, "application/vnd.informix-visionary");
		addMIMEType((short) 174, "application/vnd.intercon.formnet");
		addMIMEType((short) 175, "application/vnd.intertrust.digibox");
		addMIMEType((short) 176, "application/vnd.intertrust.nncp");
		addMIMEType((short) 177, "application/vnd.intu.qbo");
		addMIMEType((short) 178, "application/vnd.intu.qfx");
		addMIMEType((short) 179, "application/vnd.irepository.package+xml");
		addMIMEType((short) 180, "application/vnd.is-xpr");
		addMIMEType((short) 181, "application/vnd.japannet-directory-service");
		addMIMEType((short) 182, "application/vnd.japannet-jpnstore-wakeup");
		addMIMEType((short) 183, "application/vnd.japannet-payment-wakeup");
		addMIMEType((short) 184, "application/vnd.japannet-registration");
		addMIMEType((short) 185, "application/vnd.japannet-registration-wakeup");
		addMIMEType((short) 186, "application/vnd.japannet-setstore-wakeup");
		addMIMEType((short) 187, "application/vnd.japannet-verification");
		addMIMEType((short) 188, "application/vnd.japannet-verification-wakeup");
		addMIMEType((short) 189, "application/vnd.koan");
		addMIMEType((short) 190, "application/vnd.lotus-1-2-3");
		addMIMEType((short) 191, "application/vnd.lotus-approach");
		addMIMEType((short) 192, "application/vnd.lotus-freelance");
		addMIMEType((short) 193, "application/vnd.lotus-notes");
		addMIMEType((short) 194, "application/vnd.lotus-organizer");
		addMIMEType((short) 195, "application/vnd.lotus-screencam");
		addMIMEType((short) 196, "application/vnd.lotus-wordpro");
		addMIMEType((short) 197, "application/vnd.mcd");
		addMIMEType((short) 198, "application/vnd.mediastation.cdkey");
		addMIMEType((short) 199, "application/vnd.meridian-slingshot");
		addMIMEType((short) 200, "application/vnd.mif");
		addMIMEType((short) 201, "application/vnd.minisoft-hp3000-save");
		addMIMEType((short) 202, "application/vnd.mitsubishi.misty-guard.trustweb");
		addMIMEType((short) 203, "application/vnd.mobius.daf");
		addMIMEType((short) 204, "application/vnd.mobius.dis");
		addMIMEType((short) 205, "application/vnd.mobius.msl");
		addMIMEType((short) 206, "application/vnd.mobius.plc");
		addMIMEType((short) 207, "application/vnd.mobius.txf");
		addMIMEType((short) 208, "application/vnd.motorola.flexsuite");
		addMIMEType((short) 209, "application/vnd.motorola.flexsuite.adsi");
		addMIMEType((short) 210, "application/vnd.motorola.flexsuite.fis");
		addMIMEType((short) 211, "application/vnd.motorola.flexsuite.gotap");
		addMIMEType((short) 212, "application/vnd.motorola.flexsuite.kmr");
		addMIMEType((short) 213, "application/vnd.motorola.flexsuite.ttc");
		addMIMEType((short) 214, "application/vnd.motorola.flexsuite.wem");
		addMIMEType((short) 215, "application/vnd.mozilla.xul+xml", "xul");
		addMIMEType((short) 216, "application/vnd.ms-artgalry");
		addMIMEType((short) 217, "application/vnd.ms-asf");
		addMIMEType((short) 218, "application/vnd.ms-excel", "xls xlb xlt");
		addMIMEType((short) 219, "application/vnd.ms-lrm");
		addMIMEType((short) 220, "application/vnd.ms-pki.seccat", "cat");
		addMIMEType((short) 221, "application/vnd.ms-pki.stl", "stl");
		addMIMEType((short) 222, "application/vnd.ms-powerpoint", "ppt pps");
		addMIMEType((short) 223, "application/vnd.ms-project");
		addMIMEType((short) 224, "application/vnd.ms-tnef");
		addMIMEType((short) 225, "application/vnd.ms-works");
		addMIMEType((short) 226, "application/vnd.mseq");
		addMIMEType((short) 227, "application/vnd.msign");
		addMIMEType((short) 228, "application/vnd.music-niff");
		addMIMEType((short) 229, "application/vnd.musician");
		addMIMEType((short) 230, "application/vnd.netfpx");
		addMIMEType((short) 231, "application/vnd.noblenet-directory");
		addMIMEType((short) 232, "application/vnd.noblenet-sealer");
		addMIMEType((short) 233, "application/vnd.noblenet-web");
		addMIMEType((short) 234, "application/vnd.novadigm.EDM");
		addMIMEType((short) 235, "application/vnd.novadigm.EDX");
		addMIMEType((short) 236, "application/vnd.novadigm.EXT");
		addMIMEType((short) 237, "application/vnd.oasis.opendocument.chart", "odc");
		addMIMEType((short) 238, "application/vnd.oasis.opendocument.database", "odb");
		addMIMEType((short) 239, "application/vnd.oasis.opendocument.formula", "odf");
		addMIMEType((short) 240, "application/vnd.oasis.opendocument.graphics", "odg");
		addMIMEType((short) 241, "application/vnd.oasis.opendocument.graphics-template", "otg");
		addMIMEType((short) 242, "application/vnd.oasis.opendocument.image", "odi");
		addMIMEType((short) 243, "application/vnd.oasis.opendocument.presentation", "odp");
		addMIMEType((short) 244, "application/vnd.oasis.opendocument.presentation-template", "otp");
		addMIMEType((short) 245, "application/vnd.oasis.opendocument.spreadsheet", "ods");
		addMIMEType((short) 246, "application/vnd.oasis.opendocument.spreadsheet-template", "ots");
		addMIMEType((short) 247, "application/vnd.oasis.opendocument.text", "odt");
		addMIMEType((short) 248, "application/vnd.oasis.opendocument.text-master", "odm");
		addMIMEType((short) 249, "application/vnd.oasis.opendocument.text-template", "ott");
		addMIMEType((short) 250, "application/vnd.oasis.opendocument.text-web", "oth");
		addMIMEType((short) 251, "application/vnd.osa.netdeploy");
		addMIMEType((short) 252, "application/vnd.palm");
		addMIMEType((short) 253, "application/vnd.pg.format");
		addMIMEType((short) 254, "application/vnd.pg.osasli");
		addMIMEType((short) 255, "application/vnd.powerbuilder6");
		addMIMEType((short) 256, "application/vnd.powerbuilder6-s");
		addMIMEType((short) 257, "application/vnd.powerbuilder7");
		addMIMEType((short) 258, "application/vnd.powerbuilder7-s");
		addMIMEType((short) 259, "application/vnd.powerbuilder75");
		addMIMEType((short) 260, "application/vnd.powerbuilder75-s");
		addMIMEType((short) 261, "application/vnd.previewsystems.box");
		addMIMEType((short) 262, "application/vnd.publishare-delta-tree");
		addMIMEType((short) 263, "application/vnd.pvi.ptid1");
		addMIMEType((short) 264, "application/vnd.pwg-xhtml-print+xml");
		addMIMEType((short) 265, "application/vnd.rapid");
		addMIMEType((short) 266, "application/vnd.rim.cod", "cod");
		addMIMEType((short) 267, "application/vnd.s3sms");
		addMIMEType((short) 268, "application/vnd.seemail");
		addMIMEType((short) 269, "application/vnd.shana.informed.formdata");
		addMIMEType((short) 270, "application/vnd.shana.informed.formtemplate");
		addMIMEType((short) 271, "application/vnd.shana.informed.interchange");
		addMIMEType((short) 272, "application/vnd.shana.informed.package");
		addMIMEType((short) 273, "application/vnd.smaf", "mmf");
		addMIMEType((short) 274, "application/vnd.sss-cod");
		addMIMEType((short) 275, "application/vnd.sss-dtf");
		addMIMEType((short) 276, "application/vnd.sss-ntf");
		addMIMEType((short) 277, "application/vnd.stardivision.calc", "sdc");
		addMIMEType((short) 278, "application/vnd.stardivision.draw", "sda");
		addMIMEType((short) 279, "application/vnd.stardivision.impress", "sdd sdp");
		addMIMEType((short) 280, "application/vnd.stardivision.math", "smf");
		addMIMEType((short) 281, "application/vnd.stardivision.writer", "sdw vor");
		addMIMEType((short) 282, "application/vnd.stardivision.writer-global", "sgl");
		addMIMEType((short) 283, "application/vnd.street-stream");
		addMIMEType((short) 284, "application/vnd.sun.xml.calc", "sxc");
		addMIMEType((short) 285, "application/vnd.sun.xml.calc.template", "stc");
		addMIMEType((short) 286, "application/vnd.sun.xml.draw", "sxd");
		addMIMEType((short) 287, "application/vnd.sun.xml.draw.template", "std");
		addMIMEType((short) 288, "application/vnd.sun.xml.impress", "sxi");
		addMIMEType((short) 289, "application/vnd.sun.xml.impress.template", "sti");
		addMIMEType((short) 290, "application/vnd.sun.xml.math", "sxm");
		addMIMEType((short) 291, "application/vnd.sun.xml.writer", "sxw");
		addMIMEType((short) 292, "application/vnd.sun.xml.writer.global", "sxg");
		addMIMEType((short) 293, "application/vnd.sun.xml.writer.template", "stw");
		addMIMEType((short) 294, "application/vnd.svd");
		addMIMEType((short) 295, "application/vnd.swiftview-ics");
		addMIMEType((short) 296, "application/vnd.symbian.install", "sis");
		addMIMEType((short) 297, "application/vnd.triscape.mxs");
		addMIMEType((short) 298, "application/vnd.trueapp");
		addMIMEType((short) 299, "application/vnd.truedoc");
		addMIMEType((short) 300, "application/vnd.tve-trigger");
		addMIMEType((short) 301, "application/vnd.ufdl");
		addMIMEType((short) 302, "application/vnd.uplanet.alert");
		addMIMEType((short) 303, "application/vnd.uplanet.alert-wbxml");
		addMIMEType((short) 304, "application/vnd.uplanet.bearer-choice");
		addMIMEType((short) 305, "application/vnd.uplanet.bearer-choice-wbxml");
		addMIMEType((short) 306, "application/vnd.uplanet.cacheop");
		addMIMEType((short) 307, "application/vnd.uplanet.cacheop-wbxml");
		addMIMEType((short) 308, "application/vnd.uplanet.channel");
		addMIMEType((short) 309, "application/vnd.uplanet.channel-wbxml");
		addMIMEType((short) 310, "application/vnd.uplanet.list");
		addMIMEType((short) 311, "application/vnd.uplanet.list-wbxml");
		addMIMEType((short) 312, "application/vnd.uplanet.listcmd");
		addMIMEType((short) 313, "application/vnd.uplanet.listcmd-wbxml");
		addMIMEType((short) 314, "application/vnd.uplanet.signal");
		addMIMEType((short) 315, "application/vnd.vcx");
		addMIMEType((short) 316, "application/vnd.vectorworks");
		addMIMEType((short) 317, "application/vnd.vidsoft.vidconference");
		addMIMEType((short) 318, "application/vnd.visio", "vsd");
		addMIMEType((short) 319, "application/vnd.vividence.scriptfile");
		addMIMEType((short) 320, "application/vnd.wap.sic");
		addMIMEType((short) 321, "application/vnd.wap.slc");
		addMIMEType((short) 322, "application/vnd.wap.wbxml", "wbxml");
		addMIMEType((short) 323, "application/vnd.wap.wmlc", "wmlc");
		addMIMEType((short) 324, "application/vnd.wap.wmlscriptc", "wmlsc");
		addMIMEType((short) 325, "application/vnd.webturbo");
		addMIMEType((short) 326, "application/vnd.wrq-hp3000-labelled");
		addMIMEType((short) 327, "application/vnd.wt.stf");
		addMIMEType((short) 328, "application/vnd.xara");
		addMIMEType((short) 329, "application/vnd.xfdl");
		addMIMEType((short) 330, "application/vnd.yellowriver-custom-menu");
		addMIMEType((short) 331, "application/x-123", "wk");
		addMIMEType((short) 332, "application/x-abiword", "abw");
		addMIMEType((short) 333, "application/x-apple-diskimage", "dmg");
		addMIMEType((short) 334, "application/x-bcpio", "bcpio");
		addMIMEType((short) 335, "application/x-bittorrent", "torrent");
		addMIMEType((short) 336, "application/x-cdf", "cdf");
		addMIMEType((short) 337, "application/x-cdlink", "vcd");
		addMIMEType((short) 338, "application/x-chess-pgn", "pgn");
		addMIMEType((short) 339, "application/x-core");
		addMIMEType((short) 340, "application/x-cpio", "cpio");
		addMIMEType((short) 341, "application/x-csh", "csh");
		addMIMEType((short) 342, "application/x-debian-package", "deb udeb");
		addMIMEType((short) 343, "application/x-director", "dcr dir dxr");
		addMIMEType((short) 344, "application/x-dms", "dms");
		addMIMEType((short) 345, "application/x-doom", "wad");
		addMIMEType((short) 346, "application/x-dvi", "dvi");
		addMIMEType((short) 347, "application/x-executable");
		addMIMEType((short) 348, "application/x-flac", "flac");
		addMIMEType((short) 349, "application/x-font", "pfa pfb gsf pcf pcf.Z");
		addMIMEType((short) 350, "application/x-freemind", "mm");
		addMIMEType((short) 351, "application/x-futuresplash", "spl");
		addMIMEType((short) 352, "application/x-gnumeric", "gnumeric");
		addMIMEType((short) 353, "application/x-go-sgf", "sgf");
		addMIMEType((short) 354, "application/x-graphing-calculator", "gcf");
		addMIMEType((short) 355, "application/x-gtar", "gtar tgz taz");
		addMIMEType((short) 356, "application/x-hdf", "hdf");
		addMIMEType((short) 357, "application/x-httpd-php", "phtml pht php");
		addMIMEType((short) 358, "application/x-httpd-php-source", "phps");
		addMIMEType((short) 359, "application/x-httpd-php3", "php3");
		addMIMEType((short) 360, "application/x-httpd-php3-preprocessed", "php3p");
		addMIMEType((short) 361, "application/x-httpd-php4", "php4");
		addMIMEType((short) 362, "application/x-ica", "ica");
		addMIMEType((short) 363, "application/x-internet-signup", "ins isp");
		addMIMEType((short) 364, "application/x-iphone", "iii");
		addMIMEType((short) 365, "application/x-iso9660-image", "iso");
		addMIMEType((short) 366, "application/x-java-applet");
		addMIMEType((short) 367, "application/x-java-bean");
		addMIMEType((short) 368, "application/x-java-jnlp-file", "jnlp");
		addMIMEType((short) 369, "application/x-javascript", "js");
		addMIMEType((short) 370, "application/x-jmol", "jmz");
		addMIMEType((short) 371, "application/x-kchart", "chrt");
		addMIMEType((short) 372, "application/x-kdelnk");
		addMIMEType((short) 373, "application/x-killustrator", "kil");
		addMIMEType((short) 374, "application/x-koan", "skp skd skt skm");
		addMIMEType((short) 375, "application/x-kpresenter", "kpr kpt");
		addMIMEType((short) 376, "application/x-kspread", "ksp");
		addMIMEType((short) 377, "application/x-kword", "kwd kwt");
		addMIMEType((short) 378, "application/x-latex", "latex");
		addMIMEType((short) 379, "application/x-lha", "lha");
		addMIMEType((short) 380, "application/x-lzh", "lzh");
		addMIMEType((short) 381, "application/x-lzx", "lzx");
		addMIMEType((short) 382, "application/x-maker", "frm maker frame fm fb book fbdoc");
		addMIMEType((short) 383, "application/x-mif", "mif");
		addMIMEType((short) 384, "application/x-ms-wmd", "wmd");
		addMIMEType((short) 385, "application/x-ms-wmz", "wmz");
		addMIMEType((short) 386, "application/x-msdos-program", "com exe bat dll");
		addMIMEType((short) 387, "application/x-msi", "msi");
		addMIMEType((short) 388, "application/x-netcdf", "nc");
		addMIMEType((short) 389, "application/x-ns-proxy-autoconfig", "pac");
		addMIMEType((short) 390, "application/x-nwc", "nwc");
		addMIMEType((short) 391, "application/x-object", "o");
		addMIMEType((short) 392, "application/x-oz-application", "oza");
		addMIMEType((short) 393, "application/x-pkcs7-certreqresp", "p7r");
		addMIMEType((short) 394, "application/x-pkcs7-crl", "crl");
		addMIMEType((short) 395, "application/x-python-code", "pyc pyo");
		addMIMEType((short) 396, "application/x-quicktimeplayer", "qtl");
		addMIMEType((short) 397, "application/x-redhat-package-manager", "rpm");
		addMIMEType((short) 398, "application/x-rx");
		addMIMEType((short) 399, "application/x-sh", "sh");
		addMIMEType((short) 400, "application/x-shar", "shar");
		addMIMEType((short) 401, "application/x-shellscript");
		addMIMEType((short) 402, "application/x-shockwave-flash", "swf swfl");
		addMIMEType((short) 403, "application/x-stuffit", "sit");
		addMIMEType((short) 404, "application/x-sv4cpio", "sv4cpio");
		addMIMEType((short) 405, "application/x-sv4crc", "sv4crc");
		addMIMEType((short) 406, "application/x-tar", "tar");
		addMIMEType((short) 407, "application/x-tcl", "tcl");
		addMIMEType((short) 408, "application/x-tex-gf", "gf");
		addMIMEType((short) 409, "application/x-tex-pk", "pk");
		addMIMEType((short) 410, "application/x-texinfo", "texinfo texi");
		addMIMEType((short) 411, "application/x-trash", "~ % bak old sik");
		addMIMEType((short) 412, "application/x-troff", "t tr roff");
		addMIMEType((short) 413, "application/x-troff-man", "man");
		addMIMEType((short) 414, "application/x-troff-me", "me");
		addMIMEType((short) 415, "application/x-troff-ms", "ms");
		addMIMEType((short) 416, "application/x-ustar", "ustar");
		addMIMEType((short) 417, "application/x-videolan");
		addMIMEType((short) 418, "application/x-wais-source", "src");
		addMIMEType((short) 419, "application/x-wingz", "wz");
		addMIMEType((short) 420, "application/x-x509-ca-cert", "crt");
		addMIMEType((short) 421, "application/x-xcf", "xcf");
		addMIMEType((short) 422, "application/x-xfig", "fig");
		addMIMEType((short) 423, "application/x-xpinstall", "xpi");
		addMIMEType((short) 424, "audio/32kadpcm");
		addMIMEType((short) 425, "audio/basic", "au snd");
		addMIMEType((short) 426, "audio/g.722.1");
		addMIMEType((short) 427, "audio/l16");
		addMIMEType((short) 428, "audio/midi", "mid midi kar");
		addMIMEType((short) 429, "audio/mp4a-latm");
		addMIMEType((short) 430, "audio/mpa-robust");
		addMIMEType((short) 431, "audio/mpeg", "mpga mpega mp2 mp3 m4a");
		addMIMEType((short) 432, "audio/mpegurl", "m3u");
		addMIMEType((short) 433, "audio/parityfec");
		addMIMEType((short) 434, "audio/prs.sid", "sid");
		addMIMEType((short) 435, "audio/telephone-event");
		addMIMEType((short) 436, "audio/tone");
		addMIMEType((short) 437, "audio/vnd.cisco.nse");
		addMIMEType((short) 438, "audio/vnd.cns.anp1");
		addMIMEType((short) 439, "audio/vnd.cns.inf1");
		addMIMEType((short) 440, "audio/vnd.digital-winds");
		addMIMEType((short) 441, "audio/vnd.everad.plj");
		addMIMEType((short) 442, "audio/vnd.lucent.voice");
		addMIMEType((short) 443, "audio/vnd.nortel.vbk");
		addMIMEType((short) 444, "audio/vnd.nuera.ecelp4800");
		addMIMEType((short) 445, "audio/vnd.nuera.ecelp7470");
		addMIMEType((short) 446, "audio/vnd.nuera.ecelp9600");
		addMIMEType((short) 447, "audio/vnd.octel.sbc");
		addMIMEType((short) 448, "audio/vnd.qcelp");
		addMIMEType((short) 449, "audio/vnd.rhetorex.32kadpcm");
		addMIMEType((short) 450, "audio/vnd.vmx.cvsd");
		addMIMEType((short) 451, "audio/x-aiff", "aif aiff aifc");
		addMIMEType((short) 452, "audio/x-gsm", "gsm");
		addMIMEType((short) 453, "audio/x-mpegurl", "m3u");
		addMIMEType((short) 454, "audio/x-ms-wma", "wma");
		addMIMEType((short) 455, "audio/x-ms-wax", "wax");
		addMIMEType((short) 456, "audio/x-pn-realaudio-plugin");
		addMIMEType((short) 457, "audio/x-pn-realaudio", "ra rm ram");
		addMIMEType((short) 458, "audio/x-realaudio", "ra");
		addMIMEType((short) 459, "audio/x-scpls", "pls");
		addMIMEType((short) 460, "audio/x-sd2", "sd2");
		addMIMEType((short) 461, "audio/x-wav", "wav");
		addMIMEType((short) 462, "chemical/x-alchemy", "alc");
		addMIMEType((short) 463, "chemical/x-cache", "cac cache");
		addMIMEType((short) 464, "chemical/x-cache-csf", "csf");
		addMIMEType((short) 465, "chemical/x-cactvs-binary", "cbin cascii ctab");
		addMIMEType((short) 466, "chemical/x-cdx", "cdx");
		addMIMEType((short) 467, "chemical/x-cerius", "cer");
		addMIMEType((short) 468, "chemical/x-chem3d", "c3d");
		addMIMEType((short) 469, "chemical/x-chemdraw", "chm");
		addMIMEType((short) 470, "chemical/x-cif", "cif");
		addMIMEType((short) 471, "chemical/x-cmdf", "cmdf");
		addMIMEType((short) 472, "chemical/x-cml", "cml");
		addMIMEType((short) 473, "chemical/x-compass", "cpa");
		addMIMEType((short) 474, "chemical/x-crossfire", "bsd");
		addMIMEType((short) 475, "chemical/x-csml", "csml csm");
		addMIMEType((short) 476, "chemical/x-ctx", "ctx");
		addMIMEType((short) 477, "chemical/x-cxf", "cxf cef");
		addMIMEType((short) 478, "chemical/x-embl-dl-nucleotide", "emb embl");
		addMIMEType((short) 479, "chemical/x-galactic-spc", "spc");
		addMIMEType((short) 480, "chemical/x-gamess-input", "inp gam gamin");
		addMIMEType((short) 481, "chemical/x-gaussian-checkpoint", "fch fchk");
		addMIMEType((short) 482, "chemical/x-gaussian-cube", "cub");
		addMIMEType((short) 483, "chemical/x-gaussian-input", "gau gjc gjf");
		addMIMEType((short) 484, "chemical/x-gaussian-log", "gal");
		addMIMEType((short) 485, "chemical/x-gcg8-sequence", "gcg");
		addMIMEType((short) 486, "chemical/x-genbank", "gen");
		addMIMEType((short) 487, "chemical/x-hin", "hin");
		addMIMEType((short) 488, "chemical/x-isostar", "istr ist");
		addMIMEType((short) 489, "chemical/x-jcamp-dx", "jdx dx");
		addMIMEType((short) 490, "chemical/x-kinemage", "kin");
		addMIMEType((short) 491, "chemical/x-macmolecule", "mcm");
		addMIMEType((short) 492, "chemical/x-macromodel-input", "mmd mmod");
		addMIMEType((short) 493, "chemical/x-mdl-molfile", "mol");
		addMIMEType((short) 494, "chemical/x-mdl-rdfile", "rd");
		addMIMEType((short) 495, "chemical/x-mdl-rxnfile", "rxn");
		addMIMEType((short) 496, "chemical/x-mdl-sdfile", "sd sdf");
		addMIMEType((short) 497, "chemical/x-mdl-tgf", "tgf");
		addMIMEType((short) 498, "chemical/x-mmcif", "mcif");
		addMIMEType((short) 499, "chemical/x-mol2", "mol2");
		addMIMEType((short) 500, "chemical/x-molconn-Z", "b");
		addMIMEType((short) 501, "chemical/x-mopac-graph", "gpt");
		addMIMEType((short) 502, "chemical/x-mopac-input", "mop mopcrt mpc dat zmt");
		addMIMEType((short) 503, "chemical/x-mopac-out", "moo");
		addMIMEType((short) 504, "chemical/x-mopac-vib", "mvb");
		addMIMEType((short) 505, "chemical/x-ncbi-asn1", "asn");
		addMIMEType((short) 506, "chemical/x-ncbi-asn1-ascii", "prt ent");
		addMIMEType((short) 507, "chemical/x-ncbi-asn1-binary", "val aso");
		addMIMEType((short) 508, "chemical/x-ncbi-asn1-spec", "asn");
		addMIMEType((short) 509, "chemical/x-pdb", "pdb ent");
		addMIMEType((short) 510, "chemical/x-rosdal", "ros");
		addMIMEType((short) 511, "chemical/x-swissprot", "sw");
		addMIMEType((short) 512, "chemical/x-vamas-iso14976", "vms");
		addMIMEType((short) 513, "chemical/x-vmd", "vmd");
		addMIMEType((short) 514, "chemical/x-xtel", "xtel");
		addMIMEType((short) 515, "chemical/x-xyz", "xyz");
		addMIMEType((short) 516, "image/cgm");
		addMIMEType((short) 517, "image/g3fax");
		addMIMEType((short) 518, "image/gif", "gif");
		addMIMEType((short) 519, "image/ief", "ief");
		addMIMEType((short) 520, "image/jpeg", "jpeg jpg jpe");
		addMIMEType((short) 521, "image/naplps");
		addMIMEType((short) 522, "image/pcx", "pcx");
		addMIMEType((short) 523, "image/png", "png");
		addMIMEType((short) 524, "image/prs.btif");
		addMIMEType((short) 525, "image/prs.pti");
		addMIMEType((short) 526, "image/svg+xml", "svg svgz");
		addMIMEType((short) 527, "image/tiff", "tiff tif");
		addMIMEType((short) 528, "image/vnd.cns.inf2");
		addMIMEType((short) 529, "image/vnd.djvu", "djvu djv");
		addMIMEType((short) 530, "image/vnd.dwg");
		addMIMEType((short) 531, "image/vnd.dxf");
		addMIMEType((short) 532, "image/vnd.fastbidsheet");
		addMIMEType((short) 533, "image/vnd.fpx");
		addMIMEType((short) 534, "image/vnd.fst");
		addMIMEType((short) 535, "image/vnd.fujixerox.edmics-mmr");
		addMIMEType((short) 536, "image/vnd.fujixerox.edmics-rlc");
		addMIMEType((short) 537, "image/vnd.mix");
		addMIMEType((short) 538, "image/vnd.net-fpx");
		addMIMEType((short) 539, "image/vnd.svf");
		addMIMEType((short) 540, "image/vnd.wap.wbmp", "wbmp");
		addMIMEType((short) 541, "image/vnd.xiff");
		addMIMEType((short) 542, "image/x-cmu-raster", "ras");
		addMIMEType((short) 543, "image/x-coreldraw", "cdr");
		addMIMEType((short) 544, "image/x-coreldrawpattern", "pat");
		addMIMEType((short) 545, "image/x-coreldrawtemplate", "cdt");
		addMIMEType((short) 546, "image/x-corelphotopaint", "cpt");
		addMIMEType((short) 547, "image/x-icon", "ico");
		addMIMEType((short) 548, "image/x-jg", "art");
		addMIMEType((short) 549, "image/x-jng", "jng");
		addMIMEType((short) 550, "image/x-ms-bmp", "bmp");
		addMIMEType((short) 551, "image/x-photoshop", "psd");
		addMIMEType((short) 552, "image/x-portable-anymap", "pnm");
		addMIMEType((short) 553, "image/x-portable-bitmap", "pbm");
		addMIMEType((short) 554, "image/x-portable-graymap", "pgm");
		addMIMEType((short) 555, "image/x-portable-pixmap", "ppm");
		addMIMEType((short) 556, "image/x-rgb", "rgb");
		addMIMEType((short) 557, "image/x-xbitmap", "xbm");
		addMIMEType((short) 558, "image/x-xpixmap", "xpm");
		addMIMEType((short) 559, "image/x-xwindowdump", "xwd");
		addMIMEType((short) 560, "inode/chardevice");
		addMIMEType((short) 561, "inode/blockdevice");
		addMIMEType((short) 562, "inode/directory-locked");
		addMIMEType((short) 563, "inode/directory");
		addMIMEType((short) 564, "inode/fifo");
		addMIMEType((short) 565, "inode/socket");
		addMIMEType((short) 566, "message/delivery-status");
		addMIMEType((short) 567, "message/disposition-notification");
		addMIMEType((short) 568, "message/external-body");
		addMIMEType((short) 569, "message/http");
		addMIMEType((short) 570, "message/s-http");
		addMIMEType((short) 571, "message/news");
		addMIMEType((short) 572, "message/partial");
		addMIMEType((short) 573, "message/rfc822");
		addMIMEType((short) 574, "model/iges", "igs iges");
		addMIMEType((short) 575, "model/mesh", "msh mesh silo");
		addMIMEType((short) 576, "model/vnd.dwf");
		addMIMEType((short) 577, "model/vnd.flatland.3dml");
		addMIMEType((short) 578, "model/vnd.gdl");
		addMIMEType((short) 579, "model/vnd.gs-gdl");
		addMIMEType((short) 580, "model/vnd.gtw");
		addMIMEType((short) 581, "model/vnd.mts");
		addMIMEType((short) 582, "model/vnd.vtu");
		addMIMEType((short) 583, "model/vrml", "wrl vrml");
		addMIMEType((short) 584, "multipart/alternative");
		addMIMEType((short) 585, "multipart/appledouble");
		addMIMEType((short) 586, "multipart/byteranges");
		addMIMEType((short) 587, "multipart/digest");
		addMIMEType((short) 588, "multipart/encrypted");
		addMIMEType((short) 589, "multipart/form-data");
		addMIMEType((short) 590, "multipart/header-set");
		addMIMEType((short) 591, "multipart/mixed");
		addMIMEType((short) 592, "multipart/parallel");
		addMIMEType((short) 593, "multipart/related");
		addMIMEType((short) 594, "multipart/report");
		addMIMEType((short) 595, "multipart/signed");
		addMIMEType((short) 596, "multipart/voice-message");
		addMIMEType((short) 597, "text/calendar", "ics icz");
		addMIMEType((short) 598, "text/comma-separated-values", "csv");
		addMIMEType((short) 599, "text/css", "css");
		addMIMEType((short) 600, "text/directory");
		addMIMEType((short) 601, "text/english");
		addMIMEType((short) 602, "text/enriched");
		addMIMEType((short) 603, "text/h323", "323");
		addMIMEType((short) 604, "text/html", "html htm shtml");
		addMIMEType((short) 605, "text/iuls", "uls");
		addMIMEType((short) 606, "text/mathml", "mml");
		addMIMEType((short) 607, "text/parityfec");
		addMIMEType((short) 608, "text/plain", "asc txt text diff pot");
		addMIMEType((short) 609, "text/prs.lines.tag");
		addMIMEType((short) 610, "text/x-psp", "psp");
		addMIMEType((short) 611, "text/rfc822-headers");
		addMIMEType((short) 612, "text/richtext", "rtx");
		addMIMEType((short) 613, "text/rtf", "rtf");
		addMIMEType((short) 614, "text/scriptlet", "sct wsc");
		addMIMEType((short) 615, "text/t140");
		addMIMEType((short) 616, "text/texmacs", "tm ts");
		addMIMEType((short) 617, "text/tab-separated-values", "tsv");
		addMIMEType((short) 618, "text/uri-list");
		addMIMEType((short) 619, "text/vnd.abc");
		addMIMEType((short) 620, "text/vnd.curl");
		addMIMEType((short) 621, "text/vnd.DMClientScript");
		addMIMEType((short) 622, "text/vnd.flatland.3dml");
		addMIMEType((short) 623, "text/vnd.fly");
		addMIMEType((short) 624, "text/vnd.fmi.flexstor");
		addMIMEType((short) 625, "text/vnd.in3d.3dml");
		addMIMEType((short) 626, "text/vnd.in3d.spot");
		addMIMEType((short) 627, "text/vnd.IPTC.NewsML");
		addMIMEType((short) 628, "text/vnd.IPTC.NITF");
		addMIMEType((short) 629, "text/vnd.latex-z");
		addMIMEType((short) 630, "text/vnd.motorola.reflex");
		addMIMEType((short) 631, "text/vnd.ms-mediapackage");
		addMIMEType((short) 632, "text/vnd.sun.j2me.app-descriptor", "jad");
		addMIMEType((short) 633, "text/vnd.wap.si");
		addMIMEType((short) 634, "text/vnd.wap.sl");
		addMIMEType((short) 635, "text/vnd.wap.wml", "wml");
		addMIMEType((short) 636, "text/vnd.wap.wmlscript", "wmls");
		addMIMEType((short) 637, "text/x-bibtex", "bib");
		addMIMEType((short) 638, "text/x-c++hdr", "h++ hpp hxx hh");
		addMIMEType((short) 639, "text/x-c++src", "c++ cpp cxx cc");
		addMIMEType((short) 640, "text/x-chdr", "h");
		addMIMEType((short) 641, "text/x-crontab");
		addMIMEType((short) 642, "text/x-csh", "csh");
		addMIMEType((short) 643, "text/x-csrc", "c");
		addMIMEType((short) 644, "text/x-haskell", "hs");
		addMIMEType((short) 645, "text/x-java", "java");
		addMIMEType((short) 646, "text/x-literate-haskell", "lhs");
		addMIMEType((short) 647, "text/x-makefile");
		addMIMEType((short) 648, "text/x-moc", "moc");
		addMIMEType((short) 649, "text/x-pascal", "p pas");
		addMIMEType((short) 650, "text/x-pcs-gcd", "gcd");
		addMIMEType((short) 651, "text/x-perl", "pl pm");
		addMIMEType((short) 652, "text/x-python", "py");
		addMIMEType((short) 653, "text/x-server-parsed-html");
		addMIMEType((short) 654, "text/x-setext", "etx");
		addMIMEType((short) 655, "text/x-sh", "sh");
		addMIMEType((short) 656, "text/x-tcl", "tcl tk");
		addMIMEType((short) 657, "text/x-tex", "tex ltx sty cls");
		addMIMEType((short) 658, "text/x-vcalendar", "vcs");
		addMIMEType((short) 659, "text/x-vcard", "vcf");
		addMIMEType((short) 660, "video/dl", "dl");
		addMIMEType((short) 661, "video/dv", "dif dv");
		addMIMEType((short) 662, "video/fli", "fli");
		addMIMEType((short) 663, "video/gl", "gl");
		addMIMEType((short) 664, "video/mpeg", "mpeg mpg mpe");
		addMIMEType((short) 665, "video/mp4", "mp4");
		addMIMEType((short) 666, "video/quicktime", "qt mov");
		addMIMEType((short) 667, "video/mp4v-es");
		addMIMEType((short) 668, "video/parityfec");
		addMIMEType((short) 669, "video/pointer");
		addMIMEType((short) 670, "video/vnd.fvt");
		addMIMEType((short) 671, "video/vnd.motorola.video");
		addMIMEType((short) 672, "video/vnd.motorola.videop");
		addMIMEType((short) 673, "video/vnd.mpegurl", "mxu");
		addMIMEType((short) 674, "video/vnd.mts");
		addMIMEType((short) 675, "video/vnd.nokia.interleaved-multimedia");
		addMIMEType((short) 676, "video/vnd.vivo");
		addMIMEType((short) 677, "video/x-la-asf", "lsf lsx");
		addMIMEType((short) 678, "video/x-mng", "mng");
		addMIMEType((short) 679, "video/x-ms-asf", "asf asx");
		addMIMEType((short) 680, "video/x-ms-wm", "wm");
		addMIMEType((short) 681, "video/x-ms-wmv", "wmv");
		addMIMEType((short) 682, "video/x-ms-wmx", "wmx");
		addMIMEType((short) 683, "video/x-ms-wvx", "wvx");
		addMIMEType((short) 684, "video/x-msvideo", "avi");
		addMIMEType((short) 685, "video/x-sgi-movie", "movie");
		addMIMEType((short) 686, "x-conference/x-cooltalk", "ice");
		addMIMEType((short) 687, "x-world/x-vrml", "vrm vrml wrl");
	}

	/** Guess a MIME type from a filename */
	public static String guessMIMEType(String arg) {
		int x = arg.lastIndexOf('.');
		if(x == -1 || x == arg.length()-1)
			return DEFAULT_MIME_TYPE;
		String ext = arg.substring(x+1).toLowerCase();
		Short mimeIndexOb = mimeTypesByExtension.get(ext);
		if(mimeIndexOb != null) {
			return mimeTypesByNumber.get(mimeIndexOb.intValue());
		}
		return DEFAULT_MIME_TYPE;
	}

	public static String getExtension(String type) {
		short typeNumber = byName(type);
		if(typeNumber < 0) return null;
		return primaryExtensionByMimeNumber.get(typeNumber);
	}

	public static String[] getAllMIMETypes() {
		return mimeTypesByNumber.toArray(new String[mimeTypesByNumber.size()]);
	}

}
