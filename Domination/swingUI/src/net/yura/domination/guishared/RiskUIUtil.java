package net.yura.domination.guishared;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import net.yura.domination.audio.GameSound;
import net.yura.domination.audio.SimpleAudio;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskIO;
import net.yura.domination.engine.RiskSettings;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.swing.BrowserLauncher;
import net.yura.swing.GraphicsUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mapstore.MapServerClient;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.swing.TextUtil;

/**
 * @author Yura Mamyrin
 */
public class RiskUIUtil {

    private static final Logger logger = Logger.getLogger(RiskUIUtil.class.getName());
    
    // TODO missing:
    //PicturePanel.getImage(
    // setupMapsDir(null) should be called b4 the Risk() object is created

    public static class FileInputStream extends java.io.FileInputStream {

        private final File file;

        private FileInputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }


    static {
        // this could have alredy been set by lobby, so only set it if its null
        if (RiskUtil.streamOpener==null) {
            RiskUtil.streamOpener = new RiskIO() {
                public InputStream openStream(String name) throws IOException {
                    try {
                        return getRiskFileURL(name).openStream();
                    }
                    catch (FileNotFoundException fnfe) {
                        try {
                            logger.info("can not openStream for: " + name);
                            File file = new File(name).getCanonicalFile();
                            logger.info("file exists: " + file.exists() + " " + file);
                            File parent = file.getParentFile();
                            logger.info("parnet exists: " + parent.exists() + " " + parent);
                            if (parent.exists()) {
                                logger.info("siblings: " + Arrays.asList(parent.list()));
                            }
                            else {
                                File grandParent = parent.getParentFile();
                                logger.info("grand parent exists: " + grandParent.exists() + " " + grandParent);
                                if (grandParent.exists()) {
                                    logger.info("parnets siblings: " + Arrays.asList(grandParent.list()));
                                }
                            }
                        }
                        catch (Throwable th) {} // dont let errors in logging mess with anything

                        throw fnfe;
                    }
                }
                
                /**
                 * in Desktop app mode we need to allow full paths for MapEditor, and relative mapId filenames
                 */
                public InputStream openMapStream(String name) throws IOException {
                    try {
                        // TODO
                        // TODO, do NOT even try this if we are inside a applet sandbox
                        // TODO or it will spam the logs with lots of: this should never happen! bad file:...
                        // TODO
                        File mapsDir = getSaveMapDir();
                        return new FileInputStream( new File(mapsDir,name) );
                    }
                    catch (Throwable th) {
                        try {
                            // on windows, absolute paths starting with "c:" confuse java, MalformedURLException: unknown protocol: c
                            // so we need to put a "/" at the start so it knows the c: is not the protocol
                            return new URL(mapsdir, name.contains(":") ? "/" + name : name).openStream();
                        }
                        catch (Throwable ex) { // dont really care about this one, it just means the file is not found here
                            IOException exception = new IOException("openMap error \"" + name + "\" " + ex);
                            exception.initCause(th); // in java 1.4
                            throw exception;
                        }
                    }
                }
                public ResourceBundle getResourceBundle(Class c, String n, Locale l) {
                    return ResourceBundle.getBundle(c.getPackage().getName()+"."+n, l );
                }
                public void openURL(URL url) throws Exception {
                    riskOpenURL(url);
                }
                public void openDocs(String doc) throws Exception {
                    riskOpenURL(getRiskFileURL(doc));
                }
                public void saveGameFile(String name,RiskGame obj) throws Exception {
                    saveFile(name,obj);
                }
                public InputStream loadGameFile(String file) throws Exception {
                    return getLoadFileInputStream(file);
                }
                public OutputStream saveMapFile(String fileName) throws Exception {
                    return RiskUtil.getOutputStream( getSaveMapDir() , fileName);
                }
                public void getMap(String filename, Observer observer) {
                    net.yura.domination.mapstore.GetMap.getMap(filename, observer);
                }
                public void renameMapFile(String oldName, String newName) {
                    File oldFile = new File(getSaveMapDir(), oldName);
                    File newFile = new File(getSaveMapDir(), newName);
                    RiskUtil.rename(oldFile, newFile);
                }

                public boolean deleteMapFile(String mapUID) {
                    File mapFile = new File(getSaveMapDir(), mapUID);
                    if (mapFile.exists()) {
                        return mapFile.delete();
                    }
                    return false;
                }
            };
        }
    }

    public static URL mapsdir; // bundled maps are in this dir

    public static Applet applet;
    private static String webstart;

    private static boolean nosandbox;

    public static boolean checkForNoSandbox() {
            return nosandbox;
    }

    private static Map UIImagesReferences = new HashMap();

    public static Image getUIImage(Class cls, String name) {
/*
        // this can return a MultiResolutionImage
        // but it uses OS secific filenames, so on macOS it loads @2x images, but not on windows :-(
        Image img = Toolkit.getDefaultToolkit().getImage(cls.getResource(name));
        GraphicsUtil.waitForImage(img);

        // java 9+
        try {
            Class multiResolutionImageClass = Class.forName("java.awt.image.MultiResolutionImage");
            if (multiResolutionImageClass.isInstance(img)) {
                System.out.println("YAY! we got a MultiResolutionImage " + name + " " + img);
                return img;
            }
        }
        catch (Throwable ex) { }
*/
        // java 8 (this is the ONLY way to load MultiResolutionImage on java8) and only works on mac
        try {
            if (isMac() && GraphicsUtil.density > 1) {
                Class multiResolutionImageClass = Class.forName("sun.awt.image.MultiResolutionImage");

                Image img = Toolkit.getDefaultToolkit().getImage(cls.getResource(name));
                GraphicsUtil.waitForImage(img);

                // we ONLY want to use this if we HAVE to.
                // some images returned by this method do not draw first time,
                // even though getWidth/getHeight seems to be returning correctly
                // e.g. in SwingGUI 'all cards' dialog, "infantry.gif" does not draw first time
                if (multiResolutionImageClass.isInstance(img)) {
                    return img;
                }
            }
        }
        catch (Throwable ex) { }


        // this code will work on all OSs, tested on macOS and windows
        Image img = getUIImageCached(cls, name);

        if (GraphicsUtil.density > 1) {
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                String scale2x = name.substring(0, dot) + "@2x" + name.substring(dot);
                try {
                    Image img2x = getUIImageCached(cls, scale2x);
                    Image[] imgList = new Image[] {img, img2x};
                    return GraphicsUtil.newBaseMultiResolutionImage(imgList);
                }
                catch (Throwable ex) {
                    // failed to create MultiResolutionImage
                }
            }
            if (name.startsWith("/")) {
                String xhdpi = "/drawable-xhdpi" + name;
                try {
                    Image img2x = getUIImageCached(cls, xhdpi);
                    Image[] imgList = new Image[] {img, img2x};
                    return GraphicsUtil.newBaseMultiResolutionImage(imgList);
                }
                catch (Throwable ex) {
                    // failed to create MultiResolutionImage
                }
            }
        }

        return img;
    }

    private static BufferedImage getUIImageCached(Class c,String name) {
		try {
			String id = c+" - "+name;
			WeakReference wr = (WeakReference)UIImagesReferences.get(id);

			if (wr!=null) {
				BufferedImage img = (BufferedImage)wr.get();
				if (img!=null) {
					return img;
				}
			}
			BufferedImage img;
                        
                        URL imgUrl = c.getResource(name);
                        if (imgUrl == null) {
                            throw new IllegalArgumentException("no Resource found for class: " + c + " name: " + name);
                        }
                        try {
                            img = ImageIO.read(imgUrl);
                        }
                        catch (IIOException ex) {
                            // can get a javax.imageio.IIOException: Can't create cache file!
                            if (ImageIO.getUseCache()) {
                                logger.log(Level.INFO, "could not load UI image " + c + " " + name, ex);
                                ImageIO.setUseCache(false);
                                img = ImageIO.read(imgUrl);
                            }
                            else {
                                throw ex;
                            }
                        }

			UIImagesReferences.put(id,new WeakReference(img));

			return img;
		}
		catch (Exception e) {
			throw new RuntimeException("error loading UI Image "+c+" "+name,e);
		}
	}



	/**
	 * Opens the online help
	 * @return boolean Return true if you open the online help, returns false otherwise
	 */
	private static void riskOpenURL(URL docs) throws Exception {
		if (applet != null) {

			applet.getAppletContext().showDocument(docs,"_blank");
		}
		else if (webstart != null) {

			javax.jnlp.BasicService bs = (javax.jnlp.BasicService)javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService");

			boolean good = bs.showDocument(docs);

			if (!good) {

				throw new Exception("unable to open URL: "+docs);
			}

		}
		else {
			BrowserLauncher.openURL(docs.toString());
		}

/*
		if (applet == null ) {

			File file = new File(docs);
			openURL(new URL("file://" + file.getAbsolutePath()) );

		}
		else {

			URL url = applet.getCodeBase(); // get url of the applet

			openURL(new URL(url+docs));

		}


		try {

			String cmd=null;

			String os = System.getProperty("os.name");

			if ( os != null && os.startsWith("Windows")) {
				cmd = "rundll32 url.dll,FileProtocolHandler file://"+ file.getAbsolutePath();
			}
			else {
				cmd = "mozilla file://"+ file.getAbsolutePath();
			}

			Runtime.getRuntime().exec(cmd);

			return true;
		}
		catch(IOException x) {
			return false;
		}
*/
	}

	private static URL getRiskFileURL(String a) {
		try {
			if (applet!=null) {
				return new URL( applet.getCodeBase(), a );
			}
			else if (webstart!=null) {
				javax.jnlp.BasicService bs = (javax.jnlp.BasicService)javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService");
				return new URL( bs.getCodeBase() , a);
			}
			else {
                            File file = new File(a);
                            if (file.exists()) {
				return file.toURI().toURL();
                            }
                            // in case the 'user.dir' has been changed
                            return new File(System.getProperty("user.dir"), a).toURI().toURL();
			}
		}
		catch (Exception e) {
			throw new RuntimeException("unable to get url for: " + a, e);
		}
	}

	public static void setupMapsDir(Applet a) {
		applet = a;

		// only ever call this method once
		if (mapsdir==null) {

		    // if applet is null check for webstart!
		    if (applet==null) {

			webstart = System.getProperty("javawebstart.version");

			if (webstart==null) {
				nosandbox=true;

				// we only want to setup the look and feel outside a sandbox
				// though this WILL work inside too
				setupLookAndFeel();
			}
		    }

		    try {

			if (checkForNoSandbox()) {

				final AtomicReference<File> mapsdir1 = new AtomicReference(new File("maps"));

				// riskconfig.getProperty("default.map")
                                final String dmname = RiskGame.getDefaultMap();

                                if ( !(new File(mapsdir1.get(), dmname).exists()) ) {
                                    try {
                                        // if we can not find maps, attempt to get path from jar file
                                        URL url = RiskUIUtil.class.getProtectionDomain().getCodeSource().getLocation();
                                        File jarFile = new File(url.toURI());
                                        mapsdir1.set(new File(jarFile.getParentFile(), "maps"));
                                    }
                                    catch (Throwable th) {
                                        logger.info("failed to get maps dir from jar " + th);
                                    }

                                    // if we still can not find the map, try and ask the user
                                    while ( !(new File(mapsdir1.get(), dmname).exists()) ) {

                                        // on Apple OS X java 1.7 this deadlocks if not on the UI Thread
                                        SwingUtilities.invokeAndWait(new Runnable() { public void run() {

                                            JOptionPane.showMessageDialog(null, "Can not find map: " + dmname);

                                            JFileChooser fc = new JFileChooser( new File(".") );
                                            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                            fc.setDialogTitle("Select maps directory");

                                            int returnVal = fc.showOpenDialog(null);
                                            if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                                                    mapsdir1.set(fc.getSelectedFile());
                                            }
                                            else {
                                                    System.exit(0);
                                            }

                                        }});
                                    }
                                    
                                    System.setProperty("user.dir", mapsdir1.get().getParent());
                                }

				mapsdir = mapsdir1.get().toURI().toURL();
			}
			else {
				mapsdir = getRiskFileURL( "maps/");
			}

		    }
		    catch (Exception e) {
			throw new RuntimeException(e);
		    }
		}
	}

	public static Frame findParentFrame(Container c) {
		return (Frame)javax.swing.SwingUtilities.getAncestorOfClass(Frame.class, c);
/*
		// this does not work as using the method setContentPane makes this method return null

		while(c != null) {

			if (c instanceof Frame) return (Frame)c;

			c = c.getParent();
		}
		return (Frame)null;
*/
	}

        public static void center(Window window) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = window.getSize();
            frameSize.height = ((frameSize.height > screenSize.height) ? screenSize.height : frameSize.height);
            frameSize.width = ((frameSize.width > screenSize.width) ? screenSize.width : frameSize.width);
            window.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        }

        /**
         * @see GraphicsUtil#scale(int)
         */
        public static int unscale(int size) {
            return (int) Math.ceil(size * GraphicsUtil.scale / GraphicsUtil.density);
        }

        private static List getFileList(final String a) {

            List namesvector = new ArrayList();

            if (checkForNoSandbox()) {

                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith("."+a);
                    }
                };


                // get list of maps
                File file = getSaveMapDir();
                File [] mapsList = file.listFiles( filter );
                if (mapsList!=null) { // there is no reason at all this should ever be null, but sometimes it is?!
                    for (int c=0;c<mapsList.length;c++) {
                        namesvector.add( mapsList[c].getName() );
                    }
                }


                File file2 = getFile(mapsdir);
                if (!file.equals(file2)) {
                    mapsList = file2.listFiles( filter );
                    for (int c=0;c<mapsList.length;c++) {
                        String name = mapsList[c].getName();
                        if (!namesvector.contains(name)) {
                            namesvector.add( name );
                        }
                    }
                }
            }
            else {

                String names=null;
                if (applet!=null) {
                        names = applet.getParameter(a);
                }
                else if (webstart!=null) {
                        if ("map".equals(a)) {
                                names = maps;
                        }
                        else if ("cards".equals(a)) {
                                names = cards;
                        }
                }
                StringTokenizer tok = new StringTokenizer( names, ",");
                while (tok.hasMoreTokens()) {
                        namesvector.add( tok.nextToken() );
                }
            }

            return namesvector;
        }


        public static String getNewMap(Frame f) {
            try {
                if (checkForNoSandbox()) {
                    List mapsList = getFileList( RiskFileFilter.RISK_MAP_FILES );
                    // try and start new map chooser,
                    // on fail revert to using the old one
                    return SwingMEWrapper.showMapChooser(f, mapsList);
                }
            }
            catch (Throwable th) {
                logger.log(Level.WARNING, "unable to show MapChooser", th);
            }

            // can not have the map store, fall back to normal map chooser
            return getNewMapsFile(f, RiskFileFilter.RISK_MAP_FILES);
        }

        public static String getNewMapsFile(Frame f, String... extension) {
            if (checkForNoSandbox()) {
                return getNewMapsFileNoSandbox(f, extension);
            }
            else {
                return getNewFileInSandbox(f, extension[0]);
            }
        }
        
        public static boolean isMac() {
            String osName = System.getProperty("os.name").toLowerCase(Locale.US);
            return osName.startsWith("mac");
        }
        public static boolean isWindows() {
            String osName = System.getProperty("os.name").toLowerCase(Locale.US);
            return osName.startsWith("windows");
        }

        /**
         * Used if the MapStore fails, also used in the map editor
         * and also for selecting the cards file
         */
        public static String getNewMapsFileNoSandbox(Frame f, String... extension) {
            File md = getFile(mapsdir);
            File file = getFileOpenDialog(f, md, extension);

            if (file == null) {
                return null;
            }
            if (file.getParentFile().equals(md)) {
                return file.getName();
            }
            return file.getPath();
        }

        public static File getFileOpenDialog(Frame parent, File directory, String... extension) {
            RiskFileFilter filter = new RiskFileFilter(extension);
            java.io.File file;

            // JFileChooser on mac is really bad, but FileDialog uses the native picker
            if (isMac()) {
                file = getAWTFileDialogFile(parent, directory, filter, FileDialog.LOAD);
            }
            else {
                JFileChooser fc = new JFileChooser(directory);
                fc.setFileFilter(filter);

                int returnVal = fc.showOpenDialog(parent);
                if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {

                    file = fc.getSelectedFile();
                    // sometimes this is null, bug in java? fall back to FileDialog
                    if (file == null) {
                        file = getAWTFileDialogFile(parent, directory, filter, FileDialog.LOAD);
                    }
                }
                else {
                    file = null;
                }
            }
            return file;
        }

        private static File getAWTFileDialogFile(Frame f, File md, FilenameFilter filter, int mode) {
            FileDialog fileDialog = new FileDialog(f);
            fileDialog.setMode(mode);
            if (md != null) {
                fileDialog.setDirectory(md.getAbsolutePath());
            }
            fileDialog.setFilenameFilter(filter); // does nothing on windows
            fileDialog.setVisible(true);
            String filename = fileDialog.getFile();
            if (filename == null) {
                return null;
            }
            return new File(fileDialog.getDirectory(), filename);
        }

	public static String getNewFileInSandbox(Frame f, String type) {

            List namesvector = getFileList(type);

            JComboBox combobox = new JComboBox( JavaCompatUtil.asVector(namesvector) );

            // Messages
            Object[] message = new Object[] {
                    TranslationBundle.getBundle().getString("core.error.applet"),
                    combobox
            };

            // Options
            String[] options = { "OK","Cancel" };

            int result = JOptionPane.showOptionDialog(
                    f,				// the parent that the dialog blocks
                    message,			// the dialog message array
                    "select " + type,			// the title of the dialog window
                    JOptionPane.OK_CANCEL_OPTION,	// option type
                    JOptionPane.QUESTION_MESSAGE,	// message type
                    null,				// optional icon, use null to use the default icon
                    options,			// options string array, will be made into buttons
                    options[0]			// option that should be made into a default button
            );

            if (result==JOptionPane.OK_OPTION) {
                    return combobox.getSelectedItem()+"";
            }

            return null;
	}


        public static final String SAVES_DIR = "saves/";

	public static String getLoadFileName(Frame frame) {
		if (applet!=null) {
			showAppletWarning(frame);
			return null;
		}

                String extension = RiskFileFilter.RISK_SAVE_FILES;

		if (webstart != null) {
			try {
				javax.jnlp.FileOpenService fos = (javax.jnlp.FileOpenService)javax.jnlp.ServiceManager.lookup("javax.jnlp.FileOpenService");

				javax.jnlp.FileContents fc = fos.openFileDialog(SAVES_DIR, new String[] { extension } );

				if (fc != null) {
					fileio.put(fc.getName(),fc);
					return fc.getName();
				}

				return null;
			}
			catch(Exception e) {
				return null;
			}
		}

                File dir = getSaveGameDir();
                RiskFileFilter filter = new RiskFileFilter(extension);
                
                if (isMac()) {
                    File file = getAWTFileDialogFile(frame, dir, filter, FileDialog.LOAD);
                    if (file == null) {
                        return null;
                    }
                    return file.getAbsolutePath();
                }
                
                JFileChooser fc = new JFileChooser(dir);
                fc.setFileFilter(filter);

                int returnVal = fc.showDialog(frame, TranslationBundle.getBundle().getString("mainmenu.loadgame.loadbutton"));
                if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                        java.io.File file = fc.getSelectedFile();

                        // TODO file null???? windows java1.8
                        return file.getAbsolutePath();
                }

                return null;
	}

	public static InputStream getLoadFileInputStream(String file) throws Exception {
		// it is impossible for a applet to get here
		if (webstart != null) {
			javax.jnlp.FileContents fc = (javax.jnlp.FileContents)fileio.remove(file);
			return fc.getInputStream();
		}

		return new java.io.FileInputStream(file);
	}

	public static String getSaveFileName(Frame frame) {
		if (applet!=null) {
			showAppletWarning(frame);
			return null;
		}

                String extension = RiskFileFilter.RISK_SAVE_FILES;

		if (webstart != null) {
			JOptionPane.showMessageDialog(frame,"Please make sure to select a file name ending with \"."+extension+"\"");
			return SAVES_DIR+"filename."+extension;
		}

                File dir = getSaveGameDir();
                RiskFileFilter filter = new RiskFileFilter(extension);
                java.io.File file;
                
                if (isMac()) {
                    file = getAWTFileDialogFile(frame, dir, filter, FileDialog.SAVE);
                }
                else {
                    JFileChooser fc = new JFileChooser(dir);
                    fc.setFileFilter(filter);

                    int returnVal = fc.showSaveDialog(frame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                            file = fc.getSelectedFile(); // can return null, bug in java??

                            // if JFileChooser failed, fall back to awt FileDialog
                            if (file == null) {
                                file = getAWTFileDialogFile(frame, dir, filter, FileDialog.SAVE);
                            }
                    }
                    else {
                        file = null;
                    }
                }

                if (file == null) {
                    return null;
                }

                String fileName = file.getAbsolutePath();
                if (!(fileName.endsWith( "." + extension))) {
                        fileName = fileName + "." + extension;
                }
                return fileName;
	}

        /**
         * TODO what do we do if the path is already a folder or is in a folder that does not exist??
         */
	public static void saveFile(String name,RiskGame obj) throws Exception {
		// it is impossible for a applet to get here
		if (webstart!=null) {
			ByteArrayOutputStream stor = new ByteArrayOutputStream();
                        obj.saveGame(stor);
			InputStream stream = new ByteArrayInputStream(stor.toByteArray());

			javax.jnlp.FileSaveService fss = (javax.jnlp.FileSaveService)javax.jnlp.ServiceManager.lookup("javax.jnlp.FileSaveService");
			javax.jnlp.FileContents fc = fss.saveFileDialog(name.substring(0,name.indexOf('/')+1), new String[]{ name.substring(name.indexOf('.')+1) }, stream, name.substring(name.indexOf('/')+1,name.indexOf('.')) );
		}
		else {
			FileOutputStream fileout = new FileOutputStream(name);
                        obj.saveGame(fileout);
		}
	}

	public static void showAppletWarning(Frame frame) {
		JOptionPane.showMessageDialog(frame, TranslationBundle.getBundle().getString("core.error.applet"));
	}

	public static String getSystemInfoText() {

		ResourceBundle resb = TranslationBundle.getBundle();

		String netInfo,home,cpu,environment,info;

		if (checkForNoSandbox()) {
			home = System.getProperty("java.home");
			cpu = System.getProperty("sun.cpu.isalist");
                        if (cpu == null) {
                            cpu = "?";
                        }
			environment = System.getProperty("java.runtime.name") + " ("+ System.getProperty("java.runtime.version") +")";
			info = System.getProperty("java.vm.info");

			// we CAN do this outside the sandbox, but for some reason it promps the webstart
			try {
                                InetAddress localAddress = RiskUtil.getLocalHost();
				netInfo = localAddress.getHostAddress() + " (" + localAddress.getHostName() +")" ;
			}
			catch (UnknownHostException e) {
				netInfo = resb.getString("about.nonetwork");
			}
		}
		else {
			home = "?";
			cpu = "?";
			info = "?";

			if (applet!=null) {
				environment = "applet";
			}
			else if (webstart!=null) {
				environment = "web start ("+webstart+")";
			}
			else {
				environment = "?";
			}

			netInfo = "?";
		}

		String displayInfo;
		try {
			Toolkit toolkit = Toolkit.getDefaultToolkit();

                        AffineTransform dat = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
                        double scaleX = dat.getScaleX();
                        double scaleY = dat.getScaleY();

                        Object gnomeDpi = Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/DPI");
                        
			displayInfo = toolkit.getScreenSize().width + "x" + toolkit.getScreenSize().height + " (" + toolkit.getScreenResolution() + "dpi " +
                                (gnomeDpi == null ? "" : gnomeDpi + "gnome/dpi ") +
                                "scaleX=" + scaleX + " scaleY=" + scaleY + ") density=" + GraphicsUtil.density + " scale=" + GraphicsUtil.scale;
		}
		catch(HeadlessException ex) {
			displayInfo = ex.getMessage();
			if (displayInfo != null) displayInfo = JavaCompatUtil.replaceAll(displayInfo, "\n", " ");
		}

		return		" " + RiskUtil.RISK_VERSION + " (save: " + RiskGame.SAVE_VERSION + " network: "+RiskGame.NETWORK_VERSION+") " + (RiskUtil.isOldVersion()?"OLD VERSION":"") + "\n" +
				" " + "system:"+java.util.Locale.getDefault()+" current:" + resb.getLocale() + " \n" +
				" " + netInfo + " \n" +
				" " + getOSString() + " \n" +
				" " + cpu + " \n" +
				" " + UIManager.getLookAndFeel() + " \n" +
				" " + displayInfo + " \n" +
				" " + System.getProperty("java.vendor") + " \n" +
				" " + System.getProperty("java.vendor.url") + " \n" +
				" " + environment +" \n" +
				" " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.version") +", "+ info +") \n" +
				" " + System.getProperty("java.specification.version") +" ("+ System.getProperty("java.version") +") \n" +
				" " + home + " \n" +
				" " + System.getProperty("java.class.version");
	}

        public static String getOSString() {
            String patchAndArch;
            if (checkForNoSandbox()) {
                String patch = System.getProperty("sun.os.patch.level");
                patchAndArch = (patch == null ? "" : patch + " ") + "(" + System.getProperty("sun.arch.data.model") + "bit)";
            }
            else {
                patchAndArch="?";
            }
            return System.getProperty("os.name") + " " + System.getProperty("os.version") +" "+ patchAndArch + " on " + System.getProperty("os.arch");
        }


	public static void openAbout(Frame frame,String product,String version) {
		AboutDialog aboutDialog = new AboutDialog( frame, true, product, version);
		Dimension frameSize = frame.getSize();
		Dimension aboutSize = aboutDialog.getSize();
		int x = frame.getLocation().x + (frameSize.width - aboutSize.width) / 2;
		int y = frame.getLocation().y + (frameSize.height - aboutSize.height) / 2;
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		aboutDialog.setLocation(x, y);
		aboutDialog.setVisible(true);
	}


	private static String lobbyAppletURL;
	private static String lobbyURL;

	public static boolean getAddLobby() {

		boolean canlobby = false;

                if (checkForNoSandbox()) {

                        try {

                                URL url = new URL(RiskUtil.RISK_LOBBY_URL);

                                BufferedReader bufferin=new BufferedReader( new InputStreamReader(url.openStream()) );

                                StringBuffer buffer = new StringBuffer();

                                String input = bufferin.readLine();

                                while(input != null) {

                                        buffer.append(input+"\n");

                                        input = bufferin.readLine(); // get next line
                                }

                                String[] lobbyinfo = buffer.toString().split("\\n");

                                if (lobbyinfo.length>=1 && lobbyinfo[0].equals("LOBBYOK")) {

                                        if (lobbyinfo.length>=2) {

                                                lobbyAppletURL = lobbyinfo[1];
                                                canlobby = true;
                                        }
                                        if (lobbyinfo.length>=3) {

                                                lobbyURL = lobbyinfo[2];
                                                canlobby = true;

                                        }
                                }

                        }
                        catch(Throwable ex) { }
                }
		return canlobby;
	}


        public static void checkForUpdates(Risk risk) {
                if (checkForNoSandbox()) {
                        //try { Thread.sleep(5000); }
                        //catch(InterruptedException e) {}

                        String v = RiskUtil.getNewVersionCheck();

                        if (v != null) {
                            ResourceBundle resb = TranslationBundle.getBundle();

                            v = JavaCompatUtil.replaceAll(resb.getString("mainmenu.new-version.text"), "{0}", RiskUtil.GAME_NAME) + " "+v;

                            String link = getURL(v);
                            if (link!=null) {
                                int result = JOptionPane.showConfirmDialog(null, v, resb.getString("mainmenu.new-version.title"), JOptionPane.OK_CANCEL_OPTION);
                                if (result == JOptionPane.OK_OPTION) {
                                    try {
                                        RiskUtil.streamOpener.openURL( new URL(link) );
                                    }
                                    catch (Throwable th) {
                                        logger.log(Level.WARNING, "unable to open url: " + link, th);
                                    }
                                }
                            }
                            else {
                                // do not use this, this is used for errors
                                risk.showMessageDialog(v);
                            }
                        }

                        try {
                            //check for map updates
                            MapUpdateService.getInstance().init( getFileList("map"), MapServerClient.MAP_PAGE );
                        }
                        catch (Throwable th) {
                            logger.log(Level.WARNING, "unable to check for map updates", th);
                        }
                }
        }

        public static String getURL(String v) {

            int site = v.indexOf("http://");
            if (site >=0) {
                int end = v.length();
                String chars = " \n\r\t";
                for (int c=0;c<chars.length();c++) {
                    int r = v.indexOf(chars.charAt(c), site);
                    if (r>=0 && r <end) {
                        end = r;
                    }
                }
                return v.substring(site, end);
            }
            return null;

        }

	public static void runLobby(Risk risk) {
		try {
			if (lobbyURL!=null) {

				URLClassLoader ucl = URLClassLoader.newInstance(new URL[] { new URL("jar:"+lobbyURL+"/LobbyClient.jar!/") } );

				Class lobbyclass = ucl.loadClass("org.lobby.client.LobbyClientGUI");

				// TODO: should be like this
				//lobbyclass.newInstance();

				final javax.swing.JPanel panel = (javax.swing.JPanel)lobbyclass.getConstructor( new Class[] { URL.class } ).newInstance( new Object[] { new URL(lobbyURL) } );

				javax.swing.JFrame gui = new javax.swing.JFrame("yura.net Lobby");
				gui.setContentPane(panel);
				gui.setSize(800, 600);
				gui.setVisible(true);



				gui.addWindowListener( new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent evt) { panel.setVisible(false); }
				});
				panel.setVisible(true);

			}
			//else if (lobbyAppletURL!=null) {
                        else {
                                // on older clients open URL
				RiskUtil.openURL(new URL(lobbyAppletURL));
			}
		}
		catch(Exception e) {
			risk.showMessageDialog("unable to run the lobby: "+e.toString() );
		}
	}

        private static File getFile(URL url) {

            String dir = url.toString();
            File md;

            try {
                    md = new File(new URI(dir));
            }
            catch(IllegalArgumentException e) {

                    // this is an attempt at a crazy workaround that should not really work
                    if ( dir.startsWith("file://") ) {
                            md = new File( dir.substring(5,dir.length()).replaceAll("\\%20"," ") );
                    }
                    else {
                            System.err.println("this should never happen! bad file: "+dir);
                            md = new File( "maps/" );
                    }

                    // There is a bug in java 1.4/1.5 where it can not convert a URL like
                    // file://Claire/BIG_DISK/Program Files/Risk/maps/
                    // into a File Object so we will just try and make a simple file
                    // object, and hope it works

                    // java.lang.IllegalArgumentException: URI has an authority component
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5086147
                    // This has been fixed in java 1.6

                    // also can not have %20 in the name on 1.4/1.5, needs to be " ".

            }
            catch(Exception e) {
                    throw new RuntimeException("Cant create file: "+ dir, e);
            }

            return md;
        }

        private static final String GTK_PLAF_CLASS = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

	private static void setupLookAndFeel() {
		// set up system Look&Feel
		try {
                    String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
                    String os = System.getProperty("os.name");

                    // when we run in KDE, for some reason it does not select GTK as the theme, it picks Metal, but the fonts are way too small
                    if (os != null && os.startsWith("Linux") && !systemLookAndFeel.equals(GTK_PLAF_CLASS) && isPLAFInstalled(GTK_PLAF_CLASS)) {
                        systemLookAndFeel = GTK_PLAF_CLASS;
                    }
                    
                    AffineTransform dat = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();

                    // the system (gtk) theme on linux is broken with hi res screens (fonts are HUGE)
                    if (dat.getScaleX() != 1.0 && GTK_PLAF_CLASS.equals(systemLookAndFeel)) {
                        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

                        // for some crazy reason this is not set (happens on jdk8 - jdk13 on linux)
                        // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6780500
                        // https://stuffthathappens.com/blog/nimbus-jtoolbar-bug/
                        UIManager.put("ToolBar:Button[Disabled].textForeground", UIManager.getColor("nimbusDisabledText"));
                        UIManager.put("ToolBar:ToggleButton[Disabled].textForeground", UIManager.getColor("nimbusDisabledText"));
                    }
                    else {
			UIManager.setLookAndFeel(systemLookAndFeel);

			// the TabbedPaneUI in GTK theme has stopped working in Linux, use the default UI
			// https://bugs.openjdk.java.net/browse/JDK-8232865
			// there is no way to find out if its working other then draw the tab and see what happens
			// other then that everything behaves normally, its in the native GTK code that it does not draw
			if (GTK_PLAF_CLASS.equals(systemLookAndFeel) && !canDrawTabs()) {
			    UIManager.put("TabbedPaneUI", BasicTabbedPaneUI.class.getName());
			}
                    }
		}
		catch (Exception e) {
                        logger.log(Level.WARNING, "plaf failed to load", e);
		}

		// only do this check if there is NO sandbox
		// as otherwise we will get an exception anyway
		if (checkForNoSandbox()) {
			// check for java bug with JFileChooser
			try {
                                // as a side effect this also causes any swing init warnings to happen before we start grasshopper (FontUtilities.getFont2D -> CFontManager.loadFonts -> CFontManager.getFontFamilyWithExtraTry)
                                // e.g. Warning: the fonts "Times" and "Times" are not available for the Java logical font "Serif", which may have unexpected appearance or behavior. Re-enable the "Times" font to remove this warning.
                                // it is VITAL that this warning happens before grasshopper init as it can cause grasshopper deadlocks
				new JFileChooser();
			}
			catch (Throwable th) {
				logger.log(Level.INFO, "PLAF JFileChooser FAIL, falling back to metal", th);
				try {
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				}
				catch (Exception e) {
					logger.log(Level.WARNING, "metal plaf failed to load", e);
				}
			}
		}
	}
        
        private static boolean isPLAFInstalled(String className) {
            UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
            for (int c = 0; c < plafs.length; c++) {
                if (className.equals(plafs[c].getClassName())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @see com.sun.java.swing.plaf.gtk.GTKPainter#paintTabbedPaneTabBackground(javax.swing.plaf.synth.SynthContext, java.awt.Graphics, int, int, int, int, int)
         */
        private static boolean canDrawTabs() {
            try {
                JTabbedPane component = new JTabbedPane();
                javax.swing.plaf.synth.Region id = javax.swing.plaf.synth.Region.TABBED_PANE_TAB;
                javax.swing.plaf.synth.SynthStyle style = javax.swing.plaf.synth.SynthLookAndFeel.getStyle(component, id);
                javax.swing.plaf.synth.SynthContext context = new javax.swing.plaf.synth.SynthContext(component, id, style, javax.swing.plaf.synth.SynthConstants.ENABLED);
                javax.swing.plaf.synth.SynthPainter painter = style.getPainter(context);

                BufferedImage img = new BufferedImage(50, 25, BufferedImage.TYPE_4BYTE_ABGR);
                painter.paintTabbedPaneTabBackground(context, img.getGraphics(), 0, 0, img.getWidth(), img.getHeight(), 0);
                int[] out = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
                for (int a : out) {
                    if (a != 0) {
                        return true;
                    }
                }
                return false;
            }
            catch (Throwable th) {
                return true;
            }
        }
        
        public static void fontSize(int szIncr) {

            if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
                try {
                    Font font = (Font)UIManager.get("defaultFont");
                    UIManager.setLookAndFeel(UIManager.getLookAndFeel().getClass().getName());
                    UIManager.getLookAndFeelDefaults().put("defaultFont", new FontUIResource(font.getName(), font.getStyle(), font.getSize() + szIncr));
                    return;
                }
                catch (Exception ex) { }
            }

            // UIManager.getDefaults() MultiUIDefaults with fallbacks
            // UIManager.getLookAndFeelDefaults() UIDefaults for only the current theme
            Map<Object, Object> uidefCopy = new HashMap(UIManager.getDefaults());
            UIDefaults lookAndFeelDefaults = UIManager.getLookAndFeelDefaults();
            Map<Font, FontUIResource> newFonts = new HashMap();

            //order of getting a font
            // 1) overrides stored MultiUIDefaults (that extends Hashtable) directly (we can put things there with UIManager.put(...))
            // 2) look and feel stored in MultiUIDefaults.tables[0] (we can put things there with UIManager.getLookAndFeelDefaults().put(...))
            // 3) system defaults stored in MultiUIDefaults.tables[1] (we can not store things here)

            for (Map.Entry<Object,Object> e : uidefCopy.entrySet()) {
                Object val = e.getValue();

                if (String.valueOf(e.getKey()).toLowerCase().endsWith("font")) {
                    if (val instanceof UIDefaults.ActiveValue) {
                        UIDefaults.ActiveValue av = (UIDefaults.ActiveValue)val;
                        val = av.createValue(lookAndFeelDefaults);
                    }
                    else if (val instanceof UIDefaults.LazyValue) {
                        UIDefaults.LazyValue av = (UIDefaults.LazyValue)val;
                        val = av.createValue(lookAndFeelDefaults);
                    }
                }

                if (val instanceof Font) {
                    Font fui = (Font)val;

                    FontUIResource newFont = newFonts.get(fui);
                    if (newFont == null) {
                        newFont = new FontUIResource(fui.getName(), fui.getStyle(), fui.getSize() + szIncr);
                        newFonts.put(fui, newFont);
                    }

                    lookAndFeelDefaults.put(e.getKey(), newFont);
                }
            }
        }

        /**
         * In nimbus theme it does not respect a ColorUIResource when setting a color
         */
        public static Color getColorFromColorUIResource(Color colorUIResource) {
            return colorUIResource instanceof UIResource ? new Color(colorUIResource.getRGB(), true) : colorUIResource;
        }







	private static String maps;
	private static String cards;
	private static HashMap fileio = new HashMap();

	public static void parseArgs(String[] args) {

		TranslationBundle.parseArgs(args);

		for (int nA = 0; nA < args.length; nA++ ) {

			if (args[nA].length() > 5 && args[nA].substring(0,5).equals( "maps=")) {

				maps = args[nA].substring(5);
			}
			if (args[nA].length() > 6 && args[nA].substring(0,6).equals( "cards=")) {

				cards = args[nA].substring(6);
			}
		}

                // setup the maps dir for none applets
                setupMapsDir(null);
	}

    public static void openFile(String[] argv, Risk r) {
        List files = new ArrayList();
        for (int c=0;c<argv.length;c++) {
            String arg = argv[c];
            if (arg.indexOf('=')<0 && arg.length()>0 && arg.charAt(0)!='-') {
                files.add(arg);
            }
        }
        if (files.size()==1) {
            r.parser("loadgame "+files.get(0));
        }
        else if (files.size() > 1) {
            JOptionPane.showMessageDialog(null, "unknown command: "+files );
        }
    }

    public static Color getTextColorFor(Color color) {
        return new Color( ColorUtil.getTextColorFor(color.getRGB()) );
    }

    public static void donate(Component parent) {
        try {
                RiskUtil.donate();
        }
        catch(Exception e) {
                JOptionPane.showMessageDialog( parent ,"Unable to open web browser: "+e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean canWriteTo(File dir) {
        try {
            File tmp = new File(dir, "del.me");
            tmp.createNewFile();
            tmp.deleteOnExit();
            tmp.delete();
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }


    private static File gameDir;
    public static File getSaveGameDir() {

        if (gameDir!=null) {
            return gameDir;
        }

        File saveDir = new File(SAVES_DIR);
        if (RiskUIUtil.canWriteTo(saveDir)) {

            gameDir = saveDir;
            return saveDir;
        }

        // oh crap, we have hit Win Vista/7 UAC

        File userHome = new File( System.getProperty("user.home") );
        File userMaps = new File(userHome, RiskUtil.GAME_NAME + " Saves");
        
        if (isWindows() && !userMaps.exists()) {
            File savedGames = new File(userHome, "Saved Games");
            if (savedGames.exists() && savedGames.isDirectory()) {
                userMaps = new File(savedGames, userMaps.getName());
            }
        }
        
        if (!userMaps.isDirectory() && !userMaps.mkdirs()) { // if it does not exist and i cant make it
            throw new RuntimeException("can not create dir "+userMaps);
        }

        gameDir = userMaps;
        return userMaps;
    }






    private static File mapsDir;
    public static File getSaveMapDir() {

        if (mapsDir!=null) {
            return mapsDir;
        }

        File saveDir = getFile(mapsdir);
        if (RiskUIUtil.canWriteTo(saveDir)) {

            mapsDir = saveDir;
            return saveDir;
        }

        // oh crap, we have hit Win Vista/7 UAC

        File userHome = new File( System.getProperty("user.home") );
        File userMaps = new File(userHome, RiskUtil.GAME_NAME+" Maps");
        // whats going on here?? we call !userMaps.isDirectory() twice
        // just in case mkdirs returned false but still made the folder, we check again
        if (!userMaps.isDirectory() && !userMaps.mkdirs() && !userMaps.isDirectory()) { // if it does not exist and i cant make it
            throw new RuntimeException("can not create dir " + userMaps +
                    " exists=" + userMaps.exists() + " isDirectory=" + userMaps.isDirectory() +
                    " userHome.exists=" + userHome.exists() + " userHome.canWrite=" + userHome.canWrite());
        }

        mapsDir = userMaps;
        return userMaps;
    }



    /**
     * @see net.yura.domination.mapstore.MapChooser#createImage(java.io.InputStream)
     */
    public static BufferedImage read(InputStream in) throws IOException {
        try {
            BufferedImage img = ImageIO.read(in);
            if (img==null) {
                throw new IOException("ImageIO.read returned null");
            }
            return img;
        }
        finally {
            try {
                in.close();
            }
            catch (Throwable th) { }
        }
    }

    public static void setMinimumSize(Window window, Dimension size) {
        try {
            // this method can be buggy on windows when scale is not 1
            // https://stackoverflow.com/questions/67504604/inconsistent-beahaviour-with-jframess-setminimumsize-and-manully-dragging-when
            window.setMinimumSize(size);
        }
        catch(NoSuchMethodError ex) {
            // must me java 1.4
            if (window instanceof Dialog) {
                ((Dialog)window).setResizable(false);
            }
            else if (window instanceof Frame) {
                ((Frame)window).setResizable(false);
            }
        }
    }
    
    
    public static void initAudio(Risk risk) {
        try {
            GameSound.INSTANCE.setAudioSystem(risk, new SimpleAudio());
            GameSound.INSTANCE.load("medieval");
        }
        catch (FileNotFoundException ex) {
            logger.log(Level.INFO, "unable to load medieval theme", ex);
        }
        catch (Throwable th) {
            logger.log(Level.WARNING, "SimpleAudio unable to load", th);
        }

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            boolean appActive = true;
            boolean oldValue;
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof WindowEvent) {
                    WindowEvent we = (WindowEvent)event;
                    if (we.getID() == WindowEvent.WINDOW_GAINED_FOCUS && !appActive) { // WindowEvent.WINDOW_ACTIVATED
                        if (oldValue) {
                            GameSound.INSTANCE.setMusicEnabled(oldValue);
                        }
                        appActive = true;
                    }
                    else if (we.getID() == WindowEvent.WINDOW_LOST_FOCUS && we.getOppositeWindow() == null && // WindowEvent.WINDOW_DEACTIVATED
                            // this one will always come, but sometimes not in time, so when we check its not there yet :-(
                            (Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(WindowEvent.WINDOW_GAINED_FOCUS) == null)) {
                            //if we know the window is closing this is prob not the app losing focus, but this does not always work
                            //(Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(WindowEvent.WINDOW_CLOSED) == null) &&
                            //(Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(ComponentEvent.COMPONENT_HIDDEN) == null)
                            // repaint events come as INVOCATION_DEFAULT, so we cant use it to check as we can get them after app focus lost
                            //(Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(InvocationEvent.INVOCATION_DEFAULT) == null) &&

                        // maybe we need to stop the music, but we are still not 100% sure
                        // wait a tiny bit and then check if a window has focus
                        invokeLater(200, new Runnable() {
                            @Override
                            public void run() {

                                // if we have an active window, we have not lost app focus so should keep playing music
                                if (javax.swing.FocusManager.getCurrentManager().getActiveWindow() != null) { // && window.isFocused()
                                    return;
                                }

                                oldValue = GameSound.INSTANCE.isMusicEnabled();
                                GameSound.INSTANCE.setMusicEnabled(false);
                                appActive = false;
                            }
                        });

                    }
                }
            }
        }, AWTEvent.WINDOW_FOCUS_EVENT_MASK); // AWTEvent.WINDOW_EVENT_MASK works with WindowEvent.WINDOW_ACTIVATED and WindowEvent.WINDOW_DEACTIVATED,
                                              // with much the same result. on macOS FileDialog also does not register and seems to stop the music.
    }

    /**
     * @see SwingUtilities#invokeLater(java.lang.Runnable) 
     */
    public static void invokeLater(int delay, final Runnable run) {
        new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    run.run();
                }
                finally {
                    ((Timer)e.getSource()).stop();
                }
            }
        }).start();
    }
    
    public static void openOptions(Component parentComponent, Risk myrisk, boolean myTurn, Preferences preferences) {

                ResourceBundle resB = TranslationBundle.getBundle();

                JCheckBox showDice = new JCheckBox("Show dice", Risk.isShowDice());

                JSpinner aiwait = new JSpinner(new SpinnerNumberModel(AIManager.getWait(), 0, 10000, 100));
                JPanel aiWaitPanel = new JPanel();
                aiWaitPanel.add(new JLabel("AI wait time:"));
                aiWaitPanel.add(aiwait);
                aiWaitPanel.add(new JLabel("milliseconds"));

                JCheckBox colorBlindEnabled = new JCheckBox(resB.getString("game.menu.colorblind"), RiskUIUtil.colorBlind);
                JCheckBox soundEnabled = new JCheckBox(resB.getString("game.menu.sound"), GameSound.INSTANCE.isSoundEnabled());
                JCheckBox musicEnabled = new JCheckBox(resB.getString("game.menu.music"), GameSound.INSTANCE.isMusicEnabled());

                JCheckBox autoEndGo = new JCheckBox("Auto End Go", myrisk.getAutoEndGo());
                JCheckBox autoDefend = new JCheckBox("Auto Defend", myrisk.getAutoDefend());

                if (!myTurn) {
                    autoEndGo.setEnabled(false);
                    autoDefend.setEnabled(false);
                }

                int result = JOptionPane.showConfirmDialog(parentComponent,                             // the parent that the dialog blocks
                    new Component[] {                                    // the dialog message array
                            showDice,aiWaitPanel,
                            colorBlindEnabled, soundEnabled, musicEnabled,
                            autoEndGo, autoDefend
                    },
                    "Options", // the title of the dialog window
                    JOptionPane.OK_CANCEL_OPTION,                 // option type
                    JOptionPane.PLAIN_MESSAGE            // message type
                );

                if (result == JOptionPane.OK_OPTION) {
                        Risk.setShowDice(showDice.isSelected());
                        AIManager.setWait(((Integer)aiwait.getValue()).intValue());
                        GameSound.INSTANCE.setSoundEnabled(soundEnabled.isSelected());
                        GameSound.INSTANCE.setMusicEnabled(musicEnabled.isSelected());
                        RiskUIUtil.colorBlind = colorBlindEnabled.isSelected();
                        preferences.putBoolean(RiskSettings.COLOR_BLIND_KEY, RiskUIUtil.colorBlind);
                        
                        preferences.putBoolean(RiskSettings.SOUND_KEY, GameSound.INSTANCE.isSoundEnabled());
                        preferences.putBoolean(RiskSettings.MUSIC_KEY, GameSound.INSTANCE.isMusicEnabled());
            
                        RiskSettings.saveGameSettingsToPrefs(preferences);

                        if (autoEndGo.isEnabled()) {
                            boolean autoendgo = autoEndGo.isSelected();
                            // "autoendgo on" may trigger the end of my go, so must be changed last
                            if (myrisk.getAutoEndGo() != autoendgo) {
                                myrisk.parser("autoendgo " + (autoendgo ? "on" : "off"));
                            }
                        }

                        if (autoDefend.isEnabled()) {
                            boolean autodefend = autoDefend.isSelected();
                            if (myrisk.getAutoDefend() != autodefend) {
                                myrisk.parser("autodefend " + (autodefend ? "on" : "off"));
                            }
                        }
                }
        }
    
        private static boolean colorBlind;

        static Map<Integer,Image> icons = new HashMap();
        static {
            loadColorIcon(ColorUtil.RED, "/color_red.png");
            loadColorIcon(ColorUtil.BLUE, "/color_blue.png");
            loadColorIcon(ColorUtil.YELLOW, "/color_yellow.png");
            loadColorIcon(ColorUtil.CYAN, "/color_cyan.png");
            loadColorIcon(ColorUtil.GREEN, "/color_green.png");
            loadColorIcon(ColorUtil.MAGENTA, "/color_magenta.png");
        }

        private static void loadColorIcon(int color, String name) {
            try {
                icons.put(color, RiskUIUtil.getUIImage(PicturePanel.class, name));
            }
            catch(Throwable ex) {
                logger.log(Level.WARNING, "can not load color icon for  " + name, ex);
            }
        }

        /**
         * @see net.yura.domination.android.StatsActivity#getIcon(Player)
         */
        public static Image getIconForColor(int color) {
            return colorBlind ? icons.get(color) : null;
        }

        public static void loadSettingsFromPrefs(Preferences prefs) {
            RiskSettings.loadGameSettingsFromPrefs(prefs);
            
            if (prefs != null) {

                GameSound.INSTANCE.setSoundEnabled(prefs.getBoolean(RiskSettings.SOUND_KEY, GameSound.INSTANCE.isSoundEnabled()));
                GameSound.INSTANCE.setMusicEnabled(prefs.getBoolean(RiskSettings.MUSIC_KEY, GameSound.INSTANCE.isMusicEnabled()));

                colorBlind = prefs.getBoolean(RiskSettings.COLOR_BLIND_KEY, false);
            }
        }
        
        /**
         * @see net.yura.domination.mobile.flashgui.GameWindow
         */
        public static void drawDashboard(Graphics2D g, RiskGame game, boolean wrap) {

            FontMetrics font = g.getFontMetrics();
            List<Player> players = game.getPlayers();
            Player current = game.getCurrentPlayer();

            final int lineHeight = font.getHeight();
            int y = (wrap ? lineHeight * 2 : lineHeight);

            int xPadding = font.stringWidth("  ");
            String[][] data = RiskUtil.getDashboardText(game);

            // Compute max width for each column
            int[] colWidths = new int[data[0].length];
            for (String[] row : data) {
                for (int j = 0; j < row.length; j++) {
                    colWidths[j] = Math.max(colWidths[j], font.stringWidth(row[j]));
                }
            }

            // Draw each row
            for (int row = 0; row < data.length; row++) {
                int x = wrap ? lineHeight : lineHeight * 2;
                Player p = players.get(row);
                if (p == current) {
                    g.setColor(Color.WHITE);
                    String arrow = "\u25ba";
                    g.drawString(arrow, x - font.stringWidth(arrow), y);
                }
                g.setColor(new Color(p.getColor()));
                boolean isLightText = ColorUtil.isColorLight(p.getColor());

                for (int col = 0; col < data[row].length; col++) {
                    if (isLightText) {
                        TextUtil.drawString(g, data[row][col], x, y);
                    }
                    else {
                        TextUtil.drawStringWithOutline(g, data[row][col], x, y, Color.WHITE);
                    }

                    if (wrap && col == 0) {
                        x += lineHeight + font.stringWidth(" "); // player emoji width + space
                        y += lineHeight;
                    }
                    else {
                        x += colWidths[col] + xPadding;
                    }
                }
                y += lineHeight + (wrap ? xPadding : xPadding / 2);
            }
        }
}
