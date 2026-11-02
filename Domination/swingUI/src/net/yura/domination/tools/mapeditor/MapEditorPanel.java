// Yura Mamyrin

package net.yura.domination.tools.mapeditor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.guishared.PicturePanel;
import net.yura.swing.JTable;

/**
 * @author Yura Mamyrin
 */
public class MapEditorPanel extends JPanel implements MouseInputListener,MouseWheelListener {

	public static final int MODE_MOVE = 0;
	public static final int MODE_MOVEALL = 1;
	public static final int MODE_JOIN = 2;
	public static final int MODE_JOIN1WAY = 3;
	public static final int MODE_DISJOIN = 4;
	public static final int MODE_DRAW = 5;
        
        public static final int DEFAULT_BRUSH_SIZE = 5;
        
	//private List countries; // every item in this list also has its position+1 stored as the "color" value of it
	//private List continents;
	private RiskGame myMap;
	private BufferedImage pic;
	private BufferedImage map;
	private BufferedImage drawImage;
        private Rectangle drawRect;
	private Country selected;
	private Rectangle box;
	private int mode;
	private int brush = DEFAULT_BRUSH_SIZE;
	private float alpha;
        private float zoom = 1f;
	MapEditor editor;
        private ListSelectionListener selectionListener;
        private int smartDrawTolerance = 20;

        // these variables are used by the mouse listener interface
	private Point dragpoint;
	private boolean xdrag; // do we drag a single country or just move the map scroll position
	private Point[] countryPositions;

	public MapEditorPanel(MapEditor a) {
		editor = a;

        	addMouseMotionListener(this);
		addMouseListener(this);

		addMouseWheelListener(this);

		ToolTipManager.sharedInstance().setDismissDelay(10000);

		mode = MODE_MOVE;
	}
        
        public void setSelectionListener(ListSelectionListener listener) {
            selectionListener = listener;
        }

        public Country getSelectedCountry() {
            return selected;
        }

	public BufferedImage getImageMap() {
		return map;
	}


	public BufferedImage getImagePic() {
		return pic;
	}

	public void zoom(float a) {

	    zoom = a;

	    if (pic!=null) {

		Dimension size = new Dimension((int)(pic.getWidth() * zoom), (int)(pic.getHeight() * zoom));

		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);

		revalidate();
		repaint();
	    }
	}
        public float getZoom() {
            return zoom;
        }

	public void setImagePic(BufferedImage a,boolean checkmap) {
		pic = a;
            
		if (a.getWidth()!=PicturePanel.PP_X || a.getHeight()!=PicturePanel.PP_Y) {
                        // dont care about such old versions of the game any more
			//JOptionPane.showMessageDialog(this,"Only Risk 1.0.9.5+ supports any size maps!\nfor older version use: width="+PicturePanel.PP_X+" height="+PicturePanel.PP_Y);
		}

                if (checkmap && (pic.getWidth() != map.getWidth() || pic.getHeight() != map.getHeight())) {

                        // if we have no countires yet (this is a brand new map) then ignore this warnning
                        if (myMap.getCountries().length == 0) {
                            resizeAndSetImageMap(map, false);
                        }
                        else {
                            String[] options = {"Stretch", "Resize", "No"};
                            int result = JOptionPane.showOptionDialog(this,
                                    "This ImagePic does not match the ImageMap size!\n"
                                  + "ImagePic: "+pic.getWidth()+"x"+pic.getHeight()+"\n"
                                  + "ImageMap: "+map.getWidth()+"x"+map.getHeight()+"\n"
                                  + "They should match for the game to work!\n"      
                                  + "would you like to update the ImageMap size?",
                                    "?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                            if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) { 
                                    resizeAndSetImageMap(map, result == JOptionPane.YES_OPTION);
                            }
                        }
                }

		zoom(zoom);
	}
        
	private void resizeAndSetImageMap(BufferedImage imageMap, boolean stretch) {
		BufferedImage newmap = new BufferedImage(pic.getWidth(), pic.getHeight(), imageMap.getType());
		Graphics g = newmap.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, newmap.getWidth(), newmap.getHeight());
                
                if (stretch) {
                    double scale = Math.max(newmap.getWidth() / (double)imageMap.getWidth(), newmap.getHeight() / (double)imageMap.getHeight());
                    int newWidth = (int) (imageMap.getWidth() * scale);
                    int newHeight = (int) (imageMap.getHeight() * scale);
                    g.drawImage(imageMap, (newmap.getWidth() - newWidth) / 2, (newmap.getHeight() - newHeight) / 2, newWidth, newHeight, this);
                }
                else {
                    g.drawImage(imageMap, 0, 0, this);
                }
		g.dispose();
		setImageMap(newmap);
	}

	public void setImageMap(BufferedImage a) {

		if (a.getWidth() != pic.getWidth() || a.getHeight() != pic.getHeight() ) {
			int result = JOptionPane.showConfirmDialog(this,
				"This ImageMap does not match the ImagePic size!\n"
			      + "ImagePic: "+pic.getWidth()+"x"+pic.getHeight()+"\n"
			      + "ImageMap: "+a.getWidth()+"x"+a.getHeight()+"\n"
			      + "They should match for the game to work!\n"
			      + "would you like to update the ImageMap size?",
				"?", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            resizeAndSetImageMap(a, false); // this will just crop the image
                            return;
                        }
		}

		map = a;

		// some error happens when done like this?????
		//map = new BufferedImage( a.getWidth() , a.getHeight() , BufferedImage.TYPE_USHORT_GRAY);
		//Graphics g = map.getGraphics();
		//g.drawImage(a,0,0,this);
		//g.dispose();

		drawImage = new BufferedImage(a.getWidth(),a.getHeight(),BufferedImage.TYPE_BYTE_BINARY,
			new IndexColorModel(1, 2, new byte[] { 0, (byte)0xff }, new byte[] { 0, 0 }, new byte[] { 0, 0 }, 0)
		);

		box = new Rectangle( new Dimension(a.getWidth(), a.getHeight()) );
	}



	public void setMap(RiskGame a) {
		myMap = a;

		//countries = Arrays.asList( myMap.getCountries() );
		//continents = Arrays.asList( myMap.getContinents() );
	}

	public void update(Map a) {

		int width = map.getWidth();
		int height =  map.getHeight();

		// have to make new image coz if we reuse the old 1 we get 0 values for some reason		// cant use this as get 0 values

		// cant make a new image, coz ither removing stops working or drawing draws in a wrong color
		//BufferedImage newImageMap = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_INDEXED ); //  TYPE_BYTE_GRAY

		int[] pixels = map.getRGB(0,0,width,height,null,0,width);

		int oldcolor,newcolor;

		for (int c=0;c<pixels.length;c++) {

			oldcolor = pixels[c] & 0xff;

//if (a.get(Integer.valueOf(oldcolor)) == null) {
//System.out.println(oldcolor + " goes to " + a.get(Integer.valueOf(oldcolor)));
//}
			Object obj = a.get(Integer.valueOf(oldcolor));

			if (obj != null) {
				newcolor = ((Integer)obj).intValue();
			}
			else {
				newcolor = oldcolor;
				//System.out.println("bad color: "+oldcolor);
			}

//if (newcolor == 0) {
//System.out.println( oldcolor+" goes to 0!!!" );
//}

			pixels[c] = ((newcolor & 0xFF) << 16) | ((newcolor & 0xFF) << 8) | ((newcolor & 0xFF) << 0);

		}

		//newImageMap.
		map.setRGB(0,0,width,height,pixels,0,width);
		//map = newImageMap;
                editor.setMapChanged(true);
		repaint();
	}

        public void growEdges(Set<Integer> colorsToGrow) {

                int width = map.getWidth();
                int height = map.getHeight();

                int[] pixels = map.getRGB(0, 0, width, height, null, 0, width);
                int[] newpixels = new int[pixels.length];

		for (int position=0; position < pixels.length; position++) {

			int color = pixels[position];

                        int darkestNeighbour = ColorUtil.WHITE; // start with white

                        if (position >= width) {
                            int top = pixels[ position - width ]; // top
                            darkestNeighbour = top;
                        }
                        if (position % width != 0) {
                            int left = pixels[ position - 1 ]; // left
                            if (ColorUtil.getBrightness(left) < ColorUtil.getBrightness(darkestNeighbour)) {
                                darkestNeighbour = left;
                            }
                        }
                        if ((position+1) % width != 0) {
                            int right = pixels[ position + 1 ]; // right
                            if (ColorUtil.getBrightness(right) < ColorUtil.getBrightness(darkestNeighbour)) {
                                darkestNeighbour = right;
                            }
                        }
                        if (position < (pixels.length - width)) {
                            int bottom = pixels[ position + width ]; // bottom
                            if (ColorUtil.getBrightness(bottom) < ColorUtil.getBrightness(darkestNeighbour)) {
                                darkestNeighbour = bottom;
                            }
                        }

                        if (color != darkestNeighbour && ColorUtil.getBrightness(darkestNeighbour) < ColorUtil.getBrightness(color) &&
                                (!colorsToGrow.isEmpty() && colorsToGrow.contains(Integer.valueOf(darkestNeighbour & 0xff)))) {
                            color = darkestNeighbour;
                        }

                        newpixels[position] = color;
                }

                map.setRGB(0, 0, width, height, newpixels, 0, width);
                editor.setMapChanged(true);
                repaintSelected();
                repaint();
        }

	public void setSelectedCountry(Country a) {
	    if (selected != a) {
		selected = a;
                repaintSelected();
                if (selectionListener != null) {
                    int index = selected == null ? -1 : selected.getColor() - 1;
                    selectionListener.valueChanged(new ListSelectionEvent(this, index, index, false));
                }
	    }
	}

        public void repaintSelected() {
            int width = map.getWidth();
            int height =  map.getHeight();

            int[] pixels1 = map.getRGB(0,0,width,height,null,0,width);
            int[] pixels2 = drawImage.getRGB(0,0,width,height,null,0,width);

            int redColor = Color.RED.getRGB();

            int startX = Integer.MAX_VALUE,endX = Integer.MIN_VALUE,startY = Integer.MAX_VALUE,endY = Integer.MIN_VALUE;

            for (int c=0;c<pixels1.length;c++) {
                    if (selected!=null && selected.getColor() == (pixels1[c]&0xff) ) {
                            int x = c % width;
                            int y = c / width;

                            if (x < startX) { startX = x; }
                            if (x > endX) { endX = x; }
                            if (y < startY) { startY = y; }
                            if (y > endY) { endY = y; }

                            pixels2[c] = redColor;
                    }
                    else {
                            pixels2[c] = 0;
                    }
            }

            drawRect = startX == Integer.MAX_VALUE || endX == Integer.MIN_VALUE || startY == Integer.MAX_VALUE || endY == Integer.MIN_VALUE ? null : new Rectangle(startX - 1, startY - 1, endX - startX + 3, endY - startY + 3);
            drawImage.setRGB(0,0,width,height,pixels2,0,width);
            repaint();
        }

	public void setAlpha(int a) {
		alpha = a/100F;
	}

	public void setBrush(int a) {
		brush = (a==0)?1:a;
	}
        
	public void setMode(int a) {
		mode = a;
		dragpoint = null;
		repaint();
	}

    public void paintComponent(Graphics g) {
	super.paintComponent(g);

	Graphics2D g0 = (Graphics2D)g;
	g0.scale(zoom,zoom);


	if (myMap!=null) {

	    //System.out.println(alpha);

	    if (alpha!=1) {

            	drawImage(g, pic, 0, 0, this);

	    	drawCountries(g);
	    }

	    if (alpha!=0) {

		Graphics2D g2 = (Graphics2D)g.create();
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		g2.setComposite(ac);

		drawImage(g2, map, 0, 0, this);

                if (drawRect != null) {
                    Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);
                    g2.setStroke(dashed);
                    g2.setColor(Color.GREEN);
                    g2.drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
                }

		//if (mode == MODE_DRAW) {

			drawImage(g2, drawImage, 0, 0, this);

		//}
	    }



	    if (mode == MODE_DRAW && dragpoint!=null) {

		g.setXORMode(Color.WHITE);

		g.setColor(Color.BLACK);
                
                // TODO sometimes (linux java1.8 after scrolling) this draws nothing, and there is no way to tell if that happens
		g.drawOval(dragpoint.x-(brush/2),dragpoint.y-(brush/2),brush,brush);

		g.setPaintMode();
	    }
	}
    }

    // if we dont do this, draw is very slow on OS X
    public static void drawImage(Graphics g, Image img, int x, int y, ImageObserver io) {
        Rectangle clip = g.getClipBounds();
        clip = clip.intersection(new Rectangle(x, y, img.getWidth(io), img.getHeight(io)));
        int imgX = clip.x - x;
        int imgY = clip.y - y;
        g.drawImage(img, clip.x, clip.y, clip.x + clip.width, clip.y + clip.height, imgX, imgY, imgX + clip.width, imgY + clip.height, io);
    }

    int badness;
    private void drawCountries(Graphics g) {
        
            long time = System.currentTimeMillis();

	    int d = myMap.getCircleSize();

	    int width = pic.getWidth();

	    Country[] countries = myMap.getCountries();
            
            g.setFont( new java.awt.Font("Arial", java.awt.Font.PLAIN, (d+2)/2 ) );
            int fontHeight = g.getFontMetrics().getAscent();

            for (int i = 0; i < countries.length; i++) {

                Country n = countries[i];
                int x = n.getX();
                int y = n.getY();

		g.setColor( new Color( n.getContinent().getColor() ) );
                int r = d/2;
		g.fillOval( x-r , y-r, d, d );

                List ney = n.getNeighbours();
                for (int j = 0; j < ney.size(); j++) {

                    Country n1 = (Country)ney.get(j);
                    int x1 = n1.getX();
                    int y1 = n1.getY();

                    if (n1.getNeighbours().contains(n)) {
                        g.setColor(Color.BLUE);
		    }
                    else {
                        g.setColor(Color.GREEN);
		    }

			if ( Math.abs( x - x1 ) > ( width  / 2) ) {

				if ( x > (width / 2) ) { // ie "n" is on the right
					g.drawLine( x, y, x1+width, y1);
					g.drawLine( x-width, y, x1, y1);
				}
				else { // the attacker is on the left
					g.drawLine( x, y, x1-width, y1);
					g.drawLine( x+width, y, x1, y1);
				}
			}
			else {
                    		g.drawLine(x,y,x1,y1);
			}
		}



                if (selected == n) {
                    g.setColor(Color.RED);
		}
		else {
                    g.setColor(Color.BLUE);
		}
                g.drawRect(x-2,y-2,4,4);
                g.drawRect(x-3,y-3,6,6);

                // some java platforms have a HUGE bug where XOR is REALLY EEALLY SLOW
                // so if we find this method is being very slow, we must turn off using XOR
                boolean doXor = badness < 5;

                if (doXor) {
                    g.setXORMode(Color.WHITE);
                    //((Graphics2D)g).setComposite(AlphaComposite.Xor); // not sure how this works
                }

		g.setColor(Color.BLACK);

		g.drawString(n.getIdString(), x, y);
		g.drawString(String.valueOf(i+1), x, y + fontHeight);

                if (doXor) {
                    g.setPaintMode();
                }
            }
            
            long timeTaken = (System.currentTimeMillis() - time);
            
            if (timeTaken > 100) {
                badness++;
                System.out.println("XORMode Badness: " + timeTaken + " " + badness);
            }

    }

    public boolean contains(int x, int y) {

	if (myMap!=null) {

            Country mynode = getCountryAt((int)(x / zoom),(int)(y / zoom));

	    if (mynode!=null) {

		String show="<html><b>"+mynode.getIdString()+" ("+mynode.getColor()+")</b><br>Location: (x="+mynode.getX()+",y="+mynode.getY()+")<br>" +

		"Continent: " + mynode.getContinent();

                List ney = mynode.getNeighbours();
                for (int j = 0; j < ney.size(); j++) {

                    Country n1 = (Country)ney.get(j);

		    show = show + "<br>Neighbour: " + n1.getIdString() +" ("+n1.getColor()+")";
		}

		show = show + "</html>";

 		setToolTipText(show);
	    }
	    else {
		setToolTipText(null);
	    }

	    return true; // this is needed so the mouse listoner can use it

	}
	else {
	    return false;
	}
    }

	public void drawLine(Point a,Point b,boolean draw) {
            
                if (!box.contains(a) && !box.contains(b)) {
                    return;
                }
            
                // this fixes a really odd bug with drawing lines on indexed images
                if (a.y>b.y) {

                        Point z = a;
                        a = b;
                        b = z;
                }

                //@YURA:TODO  should not do this each time
                Graphics2D g1 = (Graphics2D)drawImage.getGraphics();
                Graphics2D g2 = (Graphics2D)map.getGraphics();

                BasicStroke bs = new BasicStroke(brush,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

                g1.setStroke(bs);
                g2.setStroke(bs);

                if (selected != null && draw) {
                    g1.setColor(Color.RED);
                    g2.setColor( new Color(selected.getColor(),selected.getColor(),selected.getColor()) );

                    Rectangle rectA = new Rectangle(a.x - brush / 2 -1, a.y - brush / 2 - 1, brush + 2, brush + 2);
                    if (drawRect == null) {
                        drawRect = rectA;
                    }
                    else {
                        drawRect.add(rectA);
                    }
                    drawRect.add(new Rectangle(b.x - brush / 2 -1, b.y - brush / 2 - 1, brush + 2, brush + 2));
                }
                else {
                    g1.setColor(Color.BLACK);
                    g2.setColor(Color.WHITE);
                }

                g1.drawLine(a.x, a.y, b.x, b.y);
                g2.drawLine(a.x, a.y, b.x, b.y);

                g1.dispose();
                g2.dispose();
                editor.setMapChanged(true);
	}

	public Country getCountryAt(int x,int y) {

			Country[] countries = myMap.getCountries();

			int size = 4;

			Country mynode = null;
			for (int i = 0; i < countries.length; i++) {

				Country n = countries[i];
				int x1 = n.getX();
				int y1 = n.getY();
				if (x1 >= x - size && y1 >= y - size && x1 <= x + size && y1 <= y + size) {
					mynode = n;
					break;
				}
			}

			return mynode;
	}

	public Point getPointOnImage(MouseEvent e) {
		return new Point((int)(e.getX() / zoom),(int)(e.getY() / zoom));
	}

        public void autodraw(Collection<Country> countries, boolean dots) {
            BufferedImage imgMap = getImageMap();
            Graphics g = imgMap.getGraphics();
            int size = myMap.getCircleSize();
            for (Country country:countries) {
                Color color = new Color(country.getColor(), country.getColor(), country.getColor());
                if (dots) {
                    g.setColor(color);
                    g.fillOval(country.getX()-(size/2),country.getY()-(size/2),size,size);
                }
                else {
                    ImageUtil.floodFill(imgMap, country.getX(), country.getY(), color.getRGB());
                }
            }
            g.dispose();
            editor.setMapChanged(true);
            repaintSelected();
        }
        
        public void smartDraw(Collection<Country> selectedCountries) {
            JSpinner tolerance = new JSpinner(new SpinnerNumberModel(smartDrawTolerance,0,255,1) );
            int result = JOptionPane.showConfirmDialog(this, new Object[] {
                MapEditor.getCountiresListMessage(selectedCountries),
                "Smart Fill will use the color from the Image Pic\nto select the area in the Image Map. Tolerance:", tolerance}, "Smart Fill", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                smartDrawTolerance = ((Number)tolerance.getValue()).intValue();
                for (Country country : selectedCountries) {
                    Color color = new Color(country.getColor(), country.getColor(), country.getColor());
                    ImageUtil.smartFill(getImagePic(), getImageMap(), country.getX(), country.getY(), color.getRGB(), smartDrawTolerance);
                }
                editor.setMapChanged(true);
                repaintSelected();
            }
        }

        public void delIslands(Collection<Country> countries) {
            Set<Integer> findColors = new HashSet();
            for (Country country : countries) {
                Color color = new Color(country.getColor(),country.getColor(),country.getColor());
                findColors.add(color.getRGB());
            }
            
            long startTime = System.currentTimeMillis();
            BufferedImage map = getImageMap();
            int width = map.getWidth();
            int[] pixels = map.getRGB(0,0,width,map.getHeight(),null,0,width);

            Map<Integer,List<Integer>> colorToPositions = new HashMap();
            for (int c=0;c<pixels.length;c++) {
                if (findColors.contains(pixels[c])) {
                    List<Integer> positions = colorToPositions.get( pixels[c] );
                    if (positions==null) { positions = new ArrayList(); colorToPositions.put(pixels[c], positions); }
                    positions.add(c);
                }
            }

            List<List<Integer>> allIslands = new ArrayList();
            for (List<Integer> positions:colorToPositions.values()) {
                List<Integer> largestIsland=null;
                List<List<Integer>> islands = new ArrayList();
                while (!positions.isEmpty()) {
                    List<Integer> island = new ArrayList();
                    Stack<Integer> stack = new Stack();
                    stack.push( positions.get(0) );
                    while (!stack.isEmpty()) {
                        int position = stack.pop();
                        int index = positions.indexOf(position);
                        if (index>=0) {
                            int pos = positions.remove(index);
                            island.add(pos);
                            if (position >= width) {
                                stack.push( position-width ); // top
                            }
                            if (position % width != 0) {
                                stack.push( position-1 ); // left
                            }
                            if ((position+1) % width != 0) {
                                stack.push( position+1 ); // right
                            }
                            stack.push( position + width ); // bottom
                        }
                    }
                    islands.add(island);
                    if (largestIsland==null || island.size() > largestIsland.size()) {
                        largestIsland = island;
                    }
                }
                islands.remove(largestIsland);
                allIslands.addAll(islands);
            }
            System.out.println("finished! took "+(System.currentTimeMillis()-startTime));

            if (allIslands.isEmpty()) {
                JOptionPane.showMessageDialog(this, MapEditor.getCountiresListMessage(countries) + "\nNo islands found");
            }
            else {
                final Map<Integer,Integer> counts = new TreeMap(); // island size -> number of islands
                final Map<Integer,Set<Integer>> colors = new TreeMap(); // island size -> island colors
                for (List<Integer> island: allIslands) {
                    int islandSize = island.size();
                    if (counts.get(islandSize)==null) {
                        counts.put(islandSize, 1);
                        colors.put(islandSize, new TreeSet());
                    }
                    else {
                        counts.put(islandSize, counts.get(islandSize)+1);
                    }
                    colors.get(islandSize).add(Integer.valueOf(pixels[island.get(0).intValue()] & 0xff));
                }

                final List<Integer> islandSizes = new ArrayList(counts.keySet());
                final boolean[] del = new boolean[islandSizes.size()];
                TableModel islandsTable = new AbstractTableModel() {
                        private static final int BOOL_ROW = 3;
			private final String[] columnNames = {"size", "count", "Countries", "del"};
			public int getColumnCount() {
				return columnNames.length;
			}
			public String getColumnName(int col) {
				return columnNames[col];
			}
			public int getRowCount() {
				return islandSizes.size();
  			}
			public Object getValueAt(int row, int col) {
				switch (col) {
					case 0: return islandSizes.get(row);
					case 1: return counts.get(islandSizes.get(row));
					case 2: return colors.get(islandSizes.get(row));
                                        case BOOL_ROW: return del[row];
					default: throw new RuntimeException();
				}
			}
                        public boolean isCellEditable(int row, int col) {
                                return col == BOOL_ROW;
                        }
                        public Class<?> getColumnClass(int col) {
                                return col == BOOL_ROW ? Boolean.class : super.getColumnClass(col);
                        }
                        public void setValueAt(Object aValue, int row, int col) {
                                if (col != BOOL_ROW) throw new RuntimeException();
                                del[row] = (Boolean)aValue;
                        }
		};
/*
                StringBuilder table = new StringBuilder();
                table.append("<table border=\"1\"><tr><th>size</th><th>count</th></tr>");
                for (Integer islandSize: counts.keySet()) {
                    table.append("<tr><td>");
                    table.append( islandSize );
                    table.append("</td><td>");
                    table.append( counts.get(islandSize) );
                    table.append("</td></tr>");
                }
                table.append("</table>");
*/
                final JTable islandsJTable = new JTable(islandsTable);
                islandsJTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent event) {
                            if (!event.getValueIsAdjusting() && islandsJTable.getSelectedRow() != -1) {
                                Set<Integer> colorsForRow = colors.get(islandSizes.get(islandsJTable.getSelectedRow()));
                                setSelectedCountry(myMap.getCountryInt(colorsForRow.iterator().next()));
                            }
                        }
                    });

                int result = JOptionPane.showConfirmDialog(this, new Object[] {
                    "<html>"+allIslands.size()+" islands found, are you sure you want to delete them from the map?",
                    new JScrollPane(islandsJTable)}, "Del Islands?", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    for (List<Integer> island: allIslands) {
                        if (del[islandSizes.indexOf(island.size())]) {
                            for (int pos: island) {
                                pixels[pos] = 0xFFFFFFFF;
                            }
                        }
                    }
                    map.setRGB(0,0,width,map.getHeight(),pixels,0,width);
                    editor.setMapChanged(true);
                    repaintSelected();
                }
            }
        }

        public void delPonds(Collection<Country> countries) {
            long startTime = System.currentTimeMillis();
            
            Set<Integer> findColors = new HashSet();
            for (Country country : countries) {
                Color color = new Color(country.getColor(),country.getColor(),country.getColor());
                findColors.add(color.getRGB());
            }

            BufferedImage map = getImageMap();
            int width = map.getWidth();
            int[] pixels = map.getRGB(0,0,width,map.getHeight(),null,0,width);

            // find possible start positions, when a white pixel is bellow a country pixel
            Map<Integer,List<Integer>> colorToStartPositions = new HashMap();
            for (int c = 0; c < (pixels.length - width); c++) {
                if (findColors.contains(pixels[c])) {
                    List<Integer> positions = colorToStartPositions.get( pixels[c] );
                    if (positions==null) { positions = new ArrayList(); colorToStartPositions.put(pixels[c], positions); }
                    
                    // check if pixel bellow is white and then add it
                    if (pixels[c + width] == 0xFFFFFFFF) {
                        positions.add(c + width);
                    }
                }
            }

            Map<Integer, List<List<Integer>>> allPonds = new HashMap();
            for (Map.Entry<Integer,List<Integer>> countryInfo:colorToStartPositions.entrySet()) {
                int countryColor = countryInfo.getKey();
                List<Integer> startPositions = countryInfo.getValue();
                List<List<Integer>> ponds = new ArrayList();
                outer: while (!startPositions.isEmpty()) {
                    List<Integer> pond = new ArrayList();
                    Stack<Integer> stack = new Stack();
                    stack.push(startPositions.get(0) );
                    while (!stack.isEmpty()) {
                        int position = stack.pop();

                        if (pixels[position] != 0xFFFFFFFF && pixels[position] != countryColor) { // we have found a bad color
                            continue outer;
                        }
                        if (pixels[position] == 0xFFFFFFFF && !pond.contains(position)) {
                            pond.add(position);

                            startPositions.remove(Integer.valueOf(position));

                            if (position < width) { // we are at the top edge
                                continue outer;
                            }
                            else {
                                stack.push(position - width); // top
                            }

                            if (position % width == 0) { // we are at the left edge
                                continue outer;
                            }
                            else {
                                stack.push(position - 1); // left
                            }

                            if ((position+1) % width == 0) { // we are at the right edge
                                continue outer;
                            }
                            else {
                                stack.push(position + 1); // right
                            }

                            if (position >= (pixels.length - width)) { // we are at the bottom edge
                                continue outer;
                            }
                            else {
                                stack.push(position + width); // bottom
                            }
                        }
                    }
                    
                    ponds.add(pond);
                }
                
                if (!ponds.isEmpty()) {
                    allPonds.put(countryColor, ponds);
                }
            }
            System.out.println("finished! took "+(System.currentTimeMillis()-startTime));

            if (allPonds.isEmpty()) {
                JOptionPane.showMessageDialog(this, MapEditor.getCountiresListMessage(countries) + "\nNo ponds found");
            }
            else {
                for (Map.Entry<Integer, List<List<Integer>>> countryPonds: allPonds.entrySet()) {
                    int countryColor = countryPonds.getKey();
                    System.out.println("for country " + (countryColor & 0xFF) + " found " + countryPonds.getValue().size() + " ponds");
                    for (List<Integer> ponds : countryPonds.getValue()) {
                        for (int pos: ponds) {
                            pixels[pos] = countryColor;
                        }
                    }
                }
                map.setRGB(0,0,width,map.getHeight(),pixels,0,width);
                editor.setMapChanged(true);
                repaintSelected();
            }
        }

	// #############################################################
	// ###################### mouse ###########################
	// ##################################################


    public void mouseWheelMoved(MouseWheelEvent e) {
        Point point = e.getPoint();

	if (e.getWheelRotation() < 0) {
	    editor.zoom(true, point);
	}
	else if (e.getWheelRotation() > 0) {
	    editor.zoom(false, point);
	}
    }

        /**
         * TODO!!!!!!! this method does not work on macOS High Sierra, java 1.8
         */
	public void mouseClicked(MouseEvent e) {

		if (myMap!=null) {

		    Point point = getPointOnImage(e);

		    if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
			if (mode != MODE_DRAW) {
				setSelectedCountry(null);
			}
		    }
		    else if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {

			if (mode == MODE_JOIN) {

				Country mynode = getCountryAt(point.x,point.y);

				if (mynode!=null && selected==null) {

					setSelectedCountry(mynode);

				}
				else if (mynode!=null && mynode==selected) {

					setSelectedCountry(null);

				}
				else if (mynode!=null) {

					if (!selected.getNeighbours().contains(mynode)) {
						selected.addNeighbour(mynode);
					}
					if (!mynode.getNeighbours().contains(selected)) {
						mynode.addNeighbour(selected);
					}
                                        editor.setMapChanged(true);
					repaint();
				}

			}
			else if (mode == MODE_JOIN1WAY) {

				Country mynode = getCountryAt(point.x,point.y);

				if (mynode!=null && selected==null) {

					setSelectedCountry(mynode);

				}
				else if (mynode!=null && mynode==selected) {

					setSelectedCountry(null);

				}
				else if (mynode!=null) {

					if (!selected.getNeighbours().contains(mynode)) {
						selected.addNeighbour(mynode);
					}
                                        editor.setMapChanged(true);
					repaint();
				}
			}
			else if (mode == MODE_DISJOIN) {

				Country mynode = getCountryAt(point.x,point.y);

				if (mynode!=null && selected==null) {

					setSelectedCountry(mynode);

				}
				else if (mynode!=null && mynode==selected) {

					setSelectedCountry(null);

				}
				else if (mynode!=null) {

					if (selected.getNeighbours().contains(mynode)) {
						selected.getNeighbours().remove(mynode);
					}
					if (mynode.getNeighbours().contains(selected)) {
						mynode.getNeighbours().remove(selected);
					}
                                        editor.setMapChanged(true);
					repaint();
				}
			}
		    }
		}
	}

	public void mousePressed(MouseEvent e) {

		if ( myMap!=null && (

				( (e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) ||
				( (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK)

		)) {

			Point point = getPointOnImage(e);

			if (mode==MODE_MOVE) {

				Country mynode = getCountryAt(point.x,point.y);

				if (mynode!=null) {
					setSelectedCountry(mynode);
					xdrag = true;
				}
				else {
					dragpoint = e.getPoint();
					xdrag = false;
				}
			}
			else if (mode==MODE_MOVEALL) {
				dragpoint = point;
                                
                                if (e.isShiftDown()) {
                                    Country[] countries = myMap.getCountries();
                                    countryPositions = new Point[countries.length];
                                    for (int i = 0; i < countries.length; i++) {
                                        countryPositions[i] = new Point(countries[i].getX(), countries[i].getY());
                                    }
                                }
			}
			else if (mode==MODE_DRAW) {

				dragpoint = point;

				drawLine(dragpoint,dragpoint, ( (e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) );

				repaint();
			}
		}
	}

        public void mouseReleased(MouseEvent e) {

		if (mode == MODE_MOVE) {
			dragpoint=null;
		}
		else if (mode == MODE_MOVEALL) {
			dragpoint=null;
                        countryPositions = null;
		}				
		else if (mode == MODE_DRAW) {

                        // if mouse released outside the box
                        if (!getVisibleRect().contains(e.getPoint())) {
                            // ((JViewport)getParent()).getViewRect()
                            dragpoint=null;
                            repaint();
                        }

                        // if we were using the eraser we may have shrunk the bounds of the current country, so we must re-calculate it
                        if (selected != null && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != MouseEvent.BUTTON1_MASK) {
                                repaintSelected();
                        }
		}
	}

        public void mouseDragged(MouseEvent e) {

	    if (myMap!=null) {

		Point point = getPointOnImage(e);

		if (mode == MODE_MOVE) {

			if (xdrag && box != null && selected != null && box.contains(point)) {

				selected.setX(point.x);
				selected.setY(point.y);

				//dragpoint = point;

				scrollRectToVisible( new Rectangle(e.getX(), e.getY(), 1, 1) );
                                editor.setMapChanged(true);
				repaint();
			}
			else if (!xdrag && dragpoint!=null) {

				Rectangle r = getVisibleRect();
				r.translate(dragpoint.x-e.getX(),dragpoint.y-e.getY());

				scrollRectToVisible( r );
			}
		}
		else if (mode == MODE_MOVEALL && dragpoint!=null) {

                    Country[] countries = myMap.getCountries();

                    if (countryPositions == null) {
			int xdif = point.x - dragpoint.x;
			int ydif = point.y - dragpoint.y;
			for(int i = 0; i < countries.length; i++) {
				countries[i].setX( countries[i].getX()+xdif );
				countries[i].setY( countries[i].getY()+ydif );
			}
			dragpoint = point;
                    }
                    else {
                        double xdif = point.x / (double) dragpoint.x;
                        double ydif = point.y / (double) dragpoint.y;
                        double dif = Math.max(xdif, ydif);
                        for (int i = 0; i < countries.length; i++) {
				countries[i].setX( (int)Math.round(countryPositions[i].getX() * dif) );
				countries[i].setY( (int)Math.round(countryPositions[i].getY() * dif) );
			}
                    }
                    editor.setMapChanged(true);
                    repaint();
		}
		else if (mode == MODE_DRAW && dragpoint!=null) {

			Point end = point;

			if (
				( (e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) ||
				( (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK)
			) {
                                boolean draw = selected != null && (e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK;
				drawLine(dragpoint, end, draw);

                                if (draw) {
                                    repaint((int)(zoom * drawRect.x - 3), (int)(zoom * drawRect.y - 3), (int)(zoom * drawRect.width + 6), (int)(zoom * drawRect.height + 6));
                                }
                                else {
                                     // Only repaint the area where we have drawn something (or the draw is very slow on OS X)
                                    int halfBrush = brush / 2;
                                    Rectangle rect = new Rectangle(dragpoint.x - halfBrush, dragpoint.y - halfBrush, brush, brush);
                                    rect.add(new Rectangle(end.x - halfBrush, end.y - halfBrush, brush, brush));
                                    rect.grow(3, 3);
                                    // multiply by zoom to go from image coordinates to screen coordinates
                                    repaint((int)(zoom * rect.x), (int)(zoom * rect.y), (int)(zoom * rect.width), (int)(zoom * rect.height));
                                }
			}

			dragpoint = end;
		}
	    }
	}

        public void mouseExited(MouseEvent e) {
		if (mode == MODE_DRAW) {
			if (

				( (e.getModifiers() & MouseEvent.BUTTON1_MASK) != MouseEvent.BUTTON1_MASK) &&
				( (e.getModifiers() & MouseEvent.BUTTON3_MASK) != MouseEvent.BUTTON3_MASK)

			) {
				dragpoint = null;
				repaint();
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (mode == MODE_DRAW) {
			dragpoint = getPointOnImage(e);
			repaint();
		}
	}

        public void mouseEntered(MouseEvent e) {}
}
